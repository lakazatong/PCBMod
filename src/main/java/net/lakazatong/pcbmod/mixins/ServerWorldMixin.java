package net.lakazatong.pcbmod.mixins;

import net.lakazatong.pcbmod.PCBMod;
import net.lakazatong.pcbmod.redstone.circuit.Circuit;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void tickCircuits(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        PCBMod.CIRCUITS.values().forEach(Circuit::step);
    }
}