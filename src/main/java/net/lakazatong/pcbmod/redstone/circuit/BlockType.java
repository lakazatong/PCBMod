package net.lakazatong.pcbmod.redstone.circuit;

public enum BlockType {
    AIR(0),
    SOLID(1),
    DUST(2),
    REPEATER(3),
    TORCH(4),
    COMPARATOR(5),
    BUTTON(6),
    LEVER(7),
    REDSTONE_BLOCK(8),
    PORT(9);

    public final int value;

    BlockType(int value) {
        this.value = value;
    }
}
