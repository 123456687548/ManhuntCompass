package eu.manhuntcompass.mixin;

import eu.manhuntcompass.ManhuntCompass;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(at = @At("RETURN"), method = "onPlayerMove")
    private void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo callbackInfo){
        if(!player.equals(ManhuntCompass.INSTANCE.getTrackedPlayer())) return;

        if(ManhuntCompass.INSTANCE.isActive()){
            ManhuntCompass.INSTANCE.onPlayerMove();
        }
    }
}
