package org.example.skillgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * The career paths the app knows about.
 *
 * <p>Keeping the data here (instead of inside the engine) means the engine only
 * contains matching logic, and tests can hand it their own catalog.
 */
public class CareerCatalog {

    private final List<CareerPath> paths;

    /** Builds the catalog with the career paths that ship with the app. */
    public CareerCatalog() {
        this(List.of(
                new CareerPath("Backend Developer", List.of(
                        new Skill("Java", "Programming", 4),
                        new Skill("OOP", "Programming", 3),
                        new Skill("Spring", "Framework", 3),
                        new Skill("SQL", "Database", 3),
                        new Skill("Git", "Tools", 2))),

                new CareerPath("Frontend Developer", List.of(
                        new Skill("HTML", "Web", 3),
                        new Skill("CSS", "Web", 3),
                        new Skill("JavaScript", "Programming", 4),
                        new Skill("React", "Framework", 3),
                        new Skill("Git", "Tools", 2))),

                new CareerPath("Data Analyst", List.of(
                        new Skill("SQL", "Database", 4),
                        new Skill("Excel", "Tools", 3),
                        new Skill("Python", "Programming", 3),
                        new Skill("Statistics", "Math", 3)))));
    }

    /** Lets tests - or a future "custom career paths" feature - supply their own paths. */
    public CareerCatalog(List<CareerPath> paths) {
        this.paths = List.copyOf(paths);
    }

    public List<CareerPath> getPaths() {
        return paths;
    }

    /** The aims, in the order they should be shown to the user. */
    public List<String> getAimNames() {
        List<String> names = new ArrayList<>();
        for (CareerPath path : paths) {
            names.add(path.getName());
        }
        return names;
    }

    /** Finds a path by name (case-insensitive), or null if the aim is unknown. */
    public CareerPath find(String aim) {
        for (CareerPath path : paths) {
            if (path.getName().equalsIgnoreCase(aim)) {
                return path;
            }
        }
        return null;
    }
}
