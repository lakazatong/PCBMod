package net.lakazatong.pcbmod.mixins;

import net.lakazatong.pcbmod.PCBMod;
import net.lakazatong.pcbmod.redstone.circuit.Circuit;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.ScheduledTickView;
import net.minecraft.world.tick.TickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements ScheduledTickView {

    @Shadow
    public abstract TickManager getTickManager();

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void tickCircuits(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (!this.getTickManager().shouldTick())
            return;
        ServerWorld self = (ServerWorld)(Object)this;
        for (Circuit circuit : PCBMod.CIRCUITS.values()) {
            for (BlockPos pos : circuit.hubs)
                this.scheduleBlockTick(pos, self.getBlockState(pos).getBlock(), 1);
//            circuit.setCurrentServerWorld((ServerWorld)(Object)this);
//            circuit.step();
        }
    }
}