package org.example.skillgraph;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillGraphTest {

    private final Skill oop = new Skill("OOP", "Programming", 3);
    private final Skill java = new Skill("Java", "Programming", 4);
    private final Skill spring = new Skill("Spring", "Framework", 1);

    @Test
    void addSkillRejectsDuplicateNames() {
        SkillGraph graph = new SkillGraph();

        assertTrue(graph.addSkill(java));
        assertFalse(graph.addSkill(new Skill("java", "Other", 1)),
                "names should be compared while ignoring case");
        assertEquals(1, graph.size());
    }

    @Test
    void findSkillIsCaseInsensitive() {
        SkillGraph graph = new SkillGraph();
        graph.addSkill(java);

        assertNotNull(graph.findSkill("JAVA"));
        assertNull(graph.findSkill("Python"));
        assertTrue(graph.hasSkill("Java"));
    }

    @Test
    void removingSkillAlsoRemovesItsLinks() {
        SkillGraph graph = new SkillGraph();
        graph.addSkill(oop);
        graph.addSkill(java);
        graph.addSkillDependency(new SkillDependency(oop, java));

        graph.removeSkill(java);

        assertEquals(1, graph.size());
        assertTrue(graph.getDependencies().isEmpty(), "the link oop -> java should be gone too");
    }

    @Test
    void theSameLinkCannotBeAddedTwice() {
        SkillGraph graph = new SkillGraph();
        graph.addSkill(oop);
        graph.addSkill(java);

        assertTrue(graph.addSkillDependency(new SkillDependency(oop, java)));
        assertFalse(graph.addSkillDependency(new SkillDependency(oop, java)));
        assertEquals(1, graph.getDependencies().size());
    }

    @Test
    void linkThatWouldCreateALoopIsRejected() {
        SkillGraph graph = new SkillGraph();
        graph.addSkill(oop);
        graph.addSkill(java);
        graph.addSkill(spring);
        graph.addSkillDependency(new SkillDependency(oop, java));
        graph.addSkillDependency(new SkillDependency(java, spring));

        // spring -> oop would close the loop spring -> oop -> java -> spring
        boolean added = graph.addSkillDependency(new SkillDependency(spring, oop));

        assertFalse(added);
        assertFalse(graph.hasDependency(spring, oop));
        assertFalse(graph.hasCycle());
    }

    @Test
    void prerequisitesAndUnlocksDescribeBothDirections() {
        SkillGraph graph = new SkillGraph();
        graph.addSkill(oop);
        graph.addSkill(java);
        graph.addSkillDependency(new SkillDependency(oop, java));

        assertEquals(List.of("Java"), names(graph.unlocks(oop)));
        assertEquals(List.of("OOP"), names(graph.prerequisitesOf(java)));
    }

    @Test
    void learningOrderPutsPrerequisitesFirst() {
        SkillGraph graph = new SkillGraph();
        graph.addSkill(spring);   // deliberately added before its prerequisites
        graph.addSkill(java);
        graph.addSkill(oop);
        graph.addSkillDependency(new SkillDependency(oop, java));
        graph.addSkillDependency(new SkillDependency(java, spring));

        assertEquals(List.of("OOP", "Java", "Spring"), names(graph.learningOrder()));
    }

    @Test
    void noCycleInAnEmptyOrLinklessGraph() {
        assertFalse(new SkillGraph().hasCycle());

        SkillGraph graph = new SkillGraph();
        graph.addSkill(java);
        graph.addSkill(spring);
        assertFalse(graph.hasCycle());
    }

    private static List<String> names(List<Skill> skills) {
        List<String> names = new ArrayList<>();
        for (Skill skill : skills) {
            names.add(skill.getName());
        }
        return names;
    }
}
