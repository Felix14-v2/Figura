package net.blancworks.figura.gui;

import net.blancworks.figura.gui.widgets.FiguraKeybindWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableTextContent;

public class FiguraKeyBindsScreen extends Screen {

    public Screen parentScreen;
    private FiguraKeybindWidget keyBindingsWidget;

    public FiguraKeyBindsScreen(Screen parentScreen) {
        super(MutableText.of(new TranslatableTextContent("figura.gui.keybinds.title")));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.addDrawableChild(new ButtonWidget(this.width / 2 - 75, this.height - 29, 150, 20, MutableText.of(new TranslatableTextContent("gui.back")), (buttonWidgetx) -> this.client.setScreen(parentScreen)));

        this.keyBindingsWidget = new FiguraKeybindWidget(this, this.client);
        this.addSelectableChild(this.keyBindingsWidget);
    }

    @Override
    public void close() {
        this.client.setScreen(parentScreen);
    }

    @Override
    public void tick() {
        super.tick();
        this.keyBindingsWidget.tick();
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
            keyBindingsWidget.focusedBinding.keycode = InputUtil.Type.MOUSE.createFromCode(button).getCode();
            keyBindingsWidget.focusedBinding = null;

            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyBindingsWidget.focusedBinding != null) {
            keyBindingsWidget.focusedBinding.keycode = (keyCode == 256 ? InputUtil.UNKNOWN_KEY: InputUtil.fromKeyCode(keyCode, scanCode)).getCode();
            keyBindingsWidget.focusedBinding = null;

            return true;
        }
        else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
