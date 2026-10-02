package org.example.skillgraph;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
/*
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("SkillGraph is running!");

        Scene scene = new Scene(label, 800, 500);

        stage.setTitle("SkillGraph");
        stage.setScene(scene);
        stage.show();
    }

 */
public class Main{
    public static void main(String[] args) {
        SkillGraph skillGraph = new SkillGraph();
        Skill a = new Skill("Java", "Programming Language", 5);
        Skill b = new Skill("OOOPS", "Programming Language", 5);
        SkillDependency s = new SkillDependency(a,b);
        skillGraph.addSkill(a);
        skillGraph.addSkill(b);
        skillGraph.addSkillDependency(s);
        System.out.println(skillGraph.getSkills());
        System.out.println(skillGraph.getDependencies());
        System.out.println(skillGraph.dependsOn(a));
    }
}