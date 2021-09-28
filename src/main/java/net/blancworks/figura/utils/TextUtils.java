package net.blancworks.figura.utils;

import com.google.common.collect.Lists;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TextUtils {
    public static String noBadges4U(String string) {
        return string.replaceAll("([△▲★✯☆✭]|\\\\u(?i)(25B3|25B2|2605|272F|2606|272D))", "\uFFFD");
    }

    public static List<Text> splitText(Text text, String regex) {
        ArrayList<Text> textList = new ArrayList<>();

        List<Text> list = Lists.newArrayList();
        text.visit((styleOverride, text2) -> {
            if (!text2.isEmpty()) {
                list.add((new LiteralText(text2)).fillStyle(styleOverride));
            }

            return Optional.empty();
        }, text.getStyle());

        MutableText currentText = new LiteralText("");
        for (Text entry : list) {
            String entryString = entry.getString();
            String[] lines = entryString.split(regex);
            for (int i = 0; i < lines.length; i++) {
                if (i != 0) {
                    textList.add(currentText.shallowCopy());
                    currentText = new LiteralText("");
                }
                currentText.append(new LiteralText(lines[i]).setStyle(entry.getStyle()));
            }
            if (entryString.endsWith(regex)) {
                textList.add(currentText.shallowCopy());
                currentText = new LiteralText("");
            }
        }
        textList.add(currentText);

        return textList;
    }
}
