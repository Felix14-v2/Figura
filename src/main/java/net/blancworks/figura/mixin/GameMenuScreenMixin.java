package net.blancworks.figura.mixin;

import net.blancworks.figura.config.ConfigManager;
import net.blancworks.figura.config.ConfigManager.Config;
import net.blancworks.figura.gui.FiguraGuiScreen;
import net.blancworks.figura.gui.NewFiguraGuiScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin extends Screen {

    private FiguraGuiScreen figura$screen;
    private NewFiguraGuiScreen figura$newScreen;

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("HEAD"), method = "initWidgets", require = 1)
    void initWidgets(CallbackInfo ci) {
        if (this.figura$screen == null)
            this.figura$screen = new FiguraGuiScreen(this);

        int x = 5;
        int y = 5;

        int config = (int) Config.FIGURA_BUTTON_LOCATION.value;
        switch (config) {
            //top right
            case 1 -> x = this.width - 64 - 5;
            //bottom left
            case 2 -> y = this.height - 20 - 5;
            //bottom right
            case 3 -> {
                x = this.width - 64 - 5;
                y = this.height - 20 - 5;
            }
            //icon
            case 4 -> {
                x = this.width / 2 + 4 + 100 + 2;
                y = this.height / 4 + 96 + -16;
            }
        }

        if (config != 4) {
            if (ConfigManager.modmenuButton())
                y += 12;

            addDrawableChild(new ButtonWidget(x, y, 64, 20, new LiteralText("Figura"),
                    btn -> this.client.setScreen(figura$screen)));
        }
        else {
            Identifier iconTexture = new Identifier("figura", "textures/gui/config_icon.png");
            addDrawableChild(new TexturedButtonWidget(x, y, 20, 20, 0, 0, 20, iconTexture, 20, 40, btn -> this.client.setScreen(figura$screen)));
        }

        if (this.figura$newScreen == null)
            this.figura$newScreen = new NewFiguraGuiScreen(this);
    }
}
