package org.example.skillgraph;

public class Skill {
    private String name;
    private String category;
    private int lvl; // lvl = 0 - 5;

    public Skill (String name, String category, int lvl){
        this.name = name;
        this.category = category;
        this.lvl = lvl;
    }
    public String getName(){
        return name;
    }
    public String getCategory(){
        return category;
    }
    public int getLvl(){
        return lvl;
    }
    public void increaseLvl(){
        if (lvl < 5) lvl++;
        return;
    }

    @Override
    public String toString() {
        return name + " - " + category + " - " + lvl;
    }
}
