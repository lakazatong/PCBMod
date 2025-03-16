package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;

public class RedstoneBlock extends Constant {
    public RedstoneBlock(Structure structure, Props p) {
        super(BlockType.REDSTONE_BLOCK, structure, p);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR -> false;
            case SOLID -> false;
            case DUST -> true;
            case REPEATER -> !neighbor.locked() && neighbor.isFacingAway(this);
            case COMPARATOR -> neighbor.isFacingAway(this);
            case TORCH -> (neighbor.isAbove(this) && !neighbor.onWall()) || neighbor.isOnWallOf(this);
            case BUTTON -> false;
            case LEVER -> false;
            case REDSTONE_BLOCK -> false;
            case PORT -> false;
        };
    }
}
