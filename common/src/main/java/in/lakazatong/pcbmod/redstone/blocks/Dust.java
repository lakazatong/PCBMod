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
    public void logic(long t) {
        boolean decay = true;
        nextProps.signal = 0;
        for (Block input : nextInputs().collect(Collectors.toSet())) {
            switch (input.type) {
                case BlockType.REPEATER:
                case BlockType.TORCH:
                case BlockType.BUTTON:
                    if (input.nextSignal() > 0) {
                        nextProps.signal = 15;
                       return;
                    }
                    break;
                case BlockType.SOLID:
                    if (input.nextSignal() > nextProps.signal && !input.nextWeakPowered()) {
                        decay = false;
                        nextProps.signal = input.nextSignal();
                    }
                    break;
                case BlockType.COMPARATOR:
                    if (input.nextSignal() > nextProps.signal) {
                        decay = false;
                        nextProps.signal = input.nextSignal();
                    }
                    break;
                case BlockType.DUST:
                    if (input.nextSignal() > nextProps.signal) {
                        decay = true;
                        nextProps.signal = input.nextSignal();
                    }
                    break;
            }
        }
        if (decay && nextProps.signal > 0)
            nextProps.signal--;
    }
}
