package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.redstone.Vec3;

import java.util.Map;
import java.util.Set;

public class Torch extends Block {

    public Torch(Vec3 coords, Structure structure) {
        super(BlockType.TORCH, coords, structure);
        delay = 2; // 1 redstone tick * 2 = 2
    }

    @Override
    protected void initProps(Map<String, Object> props) {
        onWall = (boolean) props.get("onWall");
        signal = (int) props.get("signal");
        if (props.get("facings") instanceof Set<?> tmp) {
            assert facings.isEmpty();
            tmp.forEach(facing -> facings.add(((Vec3) facing)));
        }
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
        // TODO: consider the 1 redstone tick delay of the torch
        for (Block block : inputs) {
            if (block.signal > 0)
                return 0;
        }
        return 15;
    }
}