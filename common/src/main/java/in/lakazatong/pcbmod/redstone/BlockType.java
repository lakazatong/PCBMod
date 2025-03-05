package in.lakazatong.pcbmod.redstone;

public enum BlockType {
    AIR(0), SOLID(1), DUST(2), REPEATER(3), TORCH(4), COMPARATOR(5);

    public final int value;

    BlockType(int value) {
        this.value = value;
    }
}