package org.example.skillgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reads a resume that was saved as plain text and pulls out the useful parts:
 *
 * <ul>
 *   <li>the name (from the first non-empty line),</li>
 *   <li>every skill from the {@link SkillVocabulary} that is mentioned anywhere,</li>
 *   <li>the projects / education / experience sections.</li>
 * </ul>
 *
 * <p>This is deliberately simple - it looks for known words, it doesn't try to
 * understand the sentences. That is exactly how many real "resume screeners"
 * start out, and it is easy to explain: <i>we know a list of skills, we search
 * the text for them, and we keep the ones we find.</i>
 */
public class ResumeParser {

    /** Level given to a skill that was only mentioned in the text. */
    public static final int DEFAULT_LEVEL = 1;

    /** Section titles we understand. A line that is exactly one of these starts a section. */
    private static final Set<String> HEADERS = Set.of(
            "SKILLS", "TECHNICAL SKILLS", "SUMMARY", "OBJECTIVE", "PROFILE",
            "PROJECTS", "PROJECT", "EDUCATION", "EXPERIENCE", "WORK EXPERIENCE",
            "EMPLOYMENT", "INTERNSHIPS", "CERTIFICATIONS", "LANGUAGES", "INTERESTS");

    private final SkillVocabulary vocabulary;

    public ResumeParser() {
        this(new SkillVocabulary());
    }

    /** Lets tests (or different vocabularies) be plugged in. */
    public ResumeParser(SkillVocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    /** Turns the text of a resume into a ResumeData object. */
    public ResumeData parse(String text) {
        if (text == null) {
            text = "";
        }
        List<String> lines = List.of(text.split("\\R"));

        return new ResumeData(
                findName(lines),
                findSkills(text),
                section(lines, "PROJECTS", "PROJECT"),
                section(lines, "EDUCATION"),
                section(lines, "EXPERIENCE", "WORK EXPERIENCE", "EMPLOYMENT", "INTERNSHIPS"));
    }

    /* ---------- name ---------- */

    /**
     * The name is assumed to be the first non-empty line, but only if it looks
     * like a name (short, no digits, no email address). Otherwise we say we
     * don't know rather than pretending.
     */
    private String findName(List<String> lines) {
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue; // skip blank lines at the top
            }
            if (isHeader(trimmed)) {
                return "Unknown";   // the resume starts with a section, so there is no name line
            }
            boolean looksLikeAName = trimmed.length() <= 40
                    && !trimmed.contains("@")
                    && !trimmed.matches(".*\\d.*");   // .*\d.* = "contains a digit"
            return looksLikeAName ? trimmed : "Unknown";
        }
        return "Unknown";
    }

    /* ---------- skills ---------- */

    /** Every known skill whose name appears in the text. */
    private List<Skill> findSkills(String text) {
        List<Skill> found = new ArrayList<>();
        for (Skill known : vocabulary.all()) {
            if (containsWord(text, known.getName())) {
                found.add(new Skill(known.getName(), known.getCategory(), DEFAULT_LEVEL));
            }
        }
        return found;
    }

    /**
     * True if {@code word} appears in {@code text}, ignoring case, and as a
     * whole word rather than part of a longer word.
     *
     * <p>The whole-word part matters: without it, the skill "Java" would also
     * match inside "JavaScript", and every Java-script developer would be
     * reported as a Java developer.
     */
    private boolean containsWord(String text, String word) {
        String haystack = text.toLowerCase(Locale.ROOT);
        String needle = word.toLowerCase(Locale.ROOT);

        int from = 0;
        while (true) {
            int at = haystack.indexOf(needle, from);
            if (at < 0) {
                return false;                       // not found anywhere
            }
            int end = at + needle.length();
            boolean clearBefore = at == 0 || !isWordChar(haystack.charAt(at - 1));
            boolean clearAfter = end == haystack.length() || !isWordChar(haystack.charAt(end));
            if (clearBefore && clearAfter) {
                return true;
            }
            from = at + 1;                          // part of a longer word - keep looking
        }
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c);
    }

    /* ---------- sections ---------- */

    /**
     * Collects the non-empty lines that belong to one section.
     *
     * <p>Walking the lines once is enough: any section title ends the previous
     * section, so we only keep lines while the "current" section is the one we
     * are after.
     */
    private List<String> section(List<String> lines, String... titles) {
        List<String> result = new ArrayList<>();
        boolean inside = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (isHeader(trimmed)) {
                inside = isOneOf(trimmed, titles);   // a new section starts here
            } else if (inside && !trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /** Is this line a section title? */
    private boolean isHeader(String line) {
        return HEADERS.contains(normalize(line));
    }

    private boolean isOneOf(String line, String... titles) {
        String normalized = normalize(line);
        for (String title : titles) {
            if (normalized.equals(title)) {
                return true;
            }
        }
        return false;
    }

    /** "  Education: " and "EDUCATION" both become "EDUCATION". */
    private static String normalize(String line) {
        String text = line.trim();
        while (text.endsWith(":") || text.endsWith("-")) {
            text = text.substring(0, text.length() - 1).trim();
        }
        return text.toUpperCase(Locale.ROOT);
    }
}
