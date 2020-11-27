package eu.manhuntcompass.mixin;

import eu.manhuntcompass.ManhuntCompass;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(at = @At("HEAD"), method = "tick")
    private void tick(CallbackInfo callbackInfo) {
        if(ManhuntCompass.INSTANCE.isActive()){
            ManhuntCompass.INSTANCE.onTick();
        }
    }
}
