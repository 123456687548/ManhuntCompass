package eu.manhuntcompass.mixin;

import eu.manhuntcompass.ManhuntCompass;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PointOfInterestStorage.class)
public class PointOfInterestStorageMixin {
    @Inject(at = @At("HEAD"), cancellable = true, method = "hasTypeAt(Lnet/minecraft/world/poi/PointOfInterestType;Lnet/minecraft/util/math/BlockPos;)Z")
    private void onHasTypeAt(PointOfInterestType type, BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (type == PointOfInterestType.LODESTONE
                && (pos.equals(ManhuntCompass.INSTANCE.getLastOverworldPos())
                || pos.equals(ManhuntCompass.INSTANCE.getLastNetherPos())
                || pos.equals(ManhuntCompass.INSTANCE.getLastEndPos()))) {
            callbackInfo.setReturnValue(true);
            callbackInfo.cancel();
        }
    }
}

