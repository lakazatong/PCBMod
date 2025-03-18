package net.lakazatong.pcbmod.mixins;
//
//import net.lakazatong.pcbmod.block.entity.PortBlockEntity;
//import net.minecraft.block.entity.StructureBlockBlockEntity;
//import net.minecraft.nbt.NbtCompound;
//import net.minecraft.nbt.NbtDouble;
//import net.minecraft.nbt.NbtIo;
//import net.minecraft.nbt.NbtList;
//import net.minecraft.server.world.ServerWorld;
//import net.minecraft.structure.StructureTemplate;
//import net.minecraft.structure.StructureTemplateManager;
//import net.minecraft.util.Identifier;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.Vec3i;
//import org.jetbrains.annotations.Nullable;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
//
//import java.io.FileOutputStream;
//import java.io.OutputStream;
//import java.nio.file.Path;
//
//@Mixin(StructureBlockBlockEntity.class)
public abstract class StructureBlockBlockEntityMixin {
//
//    @Shadow private @Nullable Identifier templateName;
//    @Shadow private Vec3i size;
//
//    @Shadow private float integrity;
//    @Unique
//    BlockPos start;
//    @Unique
//    ServerWorld world;
//    @Unique
//    StructureTemplateManager manager;
//    @Unique
//    StructureTemplate template;
//
//    @Inject(method = "saveStructure(Z)Z",
//            at = @At(value = "INVOKE", target = "net/minecraft/structure/StructureTemplateManager.saveTemplate (Lnet/minecraft/util/Identifier;)Z"),
//            locals = LocalCapture.CAPTURE_FAILSOFT)
//    private void captureTemplate(boolean interactive, CallbackInfoReturnable<Boolean> cir,
//                                 BlockPos blockPos,
//                                 ServerWorld serverWorld,
//                                 StructureTemplateManager structureTemplateManager,
//                                 StructureTemplate structureTemplate) {
//        start = blockPos;
//        world = serverWorld;
//        manager = structureTemplateManager;
//        template = structureTemplate;
//    }
//
//    @Inject(method = "saveStructure(Z)Z", at = @At("RETURN"))
//    private void onSaveStructure(boolean interactive, CallbackInfoReturnable<Boolean> cir) {
//        if (!cir.getReturnValue()) return;
//
//        Path nbtPath = manager.getTemplatePath(this.templateName, ".nbt");
//
//        BlockPos blockPos = start.add(this.size).add(-1, -1, -1);
//        BlockPos blockPos2 = new BlockPos(Math.min(start.getX(), blockPos.getX()), Math.min(start.getY(), blockPos.getY()), Math.min(start.getZ(), blockPos.getZ()));
//        BlockPos blockPos3 = new BlockPos(Math.max(start.getX(), blockPos.getX()), Math.max(start.getY(), blockPos.getY()), Math.max(start.getZ(), blockPos.getZ()));
//
//        NbtCompound nbtCompound = template.writeNbt(new NbtCompound());
//        NbtList portNumbers = new NbtList();
//
//        for (BlockPos pos : BlockPos.iterate(blockPos2, blockPos3)) {
//            if (world.getBlockEntity(pos) instanceof PortBlockEntity be) {
//                BlockPos relative = pos.subtract(blockPos2);
//
//                NbtCompound portNumberInfo = new NbtCompound();
//
//                NbtList relativeCompound = new NbtList();
//                relativeCompound.add(NbtDouble.of(relative.getX()));
//                relativeCompound.add(NbtDouble.of(relative.getY()));
//                relativeCompound.add(NbtDouble.of(relative.getZ()));
//
//                portNumberInfo.put("pos", relativeCompound);
//                portNumberInfo.putInt("portNumber", be.getPortNumber());
//
//                portNumbers.add(portNumberInfo);
//            }
//        }
//
//        nbtCompound.put("portNumbers", portNumbers);
//
//        try {
//            OutputStream outputStream = new FileOutputStream(nbtPath.toFile());
//
//            try {
//                NbtIo.writeCompressed(nbtCompound, outputStream);
//                System.out.println("NEW STRUCTURE AT " + nbtPath);
//            } catch (Throwable t) {
//                try {
//                    outputStream.close();
//                } catch (Throwable var10) {
//                    t.addSuppressed(var10);
//                }
//
//                throw t;
//            }
//
//            outputStream.close();
//        } catch (Throwable ignored) {
//        }
//    }
}