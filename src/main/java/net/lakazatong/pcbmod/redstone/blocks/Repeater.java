package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.utils.Direction;

import java.util.stream.Collectors;

public class Repeater extends Delayed {
    public Repeater(Props p) {
        super(BlockType.REPEATER, p);
        props.facings = facings().stream().map(Direction::opposite).collect(Collectors.toSet());
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR, TORCH, BUTTON, LEVER, REDSTONE_BLOCK -> false;
            case SOLID, DUST -> !locked() && isFacing(neighbor);
            case PORT -> neighbor.portType().equals(PortBlock.PortType.OUTPUT) && (!locked() && isFacing(neighbor));
            case REPEATER -> isSideInputOf(neighbor) || (!neighbor.locked() && isFacing(neighbor) && !neighbor.isFacing(this));
            case COMPARATOR -> !locked() && isFacing(neighbor) && !neighbor.isFacing(this);
        };
    }

    @Override
    public boolean getShouldPowered() {
        for (Block input : rearInputs().collect(Collectors.toSet())) {
            var x = 0;
            if (input.signal() > 0)
                return true;
        }
        return false;
    }

    @Override
    protected void setSignal() {
        nextProps.signal = 15;
    }

    @Override
    protected void clearSignal() {
        nextProps.signal = 0;
    }

    @Override
    public void logic() {
        if (locked())
            lockedLogic();
        else
            super.logic();

        nextProps.locked = nextSideInputs().anyMatch(i -> i.nextSignal() > 0);
    }

    protected void lockedLogic() {
        stableTime = 0;
        prevShouldPowered = getShouldPowered();
    }
}
