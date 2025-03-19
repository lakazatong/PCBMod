package net.lakazatong.pcbmod.redstone.circuit;


import com.github.kokorin.jaffree.ffmpeg.*;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import net.lakazatong.pcbmod.PCBMod;
import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.block.entity.HubBlockEntity;
import net.lakazatong.pcbmod.redstone.blocks.Button;
import net.lakazatong.pcbmod.redstone.blocks.Delayed;
import net.lakazatong.pcbmod.redstone.blocks.Port;
import net.lakazatong.pcbmod.redstone.blocks.SolidLike;
import net.lakazatong.pcbmod.redstone.utils.Direction;
import net.lakazatong.pcbmod.redstone.utils.SccGraph;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.io.file.PathUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Circuit {
    private ServerWorld world;

    public final Structure structure;
    public final Map<Integer, Block> graph;
    public long time = 0; // in game ticks
    public Set<BlockPos> hubs = new HashSet<>();

    private final Map<Integer, Block> portNumbers = new HashMap<>();

    public final Map<Integer, Integer> portUpdates = new HashMap<>();

    private Circuit(Structure structure, Map<Integer, Block> graph) {
        this.structure = structure;
        this.graph = graph;

        if (structure != null) {
            if (graph.isEmpty()) {
                structure.blocks().forEach(b -> graph.put(b.uuid, b));
                for (Block b : graph.values())
                    b.props.neighbors.addAll(structure.getNeighbors(b));
                graph.values().forEach(Block::init);
            }

            if (PCBMod.DEBUG) {
                Path framesDir = structure.path.resolveSibling("frames");
                try (Stream<Path> paths = Files.walk(framesDir)) {
                    paths.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (IOException ignored) {
                }

                structure.path.resolveSibling(PathUtils.getBaseName(structure.path) + ".mp4").toFile().delete();

                try {
                    Files.createDirectory(framesDir);
                } catch (IOException ignored) {
                }
            }
        }

        for (Block b : graph.values()) {
            if (b.type.equals(BlockType.PORT))
                portNumbers.put(b.props.portNumber, b);
            b.circuit = this;
        }

        firstStep();
    }

    public Circuit(Structure structure) {
        this(structure, new HashMap<>());
    }

    public Circuit(Path nbtPath) throws IOException {
        this(new Structure(nbtPath));
    }

    public Circuit(Map<Integer, Block> graph) {
        this(null, graph);
    }

    private boolean update() {
        boolean changing = false;
        for (Block b : graph.values()) {

            if (!b.props.equals(b.nextProps)) {
                changing = true;
                if (b instanceof Port port)
                    sendOutputSignal(port);
            }
            else if (b instanceof Delayed delayed && delayed.stableTime > 0) {
                changing = true;
            }

            if (b instanceof Delayed
                || b instanceof Button
                || b instanceof Port
            )
                b.dirty = true;

           b.props = b.nextProps.dup();
        }
        return changing;
    }

    public void tick() {
        SccGraph sccGraph = new SccGraph(graph);
        Queue<Integer> queue = new LinkedList<>();

        for (int sccId = 0; sccId < sccGraph.sccs.size(); sccId++) {
            if (sccGraph.inputs(sccId).isEmpty())
                queue.add(sccId);
        }

        while (!queue.isEmpty()) {
            int sccId = queue.poll();
            Set<Block> blocks = sccGraph.sccs.get(sccId);

            do {
                Set<Block> dirtyBlocks = blocks.stream().filter(b -> b.dirty).collect(Collectors.toSet());
                if (dirtyBlocks.isEmpty()) break;
                dirtyBlocks.forEach(Block::tick);
            } while (true);

            sccGraph.outputs(sccId).stream().filter(i -> !queue.contains(i)).forEach(queue::add);
        }
    }

    public void firstStep() {
        // TODO
        // comparator_test:

        // from structure
        // 14 15 ? 15 14
        // 15 15 ? 15 15
        // ->
        // 0 tick later (expected state after this function)
        // 14 14 14 14 14
        // 13 15 15 15 15

        for (Block b : graph.values()) {
            if (b.isInstant())
                b.dirty = true;
            b.props = b.nextProps.dup();
        }
        tick();
        if (PCBMod.DEBUG)
            saveAsDot();
        time++;
    }

    // return if the circuit is stable
    public boolean step(long timeout) {
        boolean changing = update();
        if (PCBMod.DEBUG)
            saveAsDot();
        // time + 1 > timeout instead of time >= timeout allows for no timeout if timeout is Long.MAX_VALUE
        if ((!changing && portUpdates.isEmpty()) || time + 1 > timeout)
            return true;
        time++;
        tick();
        return false;
    }

    public boolean step() {
        // no timeout by default
        return step(Long.MAX_VALUE);
    }

    public void stabilize(long timeout) {
        do {
            if (step(timeout))
                break;
        } while (true);
    }

    public void stabilize() {
        // default timeout of 10s
        stabilize(time + 200);
    }

    public String toDot() {
        StringBuilder dotBuilder = new StringBuilder();
        dotBuilder.append("digraph G {\n");

        Set<String> edges = new HashSet<>();

        dotBuilder.append("    graph [bgcolor=\"black\"];\n");

        for (Block block : graph.values()) {
            int signal = block.props.signal;
            int greenBlue = 255 - (int) ((signal / 15.0) * 255);
            dotBuilder.append("    \"").append(block.uuid).append("\" ")
                    .append("[label=\"").append(block.type.name().toLowerCase())
                    .append("\n").append(signal).append("\", style=filled, fillcolor=\"black\", fontcolor=\"white\", color=\"")
                    .append(String.format("#ff%02x%02x", greenBlue, greenBlue))
                    .append("\", penwidth=2];\n");

            block.inputs().forEach(input -> {
                String edge = "\"" + input.uuid + "\" -> \"" + block.uuid + "\"";
                if (edges.add(edge)) {
                    dotBuilder.append("    ").append(edge).append(" [color=\"white\", penwidth=2];\n");
                }
            });
        }

        return dotBuilder.append("}\n").toString();
    }

    public void animate() {
        assert structure != null;
        Path framesDir = structure.path.resolveSibling("frames");
        Path outputPath = structure.path.resolveSibling(PathUtils.getBaseName(structure.path) + ".mp4").toAbsolutePath();

        int maxWidth = 0, maxHeight = 0;
        for (File file : Objects.requireNonNull(framesDir.toFile().listFiles((dir, name) -> name.endsWith(".png")))) {
            try {
                BufferedImage img = ImageIO.read(file);
                if (img != null) {
                    maxWidth = Math.max(maxWidth, img.getWidth());
                    maxHeight = Math.max(maxHeight, img.getHeight());
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading frame: " + file.getName(), e);
            }
        }

        File[] frameFiles = Objects.requireNonNull(framesDir.toFile().listFiles((dir, name) -> name.endsWith(".png")));
        int frameCount = frameFiles.length;

        FrameProducer producer = getFrameProducer(maxWidth, maxHeight, framesDir, frameCount);

        FFmpeg.atPath()
                .addInput(FrameInput.withProducer(producer).setFrameRate(1))
                .addOutput(UrlOutput.toUrl(outputPath.toString()))
                .execute();
    }

    private FrameProducer getFrameProducer(int maxWidth, int maxHeight, Path framesDir, int frameCount) {
        assert structure != null;
        return new FrameProducer() {
            private long index = 0;
            private BufferedImage cachedLastFrame = null;

            public List<com.github.kokorin.jaffree.ffmpeg.Stream> produceStreams() {
                return Collections.singletonList(new com.github.kokorin.jaffree.ffmpeg.Stream()
                        .setType(com.github.kokorin.jaffree.ffmpeg.Stream.Type.VIDEO)
                        .setTimebase(1000L)
                        .setWidth(maxWidth)
                        .setHeight(maxHeight)
                );
            }

            public Frame produce() {
                if (index >= frameCount * 2L)
                    return null;
                try {
                    BufferedImage formatted;
                    if (index < frameCount) {
                        File frameFile = framesDir.resolve(PathUtils.getBaseName(structure.path) + index + ".png").toAbsolutePath().toFile();
                        BufferedImage original = ImageIO.read(frameFile);
                        formatted = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_3BYTE_BGR);
                        Graphics2D g = formatted.createGraphics();
                        g.setColor(Color.BLACK);
                        g.fillRect(0, 0, maxWidth, maxHeight);
                        if (original != null) {
                            int x = (maxWidth - original.getWidth()) / 2;
                            int y = (maxHeight - original.getHeight()) / 2;
                            g.drawImage(original, x, y, null);
                        }
                        g.dispose();
                        cachedLastFrame = formatted;
                    } else {
                        formatted = cachedLastFrame;
                    }
                    return Frame.createVideoFrame(0, ++index * 1000, formatted);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public void saveAsDot() {
        assert structure != null;
        String dotPath = structure.path.resolveSibling("frames/" + PathUtils.getBaseName(structure.path) + time + ".dot").toAbsolutePath().toString();
        String pngPath = structure.path.resolveSibling("frames/" + PathUtils.getBaseName(structure.path) + time + ".png").toAbsolutePath().toString();

        String dot = toDot();

        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(Path.of(dotPath).toFile()))) {
                writer.write(dot);
            }

            MutableGraph graph = new Parser().read(dot);
            Graphviz.fromGraph(graph).render(Format.PNG).toFile(new File(pngPath));
        } catch (IOException ignored) {
        }
    }

    public NbtCompound save() {
        NbtCompound tag = new NbtCompound();

        NbtList savedGraph = new NbtList();
        graph.forEach((uuid, block) -> savedGraph.add(block.save()));
        tag.put("graph", savedGraph);
        tag.putLong("time", time);
        NbtList hubsTag = new NbtList();
        hubs.forEach(pos -> hubsTag.add(new NbtIntArray(List.of(pos.getX(), pos.getY(), pos.getZ()))));
        tag.put("hubs", hubsTag);

        return tag;
    }

    public static Circuit load(NbtElement tag) {
        NbtCompound t = ((NbtCompound) tag);

        Map<Integer, Block> graph = new HashMap<>();
        t.getList("graph", NbtElement.COMPOUND_TYPE).forEach(blockTag -> {
            Block b = Block.load(blockTag);
            graph.put(b.uuid, b);
        });
        Circuit circuit = new Circuit(graph);
        graph.values().forEach(Block::restoreNeighbors);
        circuit.time = t.getLong("time");
        t.getList("hubs", NbtElement.INT_ARRAY_TYPE).forEach(e -> {
            NbtIntArray pos = (NbtIntArray) e;
            circuit.hubs.add(new BlockPos(pos.get(0).intValue(), pos.get(1).intValue(), pos.get(2).intValue()));
        });

        return circuit;
    }

    public int getOutputSignal(int portNumber) {
        Block p = portNumbers.get(portNumber);
        return p != null && p.portType() == PortBlock.PortType.OUTPUT ? p.signal() : 0;
    }

    public void setInputSignal(int portNumber, int signal) {
        Block p = portNumbers.get(portNumber);
        if (p != null && p.portType() == PortBlock.PortType.INPUT) {
            portUpdates.put(p.uuid, signal);
        }
    }

    private void sendOutputSignal(Port port) {
        for (BlockPos pos : hubs) {
            if (world.getBlockEntity(pos) instanceof HubBlockEntity be) {
                Integer side = be.getSideOf(port.portNumber());
                if (side != null) {
                    BlockState state = world.getBlockState(pos);
                    Direction facing = be.getFacing(state);
                    Direction dir = Direction.fromSide(side);
                    Direction aligned = dir.toRelative(facing);
                    world.updateNeighbor(pos.offset(aligned.toMinecraft()), state.getBlock(), null);
                }
            }
        }
    }

    public void setCurrentServerWorld(ServerWorld serverWorld) {
        world = serverWorld;
    }

    public static String structureNameFrom(String circuitName) {
        return circuitName.replaceFirst("\\d+$", "");
    }

    public static int instanceIdFrom(String circuitName) {
        return Integer.parseInt(circuitName.replaceFirst(".+?(\\d+)$", "$1"));
    }
}
