package net.lakazatong.pcbmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lakazatong.pcbmod.PCBMod;
import net.lakazatong.pcbmod.block.custom.PortBlock.PortType;
import net.lakazatong.pcbmod.block.entity.HubBlockEntity;
import net.lakazatong.pcbmod.payloads.OpenHubScreenPayload;
import net.lakazatong.pcbmod.redstone.circuit.Circuit;
import net.lakazatong.pcbmod.redstone.utils.Vec3;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import javax.annotation.Nullable;

public class HubBlock extends HorizontalFacingBlock implements BlockEntityProvider {

    public static final EnumProperty<PortType> FRONT_TYPE = EnumProperty.of("front_type", PortType.class);
    public static final EnumProperty<PortType> BACK_TYPE = EnumProperty.of("back_type", PortType.class);
    public static final EnumProperty<PortType> LEFT_TYPE = EnumProperty.of("left_type", PortType.class);
    public static final EnumProperty<PortType> RIGHT_TYPE = EnumProperty.of("right_type", PortType.class);
    public static final EnumProperty<PortType> UP_TYPE = EnumProperty.of("up_type", PortType.class);
    public static final EnumProperty<PortType> DOWN_TYPE = EnumProperty.of("down_type", PortType.class);

    @SuppressWarnings("unchecked")
    public static final EnumProperty<PortType>[] SIDE_TYPES = (EnumProperty<PortType>[]) new EnumProperty<?>[]{
        FRONT_TYPE, BACK_TYPE,
        LEFT_TYPE, RIGHT_TYPE,
        UP_TYPE, DOWN_TYPE,
    };

    public HubBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
                .with(FRONT_TYPE, PortType.CLOSE).with(BACK_TYPE, PortType.CLOSE)
                .with(LEFT_TYPE, PortType.CLOSE).with(RIGHT_TYPE, PortType.CLOSE)
                .with(UP_TYPE, PortType.CLOSE).with(DOWN_TYPE, PortType.CLOSE)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING)
            .add(FRONT_TYPE).add(BACK_TYPE)
            .add(LEFT_TYPE).add(RIGHT_TYPE)
            .add(UP_TYPE).add(DOWN_TYPE);
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if (state == null)
            state = getDefaultState();
        return state.with(FACING, ctx.getHorizontalPlayerFacing());
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld)
            return ActionResult.PASS;

        if (!(world.getBlockEntity(pos) instanceof HubBlockEntity hubBlockEntity)) {
            return super.onUse(state, world, pos, player, hit);
        }

        MinecraftServer server = world.getServer();
        if (server != null) {
            ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(player.getUuid());
            server.execute(() -> {
                assert playerEntity != null;
                ServerPlayNetworking.send(playerEntity, new OpenHubScreenPayload(pos));
            });
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return createCodec(HubBlock::new);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HubBlockEntity(pos, state);
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && world.getBlockEntity(pos) instanceof HubBlockEntity be) {
            String circuitName = be.getCircuitName();
            Circuit circuit = PCBMod.CIRCUITS.get(circuitName);
            if (circuit != null) {
                circuit.hubs.remove(pos);
                if (circuit.hubs.isEmpty())
                    PCBMod.CIRCUITS.remove(circuitName);
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    //    @Override
//    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @org.jetbrains.annotations.Nullable BlockEntity blockEntity, ItemStack tool) {
//
//        super.afterBreak(world, player, pos, state, blockEntity, tool);
//    }

//    @Override
//    protected void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
//
//    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (world.getBlockEntity(pos) instanceof HubBlockEntity be) {
            Circuit circuit = PCBMod.CIRCUITS.get(be.getCircuitName());
            if (circuit == null) return 0;
            int portNumber = be.getPortNumberAt(Side.fromDirection(Vec3.fromMinecraft(direction.getOpposite()).toRelative(Vec3.fromMinecraft(state.get(FACING)))).ordinal());
            int signal = circuit.getSignalOfPortNumber(portNumber);
            return signal;
        }
        return 0;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (world.getBlockEntity(pos) instanceof HubBlockEntity be) {
            Circuit circuit = PCBMod.CIRCUITS.get(be.getCircuitName());
            if (circuit == null) return state;
            Vec3 facing = Vec3.fromMinecraft(state.get(FACING)); // 1, 0, 0
            Vec3 aligned = Vec3.fromMinecraft(direction).toRelative(facing); // -1, 0, 0
            int side = Side.fromDirection(aligned).ordinal(); // 2
            int portNumber = be.getPortNumberAt(side); // 1
            BlockPos offset = pos.offset(direction); // -10 56 13
            int signal = world.getBlockState(offset).getStrongRedstonePower(world, offset, direction.getOpposite()); // 15
            circuit.setSignalOfPortNumber(portNumber, signal);
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    public enum Side implements StringIdentifiable {
        FRONT, BACK,
        LEFT, RIGHT,
        UP, DOWN,
        NONE;

        private final int color;

        Side() {
            this.color = colorAt(this.ordinal());
        }

        public static int colorAt(int side) {
            return switch (side) {
                case 0 -> 0xFF0000;  // Red
                case 1 -> 0x00FF00;  // Green
                case 2 -> 0x0000FF;  // Blue
                case 3 -> 0xFFFF00;  // Yellow
                case 4 -> 0xFF00FF;  // Magenta
                case 5 -> 0x00FFFF;  // Cyan
                default -> 0xFFFFFF; // White
            };
        }

        public int color() {
            return color;
        }

        @Override
        public String asString() {
            return name().toLowerCase();
        }

        public Side next() {
            return values()[(this.ordinal() + 1) % values().length];
        }

        public static Side fromDirection(Vec3 dir) {
            if (dir.equals(Vec3.NORTH)) return Side.FRONT;
            if (dir.equals(Vec3.SOUTH)) return Side.BACK;
            if (dir.equals(Vec3.WEST)) return Side.LEFT;
            if (dir.equals(Vec3.EAST)) return Side.RIGHT;
            if (dir.equals(Vec3.UP)) return Side.UP;
            if (dir.equals(Vec3.DOWN)) return Side.DOWN;
            throw new IllegalArgumentException();
        }
    }
}
