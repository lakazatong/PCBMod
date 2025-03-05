package in.lakazatong.pcbmod.redstone.parser;

import in.lakazatong.pcbmod.redstone.parser.blocks.Dust;
import in.lakazatong.pcbmod.redstone.parser.blocks.Solid;
import in.lakazatong.pcbmod.redstone.parser.blocks.Torch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;
import static net.minecraft.nbt.Tag.TAG_DOUBLE;

public class Structure {
    private List<List<List<Block>>> xyzGrid;

    public Structure() {
        this.xyzGrid = new ArrayList<>();
    }

    public Structure(int maxX, int maxY, int maxZ) {
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
            if (coords.x >= xyzGrid.size()) continue;
            List<List<Block>> yzPlane = xyzGrid.get(coords.x);
            if (coords.y >= yzPlane.size()) continue;
            List<Block> zLine = yzPlane.get(coords.y);
            if (coords.z >= zLine.size()) continue;
            Block neighbor = zLine.get(coords.z);
            if (neighbor != null)
                neighbors.add(neighbor);
        }
        return neighbors;
    }

    public void setBlock(Block block) {
        Vec3 coords = block.coords;
        while (xyzGrid.size() <= coords.x)
            xyzGrid.add(new ArrayList<>());
        while (xyzGrid.get(coords.x).size() <= coords.y)
            xyzGrid.get(coords.x).add(new ArrayList<>());
        while (xyzGrid.get(coords.x).get(coords.y).size() <= coords.z)
            xyzGrid.get(coords.x).get(coords.y).add(null);
        xyzGrid.get(coords.x).get(coords.y).set(coords.z, block);
    }

    @Override
    public String toString() {
        if (xyzGrid.isEmpty()) {
            return "Empty Structure";
        }

        int maxX = xyzGrid.size();
        int maxY = xyzGrid.stream().mapToInt(List::size).max().orElse(0);
        int maxZ = xyzGrid.stream()
                .flatMap(List::stream)
                .mapToInt(List::size)
                .max()
                .orElse(0);

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
                    if (block != null) {
                        return block;
                    }
                }
            }
        }
        return null;
    }

    @FunctionalInterface
    public interface BlockBuilder {
        Block apply(Vec3 coords, Structure Structure);
    }

    private static List<BlockBuilder> convertPalette(ListTag palette) {
        List<BlockBuilder> result = new ArrayList<>();

        for (Tag tag : palette) {
            CompoundTag t = (CompoundTag) tag;
            Map<String, Object> props = new HashMap<>();
            props.put("facings", new ArrayList<Vec3>());

            BlockBuilder builder = (coords, Structure) -> new Solid(coords, Structure).withProps(props);

            switch (t.getString("Name")) {
                case "minecraft:air":
                    result.add(null);
                    continue;
                case "minecraft:redstone_wire":
                    builder = (coords, Structure) -> new Dust(coords, Structure).withProps(props);
                    break;
                case "minecraft:redstone_torch":
                    builder = (coords, Structure) -> new Torch(coords, Structure).withProps(props);
                    break;
                case "minecraft:redstone_wall_torch":
                    props.put("on_wall", true);
                    builder = (coords, Structure) -> new Torch(coords, Structure).withProps(props);
                    break;
                default:
                    break;
            }

            if (t.contains("Properties")) {
                CompoundTag properties = t.getCompound("Properties");
                for (String key : properties.getAllKeys()) {
                    String value = properties.getString(key);

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
        CompoundTag tag = NbtIo.read(path);
        assert tag != null;
        ListTag blocks = tag.getList("blocks", TAG_COMPOUND);
        List<BlockBuilder> palette = convertPalette(tag.getList("palette", TAG_COMPOUND));

        Structure Structure = new Structure();

        for (Tag blockTag : blocks) {
            CompoundTag bTag = (CompoundTag) blockTag;
            ListTag pos = bTag.getList("Pos", TAG_DOUBLE);
            BlockBuilder builder = palette.get(bTag.getInt("State"));

            if (builder == null) continue;

            Block block = builder.apply(new Vec3((int)pos.getDouble(0), (int)pos.getDouble(1), (int)pos.getDouble(2)), Structure);
            Structure.setBlock(block);
        }

        return Structure;
    }
}
