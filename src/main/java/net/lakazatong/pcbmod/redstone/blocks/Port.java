package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;

public class Port extends SolidLike {
    public Port(Structure structure, Props initialProps) {
        super(BlockType.PORT, structure, initialProps);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return portType().equals(PortBlock.PortType.INPUT) && super.isInputOf(neighbor);
    }

    @Override
    public void logic() {
        if (portType().equals(PortBlock.PortType.INPUT)) {
            Integer newSignal = circuit.portUpdates.remove(uuid);
            if (newSignal != null)
                nextProps.signal = newSignal;
        } else if (portType().equals(PortBlock.PortType.OUTPUT)) {
            super.logic();
        }
    }
}
