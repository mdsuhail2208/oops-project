package org.example.skillgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * The answer to "how do I get to this aim?": the skills that are still needed.
 *
 * <p>A Recommendation is a small, read-only result object. It is created by the
 * {@link RecommendationEngine} and only read by the UI.
 */
public class Recommendation {

    private final String aim;
    private final List<Skill> skillsNeeded;

    public Recommendation(String aim, List<Skill> skillsNeeded) {
        this.aim = aim;
        // Copy the list so nobody outside can change this object later.
        this.skillsNeeded = new ArrayList<>(skillsNeeded);
    }

    public String getAim() {
        return aim;
    }

    /** The skills to learn (or improve), in a sensible learning order. */
    public List<Skill> getSkillsNeeded() {
        return List.copyOf(skillsNeeded);
    }

    public int size() {
        return skillsNeeded.size();
    }

    /** True when the user already meets every requirement of the aim. */
    public boolean isEmpty() {
        return skillsNeeded.isEmpty();
    }
}
