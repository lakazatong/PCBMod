package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;

import java.util.stream.Collectors;

public class Solid extends Block {
    public Solid(Structure structure, Props p) {
        super(BlockType.SOLID, structure, p);
    }

    @Override
    public void init() {
        props.signal = 0;
        outer:
        for (Block input : inputs().collect(Collectors.toSet())) {
            switch (input.type) {
                case BlockType.REPEATER:
                case BlockType.TORCH:
                case BlockType.BUTTON:
                    if (input.signal() > 0) {
                        props.weakPowered = false;
                        props.signal = 15;
                        break outer;
                    }
                    break;
                case BlockType.COMPARATOR:
                    if (input.signal() > props.signal) {
                        props.weakPowered = false;
                        props.signal = input.signal();
                    }
                    break;
                case BlockType.DUST:
                    if (input.signal() > props.signal) {
                        props.weakPowered = true;
                        props.signal = input.signal();
                    }
                    break;
            }
        }
        super.init();
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

    @Override
    public void logic(long t) {
        nextProps.signal = 0;
        for (Block input : nextInputs().collect(Collectors.toSet())) {
            switch (input.type) {
                case BlockType.REPEATER:
                case BlockType.TORCH:
                case BlockType.BUTTON:
                    if (input.nextSignal() > 0) {
                        nextProps.weakPowered = false;
                        nextProps.signal = 15;
                        return;
                    }
                    break;
                case BlockType.COMPARATOR:
                    if (input.nextSignal() > nextProps.signal) {
                        nextProps.weakPowered = false;
                        nextProps.signal = input.nextSignal();
                    }
                    break;
                case BlockType.DUST:
                    if (input.nextSignal() > nextProps.signal) {
                        nextProps.weakPowered = true;
                        nextProps.signal = input.nextSignal();
                    }
                    break;
            }
        }
    }
}
