/*
 * AddTagsDialog.java
 *
 * Level Tags Plugin for Sweet Home 3D
 * Copyright (c) 2024 Level Tags Plugin Contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.shlomoswidler.sh3d.leveltagsplugin;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Level;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Dialog for adding tags to a level.
 * Provides input for new tags and shows existing tags for quick selection.
 * 
 * @author Level Tags Plugin Contributors
 */
public class AddTagsDialog extends JDialog {
    
    private final Level level;
    private final LevelTagsManager tagsManager;
    private final Home home;
    private final ResourceBundle resources;
    
    // UI Components
    private JLabel levelLabel;
    private JLabel currentTagsLabel;
    private JTextField newTagsField;
    private JList<String> existingTagsList;
    private DefaultListModel<String> existingTagsModel;
    private JButton addButton;
    private JButton cancelButton;
    
    /**
     * Creates a new Add Tags dialog.
     * 
     * @param parent the parent dialog
     * @param level the level to add tags to
     * @param tagsManager the tags manager
     * @param home the home containing all levels
     * @param resources the resource bundle for localized strings
     */
    public AddTagsDialog(Dialog parent, Level level, LevelTagsManager tagsManager, 
                        Home home, ResourceBundle resources) {
        super(parent, true); // Modal dialog
        
        this.level = level;
        this.tagsManager = tagsManager;
        this.home = home;
        this.resources = resources;
        
        initializeDialog();
        createComponents();
        layoutComponents();
        setupEventHandlers();
        populateExistingTags();
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    /**
     * Initializes the dialog properties.
     */
    private void initializeDialog() {
        setTitle(resources.getString("addTagsDialog.title"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Add Esc key handling to cancel the dialog
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    /**
     * Creates all UI components.
     */
    private void createComponents() {
        // Level information
        levelLabel = new JLabel(resources.getString("addTagsDialog.level") + " " + level.getName());
        levelLabel.setFont(levelLabel.getFont().deriveFont(Font.BOLD));
        
        // Current tags display
        List<String> currentTags = tagsManager.getTagsForLevel(level);
        String currentTagsText = currentTags.isEmpty() ? 
            "(none)" : String.join(", ", currentTags);
        currentTagsLabel = new JLabel(resources.getString("addTagsDialog.currentTags") + " " + currentTagsText);
        
        // New tags input
        newTagsField = new JTextField(30);
        newTagsField.setToolTipText("Enter tags separated by commas");
        
        // Existing tags list
        existingTagsModel = new DefaultListModel<>();
        existingTagsList = new JList<>(existingTagsModel);
        existingTagsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        existingTagsList.setVisibleRowCount(8);
        existingTagsList.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // Buttons
        addButton = new JButton(resources.getString("addTagsDialog.add"));
        cancelButton = new JButton(resources.getString("addTagsDialog.cancel"));
        
        // Make add button default
        getRootPane().setDefaultButton(addButton);
    }
    
    /**
     * Lays out all components in the dialog.
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Level name
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 0);
        mainPanel.add(levelLabel, gbc);
        
        // Current tags
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(currentTagsLabel, gbc);
        
        // New tags label
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(new JLabel(resources.getString("addTagsDialog.newTags")), gbc);
        
        // New tags input field
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(newTagsField, gbc);
        
        // Existing tags label
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(new JLabel(resources.getString("addTagsDialog.existingTags")), gbc);
        
        // Existing tags list
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        JScrollPane scrollPane = new JScrollPane(existingTagsList);
        mainPanel.add(scrollPane, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(10, 15, 15, 15));
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Sets up event handlers for UI components.
     */
    private void setupEventHandlers() {
        // Add button
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTags();
            }
        });
        
        // Cancel button
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Double-click on existing tags list to add to text field
        existingTagsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addSelectedTagsToTextField();
                }
            }
        });
        
        // Enter key in text field triggers add
        newTagsField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTags();
            }
        });
    }
    
    /**
     * Populates the existing tags list with tags from other levels.
     */
    private void populateExistingTags() {
        Set<String> allTags = tagsManager.getAllTags(home);
        Set<String> currentTags = new HashSet<>(tagsManager.getTagsForLevel(level));
        
        // Remove tags that this level already has
        allTags.removeAll(currentTags);
        
        // Add to list model in sorted order
        List<String> sortedTags = new ArrayList<>(allTags);
        Collections.sort(sortedTags);
        
        for (String tag : sortedTags) {
            existingTagsModel.addElement(tag);
        }
    }
    
    /**
     * Adds selected tags from the existing tags list to the text field.
     */
    private void addSelectedTagsToTextField() {
        List<String> selectedTags = existingTagsList.getSelectedValuesList();
        if (selectedTags.isEmpty()) {
            return;
        }
        
        String currentText = newTagsField.getText().trim();
        StringBuilder sb = new StringBuilder();
        
        if (!currentText.isEmpty()) {
            sb.append(currentText);
            if (!currentText.endsWith(",")) {
                sb.append(", ");
            } else {
                sb.append(" ");
            }
        }
        
        sb.append(String.join(", ", selectedTags));
        newTagsField.setText(sb.toString());
        
        // Clear selection
        existingTagsList.clearSelection();
        
        // Focus back to text field
        newTagsField.requestFocus();
        newTagsField.setCaretPosition(newTagsField.getText().length());
    }
    
    /**
     * Adds the tags from the text field and selected existing tags to the level.
     */
    private void addTags() {
        List<String> tagsToAdd = new ArrayList<>();
        
        // Parse tags from text field
        String textFieldContent = newTagsField.getText().trim();
        if (!textFieldContent.isEmpty()) {
            List<String> parsedTags = LevelTagsManager.parseTagString(textFieldContent);
            tagsToAdd.addAll(parsedTags);
        }
        
        // Add selected existing tags
        List<String> selectedTags = existingTagsList.getSelectedValuesList();
        tagsToAdd.addAll(selectedTags);
        
        if (tagsToAdd.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                resources.getString("message.noTagsEntered"),
                getTitle(),
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Add tags to level
        tagsManager.addTagsToLevel(level, tagsToAdd);
        
        // Close dialog
        dispose();
    }
}