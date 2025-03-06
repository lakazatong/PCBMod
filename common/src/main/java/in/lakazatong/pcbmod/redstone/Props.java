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

    public Set<Vec3> facings; // button, comparator, dust, repeater, torch
    public int signal; // all
    public boolean hardPowered; // solid
    // the following two could be changed with pistons
    public Vec3 coords;
    public Set<Block> inputs;

    private Props(int delay, boolean onWall, boolean locked, boolean subtract, Set<Vec3> facings, int signal, boolean hardPowered, Vec3 coords, Set<Block> inputs) {
        this.delay = delay;
        this.onWall = onWall;
        this.locked = locked;
        this.subtract = subtract;
        this.facings = facings;
        this.signal = signal;
        this.hardPowered = hardPowered;
        this.coords = coords;
        this.inputs = inputs;
    }

    public static Props defaults() {
        return new Props(0, false, false, false, new HashSet<>(), 0, false, new Vec3(0, 0, 0), new HashSet<>());
    }

    public Props dup() {
        return new Props(
            this.delay, this.onWall, this.locked, this.subtract,
            new HashSet<>(this.facings), this.signal, this.hardPowered, this.coords.dup(), new HashSet<>(this.inputs)
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
                    hardPowered == other.hardPowered &&
                    Objects.equals(facings, other.facings) &&
                    Objects.equals(coords, other.coords) &&
                    Objects.equals(inputs, other.inputs);
        }
        return false;
    }
}
