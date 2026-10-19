package org.example.skillgraph;

import java.util.List;

/**
 * The skills the app can recognise when it reads a resume.
 *
 * <p>This is just a list of known skills (name + category). The category is
 * used to fill in a detected skill, so a resume never has to say what kind of
 * skill "Docker" is. The level in these entries is not used - a detected skill
 * gets {@link ResumeParser#DEFAULT_LEVEL} instead.
 *
 * <p>In a real product this list would come from a database. Keeping it as a
 * plain list here makes it easy to see and to extend.
 */
public class SkillVocabulary {

    private final List<Skill> skills;

    /** The vocabulary that ships with the app. */
    public SkillVocabulary() {
        this(List.of(
                // Programming languages
                new Skill("Java", "Programming", 0),
                new Skill("Python", "Programming", 0),
                new Skill("C++", "Programming", 0),
                new Skill("C#", "Programming", 0),
                new Skill("JavaScript", "Programming", 0),
                new Skill("TypeScript", "Programming", 0),
                new Skill("Kotlin", "Programming", 0),
                new Skill("OOP", "Programming", 0),

                // Web
                new Skill("HTML", "Web", 0),
                new Skill("CSS", "Web", 0),
                new Skill("React", "Web", 0),
                new Skill("Angular", "Web", 0),
                new Skill("Node.js", "Web", 0),

                // Frameworks
                new Skill("Spring", "Framework", 0),
                new Skill("Django", "Framework", 0),
                new Skill("Flutter", "Framework", 0),

                // Databases
                new Skill("SQL", "Database", 0),
                new Skill("MySQL", "Database", 0),
                new Skill("PostgreSQL", "Database", 0),
                new Skill("MongoDB", "Database", 0),
                new Skill("Redis", "Database", 0),

                // Tools and systems
                new Skill("Git", "Tools", 0),
                new Skill("Docker", "Tools", 0),
                new Skill("Kubernetes", "Tools", 0),
                new Skill("Linux", "Tools", 0),
                new Skill("JUnit", "Tools", 0),
                new Skill("Excel", "Tools", 0),

                // Data
                new Skill("Statistics", "Math", 0),
                new Skill("Pandas", "Data", 0),
                new Skill("NumPy", "Data", 0),
                new Skill("Machine Learning", "Data", 0)));
    }

    /** Lets tests (or a future "custom vocabulary" feature) supply their own skills. */
    public SkillVocabulary(List<Skill> skills) {
        this.skills = List.copyOf(skills);
    }

    public List<Skill> all() {
        return skills;
    }

    public int size() {
        return skills.size();
    }

    public boolean contains(String name) {
        return find(name) != null;
    }

    /** Finds a known skill by name (case-insensitive), or null if we don't know it. */
    public Skill find(String name) {
        for (Skill skill : skills) {
            if (skill.getName().equalsIgnoreCase(name)) {
                return skill;
            }
        }
        return null;
    }
}
