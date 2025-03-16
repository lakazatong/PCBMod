package net.lakazatong.pcbmod.screen;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public abstract class CommonScreen<E extends BlockEntity> extends Screen {

    protected BlockPos pos;
    protected E be;

    int titleLabelX = 0;
    int titleLabelY = 0;

    private int refWidth = 0;

    protected CommonScreen(Text title, BlockPos pos) {
        super(title);
        this.pos = pos;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void init() {
        super.init();
        assert client != null;
        assert client.world != null;
        be = (E) client.world.getBlockEntity(pos);
        refWidth = textRenderer.getWidth("0");
    }

    protected abstract void onDone();
    protected abstract void onCancel();

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return keyPressed(pKeyCode, this::onDone, this::onCancel) || super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(null);
    }

    public static boolean keyPressed(int pKeyCode, Runnable onDone, Runnable onCancel) {
        if (pKeyCode == 257) {
            onDone.run();
            return true;
        }

        if (pKeyCode == 1) {
            onCancel.run();
            return true;
        }

        return false;
    }

    // considering length as the number of characters
    // the + 2 is to compensate for the different paddings and margins
    protected int getFieldWidth(int length) { return refWidth * (length + 3); }
    // inverse operation
    protected int getFieldLength(int width) {
        return width / refWidth - 3;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
