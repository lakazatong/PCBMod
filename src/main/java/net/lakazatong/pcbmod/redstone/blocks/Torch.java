package net.lakazatong.pcbmod.redstone.blocks;

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
            case AIR, TORCH, BUTTON, LEVER, REDSTONE_BLOCK -> false;
            case SOLID, PORT -> isBelow(neighbor);
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
