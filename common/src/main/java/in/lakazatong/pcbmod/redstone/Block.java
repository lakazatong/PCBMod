package in.lakazatong.pcbmod.redstone;

import in.lakazatong.pcbmod.utils.Vec3;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

abstract public class Block {
    // fixed properties

    public final BlockType type;
    public final Structure structure;
    public final UUID uuid;

    // editable properties

    // makes more sense to be here as it's managed within the same tick
    public boolean dirty = true;
    // holds all states that can changes over time (and some more for convenience)
    public Props props;

    // temporary properties

    // used in Block::hasChanged
    public Props previousProps = Props.defaults();

    protected Block(BlockType type, Structure structure, Props initialProps) {
        this.type = type;
        this.structure = structure;
        this.props = initialProps;

        this.uuid = UUID.nameUUIDFromBytes(this.props.coords.toString().getBytes());
    }

    public void init() {
    }

    @FunctionalInterface
    public interface LogicImpl {
        void apply(long t, Props p);
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

        public Block apply(Structure structure, Vec3 coords) {
            Props initialProps = commonInitialProps.dup();
            initialProps.coords = coords;
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
        // This checks if the block is on a wall relative to the other block
        // This can be interpreted as being "on the wall" and facing away from it
        return this.onWall() && this.isFacingAway(other);
    }

    public boolean isFacing(Block other) {
        for (Vec3 facing : facings()) {
            if (this.coords().add(facing).equals(other.coords()))
                return true;
        }
        return false;
    }

    public boolean isFacingAway(Block other) {
        // Check if this block is facing away from the given neighbor
        for (Vec3 facing : facings()) {
            if (this.coords().subtract(facing).equals(other.coords()))
                return true;
        }
        return false;
    }

    abstract public boolean isInputOf(Block neighbor);

    public boolean isSideInputOf(Block neighbor) {
        List<Vec3> neighborFacings = neighbor.facings().stream().toList();
        return this.facings().stream().allMatch(f -> neighborFacings.stream().allMatch(f::isPerpendicular));
    }

    public Props tick(long t) {
        previousProps = props.dup();
        Props p = props.dup();
        logic(t, p);

        dirty = false;
        if (!p.equals(props)) {
            outputs().forEach(output -> output.dirty = true);
            if (delay() == 0)
                dirty = true;
        }

        return p;
    }

    public abstract void logic(long t, Props p);

    public int delay() {
        return props.delay;
    }

    public boolean onWall() {
        return props.onWall;
    }

    public boolean locked() {
        return props.locked;
    }

    public boolean subtract() {
        return props.subtract;
    }

    public Set<Vec3> facings() {
        return props.facings;
    }

    public int signal() {
        return props.signal;
    }

    public boolean weakPowered() {
        return props.weakPowered;
    }

    public Vec3 coords() {
        return props.coords;
    }

    public Set<Block> neighbors() {
        return props.neighbors;
    }

    public Stream<Block> inputs() {
        return neighbors().stream().filter(neighbor -> neighbor.isInputOf(this));
    }

    public Stream<Block> outputs() {
        return neighbors().stream().filter(this::isInputOf);
    }

    public Stream<Block> sideInputs() {
        return inputs().filter(input -> input.isSideInputOf(this));
    }

    public Stream<Block> rearInputs() {
        return inputs().filter(input -> !input.isSideInputOf(this));
    }
}
