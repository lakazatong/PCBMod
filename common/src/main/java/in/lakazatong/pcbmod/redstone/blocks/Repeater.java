package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.utils.Vec3;

import java.util.stream.Collectors;

public class Repeater extends Delayed {
    public Repeater(Structure structure, Props p) {
        super(BlockType.REPEATER, structure, p);
        props.facings = facings().stream().map(Vec3::opposite).collect(Collectors.toSet());
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID, DUST -> this.isFacing(neighbor);
            case REPEATER -> !neighbor.locked() && this.isFacing(neighbor) && !neighbor.isFacing(this);
            case COMPARATOR -> this.isFacing(neighbor) && !neighbor.isFacing(this);
            default -> false;
        };
    }

    @Override
    public boolean getShouldPowered() {
        return rearInputs().anyMatch(i -> i.signal() > 0);
    }

    @Override
    protected void setSignal(long t) {
        nextProps.signal = 15;
    }

    @Override
    protected void clearSignal(long t) {
        nextProps.signal = 0;
    }

    @Override
    public void logic(long t) {
        super.logic(t);
        nextProps.locked = sideInputs().anyMatch(i -> i.signal() > 0);
    }
}
