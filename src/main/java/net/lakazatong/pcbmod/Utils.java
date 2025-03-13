package net.lakazatong.pcbmod;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class Utils {
    public static MutableText translate(String category, String... keys) {
        return Text.translatable(String.join(".", category, PCBMod.MOD_ID, String.join(".", keys)));
    }
}
