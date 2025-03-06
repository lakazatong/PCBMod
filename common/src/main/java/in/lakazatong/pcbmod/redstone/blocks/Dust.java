package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.utils.Vec3;

public class Dust extends Block {
    public Dust(Vec3 coords, Structure structure) {
        super(BlockType.DUST, coords, structure);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID -> this.isFacing(neighbor) || this.isAbove(neighbor);
            case DUST -> true;
            case REPEATER -> neighbor.isFacingAway(this);
            case COMPARATOR -> !neighbor.isFacing(this);
            default -> false;
        };
    }

    @Override
    public void logic(double t, Props p) {
        boolean degradeSignal = true;
        p.signal = 0;
        for (Block block : inputs()) {
            switch (block.type) {
                case BlockType.REPEATER:
                case BlockType.TORCH:
                case BlockType.BUTTON:
                    if (block.signal() > 0) {
                        p.signal = 15;
                        return;
                    }
                    break;
                case BlockType.SOLID:
                    if (block.signal() > p.signal && block.hardPowered()) {
                        degradeSignal = false;
                        p.signal = block.signal();
                    }
                    break;
                case BlockType.COMPARATOR:
                    if (block.signal() > p.signal) {
                        degradeSignal = false;
                        p.signal = block.signal();
                    }
                    break;
                case BlockType.DUST:
                    if (block.signal() > p.signal) {
                        degradeSignal = true;
                        p.signal = block.signal();
                    }
                    break;
            }
        }
        if (degradeSignal)
            p.signal--;
    }
}
