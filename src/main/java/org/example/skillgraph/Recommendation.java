package org.example.skillgraph;

import java.util.ArrayList;
import java.util.List;

public class Recommendation {
    private final String aim;
    private List<Skill> skillsNeeded = new ArrayList<Skill>();

    Recommendation(String aim ,List<Skill> skills_needed ){
        this.aim = aim;
        this.skillsNeeded = skills_needed;
    }

    public String getAim(){
        return aim;
    }
    public List<Skill> getSkillsNeeded(){
        return skillsNeeded;
    }
}
