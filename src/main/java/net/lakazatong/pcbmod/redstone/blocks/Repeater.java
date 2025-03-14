package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;
import net.lakazatong.pcbmod.redstone.utils.Vec3;

import java.util.stream.Collectors;

public class Repeater extends Delayed {
    public Repeater(Structure structure, Props p) {
        super(BlockType.REPEATER, structure, p);
        props.facings = facings().stream().map(Vec3::opposite).collect(Collectors.toSet());
        logicImpl = super::lockableLogic;
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR -> false;
            case SOLID, DUST -> !locked() && isFacing(neighbor);
            case REPEATER -> isSideInputOf(neighbor) || (!neighbor.locked() && isFacing(neighbor) && !neighbor.isFacing(this));
            case TORCH -> false;
            case COMPARATOR -> !locked() && isFacing(neighbor) && !neighbor.isFacing(this);
            case BUTTON -> false;
            case LEVER -> false;
            case REDSTONE_BLOCK -> false;
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
//        return rearInputs().anyMatch(i -> i.signal() > 0);
    }

    @Override
    protected void setSignal(long t) {
        nextProps.signal = 15;
    }

    @Override
    protected void clearSignal(long t) {
        nextProps.signal = 0;
    }

    @Override
    public void logic(long t) {
        super.logic(t);
        nextProps.locked = nextSideInputs().anyMatch(i -> i.nextSignal() > 0);
    }
}
