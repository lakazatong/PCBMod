package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;

import java.util.stream.Collectors;

public class Solid extends SolidLike {
    public Solid(Structure structure, Props p) {
        super(BlockType.SOLID, structure, p);
    }

    @Override
    public void init() {
        props.signal = 0;
        outer:
        for (Block input : inputs().collect(Collectors.toSet())) {
            switch (input.type) {
                case BlockType.REPEATER:
                case BlockType.TORCH:
                case BlockType.BUTTON:
                    if (input.signal() > 0) {
                        props.weakPowered = false;
                        props.signal = 15;
                        break outer;
                    }
                    break;
                case BlockType.COMPARATOR:
                    if (input.signal() > props.signal) {
                        props.weakPowered = false;
                        props.signal = input.signal();
                    }
                    break;
                case BlockType.DUST:
                    if (input.signal() > props.signal) {
                        props.weakPowered = true;
                        props.signal = input.signal();
                    }
                    break;
            }
        }
        super.init();
    }
}
