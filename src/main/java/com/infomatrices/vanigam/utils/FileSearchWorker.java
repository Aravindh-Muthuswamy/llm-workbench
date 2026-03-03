/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.utils;

/**
 *
 * @author aravindhmuthuswamy
 */

import javax.swing.*;
import javax.swing.tree.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileSearchWorker extends SwingWorker<Void, DefaultMutableTreeNode> {

    private final File rootFolder;
    private final String query;
    private final DefaultMutableTreeNode searchRoot;
    private final DefaultTreeModel searchModel;
    private final JTree tree;

    public FileSearchWorker(File rootFolder, String query,
            DefaultMutableTreeNode searchRoot, DefaultTreeModel searchModel, JTree tree) {
        this.rootFolder  = rootFolder;
        this.query       = query.toLowerCase();
        this.searchRoot  = searchRoot;
        this.searchModel = searchModel;
        this.tree        = tree;
    }

    @Override
    protected Void doInBackground() {
        walkAndPublish(rootFolder);
        return null;
    }

    private void walkAndPublish(File dir) {
        if (isCancelled()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        for (File file : files) {
            if (isCancelled()) return;
            if (file.getName().toLowerCase().contains(query)) {
                publish(new DefaultMutableTreeNode(file));
            }
            if (file.isDirectory()) {
                walkAndPublish(file);
            }
        }
    }

    @Override
    protected void process(List<DefaultMutableTreeNode> chunks) {
        // Remove "Searching..." once first result arrives
        if (searchRoot.getChildCount() == 1) {
            DefaultMutableTreeNode first =
                (DefaultMutableTreeNode) searchRoot.getFirstChild();
            if ("Searching...".equals(first.getUserObject())) {
                searchRoot.remove(0);
            }
        }
        for (DefaultMutableTreeNode node : chunks) {
            searchRoot.add(node);
            searchModel.nodesWereInserted(searchRoot,
                new int[]{searchRoot.getIndex(node)});
        }
    }

    @Override
    protected void done() {
        if (isCancelled()) return;

        // Remove "Searching..." if still present (no results came via process)
        if (searchRoot.getChildCount() == 1) {
            DefaultMutableTreeNode first =
                (DefaultMutableTreeNode) searchRoot.getFirstChild();
            if ("Searching...".equals(first.getUserObject())) {
                searchRoot.remove(0);
                searchModel.nodeStructureChanged(searchRoot);
            }
        }

        // Show "No results" if tree is empty
        if (searchRoot.getChildCount() == 0) {
            DefaultMutableTreeNode none =
                new DefaultMutableTreeNode("No results for \"" + query + "\"");
            searchRoot.add(none);
            searchModel.nodeStructureChanged(searchRoot);
        }
    }
}