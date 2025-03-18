package net.lakazatong.pcbmod.redstone.circuit;

import net.lakazatong.pcbmod.Utils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.lakazatong.pcbmod.PCBMod.MOD_ID;
import static net.lakazatong.pcbmod.PCBMod.STRUCTURES_PATH;

public final class Circuits extends PersistentState implements Map<String, Circuit> {

    private static final PersistentState.Type<Circuits> TYPE = new Type<>(Circuits::new, Circuits::createFromNbt, null);
    private static final String KEY = MOD_ID + ".circuits";

    private Circuits() {
    }

    private final Map<String, Circuit> circuits = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        forEach((key, value) -> nbt.put(key, value.save()));
        return nbt;
    }

    private static Circuits createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        Circuits r = new Circuits();
        for (String circuitName : tag.getKeys()) {
            try {
                NbtElement circuitData = tag.get(circuitName);
                String structureName = Utils.structureNameFrom(circuitName);
                Path structurePath = STRUCTURES_PATH.resolve(structureName + ".nbt");
                Structure structure = new Structure(structurePath);
                Circuit circuit = Circuit.load(circuitData, structure);
                r.put(circuitName, circuit);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return r;
    }

    public static Circuits init(MinecraftServer server) {
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        assert serverWorld != null;
        return serverWorld.getPersistentStateManager().getOrCreate(TYPE, KEY);
    }

    @Override
    public int size() {
        return circuits.size();
    }

    @Override
    public boolean isEmpty() {
        return circuits.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return circuits.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return circuits.containsValue(value);
    }

    @Override
    public Circuit get(Object key) {
        markDirty();
        return circuits.get(key);
    }

    @Override
    public @Nullable Circuit put(String key, Circuit value) {
        markDirty();
        return circuits.put(key, value);
    }

    @Override
    public Circuit remove(Object key) {
        markDirty();
        return circuits.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends Circuit> m) {
        markDirty();
        circuits.putAll(m);
    }

    @Override
    public void clear() {
        markDirty();
        circuits.clear();
    }

    @Override
    public @NotNull Set<String> keySet() {
        return circuits.keySet();
    }

    @Override
    public @NotNull Collection<Circuit> values() {
        markDirty();
        return circuits.values();
    }

    @Override
    public @NotNull Set<Entry<String, Circuit>> entrySet() {
        markDirty();
        return circuits.entrySet();
    }
}
