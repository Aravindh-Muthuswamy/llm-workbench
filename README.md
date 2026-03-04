# LLM Workbench

> A Java Swing desktop application that helps you select coding files and DDL statements before generating prompts for Large Language Models.

> 🚧 **Research Preview** — This software is currently in research preview and is **not intended for production use**. Features may be incomplete, unstable, or subject to breaking changes without notice.

---

## About

**LLM Workbench** is a Java-based desktop GUI tool designed to streamline the process of preparing context for LLM interactions. Instead of manually copying and pasting source code or database schema into a prompt, LLM Workbench lets you browse and select the specific files and DDL statements you want to include — then assembles a clean, well-structured prompt ready to send to any LLM.

This is especially useful for developers who work with large codebases or complex database schemas and want to ask an LLM targeted questions without exceeding token limits or including irrelevant context.

---

## Features

- 📁 **File selector** — browse your project directory and pick the source files to include in your prompt
- 🗄️ **DDL support** — select database schema (DDL statements) alongside code for schema-aware prompting
- ✍️ **Prompt generation** — assembles selected content into a structured, copy-ready LLM prompt
- 🖥️ **Rich Swing UI** — polished desktop interface powered by [FlatLaf](https://www.formdev.com/flatlaf/) with syntax-highlighted code editing via RSyntaxTextArea
- 🔍 **Autocomplete** — code editor autocomplete support via the AutoComplete library
- 💾 **Persistent storage** — stores selections and session data via a local SQLite database (`llm-workbench.db`) managed with Liquibase migrations
- 🖥️ **Embedded terminal** — built-in terminal emulator via JediTerm + pty4j
- 📅 **Date picker** — LGoodDatePicker integration for date-based filtering
- 📊 **Reporting** — JasperReports integration for generating reports

---

## Prerequisites

- **Java 24** or higher
- **Apache Maven 3.6+** (or use the included `mvnw` wrapper)
- **NetBeans IDE** (recommended — project includes `nbactions.xml` for IDE-native run/build actions)

---

## Installation

### Clone the repository

```bash
git clone https://github.com/Aravindh-Muthuswamy/llm-workbench.git
cd llm-workbench
```

### Build the project

Using the Maven wrapper (no Maven installation required):

```bash
# macOS / Linux
./mvnw clean package

# Windows
mvnw.cmd clean package
```

Or with a local Maven installation:

```bash
mvn clean package
```

This produces a fat JAR with all dependencies bundled:

```
target/llm-workbench-1.0-SNAPSHOT-jar-with-dependencies.jar
```

---

## Usage

### Run from the fat JAR

```bash
java -jar target/llm-workbench-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Run from NetBeans

Open the project in **Apache NetBeans**, then use **Run > Run Project** (or press `F6`). The `nbactions.xml` file pre-configures the run and release actions for you.

### Workflow

1. **Select files** — choose the source code files you want to include as context
2. **Select DDL** — optionally add relevant database schema statements
3. **Generate prompt** — LLM Workbench assembles your selections into a ready-to-use prompt
4. **Copy & paste** — paste the generated prompt into your LLM of choice (ChatGPT, Claude, Gemini, etc.)

---

## Project Structure

```
llm-workbench/
├── src/                    # Java source code
│   └── main/java/com/infomatrices/vanigam/
│       └── LlmWorkbench.java   # Application entry point
├── lib/                    # Local/external library dependencies
├── target/                 # Compiled output (generated)
├── llm-workbench.db        # Local SQLite database for persistence
├── pom.xml                 # Maven project configuration
├── mvnw / mvnw.cmd         # Maven wrapper scripts
├── nbactions.xml           # NetBeans run/build actions
└── nbactions-release-profile.xml  # NetBeans release profile
```

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 24 | Core application language |
| Maven | 3.6+ | Build and dependency management |
| FlatLaf | 3.6.2 | Modern Swing look and feel |
| Hibernate ORM | 7.1.10 | ORM / data persistence |
| SQLite JDBC | 3.51.0 | Local database driver |
| Liquibase | 5.0.1 | Database schema migrations |
| RSyntaxTextArea | 3.6.0 | Syntax-highlighted code editor |
| AutoComplete | 3.3.2 | Code editor autocomplete |
| JediTerm | 3.61 | Embedded terminal emulator |
| pty4j | 0.13.12 | Pseudo-terminal support |
| JasperReports | 7.0.3 | Report generation |
| LGoodDatePicker | 11.2.1 | Date picker UI component |
| SwingX | 1.6.5 | Extended Swing components |
| Ikonli (Codicons) | 12.3.1 | Icon pack for the UI |
| Lombok | 1.18.42 | Boilerplate reduction |

---

## Contributing

Contributions are welcome! To get started:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add your feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

Please open an issue first to discuss significant changes.

---

## Privacy & LLM Usage

> ⚠️ **LLM Workbench does not send any data to any LLM provider.**

This tool is purely a **local preprocessing assistant**. It helps you select and assemble your files and DDL statements into a structured prompt — but it stops there. No content is transmitted over the network to any AI service.

> 🔧 **The prompt is generated by a simple Java function** that assembles your selected files and DDL statements based on a chosen template. There is no AI or LLM involved in the prompt generation process — it is purely deterministic string assembly logic. See [`PromptGenerator.java`](src/main/java/com/infomatrices/vanigam/PromptGenerator.java) for the full implementation.

**It is entirely your responsibility to:**
- Launch your LLM of choice (via the embedded terminal, a browser, or any other interface)
- Copy the generated prompt and paste it into your LLM provider (e.g. ChatGPT, Claude, Gemini, a locally running model via Ollama, etc.)
- Ensure you comply with your LLM provider's terms of service regarding the content you submit

This design means your source code and database schemas **never leave your machine** unless you choose to send them.

---

## License

This project is licensed under the [MIT License](LICENSE).

### What the MIT License allows
- ✅ Use the software for any purpose, including commercially
- ✅ Modify the source code freely
- ✅ Distribute original or modified copies
- ✅ Include it in proprietary projects without open-sourcing your own code

### What the MIT License requires
- 📄 The original copyright notice and license text must be included in any copy or substantial portion of the software

### What the MIT License does not cover
- ❌ No warranty is provided — the software is provided **"as is"**
- ❌ The author is not liable for any damages or issues arising from use of the software
- ❌ It does not grant any trademark rights to the project name or author's name

---

## Author

**Aravindh Muthuswamy** — [@Aravindh-Muthuswamy](https://github.com/Aravindh-Muthuswamy)
