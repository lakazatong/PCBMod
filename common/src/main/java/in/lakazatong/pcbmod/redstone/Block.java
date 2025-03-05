package in.lakazatong.pcbmod.redstone;

import java.util.*;

abstract public class Block {
    public BlockType type;
    public Vec3 coords;
    public Structure structure;
    public Map<String, Object> props;
    public UUID uuid;

    public final Set<Block> inputs;
    public final Set<Block> outputs; // for temporary caching, should not be used
    public int signal = 0;
    public int previousSignal = 0;

    public Block(BlockType type, Vec3 coords, Structure structure) {
        this.type = type;
        this.coords = coords;
        this.structure = structure;
        this.props = new HashMap<>();
        this.uuid = UUID.nameUUIDFromBytes(coords.toString().getBytes());

        this.inputs = new HashSet<>();
        this.outputs = new HashSet<>();
    }

    @FunctionalInterface
    public interface BlockBuilder {
        Block apply(Vec3 coords, Structure Structure);
    }

    public Block withProps(Map<String, Object> props) {
        this.props = props;
        return this;
    }

    public boolean isAbove(Block other) {
        return this.coords.y > other.coords.y;
    }

    public boolean isBelow(Block other) {
        return this.coords.y < other.coords.y;
    }

    public boolean isOnWall() {
        return this.props.getOrDefault("on_wall", false).equals(true);
    }

    public boolean isOnWallOf(Block other) {
        // This checks if the block is on a wall relative to the other block
        // This can be interpreted as being "on the wall" and facing away from it
        return this.isOnWall() && this.isFacingAway(other);
    }

    @SuppressWarnings("unchecked")
    public boolean isFacing(Block other) {
        Object facings = this.props.get("facings");
        for (Vec3 facing : (Iterable<Vec3>) facings) {
            if (this.coords.add(facing).equals(other.coords)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean isFacingAway(Block other) {
        // Check if this block is facing away from the given neighbor
        Object facings = this.props.get("facings");
        if (facings == null) {
            return false;
        }
        for (Vec3 facing : (Iterable<Vec3>) facings) {
            if (this.coords.subtract(facing).equals(other.coords)) {
                return true;
            }
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