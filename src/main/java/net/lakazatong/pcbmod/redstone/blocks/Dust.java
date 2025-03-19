package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;

import java.util.stream.Collectors;

public class Dust extends Block {
    public Dust(Props p) {
        super(BlockType.DUST, p);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR, TORCH, BUTTON, LEVER, REDSTONE_BLOCK -> false;
            case SOLID -> isFacing(neighbor) || isAbove(neighbor);
            case PORT -> neighbor.portType().equals(PortBlock.PortType.OUTPUT) && (isFacing(neighbor) || isAbove(neighbor));
            case DUST -> true;
            case REPEATER -> !neighbor.locked() && neighbor.isFacingAway(this);
            case COMPARATOR -> !neighbor.isFacing(this);
        };
    }

    @Override
    public void logic() {
        boolean decay = true;
        for (Block input : nextInputs().collect(Collectors.toSet())) {
            switch (input.type) {
                case BlockType.AIR:
                    break;
                case BlockType.LEVER, BlockType.REDSTONE_BLOCK, BlockType.REPEATER, BlockType.TORCH, BlockType.BUTTON:
                    if (input.nextSignal() > 0) {
                        nextProps.signal = 15;
                       return;
                    }
                    break;
                case BlockType.SOLID:
                    if (input.nextSignal() > nextSignal() && !input.nextWeakPowered()) {
                        decay = false;
                        nextProps.signal = input.nextSignal();
                    }
                    break;
                case BlockType.PORT, BlockType.COMPARATOR:
                    if (input.nextSignal() >= nextSignal()) {
                        decay = false;
                        nextProps.signal = input.nextSignal();
                    }
                    break;
                case BlockType.DUST:
                    if (input.nextSignal() > nextSignal()) {
                        decay = true;
                        nextProps.signal = input.nextSignal();
                    }
                    break;
            }
        }
        if (decay && nextSignal() > 0)
            nextProps.signal--;
    }
}
