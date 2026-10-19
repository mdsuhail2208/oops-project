package org.example.skillgraph;

import java.util.List;

/**
 * One career goal and the skills it needs - for example "Backend Developer"
 * needs Java at level 4, SQL at level 3, and so on.
 *
 * <p>This class only holds data. The logic that compares a CareerPath with the
 * user's skills lives in {@link RecommendationEngine}.
 */
public class CareerPath {

    private final String name;
    private final List<Skill> requiredSkills;

    public CareerPath(String name, List<Skill> requiredSkills) {
        this.name = name;
        this.requiredSkills = List.copyOf(requiredSkills);
    }

    public String getName() {
        return name;
    }

    /** The skills this path needs, each one carrying the level it should reach. */
    public List<Skill> getRequiredSkills() {
        return requiredSkills;
    }

    public int size() {
        return requiredSkills.size();
    }
}
