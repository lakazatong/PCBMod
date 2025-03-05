package in.lakazatong.pcbmod.redstone.parser.blocks;

import in.lakazatong.pcbmod.redstone.parser.Block;
import in.lakazatong.pcbmod.redstone.parser.BlockType;
import in.lakazatong.pcbmod.redstone.parser.Structure;
import in.lakazatong.pcbmod.redstone.parser.Vec3;

public class Comparator extends Block {
    public Comparator(Vec3 coords, Structure structure) {
        super(BlockType.COMPARATOR, coords, structure);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID, DUST -> this.isFacing(neighbor);
            case REPEATER, COMPARATOR -> this.isFacing(neighbor) && !neighbor.isFacing(this);
            default -> false;
        };
    }

    @Override
    public boolean isOutputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID, TORCH -> this.isFacingAway(neighbor);
            case DUST, REPEATER, COMPARATOR -> !this.isFacing(neighbor);
            default -> false;
        };
    }
}