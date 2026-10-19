package org.example.skillgraph;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillGraphStoreTest {

    /** JUnit gives us a fresh empty folder for every test and cleans it up afterwards. */
    @TempDir
    Path tempDir;

    private final SkillGraphStore store = new SkillGraphStore();

    @Test
    void savedGraphCanBeLoadedBack() throws IOException {
        SkillGraph original = new SkillGraph();
        Skill oop = new Skill("OOP", "Programming", 3);
        Skill java = new Skill("Java", "Programming", 4);
        original.addSkill(oop);
        original.addSkill(java);
        original.addSkillDependency(new SkillDependency(oop, java));

        Path file = tempDir.resolve("data.txt");
        store.save(original, file);
        SkillGraph loaded = store.load(file);

        assertEquals(2, loaded.size());
        assertEquals(1, loaded.getDependencies().size());
        assertEquals(4, loaded.findSkill("java").getLvl(), "lookup should ignore case");
        assertTrue(loaded.hasDependency(loaded.findSkill("OOP"), loaded.findSkill("Java")));
    }

    @Test
    void loadingAMissingFileGivesAnEmptyGraph() throws IOException {
        SkillGraph graph = store.load(tempDir.resolve("nothing-here.txt"));

        assertEquals(0, graph.size());
        assertTrue(graph.getDependencies().isEmpty());
    }

    @Test
    void brokenLinesAreSkippedInsteadOfCrashing() throws IOException {
        Path file = tempDir.resolve("broken.txt");
        Files.writeString(file, String.join("\n",
                "Skill|Java|Programming|4",
                "rubbish line with no separator",
                "Skill|Broken",                  // not enough fields
                "Skill|Weird|Category|notANumber",
                "Link|Java|DoesNotExist"),
                StandardCharsets.UTF_8);

        SkillGraph graph = store.load(file);

        assertEquals(1, graph.size());
        assertTrue(graph.hasSkill("Java"));
        assertTrue(graph.getDependencies().isEmpty(), "a link to a missing skill is dropped");
    }
}
