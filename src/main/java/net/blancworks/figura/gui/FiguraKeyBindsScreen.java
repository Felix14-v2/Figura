package net.blancworks.figura.gui;

import net.blancworks.figura.gui.widgets.KeyBindingsWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class FiguraKeyBindsScreen extends Screen {

    public Screen parentScreen;
    private KeyBindingsWidget keyBindingsWidget;

    public FiguraKeyBindsScreen(Screen parentScreen) {
        super(new TranslatableText("gui.figura.keybindstitle"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.addDrawableChild(new ButtonWidget(this.width / 2 - 75, this.height - 29, 150, 20, new TranslatableText("gui.back"), (buttonWidgetx) -> this.client.openScreen(parentScreen)));

        this.keyBindingsWidget = new KeyBindingsWidget(this, this.client);
        this.addSelectableChild(this.keyBindingsWidget);
    }

    @Override
    public void onClose() {
        this.client.openScreen(parentScreen);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        //background
        this.renderBackgroundTexture(0);

        //list
        this.keyBindingsWidget.render(matrices, mouseX, mouseY, delta);

        //buttons
        super.render(matrices, mouseX, mouseY, delta);

        //screen title
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 12, 16777215);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (keyBindingsWidget.focusedBinding != null) {
            keyBindingsWidget.focusedBinding.setBoundKey(InputUtil.Type.MOUSE.createFromCode(button));
            keyBindingsWidget.focusedBinding = null;

            KeyBinding.updateKeysByCode();

            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyBindingsWidget.focusedBinding != null) {
            keyBindingsWidget.focusedBinding.setBoundKey(keyCode == 256 ? InputUtil.UNKNOWN_KEY: InputUtil.fromKeyCode(keyCode, scanCode));
            keyBindingsWidget.focusedBinding = null;

            KeyBinding.updateKeysByCode();

            return true;
        }
        else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
