package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;

public class Torch extends Delayed {

    public Torch(Props p) {
        super(BlockType.TORCH, p);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR, TORCH, BUTTON, LEVER, REDSTONE_BLOCK -> false;
            case SOLID -> isBelow(neighbor);
            case PORT -> neighbor.portType().equals(PortBlock.PortType.OUTPUT) && isBelow(neighbor);
            case DUST -> true;
            case REPEATER -> !neighbor.locked() && neighbor.isFacingAway(this);
            case COMPARATOR -> neighbor.isFacingAway(this);
        };
    }

    @Override
    public boolean getShouldPowered() {
        return inputs().allMatch(input -> input.signal() == 0);
    }

    @Override
    protected void setSignal() {
        nextProps.signal = 15;
    }

    @Override
    protected void clearSignal() {
        nextProps.signal = 0;
    }
}
