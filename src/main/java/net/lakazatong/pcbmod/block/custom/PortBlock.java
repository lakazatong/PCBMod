package net.lakazatong.pcbmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lakazatong.pcbmod.block.entity.PortBlockEntity;
import net.lakazatong.pcbmod.payloads.OpenPortScreenPayload;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class PortBlock extends BlockWithEntity {

    public static final EnumProperty<PortType> TYPE = EnumProperty.of("type", PortType.class);
    public static final EnumProperty<Side> SIDE = EnumProperty.of("side", Side.class);

    public PortBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(TYPE, PortType.CLOSE).with(SIDE, Side.NONE));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TYPE).add(SIDE);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld)
            return ActionResult.PASS;

        if (!(world.getBlockEntity(pos) instanceof PortBlockEntity portBlockEntity)) {
            return super.onUse(state, world, pos, player, hit);
        }

//        Circuits circuits = Circuits.getServerState(server);
//        circuits.totalDirtBlocksBroken += 1;

        MinecraftServer server = world.getServer();
        if (server != null) {
            ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(player.getUuid());
            server.execute(() -> {
                assert playerEntity != null;
                ServerPlayNetworking.send(playerEntity, new OpenPortScreenPayload(pos));
            });
        }

//        player.sendMessage(Text.literal("Current port number: " + portBlockEntity.getPortNumber()), true);

//        if (player.isSneaking()) {
//            world.setBlockState(pos, state.with(SIDE, state.get(SIDE).next()));
//        } else {
//            world.setBlockState(pos, state.with(TYPE, state.get(TYPE).next()));
//        }
//
//        world.playSound(player, pos, SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.BLOCKS, 1.0F, 1.0F);

        return ActionResult.SUCCESS;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(PortBlock::new);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PortBlockEntity(pos, state);
    }

    public enum PortType implements StringIdentifiable {
        CLOSE, INPUT, OUTPUT;

        public static final List<PortType> values = Arrays.stream(PortType.values()).toList();
        public static final int count = PortType.values().length;

        @Override
        public String asString() {
            return name().toLowerCase();
        }

        public PortType next() {
            return values.get((this.ordinal() + 1) % count);
        }

        public static PortType of(int type) {
            assert type >= 0 && type < count;
            return values.get(type);
        }
    }

    public enum Side implements StringIdentifiable {
        NONE(0xFFFFFF),      // White
        FRONT(0xFF0000),     // Red
        BACK(0x00FF00),      // Green
        LEFT(0x0000FF),      // Blue
        RIGHT(0xFFFF00),     // Yellow
        UP(0xFF00FF),        // Magenta
        DOWN(0x00FFFF);      // Cyan

        private final int color;

        Side(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }

        @Override
        public String asString() {
            return name().toLowerCase();
        }

        public Side next() {
            return Arrays.stream(Side.values()).toList().get((this.ordinal() + 1) % values().length);
        }
    }
}
