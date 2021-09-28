package net.blancworks.figura.mixin;

import net.blancworks.figura.FiguraMod;
import net.blancworks.figura.PlayerDataManager;
import net.blancworks.figura.gui.ActionWheel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow @Final public Mouse mouse;
    public boolean actionWheelActive = false;

    @Inject(at = @At("INVOKE"), method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V")
    public void disconnect(Screen screen, CallbackInfo ci) {
        try {
            PlayerDataManager.clearCache();
        } catch (Exception ignored) {}
    }

    @Inject(at = @At("RETURN"), method = "handleInputEvents")
    public void handleInputEvents(CallbackInfo ci) {
        if (FiguraMod.actionWheel.isPressed()) {
            if (ActionWheel.enabled) {
                this.mouse.unlockCursor();
                actionWheelActive = true;
            }
        }
        else if (actionWheelActive) {
            ActionWheel.play();
            this.mouse.lockCursor();
            actionWheelActive = false;
        }
    }

    @Inject(at = @At("HEAD"), method = "openScreen")
    public void openScreen(Screen screen, CallbackInfo ci) {
        if (actionWheelActive) {
            ActionWheel.play();
            actionWheelActive = false;
        }
    }
}
