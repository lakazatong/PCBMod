package in.lakazatong.pcbmod.redstone;


import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import in.lakazatong.pcbmod.redstone.blocks.Button;
import in.lakazatong.pcbmod.redstone.blocks.Delayed;
import in.lakazatong.pcbmod.redstone.blocks.Solid;
import in.lakazatong.pcbmod.utils.SccGraph;
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
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Circuit {
    public final Structure structure;
//    public final Map<UUID, Block> graph = new HashMap<>();
    public final Map<String, Block> graph = new HashMap<>();
    private long time = 0; // in game ticks

    public Circuit(Structure structure) {
        this.structure = structure;

//        structure.blocks().forEach(b -> graph.put(b.uuid, b));
        structure.blocks().forEach(b -> graph.put(b.uuidDebug, b));
        for (Block b : graph.values())
            b.props.neighbors.addAll(structure.getNeighbors(b));

        graph.values().stream().filter(b -> b instanceof Solid).forEach(Block::init);
        graph.values().stream().filter(b -> !(b instanceof Solid)).forEach(Block::init);

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

//    private void update() {
//        for (Block b : graph.values()) {
//            var x = 0;
//            b.props = b.nextProps.dup();
//        }
//        for (Block b : graph.values()) {
//            if (b instanceof Delayed delayed) {
//                delayed.dirty = true;
//                var x = 0;
//            } else if (b instanceof Button button) {
//                button.dirty = true;
//            }
//        }
//        graph.values().stream()
//            .filter(Delayed.class::isInstance)
//            .map(Delayed.class::cast)
//            .forEach(delayed -> {
//            });
//    }

    private boolean update() {
        boolean changing = false;
        for (Block b : graph.values()) {
            if (!b.props.equals(b.nextProps) || b instanceof Delayed delayed && delayed.stableTime() > 0)
                changing = true;
            b.props = b.nextProps.dup();
        }
        for (Block b : graph.values()) {
            if (b instanceof Delayed delayed) {
                delayed.dirty = true;
            } else if (b instanceof Button button) {
                button.dirty = true;
            }
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

            var x = 0;

            do {
                Set<Block> dirtyBlocks = blocks.stream().filter(b -> b.dirty).collect(Collectors.toSet());
                if (dirtyBlocks.isEmpty()) break;
                dirtyBlocks.forEach(b -> b.tick(time));
            } while (true);

            queue.addAll(sccGraph.outputs(sccId));
        }
    }

    public void simulateUntilUnchanged() {
        time = 0;
        do {
            boolean changing = update();
            saveAsDot();
            if ((time > 0 && !changing) || time >= 200)
                break;
            tick();
            time++;
        } while (true);
        animate();
    }

    public String toDot() {
        StringBuilder dotBuilder = new StringBuilder();
        dotBuilder.append("digraph G {\n");

        Set<String> edges = new HashSet<>();

        dotBuilder.append("    graph [bgcolor=\"black\"];\n");

        for (Block block : graph.values()) {
            int signal = block.props.signal;
            int greenBlue = 255 - (int) ((signal / 15.0) * 255);
//            dotBuilder.append("    \"").append(block.uuid).append("\" ")
//                    .append("[label=\"").append(block.type.name().toLowerCase())
//                    .append("\n").append(signal).append("\", style=filled, fillcolor=\"black\", fontcolor=\"white\", color=\"")
//                    .append(String.format("#ff%02x%02x", greenBlue, greenBlue))
//                    .append("\", penwidth=2];\n");
            dotBuilder.append("    \"").append(block.uuidDebug).append("\" ")
                    .append("[label=\"").append(block.type.name().toLowerCase())
                    .append("\n").append(signal).append("\", style=filled, fillcolor=\"black\", fontcolor=\"white\", color=\"")
                    .append(String.format("#ff%02x%02x", greenBlue, greenBlue))
                    .append("\", penwidth=2];\n");

//            block.inputs().forEach(input -> {
//                String edge = "\"" + input.uuid + "\" -> \"" + block.uuid + "\"";
//                if (edges.add(edge)) {
//                    dotBuilder.append("    ").append(edge).append(" [color=\"white\", penwidth=2];\n");
//                }
//            });
            block.inputs().forEach(input -> {
                String edge = "\"" + input.uuidDebug + "\" -> \"" + block.uuidDebug + "\"";
                if (edges.add(edge)) {
                    dotBuilder.append("    ").append(edge).append(" [color=\"white\", penwidth=2];\n");
                }
            });
        }

        return dotBuilder.append("}\n").toString();
    }

    public void animate() {
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

        FrameProducer producer = getFrameProducer(maxWidth, maxHeight, framesDir);

        FFmpeg.atPath()
            .addInput(FrameInput.withProducer(producer).setFrameRate(1))
                .addOutput(UrlOutput.toUrl(outputPath.toString()).setDuration(2000))
            .execute();
    }

    private FrameProducer getFrameProducer(int maxWidth, int maxHeight, Path framesDir) {
        return new FrameProducer() {
            private long index = 0;

            @Override
            public List<com.github.kokorin.jaffree.ffmpeg.Stream> produceStreams() {
                return Collections.singletonList(new com.github.kokorin.jaffree.ffmpeg.Stream()
                        .setType(com.github.kokorin.jaffree.ffmpeg.Stream.Type.VIDEO)
                        .setTimebase(1000L)
                        .setWidth(maxWidth)
                        .setHeight(maxHeight)
                );
            }

            @Override
            public Frame produce() {
                if (index >= time + 1)
                    return null;
                try {
                    File frameFile = framesDir.resolve(PathUtils.getBaseName(structure.path) + index + ".png").toAbsolutePath().toFile();
                    BufferedImage original = ImageIO.read(frameFile);
                    BufferedImage formatted = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D g = formatted.createGraphics();
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, maxWidth, maxHeight);
                    if (original != null) {
                        int x = (maxWidth - original.getWidth()) / 2;
                        int y = (maxHeight - original.getHeight()) / 2;
                        g.drawImage(original, x, y, null);
                    }
                    g.dispose();
                    return Frame.createVideoFrame(0, (index++ * 1000), formatted);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public void saveAsDot() {
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
}
