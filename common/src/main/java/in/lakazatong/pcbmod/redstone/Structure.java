package in.lakazatong.pcbmod.redstone;

import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.collection.ListTag;
import dev.dewy.nbt.tags.primitive.IntTag;
import in.lakazatong.pcbmod.redstone.Block.BlockBuilder;
import in.lakazatong.pcbmod.redstone.blocks.Comparator;
import in.lakazatong.pcbmod.redstone.blocks.*;
import in.lakazatong.pcbmod.utils.Vec3;
import org.apache.commons.io.file.PathUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Structure {
    public final Path path;
    public final String name;

    private final double maxX;
    private final double maxY;
    private final double maxZ;
    private final List<List<List<Block>>> xyzGrid;

    private Structure(Path path, int maxX, int maxY, int maxZ) {
        this.path = path;
        this.name = PathUtils.getBaseName(path.getFileName());
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

    public boolean withinBounds(Vec3 coords) {
        return coords.x() >= 0 && coords.x() < maxX &&
                coords.y() >= 0 && coords.y() < maxY &&
                coords.z() >= 0 && coords.z() < maxZ;
    }

    public List<Block> getNeighbors(Block block) {
        return block.coords.neighbors().stream()
                .filter(this::withinBounds)
                .map(coords -> xyzGrid.get((int) coords.x()).get((int) coords.y()).get((int) coords.z()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void setBlock(Block block) {
        ensureCapacity(block.coords);
        xyzGrid.get((int) block.coords.x())
                .get((int) block.coords.y())
                .set((int) block.coords.z(), block);
    }

    private void ensureCapacity(Vec3 coords) {
        expandList(xyzGrid, (int) coords.x(), ArrayList::new);
        expandList(xyzGrid.get((int) coords.x()), (int) coords.y(), ArrayList::new);
        expandList(xyzGrid.get((int) coords.x()).get((int) coords.y()), (int) coords.z(), () -> null);
    }

    private static <T> void expandList(List<T> list, int index, Supplier<T> supplier) {
        while (list.size() <= index)
            list.add(supplier.get());
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

    private static List<BlockBuilder> convertPalette(ListTag<CompoundTag> palette) {
        List<BlockBuilder> result = new ArrayList<>();

        for (CompoundTag t : palette) {
            Set<Vec3> facings = new HashSet<>();

            BlockBuilder builder = switch (t.getString("Name").getValue()) {
                case "minecraft:air" -> null;
                case "minecraft:redstone_wire" -> new BlockBuilder(Dust::new);
                case "minecraft:repeater" -> new BlockBuilder(Repeater::new);
                case "minecraft:redstone_torch" -> new BlockBuilder(Torch::new).withProp("onWall", false);
                case "minecraft:redstone_wall_torch" -> new BlockBuilder(Torch::new).withProp("onWall", true);
                case "minecraft:stone_button" -> new BlockBuilder(Button::new).withProp("delay", 10);
                case "minecraft:wooden_button" -> new BlockBuilder(Button::new).withProp("delay", 20);
                case "minecraft:comparator" -> new BlockBuilder(Comparator::new);
                default -> new BlockBuilder(Solid::new);
            };

            if (builder == null) {
                result.add(null);
                continue;
            }

            if (t.contains("Properties")) {
                CompoundTag properties = t.getCompound("Properties");
                for (String key : properties.keySet()) {
                    String value = properties.getString(key).getValue();

                    switch (key) {
                        case "lit", "powered":
                            builder.withProp("signal", Boolean.parseBoolean(value) ? 15 : 0);
                            break;
                        case "power":
                            builder.withProp("signal", Integer.parseInt(value));
                            break;
                        case "facing":
                            facings.add(Vec3.fromCardinal(value));
                            break;
                        case "delay":
                            builder.withProp("delay", Integer.parseInt(value));
                            break;
                        case "locked":
                            builder.withProp("locked", Boolean.parseBoolean(value));
                            break;
                        case "mode":
                            builder.withProp("subtract", value.equals("subtract"));
                            break;
                    }

                    if (Vec3.fromCardinal(key) != null && value.equals("side"))
                        facings.add(Vec3.fromCardinal(key));
                }
            }

            result.add(builder.withProp("facings", facings));
        }

        return result;
    }

    public static Structure fromNBT(Path path) throws IOException {
        Nbt nbt = new Nbt();
        CompoundTag tag = nbt.fromFile(path.toFile());
        ListTag<CompoundTag> blocks = tag.getList("blocks");
        List<BlockBuilder> palette = convertPalette(tag.getList("palette"));
        ListTag<IntTag> dimensions = tag.getList("size");

        Structure structure = new Structure(path, dimensions.get(0).intValue(), dimensions.get(1).intValue(), dimensions.get(2).intValue());

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
