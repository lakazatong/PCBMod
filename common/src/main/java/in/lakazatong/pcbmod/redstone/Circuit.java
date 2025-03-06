package in.lakazatong.pcbmod.redstone;

import org.apache.commons.io.file.PathUtils;

import java.io.*;
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
        time = 0;
        tick();
        while (hasChanged()) {
            time++;
            tick();
        }
    }

    public void saveAsDot() throws IOException, InterruptedException {
        StringBuilder dotBuilder = new StringBuilder();
        dotBuilder.append("digraph G {\n");

        Set<String> edges = new HashSet<>();

        dotBuilder.append("    graph [bgcolor=\"black\"];\n");

        for (Map.Entry<UUID, Block> entry : graph.entrySet()) {
            Block block = entry.getValue();

            String blockName = block.type.name().toLowerCase();

            dotBuilder
                .append("    \"")
                .append(block.uuid)
                .append("\" [label=\"")
                .append(blockName)
                .append("\", style=filled, fillcolor=\"black\", fontcolor=\"white\", color=\"white\", width=0.2, height=0.2];\n");

            block.inputs().forEach(input -> {
                String edge = "\"" + input.uuid + "\" -> \"" + block.uuid + "\"";
                if (!edges.contains(edge)) {
                    dotBuilder
                        .append("    ")
                        .append(edge)
                        .append(" [color=\"white\", arrowhead=\"normal\", fontcolor=\"white\", penwidth=2];\n");
                    edges.add(edge);
                }
            });
        }

        dotBuilder.append("}\n");

        String dotPath = structure.path.getParent().resolve(PathUtils.getBaseName(structure.path) + ".dot").toAbsolutePath().toString();
        String pngPath = structure.path.getParent().resolve(PathUtils.getBaseName(structure.path) + ".png").toAbsolutePath().toString();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Path.of(dotPath).toFile()))) {
            writer.write(dotBuilder.toString());
        }

        dotPath = "/mnt/" + dotPath.split(":")[0].toLowerCase() + dotPath.substring(dotPath.indexOf(":") + 1);
        pngPath = "/mnt/" + pngPath.split(":")[0].toLowerCase() + pngPath.substring(pngPath.indexOf(":") + 1);

        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c",
                "dot -Tpng " +
                dotPath.replace("\\", "/") +
                " -o " +
                pngPath.replace("\\", "/"));

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            System.out.println("PNG file generated successfully.");
        } else {
            System.out.println("Error generating PNG.");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println(line);
                }
            }
        }
    }
}
