package net.lakazatong.pcbmod.redstone.circuit;

import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.redstone.utils.Vec3;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Props {
    public long delay;
    public boolean onWall; // button, torch
    public boolean locked; // repeater
    public boolean subtract; // comparator
    public Set<Vec3> facings; // button, delayed, dust
    public int signal;
    public boolean weakPowered; // solid
    public Vec3 coords;
    public Set<Block> neighbors;
    public PortBlock.PortType portType; // port
    public int portNumber; // port

    private Props(long delay, boolean onWall, boolean locked, boolean subtract, Set<Vec3> facings, int signal, boolean weakPowered, Vec3 coords, Set<Block> neighbors, PortBlock.PortType portType, int portNumber) {
        this.delay = delay;
        this.onWall = onWall;
        this.locked = locked;
        this.subtract = subtract;
        this.facings = facings;
        this.signal = signal;
        this.weakPowered = weakPowered;
        this.coords = coords;
        this.neighbors = neighbors;
        this.portType = portType;
        this.portNumber = portNumber;
    }

    public static Props defaults() {
        return new Props(0, false, false, false, new HashSet<>(), 0, false, new Vec3(0, 0, 0), new HashSet<>(), PortBlock.PortType.CLOSE, 0);
    }

    public Props dup() {
        return new Props(
            delay, onWall, locked, subtract,
            new HashSet<>(facings), signal, weakPowered, coords.dup(), new HashSet<>(neighbors),
            portType, portNumber
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
                    Objects.equals(neighbors, other.neighbors) &&
                    portType.equals(other.portType) &&
                    portNumber == other.portNumber;
        }
        return false;
    }

    public NbtCompound save() {
        NbtCompound tag = new NbtCompound();

        tag.putLong("delay", delay);
        tag.putBoolean("onWall", onWall);
        tag.putBoolean("locked", locked);
        tag.putBoolean("subtract", subtract);

        tag.put("facings", new NbtIntArray(facings.stream().map(Vec3::hashCode).toList()));

        tag.putInt("signal", signal);
        tag.putBoolean("weakPowered", weakPowered);
        tag.putInt("coords", coords.hashCode());

        tag.put("neighbors", new NbtIntArray(neighbors.stream().map(neighbor -> neighbor.uuid).toList()));

        tag.putInt("portType", portType.ordinal());
        tag.putInt("portNumber", portNumber);

        return tag;
    }

    public static Props load(NbtElement tag, Structure structure) {
        NbtCompound t = ((NbtCompound) tag);
        Props props =  Props.defaults();

        props.delay = t.getLong("delay");
        props.onWall = t.getBoolean("onWall");
        props.locked = t.getBoolean("locked");
        props.subtract = t.getBoolean("subtract");

        props.facings = new HashSet<>();
        for (int facing : t.getIntArray("facings"))
            props.facings.add(Vec3.fromHash(facing));

        props.signal = t.getInt("signal");
        props.weakPowered = t.getBoolean("weakPowered");
        props.coords = Vec3.fromHash(t.getInt("coords"));

        props.neighbors = new HashSet<>();
        for (int neighbor : t.getIntArray("neighbors"))
            props.neighbors.add(structure.getBlock(Vec3.fromHash(neighbor)));

        props.portType = PortBlock.PortType.of(t.getInt("portType"));
        props.portNumber = t.getInt("portNumber");

        return props;
    }
}
