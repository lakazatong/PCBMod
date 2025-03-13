package net.lakazatong.pcbmod.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public class PortScreen extends Screen {
    public PortScreen() {
        super(Text.literal("Port Screen"));
    }

    @Override
    protected void init() {
        ButtonWidget buttonWidget = ButtonWidget.builder(Text.of("Hello World"), (btn) -> {
            assert this.client != null;
            this.client.getToastManager().add(
                    SystemToast.create(this.client, SystemToast.Type.NARRATOR_TOGGLE, Text.of("Hello World!"), Text.of("This is a toast."))
            );
        }).dimensions(40, 40, 120, 20).build();
        this.addDrawableChild(buttonWidget);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, "Special Button", 40, 40 - this.textRenderer.fontHeight - 10, 0xFFFFFFFF, true);
    }
}