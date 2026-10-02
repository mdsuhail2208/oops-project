package org.example.skillgraph;

public class SkillDependency {
    private Skill prereq;
    private Skill req;

    public SkillDependency(Skill prereq, Skill req){
        this.prereq = prereq;
        this.req = req;
    }
    public Skill getReq(){
        return req;
    }
    public Skill getPrereq(){
        return prereq;
    }

    @Override
    public String toString() {
        return prereq.getName() + "->" + req.getName();
    }
}
