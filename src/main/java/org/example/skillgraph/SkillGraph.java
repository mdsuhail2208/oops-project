package org.example.skillgraph;

import java.util.ArrayList;
import java.util.List;

public class SkillGraph {
    private final List<Skill> skills = new ArrayList<>();
    private final List<SkillDependency> dependencies =  new ArrayList<>();

    public void addSkill(Skill a){
        skills.add(a);
    }
    public void addSkillDependency(SkillDependency dependency){
        dependencies.add(dependency);
    }
    public List<Skill> getSkills(){
        return skills;
    }
    public List<SkillDependency> getDependencies(){
        return dependencies;
    }
    public List<Skill> dependsOn(Skill skill){
        List<Skill> depends =  new ArrayList<>();
        for (SkillDependency dependency : dependencies){
            if (dependency.getPrereq().equals(skill)){
                depends.add(dependency.getReq());
            }
        }
        return depends;
    }
}
