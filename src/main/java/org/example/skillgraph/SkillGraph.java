package org.example.skillgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The heart of the model: a set of skills plus the "prerequisite -> target" links
 * between them, for example "Java unlocks Spring".
 *
 * <p>Skills are kept in a map keyed by their lower-case name. That means:
 * looking up a skill by name is fast, names are compared case-insensitively,
 * and there can never be two skills with the same name in the graph.
 */
public class SkillGraph {

    private final Map<String, Skill> skills = new LinkedHashMap<>();
    private final List<SkillDependency> dependencies = new ArrayList<>();

    /* =====================================================================
     *  Skills
     * ===================================================================== */

    /** Adds a skill. Returns false if a skill with the same name is already there. */
    public boolean addSkill(Skill skill) {
        String key = keyOf(skill.getName());
        if (skills.containsKey(key)) {
            return false;
        }
        skills.put(key, skill);
        return true;
    }

    /** Finds a skill by name (case-insensitive), or null if it isn't in the graph. */
    public Skill findSkill(String name) {
        return name == null ? null : skills.get(keyOf(name));
    }

    public boolean hasSkill(String name) {
        return findSkill(name) != null;
    }

    /** Removes a skill, and any links that pointed to it or from it. */
    public boolean removeSkill(Skill skill) {
        if (skills.remove(keyOf(skill.getName())) == null) {
            return false;
        }
        dependencies.removeIf(d -> d.getPrereq().equals(skill) || d.getReq().equals(skill));
        return true;
    }

    /** All skills, in the order they were added. Returns a snapshot, not the internal map. */
    public List<Skill> getSkills() {
        return List.copyOf(skills.values());
    }

    public int size() {
        return skills.size();
    }

    /* =====================================================================
     *  Dependencies
     * ===================================================================== */

    /**
     * Adds a prerequisite link ("prereq unlocks target").
     *
     * <p>Returns false and changes nothing when the link is invalid:
     * both skills must already be in the graph, a skill can't unlock itself,
     * the same link can't be added twice, and links must not form a loop.
     */
    public boolean addSkillDependency(SkillDependency dependency) {
        Skill prereq = dependency.getPrereq();
        Skill target = dependency.getReq();

        if (!hasSkill(prereq.getName()) || !hasSkill(target.getName())) return false;
        if (prereq.equals(target)) return false;
        if (hasDependency(prereq, target)) return false;

        dependencies.add(dependency);
        if (hasCycle()) {
            dependencies.remove(dependency); // that link created a loop, so undo it
            return false;
        }
        return true;
    }

    public boolean removeDependency(SkillDependency dependency) {
        return dependencies.remove(dependency);
    }

    /** Is there already a link prereq -> target? */
    public boolean hasDependency(Skill prereq, Skill target) {
        for (SkillDependency d : dependencies) {
            if (d.getPrereq().equals(prereq) && d.getReq().equals(target)) {
                return true;
            }
        }
        return false;
    }

    /** The skills this one enables - what you can learn after it. */
    public List<Skill> unlocks(Skill skill) {
        List<Skill> result = new ArrayList<>();
        for (SkillDependency d : dependencies) {
            if (d.getPrereq().equals(skill)) {
                result.add(d.getReq());
            }
        }
        return result;
    }

    /** The skills you should already have before learning this one. */
    public List<Skill> prerequisitesOf(Skill skill) {
        List<Skill> result = new ArrayList<>();
        for (SkillDependency d : dependencies) {
            if (d.getReq().equals(skill)) {
                result.add(d.getPrereq());
            }
        }
        return result;
    }

    /** All links. Returns a snapshot, not the internal list. */
    public List<SkillDependency> getDependencies() {
        return List.copyOf(dependencies);
    }

    /* =====================================================================
     *  Graph algorithms
     * ===================================================================== */

    /** True if the links contain a loop such as A -> B -> A. */
    public boolean hasCycle() {
        Set<String> checked = new HashSet<>();
        for (Skill skill : skills.values()) {
            if (hasCycleFrom(skill, new HashSet<>(), checked)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Walks the links starting at one skill and looks for a loop.
     *
     * @param onPath  skills on the path we're currently walking
     * @param checked skills we've fully checked before (no loop behind them)
     */
    private boolean hasCycleFrom(Skill skill, Set<String> onPath, Set<String> checked) {
        String key = keyOf(skill.getName());
        if (checked.contains(key)) return false;
        if (!onPath.add(key)) return true; // we arrived here earlier on this same path -> loop

        for (Skill next : unlocks(skill)) {
            if (hasCycleFrom(next, onPath, checked)) {
                return true;
            }
        }

        onPath.remove(key);
        checked.add(key);
        return false;
    }

    /**
     * Every skill in the order it should be learnt: each skill appears after all
     * of its prerequisites. If A unlocks B and B unlocks C, the result is A, B, C.
     *
     * <p>This is a depth-first search that adds a skill only after it has added
     * all of that skill's prerequisites ("post-order" topological sort).
     */
    public List<Skill> learningOrder() {
        List<Skill> order = new ArrayList<>();
        Set<String> added = new HashSet<>();
        for (Skill skill : skills.values()) {
            addAfterPrerequisites(skill, new HashSet<>(), added, order);
        }
        return order;
    }

    private void addAfterPrerequisites(Skill skill, Set<String> onPath, Set<String> added, List<Skill> order) {
        String key = keyOf(skill.getName());
        if (added.contains(key) || !onPath.add(key)) {
            return; // already placed, or we hit a loop (guarded against just in case)
        }

        for (Skill prereq : prerequisitesOf(skill)) {
            addAfterPrerequisites(prereq, onPath, added, order);
        }

        onPath.remove(key);
        added.add(key);
        order.add(skill); // all prerequisites are in the list by now, so it's safe to add this skill
    }

    /* =====================================================================
     *  Helpers
     * ===================================================================== */

    /** The map key for a skill name: lower-case so lookups ignore case. */
    private static String keyOf(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
