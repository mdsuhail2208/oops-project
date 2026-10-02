package org.example.skillgraph;

import java.util.ArrayList;
import java.util.List;

public class ResumeData {
    private String name;
    private List<Skill> skills = new ArrayList<Skill>();
    private List<String> prjs = new ArrayList<String>();
    private List<String> edu = new ArrayList<String>();
    private List<String> exp = new ArrayList<String>();

    public ResumeData(String name, List<Skill> skills, List<String> prjs, List<String> edu, List<String> exp){
        this.name = name;
        this.skills = skills;
        this.prjs = prjs;
        this.edu = edu;
        this.exp = exp;
    }

    public String getName() {
        return name;
    }

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

}
