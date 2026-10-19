# SkillGraph

A JavaFX desktop app that maps your skills, shows how they unlock each other,
and works out what you still need to learn for a career goal - in the order you
should learn it.

Built as an OOPS course project with **Java 21**, **JavaFX** and **Maven**.

## Features

- **Skills with levels** - every skill has a name, a category and a level from 0 (not learnt yet) to 5 (expert).
- **Skill graph** - link skills as "prerequisite unlocks target" (for example *Java unlocks Spring*). Links that would create a loop are refused.
- **Visual graph** - all skills are drawn as nodes, coloured by level, with arrows for the links.
- **Career aims** - pick an aim (Backend Developer, Frontend Developer, Data Analyst) and get the skills you're missing or need to level up, listed in learning order.
- **Resume import** - read a plain-text resume, recognise the skills in it, and add them to your graph (after you confirm).
- **Saves itself** - everything is written to `~/.skillgraph/data.txt` (on Windows: `C:\Users\<you>\.skillgraph\data.txt`) and loaded next time. Delete that file to start fresh.

## Run

```bash
mvn javafx:run
```

## Test

```bash
mvn test
```

## Try it

1. Click **Upload Resume** and pick `sample-resume.txt` from the project folder.
2. Confirm the import - the recognised skills are added at level 1.
3. Select a skill and use **Set Level** to say how good you actually are.
4. Open the **Skill Graph** tab to see the graph; try linking two skills to see the loop warning.
5. Open **Recommendations**, choose an aim and hit **Get Recommendations**.

Resume import reads **plain text** (`.txt`, `.md`). PDF and Word files would need
an extra library (PDFBox / Apache POI) to read, so they are not accepted yet.

## How it fits together

The model is plain Java with no JavaFX in it, which is why it can be unit tested
without opening a window:

| Class | Job |
|---|---|
| `Skill` | One skill: name, category, level (0-5, validated). Skills are equal when their names match. |
| `SkillDependency` | One link: prerequisite -> target. |
| `SkillGraph` | Owns the skills and links. Fast lookup by name, duplicate prevention, cycle detection, learning order. |
| `CareerPath` | One career aim plus the skills it needs, at target levels. |
| `CareerCatalog` | The career data. Separate from the logic that matches it. |
| `RecommendationEngine` | Compares a `SkillGraph` with a `CareerPath` and returns a `Recommendation`. |
| `Recommendation` | Read-only result: the aim plus the skills still needed. |
| `ResumeParser` | Finds known skills, the name and the main sections in resume text. |
| `SkillVocabulary` | The list of skills the parser can recognise. |
| `ResumeData` | Read-only result of parsing: name, skills, projects, education, experience. |
| `SkillGraphStore` | Saves and loads a `SkillGraph` as a readable text file. |
| `MainController` | The glue between the FXML view and the model. |

## Project log

- First commit
- Project started on 20th Aug
