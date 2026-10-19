package org.example.skillgraph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads a SkillGraph as a plain text file, so your skills are still
 * there the next time you open the app.
 *
 * <p>The format is one record per line with fields separated by '|':
 * <pre>
 * Skill|Java|Programming|4
 * Link|OOP|Java
 * </pre>
 * Plain text means you can open the file and read or edit it yourself.
 */
public class SkillGraphStore {

    private static final String SKILL = "Skill";
    private static final String LINK = "Link";
    private static final String SEP = "|";

    /** Where the app keeps your data: &lt;user home&gt;/.skillgraph/data.txt */
    public static Path defaultFile() {
        return Path.of(System.getProperty("user.home"), ".skillgraph", "data.txt");
    }

    /** Writes the whole graph to the file, creating the folder if it doesn't exist yet. */
    public void save(SkillGraph graph, Path file) throws IOException {
        List<String> lines = new ArrayList<>();

        for (Skill skill : graph.getSkills()) {
            lines.add(String.join(SEP, SKILL, skill.getName(), skill.getCategory(),
                    String.valueOf(skill.getLvl())));
        }
        for (SkillDependency dep : graph.getDependencies()) {
            lines.add(String.join(SEP, LINK, dep.getPrereq().getName(), dep.getReq().getName()));
        }

        Path folder = file.getParent();
        if (folder != null) {
            Files.createDirectories(folder);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    /**
     * Reads a graph from the file. A file that doesn't exist gives an empty graph.
     *
     * <p>Lines that don't make sense (hand-edited mistakes, half-written files)
     * are skipped instead of crashing the app.
     */
    public SkillGraph load(Path file) throws IOException {
        SkillGraph graph = new SkillGraph();
        if (!Files.exists(file)) {
            return graph;
        }

        // Links are remembered for later: a link needs both of its skills to
        // exist first, and the file may list a link before those skills.
        List<String[]> links = new ArrayList<>();

        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] parts = line.split("\\" + SEP, -1);
            try {
                if (SKILL.equals(parts[0])) {
                    graph.addSkill(new Skill(parts[1], parts[2], Integer.parseInt(parts[3])));
                } else if (LINK.equals(parts[0])) {
                    links.add(new String[]{parts[1], parts[2]});
                }
                // Any other line is ignored on purpose.
            } catch (RuntimeException ignored) {
                // Not enough fields, or a level that isn't a number: skip this line.
            }
        }

        for (String[] link : links) {
            Skill prereq = graph.findSkill(link[0]);
            Skill target = graph.findSkill(link[1]);
            if (prereq != null && target != null) {
                graph.addSkillDependency(new SkillDependency(prereq, target));
            }
        }
        return graph;
    }
}
