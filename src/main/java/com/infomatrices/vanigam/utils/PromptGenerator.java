/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.utils;

/**
 *
 * @author aravindhmuthuswamy
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;

public class PromptGenerator {

    private static final String ANNOTATION = "TODO-LLMW";

    public enum Template {
        GENERAL("General Programming Tasks"),
        SQL("SQL Query Tasks"),
        ANALYSE("Analyse Only");

        private final String label;
        Template(String label) { this.label = label; }

        public static Template fromLabel(String label) {
            for (Template t : values()) {
                if (t.label.equalsIgnoreCase(label)) return t;
            }
            return GENERAL;
        }
    }

    private final DefaultListModel<File> listModel;
    private final String ddlContent;
    private final boolean verbose;
    private final Template template;

    public PromptGenerator(DefaultListModel<File> listModel, String ddlContent,
            boolean verbose, String templateLabel) {
        this.listModel  = listModel;
        this.ddlContent = ddlContent;
        this.verbose    = verbose;
        this.template   = Template.fromLabel(templateLabel);
    }

    public String generate() {
        switch (template) {
            case SQL:     return generateSql();
            case ANALYSE: return generateAnalyse();
            default:      return generateGeneral();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEMPLATE 1 — General Programming Tasks
    // ─────────────────────────────────────────────────────────────────────
    private String generateGeneral() {
        StringBuilder sb = new StringBuilder();
        boolean hasDdl = isDdlProvided();

        List<File> implFiles = new ArrayList<>();
        List<File> refFiles  = new ArrayList<>();
        splitFiles(implFiles, refFiles);

        // Header
        sb.append("You are an expert software developer.\n");
        sb.append("Below is a list of source files that need to be implemented.\n");
        sb.append("Each file contains `").append(ANNOTATION)
          .append("` annotations marking exactly where code needs to be written.\n\n");

        // DDL
        appendDdl(sb, hasDdl);

        // Task
        sb.append("## Task\n");
        sb.append("For each implementation file listed below:\n");
        sb.append("1. Read the filename and understand the language.\n");
        sb.append("2. Locate every `").append(ANNOTATION).append("` annotation.\n");
        sb.append("3. Replace each `").append(ANNOTATION).append("` with a complete, correct implementation.\n");
        int step = 4;
        if (hasDdl)          sb.append(step++).append(". Ensure all database operations match the DDL schema above.\n");
        if (!refFiles.isEmpty()) sb.append(step++).append(". Use reference files for context only — do not modify them.\n");
        sb.append(step++).append(". Return the full updated file content for each implementation file.\n");
        sb.append(step).append(". Do not modify any code outside the `").append(ANNOTATION).append("` markers.\n\n");

        // Files
        appendImplFiles(sb, implFiles);
        appendRefFiles(sb, refFiles);

        // Instructions
        sb.append("## Instructions\n");
        sb.append("- Implement all `").append(ANNOTATION).append("` annotations with production-quality code.\n");
        sb.append("- Preserve all existing code, imports, and structure.\n");
        sb.append("- Follow the conventions already present in each file.\n");
        if (hasDdl) {
            sb.append("- All SQL queries must strictly follow the DDL schema above.\n");
            sb.append("- Use exact table and column names as defined in the DDL.\n");
        }
        if (!refFiles.isEmpty()) sb.append("- Do not return modified versions of reference files.\n");
        if (!verbose)           sb.append("- Read each file from the provided path before implementing.\n");
        sb.append("- Return each implementation file as a fenced code block labeled with the filename.\n");

        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEMPLATE 2 — SQL Query Tasks
    // ─────────────────────────────────────────────────────────────────────
   private String generateSql() {
    StringBuilder sb = new StringBuilder();
    boolean hasDdl = isDdlProvided();

    // SQL template does not require files — DDL is the primary input
    List<File> implFiles = new ArrayList<>();
    List<File> refFiles  = new ArrayList<>();
    splitFiles(implFiles, refFiles);

    // Header
    sb.append("You are an expert SQL developer and database engineer.\n");
    sb.append("Your task is to write SQL queries based on the requirements below.\n\n");

    // DDL
    if (hasDdl) {
        appendDdl(sb, true);
    } else {
        sb.append("> **Warning:** No DDL schema provided. ");
        sb.append("Provide DDL in the DDL panel for best results.\n\n");
    }

    // Task
    sb.append("## Task\n");
    sb.append("Write complete, optimised SQL queries for the following requirements:\n");
    sb.append("1. Use exact table and column names from the DDL schema above.\n");
    sb.append("2. Avoid SELECT * — always specify column names explicitly.\n");
    sb.append("3. Use proper JOINs, WHERE clauses, and indexing for performance.\n");
    sb.append("4. Prefer parameterised queries to prevent SQL injection.\n");
    sb.append("5. Use transactions where multiple related DML statements are needed.\n");
    sb.append("6. Add meaningful aliases for readability.\n");
    sb.append("7. For INSERT/UPDATE, list all non-nullable columns explicitly.\n");
    sb.append("8. Add ORDER BY only when ordering is functionally required.\n\n");

    // Include files only if present
    if (!implFiles.isEmpty() || !refFiles.isEmpty()) {
        sb.append("## Related Files\n");
        sb.append("The following files provide context on how SQL is used in the codebase ");
        sb.append("(JDBC, JPA, MyBatis, raw SQL, etc.).\n\n");

        List<File> allFiles = new ArrayList<>();
        allFiles.addAll(implFiles);
        allFiles.addAll(refFiles);
        int fileNum = 1;
        for (File file : allFiles) {
            appendFile(sb, file, "File", fileNum++, !scanTodos(file).isEmpty());
        }
    }

    // Instructions
    sb.append("## Instructions\n");
    sb.append("- All queries must use exact table and column names from the DDL schema.\n");
    sb.append("- Prefer parameterised queries to prevent SQL injection.\n");
    sb.append("- Add meaningful aliases for readability.\n");
    sb.append("- For INSERT/UPDATE, list all non-nullable columns explicitly.\n");
    sb.append("- For SELECT queries, specify only required columns — never SELECT *.\n");
    sb.append("- Add ORDER BY only when ordering is functionally required.\n");
    sb.append("- Use transactions where multiple related DML statements are needed.\n");
    sb.append("- Return each query in a separate fenced ```sql code block with a descriptive comment header.\n");

    return sb.toString();
}

    // ─────────────────────────────────────────────────────────────────────
    // TEMPLATE 3 — Analyse Only
    // ─────────────────────────────────────────────────────────────────────
    private String generateAnalyse() {
        StringBuilder sb = new StringBuilder();
        boolean hasDdl = isDdlProvided();

        List<File> implFiles = new ArrayList<>();
        List<File> refFiles  = new ArrayList<>();
        splitFiles(implFiles, refFiles);

        // All files are treated as analysis targets — merge both lists
        List<File> allFiles = new ArrayList<>();
        allFiles.addAll(implFiles);
        allFiles.addAll(refFiles);

        // Header
        sb.append("You are an expert software architect and code reviewer.\n");
        sb.append("Analyse the following source files and provide a detailed technical review.\n");
        sb.append("Do NOT modify or rewrite any code — this is an analysis task only.\n\n");

        // DDL context
        if (hasDdl) {
            appendDdl(sb, true);
        }

        // Task
        sb.append("## Task\n");
        sb.append("For each file listed below, provide:\n");
        sb.append("1. **Summary** — what the file does and its role in the system.\n");
        sb.append("2. **Code Quality** — assess readability, structure, naming conventions, and complexity.\n");
        sb.append("3. **Bugs & Issues** — identify any bugs, null pointer risks, or logic errors.\n");
        sb.append("4. **Performance** — flag any inefficient loops, queries, or memory usage.\n");
        sb.append("5. **Security** — highlight injection risks, improper input validation, or exposure of sensitive data.\n");
        sb.append("6. **").append(ANNOTATION).append(" markers** — for each marker found, suggest what the implementation should be.\n");
        if (hasDdl) {
            sb.append("7. **Schema alignment** — verify the code aligns with the DDL schema provided above.\n");
        }
        sb.append("\n");

        // Files
        if (!allFiles.isEmpty()) {
            sb.append("## Files\n\n");
            int fileNum = 1;
            for (File file : allFiles) {
                String ext  = getFileExtension(file.getName());
                String lang = getLanguageFromExtension(ext);
                List<String> todos = scanTodos(file);

                sb.append("### File ").append(fileNum++).append(": `").append(file.getName()).append("`\n");
                sb.append("- **Path:** `").append(file.getAbsolutePath()).append("`\n");
                sb.append("- **Language:** ").append(lang).append("\n");

                if (!todos.isEmpty()) {
                    sb.append("- **").append(ANNOTATION).append(" markers found:**\n");
                    for (String todo : todos) {
                        sb.append("  - Line ").append(todo).append("\n");
                    }
                } else {
                    sb.append("- **").append(ANNOTATION).append(" markers:** None.\n");
                }

                if (verbose) {
                    sb.append("\n```").append(lang.toLowerCase()).append("\n");
                    try {
                        String content = new String(Files.readAllBytes(file.toPath()));
                        sb.append(content);
                        if (!content.endsWith("\n")) sb.append("\n");
                    } catch (IOException ex) {
                        sb.append("// Could not read file: ").append(ex.getMessage()).append("\n");
                    }
                    sb.append("```\n\n");
                } else {
                    sb.append("\n");
                }
            }
        }

        // Instructions
        sb.append("## Instructions\n");
        sb.append("- Do NOT rewrite or modify any code.\n");
        sb.append("- Structure your analysis per file using the headings: Summary, Code Quality, Bugs & Issues, Performance, Security");
        if (hasDdl) sb.append(", Schema Alignment");
        sb.append(".\n");
        sb.append("- For each `").append(ANNOTATION).append("` marker, suggest a concrete implementation approach.\n");
        sb.append("- Be specific — reference actual line numbers, method names, and variable names in your findings.\n");
        sb.append("- Prioritise findings as: 🔴 Critical, 🟠 Major, 🟡 Minor, 🟢 Suggestion.\n");

        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Shared helpers
    // ─────────────────────────────────────────────────────────────────────

    private void splitFiles(List<File> implFiles, List<File> refFiles) {
        for (int i = 0; i < listModel.size(); i++) {
            File file = listModel.getElementAt(i);
            if (scanTodos(file).isEmpty()) {
                refFiles.add(file);
            } else {
                implFiles.add(file);
            }
        }
    }

    private void appendDdl(StringBuilder sb, boolean hasDdl) {
        if (!hasDdl) return;
        sb.append("## Database Schema (DDL)\n");
        sb.append("The following DDL defines the database schema your implementation must align with.\n");
        sb.append("Use the correct table names, column names, and data types from this schema.\n\n");
        sb.append("```sql\n");
        sb.append(ddlContent.trim()).append("\n");
        sb.append("```\n\n");
    }

    private void appendImplFiles(StringBuilder sb, List<File> implFiles) {
        if (implFiles.isEmpty()) return;
        sb.append("## Files\n\n");
        int fileNum = 1;
        for (File file : implFiles) {
            appendFile(sb, file, "File", fileNum++, true);
        }
    }

    private void appendRefFiles(StringBuilder sb, List<File> refFiles) {
        if (refFiles.isEmpty()) return;
        sb.append("## Reference Files\n");
        sb.append("These files contain no `").append(ANNOTATION)
          .append("` annotations. Use them for context, structure, and conventions only.\n\n");
        int fileNum = 1;
        for (File file : refFiles) {
            appendFile(sb, file, "Reference", fileNum++, false);
        }
    }

    private void appendFile(StringBuilder sb, File file, String prefix,
            int fileNum, boolean isImpl) {
        String ext  = getFileExtension(file.getName());
        String lang = getLanguageFromExtension(ext);

        sb.append("### ").append(prefix).append(" ").append(fileNum)
          .append(": `").append(file.getName()).append("`\n");
        sb.append("- **Path:** `").append(file.getAbsolutePath()).append("`\n");
        sb.append("- **Language:** ").append(lang).append("\n");

        if (isImpl) {
            List<String> todos = scanTodos(file);
            if (!todos.isEmpty()) {
                sb.append("- **").append(ANNOTATION).append(" markers found:**\n");
                for (String todo : todos) {
                    sb.append("  - Line ").append(todo).append("\n");
                }
            }
        }

        if (verbose) {
            sb.append("\n```").append(lang.toLowerCase()).append("\n");
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                sb.append(content);
                if (!content.endsWith("\n")) sb.append("\n");
            } catch (IOException ex) {
                sb.append("// Could not read file: ").append(ex.getMessage()).append("\n");
            }
            sb.append("```\n\n");
        } else {
            if (isImpl) {
                sb.append("- **").append(ANNOTATION).append(" count:** ")
                  .append(scanTodos(file).size()).append("\n\n");
            } else {
                sb.append("\n");
            }
        }
    }

    private List<String> scanTodos(File file) {
        List<String> todos = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).contains(ANNOTATION)) {
                    todos.add((i + 1) + ": " + lines.get(i).trim());
                }
            }
        } catch (IOException ignored) {}
        return todos;
    }

    private boolean isDdlProvided() {
        if (ddlContent == null || ddlContent.isBlank()) return false;
        String t = ddlContent.trim();
        return !t.equals("-- Paste your DDL statements here")
            && !t.equals("-- Paste your DDL statements here\n-- e.g. CREATE TABLE, ALTER TABLE...");
    }

    private String getFileExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot != -1 ? fileName.substring(dot + 1).toLowerCase() : "";
    }

    private String getLanguageFromExtension(String ext) {
        switch (ext) {
            case "java":       return "Java";
            case "py":         return "Python";
            case "js":         return "JavaScript";
            case "ts":         return "TypeScript";
            case "html":       return "HTML";
            case "css":        return "CSS";
            case "xml":        return "XML";
            case "json":       return "JSON";
            case "sql":        return "SQL";
            case "cpp":        return "C++";
            case "c":          return "C";
            case "cs":         return "C#";
            case "rb":         return "Ruby";
            case "php":        return "PHP";
            case "sh":         return "Shell";
            case "kt":         return "Kotlin";
            case "swift":      return "Swift";
            case "go":         return "Go";
            case "rs":         return "Rust";
            case "md":         return "Markdown";
            case "yaml":
            case "yml":        return "YAML";
            case "properties": return "Properties";
            case "lua":        return "Lua";
            default:           return "Text";
        }
    }
}