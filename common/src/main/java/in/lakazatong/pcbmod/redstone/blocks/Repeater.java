package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.redstone.Vec3;

import java.util.HashSet;
import java.util.Set;

public class Repeater extends Block {
    private int delay;

    @FunctionalInterface
    private interface LogicImpl {
        int apply(double t);
    }

    private LogicImpl logicImpl;

    public Repeater(Vec3 coords, Structure structure) {
        super(BlockType.REPEATER, coords, structure);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initFromProps() {
        signal = (int) props.get("initial_power");
        delay = (int) props.get("delay") * 2;
        logicImpl = (boolean) props.get("locked") ? this::lockedLogic : this::defaultLogic;
        Iterable<Vec3> facings = (Iterable<Vec3>) props.get("facings");
        var newFacings = new HashSet<>();
        facings.forEach(facing -> newFacings.add(facing.opposite()));
        props.put("facings", newFacings);
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

    private int lockedLogic(double t) {
        // TODO
        return 0;
    }

    @Override
    public int logic(double t) {
        return logicImpl.apply(t);
    }
}