package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;

public class Button extends Block {
    public Button(Structure structure, Props p) {
        super(BlockType.BUTTON, structure, p);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR -> false;
            case SOLID -> isFacingAway(neighbor);
            case DUST -> true;
            case TORCH -> false;
            case REPEATER, COMPARATOR -> neighbor.isFacingAway(this);
            case BUTTON -> false;
            case LEVER -> false;
            case REDSTONE_BLOCK -> false;
        };
    }

    @Override
    public void logic(long t) {
        if (t + 1 >= delay() )
            nextProps.signal = 0;
    }
}
