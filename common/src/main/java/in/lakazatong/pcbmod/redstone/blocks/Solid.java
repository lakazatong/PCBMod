package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;

public class Solid extends Block {
    public Solid(Structure structure, Props p) {
        super(BlockType.SOLID, structure, p);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case DUST -> true;
            case REPEATER, COMPARATOR -> neighbor.isFacingAway(this);
            case TORCH -> (neighbor.isAbove(this) && !neighbor.onWall()) || neighbor.isOnWallOf(this);
            default -> false;
        };
    }

    @Override
    public void logic(double t, Props p) {
        p.signal = 0;
        for (Block block : inputs()) {
            switch (block.type) {
                case BlockType.REPEATER:
                case BlockType.TORCH:
                case BlockType.BUTTON:
                    if (block.signal() > 0) {
                        p.hardPowered = true;
                        p.signal = 15;
                        return;
                    }
                    break;
                case BlockType.COMPARATOR, BlockType.DUST:
                    if (block.signal() > p.signal) {
                        p.hardPowered = false;
                        p.signal = block.signal();
                    }
                    break;
            }
        }
    }
}
