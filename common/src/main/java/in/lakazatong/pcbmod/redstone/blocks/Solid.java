package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.redstone.Vec3;

public class Solid extends Block {
    public Solid(Vec3 coords, Structure structure) {
        super(BlockType.SOLID, coords, structure);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case DUST -> true;
            case REPEATER, COMPARATOR -> neighbor.isFacingAway(this);
            case TORCH -> (neighbor.isAbove(this) && !neighbor.isOnWall()) || neighbor.isOnWallOf(this);
            default -> false;
        };
    }

    // Unused
    @Override
    public int logic(double t) {
        return 0;
    }
}