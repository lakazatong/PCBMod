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
    public void init() {
        logic(0, props);
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
    public void logic(long t, Props p) {
        p.signal = 0;
        inputs().forEach(input -> {
            switch (input.type) {
                case BlockType.REPEATER:
                case BlockType.TORCH:
                case BlockType.BUTTON:
                    if (input.signal() > 0) {
                        p.weakPowered = false;
                        p.signal = 15;
                        return;
                    }
                    break;
                case BlockType.COMPARATOR:
                    if (input.signal() > p.signal) {
                        p.weakPowered = false;
                        p.signal = input.signal();
                    }
                    break;
                case BlockType.DUST:
                    if (input.signal() > p.signal) {
                        p.weakPowered = true;
                        p.signal = input.signal();
                    }
                    break;
            }
        });
    }
}
