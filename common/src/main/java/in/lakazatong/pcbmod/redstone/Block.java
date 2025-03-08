package in.lakazatong.pcbmod.redstone;

import in.lakazatong.pcbmod.redstone.blocks.Delayed;
import in.lakazatong.pcbmod.utils.Vec3;

import java.util.Collections;
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
    // here so that 0 tick blocks can "see in the future" and only them
    public Props nextProps;

    protected Block(BlockType type, Structure structure, Props initialProps) {
        this.type = type;
        this.structure = structure;
        this.props = initialProps;

        this.uuid = UUID.nameUUIDFromBytes(this.props.coords.toString().getBytes());
    }

    // stuff that must wait until all Blocks are initialized (once in a circuit)
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
        return facings().stream().anyMatch(f -> this.coords().add(f).equals(other.coords()));
    }

    public boolean isFacingAway(Block other) {
        return facings().stream().anyMatch(f -> this.coords().subtract(f).equals(other.coords()));
    }

    abstract public boolean isInputOf(Block neighbor);

    public boolean isSideInputOf(Block neighbor) {
        List<Vec3> neighborFacings = neighbor.facings().stream().toList();
        return (facings().isEmpty() ? Collections.singleton(neighbor.coords().subtract(coords())) : facings())
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

    public int delay() {
        return props.delay;
    }

    public int nextDelay() {
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
        return inputs().filter(input -> !input.isSideInputOf(this) && isFacingAway(input));
    }

    public Stream<Block> nextRearInputs() {
        return nextInputs().filter(input -> !input.isSideInputOf(this) && isFacingAway(input));
    }
}
