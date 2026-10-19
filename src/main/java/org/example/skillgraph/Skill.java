package org.example.skillgraph;

import java.util.Locale;

/**
 * One skill a person can have, for example ("Java", "Programming", 4).
 * The level is always between MIN_LEVEL (0 = not learnt yet) and MAX_LEVEL (5 = expert).
 */
public class Skill {

    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 5;

    private final String name;
    private final String category;
    private int lvl; // lvl = MIN_LEVEL .. MAX_LEVEL

    public Skill(String name, String category, int lvl) {
        this.name = requireText(name, "Skill name");
        this.category = requireText(category, "Skill category");
        this.lvl = clampLevel(lvl);
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getLvl() {
        return lvl;
    }

    /** Changes the level, but keeps it inside the allowed 0..5 range. */
    public void setLvl(int lvl) {
        this.lvl = clampLevel(lvl);
    }

    public void increaseLvl() {
        setLvl(lvl + 1);
    }

    /** A level-0 skill is known about, but not learnt yet. */
    public boolean isLearnt() {
        return lvl > MIN_LEVEL;
    }

    // Two skills are considered the same skill if they have the same name
    // (case-insensitive), no matter which object instance they are.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skill other)) return false;
        return name.equalsIgnoreCase(other.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase(Locale.ROOT).hashCode();
    }

    @Override
    public String toString() {
        return name + " - " + category + " - " + lvl;
    }

    /* ---------- helpers ---------- */

    /** Stops a level like 99 or -3 from ever getting into the object. */
    private static int clampLevel(int lvl) {
        return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, lvl));
    }

    /** Rejects null/blank text so a skill can never be called "". */
    private static String requireText(String value, String what) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(what + " can't be empty");
        }
        return value.trim();
    }
}
