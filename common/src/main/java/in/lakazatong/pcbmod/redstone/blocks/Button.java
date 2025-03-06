package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;

public class Button extends Block {
    public Button(Structure structure, Props p) {
        super(BlockType.BUTTON, structure, p);
        props.onWall = true;
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID -> this.isOnWallOf(neighbor);
            case DUST -> true;
            case REPEATER, COMPARATOR -> neighbor.isFacingAway(this);
            default -> false;
        };
    }

    @Override
    public void logic(long t, Props p) {
        p.signal = t < delay() ? 15 : 0;
    }
}
