/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.infomatrices.vanigam.dashboard;

import static com.infomatrices.vanigam.utils.CodeUtils.getSyntaxStyle;
import com.infomatrices.vanigam.utils.FileSearchWorker;
import com.infomatrices.vanigam.utils.PromptGenerator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import javax.swing.tree.DefaultTreeCellRenderer;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.fife.ui.rtextarea.SearchContext;

/**
 *
 * @author aravindhmuthuswamy
 */
public class DashboardForm extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DashboardForm.class.getName());
    private JTree tree;
    private RSyntaxTextArea textArea;
    private DefaultTreeModel treeModel;
    private JTextField searchField;        // add this
    private DefaultMutableTreeNode root;
    private JComponent terminalPanel;// add this (make root a field)
    private DefaultListModel<File> listModel;
    private JList<File> fileList;
    private RSyntaxTextArea promptArea;
    private JCheckBox verboseCheckBox;
    private JTabbedPane editorTabs;
    private java.util.Map<File, Integer> openFileTabs = new java.util.HashMap<>();
    private RSyntaxTextArea ddlArea;
    private SearchContext searchContext = new SearchContext();
    private javax.swing.SwingWorker<Void, DefaultMutableTreeNode> searchWorker;
    private File projectRootFolder;
    private JComboBox<String> templateSelector;

    /**
     * Creates new form DashboardForm
     */
    public DashboardForm() {
        initComponents();
        setupKeyBindings(getRootPane());
        loadComponents();
        loadPromptComponents();
        loadDdlComponents();
    }

    private void loadDdlComponents() {
        ddlArea = new RSyntaxTextArea(10, 80);
        ddlArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        ddlArea.setCodeFoldingEnabled(true);
        ddlArea.setAntiAliasingEnabled(true);
        ddlArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
        ddlArea.setText("-- Paste your DDL statements here\n-- e.g. CREATE TABLE, ALTER TABLE...\n");

        JButton clearDdlBtn = new JButton("Clear");
        clearDdlBtn.addActionListener(e -> ddlArea.setText(""));

        JLabel ddlHint = new JLabel("  DDL will be included in the generated prompt when not empty");
        ddlHint.setForeground(java.awt.Color.GRAY);
        ddlHint.setFont(ddlHint.getFont().deriveFont(java.awt.Font.ITALIC, 11f));

        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.add(clearDdlBtn, BorderLayout.WEST);
        btnPanel.add(ddlHint, BorderLayout.CENTER);

        ddlJPanel.setLayout(new BorderLayout());
        ddlJPanel.add(btnPanel, BorderLayout.NORTH);
        ddlJPanel.add(new RTextScrollPane(ddlArea), BorderLayout.CENTER);

        ddlJPanel.revalidate();
        ddlJPanel.repaint();

    }

    private void loadPromptComponents() {
        promptArea = new RSyntaxTextArea(10, 80);
        promptArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
        promptArea.setCodeFoldingEnabled(false);
        promptArea.setAntiAliasingEnabled(true);
        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);
        promptArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));

        JButton generateBtn = new JButton("Generate Prompt");
        generateBtn.addActionListener(e -> generatePrompt());

        JButton copyBtn = new JButton("Copy");
        copyBtn.addActionListener(e -> {
            promptArea.selectAll();
            promptArea.copy();
            promptArea.select(0, 0);
        });

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> promptArea.setText(""));

        verboseCheckBox = new JCheckBox("Verbose");
        verboseCheckBox.setSelected(false);

        templateSelector = new JComboBox<>(new String[]{
            "General Programming Tasks",
            "SQL Query Tasks",
            "Analyse Only"
        });

        JLabel templateLabel = new JLabel("Template:");

        JPanel btnPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 2));
        btnPanel.add(templateLabel);
        btnPanel.add(templateSelector);
        btnPanel.add(new JSeparator(JSeparator.VERTICAL));
        btnPanel.add(verboseCheckBox);
        btnPanel.add(new JSeparator(JSeparator.VERTICAL));
        btnPanel.add(generateBtn);
        btnPanel.add(copyBtn);
        btnPanel.add(clearBtn);

        promptJPanel.setLayout(new BorderLayout());
        promptJPanel.add(btnPanel, BorderLayout.NORTH);
        promptJPanel.add(new RTextScrollPane(promptArea), BorderLayout.CENTER);

        promptJPanel.revalidate();
        promptJPanel.repaint();

    }

    private JPanel createFindBar() {
        JTextField findField = new JTextField(20);
        JTextField replaceField = new JTextField(20);

        JCheckBox matchCase = new JCheckBox("Match Case");
        JCheckBox wholeWord = new JCheckBox("Whole Word");
        JCheckBox regex = new JCheckBox("Regex");

        JButton findNext = new JButton("Find Next");
        JButton findPrev = new JButton("Find Prev");
        JButton replaceBtn = new JButton("Replace");
        JButton replaceAll = new JButton("Replace All");
        JButton closeBtn = new JButton("✕");

        // Labels
        JLabel findLabel = new JLabel("Find:");
        JLabel replaceLabel = new JLabel("Replace:");

        JPanel bar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 2));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, java.awt.Color.GRAY));
        bar.add(findLabel);
        bar.add(findField);
        bar.add(replaceLabel);
        bar.add(replaceField);
        bar.add(matchCase);
        bar.add(wholeWord);
        bar.add(regex);
        bar.add(findNext);
        bar.add(findPrev);
        bar.add(replaceBtn);
        bar.add(replaceAll);
        bar.add(closeBtn);

        // Actions
        findNext.addActionListener(e -> doFind(findField.getText(), true, matchCase, wholeWord, regex));
        findPrev.addActionListener(e -> doFind(findField.getText(), false, matchCase, wholeWord, regex));

        replaceBtn.addActionListener(e -> {
            RSyntaxTextArea editor = getActiveEditor();
            if (editor == null || findField.getText().isEmpty()) {
                return;
            }
            updateSearchContext(findField.getText(), matchCase, wholeWord, regex);
            searchContext.setReplaceWith(replaceField.getText());
            org.fife.ui.rtextarea.SearchEngine.replace(editor, searchContext);
            // Find next after replace
            org.fife.ui.rtextarea.SearchEngine.find(editor, searchContext);
        });

        replaceAll.addActionListener(e -> {
            RSyntaxTextArea editor = getActiveEditor();
            if (editor == null || findField.getText().isEmpty()) {
                return;
            }
            updateSearchContext(findField.getText(), matchCase, wholeWord, regex);
            searchContext.setReplaceWith(replaceField.getText());
            org.fife.ui.rtextarea.SearchEngine.replaceAll(editor, searchContext);
        });

        // Close hides the bar
        closeBtn.addActionListener(e -> {
            bar.setVisible(false);
            RSyntaxTextArea editor = getActiveEditor();
            if (editor != null) {
                editor.requestFocusInWindow();
            }
        });

        // Find on Enter key in find field
        findField.addActionListener(e
                -> doFind(findField.getText(), true, matchCase, wholeWord, regex));

        // Auto-search as you type
        findField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                doFind(findField.getText(), true, matchCase, wholeWord, regex);
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                doFind(findField.getText(), true, matchCase, wholeWord, regex);
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
        });

        bar.setVisible(false); // hidden by default
        return bar;
    }

    private void updateSearchContext(String text, JCheckBox matchCase,
            JCheckBox wholeWord, JCheckBox regex) {
        searchContext.setSearchFor(text);
        searchContext.setMatchCase(matchCase.isSelected());
        searchContext.setWholeWord(wholeWord.isSelected());
        searchContext.setRegularExpression(regex.isSelected());
        searchContext.setSearchForward(true);
        searchContext.setMarkAll(true);  // highlights all matches
    }

    private RSyntaxTextArea getActiveEditor() {
        int idx = editorTabs.getSelectedIndex();
        if (idx == -1) {
            return null;
        }
        java.awt.Component comp = editorTabs.getComponentAt(idx);
        if (!(comp instanceof RTextScrollPane)) {
            return null;
        }
        return (RSyntaxTextArea) ((RTextScrollPane) comp).getViewport().getView();
    }

    private void doFind(String text, boolean forward, JCheckBox matchCase,
            JCheckBox wholeWord, JCheckBox regex) {
        RSyntaxTextArea editor = getActiveEditor();
        if (editor == null || text.isEmpty()) {
            return;
        }
        updateSearchContext(text, matchCase, wholeWord, regex);
        searchContext.setSearchForward(forward);
        org.fife.ui.rtextarea.SearchResult result
                = org.fife.ui.rtextarea.SearchEngine.find(editor, searchContext);
        if (!result.wasFound()) {
            // Wrap around
            editor.setCaretPosition(forward ? 0 : editor.getDocument().getLength());
            org.fife.ui.rtextarea.SearchEngine.find(editor, searchContext);
        }
    }

    private static class ProjectState implements java.io.Serializable {

        private static final long serialVersionUID = 1L;
        String projectFolder;
        java.util.List<String> fileListPaths;
        String ddlContent;
        String promptContent;

        public ProjectState(String projectFolder, java.util.List<String> fileListPaths,
                String ddlContent, String promptContent) {
            this.projectFolder = projectFolder;
            this.fileListPaths = fileListPaths;
            this.ddlContent = ddlContent;
            this.promptContent = promptContent;
        }
    }

    private void saveProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Project");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "LLM Workbench Project (*.lwb)", "lwb"));
        chooser.setSelectedFile(new File("project.lwb"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File saveFile = chooser.getSelectedFile();
        if (!saveFile.getName().endsWith(".lwb")) {
            saveFile = new File(saveFile.getAbsolutePath() + ".lwb");
        }

        // Collect file list paths
        java.util.List<String> paths = new java.util.ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            paths.add(listModel.getElementAt(i).getAbsolutePath());
        }

        // Collect project folder from root node
        String projectFolder = "";
        if (root != null && root.getUserObject() instanceof File) {
            projectFolder = ((File) root.getUserObject()).getAbsolutePath();
        }

        ProjectState state = new ProjectState(
                projectFolder,
                paths,
                ddlArea != null ? ddlArea.getText() : "",
                promptArea != null ? promptArea.getText() : ""
        );

        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(
                new java.io.FileOutputStream(saveFile))) {
            oos.writeObject(state);
            JOptionPane.showMessageDialog(this,
                    "Project saved: " + saveFile.getName(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save project: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Project");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "LLM Workbench Project (*.lwb)", "lwb"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.FileInputStream(chooser.getSelectedFile()))) {

            ProjectState state = (ProjectState) ois.readObject();

            // Restore project folder tree
            if (state.projectFolder != null && !state.projectFolder.isEmpty()) {
                File folder = new File(state.projectFolder);
                if (folder.exists()) {
                    loadDirectoryTree(folder);
                }
            }

            // Restore file list
            listModel.clear();
            openFileTabs.clear();

            // Close all open tabs
            editorTabs.removeAll();

            if (state.fileListPaths != null) {
                for (String path : state.fileListPaths) {
                    File f = new File(path);
                    if (f.exists()) {
                        listModel.addElement(f);
                    }
                }
            }

            // Restore DDL
            if (ddlArea != null && state.ddlContent != null) {
                ddlArea.setText(state.ddlContent);
            }

            // Restore prompt
            if (promptArea != null && state.promptContent != null) {
                promptArea.setText(state.promptContent);
            }

            JOptionPane.showMessageDialog(this,
                    "Project loaded successfully.",
                    "Loaded", JOptionPane.INFORMATION_MESSAGE);
            if (state.projectFolder != null && !state.projectFolder.isEmpty()) {
                setTitle("LLM Workbench v0.2 - " + new File(state.projectFolder).getName());
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load project: " + ex.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generatePrompt() {
        String template = templateSelector != null
                ? (String) templateSelector.getSelectedItem()
                : "General Programming Tasks";

        boolean isSql = "SQL Query Tasks".equals(template);

        // Only require files for non-SQL templates
        if (!isSql && listModel.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No files in the list. Right-click files in the tree to add them.",
                    "No Files", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ddlContent = ddlArea != null ? ddlArea.getText() : "";
        boolean verbose = verboseCheckBox != null && verboseCheckBox.isSelected();

        PromptGenerator generator = new PromptGenerator(listModel, ddlContent, verbose, template);
        promptArea.setText(generator.generate());
        promptArea.setCaretPosition(0);

    }

// Scan file for //TODO-LLM markers and return "lineNumber: context"
    private java.util.List<String> scanTodos(File file) {
        java.util.List<String> todos = new java.util.ArrayList<>();
        try {
            java.util.List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).contains("//TODO-LLM")) {
                    todos.add((i + 1) + ": " + lines.get(i).trim());
                }
            }
        } catch (java.io.IOException ignored) {
        }
        return todos;
    }

    private void loadComponents() {
        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Search files...");

        JButton clearBtn = new JButton("✕");
        clearBtn.setPreferredSize(new Dimension(40, searchField.getPreferredSize().height));
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            tree.setModel(treeModel);   // restore full tree
        });

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(clearBtn, BorderLayout.EAST);

// Live search on each keystroke
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterTree();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterTree();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterTree();
            }
        });

        root = new DefaultMutableTreeNode();

        tree = new JTree(root);
        tree.setCellRenderer(new FileTreeCellRenderer());
        tree.setRowHeight(20);
        JScrollPane treeScroll = new JScrollPane(tree);

        // Left panel: search bar on top, tree below
        // RSyntaxTextArea
        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));

        // Autocomplete
        DefaultCompletionProvider provider = createCompletionProvider();
        AutoCompletion ac = new AutoCompletion(provider);
        ac.setAutoActivationEnabled(true);        // triggers automatically while typing
        ac.setAutoActivationDelay(300);           // ms delay before popup appears
        ac.setShowDescWindow(true);               // shows description panel on the side
        ac.setParameterAssistanceEnabled(true);   // helps fill in method parameters
        ac.install(textArea);

        editorTabs = new JTabbedPane();
        editorTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        editorTabs.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getButton() == java.awt.event.MouseEvent.BUTTON2) {  // middle click
                    int idx = editorTabs.indexAtLocation(e.getX(), e.getY());
                    if (idx != -1) {
                        openFileTabs.entrySet().stream()
                                .filter(entry -> entry.getValue() == idx)
                                .findFirst()
                                .ifPresent(entry -> closeTab(entry.getKey()));
                    }
                }
            }
        });

        JediTermWidget terminal = createTerminal();

        // Tab pane so you can add more terminals later
        JTabbedPane terminalTabs = new JTabbedPane();
        terminalTabs.addTab("Terminal", terminal);
        terminalTabs.setPreferredSize(new Dimension(0, 220));
// Add a permanent "+" tab at the end
        terminalTabs.addTab("+", null);
        int plusTabIndex = terminalTabs.getTabCount() - 1;

        terminalTabs.addChangeListener(e -> {
            int selected = terminalTabs.getSelectedIndex();
            if (selected == terminalTabs.getTabCount() - 1) {

                // Snap back immediately so UI doesn't freeze on "+" tab
                int prev = selected - 1;
                if (prev >= 0) {
                    terminalTabs.setSelectedIndex(prev);
                }

                // Create terminal in background thread
                new javax.swing.SwingWorker<JediTermWidget, Void>() {
                    @Override
                    protected JediTermWidget doInBackground() {
                        return createTerminal();  // runs off EDT
                    }

                    @Override
                    protected void done() {
                        try {
                            JediTermWidget newTerm = get();
                            int insertAt = terminalTabs.getTabCount() - 1; // before "+"
                            terminalTabs.insertTab("Terminal " + insertAt, null, newTerm, null, insertAt);
                            terminalTabs.setSelectedIndex(insertAt);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null,
                                    "Could not start terminal: " + ex.getMessage(),
                                    "Terminal Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            }
        });

        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof File) {
                    setText(((File) value).getName());
                    setToolTipText(((File) value).getAbsolutePath());
                }
                return this;
            }
        });

// Right-click on list -> delete
        JPopupMenu listPopup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            int index = fileList.getSelectedIndex();
            if (index != -1) {
                listModel.remove(index);
            }
        });
        listPopup.add(deleteItem);

        fileList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                maybeShowListPopup(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                maybeShowListPopup(e);
            }

            private void maybeShowListPopup(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = fileList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        fileList.setSelectedIndex(index);
                        listPopup.show(fileList, e.getX(), e.getY());
                    }
                }
            }
        });

// Double-click list item to open it in editor
        fileList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    File selected = fileList.getSelectedValue();
                    if (selected != null) {
                        loadFileContent(selected);
                    }
                }
            }
        });

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("File List for LLM Context"));
        rightPanel.setPreferredSize(new Dimension(180, 0));
        rightPanel.add(new JScrollPane(fileList), BorderLayout.CENTER);

        JPopupMenu treePopup = new JPopupMenu();
        JMenuItem addToListItem = new JMenuItem("Add to List");
        addToListItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof File) {
                File file = (File) node.getUserObject();
                if (!file.isDirectory() && !listModel.contains(file)) {
                    listModel.addElement(file);
                }
            }
        });
        treePopup.add(addToListItem);

        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                maybeShowTreePopup(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                maybeShowTreePopup(e);
            }

            private void maybeShowTreePopup(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        tree.setSelectionPath(path);   // select node under cursor
                        DefaultMutableTreeNode node
                                = (DefaultMutableTreeNode) path.getLastPathComponent();
                        // Only show popup for files, not folders
                        if (node.getUserObject() instanceof File
                                && !((File) node.getUserObject()).isDirectory()) {
                            treePopup.show(tree, e.getX(), e.getY());
                        }
                    }
                }
            }
        });
        JPanel findBar = createFindBar();

// Wrap editorTabs + findBar in a panel
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(editorTabs, BorderLayout.CENTER);
        editorPanel.add(findBar, BorderLayout.SOUTH);

        KeyStroke ctrlF = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());

        editorPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlF, "showFind");
        editorPanel.getActionMap().put("showFind", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                findBar.setVisible(!findBar.isVisible());
                if (findBar.isVisible()) {
                    // Focus the find field when opened
                    java.awt.Component[] comps = findBar.getComponents();
                    for (java.awt.Component c : comps) {
                        if (c instanceof JTextField) {
                            c.requestFocusInWindow();
                            break;
                        }
                    }
                }
            }
        });
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Project Files"));
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(treeScroll, BorderLayout.CENTER);
// Use editorPanel in the split pane instead of editorTabs directly
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, editorPanel);

        centerSplit.setDividerLocation(220);
        centerSplit.setOneTouchExpandable(true);
        centerSplit.setContinuousLayout(true);
        centerSplit.setResizeWeight(0.2);

// Right split: (tree + editor) | file list
        JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerSplit, rightPanel);
        rightSplit.setOneTouchExpandable(true);
        rightSplit.setContinuousLayout(true);
        rightSplit.setResizeWeight(0.85);  // editor gets most space

// Outer split: top | terminal
        JSplitPane outerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightSplit, terminalTabs);
        outerSplit.setDividerLocation(400);
        outerSplit.setOneTouchExpandable(true);
        outerSplit.setContinuousLayout(true);
        outerSplit.setResizeWeight(0.75);
        java.awt.Color white = java.awt.Color.WHITE;

        jPanel1.setBackground(white);
        leftPanel.setBackground(white);
        searchPanel.setBackground(white);
        editorPanel.setBackground(white);
        rightPanel.setBackground(white);
        promptJPanel.setBackground(white);
        ddlJPanel.setBackground(white);
        terminalTabs.setBackground(white);
        editorTabs.setBackground(white);
        tree.setBackground(white);
        fileList.setBackground(white);
        workbenchJPanel.setLayout(new BorderLayout());
        workbenchJPanel.add(outerSplit, BorderLayout.CENTER);

        workbenchJPanel.revalidate();
        workbenchJPanel.repaint();

    }

    private static class FileTreeCellRenderer extends DefaultTreeCellRenderer {

        // Folder colors
        private static final Color FOLDER_COLOR = new Color(0xDCB67A);
        private static final Color FOLDER_OPEN_COLOR = new Color(0xDCB67A);

        // File type colors
        private static final Color JAVA_COLOR = new Color(0xF89820);
        private static final Color WEB_COLOR = new Color(0x61AFEF);
        private static final Color DATA_COLOR = new Color(0x98C379);
        private static final Color IMAGE_COLOR = new Color(0xC678DD);
        private static final Color TEXT_COLOR = new Color(0xABB2BF);
        private static final Color DEFAULT_COLOR = new Color(0xABB2BF);

        @Override
        public java.awt.Component getTreeCellRendererComponent(
                javax.swing.JTree tree, Object value, boolean selected,
                boolean expanded, boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            if (!(value instanceof DefaultMutableTreeNode)) {
                return this;
            }
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObj = node.getUserObject();

            if (!(userObj instanceof java.io.File)) {

                return this;
            }

            java.io.File file = (java.io.File) userObj;
            setText(file.getName());

            if (file.isDirectory()) {

            }

            return this;
        }

        private String getExtension(String name) {
            int dot = name.lastIndexOf('.');
            return dot != -1 ? name.substring(dot + 1).toLowerCase() : "";
        }

    }

    private File currentFile;   // add this as a class field

    private void saveCurrentFile() {
        int idx = editorTabs.getSelectedIndex();
        if (idx == -1) {
            JOptionPane.showMessageDialog(this, "No file open.", "Save", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get current file from active tab index
        File file = openFileTabs.entrySet().stream()
                .filter(e -> e.getValue() == idx)
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (file == null) {
            saveAs();
            return;
        }

        // Confirmation dialog
        int choice = JOptionPane.showConfirmDialog(this,
                "Save changes to " + file.getName() + "?",
                "Save File",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        // Get the editor from the active tab directly
        RTextScrollPane scroll = (RTextScrollPane) editorTabs.getComponentAt(idx);
        RSyntaxTextArea activeEditor = (RSyntaxTextArea) scroll.getViewport().getView();

        saveToFile(file, activeEditor.getText());
    }

    private void saveAs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save File");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            int idx = editorTabs.getSelectedIndex();
            if (idx != -1) {
                RTextScrollPane scroll = (RTextScrollPane) editorTabs.getComponentAt(idx);
                RSyntaxTextArea activeEditor = (RSyntaxTextArea) scroll.getViewport().getView();
                saveToFile(file, activeEditor.getText());
            }
        }
    }

    private void saveToFile(File file, String content) {
        try {
            java.nio.file.Files.writeString(file.toPath(), content);

            // Clear the modified marker (* ) from the tab header
            int idx = editorTabs.getSelectedIndex();
            if (idx != -1) {
                JPanel header = (JPanel) editorTabs.getTabComponentAt(idx);
                if (header != null) {
                    JLabel label = (JLabel) header.getComponent(0);
                    label.setText(file.getName());  // remove * prefix
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Saved: " + file.getName(), "Saved", JOptionPane.INFORMATION_MESSAGE);

        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private DefaultCompletionProvider createCompletionProvider() {
        DefaultCompletionProvider provider = new DefaultCompletionProvider();

        // --- Java keywords ---
        String[] keywords = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch",
            "char", "class", "const", "continue", "default", "do", "double",
            "else", "enum", "extends", "final", "finally", "float", "for",
            "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private",
            "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while"
        };
        for (String kw : keywords) {
            provider.addCompletion(new BasicCompletion(provider, kw));
        }

        // --- Shorthands (type shortcut → expands to full snippet) ---
        provider.addCompletion(new ShorthandCompletion(provider, "sout",
                "System.out.println();", "System.out.println()"));

        provider.addCompletion(new ShorthandCompletion(provider, "serr",
                "System.err.println();", "System.err.println()"));

        provider.addCompletion(new ShorthandCompletion(provider, "psvm",
                "public static void main(String[] args) {\n\t\n}", "main method"));

        provider.addCompletion(new ShorthandCompletion(provider, "fori",
                "for (int i = 0; i < ; i++) {\n\t\n}", "for loop"));

        provider.addCompletion(new ShorthandCompletion(provider, "foreach",
                "for ( : ) {\n\t\n}", "for-each loop"));

        provider.addCompletion(new ShorthandCompletion(provider, "trycatch",
                "try {\n\t\n} catch (Exception e) {\n\te.printStackTrace();\n}", "try-catch block"));

        // --- Common class names ---
        String[] classes = {
            "String", "Integer", "Double", "Boolean", "List", "ArrayList",
            "HashMap", "HashSet", "Map", "Set", "Optional", "StringBuilder",
            "Thread", "Runnable", "Exception", "RuntimeException", "Object"
        };
        for (String cls : classes) {
            provider.addCompletion(new BasicCompletion(provider, cls));
        }

        return provider;
    }

    private void loadDirectoryTree(File folder) {
        // Create root node
        root = new DefaultMutableTreeNode(folder);
        projectRootFolder = folder;

        // Add children; if a child is a folder, add a dummy node so it shows the expand arrow
        addChildren(root, folder);

        treeModel = new DefaultTreeModel(root);

        tree.setModel(treeModel);
        tree.setCellRenderer(new FileTreeCellRenderer());
        // Force full repaint of the tree
        tree.revalidate();
        tree.repaint();

        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path == null) {
                        return;
                    }

                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (!(node.getUserObject() instanceof File)) {
                        return;
                    }

                    File file = (File) node.getUserObject();
                    if (file.isDirectory()) {
                        return;  // ignore folders
                    }
                    loadFileContent(file);
                }
            }
        });

        // Lazy loading: when a node is expanded, load its children on demand
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                TreePath path = event.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

                // If the only child is the dummy placeholder, replace it with real children
                if (node.getChildCount() == 1) {
                    DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode) node.getFirstChild();
                    if (firstChild.getUserObject().equals("Loading...")) {
                        node.removeAllChildren();
                        File dir = (File) node.getUserObject();
                        addChildren(node, dir);
                        treeModel.nodeStructureChanged(node);
                    }
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {
            }
        });

        // Show file/folder names instead of full paths in the tree
        tree.setCellRenderer((javax.swing.tree.TreeCellRenderer) new DefaultTreeCellRenderer() {
            @Override
            public java.awt.Component getTreeCellRendererComponent(JTree tree, Object value,
                    boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                if (node.getUserObject() instanceof File) {
                    File f = (File) node.getUserObject();
                    setText(f.getName());
                    if (f.isDirectory()) {
                        setIcon(expanded ? getOpenIcon() : getClosedIcon());
                    } else {
                        setIcon(getLeafIcon());
                    }
                }
                return this;
            }
        });
        setTitle("LLM Workbench v0.2 - " + folder.getName());  // add this

    }

    private void addChildren(DefaultMutableTreeNode node, File folder) {
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        // Sort: folders first, then files, both alphabetically
        java.util.Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) {
                return -1;
            }
            if (!a.isDirectory() && b.isDirectory()) {
                return 1;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });

        for (File file : files) {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(file);
            node.add(child);
            // Add a dummy "Loading..." child to folders so they show the expand arrow
            if (file.isDirectory()) {
                child.add(new DefaultMutableTreeNode("Loading..."));
            }
        }
    }

    private void openProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File folder = chooser.getSelectedFile();
        loadDirectoryTree(folder);

        // CD into the project folder in the terminal
        if (terminalPanel instanceof JediTermWidget) {
            JediTermWidget t = (JediTermWidget) terminalPanel;
            t.getTerminalStarter().sendString("cd \"" + folder.getAbsolutePath() + "\"\n", false);
        }
    }

    private void loadFileContent(File file) {
        if (openFileTabs.containsKey(file)) {
            editorTabs.setSelectedIndex(openFileTabs.get(file));
            return;
        }

        try {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));

            // Create editor for this file
            RSyntaxTextArea fileTextArea = new RSyntaxTextArea(20, 80);
            fileTextArea.setSyntaxEditingStyle(getSyntaxStyle(getFileExtension(file.getName())));
            fileTextArea.setCodeFoldingEnabled(true);
            fileTextArea.setAntiAliasingEnabled(true);
            fileTextArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
            fileTextArea.setText(content);
            fileTextArea.setCaretPosition(0);

            // Autocomplete
            DefaultCompletionProvider provider = createCompletionProvider();
            AutoCompletion ac = new AutoCompletion(provider);
            ac.setAutoActivationEnabled(true);
            ac.setAutoActivationDelay(300);
            ac.setShowDescWindow(true);
            ac.install(fileTextArea);

            RTextScrollPane scrollPane = new RTextScrollPane(fileTextArea);
            scrollPane.setLineNumbersEnabled(true);

            // Add tab with custom close button
            int tabIndex = editorTabs.getTabCount();
            editorTabs.addTab(file.getName(), scrollPane);
            editorTabs.setTabComponentAt(tabIndex, createTabHeader(file, fileTextArea));
            editorTabs.setSelectedIndex(tabIndex);

            // Track open file
            openFileTabs.put(file, tabIndex);

            // Update currentFile when tab is switched
            editorTabs.addChangeListener(e -> {
                int idx = editorTabs.getSelectedIndex();
                if (idx != -1) {
                    // Find which file matches this tab index
                    openFileTabs.entrySet().stream()
                            .filter(entry -> entry.getValue() == idx)
                            .findFirst()
                            .ifPresent(entry -> {
                                currentFile = entry.getKey();
                                textArea = fileTextArea;
                            });
                }
            });

            currentFile = file;
            textArea = fileTextArea;

        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not read file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private java.awt.Component createTabHeader(File file, RSyntaxTextArea fileTextArea) {
        JPanel header = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
        header.setOpaque(false);

        JLabel titleLabel = new JLabel(file.getName());
        titleLabel.setToolTipText(file.getAbsolutePath());

        // Mark tab as modified with * when text changes
        fileTextArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void markModified() {
                if (!titleLabel.getText().startsWith("* ")) {
                    titleLabel.setText("* " + file.getName());
                }
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                markModified();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                markModified();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                markModified();
            }
        });

        // Close button
        JButton closeBtn = new JButton("✕");
        closeBtn.setPreferredSize(new java.awt.Dimension(16, 16));
        closeBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 9));
        closeBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        closeBtn.setFocusable(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setToolTipText("Close");

        closeBtn.addActionListener(e -> closeTab(file));

        // Hover effect on close button
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeBtn.setContentAreaFilled(true);
                closeBtn.setForeground(java.awt.Color.RED);
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                closeBtn.setContentAreaFilled(false);
                closeBtn.setForeground(null);
            }
        });

        header.add(titleLabel);
        header.add(closeBtn);
        return header;
    }

    private void closeTab(File file) {
        Integer tabIndex = openFileTabs.get(file);
        if (tabIndex == null) {
            return;
        }

        // Check for unsaved changes (tab title starts with *)
        JPanel header = (JPanel) editorTabs.getTabComponentAt(tabIndex);
        JLabel label = (JLabel) header.getComponent(0);
        if (label.getText().startsWith("* ")) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Save changes to " + file.getName() + "?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                RTextScrollPane scroll = (RTextScrollPane) editorTabs.getComponentAt(tabIndex);
                RSyntaxTextArea editor = (RSyntaxTextArea) scroll.getViewport().getView();
                saveToFile(file, editor.getText());
            } else if (choice == JOptionPane.CANCEL_OPTION) {
                return;  // abort close
            }
        }

        editorTabs.removeTabAt(tabIndex);
        openFileTabs.remove(file);

        // Re-index remaining tabs after removal
        openFileTabs.replaceAll((f, idx) -> idx > tabIndex ? idx - 1 : idx);

        // Update currentFile to newly selected tab
        int newIndex = editorTabs.getSelectedIndex();
        if (newIndex != -1) {
            openFileTabs.entrySet().stream()
                    .filter(e -> e.getValue() == newIndex)
                    .findFirst()
                    .ifPresent(e -> currentFile = e.getKey());
        } else {
            currentFile = null;
            textArea = null;
        }
    }

    private String getFileExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot != -1) ? fileName.substring(dot + 1).toLowerCase() : "";
    }

    private void filterTree() {
        String query = searchField.getText().trim().toLowerCase();

        // Cancel any running search
        if (searchWorker != null && !searchWorker.isDone()) {
            searchWorker.cancel(true);
        }

        // Empty query — restore original tree
        if (query.isEmpty()) {
            tree.setModel(treeModel);
            tree.setCellRenderer(new FileTreeCellRenderer());
            return;
        }

        if (projectRootFolder == null) {
            return;
        }

        // Build fresh search tree
        DefaultMutableTreeNode searchRoot = new DefaultMutableTreeNode(projectRootFolder);
        DefaultTreeModel searchModel = new DefaultTreeModel(searchRoot);
        tree.setModel(searchModel);
        tree.setCellRenderer(new FileTreeCellRenderer());

        // Show "Searching..." placeholder
        searchRoot.add(new DefaultMutableTreeNode("Searching..."));
        searchModel.nodeStructureChanged(searchRoot);

        // Delegate to worker
        searchWorker = new FileSearchWorker(projectRootFolder, query, searchRoot, searchModel, tree);
        searchWorker.execute();
    }

// Recursively copies matching nodes into filteredParent
    private boolean filterNode(DefaultMutableTreeNode source, DefaultMutableTreeNode filteredParent, String query) {
        boolean hasMatch = false;

        for (int i = 0; i < source.getChildCount(); i++) {
            DefaultMutableTreeNode sourceChild = (DefaultMutableTreeNode) source.getChildAt(i);
            Object userObj = sourceChild.getUserObject();

            // Skip dummy "Loading..." nodes
            if (!(userObj instanceof File)) {
                continue;
            }

            File file = (File) userObj;
            String name = file.getName().toLowerCase();

            DefaultMutableTreeNode filteredChild = new DefaultMutableTreeNode(userObj);

            if (file.isDirectory()) {
                // Recurse into directory
                boolean childMatch = filterNode(sourceChild, filteredChild, query);
                if (childMatch || name.contains(query)) {
                    filteredParent.add(filteredChild);
                    hasMatch = true;
                }
            } else {
                // It's a file — include if name matches
                if (name.contains(query)) {
                    filteredParent.add(filteredChild);
                    hasMatch = true;
                }
            }
        }
        return hasMatch;
    }

    private void expandAll(JTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    private void setupKeyBindings(JComponent component) {
        // Define an Action for moving left
        // Get the InputMap for "WHEN_IN_FOCUSED_WINDOW"
        Action openInventory = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };

        Action openSuppliers = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };

        Action openCustomers = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };

        Action openEmployees = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };

        Action openPosBill = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };

        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        // Get the ActionMap
        ActionMap actionMap = component.getActionMap();

        // Bind the 'A' key to the "moveLeft" command
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "openInventory");
        actionMap.put("openInventory", openInventory);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "openSuppliers");
        actionMap.put("openSuppliers", openSuppliers);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "openCustomers");
        actionMap.put("openCustomers", openCustomers);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "openEmployees");
        actionMap.put("openEmployees", openEmployees);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "openPosBill");
        actionMap.put("openPosBill", openPosBill);

    }

    private JediTermWidget createTerminal() {
        JediTermWidget terminal = new JediTermWidget(new DefaultSettingsProvider() {
            @Override
            public float getTerminalFontSize() {
                return 13f;
            }

            @Override
            public java.awt.Font getTerminalFont() {
                // Try native monospace fonts in order of preference per OS
                String os = System.getProperty("os.name").toLowerCase();
                String[] candidates;

                if (os.contains("win")) {
                    candidates = new String[]{"Cascadia Code", "Cascadia Mono", "Consolas", "Lucida Console", "Courier New"};
                } else if (os.contains("mac")) {
                    candidates = new String[]{"SF Mono", "Menlo", "Monaco", "Courier New"};
                } else {
                    candidates = new String[]{"Ubuntu Mono", "DejaVu Sans Mono", "Liberation Mono", "Monospace", "Courier New"};
                }

                // Pick the first font that is actually installed
                java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
                java.util.Set<String> available = new java.util.HashSet<>(
                        java.util.Arrays.asList(ge.getAvailableFontFamilyNames()));

                for (String name : candidates) {
                    if (available.contains(name)) {
                        return new java.awt.Font(name, java.awt.Font.PLAIN, 14);
                    }
                }

                // Fallback — Java's built-in monospaced
                return new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 14);
            }
        });

        try {
            String os = System.getProperty("os.name").toLowerCase();
            String[] command = os.contains("win")
                    ? new String[]{"cmd.exe"}
                    : os.contains("mac")
                    ? new String[]{"/bin/zsh"}
                    : new String[]{"/bin/bash"};

            java.util.Map<String, String> env = new java.util.HashMap<>(System.getenv());
            env.put("TERM", "xterm-256color");

            PtyProcess process = new PtyProcessBuilder()
                    .setCommand(command)
                    .setEnvironment(env)
                    .setInitialColumns(200)
                    .setInitialRows(20)
                    .start();

            // Build TtyConnector manually — no com.jediterm.pty needed
            TtyConnector connector = new TtyConnector() {
                private final BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                private final OutputStream writer = process.getOutputStream();

                @Override
                public boolean init(com.jediterm.terminal.Questioner q) {
                    return true;
                }

                @Override
                public void close() {
                    process.destroy();
                }

                @Override
                public String getName() {
                    return "Local Terminal";
                }

                @Override
                public boolean ready() throws IOException {
                    return reader.ready();
                }

                @Override
                public int read(char[] buf, int offset, int length) throws IOException {
                    int ch = reader.read();
                    if (ch == -1) {
                        return -1;
                    }
                    buf[offset] = (char) ch;
                    int count = 1;
                    while (count < length && reader.ready()) {
                        ch = reader.read();
                        if (ch == -1) {
                            break;
                        }
                        buf[offset + count++] = (char) ch;
                    }
                    return count;
                }

                @Override
                public void write(byte[] bytes) throws IOException {
                    writer.write(bytes);
                    writer.flush();
                }

                @Override
                public void write(String string) throws IOException {
                    writer.write(string.getBytes(StandardCharsets.UTF_8));
                    writer.flush();
                }

                @Override
                public boolean isConnected() {
                    return process.isAlive();
                }

                @Override
                public void resize(com.jediterm.core.util.TermSize termSize) {
                    process.setWinSize(new com.pty4j.WinSize(
                            termSize.getColumns(), termSize.getRows()));
                }

                @Override
                public int waitFor() throws InterruptedException {
                    try {
                        return process.waitFor();
                    } catch (Exception e) {
                        return -1;
                    }
                }
            };

            terminal.createTerminalSession(connector);
            terminal.start();

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Could not start terminal: " + ex.getMessage(),
                    "Terminal Error", JOptionPane.ERROR_MESSAGE);
        }

        return terminal;
    }

    private void newProject() {
        // Warn if there is anything open
        boolean hasContent = (root != null)
                || !listModel.isEmpty()
                || (ddlArea != null && !ddlArea.getText().isBlank())
                || (promptArea != null && !promptArea.getText().isBlank())
                || editorTabs.getTabCount() > 0;

        if (hasContent) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Start a new project? All unsaved changes will be lost.",
                    "New Project",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Clear tree
        root = null;
        treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("No project open"));
        tree = new JTree(treeModel);
        tree.setCellRenderer(new FileTreeCellRenderer());
        tree.setRowHeight(22);
        tree.setShowsRootHandles(true);
        tree.revalidate();
        tree.repaint();
        // Clear file list
        listModel.clear();

        // Close all editor tabs
        editorTabs.removeAll();
        openFileTabs.clear();
        currentFile = null;
        textArea = null;

        // Clear DDL
        if (ddlArea != null) {
            ddlArea.setText("-- Paste your DDL statements here\n-- e.g. CREATE TABLE, ALTER TABLE...\n");
        }

        // Clear prompt
        if (promptArea != null) {
            promptArea.setText("");
        }

        // Reset verbose checkbox
        if (verboseCheckBox != null) {
            verboseCheckBox.setSelected(false);
        }

        setTitle("LLM Workbench v0.2 - New Project");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jPanel2 = new javax.swing.JPanel();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        LLMWorkBenchJTabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        workbenchJPanel = new javax.swing.JPanel();
        ddlJPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        promptJPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jToolBar2 = new javax.swing.JToolBar();
        jToolBar3 = new javax.swing.JToolBar();
        welcomeJLabel1 = new javax.swing.JLabel();
        welcomeJLabel2 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        masterJMenu = new javax.swing.JMenu();
        newProjectJMenuItem = new javax.swing.JMenuItem();
        saveProjectJMenuItem = new javax.swing.JMenuItem();
        loadProjectJMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem openProjectJMenuItem = new javax.swing.JMenuItem();
        saveFileJMenuItem = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenuItem17 = new javax.swing.JMenuItem();
        jMenuItem18 = new javax.swing.JMenuItem();

        jMenuItem1.setText("jMenuItem1");

        jMenuItem4.setText("jMenuItem4");

        jMenuItem7.setText("jMenuItem7");

        jMenu1.setText("File");
        jMenuBar2.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar2.add(jMenu2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("LLM Workbench v0.2");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(51, 153, 255));
        jPanel2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPanel2KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jPanel2KeyReleased(evt);
            }
        });

        jDesktopPane1.setBackground(new java.awt.Color(255, 255, 255));
        jDesktopPane1.setAutoscrolls(true);
        jDesktopPane1.setDoubleBuffered(true);
        jDesktopPane1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jDesktopPane1KeyReleased(evt);
            }
        });

        LLMWorkBenchJTabbedPane.setBackground(new java.awt.Color(255, 255, 255));
        LLMWorkBenchJTabbedPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        LLMWorkBenchJTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        LLMWorkBenchJTabbedPane.setMinimumSize(new java.awt.Dimension(140, 95));

        workbenchJPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout workbenchJPanelLayout = new javax.swing.GroupLayout(workbenchJPanel);
        workbenchJPanel.setLayout(workbenchJPanelLayout);
        workbenchJPanelLayout.setHorizontalGroup(
            workbenchJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 895, Short.MAX_VALUE)
        );
        workbenchJPanelLayout.setVerticalGroup(
            workbenchJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 580, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(workbenchJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(workbenchJPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        LLMWorkBenchJTabbedPane.addTab("Workbench", jPanel1);

        ddlJPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout ddlJPanelLayout = new javax.swing.GroupLayout(ddlJPanel);
        ddlJPanel.setLayout(ddlJPanelLayout);
        ddlJPanelLayout.setHorizontalGroup(
            ddlJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 895, Short.MAX_VALUE)
        );
        ddlJPanelLayout.setVerticalGroup(
            ddlJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 580, Short.MAX_VALUE)
        );

        LLMWorkBenchJTabbedPane.addTab("DDL Workbench", ddlJPanel);

        promptJPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout promptJPanelLayout = new javax.swing.GroupLayout(promptJPanel);
        promptJPanel.setLayout(promptJPanelLayout);
        promptJPanelLayout.setHorizontalGroup(
            promptJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 895, Short.MAX_VALUE)
        );
        promptJPanelLayout.setVerticalGroup(
            promptJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 580, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(promptJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(promptJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        LLMWorkBenchJTabbedPane.addTab("Prompt Enginnering", jPanel3);

        jToolBar1.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar1.setBorder(null);
        jToolBar1.setRollover(true);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/add-document-new16.png"))); // NOI18N
        jButton1.setText("New Project");
        jButton1.setToolTipText("New Project");
        jButton1.setFocusable(false);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/diskette16.png"))); // NOI18N
        jButton2.setText("Save Project");
        jButton2.setToolTipText("Save Project");
        jButton2.setFocusable(false);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/open-folder16.png"))); // NOI18N
        jButton3.setText("Load Project");
        jButton3.setToolTipText("Load Project");
        jButton3.setFocusable(false);
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton3);

        jLabel1.setText("    |    ");
        jToolBar1.add(jLabel1);

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/diskette16.png"))); // NOI18N
        jButton5.setText("Save File");
        jButton5.setToolTipText("Save file");
        jButton5.setFocusable(false);
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton5);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/open-folder16.png"))); // NOI18N
        jButton4.setText("Open Folder");
        jButton4.setToolTipText("Open Folder");
        jButton4.setFocusable(false);
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton4);

        jToolBar2.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar2.setRollover(true);

        jToolBar3.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar3.setRollover(true);

        welcomeJLabel1.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        welcomeJLabel1.setText("LLM Workbench v0.2");
        jToolBar3.add(welcomeJLabel1);

        welcomeJLabel2.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        welcomeJLabel2.setForeground(new java.awt.Color(255, 51, 51));
        welcomeJLabel2.setText("  - Research Preview");
        jToolBar3.add(welcomeJLabel2);

        jDesktopPane1.setLayer(LLMWorkBenchJTabbedPane, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jDesktopPane1.setLayer(jToolBar1, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jDesktopPane1.setLayer(jToolBar2, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jDesktopPane1.setLayer(jToolBar3, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jDesktopPane1Layout = new javax.swing.GroupLayout(jDesktopPane1);
        jDesktopPane1.setLayout(jDesktopPane1Layout);
        jDesktopPane1Layout.setHorizontalGroup(
            jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDesktopPane1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDesktopPane1Layout.createSequentialGroup()
                        .addGroup(jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jToolBar3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(LLMWorkBenchJTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jDesktopPane1Layout.setVerticalGroup(
            jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDesktopPane1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jDesktopPane1Layout.createSequentialGroup()
                        .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(LLMWorkBenchJTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jDesktopPane1)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jDesktopPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        masterJMenu.setText("File");

        newProjectJMenuItem.setText("New Project");
        newProjectJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newProjectJMenuItemActionPerformed(evt);
            }
        });
        masterJMenu.add(newProjectJMenuItem);

        saveProjectJMenuItem.setText("Save Project");
        saveProjectJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveProjectJMenuItemActionPerformed(evt);
            }
        });
        masterJMenu.add(saveProjectJMenuItem);

        loadProjectJMenuItem.setText("Load Project");
        loadProjectJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadProjectJMenuItemActionPerformed(evt);
            }
        });
        masterJMenu.add(loadProjectJMenuItem);

        openProjectJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        openProjectJMenuItem.setText("Open Folder");
        openProjectJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openProjectJMenuItemActionPerformed(evt);
            }
        });
        masterJMenu.add(openProjectJMenuItem);

        saveFileJMenuItem.setText("Save file");
        saveFileJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileJMenuItemActionPerformed(evt);
            }
        });
        masterJMenu.add(saveFileJMenuItem);

        jMenuBar1.add(masterJMenu);

        jMenu5.setText("Help");

        jMenuItem17.setText("License");
        jMenu5.add(jMenuItem17);

        jMenuItem18.setText("About");
        jMenu5.add(jMenuItem18);

        jMenuBar1.add(jMenu5);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:

    }//GEN-LAST:event_formWindowOpened

    private void openProjectJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openProjectJMenuItemActionPerformed
        // TODO add your handling code here:
        openProject();

    }//GEN-LAST:event_openProjectJMenuItemActionPerformed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        // TODO add your handling code here:

    }//GEN-LAST:event_formKeyPressed

    private void jPanel2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanel2KeyPressed
        // TODO add your handling code here:

    }//GEN-LAST:event_jPanel2KeyPressed

    private void jPanel2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanel2KeyReleased
        // TODO add your handling code here:


    }//GEN-LAST:event_jPanel2KeyReleased

    private void jDesktopPane1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jDesktopPane1KeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_jDesktopPane1KeyReleased

    private void saveFileJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileJMenuItemActionPerformed
        // TODO add your handling code here:
        saveCurrentFile();
    }//GEN-LAST:event_saveFileJMenuItemActionPerformed

    private void saveProjectJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveProjectJMenuItemActionPerformed
        // TODO add your handling code here:
        saveProject();
    }//GEN-LAST:event_saveProjectJMenuItemActionPerformed

    private void loadProjectJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadProjectJMenuItemActionPerformed
        // TODO add your handling code here:
        loadProject();
    }//GEN-LAST:event_loadProjectJMenuItemActionPerformed

    private void newProjectJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newProjectJMenuItemActionPerformed
        // TODO add your handling code here:
        newProject();
    }//GEN-LAST:event_newProjectJMenuItemActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        newProject();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        saveProject();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        loadProject();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        saveCurrentFile();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        openProject();
    }//GEN-LAST:event_jButton4ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new DashboardForm().setVisible(true));

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane LLMWorkBenchJTabbedPane;
    private javax.swing.JPanel ddlJPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem17;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JMenuItem loadProjectJMenuItem;
    private javax.swing.JMenu masterJMenu;
    private javax.swing.JMenuItem newProjectJMenuItem;
    private javax.swing.JPanel promptJPanel;
    private javax.swing.JMenuItem saveFileJMenuItem;
    private javax.swing.JMenuItem saveProjectJMenuItem;
    private javax.swing.JLabel welcomeJLabel1;
    private javax.swing.JLabel welcomeJLabel2;
    private javax.swing.JPanel workbenchJPanel;
    // End of variables declaration//GEN-END:variables
}
