package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.utils.Vec3;

import java.util.stream.Collectors;

public abstract class RepeaterLike extends Block {
    protected LogicImpl logicImpl;

    protected abstract void setSignal(double t, Props p);

    protected boolean powered = false;
    private boolean nextPowered = false;
    private int stableTime = 0;

    public RepeaterLike(BlockType type, Structure structure, Props p) {
        super(type, structure, p);
        props.facings = facings().stream().map(Vec3::opposite).collect(Collectors.toSet());
    }

    protected void lockableLogic(double t, Props p) {
        boolean receivedPowered = inputs().stream()
                .filter(i -> !isSideInputOf(this))
                .anyMatch(i -> i.signal() > 0);
        boolean receivedLocked = inputs().stream()
                .filter(i -> isSideInputOf(this))
                .anyMatch(i -> i.signal() > 0);
        if (nextPowered != receivedPowered) {
            nextPowered = receivedPowered;
            if (!receivedLocked)
                stableTime = 0;
        } else if (stableTime >= delay() && !receivedLocked) {
            powered = nextPowered;
            stableTime = 0;
        } else if (!receivedLocked) {
            stableTime++;
        }

        if (receivedLocked)
            stableTime = 0;

        setSignal(t, p);
    }

    protected void unlockableLogic(double t, Props p) {
        boolean receivedPowered = inputs().stream()
                .anyMatch(i -> i.signal() > 0);
        if (nextPowered != receivedPowered) {
            nextPowered = receivedPowered;
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
    public void logic(double t, Props p) {
        logicImpl.apply(t, p);
    }
}
