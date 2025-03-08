package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;

public abstract class Delayed extends Block {
    protected LogicImpl logicImpl;

    public abstract boolean getShouldPowered();
    protected abstract void setSignal(long t);
    protected abstract void clearSignal(long t);

    public boolean prevShouldPowered;
    private long stableTime = 0;

    public Delayed(BlockType type, Structure structure, Props p) {
        super(type, structure, p);
    }

    @Override
    public void init() {
        prevShouldPowered = getShouldPowered();
        super.init();
    }

    protected void unlockedLogic(long t) {
        boolean powered = signal() > 0;
        boolean shouldPowered = getShouldPowered();
        boolean delayOver = stableTime + 1 >= delay();

        if (delayOver) {
            if (powered != prevShouldPowered) {
                stableTime = 0;
                if (prevShouldPowered)
                    setSignal(t);
                else
                    clearSignal(t);
            }
        } else if (powered != shouldPowered) {
            stableTime++;
        }

        prevShouldPowered = shouldPowered;
    }

    protected void lockedLogic(long t) {
        stableTime = 0;
        prevShouldPowered = getShouldPowered();
    }

    protected void unlockableLogic(long t) {
        unlockedLogic(t);
    }

    protected void lockableLogic(long t) {
        if (locked())
            lockedLogic(t);
        else
            unlockedLogic(t);
    }

    @Override
    public void logic(long t) {
        logicImpl.apply(t);
    }

    public long stableTime() {
        return stableTime;
    }
}
