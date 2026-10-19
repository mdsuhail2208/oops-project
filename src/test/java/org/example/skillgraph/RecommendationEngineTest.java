package org.example.skillgraph;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationEngineTest {

    private Skill addSkill(SkillGraph graph, String name, String category, int lvl) {
        Skill skill = new Skill(name, category, lvl);
        graph.addSkill(skill);
        return skill;
    }

    @Test
    void listsSkillsThatAreMissingOrTooWeakButNotFinishedOnes() {
        SkillGraph user = new SkillGraph();
        addSkill(user, "Java", "Programming", 4);   // already at the target level
        addSkill(user, "Spring", "Framework", 1);   // owned, but too weak (target 3)
        addSkill(user, "SQL", "Database", 2);       // owned, but too weak (target 3)

        Recommendation rec = new RecommendationEngine().recommend(user, "Backend Developer");

        List<String> names = names(rec.getSkillsNeeded());
        assertTrue(names.contains("Spring"), "too weak, should be recommended");
        assertTrue(names.contains("SQL"), "too weak, should be recommended");
        assertTrue(names.contains("OOP"), "missing, should be recommended");
        assertFalse(names.contains("Java"), "already at the target level");
    }

    @Test
    void givesNothingWhenEveryRequirementIsMet() {
        SkillGraph user = new SkillGraph();
        addSkill(user, "Java", "Programming", 5);
        addSkill(user, "OOP", "Programming", 4);
        addSkill(user, "Spring", "Framework", 3);
        addSkill(user, "SQL", "Database", 5);
        addSkill(user, "Git", "Tools", 3);

        Recommendation rec = new RecommendationEngine().recommend(user, "Backend Developer");

        assertTrue(rec.isEmpty());
    }

    @Test
    void recommendationsFollowTheLearningOrder() {
        SkillGraph user = new SkillGraph();
        Skill java = addSkill(user, "Java", "Programming", 1);      // needs levelling up
        Skill spring = addSkill(user, "Spring", "Framework", 1);    // needs levelling up
        user.addSkillDependency(new SkillDependency(java, spring));

        Recommendation rec = new RecommendationEngine().recommend(user, "Backend Developer");

        List<String> names = names(rec.getSkillsNeeded());
        assertTrue(names.indexOf("Java") < names.indexOf("Spring"),
                "Java unlocks Spring, so Java should be suggested first");
    }

    @Test
    void unknownAimGivesAnEmptyRecommendation() {
        Recommendation rec = new RecommendationEngine()
                .recommend(new SkillGraph(), "Astronaut");

        assertTrue(rec.isEmpty());
        assertEquals("Astronaut", rec.getAim());
    }

    @Test
    void engineUsesWhateverCatalogItIsGiven() {
        CareerCatalog catalog = new CareerCatalog(List.of(
                new CareerPath("Automated Tester", List.of(new Skill("JUnit", "Testing", 2)))));

        RecommendationEngine engine = new RecommendationEngine(catalog);

        assertEquals(List.of("Automated Tester"), engine.getAims());
        assertFalse(engine.recommend(new SkillGraph(), "Automated Tester").isEmpty());
    }

    private static List<String> names(List<Skill> skills) {
        List<String> names = new ArrayList<>();
        for (Skill skill : skills) {
            names.add(skill.getName());
        }
        return names;
    }
}
