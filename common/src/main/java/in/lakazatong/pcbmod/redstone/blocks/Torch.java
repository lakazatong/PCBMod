package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.redstone.Vec3;

public class Torch extends Block {
    public Torch(Vec3 coords, Structure structure) {
        super(BlockType.TORCH, coords, structure);
    }

    @Override
    protected void initFromProps() {
        signal = props.getOrDefault("lit", "false").equals("true") ? 15 : 0;
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
    public int logic(double t) {
        for (Block block : inputs) {
            if (block.signal > 0)
                return 0;
        }
        return 15;
    }
}