package net.lakazatong.pcbmod.redstone.circuit;

import net.lakazatong.pcbmod.block.custom.PortBlock;
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
    public Circuit circuit = null;

    public final BlockType type;
    public int uuid;

    // here as it's managed within the same tick
    public boolean dirty = false;
    // every property packed as one for convenience
    public Props props;
    public Props nextProps;

    protected Block(BlockType type, Props initialProps) {
        this.type = type;
        this.props = initialProps;

        this.uuid = this.props.coords.hashCode();
    }

    // stuff that must wait until all Blocks are initialized (within a circuit)
    // should be called at the end of the overridden init
    public void init() {
        nextProps = props.dup();
        var x = 0;
    }

    public static class BlockBuilder {
        @FunctionalInterface
        public interface BlockConstructor {
            Block apply(Props initialProps);
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
                case PORT -> Port::new;
            };
        }

        public Block apply(Vec3 coords) {
            Props initialProps = commonInitialProps.dup();
            initialProps.coords = coords;
            return cons.apply(initialProps);
        }

        public Block apply(Props initialProps) {
            return cons.apply(initialProps);
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

    public boolean isFacingHorizontally(Block other) {
        return facings().stream().anyMatch(f -> {
            Vec3 adjusted = adjustHorizontalOnly(f);
            return coords().add(adjusted).equals(other.coords());
        });
    }

    public boolean isFacingAwayHorizontally(Block other) {
        return facings().stream().anyMatch(f -> {
            Vec3 adjusted = adjustHorizontalOnly(f);
            return coords().subtract(adjusted).equals(other.coords());
        });
    }

    private Vec3 adjustHorizontalOnly(Vec3 f) {
        return (f.x() != 0 || f.z() != 0) && f.y() != 0
                ? new Vec3(f.x(), 0, f.z())
                : f;
    }

    abstract public boolean isInputOf(Block neighbor);

    public boolean isSideInputOf(Block neighbor) {
        List<Vec3> neighborFacings = neighbor.facings().stream().toList();
        return isFacing(neighbor) && (facings().isEmpty() ? Collections.singleton(neighbor.coords().subtract(coords())) : facings())
                .stream().allMatch(f -> neighborFacings.stream().allMatch(f::isPerpendicular));
    }

    public void tick() {
        Props curProps = isInstant() ? nextProps.dup() : props;
        logic();

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

    public abstract void logic();

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

    public PortBlock.PortType portType() { return props.portType; }
    public PortBlock.PortType nextPortType() { return nextProps.portType; }

    public int portNumber() { return props.portNumber; }
    public int nextPortNumber() { return nextProps.portNumber; }

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

        if (this instanceof Delayed delayed) {
            tag.putBoolean("prevShouldPowered", delayed.prevShouldPowered);
            tag.putLong("stableTime", delayed.stableTime);
        }

        return tag;
    }

    public static Block load(NbtElement tag) {
        NbtCompound t = ((NbtCompound) tag);

        int type = t.getInt("type");
        int uuid = t.getInt("uuid");
        boolean dirty = t.getBoolean("dirty");
        NbtElement props = t.get("props");
        NbtElement nextProps = t.get("nextProps");

        BlockBuilder builder = new BlockBuilder(Arrays.stream(BlockType.values()).toList().get(type));
        Block b = builder.apply(Props.load(props));
        b.uuid = uuid;
        b.dirty = dirty;
        b.nextProps = Props.load(nextProps);

        if (b instanceof Delayed delayed) {
            delayed.prevShouldPowered = t.getBoolean("prevShouldPowered");
            delayed.stableTime = t.getLong("stableTime");
        }

        return b;
    }

    public void restoreNeighbors() {
        while (!props.scheduledNeighborsRestore.isEmpty()) props.scheduledNeighborsRestore.pop().accept(circuit.graph);
        while (!nextProps.scheduledNeighborsRestore.isEmpty()) nextProps.scheduledNeighborsRestore.pop().accept(circuit.graph);
    }
}
