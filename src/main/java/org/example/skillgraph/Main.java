package org.example.skillgraph;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/main-view.fxml")
        );

        Scene scene = new Scene(root, 1024, 640);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("SkillGraph - map your skills, plan your path");
        stage.setMinWidth(860);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
/*
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
        System.out.println(skillGraph.unlocks(a));
        System.out.println(skillGraph.prerequisitesOf(b));
        System.out.println(skillGraph.learningOrder());
        System.out.println(skillGraph.hasCycle());
    }
}
 */