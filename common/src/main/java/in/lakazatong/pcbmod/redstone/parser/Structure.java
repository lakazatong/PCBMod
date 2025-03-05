package in.lakazatong.pcbmod.redstone.parser;

import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.collection.ListTag;
import dev.dewy.nbt.tags.primitive.IntTag;
import in.lakazatong.pcbmod.redstone.parser.blocks.Dust;
import in.lakazatong.pcbmod.redstone.parser.blocks.Solid;
import in.lakazatong.pcbmod.redstone.parser.blocks.Torch;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Structure {
    private final double maxX;
    private final double maxY;
    private final double maxZ;
    private final List<List<List<Block>>> xyzGrid;

    public Structure(int maxX, int maxY, int maxZ) {
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.xyzGrid = new ArrayList<>();
        for (int x = 0; x < maxX; x++) {
            List<List<Block>> yzPlane = new ArrayList<>();
            for (int y = 0; y < maxY; y++) {
                List<Block> zLine = new ArrayList<>();
                for (int z = 0; z < maxZ; z++) {
                    zLine.add(null);
                }
                yzPlane.add(zLine);
            }
            xyzGrid.add(yzPlane);
        }
    }

    public List<Block> getNeighbors(Block block) {
        List<Block> neighbors = new ArrayList<>();

        for (Vec3 coords : block.coords.neighbors()) {
            if (coords.x >= 0 && coords.x < maxX &&
                    coords.y >= 0 && coords.y < maxY &&
                    coords.z >= 0 && coords.z < maxZ) {

                List<List<Block>> yzPlane = xyzGrid.get((int) coords.x);
                List<Block> zLine = yzPlane.get((int) coords.y);
                Block neighbor = zLine.get((int) coords.z);

                if (neighbor != null) {
                    neighbors.add(neighbor);
                }
            }
        }

        return neighbors;
    }

    public void setBlock(Block block) {
        Vec3 coords = block.coords;
        while (xyzGrid.size() <= coords.x)
            xyzGrid.add(new ArrayList<>());
        while (xyzGrid.get((int) coords.x).size() <= coords.y)
            xyzGrid.get((int) coords.x).add(new ArrayList<>());
        while (xyzGrid.get((int) coords.x).get((int) coords.y).size() <= coords.z)
            xyzGrid.get((int) coords.x).get((int) coords.y).add(null);
        xyzGrid.get((int) coords.x).get((int) coords.y).set((int) coords.z, block);
    }

    @Override
    public String toString() {
        if (xyzGrid.isEmpty())
            return "Empty Structure";

        StringBuilder output = new StringBuilder();
        for (int y = 0; y < maxY; y++) {
            output.append("Y-Level ").append(y).append(":\n");
            for (int x = 0; x < maxX; x++) {
                StringBuilder row = new StringBuilder();
                for (int z = 0; z < maxZ; z++) {
                    if (y < xyzGrid.get(x).size() && z < xyzGrid.get(x).get(y).size()) {
                        Block block = xyzGrid.get(x).get(y).get(z);
                        row.append(block != null ? block.type.value : "0").append(" ");
                    } else {
                        row.append("0 ");
                    }
                }
                output.append(row.toString().trim()).append("\n");
            }
            output.append("\n");
        }
        return output.toString();
    }

    public Block getFirstBlock() {
        for (List<List<Block>> yzPlane : xyzGrid) {
            if (yzPlane.isEmpty()) continue;
            for (List<Block> zLine : yzPlane) {
                if (zLine.isEmpty()) continue;
                for (Block block : zLine) {
                    if (block != null)
                        return block;
                }
            }
        }
        return null;
    }

    @FunctionalInterface
    public interface BlockBuilder {
        Block apply(Vec3 coords, Structure Structure);
    }

    private static List<BlockBuilder> convertPalette(ListTag<CompoundTag> palette) {
        List<BlockBuilder> result = new ArrayList<>();

        for (CompoundTag t : palette) {
            Map<String, Object> props = new HashMap<>();
            props.put("facings", new ArrayList<Vec3>());

            BlockBuilder builder = (coords, structure) -> new Solid(coords, structure).withProps(props);

            switch (t.getString("Name").getValue()) {
                case "minecraft:air":
                    result.add(null);
                    continue;
                case "minecraft:redstone_wire":
                    builder = (coords, structure) -> new Dust(coords, structure).withProps(props);
                    break;
                case "minecraft:redstone_torch":
                    builder = (coords, structure) -> new Torch(coords, structure).withProps(props);
                    break;
                case "minecraft:redstone_wall_torch":
                    props.put("on_wall", true);
                    builder = (coords, structure) -> new Torch(coords, structure).withProps(props);
                    break;
                default:
                    break;
            }

            if (t.contains("Properties")) {
                CompoundTag properties = t.getCompound("Properties");
                for (String key : properties.keySet()) {
                    String value = properties.getString(key).getValue();

                    if (key.equals("Facing")) {
                        props.put("facings", Vec3.fromCardinal(value));
                    } else if (Vec3.fromCardinal(key) != null && value.equals("Side")) {
                        props.put("facings", Vec3.fromCardinal(key));
                    } else if (key.equals("Lit")) {
                        props.put("lit", Boolean.parseBoolean(value));
                    } else if (key.equals("Power")) {
                        props.put("power", Integer.parseInt(value));
                    }
                }
            }

            result.add(builder);
        }

        return result;
    }


    public static Structure fromNBT(Path path) throws IOException {
        Nbt nbt = new Nbt();
        CompoundTag tag = nbt.fromFile(path.toFile());
        ListTag<CompoundTag> blocks = tag.getList("blocks");
        List<BlockBuilder> palette = convertPalette(tag.getList("palette"));
        ListTag<IntTag> dimensions = tag.getList("size");

        Structure structure = new Structure(dimensions.get(0).intValue(), dimensions.get(1).intValue(), dimensions.get(2).intValue());

        for (CompoundTag bTag : blocks) {
            ListTag<IntTag> pos = bTag.getList("pos");
            BlockBuilder builder = palette.get(bTag.getInt("state").getValue());

            if (builder == null) continue;

            Vec3 coordinates = new Vec3(pos.get(0).intValue(), pos.get(1).intValue(), pos.get(2).intValue());
            Block block = builder.apply(coordinates, structure);
            structure.setBlock(block);
        }

        return structure;
    }

}
