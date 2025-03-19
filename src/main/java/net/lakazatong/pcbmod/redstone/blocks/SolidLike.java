package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;

import java.util.stream.Collectors;

public class SolidLike extends Block {
    public SolidLike(BlockType type, Props p) {
        super(type, p);
    }

    @Override
    public void init() {
        props.signal = 0;
        outer:
        for (Block input : inputs().collect(Collectors.toSet())) {
            switch (input.type) {
                case BlockType.AIR, BlockType.SOLID, BlockType.REDSTONE_BLOCK, BlockType.PORT:
                    break;
                case BlockType.LEVER, BlockType.REPEATER, BlockType.TORCH, BlockType.BUTTON:
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
            case AIR, SOLID, PORT, BUTTON, LEVER, REDSTONE_BLOCK -> false;
            case DUST -> true;
            case REPEATER -> !neighbor.locked() && neighbor.isFacingAway(this);
            case COMPARATOR -> neighbor.isFacingAway(this);
            case TORCH -> (neighbor.isAbove(this) && !neighbor.onWall()) || neighbor.isOnWallOf(this);
        };
    }

    @Override
    public void logic() {
        nextProps.signal = 0;
        var inputs = nextInputs().collect(Collectors.toSet());
        for (Block input : inputs) {
            switch (input.type) {
                case BlockType.AIR, BlockType.SOLID, BlockType.REDSTONE_BLOCK, BlockType.PORT:
                    break;
                case BlockType.REPEATER, BlockType.TORCH, BlockType.BUTTON, BlockType.LEVER:
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
                        nextProps.weakPowered = !input.isAbove(this) && !input.getFacing(this).up;

                        nextProps.signal = input.nextSignal();
                    }
                    break;
            }
        }
    }
}
