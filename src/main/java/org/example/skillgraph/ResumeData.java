package org.example.skillgraph;

import java.util.List;

/**
 * What we managed to read out of a resume: who it belongs to, which skills were
 * recognised, and the lines of the projects / education / experience sections.
 *
 * <p>Like {@link Recommendation}, this is a read-only result object: it copies
 * the lists it is given, so nobody outside can change it afterwards.
 */
public class ResumeData {

    private final String name;
    private final List<Skill> skills;
    private final List<String> prjs;
    private final List<String> edu;
    private final List<String> exp;

    public ResumeData(String name, List<Skill> skills, List<String> prjs,
                      List<String> edu, List<String> exp) {
        this.name = name;
        this.skills = List.copyOf(skills);
        this.prjs = List.copyOf(prjs);
        this.edu = List.copyOf(edu);
        this.exp = List.copyOf(exp);
    }

    public String getName() {
        return name;
    }

    /** The skills that were found in the resume. */
    public List<Skill> getSkills() {
        return skills;
    }

    public List<String> getPrjs() {
        return prjs;
    }

    public List<String> getEdu() {
        return edu;
    }

    public List<String> getExp() {
        return exp;
    }

    /** Handy for summaries such as "found 7 skills". */
    public int skillCount() {
        return skills.size();
    }
}
