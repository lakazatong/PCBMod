package net.lakazatong.pcbmod.mixins;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin {

    @Inject(method = "saveTemplate", at = @At(value = "RETURN"), cancellable = true)
    private void onSaveTemplate(Identifier id, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            StructureTemplateManager instance = (StructureTemplateManager) (Object) this;
            Optional<StructureTemplate> optional = instance.templates.get(id);
            if (optional.isPresent()) {
                StructureTemplate structureTemplate = optional.get();
                NbtCompound nbtCompound = structureTemplate.writeNbt(new NbtCompound());

                modifyNbt(nbtCompound);
            }
        }
    }

    private void modifyNbt(NbtCompound nbt) {
        // Modify NBT data here
    }
}