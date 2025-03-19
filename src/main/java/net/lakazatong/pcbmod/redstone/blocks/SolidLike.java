package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;

import java.util.stream.Collectors;

public class SolidLike extends Block {
    public SolidLike(BlockType type, Props p) {
        super(type, p);
    }

//    @Override
//    public void init() {
//        props.signal = 0;
//        outer:
//        for (Block input : inputs().collect(Collectors.toSet())) {
//            switch (input.type) {
//                case BlockType.AIR, BlockType.SOLID, BlockType.REDSTONE_BLOCK, BlockType.PORT:
//                    break;
//                case BlockType.REPEATER, BlockType.TORCH, BlockType.BUTTON, BlockType.LEVER:
//                    if (input.signal() > 0) {
//                        props.weakPowered = false;
//                        props.signal = 15;
//                        break outer;
//                    }
//                    break;
//                case BlockType.COMPARATOR:
//                    if (input.signal() > props.signal) {
//                        props.weakPowered = false;
//                        props.signal = input.signal();
//                    }
//                    break;
//                case BlockType.DUST:
//                    if (input.signal() > props.signal) {
//                        props.weakPowered = !input.isAbove(this) && !input.getFacing(this).up;
//                        props.signal = input.signal();
//                    }
//                    break;
//            }
//        }
//        super.init();
//    }

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
        boolean decay = true;
        for (Block input : nextInputs().collect(Collectors.toSet())) {
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
                    if (input.nextSignal() >= nextSignal()) {
                        decay = false;
                        nextProps.weakPowered = false;
                        nextProps.signal = input.nextSignal();
                    }
                    break;
                case BlockType.DUST:
                    if (input.nextSignal() > nextSignal()) {
                        decay = true;
                        // TODO: just wrong
                        // shouldnt be hard powered just because the dust is above
                        nextProps.weakPowered = !input.isAbove(this) && !input.getFacing(this).up;
                        nextProps.signal = input.nextSignal();
                    }
                    break;
            }
        }
        if (decay && nextSignal() > 0)
            nextProps.signal--;
    }
}
