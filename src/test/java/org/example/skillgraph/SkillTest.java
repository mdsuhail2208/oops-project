package org.example.skillgraph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillTest {

    @Test
    void levelsAreClampedToTheAllowedRange() {
        Skill tooHigh = new Skill("Java", "Programming", 99);
        assertEquals(Skill.MAX_LEVEL, tooHigh.getLvl());

        Skill tooLow = new Skill("Java", "Programming", -3);
        assertEquals(Skill.MIN_LEVEL, tooLow.getLvl());
    }

    @Test
    void setLvlCannotLeaveTheAllowedRange() {
        Skill skill = new Skill("Java", "Programming", 2);

        skill.setLvl(10);
        assertEquals(Skill.MAX_LEVEL, skill.getLvl());

        skill.setLvl(4);
        assertEquals(4, skill.getLvl());
    }

    @Test
    void increaseLvlStopsAtTheMaximum() {
        Skill skill = new Skill("Java", "Programming", Skill.MAX_LEVEL);

        skill.increaseLvl();

        assertEquals(Skill.MAX_LEVEL, skill.getLvl());
    }

    @Test
    void nameAndCategoryAreTrimmedAndMustNotBeEmpty() {
        Skill skill = new Skill("  Java  ", "  Programming  ", 3);
        assertEquals("Java", skill.getName());
        assertEquals("Programming", skill.getCategory());

        assertThrows(IllegalArgumentException.class, () -> new Skill("", "Programming", 1));
        assertThrows(IllegalArgumentException.class, () -> new Skill("Java", "   ", 1));
    }

    @Test
    void skillsWithTheSameNameAreEqual() {
        Skill a = new Skill("Java", "Programming", 1);
        Skill b = new Skill("java", "Language", 5);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
