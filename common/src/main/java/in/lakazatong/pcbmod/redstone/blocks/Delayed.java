package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;

public abstract class Delayed extends Block {

    protected abstract boolean getShouldPowered();
    protected abstract void setSignal(long t, Props p);
    protected abstract void clearSignal(long t, Props p);

    protected boolean nextPowered;
    private int stableTime = 0;

    public Delayed(BlockType type, Structure structure, Props p) {
        super(type, structure, p);
    }

    @Override
    public void init() {
        nextPowered = getShouldPowered();
    }

    @Override
    public void logic(long t, Props p) {
        boolean powered = signal() > 0;
        boolean shouldPowered = getShouldPowered();
        boolean delayOver = stableTime >= delay();

        boolean updateSignal = delayOver && (powered != nextPowered);
        stableTime = updateSignal || (nextPowered != shouldPowered) ? 0 : stableTime + 1;

        if (updateSignal) {
            if (nextPowered)
                setSignal(t, p);
            else
                clearSignal(t, p);
        }

        nextPowered = shouldPowered;
    }
}
