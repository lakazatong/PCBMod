package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;

public class Button extends Block {
    public Button(Structure structure, Props p) {
        super(BlockType.BUTTON, structure, p);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR, TORCH, BUTTON, LEVER, REDSTONE_BLOCK -> false;
            case SOLID -> isFacingAway(neighbor);
            case PORT -> neighbor.portType().equals(PortBlock.PortType.OUTPUT) && isFacingAway(neighbor);
            case DUST -> true;
            case REPEATER, COMPARATOR -> neighbor.isFacingAway(this);
        };
    }

    @Override
    public void logic() {
        if (circuit.time + 1 >= delay() )
            nextProps.signal = 0;
    }
}
