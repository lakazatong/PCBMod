package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.redstone.Vec3;

import java.util.Map;
import java.util.Set;

public class Comparator extends Block {
    private LogicImpl logicImpl;

    public Comparator(Vec3 coords, Structure structure) {
        super(BlockType.COMPARATOR, coords, structure);
        delay = 2;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initProps(Map<String, Object> props) {
        signal = (int) props.get("signal");
        subtract = (boolean) props.get("subtract");
        logicImpl = subtract ? this::SubtractLogic : this::defaultLogic;
        if (props.get("facings") instanceof Set<?> tmp)
            tmp.forEach(facing -> facings.add((((Vec3) facing).opposite())));
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID, DUST -> this.isFacing(neighbor);
            case REPEATER, COMPARATOR -> this.isFacing(neighbor) && !neighbor.isFacing(this);
            default -> false;
        };
    }

    private int defaultLogic(double t) {
        // TODO
        return 0;
    }

    private int SubtractLogic(double t) {
        // TODO
        return 0;
    }

    @Override
    public int logic(double t) {
        return logicImpl.apply(t);
    }
}