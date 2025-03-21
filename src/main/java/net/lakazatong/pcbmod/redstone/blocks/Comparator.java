package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.utils.Direction;

import java.util.List;
import java.util.stream.Collectors;

public class Comparator extends Delayed {
    public Comparator(Props p) {
        super(BlockType.COMPARATOR, p);
        props.facings = facings().stream().map(Direction::opposite).collect(Collectors.toSet());
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR, TORCH, BUTTON, LEVER, REDSTONE_BLOCK -> false;
            case SOLID, DUST -> isFacing(neighbor);
            case PORT -> neighbor.portType().equals(PortBlock.PortType.OUTPUT) && isFacing(neighbor);
            case REPEATER -> isSideInputOf(neighbor) || (!neighbor.locked() && isFacing(neighbor) && neighbor.isFacingAway(this));
            case COMPARATOR -> isFacing(neighbor) && !neighbor.isFacing(this);
        };
    }

    @Override
    public boolean getShouldPowered() {
        return rearInputs().anyMatch(i -> i.signal() > 0);
    }

    protected void setSignalSubtract() {
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

    protected void setSignalNormal() {
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
    protected void setSignal() {
        if (subtract())
            setSignalSubtract();
        else
            setSignalNormal();
    }

    @Override
    protected void clearSignal() {
        nextProps.signal = 0;
    }
}
