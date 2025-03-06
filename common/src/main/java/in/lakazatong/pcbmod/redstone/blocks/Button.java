package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.utils.Vec3;

import java.util.Map;
import java.util.Set;

public class Button extends Block {
    public Button(Vec3 coords, Structure structure) {
        super(BlockType.BUTTON, coords, structure);
        onWall = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initProps(Map<String, Object> props) {
        signal = (int) props.get("signal");
        delay = (int) props.get("delay") * 2;
        if (props.get("facings") instanceof Set<?> tmp)
            tmp.forEach(facing -> facings.add(((Vec3) facing)));
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
    public int logic(double t) {
        return t < delay ? 15 : 0;
    }
}
