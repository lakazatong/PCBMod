package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.utils.Vec3;

import java.util.List;
import java.util.stream.Collectors;

public class Comparator extends Delayed {
    public Comparator(Structure structure, Props p) {
        super(BlockType.COMPARATOR, structure, p);
        props.facings = facings().stream().map(Vec3::opposite).collect(Collectors.toSet());
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID, DUST -> this.isFacing(neighbor);
            case REPEATER -> isSideInputOf(neighbor) || (!neighbor.locked() && isFacing(neighbor) && neighbor.isFacingAway(this));
            case COMPARATOR -> this.isFacing(neighbor) && !neighbor.isFacing(this);
            default -> false;
        };
    }

    @Override
    protected boolean getShouldPowered() {
        return inputs().anyMatch(i -> i.signal() > 0);
    }

    protected void setSignalSubtract(long t, Props p) {
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

    protected void setSignalNormal(long t, Props p) {
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
    protected void setSignal(long t, Props p) {
        if (subtract())
            setSignalSubtract(t, p);
        else
            setSignalNormal(t, p);
    }

    @Override
    protected void clearSignal(long t, Props p) {
        p.signal = 0;
    }
}
