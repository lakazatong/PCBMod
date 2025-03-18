package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;
import net.lakazatong.pcbmod.redstone.utils.Vec3;

import java.util.stream.Collectors;

public class SolidLike extends Block {
    public SolidLike(BlockType type, Structure structure, Props p) {
        super(type, structure, p);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR, SOLID, PORT, BUTTON, LEVER, REDSTONE_BLOCK -> false;
            case DUST -> true;
            case REPEATER -> !neighbor.locked() && neighbor.isFacingAway(this);
            case COMPARATOR -> neighbor.isFacingAway(this);
            case TORCH -> (neighbor.isAbove(this) && !neighbor.onWall()) || neighbor.isOnWallOf(this);
        };
    }

    @Override
    public void logic() {
        nextProps.signal = 0;
        for (Block input : nextInputs().collect(Collectors.toSet())) {
            switch (input.type) {
                case BlockType.AIR:
                case BlockType.SOLID:
                case BlockType.REDSTONE_BLOCK:
                case BlockType.PORT:
                    break;
                case BlockType.REPEATER:
                case BlockType.TORCH:
                case BlockType.BUTTON:
                case BlockType.LEVER:
                    if (input.nextSignal() > 0) {
                        nextProps.weakPowered = false;
                        nextProps.signal = 15;
                        return;
                    }
                    break;
                case BlockType.COMPARATOR:
                    if (input.nextSignal() > nextProps.signal) {
                        nextProps.weakPowered = false;
                        nextProps.signal = input.nextSignal();
                    }
                    break;
                case BlockType.DUST:
                    if (input.nextSignal() > nextProps.signal) {
                        Vec3 horizontalFacing = input.nextFacings().stream()
                                .filter(f -> input.coords().add(new Vec3(f.x(), 0, f.z())).equals(coords()))
                                .findFirst()
                                .orElse(null);

                        nextProps.weakPowered = horizontalFacing != null && horizontalFacing.y() == 0;

                        nextProps.signal = input.nextSignal();
                    }
                    break;
            }
        }
    }
}
