package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;

public class Lever extends Constant {
    public Lever(Structure structure, Props p) {
        super(BlockType.LEVER, structure, p);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR -> false;
            case SOLID -> isFacingAway(neighbor);
            case DUST -> true;
            case TORCH -> false;
            case REPEATER, COMPARATOR -> neighbor.isFacingAway(this);
            case BUTTON -> false;
            case LEVER -> false;
            case REDSTONE_BLOCK -> false;
            case PORT -> neighbor.portType() == PortBlock.PortType.OUTPUT && isFacingAway(neighbor);
        };
    }
}
