package in.lakazatong.pcbmod.redstone;

import in.lakazatong.pcbmod.utils.Vec3;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Props {
    // static properties

    public int delay; // all
    public boolean onWall; // button, torch
    public boolean locked; // repeater (might be useless)
    public boolean subtract; // comparator (might be useless)

    // dynamic properties

    public Set<Vec3> facings; // button, delayed, dust
    public int signal; // all
    public boolean weakPowered; // solid
    // the following two could be changed with pistons
    public Vec3 coords;
    public Set<Block> neighbors;

    private Props(int delay, boolean onWall, boolean locked, boolean subtract, Set<Vec3> facings, int signal, boolean weakPowered, Vec3 coords, Set<Block> neighbors) {
        this.delay = delay;
        this.onWall = onWall;
        this.locked = locked;
        this.subtract = subtract;
        this.facings = facings;
        this.signal = signal;
        this.weakPowered = weakPowered;
        this.coords = coords;
        this.neighbors = neighbors;
    }

    public static Props defaults() {
        return new Props(0, false, false, false, new HashSet<>(), 0, false, new Vec3(0, 0, 0), new HashSet<>());
    }

    public Props dup() {
        return new Props(
            delay, onWall, locked, subtract,
            new HashSet<>(facings), signal, weakPowered, coords.dup(), new HashSet<>(neighbors)
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Props other) {
            return delay == other.delay &&
                    onWall == other.onWall &&
                    locked == other.locked &&
                    subtract == other.subtract &&
                    signal == other.signal &&
                    weakPowered == other.weakPowered &&
                    Objects.equals(facings, other.facings) &&
                    Objects.equals(coords, other.coords) &&
                    Objects.equals(neighbors, other.neighbors);
        }
        return false;
    }
}
