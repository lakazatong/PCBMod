package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;

import java.util.List;

public class Comparator extends RepeaterLike {
    private final LogicImpl setSignalImpl;

    public Comparator(Structure structure, Props p) {
        super(BlockType.COMPARATOR, structure, p);
        props.delay = 2;
        setSignalImpl = subtract() ? this::setSignalSubtract : this::setSignalNormal;
        logicImpl = super::unlockableLogic;
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID, DUST -> this.isFacing(neighbor);
            case REPEATER, COMPARATOR -> this.isFacing(neighbor) && !neighbor.isFacing(this);
            default -> false;
        };
    }

    protected void setSignalSubtract(double t, Props p) {
        if (!powered) {
            p.signal = 0;
            return;
        }
        List<Block> rearInputs = rearInputs().toList();
        assert rearInputs.size() <= 1;
        if (rearInputs.isEmpty()) {
            p.signal = 0;
            return;
        }
        List<Block> sideInputs = sideInputs().toList();
        assert sideInputs.size() <= 2;
        int rearSignal = rearInputs.getFirst().signal();
        int maxSideSignal = sideInputs.stream()
                .mapToInt(Block::signal)
                .max()
                .orElse(0);
        p.signal = Math.max(0, rearSignal - maxSideSignal);
    }

    protected void setSignalNormal(double t, Props p) {
        if (!powered) {
            p.signal = 0;
            return;
        }
        List<Block> rearInputs = rearInputs().toList();
        assert rearInputs.size() <= 1;
        if (rearInputs.isEmpty()) {
            p.signal = 0;
            return;
        }
        List<Block> sideInputs = sideInputs().toList();
        assert sideInputs.size() <= 2;
        int rearSignal = rearInputs.getFirst().signal();
        p.signal = sideInputs.stream().anyMatch(i -> i.signal() > rearSignal) ? 0 : rearSignal;
    }

    @Override
    protected void setSignal(double t, Props p) {
        setSignalImpl.apply(t, p);
    }
}
