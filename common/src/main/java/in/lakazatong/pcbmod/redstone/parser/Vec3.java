package in.lakazatong.pcbmod.redstone.parser;

import java.util.ArrayList;
import java.util.List;

public class Vec3 {
    public int x;
    public int y;
    public int z;

    public Vec3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3 above() {
        return new Vec3(x, y + 1, z);
    }

    public Vec3 below() {
        return new Vec3(x, y - 1, z);
    }

    public List<Vec3> around() {
        return List.of(
                new Vec3(x + 1, y, z),
                new Vec3(x - 1, y, z),
                new Vec3(x, y, z + 1),
                new Vec3(x, y, z - 1)
        );
    }

    public List<Vec3> neighbors() {
        List<Vec3> r = new ArrayList<>(around());
        r.add(above());
        r.add(below());
        return r;
    }

    public Vec3 opposite() {
        return new Vec3(-x, -y, -z);
    }

    public Vec3 add(Vec3 other) {
        return new Vec3(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vec3 subtract(Vec3 other) {
        return new Vec3(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public static Vec3 fromCardinal(String cardinal) {
        return switch (cardinal) {
            case "east" -> new Vec3(1, 0, 0);
            case "west" -> new Vec3(-1, 0, 0);
            case "south" -> new Vec3(0, 0, 1);
            case "north" -> new Vec3(0, 0, -1);
            case "up" -> new Vec3(0, 1, 0);
            case "down" -> new Vec3(0, -1, 0);
            default -> null;
        };
    }

    @Override
    public String toString() {
        return this.x + " " + this.y + " " + this.z;
    }
}