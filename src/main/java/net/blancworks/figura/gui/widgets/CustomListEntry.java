package net.blancworks.figura.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

public class CustomListEntry extends AlwaysSelectedEntryListWidget.Entry<CustomListEntry>{
    protected final MinecraftClient client;
    public Object entryValue;
    protected final CustomListWidget list;
    
    public CustomListEntry(Object obj, CustomListWidget list) {
        this.entryValue = obj;
        this.list = list;
        this.client = MinecraftClient.getInstance();
    }

    public void render(MatrixStack matrices, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        x += getXOffset();
        rowWidth -= getXOffset();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Text name = getDisplayText();
        StringVisitable trimmedName = name;
        int maxNameWidth = rowWidth - 3;
        TextRenderer font = this.client.textRenderer;
        if (font.getWidth(name) > maxNameWidth) {
            StringVisitable ellipsis = StringVisitable.styled("...", name.getStyle());
            trimmedName = StringVisitable.concat(font.trimToWidth(name, maxNameWidth - font.getWidth(ellipsis)), ellipsis);
        }
        font.draw(matrices, Language.getInstance().reorder(trimmedName), x + 3, y + (rowHeight / 2) - (font.fontHeight/2), 0xFFFFFF);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        list.select(this);
        return true;
    }
    
    public int getXOffset() {
        return 0;
    }
    
    public Text getDisplayText(){
        return Text.literal("ENTRY");
    }
    
    public String getIdentifier(){
        return "ENTRY";
    }
    
    public Object getEntryObject(){
        return entryValue;
    }

    public void tick(double mouseX, double mouseY) {}

    @Override
    public Text getNarration() {
        return Text.literal(String.valueOf(entryValue));
    }
}
