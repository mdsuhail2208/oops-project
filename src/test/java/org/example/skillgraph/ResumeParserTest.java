package org.example.skillgraph;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeParserTest {

    private final ResumeParser parser = new ResumeParser();

    @Test
    void findsTheSkillsItKnows() {
        ResumeData data = parser.parse("Skills: Java, SQL, Docker, Python");

        List<String> names = names(data.getSkills());
        assertEquals(4, names.size());
        assertTrue(names.containsAll(List.of("Java", "SQL", "Docker", "Python")));
    }

    @Test
    void ignoresWordsThatAreNotInTheVocabulary() {
        ResumeData data = parser.parse("I am a fast learner and a good problem solver.");

        assertTrue(data.getSkills().isEmpty());
    }

    @Test
    void javaInsideJavaScriptIsNotCountedAsJava() {
        ResumeData data = parser.parse("Frontend developer using JavaScript and React.");

        List<String> names = names(data.getSkills());
        assertTrue(names.contains("JavaScript"));
        assertFalse(names.contains("Java"), "'JavaScript' must not be read as 'Java'");
    }

    @Test
    void skillsAreFoundWhateverTheCaseOrPunctuation() {
        ResumeData data = parser.parse("I use JAVA. Mostly SPRING, and sql.");

        List<String> names = names(data.getSkills());
        assertTrue(names.contains("Java"));
        assertTrue(names.contains("Spring"));
        assertTrue(names.contains("SQL"));
    }

    @Test
    void importedSkillsStartAtTheDefaultLevel() {
        ResumeData data = parser.parse("Java");

        assertEquals(1, data.skillCount());
        assertEquals(ResumeParser.DEFAULT_LEVEL, data.getSkills().get(0).getLvl());
    }

    @Test
    void readsTheNameFromTheFirstNonEmptyLine() {
        ResumeData data = parser.parse("\n\n   Md Suhail\nmd.suhail@example.com\n\nSKILLS\nJava");

        assertEquals("Md Suhail", data.getName());
    }

    @Test
    void nameIsUnknownWhenTheFirstLineLooksLikeAContact() {
        ResumeData data = parser.parse("someone@example.com\nSKILLS\nJava");

        assertEquals("Unknown", data.getName());
    }

    @Test
    void nameIsUnknownWhenTheResumeStartsWithASectionTitle() {
        ResumeData data = parser.parse("SKILLS\nJava, SQL");

        assertEquals("Unknown", data.getName());
    }

    @Test
    void collectsTheProjectsEducationAndExperienceSections() {
        String text = String.join("\n",
                "Md Suhail",
                "PROJECTS",
                "SkillGraph - a JavaFX app",
                "Campus Navigator - a Spring service",
                "EDUCATION:",
                "B.Tech Computer Science",
                "EXPERIENCE",
                "Teaching assistant for the OOP course");

        ResumeData data = parser.parse(text);

        assertEquals(2, data.getPrjs().size());
        assertEquals(1, data.getEdu().size());
        assertEquals(1, data.getExp().size());
        assertTrue(data.getPrjs().get(0).contains("SkillGraph"));
        assertTrue(data.getEdu().get(0).contains("B.Tech"));
        assertTrue(data.getExp().get(0).contains("OOP"));
    }

    @Test
    void aSectionEndsWhenTheNextSectionStarts() {
        String text = String.join("\n",
                "PROJECTS",
                "One project",
                "SKILLS",
                "Java, SQL",
                "EDUCATION",
                "A degree");

        ResumeData data = parser.parse(text);

        assertEquals(List.of("One project"), data.getPrjs(),
                "the skills section must not leak into projects");
        assertEquals(List.of("A degree"), data.getEdu());
    }

    @Test
    void parserOnlyFindsSkillsFromItsOwnVocabulary() {
        ResumeParser tiny = new ResumeParser(new SkillVocabulary(List.of(
                new Skill("Cobol", "Programming", 0))));

        ResumeData data = tiny.parse("Java, Cobol, Python");

        assertEquals(List.of("Cobol"), names(data.getSkills()));
    }

    @Test
    void emptyTextGivesEmptyResumeInsteadOfCrashing() {
        ResumeData data = parser.parse("");

        assertEquals("Unknown", data.getName());
        assertEquals(0, data.skillCount());
    }

    private static List<String> names(List<Skill> skills) {
        List<String> names = new ArrayList<>();
        for (Skill skill : skills) {
            names.add(skill.getName());
        }
        return names;
    }
}
