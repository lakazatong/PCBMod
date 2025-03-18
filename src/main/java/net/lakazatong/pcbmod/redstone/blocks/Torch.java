package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;

public class Torch extends Delayed {

    public Torch(Structure structure, Props p) {
        super(BlockType.TORCH, structure, p);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR -> false;
            case SOLID -> isBelow(neighbor);
            case DUST -> true;
            case REPEATER -> !neighbor.locked() && neighbor.isFacingAway(this);
            case TORCH -> false;
            case COMPARATOR -> neighbor.isFacingAway(this);
            case BUTTON -> false;
            case LEVER -> false;
            case REDSTONE_BLOCK -> false;
            case PORT -> neighbor.portType() == PortBlock.PortType.OUTPUT && isBelow(neighbor);
        };
    }

    @Override
    public boolean getShouldPowered() {
        return inputs().allMatch(input -> input.signal() == 0);
    }

    @Override
    protected void setSignal(long t) {
        nextProps.signal = 15;
    }

    @Override
    protected void clearSignal(long t) {
        nextProps.signal = 0;
    }
}
