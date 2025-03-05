package in.lakazatong.pcbmod.redstone.parser.blocks;

import in.lakazatong.pcbmod.redstone.parser.Block;
import in.lakazatong.pcbmod.redstone.parser.BlockType;
import in.lakazatong.pcbmod.redstone.parser.Structure;
import in.lakazatong.pcbmod.redstone.parser.Vec3;

public class Dust extends Block {
    public Dust(Vec3 coords, Structure structure) {
        super(BlockType.DUST, coords, structure);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID -> this.isFacing(neighbor) || (this.isAbove(neighbor) && neighbor.type == BlockType.SOLID);
            case DUST -> true;
            case REPEATER, COMPARATOR -> neighbor.isFacingAway(this);
            default -> false;
        };
    }
}