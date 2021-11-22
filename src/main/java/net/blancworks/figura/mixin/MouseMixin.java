package net.blancworks.figura.mixin;

import net.blancworks.figura.gui.PlayerPopup;
import net.blancworks.figura.lua.api.keybind.FiguraKeybind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        boolean pressed = action == 1;
        FiguraKeybind.setKeyPressed(InputUtil.Type.MOUSE.createFromCode(button), pressed);

        if (pressed)
            FiguraKeybind.onKeyPressed(InputUtil.Type.MOUSE.createFromCode(button));
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == MinecraftClient.getInstance().getWindow().getHandle()) {
            if (PlayerPopup.mouseScrolled(Math.signum(vertical)))
                ci.cancel();
        }
    }
}
