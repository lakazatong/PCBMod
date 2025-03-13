package net.lakazatong.pcbmod.mixins;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.file.Path;
import java.util.Optional;

@Mixin(StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin {

    @Unique
    private Path nbtPath = null;

    @Inject(method = "saveTemplate", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/NbtCompound;Ljava/io/OutputStream;)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void captureNbtCompound(Identifier id, CallbackInfoReturnable<Boolean> cir, Optional<StructureTemplate> optional, StructureTemplate structureTemplate, Path path, Path path2, NbtCompound nbtCompound) {
        this.nbtPath = path;
    }

    @Inject(method = "saveTemplate", at = @At("RETURN"))
    private void onSaveTemplate(Identifier id, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || nbtPath == null) return;

        System.out.println("NEW STRUCTURE AT " + nbtPath);

//        Circuits circuits = Circuits.getServerState(server);
//        circuits.totalDirtBlocksBroken += 1;
//
//        ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(player.getUuid());
//        server.execute(() -> {
//            assert playerEntity != null;
//            ServerPlayNetworking.send(playerEntity, new NewCircuitPayload(nbtPath.toAbsolutePath().toString()));
//        });
    }
}
