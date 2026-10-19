package org.example.skillgraph;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controls the main screen: the skill list (left), the Recommendations tab,
 * the interactive Skill Graph tab and the status bar.
 *
 * <p>The controller is the "glue" between the UI and the model classes
 * (SkillGraph, RecommendationEngine, SkillGraphStore). It contains no
 * skill/graph logic of its own - it only reads input, calls the model, and
 * shows the result.
 */
public class MainController {

    /* ---------- FXML widgets ---------- */
    @FXML private Label statusLabel;
    @FXML private ListView<Skill> skillListView;
    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private ComboBox<Integer> levelBox;
    @FXML private ComboBox<Integer> editLevelBox;
    @FXML private ComboBox<String> aimBox;
    @FXML private ListView<String> recommendationList;
    @FXML private Label recommendationSummary;
    @FXML private Pane graphContainer;
    @FXML private Canvas graphCanvas;
    @FXML private ComboBox<Skill> prereqBox;
    @FXML private ComboBox<Skill> reqBox;
    @FXML private ListView<SkillDependency> dependencyList;

    /* ---------- Model + view state ---------- */
    private SkillGraph skillGraph = new SkillGraph();
    private final RecommendationEngine engine = new RecommendationEngine();
    private final SkillGraphStore store = new SkillGraphStore();
    private final ResumeParser parser = new ResumeParser();

    /** Mirror lists bound to the views; they auto-refresh on every change. */
    private final ObservableList<Skill> skills = FXCollections.observableArrayList();
    private final ObservableList<SkillDependency> dependencies = FXCollections.observableArrayList();
    private final ObservableList<String> recommendations = FXCollections.observableArrayList();

    /* ---------- Graph drawing constants ---------- */
    private static final double NODE_RADIUS = 18;
    private static final Color EDGE_COLOR = Color.web("#94a3b8");

    /* =====================================================================
     *  Setup
     * ===================================================================== */

    @FXML
    private void initialize() {
        // Hook the observable lists to the views.
        skillListView.setItems(skills);
        prereqBox.setItems(skills);
        reqBox.setItems(skills);
        dependencyList.setItems(dependencies);
        recommendationList.setItems(recommendations);

        levelBox.getItems().addAll(Skill.MIN_LEVEL, 1, 2, 3, 4, Skill.MAX_LEVEL);
        levelBox.setValue(3);
        editLevelBox.getItems().addAll(Skill.MIN_LEVEL, 1, 2, 3, 4, Skill.MAX_LEVEL);
        aimBox.getItems().addAll(engine.getAims());

        // When a skill is picked in the list, show its current level in the box,
        // so "Set Level" starts from the real value and only needs changing.
        skillListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null) {
                        editLevelBox.setValue(selected.getLvl());
                    }
                });

        // Pretty rows: "Java  [Programming]  ...... ●●●○○"
        skillListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Skill skill, boolean empty) {
                super.updateItem(skill, empty);
                setText(null);
                if (empty || skill == null) {
                    setGraphic(null);
                    return;
                }
                Label name = new Label(skill.getName());
                name.getStyleClass().add("skill-name");
                Label chip = new Label(skill.getCategory());
                chip.getStyleClass().add("chip");
                Label pips = new Label(pips(skill.getLvl()));
                pips.getStyleClass().add("level-pips");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox row = new HBox(8, name, chip, spacer, pips);
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row);
            }
        });

        // Pretty rows for dependencies: "Java unlocks Spring"
        dependencyList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(SkillDependency dep, boolean empty) {
                super.updateItem(dep, empty);
                setText(null);
                if (empty || dep == null) {
                    setGraphic(null);
                    return;
                }
                Label from = new Label(dep.getPrereq().getName());
                Label arrow = new Label("unlocks");
                Label to = new Label(dep.getReq().getName());
                from.getStyleClass().add("dep-name");
                to.getStyleClass().add("dep-name");
                arrow.getStyleClass().add("dep-arrow");
                HBox row = new HBox(6, from, arrow, to);
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row);
            }
        });

        // Keep the canvas the same size as its container, and redraw on resize.
        graphCanvas.widthProperty().bind(graphContainer.widthProperty());
        graphCanvas.heightProperty().bind(graphContainer.heightProperty());
        graphCanvas.widthProperty().addListener((obs, old, now) -> drawGraph());
        graphCanvas.heightProperty().addListener((obs, old, now) -> drawGraph());

        loadOrSeed();
        refreshFromGraph();
    }

    /**
     * Loads the data saved last time. Only when there is nothing saved yet
     * does the app start with demo data.
     */
    private void loadOrSeed() {
        Path file = SkillGraphStore.defaultFile();
        try {
            if (Files.exists(file)) {
                skillGraph = store.load(file);
                setStatus("Loaded your saved skills from " + file);
            } else {
                seedSampleData();
                setStatus("Welcome! Sample data loaded - open the Skill Graph tab.");
            }
        } catch (IOException e) {
            seedSampleData();
            setStatus("Couldn't read your saved data (" + e.getMessage() + "), starting fresh.");
        }
    }

    /**
     * Loads a few demo skills/links used on first run.
     * Delete the call in loadOrSeed() to start with an empty graph instead.
     */
    private void seedSampleData() {
        Skill oop = new Skill("OOP", "Programming", 3);
        Skill java = new Skill("Java", "Programming", 4);
        Skill spring = new Skill("Spring", "Framework", 1);
        Skill sql = new Skill("SQL", "Database", 2);
        Skill git = new Skill("Git", "Tools", 3);
        Skill html = new Skill("HTML", "Web", 3);

        skillGraph.addSkill(oop);
        skillGraph.addSkill(java);
        skillGraph.addSkill(spring);
        skillGraph.addSkill(sql);
        skillGraph.addSkill(git);
        skillGraph.addSkill(html);

        skillGraph.addSkillDependency(new SkillDependency(oop, java));
        skillGraph.addSkillDependency(new SkillDependency(java, spring));
        skillGraph.addSkillDependency(new SkillDependency(sql, spring));
    }

    /**
     * Runs after every change (add/remove): copy the model into the view lists,
     * tell the user what happened, and write the data to disk.
     */
    private void applyChange(String statusMessage) {
        refreshFromGraph();
        setStatus(statusMessage);
        saveQuietly();
    }

    /** Copies the model's state into the observable lists and redraws the graph. */
    private void refreshFromGraph() {
        skills.setAll(skillGraph.getSkills());
        dependencies.setAll(skillGraph.getDependencies());
        drawGraph();
    }

    /** Saves the data; a failed save is reported but doesn't stop the app. */
    private void saveQuietly() {
        try {
            store.save(skillGraph, SkillGraphStore.defaultFile());
        } catch (IOException e) {
            setStatus("Warning: couldn't save your data (" + e.getMessage() + ")");
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    /* =====================================================================
     *  Header actions
     * ===================================================================== */

    @FXML
    private void handleUpload() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select your resume");
        // Only plain text: PDF and Word files are binary/zip formats and would
        // need an extra library (PDFBox, Apache POI) just to read them.
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text resume", "*.txt", "*.md"));

        // Start in the project folder, where sample-resume.txt lives.
        File startFolder = new File(System.getProperty("user.dir", "."));
        if (startFolder.isDirectory()) {
            chooser.setInitialDirectory(startFolder);
        }

        File file = chooser.showOpenDialog(statusLabel.getScene().getWindow());
        if (file == null) {
            setStatus("Upload cancelled.");
            return;
        }

        try {
            ResumeData resume = parser.parse(Files.readString(file.toPath()));
            importSkills(resume, file.getName());
        } catch (IOException e) {
            setStatus("Couldn't read '" + file.getName() + "': " + e.getMessage());
        }
    }

    /**
     * Adds the skills found in a resume to the graph.
     * Skills the user already has are skipped, nothing is added before the
     * user confirms, and every imported skill starts at level 1.
     */
    private void importSkills(ResumeData resume, String fileName) {
        List<Skill> fresh = new ArrayList<>();
        List<String> alreadyKnown = new ArrayList<>();
        for (Skill skill : resume.getSkills()) {
            if (skillGraph.hasSkill(skill.getName())) {
                alreadyKnown.add(skill.getName());
            } else {
                fresh.add(skill);
            }
        }

        if (resume.skillCount() == 0) {
            setStatus("Read '" + fileName + "' but recognised no skills in it.");
            return;
        }
        if (fresh.isEmpty()) {
            setStatus("Every skill found in '" + fileName + "' is already in your graph.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Import resume");
        confirm.setHeaderText("Found " + resume.skillCount() + " skills in " + fileName);
        confirm.setContentText(importSummary(resume, fresh, alreadyKnown));

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            setStatus("Import cancelled - nothing was changed.");
            return;
        }

        for (Skill skill : fresh) {
            skillGraph.addSkill(skill);
        }
        applyChange("Imported " + fresh.size() + " skills at level " + ResumeParser.DEFAULT_LEVEL
                + " - select one and use Set Level to adjust it.");
    }

    /** The text shown in the import confirmation dialog. */
    private String importSummary(ResumeData resume, List<Skill> fresh, List<String> alreadyKnown) {
        StringBuilder text = new StringBuilder();
        text.append("Name: ").append(resume.getName()).append('\n');
        text.append("Projects: ").append(resume.getPrjs().size())
            .append("    Education: ").append(resume.getEdu().size())
            .append("    Experience: ").append(resume.getExp().size()).append('\n');
        text.append("Found: ").append(names(resume.getSkills())).append('\n');
        if (!alreadyKnown.isEmpty()) {
            text.append("Already in your graph: ").append(alreadyKnown).append('\n');
        }
        text.append("Will be added: ").append(names(fresh));
        return text.toString();
    }

    private static List<String> names(List<Skill> skills) {
        List<String> names = new ArrayList<>();
        for (Skill skill : skills) {
            names.add(skill.getName());
        }
        return names;
    }

    /* =====================================================================
     *  Skill list actions
     * ===================================================================== */

    @FXML
    private void handleAddSkill() {
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        if (name.isEmpty() || category.isEmpty()) {
            setStatus("Enter both a name and a category first.");
            return;
        }
        if (skillGraph.hasSkill(name)) {
            setStatus("You already have '" + name + "'.");
            return;
        }

        int lvl = levelBox.getValue() == null ? 1 : levelBox.getValue();
        skillGraph.addSkill(new Skill(name, category, lvl));

        nameField.clear();
        categoryField.clear();
        levelBox.setValue(3);
        applyChange("Added '" + name + "' (level " + lvl + ").");
    }

    @FXML
    private void handleSetLevel() {
        Skill selected = skillListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a skill first, then choose a level for it.");
            return;
        }
        Integer lvl = editLevelBox.getValue();
        if (lvl == null) {
            setStatus("Choose a level (0-5) for '" + selected.getName() + "'.");
            return;
        }
        selected.setLvl(lvl);   // Skill itself keeps the value inside 0..5
        applyChange(selected.getName() + " is now level " + selected.getLvl() + ".");
    }

    @FXML
    private void handleRemoveSkill() {
        Skill selected = skillListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a skill to remove first.");
            return;
        }
        skillGraph.removeSkill(selected);
        applyChange("Removed '" + selected.getName() + "' and its links.");
    }

    /* =====================================================================
     *  Dependency actions
     * ===================================================================== */

    @FXML
    private void handleAddDependency() {
        Skill prereq = prereqBox.getValue();
        Skill target = reqBox.getValue();
        if (prereq == null || target == null) {
            setStatus("Pick both skills to link first.");
            return;
        }
        if (prereq.equals(target)) {
            setStatus("A skill can't unlock itself.");
            return;
        }
        if (skillGraph.hasDependency(prereq, target)) {
            setStatus("That link already exists.");
            return;
        }

        boolean added = skillGraph.addSkillDependency(new SkillDependency(prereq, target));
        if (!added) {
            setStatus("That link would create a loop (" + target.getName() + " is already a "
                    + "prerequisite of " + prereq.getName() + "), so it wasn't added.");
            return;
        }
        applyChange("Linked " + prereq.getName() + " -> " + target.getName() + ".");
    }

    @FXML
    private void handleRemoveDependency() {
        SkillDependency selected = dependencyList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a link to remove first.");
            return;
        }
        skillGraph.removeDependency(selected);
        applyChange("Removed link " + selected + ".");
    }

    /* =====================================================================
     *  Recommendations
     * ===================================================================== */

    @FXML
    private void handleRecommend() {
        String aim = aimBox.getValue();
        if (aim == null) {
            setStatus("Choose a career aim first.");
            return;
        }

        Recommendation rec = engine.recommend(skillGraph, aim);

        List<String> lines = new ArrayList<>();
        int step = 1;
        for (Skill needed : rec.getSkillsNeeded()) {
            Skill owned = skillGraph.findSkill(needed.getName());
            String note = (owned == null)
                    ? "learn from scratch (target level " + needed.getLvl() + ")"
                    : "level up from " + owned.getLvl() + " to " + needed.getLvl();
            lines.add(step++ + ". " + needed.getName() + " (" + needed.getCategory() + ") - " + note);
        }
        recommendations.setAll(lines);

        int total = engine.getRequiredSkills(aim).size();
        if (rec.isEmpty()) {
            recommendationSummary.setText("Great - you already meet every skill for '" + aim + "'.");
        } else {
            recommendationSummary.setText("You meet " + (total - rec.size()) + " of " + total
                    + " skills for '" + aim + "'. Suggestions are in learning order.");
        }
        setStatus("Generated recommendations for '" + aim + "'.");
    }

    /* =====================================================================
     *  Graph drawing
     * ===================================================================== */

    /** Draws all skills as a circle of nodes, with arrows for every dependency. */
    private void drawGraph() {
        double w = graphCanvas.getWidth();
        double h = graphCanvas.getHeight();
        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);
        if (w < 60 || h < 60) return;

        List<Skill> all = skillGraph.getSkills();
        if (all.isEmpty()) {
            gc.setFill(Color.web("#6b7280"));
            gc.setFont(Font.font("Segoe UI", 13));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Add some skills to see your graph", w / 2, h / 2);
            return;
        }

        // Place the nodes evenly on a circle.
        double cx = w / 2, cy = h / 2;
        double radius = Math.max(40, Math.min(w, h) / 2 - 55);
        Map<Skill, Point2D> positions = new HashMap<>();
        for (int i = 0; i < all.size(); i++) {
            double angle = 2 * Math.PI * i / all.size() - Math.PI / 2;
            positions.put(all.get(i),
                    new Point2D(cx + radius * Math.cos(angle), cy + radius * Math.sin(angle)));
        }

        // Draw every dependency as an arrow, then the nodes on top.
        for (SkillDependency dep : skillGraph.getDependencies()) {
            drawArrow(gc, positions.get(dep.getPrereq()), positions.get(dep.getReq()));
        }
        positions.forEach((skill, p) -> drawNode(gc, skill, p));
        drawLegend(gc);
    }

    private void drawNode(GraphicsContext gc, Skill skill, Point2D p) {
        double x = p.getX(), y = p.getY();

        gc.setFill(colorForLevel(skill.getLvl()));
        gc.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);
        gc.setStroke(Color.web("#ffffff"));
        gc.setLineWidth(2.5);
        gc.strokeOval(x - NODE_RADIUS, y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);

        // Level number inside the circle.
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.valueOf(skill.getLvl()), x, y + 5);

        // Skill name below the circle.
        gc.setFill(Color.web("#374151"));
        gc.setFont(Font.font("Segoe UI", 11));
        gc.fillText(skill.getName(), x, y + NODE_RADIUS + 16);
    }

    private void drawArrow(GraphicsContext gc, Point2D from, Point2D to) {
        if (from == null || to == null) return;
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < NODE_RADIUS * 2 + 14) return;   // nodes overlap, nothing to draw

        double ux = dx / len, uy = dy / len;
        double sx = from.getX() + ux * NODE_RADIUS;
        double sy = from.getY() + uy * NODE_RADIUS;
        double tipX = to.getX() - ux * (NODE_RADIUS + 4);
        double tipY = to.getY() - uy * (NODE_RADIUS + 4);
        double baseX = tipX - ux * 11;
        double baseY = tipY - uy * 11;

        gc.setStroke(EDGE_COLOR);
        gc.setLineWidth(2);
        gc.strokeLine(sx, sy, baseX, baseY);

        double px = -uy * 5, py = ux * 5;
        gc.setFill(EDGE_COLOR);
        gc.fillPolygon(new double[]{tipX, baseX + px, baseX - px},
                       new double[]{tipY, baseY + py, baseY - py}, 3);
    }

    private void drawLegend(GraphicsContext gc) {
        gc.setFont(Font.font("Segoe UI", 11));
        gc.setTextAlign(TextAlignment.LEFT);
        double x = 14, y = 10;
        String[][] legend = {
                {"low (0-1)", "#f87171"},
                {"medium (2-3)", "#fbbf24"},
                {"high (4-5)", "#34d399"}
        };
        for (String[] item : legend) {
            gc.setFill(Color.web(item[1]));
            gc.fillOval(x, y, 9, 9);
            gc.setFill(Color.web("#6b7280"));
            gc.fillText(item[0], x + 14, y + 8);
            x += 14 + item[0].length() * 6.5;
        }
    }

    private Color colorForLevel(int lvl) {
        if (lvl <= 1) return Color.web("#f87171");
        if (lvl <= 3) return Color.web("#fbbf24");
        return Color.web("#34d399");
    }

    /** e.g. level 3 -> "●●●○○" */
    private String pips(int lvl) {
        return "●".repeat(Math.max(0, Math.min(5, lvl)))
                + "○".repeat(5 - Math.max(0, Math.min(5, lvl)));
    }
}
