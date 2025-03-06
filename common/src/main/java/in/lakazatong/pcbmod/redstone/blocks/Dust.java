package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.redstone.Vec3;

public class Dust extends Block {
    public Dust(Vec3 coords, Structure structure) {
        super(BlockType.DUST, coords, structure);
    }

    // no need to bother with the properties here since this block will not appear in the circuit graph

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID -> this.isFacing(neighbor) || (this.isAbove(neighbor) && neighbor.type == BlockType.SOLID);
            case DUST -> true;
            case REPEATER, COMPARATOR -> neighbor.isFacingAway(this);
            default -> false;
        };
    }

    // Unused
    @Override
    public int logic(double t) {
        return 0;
    }
}