package net.lakazatong.pcbmod.mixins;

import net.lakazatong.pcbmod.PCBMod;
import net.lakazatong.pcbmod.redstone.circuit.Circuit;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//@Mixin(CommandBlockBlockEntity.class)
public class CommandBlockBlockEntityMixin {
//
//    @Inject(method = "scheduleAutoTick", at = @At(value = "TAIL"))
//    private void tickCircuits(CallbackInfo ci) {
//        World world = ((BlockEntity)(Object)this).getWorld();
//        if (world == null)
//            return;
//        for (Circuit circuit : PCBMod.CIRCUITS.values()) {
//            for (BlockPos pos : circuit.hubs)
//                world.scheduleBlockTick(pos, world.getBlockState(pos).getBlock(), 1);
//        }
//
//    }
}
