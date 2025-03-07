package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;

public abstract class Delayed extends Block {
    protected LogicImpl logicImpl;

    protected abstract void setSignal(long t, Props p);

    protected boolean powered = false;
    private boolean nextPowered = false;
    private int stableTime = 0;

    public Delayed(BlockType type, Structure structure, Props p) {
        super(type, structure, p);
    }

    protected void lockableLogic(long t, Props p) {
        boolean receivingSignal = rearInputs().anyMatch(i -> i.signal() > 0);
        boolean locked = locked();
        if (nextPowered != receivingSignal) {
            nextPowered = receivingSignal;
            if (!locked)
                stableTime = 0;
        } else if (stableTime >= delay() && !locked) {
            powered = nextPowered;
            stableTime = 0;
        } else if (!locked) {
            stableTime++;
        }

        if (locked)
            stableTime = 0;

        setSignal(t, p);
        p.locked = sideInputs().anyMatch(i -> i.signal() > 0);
    }

    protected void unlockableLogic(long t, Props p) {
        boolean receivedSignal = inputs().anyMatch(i -> i.signal() > 0);
        if (nextPowered != receivedSignal) {
            nextPowered = receivedSignal;
            stableTime = 0;
        } else if (stableTime >= delay()) {
            powered = nextPowered;
            stableTime = 0;
        } else {
            stableTime++;
        }

        setSignal(t, p);
    }

    @Override
    public void logic(long t, Props p) {
        logicImpl.apply(t, p);
    }
}
