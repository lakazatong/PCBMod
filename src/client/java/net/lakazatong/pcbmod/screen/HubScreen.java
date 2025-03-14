package net.lakazatong.pcbmod.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lakazatong.pcbmod.Utils;
import net.lakazatong.pcbmod.block.custom.HubBlock.Side;
import net.lakazatong.pcbmod.block.entity.HubBlockEntity;
import net.lakazatong.pcbmod.payloads.UpdateHubPayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class HubScreen extends CommonScreen<HubBlockEntity> {

    private EditBoxWidget structureNameField;

    private final EditBoxWidget[] portNumberFields = new EditBoxWidget[6];

    public HubScreen(BlockPos pos) {
        super(Utils.translate("screen", "hub", "title"), pos);
    }

    @Override
    protected void init() {
        super.init();

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

        structureNameField = new EditBoxWidget(textRenderer, centerX - maxWidth / 2, centerY - 2 * verticalTotalSpacingWidth, maxWidth, fieldHeight, Text.empty(), Text.empty());
        structureNameField.setMaxLength(maxLength);
        structureNameField.setText(be.getStructureName());
        addDrawableChild(structureNameField);

        int nbCols = 3;
        int nbRows = 2;
        int portNumberFieldsWidth = (maxWidth - horizontalSpacingWidth * (nbCols - 1)) / nbCols;
        int portNumberFieldsLength = getFieldLength(portNumberFieldsWidth);

        for (int i = 0; i < nbRows; i++) {
            for (int j = 0; j < nbCols; j++) {
                int fieldIndex = j * nbRows + i;
                int x = centerX - maxWidth / 2 + j * (portNumberFieldsWidth + horizontalSpacingWidth);
                int y = centerY + (i - 1) * verticalTotalSpacingWidth;

                EditBoxWidget portField = new EditBoxWidget(textRenderer, x, y, portNumberFieldsWidth, fieldHeight, Text.empty(), Text.empty());
                portField.setMaxLength(portNumberFieldsLength);
                portField.setText(be.getPortNumberAt(fieldIndex) > 0 ? String.valueOf(be.getPortNumberAt(fieldIndex)) : "");
                addDrawableChild(portField);

                portNumberFields[fieldIndex] = portField;
            }
        }

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

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawText(textRenderer, title, titleLabelX, titleLabelY, 0xFFFFFF, true);
        context.drawText(textRenderer, Utils.translate("screen", "hub", "structure_name"), structureNameField.getX(), structureNameField.getY() - 10, 0xA0A0A0, true);

        MutableText[] sides = {
                Utils.translate("word", "front"),
                Utils.translate("word", "back"),
                Utils.translate("word", "left"),
                Utils.translate("word", "right"),
                Utils.translate("word", "up"),
                Utils.translate("word", "down")
        };
        for (int i = 0; i < 6; i++) {
            context.drawText(textRenderer, sides[i], portNumberFields[i].getX(), portNumberFields[i].getY() - 10, Side.colorAt(i), true);
        }

        structureNameField.render(context, mouseX, mouseY, delta);
        for (int i = 0; i < 6; i++) {
            portNumberFields[i].render(context, mouseX, mouseY, delta);
        }
    }

    private int getPortNumberAt(int side) {
        String txt = portNumberFields[side].getText();
        return txt.isEmpty()
            ? 0
            : (txt.chars().allMatch(Character::isDigit)
                ? Integer.parseInt(txt)
                : be.getPortNumberAt(side));
    }

    @Override
    protected void onDone() {
        String structureName = structureNameField.getText();
        int frontPortNumber = getPortNumberAt(Side.FRONT.ordinal());
        int backPortNumber = getPortNumberAt(Side.BACK.ordinal());
        int leftPortNumber = getPortNumberAt(Side.LEFT.ordinal());
        int rightPortNumber = getPortNumberAt(Side.RIGHT.ordinal());
        int upPortNumber = getPortNumberAt(Side.UP.ordinal());
        int downPortNumber = getPortNumberAt(Side.DOWN.ordinal());

        ClientPlayNetworking.send(new UpdateHubPayload(pos, structureName,
                frontPortNumber, backPortNumber,
                leftPortNumber, rightPortNumber,
                upPortNumber, downPortNumber));

        be.setStructureName(structureName);

        be.setPortNumberAt(Side.FRONT.ordinal(), frontPortNumber);
        be.setPortNumberAt(Side.BACK.ordinal(), backPortNumber);
        be.setPortNumberAt(Side.LEFT.ordinal(), leftPortNumber);
        be.setPortNumberAt(Side.RIGHT.ordinal(), rightPortNumber);
        be.setPortNumberAt(Side.UP.ordinal(), upPortNumber);
        be.setPortNumberAt(Side.DOWN.ordinal(), downPortNumber);

        close();
    }

    @Override
    protected void onCancel() {
        close();
    }
}