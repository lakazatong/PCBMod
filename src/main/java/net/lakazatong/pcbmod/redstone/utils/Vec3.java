package net.lakazatong.pcbmod.redstone.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashSet;
import java.util.Set;

public record Vec3(int x, int y, int z) {

    public static Vec3 NORTH = new Vec3(0, 0, -1);
    public static Vec3 SOUTH = new Vec3(0, 0, 1);
    public static Vec3 WEST = new Vec3(-1, 0, 0);
    public static Vec3 EAST = new Vec3(1, 0, 0);
    public static Vec3 UP = new Vec3(0, 1, 0);
    public static Vec3 DOWN = new Vec3(0, -1, 0);

    public boolean pure() {
        return (x != 0 && y == 0 && z == 0) ||
                (x == 0 && y != 0 && z == 0) ||
                (x == 0 && y == 0 && z != 0);
    }

    public Vec3 flatten() {
        return y != 0 && (x != 0 || z != 0) ? new Vec3(x, 0, z) : this;
    }

    public Vec3 above() {
        return new Vec3(x, y + 1, z);
    }

    public Vec3 below() {
        return new Vec3(x, y - 1, z);
    }

    public Set<Vec3> around() {
        return Set.of(
                new Vec3(x + 1, y, z),
                new Vec3(x - 1, y, z),
                new Vec3(x, y, z + 1),
                new Vec3(x, y, z - 1)
        );
    }

    public Set<Vec3> neighbors() {
        Set<Vec3> r = new HashSet<>(around());
        r.add(above());
        r.add(below());
        return r;
    }

    public Vec3 opposite() {
        return new Vec3(-x, -y, -z);
    }

    public Vec3 clockwise() {
        Vec3 ref = referenceAxis();
        return ref.cross(this);
    }

    public Vec3 counterClockwise() {
        Vec3 ref = referenceAxis();
        return cross(ref);
    }

    public Vec3 referenceAxis() {
        if (y == 0)
            return new Vec3(0, 1, 0);
        if (x == 0)
            return new Vec3(1, 0, 0);
        if (z == 0)
            return new Vec3(0, 0, 1);
        throw new IllegalStateException();
    }

    public Vec3 cross(Vec3 v) {
        return new Vec3(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
    }

    public Vec3 add(Vec3 other) {
        return new Vec3(x + other.x, y + other.y, z + other.z);
    }

    public Vec3 subtract(Vec3 other) {
        return new Vec3(x - other.x, y - other.y, z - other.z);
    }

    public static Vec3 fromCardinal(String cardinal) {
        return switch (cardinal) {
            case "north" -> NORTH.dup();
            case "south" -> SOUTH.dup();
            case "west" -> WEST.dup();
            case "east" -> EAST.dup();
            case "up" -> UP.dup();
            case "down" -> DOWN.dup();
            default -> null;
        };
    }

    @Override
    public String toString() {
        return x + " " + y + " " + z;
    }

    @Override
    public int hashCode() {
        return x * 255 * 255 + y * 255 + z;
    }

    public static Vec3 fromHash(int hash) {
        int z = hash % 255;
        int y = (hash / 255) % 255;
        int x = hash / (255 * 255);
        return new Vec3(x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Vec3(int x1, int y1, int z1)) {
            return x == x1 && y == y1 && z == z1;
        }
        return false;
    }

    public Vec3 dup() {
        return new Vec3(x, y, z);
    }

    public boolean isPerpendicular(Vec3 other) {
        return x * other.x + y * other.y + z * other.z == 0;
    }

    // align the direction given that I face this way
    public Vec3 toRelative(Vec3 facing) {
        assert pure() && facing.pure();
        if (this == UP || this == DOWN) return this;
        if (facing == NORTH) return this;
        if (facing == SOUTH) return opposite();
        if (facing == WEST) return clockwise();
        if (facing == EAST) return counterClockwise();
        return this;
    }

    public static Vec3 fromMinecraft(Direction mDirection) {
        return switch (mDirection) {
            case NORTH -> NORTH.dup();
            case SOUTH -> SOUTH.dup();
            case WEST -> WEST.dup();
            case EAST -> EAST.dup();
            case UP -> UP.dup();
            case DOWN -> DOWN.dup();
        };
    }

    public double norm() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static Vec3 fromBlockDiff(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        return new Vec3(dx, dy, dz);
    }
}
