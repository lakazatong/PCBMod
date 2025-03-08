package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;

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
        };
    }
}
