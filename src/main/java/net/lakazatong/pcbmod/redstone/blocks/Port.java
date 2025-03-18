package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;

public class Port extends Constant {
    public Port(Structure structure, Props initialProps) {
        super(BlockType.PORT, structure, initialProps);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        if (props.portType != PortBlock.PortType.INPUT) return false;
        return switch (neighbor.type) {
            case AIR -> false;
            case SOLID -> false;
            case DUST -> true;
            case REPEATER -> !neighbor.locked() && neighbor.isFacingAway(this);
            case COMPARATOR -> neighbor.isFacingAway(this);
            case TORCH -> (neighbor.isAbove(this) && !neighbor.onWall()) || neighbor.isOnWallOf(this);
            case BUTTON -> false;
            case LEVER -> false;
            case REDSTONE_BLOCK -> false;
            case PORT -> false;
        };
    }

    @Override
    public void logic() {
        Integer newSignal = circuit.portUpdates.remove(uuid);
        if (newSignal != null)
            nextProps.signal = newSignal;
    }
}
