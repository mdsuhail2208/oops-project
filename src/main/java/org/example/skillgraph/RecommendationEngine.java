package org.example.skillgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns "I want to become a ..." into a {@link Recommendation}: the skills the
 * user still needs, in the order they should be learnt.
 *
 * <p>The career paths themselves live in {@link CareerCatalog}, so this class
 * contains only the matching logic.
 */
public class RecommendationEngine {

    private final CareerCatalog catalog;

    /** Uses the career paths that ship with the app. */
    public RecommendationEngine() {
        this(new CareerCatalog());
    }

    /** Lets tests (or a future custom catalog) inject their own career paths. */
    public RecommendationEngine(CareerCatalog catalog) {
        this.catalog = catalog;
    }

    /** All career aims the engine knows about. */
    public List<String> getAims() {
        return catalog.getAimNames();
    }

    /** The full list of skills an aim requires (empty list for an unknown aim). */
    public List<Skill> getRequiredSkills(String aim) {
        CareerPath path = catalog.find(aim);
        return path == null ? List.of() : path.getRequiredSkills();
    }

    /**
     * Compares the user's graph with the aim and returns the skills that are
     * missing or not at the target level yet.
     */
    public Recommendation recommend(SkillGraph userGraph, String aim) {
        List<Skill> needed = new ArrayList<>();
        for (Skill required : getRequiredSkills(aim)) {
            Skill owned = userGraph.findSkill(required.getName());
            if (owned == null || owned.getLvl() < required.getLvl()) {
                needed.add(required); // missing completely, or owned but too weak
            }
        }
        return new Recommendation(aim, orderByGraph(userGraph, needed));
    }

    /**
     * Sorts the needed skills the way the graph says they should be learnt:
     * walk the graph's learning order and keep the skills we actually need.
     * Skills the user hasn't added to the graph yet are unknown to that order,
     * so they simply go at the end.
     */
    private List<Skill> orderByGraph(SkillGraph graph, List<Skill> needed) {
        List<Skill> ordered = new ArrayList<>();

        for (Skill skill : graph.learningOrder()) {
            for (Skill n : needed) {
                if (n.getName().equalsIgnoreCase(skill.getName())) {
                    ordered.add(n);
                }
            }
        }

        for (Skill n : needed) {
            if (!containsName(ordered, n.getName())) {
                ordered.add(n);
            }
        }
        return ordered;
    }

    private boolean containsName(List<Skill> skills, String name) {
        for (Skill s : skills) {
            if (s.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
