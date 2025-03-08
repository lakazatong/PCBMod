package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;

public abstract class Delayed extends Block {

    public abstract boolean getShouldPowered();
    protected abstract void setSignal(long t);
    protected abstract void clearSignal(long t);

    public boolean nextPowered;
    private int stableTime = 1;

    public Delayed(BlockType type, Structure structure, Props p) {
        super(type, structure, p);
    }

    @Override
    public void init() {
        nextPowered = getShouldPowered();
        super.init();
    }

    @Override
    public void logic(long t) {
        boolean powered = signal() > 0;
        boolean shouldPowered = getShouldPowered();
        boolean delayOver = stableTime >= delay();

        boolean updateSignal = delayOver && (powered != nextPowered);
        stableTime = updateSignal || (nextPowered != shouldPowered) ? 1 : stableTime + 1;

        if (updateSignal) {
            if (nextPowered)
                setSignal(t);
            else
                clearSignal(t);
        }

        nextPowered = shouldPowered;
    }
}
