package in.lakazatong.pcbmod.redstone.parser.blocks;

import in.lakazatong.pcbmod.redstone.parser.Block;
import in.lakazatong.pcbmod.redstone.parser.BlockType;
import in.lakazatong.pcbmod.redstone.parser.Structure;
import in.lakazatong.pcbmod.redstone.parser.Vec3;

public class Torch extends Block {
    public Torch(Vec3 coords, Structure structure) {
        super(BlockType.TORCH, coords, structure);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID -> neighbor.isAbove(this);
            case DUST -> true;
            case REPEATER, COMPARATOR -> neighbor.isFacingAway(this);
            default -> false;
        };
    }

    @Override
    public boolean isOutputOf(Block neighbor) {
        if (neighbor.type == BlockType.SOLID) {
            return (this.isAbove(neighbor) && !this.isOnWall()) || this.isOnWallOf(neighbor);
        }
        return false;
    }
}