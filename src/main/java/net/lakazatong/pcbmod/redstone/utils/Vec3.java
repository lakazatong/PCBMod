package net.lakazatong.pcbmod.redstone.utils;

import java.util.HashSet;
import java.util.Set;

public record Vec3(int x, int y, int z) {

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

    public Vec3 add(Vec3 other) {
        return new Vec3(x + other.x, y + other.y, z + other.z);
    }

    public Vec3 add(Direction other) {
        return new Vec3(x + other.x(), y + other.y(), z + other.z());
    }

    public Vec3 subtract(Direction other) {
        return new Vec3(x - other.x(), y - other.y(), z - other.z());
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
}
