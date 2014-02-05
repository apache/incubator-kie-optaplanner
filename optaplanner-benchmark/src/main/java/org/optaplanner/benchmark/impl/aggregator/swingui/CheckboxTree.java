/*
 * Copyright 2014 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.optaplanner.benchmark.impl.aggregator.swingui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import static org.optaplanner.benchmark.impl.aggregator.swingui.MixedCheckBox.MixedCheckBoxStatus.*;

public class CheckboxTree extends JTree {

    public CheckboxTree(DefaultMutableTreeNode root) {
        super(root);
        addMouseListener(new CheckboxTreeMouseListener(this));
        setCellRenderer(new CheckboxTreeCellRenderer());
        setToggleClickCount(0);
    }

    private static class CheckboxTreeMouseListener extends MouseAdapter {

        private CheckboxTree tree;

        public CheckboxTreeMouseListener(CheckboxTree tree) {
            this.tree = tree;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            if (path != null) {
                DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                MixedCheckBox checkbox = (MixedCheckBox) currentNode.getUserObject();
                switch (checkbox.getStatus()) {
                    case CHECKED: {
                        checkbox.setStatus(UNCHECKED);
                        selectChildren(currentNode, UNCHECKED);
                        TreeNode[] ancestorNodes = currentNode.getPath();
                        // examine ancestors, don't lose track of most recent changes - bottom-up approach 
                        for (int i = ancestorNodes.length - 2; i >= 0; i--) {
                            DefaultMutableTreeNode ancestorNode = (DefaultMutableTreeNode) ancestorNodes[i];
                            MixedCheckBox ancestorCheckbox = (MixedCheckBox) ancestorNode.getUserObject();
                            if (checkChildren(ancestorNode, UNCHECKED)) {
                                ancestorCheckbox.setStatus(UNCHECKED);
                            } else {
                                ancestorCheckbox.setStatus(MIXED);
                            }
                        }
                        break;
                    }
                    case UNCHECKED: {
                        checkbox.setStatus(CHECKED);
                        selectChildren(currentNode, CHECKED);
                        TreeNode[] ancestorNodes = currentNode.getPath();
                        for (int i = ancestorNodes.length - 2; i >= 0; i--) {
                            DefaultMutableTreeNode ancestorNode = (DefaultMutableTreeNode) ancestorNodes[i];
                            MixedCheckBox ancestorCheckbox = (MixedCheckBox) ancestorNode.getUserObject();
                            if (checkChildren(ancestorNode, CHECKED)) {
                                ancestorCheckbox.setStatus(CHECKED);
                            } else {
                                ancestorCheckbox.setStatus(MIXED);
                            }
                        }
                        break;
                    }
                    case MIXED: {
                        checkbox.setStatus(CHECKED);
                        selectChildren(currentNode, CHECKED);
                        TreeNode[] ancestorNodes = currentNode.getPath();
                        for (int i = ancestorNodes.length - 2; i >= 0; i--) {
                            DefaultMutableTreeNode ancestorNode = (DefaultMutableTreeNode) ancestorNodes[i];
                            MixedCheckBox ancestorCheckbox = (MixedCheckBox) ancestorNode.getUserObject();
                            if (checkChildren(ancestorNode, CHECKED)) {
                                ancestorCheckbox.setStatus(CHECKED);
                            } else {
                                break;
                            }
                        }
                    }
                }
                tree.treeDidChange();
            }
        }

        private void selectChildren(DefaultMutableTreeNode parent, MixedCheckBox.MixedCheckBoxStatus status) {
            Enumeration children = parent.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
                MixedCheckBox checkbox = (MixedCheckBox) child.getUserObject();
                checkbox.setStatus(status);
                selectChildren(child, status);
            }
        }

        private boolean checkChildren(DefaultMutableTreeNode parent, MixedCheckBox.MixedCheckBoxStatus status) {
            boolean childrenCheck = true;
            Enumeration children = parent.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
                MixedCheckBox checkbox = (MixedCheckBox) child.getUserObject();
                if (checkbox.getStatus() != status) {
                    childrenCheck = false;
                    break;
                }
            }
            return childrenCheck;
        }
    }

    private static class CheckboxTreeCellRenderer implements TreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            MixedCheckBox checkbox = (MixedCheckBox) node.getUserObject();
            checkbox.setBackground(Color.WHITE);
            // TODO visual part
            return checkbox;
        }
        
    }
}
