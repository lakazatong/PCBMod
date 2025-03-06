package in.lakazatong.pcbmod.redstone;

import java.util.*;

abstract public class Block {
    // static properties

    public final Set<Vec3> facings = new HashSet<>();
    public final BlockType type;
    public final Structure structure;
    public final UUID uuid;
    public boolean onWall = false;
    public int delay = 0;

    // dynamic properties

    public int signal = 0;
    public int previousSignal = 0;
    // the following two could be changed with pistons
    public Vec3 coords;
    public final Set<Block> inputs = new HashSet<>();
    // for temporary caching in Circuit::remove0TickNodes, should not be used
    public final Set<Block> outputs = new HashSet<>();

    public Block(BlockType type, Vec3 coords, Structure structure) {
        this.type = type;
        this.coords = coords;
        this.structure = structure;
        this.uuid = UUID.nameUUIDFromBytes(coords.toString().getBytes());
    }

    public static class BlockBuilder {
        @FunctionalInterface
        public interface BlockConstructor {
            Block apply(Vec3 coords, Structure structure);
        }

        private final BlockConstructor cons;
        private final Map<String, Object> props = new HashMap<>();

        public BlockBuilder(BlockConstructor cons) {
            this.cons = cons;
            props.put(key, value);
            return this;
        }

        public Block apply(Vec3 coords, Structure structure) {
            return cons.apply(coords, structure).withProps(props);
        }
    }

    protected void initProps(Map<String, Object> props) {
    }

    public Block withProps(Map<String, Object> props) {
        initProps(props);
        return this;
    }

    public boolean isAbove(Block other) {
        return this.coords.y() > other.coords.y();
    }

    public boolean isBelow(Block other) {
        return this.coords.y() < other.coords.y();
    }

    public boolean isOnWallOf(Block other) {
        // This checks if the block is on a wall relative to the other block
        // This can be interpreted as being "on the wall" and facing away from it
        return this.onWall && this.isFacingAway(other);
    }

    public boolean isFacing(Block other) {
        for (Vec3 facing : this.facings) {
            if (this.coords.add(facing).equals(other.coords))
                return true;
        }
        return false;
    }

    public boolean isFacingAway(Block other) {
        // Check if this block is facing away from the given neighbor
        for (Vec3 facing : this.facings) {
            if (this.coords.subtract(facing).equals(other.coords))
                return true;
        }
        return false;
    }

    abstract public boolean isInputOf(Block neighbor);

    public int tick(double t) {
        previousSignal = signal;
        return logic(t);
    }

    abstract public int logic(double t);

    public boolean hasChanged() {
        return signal != previousSignal;
    }
}