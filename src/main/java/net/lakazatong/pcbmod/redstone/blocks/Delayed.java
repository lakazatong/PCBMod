package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;

public abstract class Delayed extends Block {
    public abstract boolean getShouldPowered();
    protected abstract void setSignal();
    protected abstract void clearSignal();

    public boolean prevShouldPowered;
    public long stableTime = 0;

    public Delayed(BlockType type, Structure structure, Props p) {
        super(type, structure, p);
    }

    @Override
    public void init() {
        prevShouldPowered = getShouldPowered();
        super.init();
    }

    @Override
    public void logic() {
        boolean powered = signal() > 0;
        boolean shouldPowered = getShouldPowered();
        boolean delayOver = stableTime + 1 >= delay();

        if (delayOver) {
            if (powered != prevShouldPowered) {
                stableTime = 0;
                if (prevShouldPowered)
                    setSignal();
                else
                    clearSignal();
            }
        } else if (powered != shouldPowered) {
            stableTime++;
        }

        prevShouldPowered = shouldPowered;
    }
}
