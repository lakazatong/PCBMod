package net.lakazatong.pcbmod.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lakazatong.pcbmod.block.entity.HubBlockEntity;
import net.lakazatong.pcbmod.payloads.NewCircuitPayload;
import net.lakazatong.pcbmod.payloads.UpdateHubPayload;
import net.lakazatong.pcbmod.redstone.circuit.Circuit;
import net.lakazatong.pcbmod.redstone.circuit.Circuits;
import net.lakazatong.pcbmod.redstone.utils.Direction;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.nio.file.Path;
import java.util.List;

import static net.lakazatong.pcbmod.PCBMod.*;
import static net.lakazatong.pcbmod.PCBModClient.colorAt;

public class HubScreen extends CommonScreen<HubBlockEntity> {

    private EditBoxWidget circuitNameField;

    private final EditBoxWidget[] portNumberFields = new EditBoxWidget[6];

    public HubScreen(BlockPos pos) {
        super(translate("screen", "hub", "title"), pos);
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

        circuitNameField = new EditBoxWidget(textRenderer, centerX - maxWidth / 2, centerY - 2 * verticalTotalSpacingWidth, maxWidth, fieldHeight, Text.empty(), Text.empty());
        circuitNameField.setMaxLength(maxLength);
        circuitNameField.setText(be.getCircuitName());
        addDrawableChild(circuitNameField);

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
                translate("word", "done"),
                translate("word", "cancel")
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
        context.drawText(textRenderer, translate("screen", "hub", "circuit_name"), circuitNameField.getX(), circuitNameField.getY() - 10, 0xA0A0A0, true);

        MutableText[] sides = {
                translate("word", "front"),
                translate("word", "back"),
                translate("word", "left"),
                translate("word", "right"),
                translate("word", "up"),
                translate("word", "down")
        };
        for (int i = 0; i < 6; i++) {
            context.drawText(textRenderer, sides[i], portNumberFields[i].getX(), portNumberFields[i].getY() - 10, colorAt(i), true);
        }

        circuitNameField.render(context, mouseX, mouseY, delta);
        for (int i = 0; i < 6; i++) {
            portNumberFields[i].render(context, mouseX, mouseY, delta);
        }
    }

    private int getPortNumberAt(Direction dir) {
        int side = dir.side;
        String txt = portNumberFields[side].getText();
        return txt.isEmpty()
            ? 0
            : (txt.chars().allMatch(Character::isDigit)
                ? Integer.parseInt(txt)
                : be.getPortNumberAt(side));
    }

    @Override
    protected void onDone() {
        String circuitName = circuitNameField.getText();
        String structureName = be.getStructureName();
        int instanceId = be.getInstanceId();

        if (Circuits.isValidCircuitName(circuitName)) {
            String tmp = Circuit.structureNameFrom(circuitName);
            Path structurePath = STRUCTURES_PATH.resolve(tmp + ".nbt");
            if (structurePath.toFile().exists()) {
                structureName = tmp;
                instanceId = Circuit.instanceIdFrom(circuitName);
                if (!CIRCUITS.containsKey(circuitName))
                    ClientPlayNetworking.send(new NewCircuitPayload(circuitName, structurePath.toString()));
            }
        }

        List<Integer> portNumbers = List.of(
                getPortNumberAt(Direction.NORTH), getPortNumberAt(Direction.SOUTH),
                getPortNumberAt(Direction.WEST), getPortNumberAt(Direction.EAST),
                getPortNumberAt(Direction.UP), getPortNumberAt(Direction.DOWN)
        );

        ClientPlayNetworking.send(new UpdateHubPayload(pos, structureName, instanceId, portNumbers));

        be.setStructureName(structureName);
        be.setInstanceId(instanceId);
        be.setPortNumbers(portNumbers.stream().mapToInt(Integer::intValue).toArray());

        close();
    }

    @Override
    protected void onCancel() {
        close();
    }

    @Override
    protected void setInitialFocus() {
        circuitNameField.setFocused(true);
    }
}