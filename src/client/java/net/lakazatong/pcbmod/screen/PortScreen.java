package net.lakazatong.pcbmod.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lakazatong.pcbmod.Utils;
import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.block.entity.PortBlockEntity;
import net.lakazatong.pcbmod.payloads.UpdatePortPayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class PortScreen extends CommonScreen<PortBlockEntity> {

    private EditBoxWidget portNumberField;

    private PortBlock.PortType initialPortType;

    public PortScreen(BlockPos pos) {
        super(Utils.translate("screen", "port", "title"), pos);
    }

    @Override
    protected void init() {
        super.init();
        initialPortType = be.getPortType();

        int maxLength = 32;
        int maxWidth = getFieldWidth(maxLength);
        int fieldHeight = textRenderer.fontHeight * 2;
        int verticalSpacingWidth = 16;
        int verticalTotalSpacingWidth = fieldHeight + verticalSpacingWidth;
        int horizontalSpacing = 2;
        int horizontalSpacingWidth = getFieldWidth(horizontalSpacing);

        int centerX = width / 2;
        int centerY = height / 2;

        titleLabelX = centerX - textRenderer.getWidth(title) / 2;
        titleLabelY = centerY - 3 * verticalTotalSpacingWidth;

        portNumberField = new EditBoxWidget(textRenderer,
                centerX - maxWidth / 2,
                centerY - verticalTotalSpacingWidth,
                maxWidth / 2,
                fieldHeight,
                Text.empty(),
                Text.empty());
        portNumberField.setMaxLength(maxLength / 2);
        portNumberField.setText(be.getPortNumber() > 0 ? String.valueOf(be.getPortNumber()) : "");
        addDrawableChild(portNumberField);

        ButtonWidget typeButton = ButtonWidget.builder(Text.literal(be.getPortType().asString()), this::portTypeCallback)
                .dimensions(
                        centerX - maxWidth / 2,
                        centerY,
                        maxWidth,
                        fieldHeight)
                .build();
        addDrawableChild(typeButton);

        int buttonsWidth = (maxWidth - horizontalSpacingWidth) / 2;

        MutableText[] buttonTexts = {
                Utils.translate("word", "done"),
                Utils.translate("word", "cancel")
        };
        ButtonWidget.PressAction[] buttonCallbacks = {button -> onDone(), button -> onCancel()};
        for (int k = 0; k < 2; k++) {
            int x = centerX - maxWidth / 2 + k * (buttonsWidth + horizontalSpacingWidth);
            int y = centerY + verticalTotalSpacingWidth;

            addDrawableChild(ButtonWidget.builder(buttonTexts[k], buttonCallbacks[k])
                    .dimensions(x, y, buttonsWidth, fieldHeight)
                    .build());
        }
    }

    protected void portTypeCallback(ButtonWidget button) {
        PortBlock.PortType newType = be.getPortType().next();
        be.setPortType(newType);
        button.setMessage(Text.literal(newType.asString()));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawText(textRenderer, title, titleLabelX, titleLabelY, 0xFFFFFF, true);

        context.drawText(textRenderer, Utils.translate("screen", "port", "port_number"), portNumberField.getX(), portNumberField.getY() - 10, 0xA0A0A0, true);

        portNumberField.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void onDone() {
        String portText = portNumberField.getText();
        int portNumber = portText.isEmpty()
            ? 0
            : (portText.chars().allMatch(Character::isDigit)
                ? Integer.parseInt(portText)
                : be.getPortNumber());

        ClientPlayNetworking.send(new UpdatePortPayload(pos, portNumber, be.getPortType().ordinal()));

        // keep a copy of the port number client side
        be.setPortNumber(portNumber);

        close();
    }

    @Override
    protected void onCancel() {
        if (be.getPortType() != initialPortType)
            be.setPortType(initialPortType);

        close();
    }

    @Override
    protected void setInitialFocus() {
        portNumberField.setFocused(true);
    }
}