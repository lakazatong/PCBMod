package in.lakazatong.pcbmod.redstone;

import com.github.kokorin.jaffree.ffmpeg.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.apache.commons.io.file.PathUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Circuit {
    public final Structure structure;
    public final Map<UUID, Block> graph = new HashMap<>();
    private double time = 0; // in game ticks

    public Circuit(Structure structure) {
        this.structure = structure;
        structure.blocks().forEach(b -> graph.put(b.uuid, b));
        for (Block b : graph.values())
            b.props.neighbors.addAll(structure.getNeighbors(b));
    }

    public void tick() {
        Map<UUID, Props> nextProps = new HashMap<>(graph.size());

        Queue<Block> queue = new LinkedList<>();

        queue.add(structure.getFirstBlock());

        while (!queue.isEmpty()) {
            Block block = queue.poll();

            if (nextProps.containsKey(block.uuid))
                continue;

            nextProps.put(block.uuid, block.tick(time));
            block.outputs().forEach(queue::add);
        }

        for (Map.Entry<UUID, Props> entry : nextProps.entrySet())
            graph.get(entry.getKey()).props = entry.getValue();
    }

    public boolean hasChanged() {
        for (Block block : graph.values()) {
            if (block.hasChanged())
                return true;
        }
        return false;
    }

    public void simulateUntilUnchanged() {
        try {
            Files.createDirectories(structure.path.resolveSibling("frames"));
        } catch (IOException ignored) {
            return;
        }
        time = 0;
        saveAsDot(0);
        tick();
        while (hasChanged()) {
            saveAsDot(++time);
            tick();
        }
        animate();
    }

    public String toDot() {
        StringBuilder dotBuilder = new StringBuilder();
        dotBuilder.append("digraph G {\n");

        Set<String> edges = new HashSet<>();

        dotBuilder.append("    graph [bgcolor=\"black\"];\n");

        for (Block block : graph.values()) {
            int signal = block.props.signal;
            int red = (int) ((signal / 15.0) * 255);
            String color = String.format("#%02x0000", red);

            dotBuilder.append("    \"").append(block.uuid).append("\" ")
                    .append("[label=\"").append(block.type.name().toLowerCase())
                    .append("\n").append(signal).append("\", style=filled, fillcolor=\"")
                    .append(color).append("\", fontcolor=\"white\", color=\"white\"];\n");

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
        Path framesDir = structure.path.resolveSibling("frames");
        Path outputPath = structure.path.resolveSibling(PathUtils.getBaseName(structure.path) + ".mp4").toAbsolutePath();

        FrameProducer producer = new FrameProducer() {
            private long index = 0;

            @Override
            public List<Stream> produceStreams() {
                File firstFrameFile = framesDir.resolve(PathUtils.getBaseName(structure.path) + index + ".png").toAbsolutePath().toFile();
                BufferedImage firstImage;
                try {
                    firstImage = ImageIO.read(firstFrameFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                int width = firstImage.getWidth();
                int height = firstImage.getHeight();

                return Collections.singletonList(new Stream()
                        .setType(Stream.Type.VIDEO)
                        .setTimebase(1000L)
                        .setWidth(width)
                        .setHeight(height)
                );
            }

            @Override
            public Frame produce() {
                File frameFile = framesDir.resolve(PathUtils.getBaseName(structure.path) + index + ".png").toAbsolutePath().toFile();
                try {
                    return frameFile.exists() ? Frame.createVideoFrame(0, (index++) * 1000, ImageIO.read(frameFile)) : null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        FFmpeg.atPath()
                .addInput(FrameInput.withProducer(producer))
                .addOutput(UrlOutput.toUrl(outputPath.toString()))
                .execute();
    }

    public void saveAsDot(double index) {
        String dotPath = structure.path.resolveSibling("frames/" + PathUtils.getBaseName(structure.path) + index + ".dot").toAbsolutePath().toString();
        String pngPath = structure.path.resolveSibling("frames/" + PathUtils.getBaseName(structure.path) + index + ".png").toAbsolutePath().toString();

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
