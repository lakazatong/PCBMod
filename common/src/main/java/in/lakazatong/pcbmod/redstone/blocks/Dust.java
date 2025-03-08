package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;

import java.util.stream.Collectors;

public class Dust extends Block {
    public Dust(Structure structure, Props p) {
        super(BlockType.DUST, structure, p);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID -> this.isFacing(neighbor) || this.isAbove(neighbor);
            case DUST -> true;
            case REPEATER -> !neighbor.locked() && neighbor.isFacingAway(this);
            case COMPARATOR -> !neighbor.isFacing(this);
            default -> false;
        };
    }

    @Override
    public void logic(long t, Props p) {
        boolean decay = true;
        p.signal = 0;
        for (Block block : inputs().collect(Collectors.toSet())) {
            switch (block.type) {
                case BlockType.REPEATER:
                case BlockType.TORCH:
                case BlockType.BUTTON:
                    if (block.signal() > 0)
                        p.signal = 15;
                    break;
                case BlockType.SOLID:
                    if (block.signal() > p.signal && !block.weakPowered()) {
                        decay = false;
                        p.signal = block.signal();
                    }
                    break;
                case BlockType.COMPARATOR:
                    if (block.signal() > p.signal) {
                        decay = false;
                        p.signal = block.signal();
                    }
                    break;
                case BlockType.DUST:
                    if (block.signal() > p.signal) {
                        decay = true;
                        p.signal = block.signal();
                    }
                    break;
            }
        }
        if (decay && p.signal > 0)
            p.signal--;
    }
}
