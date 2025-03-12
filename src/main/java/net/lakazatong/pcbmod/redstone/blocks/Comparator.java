package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;
import net.lakazatong.pcbmod.redstone.utils.Vec3;

import java.util.List;
import java.util.stream.Collectors;

public class Comparator extends Delayed {
    public Comparator(Structure structure, Props p) {
        super(BlockType.COMPARATOR, structure, p);
        props.facings = facings().stream().map(Vec3::opposite).collect(Collectors.toSet());
        logicImpl = super::unlockableLogic;
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR -> false;
            case SOLID, DUST -> this.isFacing(neighbor);
            case REPEATER -> isSideInputOf(neighbor) || (!neighbor.locked() && isFacing(neighbor) && neighbor.isFacingAway(this));
            case TORCH -> false;
            case COMPARATOR -> this.isFacing(neighbor) && !neighbor.isFacing(this);
            case BUTTON -> false;
            case LEVER -> false;
            case REDSTONE_BLOCK -> false;
        };
    }

    @Override
    public boolean getShouldPowered() {
        return rearInputs().anyMatch(i -> i.signal() > 0);
    }

    protected void setSignalSubtract(long t) {
        List<Block> rearInputs = rearInputs().toList();
        assert rearInputs.size() <= 1;
        if (rearInputs.isEmpty()) {
            nextProps.signal = 0;
            return;
        }
        List<Block> sideInputs = sideInputs().toList();
        assert sideInputs.size() <= 2;
        int rearSignal = rearInputs.getFirst().signal();
        int maxSideSignal = sideInputs.stream()
                .mapToInt(Block::signal)
                .max()
                .orElse(0);
        nextProps.signal = Math.max(0, rearSignal - maxSideSignal);
    }

    protected void setSignalNormal(long t) {
        List<Block> rearInputs = rearInputs().toList();
        assert rearInputs.size() <= 1;
        if (rearInputs.isEmpty()) {
            nextProps.signal = 0;
            return;
        }
        List<Block> sideInputs = sideInputs().toList();
        assert sideInputs.size() <= 2;
        int rearSignal = rearInputs.getFirst().signal();
        nextProps.signal = sideInputs.stream().anyMatch(i -> i.signal() > rearSignal) ? 0 : rearSignal;
    }

    @Override
    protected void setSignal(long t) {
        if (subtract())
            setSignalSubtract(t);
        else
            setSignalNormal(t);
    }

    @Override
    protected void clearSignal(long t) {
        nextProps.signal = 0;
    }
}
