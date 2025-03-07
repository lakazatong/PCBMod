package in.lakazatong.pcbmod.redstone;


import com.github.kokorin.jaffree.ffmpeg.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import in.lakazatong.pcbmod.utils.SccGraph;
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
import java.util.stream.Stream;

public class Circuit {
    public final Structure structure;
    public final Map<UUID, Block> graph = new HashMap<>();
    private long time = 0; // in game ticks

    public Circuit(Structure structure) {
        this.structure = structure;
        structure.blocks().forEach(b -> graph.put(b.uuid, b));
        for (Block b : graph.values())
            b.props.neighbors.addAll(structure.getNeighbors(b));

        Path framesDir = structure.path.resolveSibling("frames");
        try (Stream<Path> paths = Files.walk(framesDir)) {
            paths.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException ignored) {
        }

        try {
            Files.createDirectory(framesDir);
        } catch (IOException ignored) {
        }
    }

    public void tick() {
        Map<UUID, Props> nextProps = new HashMap<>(graph.size());

        SccGraph sccGraph = new SccGraph(graph);

        Queue<Integer> queue = new LinkedList<>();
        for (int sccId = 0; sccId < sccGraph.sccGraph.size(); sccId++) {
            if (sccGraph.outputs(sccId).isEmpty())
                queue.add(sccId);
        }
        while (!queue.isEmpty()) {
            int sccId = queue.poll();
            for (Block block : sccGraph.sccMap.get(sccId))
                nextProps.put(block.uuid, block.tick(time));
            queue.addAll(sccGraph.inputs(sccId));
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
        time = 0;
        do {
            saveAsDot();
            tick();
            time++;
        }  while (hasChanged());
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
        Path framesDir = structure.path.resolveSibling("frames");
        Path outputPath = structure.path.resolveSibling(PathUtils.getBaseName(structure.path) + ".mp4").toAbsolutePath();

        FrameProducer producer = new FrameProducer() {
            private long index = 0;

            @Override
            public List<com.github.kokorin.jaffree.ffmpeg.Stream> produceStreams() {
                File firstFrameFile = framesDir.resolve(PathUtils.getBaseName(structure.path) + index + ".png").toAbsolutePath().toFile();
                BufferedImage firstImage;
                try {
                    firstImage = ImageIO.read(firstFrameFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                int width = firstImage.getWidth();
                int height = firstImage.getHeight();

                return Collections.singletonList(new com.github.kokorin.jaffree.ffmpeg.Stream()
                        .setType(com.github.kokorin.jaffree.ffmpeg.Stream.Type.VIDEO)
                        .setTimebase(1000L)
                        .setWidth(width)
                        .setHeight(height)
                );
            }

            @Override
            public Frame produce() {
                File frameFile = framesDir.resolve(PathUtils.getBaseName(structure.path) + index + ".png").toAbsolutePath().toFile();
                try {
                    return frameFile.exists() ? Frame.createVideoFrame(0, ((index++) * 1000), ImageIO.read(frameFile)) : null;
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
