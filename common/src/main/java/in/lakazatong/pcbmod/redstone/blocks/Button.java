package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.utils.Vec3;

public class Button extends Block {
    public Button(Vec3 coords, Structure structure) {
        super(BlockType.BUTTON, coords, structure);
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
    public void logic(double t, Props p) {
        p.signal = t < delay() ? 15 : 0;
    }
}
