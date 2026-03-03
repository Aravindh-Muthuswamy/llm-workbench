/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.utils;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 *
 * @author aravindhmuthuswamy
 */
public class CodeUtils {
    public static String getSyntaxStyle(String ext) {
        switch (ext) {
            case "java":
                return SyntaxConstants.SYNTAX_STYLE_JAVA;
            case "py":
                return SyntaxConstants.SYNTAX_STYLE_PYTHON;
            case "js":
                return SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
            case "ts":
                return SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT;
            case "tsx":
                return SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT;
            case "html":
                return SyntaxConstants.SYNTAX_STYLE_HTML;
            case "xml":
                return SyntaxConstants.SYNTAX_STYLE_XML;
            case "json":
                return SyntaxConstants.SYNTAX_STYLE_JSON;
            case "sql":
                return SyntaxConstants.SYNTAX_STYLE_SQL;
            case "css":
                return SyntaxConstants.SYNTAX_STYLE_CSS;
            case "c":
            case "h":
                return SyntaxConstants.SYNTAX_STYLE_C;
            case "cpp":
                return SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
            case "cs":
                return SyntaxConstants.SYNTAX_STYLE_CSHARP;
            case "php":
                return SyntaxConstants.SYNTAX_STYLE_PHP;
            case "rb":
                return SyntaxConstants.SYNTAX_STYLE_RUBY;
            case "sh":
            case "bash":
                return SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
            case "bat":
            case "cmd":
                return SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH;
            case "yaml":
            case "yml":
                return SyntaxConstants.SYNTAX_STYLE_YAML;
            case "md":
                return SyntaxConstants.SYNTAX_STYLE_MARKDOWN;
            case "properties":
                return SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
            default:
                return SyntaxConstants.SYNTAX_STYLE_NONE;
        }
    }
}
