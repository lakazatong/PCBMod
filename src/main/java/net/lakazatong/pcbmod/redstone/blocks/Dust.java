package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;

import java.util.stream.Collectors;

public class Dust extends Block {
    public Dust(Structure structure, Props p) {
        super(BlockType.DUST, structure, p);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR, TORCH, BUTTON, LEVER, REDSTONE_BLOCK -> false;
            case SOLID, PORT -> isFacingHorizontally(neighbor) || isAbove(neighbor);
            case DUST -> true;
            case REPEATER -> !neighbor.locked() && neighbor.isFacingAwayHorizontally(this);
            case COMPARATOR -> !neighbor.isFacing(this);
        };
    }

    @Override
    public void logic() {
        boolean decay = true;
        nextProps.signal = 0;
        for (Block input : nextInputs().collect(Collectors.toSet())) {
            switch (input.type) {
                case BlockType.AIR:
                    break;
                case BlockType.LEVER:
                case BlockType.REDSTONE_BLOCK:
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
                case BlockType.PORT:
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
