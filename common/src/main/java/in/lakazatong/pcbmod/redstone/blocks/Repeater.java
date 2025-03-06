package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.redstone.Vec3;

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

    @Override
    protected void initFromProps() {
        signal = (int) props.get("initial_power");
        delay = (int) props.get("delay") * 2;
        logicImpl = (boolean) props.get("locked") ? this::lockedLogic : this::defaultLogic;
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