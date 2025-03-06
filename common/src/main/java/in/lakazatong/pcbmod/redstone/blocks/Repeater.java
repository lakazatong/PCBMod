package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.utils.Vec3;

import java.util.stream.Collectors;

public class Repeater extends Block {
    private LogicImpl logicImpl;

    public Repeater(Vec3 coords, Structure structure) {
        super(BlockType.REPEATER, coords, structure);
        logicImpl = locked() ? this::lockedLogic : this::defaultLogic;
        props.facings = facings().stream().map(Vec3::opposite).collect(Collectors.toSet());
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID, DUST -> this.isFacing(neighbor);
            case REPEATER, COMPARATOR -> this.isFacing(neighbor) && !neighbor.isFacing(this);
            default -> false;
        };
    }

    private void defaultLogic(double t, Props p) {
        // TODO
    }

    private void lockedLogic(double t, Props p) {
        // TODO
    }

    @Override
    public void logic(double t, Props p) {
        logicImpl.apply(t, p);
    }
}
