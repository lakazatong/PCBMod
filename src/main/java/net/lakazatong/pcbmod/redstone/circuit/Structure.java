package net.lakazatong.pcbmod.redstone.circuit;

import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.collection.ListTag;
import dev.dewy.nbt.tags.primitive.IntTag;
import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.redstone.blocks.*;
import net.lakazatong.pcbmod.redstone.circuit.Block.BlockBuilder;
import net.lakazatong.pcbmod.redstone.utils.Direction;
import net.lakazatong.pcbmod.redstone.utils.Vec3;
import org.apache.commons.io.file.PathUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Structure {
    public final Path path;
    public final String name;

    private final long maxX;
    private final long maxY;
    private final long maxZ;
    private final List<List<List<Block>>> xyzGrid;

    public Structure(Path path) throws IOException {
        Nbt nbt = new Nbt();
        CompoundTag tag = nbt.fromFile(path.toFile());
        ListTag<CompoundTag> blocks = tag.getList("blocks");
        List<BlockBuilder> palette = convertPalette(tag.getList("palette"));
        ListTag<IntTag> dimensions = tag.getList("size");

        this.path = path;
        this.name = PathUtils.getBaseName(path.getFileName());
        this.maxX = dimensions.get(0).intValue();
        this.maxY = dimensions.get(1).intValue();
        this.maxZ = dimensions.get(2).intValue();
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

        for (CompoundTag bTag : blocks) {
            ListTag<IntTag> tmp = bTag.getList("pos");
            Vec3 pos = new Vec3(tmp.get(0).intValue(), tmp.get(1).intValue(), tmp.get(2).intValue());
            BlockBuilder builder = palette.get(bTag.getInt("state").getValue());
            if (builder == null) continue;
            Block block = builder.apply(pos);
            if (block instanceof Port port) {
                CompoundTag bNbt = bTag.get("nbt");
                port.props.signal = bNbt.getInt("signal").intValue();
                port.props.portNumber = bNbt.getInt("portNumber").intValue();
            }
            setBlock(block);
        }
    }

    public boolean withinBounds(Vec3 coords) {
        return coords.x() >= 0 && coords.x() < maxX &&
                coords.y() >= 0 && coords.y() < maxY &&
                coords.z() >= 0 && coords.z() < maxZ;
    }

    public List<Block> getNeighbors(Block block) {
        return block.coords().neighbors().stream()
                .filter(this::withinBounds)
                .map(coords -> xyzGrid.get(coords.x()).get(coords.y()).get(coords.z()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Block getBlock(Vec3 coords) {
        return xyzGrid.get(coords.x())
                .get(coords.y())
                .get(coords.z());
    }

    public void setBlock(Block block) {
        ensureCapacity(block.coords());
        xyzGrid.get(block.coords().x())
                .get(block.coords().y())
                .set(block.coords().z(), block);
    }

    private void ensureCapacity(Vec3 coords) {
        expandList(xyzGrid, coords.x(), ArrayList::new);
        expandList(xyzGrid.get(coords.x()), coords.y(), ArrayList::new);
        expandList(xyzGrid.get(coords.x()).get(coords.y()), coords.z(), () -> null);
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

    public Stream<Block> blocks() {
        return xyzGrid.stream().flatMap(Collection::stream).flatMap(Collection::stream).filter(Objects::nonNull);
    }

    private static List<BlockBuilder> convertPalette(ListTag<CompoundTag> palette) {
        List<BlockBuilder> result = new ArrayList<>();

        for (CompoundTag t : palette) {
            BlockBuilder builder = switch (t.getString("Name").getValue()) {
                case "minecraft:air" -> null;
                case "minecraft:redstone_wire" -> new BlockBuilder(Dust::new);
                case "minecraft:repeater" -> new BlockBuilder(Repeater::new);
                case "minecraft:redstone_torch" -> {
                    BlockBuilder b = new BlockBuilder(Torch::new);
                    b.commonInitialProps.delay = 2;
                    yield b;
                }
                case "minecraft:redstone_wall_torch" -> {
                    BlockBuilder b = new BlockBuilder(Torch::new);
                    b.commonInitialProps.delay = 2;
                    b.commonInitialProps.onWall = true;
                    yield b;
                }
                case "minecraft:comparator" ->{
                    BlockBuilder b = new BlockBuilder(Comparator::new);
                    b.commonInitialProps.delay = 2;
                    yield b;
                }
                case "minecraft:stone_button" -> {
                    BlockBuilder b = new BlockBuilder(Button::new);
                    b.commonInitialProps.onWall = true;
                    b.commonInitialProps.delay = 20;
                    yield b;
                }
                case "minecraft:wooden_button" -> {
                    BlockBuilder b = new BlockBuilder(Button::new);
                    b.commonInitialProps.delay = 40;
                    yield b;
                }
                case "minecraft:lever" -> new BlockBuilder(Lever::new);
                case "minecraft:redstone_block" -> {
                    BlockBuilder b = new BlockBuilder(RedstoneBlock::new);
                    b.commonInitialProps.signal = 15;
                    yield b;
                }
                case "pcbmod:port" -> new BlockBuilder(Port::new);
                default -> new BlockBuilder(Solid::new);
            };

            if (builder == null) {
                result.add(null);
                continue;
            }

            if (t.contains("Properties")) {
                CompoundTag properties = t.getCompound("Properties");
                boolean hasFacing = properties.contains("facing");
                boolean hasFace = properties.contains("face");

                assert !hasFace || hasFacing; // checks that hasFace implies hasFacing

                String facingValue = hasFacing ? properties.getString("facing").getValue() : null;
                String faceValue = hasFace ? properties.getString("face").getValue() : null;

                for (String key : properties.keySet()) {
                    String value = properties.getString(key).getValue();

                    switch (key) {
                        case "lit", "powered":
                            builder.commonInitialProps.signal = Boolean.parseBoolean(value) ? 15 : 0;
                            break;
                        case "power":
                            builder.commonInitialProps.signal = Integer.parseInt(value);
                            break;
                        case "delay":
                            builder.commonInitialProps.delay = Integer.parseInt(value) * 2L;
                            break;
                        case "locked":
                            builder.commonInitialProps.locked = Boolean.parseBoolean(value);
                            break;
                        case "mode":
                            builder.commonInitialProps.subtract = value.equals("subtract");
                            break;
                        case "type":
                            builder.commonInitialProps.portType = PortBlock.PortType.of(value);
                            break;
                    }

                    // dust
                    Direction tmp = Direction.fromCardinal(key);
                    if (tmp != null) {
                        if (value.equals("side"))
                            builder.commonInitialProps.facings.add(tmp);
                        else if (value.equals("up")) {
                            tmp.up = true;
                            builder.commonInitialProps.facings.add(tmp);
                        }
                    }
                }

                // button, lever
                if (hasFace) {
                    if (faceValue.equals("wall")) {
                        builder.commonInitialProps.onWall = true;
                        builder.commonInitialProps.facings.add(Direction.fromCardinal(facingValue));
                    } else if (faceValue.equals("floor")) {
                        builder.commonInitialProps.facings.add(Direction.fromCardinal("up"));
                    } else if (faceValue.equals("ceiling")) {
                        builder.commonInitialProps.facings.add(Direction.fromCardinal("down"));
                    }
                } else if (hasFacing) {
                    builder.commonInitialProps.facings.add(Direction.fromCardinal(facingValue));
                }
            }

            result.add(builder);
        }

        return result;
    }
}
