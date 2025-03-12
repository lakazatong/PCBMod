package net.lakazatong.pcbmod.redstone.circuit;

import net.lakazatong.pcbmod.redstone.blocks.*;
import net.lakazatong.pcbmod.redstone.utils.Vec3;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract public class Block {
    // fixed properties

    public final BlockType type;
    public final Structure structure;
    public int uuid;

    // editable properties

    // makes more sense to be here as it's managed within the same tick
    public boolean dirty = false;
    // holds all states that can changes over time (and some more for convenience)
    public Props props;
    // here so that 0 tick blocks can "see in the future" and only them
    public Props nextProps;

    protected Block(BlockType type, Structure structure, Props initialProps) {
        this.type = type;
        this.structure = structure;
        this.props = initialProps;

        this.uuid = this.props.coords.hashCode();
    }

    // stuff that must wait until all Blocks are initialized (within a circuit)
    // should be called at the end of the overridden init
    public void init() {
        this.nextProps = props.dup();
    }

    @FunctionalInterface
    public interface LogicImpl {
        void apply(long t);
    }

    public static class BlockBuilder {
        @FunctionalInterface
        public interface BlockConstructor {
            Block apply(Structure structure, Props initialProps);
        }

        private final BlockConstructor cons;
        public final Props commonInitialProps = Props.defaults();

        public BlockBuilder(BlockConstructor cons) {
            this.cons = cons;
        }

        public BlockBuilder(BlockType blockType) {
            cons = switch (blockType) {
                case AIR -> null;
                case SOLID -> Solid::new;
                case DUST -> Dust::new;
                case REPEATER -> Repeater::new;
                case TORCH -> Torch::new;
                case COMPARATOR -> Comparator::new;
                case BUTTON -> Button::new;
                case LEVER -> Lever::new;
                case REDSTONE_BLOCK -> RedstoneBlock::new;
            };
        }

        public Block apply(Structure structure, Vec3 coords) {
            Props initialProps = commonInitialProps.dup();
            initialProps.coords = coords;
            return cons.apply(structure, initialProps);
        }

        public Block apply(Structure structure, Props initialProps) {
            return cons.apply(structure, initialProps);
        }
    }

    public boolean isAbove(Block other) {
        return this.coords().y() > other.coords().y();
    }

    public boolean isBelow(Block other) {
        return this.coords().y() < other.coords().y();
    }

    public boolean isOnWallOf(Block other) {
        return onWall() && isFacingAway(other);
    }

    public boolean isFacing(Block other) {
        return facings().stream().anyMatch(f -> coords().add(f).equals(other.coords()));
    }

    public boolean isFacingAway(Block other) {
        return facings().stream().anyMatch(f -> coords().subtract(f).equals(other.coords()));
    }

    abstract public boolean isInputOf(Block neighbor);

    public boolean isSideInputOf(Block neighbor) {
        List<Vec3> neighborFacings = neighbor.facings().stream().toList();
        return isFacing(neighbor) && (facings().isEmpty() ? Collections.singleton(neighbor.coords().subtract(coords())) : facings())
                .stream().allMatch(f -> neighborFacings.stream().allMatch(f::isPerpendicular));
    }

    public void tick(long t) {
        Props curProps = isInstant() ? nextProps.dup() : props;
        logic(t);

        dirty = false;
        if (!nextProps.equals(curProps)) {
            outputs().filter(Block::isInstant).forEach(output -> output.dirty = true);
            if (isInstant())
                dirty = true;
        }
    }

    public boolean isInstant() {
        return delay() == 0 && !(this instanceof Delayed);
    }

    public abstract void logic(long t);

    public long delay() {
        return props.delay;
    }

    public long nextDelay() {
        return nextProps.delay;
    }

    public boolean onWall() {
        return props.onWall;
    }

    public boolean nextOnWall() {
        return nextProps.onWall;
    }

    public boolean locked() {
        return props.locked;
    }

    public boolean nextLocked() {
        return nextProps.locked;
    }

    public boolean subtract() {
        return props.subtract;
    }

    public boolean nextSubtract() {
        return nextProps.subtract;
    }

    public Set<Vec3> facings() {
        return props.facings;
    }

    public Set<Vec3> nextFacings() {
        return nextProps.facings;
    }

    public int signal() {
        return props.signal;
    }

    public int nextSignal() {
        return nextProps.signal;
    }

    public boolean weakPowered() {
        return props.weakPowered;
    }

    public boolean nextWeakPowered() {
        return nextProps.weakPowered;
    }

    public Vec3 coords() {
        return props.coords;
    }

    public Vec3 nextCoords() {
        return nextProps.coords;
    }

    public Set<Block> neighbors() {
        return props.neighbors;
    }

    public Set<Block> nextNeighbors() {
        return nextProps.neighbors;
    }

    public Stream<Block> inputs() {
        return neighbors().stream().filter(neighbor -> neighbor.isInputOf(this));
    }

    public Stream<Block> nextInputs() {
        return nextNeighbors().stream().filter(neighbor -> neighbor.isInputOf(this));
    }

    public Stream<Block> outputs() {
        return neighbors().stream().filter(this::isInputOf);
    }

    public Stream<Block> nextOutputs() {
        return nextNeighbors().stream().filter(this::isInputOf);
    }

    public Stream<Block> sideInputs() {
        return inputs().filter(input -> input.isSideInputOf(this));
    }

    public Stream<Block> nextSideInputs() {
        return nextInputs().filter(input -> input.isSideInputOf(this));
    }

    public Stream<Block> frontInputs() {
        return inputs().filter(input -> !input.isSideInputOf(this) && isFacing(input));
    }

    public Stream<Block> nextFrontInputs() {
        return nextInputs().filter(input -> !input.isSideInputOf(this) && isFacing(input));
    }

    public Stream<Block> rearInputs() {
        var inputs = inputs().collect(Collectors.toSet());
        return inputs.stream().filter(input -> !input.isSideInputOf(this) && isFacingAway(input));
    }

    public Stream<Block> nextRearInputs() {
        return nextInputs().filter(input -> !input.isSideInputOf(this) && isFacingAway(input));
    }

    public NbtCompound save() {
        NbtCompound tag = new NbtCompound();

        tag.putInt("type", type.value);
        tag.putInt("uuid", uuid);
        tag.putBoolean("dirty", dirty);
        tag.put("props", props.save());
        tag.put("nextProps", nextProps.save());

        return tag;
    }

    public static Block load(NbtElement tag, Structure structure) {
        NbtCompound t = ((NbtCompound) tag);

        int type = t.getInt("type");
        int uuid = t.getInt("uuid");
        boolean dirty = t.getBoolean("dirty");
        NbtElement props = t.get("props");
        NbtElement nextProps = t.get("nextProps");

        BlockBuilder builder = new BlockBuilder(Arrays.stream(BlockType.values()).toList().get(type));
        Block b = builder.apply(structure, Props.load(props, structure));
        b.uuid = uuid;
        b.dirty = dirty;
        b.nextProps = Props.load(nextProps, structure);

        return b;
    }
}
