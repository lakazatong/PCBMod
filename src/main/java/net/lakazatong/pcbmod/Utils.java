package net.lakazatong.pcbmod;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class Utils {
    public static MutableText translate(String category, String... keys) {
        return Text.translatable(String.join(".", category, PCBMod.MOD_ID, String.join(".", keys)));
    }

    public static String structureNameFrom(String circuitName) {
        return circuitName.replaceFirst("\\d+$", "");
    }

    public static int instanceIdFrom(String circuitName) {
        return Integer.parseInt(circuitName.replaceFirst(".+?(\\d+)$", "$1"));
    }
}
