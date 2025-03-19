package net.lakazatong.pcbmod.redstone.utils;

public enum Direction {
    NORTH(0), SOUTH(1), WEST(2), EAST(3), UP(4), DOWN(5);

    public final int side;
    public boolean up = false; // only for Dust

    private static final Direction[] sideToDirection = values();
    private static final Direction[] opposites = {SOUTH, NORTH, EAST, WEST, DOWN, UP};
    private static final Direction[] clockwise = {EAST, WEST, NORTH, SOUTH, UP, DOWN};
    private static final Direction[] counterClockwise = {WEST, EAST, SOUTH, NORTH, UP, DOWN};
    private static final net.minecraft.util.math.Direction[] minecraftDirections = {
            net.minecraft.util.math.Direction.NORTH,
            net.minecraft.util.math.Direction.SOUTH,
            net.minecraft.util.math.Direction.WEST,
            net.minecraft.util.math.Direction.EAST,
            net.minecraft.util.math.Direction.UP,
            net.minecraft.util.math.Direction.DOWN
    };
    private static final Direction[] myDirections = { DOWN, UP, NORTH, SOUTH, WEST, EAST };
    private static final int[] xs = {0,0,-1,1,0,0};
    private static final int[] ys = {0,0,0,0,1,-1};
    private static final int[] zs = {-1,1,0,0,0,0};

    Direction(int side) {
        this.side = side;
    }

    public int x() {
        return xs[side];
    }

    public int y() {
        return ys[side];
    }

    public int z() {
        return zs[side];
    }

    public Direction opposite() {
        return opposites[this.side];
    }

    public Direction clockwise() {
        return clockwise[this.side];
    }

    public Direction counterClockwise() {
        return counterClockwise[this.side];
    }

    public net.minecraft.util.math.Direction toMinecraft() {
        return minecraftDirections[this.side];
    }

    // I want the FRONT, BACK, LEFT or RIGHT direction given that I face this way
    public Direction toAbsolute(Direction facing) {
        if (this == Direction.UP || this == Direction.DOWN) return this;
        if (facing == Direction.NORTH) return this;
        if (facing == Direction.SOUTH) return this.opposite();
        if (facing == Direction.EAST) return this.clockwise();
        if (facing == Direction.WEST) return this.counterClockwise();
        return this;
    }

    // align the direction given that I face this way
    public Direction toRelative(Direction facing) {
        if (this == Direction.UP || this == Direction.DOWN) return this;
        if (facing == Direction.NORTH) return this;
        if (facing == Direction.SOUTH) return this.opposite();
        if (facing == Direction.EAST) return this.counterClockwise();
        if (facing == Direction.WEST) return this.clockwise();
        return this;
    }

    public static Direction fromSide(int side) {
        return sideToDirection[side];
    }

    public static Direction fromMinecraft(net.minecraft.util.math.Direction direction) {
        return myDirections[direction.ordinal()];
    }

    public boolean isPerpendicular(Direction other) {
        return (this.side / 2) != (other.side / 2);
    }

    public static Direction fromDiff(int dx, int dy, int dz) {
        if (dx > 0) return EAST;
        if (dx < 0) return WEST;
        if (dy > 0) return UP;
        if (dy < 0) return DOWN;
        if (dz > 0) return SOUTH;
        return NORTH;
    }

    public static Direction fromBlockDiff(Vec3 from, Vec3 to) {
        return fromDiff(
                to.x() - from.x(),
                to.y() - from.y(),
                to.z() - from.z()
        );
    }

    public static Direction fromCardinal(String cardinal) {
        return switch (cardinal) {
            case "north" -> NORTH;
            case "south" -> SOUTH;
            case "west" -> WEST;
            case "east" -> EAST;
            case "up" -> UP;
            case "down" -> DOWN;
            default -> null;
        };
    }
}
