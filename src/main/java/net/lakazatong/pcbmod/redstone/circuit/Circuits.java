package net.lakazatong.pcbmod.redstone.circuit;

import net.lakazatong.pcbmod.PCBMod;
import net.minecraft.nbt.NbtCompound;
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

public class Circuits extends PersistentState implements Map<String, Circuit> {

    public final Map<String, Circuit> circuits;

    public Circuits() {
        circuits = new HashMap<>();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound tag = new NbtCompound();

        this.forEach((key, value) -> tag.put(key, value.save()));

        nbt.put("circuits", tag);

        return nbt;
    }

    public static Circuits createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        Circuits circuits = new Circuits();
        for (String structureName : tag.getKeys()) {
            try {
                circuits.put(structureName, Circuit.load(tag.get(structureName), new Structure(Path.of(structureName))));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return circuits;
    }

    public static Circuits createNew() {
        return new Circuits();
    }

    private static final Type<Circuits> type = new Type<>(
            Circuits::createNew,
            Circuits::createFromNbt,
            null
    );

    public static Circuits getServerState(MinecraftServer server) {
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        assert serverWorld != null;
        Circuits circuits = serverWorld.getPersistentStateManager().getOrCreate(type, PCBMod.MOD_ID);
        circuits.markDirty();
        return circuits;
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
        return circuits.get(key);
    }

    @Override
    public @Nullable Circuit put(String key, Circuit value) {
        return circuits.put(key, value);
    }

    @Override
    public Circuit remove(Object key) {
        return circuits.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends Circuit> m) {
        circuits.putAll(m);
    }

    @Override
    public void clear() {
        circuits.clear();
    }

    @Override
    public @NotNull Set<String> keySet() {
        return circuits.keySet();
    }

    @Override
    public @NotNull Collection<Circuit> values() {
        return circuits.values();
    }

    @Override
    public @NotNull Set<Entry<String, Circuit>> entrySet() {
        return circuits.entrySet();
    }
}
