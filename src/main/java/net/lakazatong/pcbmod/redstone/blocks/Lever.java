package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;

public class Lever extends Constant {
    public Lever(Structure structure, Props p) {
        super(BlockType.LEVER, structure, p);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR, TORCH, BUTTON, LEVER, REDSTONE_BLOCK -> false;
            case SOLID, PORT -> isFacingAway(neighbor);
            case DUST -> true;
            case REPEATER, COMPARATOR -> neighbor.isFacingAway(this);
        };
    }
}
