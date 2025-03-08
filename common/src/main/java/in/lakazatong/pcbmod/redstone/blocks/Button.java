package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;

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
