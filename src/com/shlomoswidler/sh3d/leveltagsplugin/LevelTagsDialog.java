/*
 * LevelTagsDialog.java
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

import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.viewcontroller.HomeController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

/**
 * Main dialog for managing level tags.
 * Provides a dual-tab interface for viewing levels with their tags and tags with their levels.
 * 
 * @author Level Tags Plugin Contributors
 */
public class LevelTagsDialog extends JDialog {
    
    private final HomeController homeController;
    private final Home home;
    private final LevelTagsManager tagsManager;
    private final ResourceBundle resources;
    
    // UI Components
    private JTabbedPane tabbedPane;
    private JTable levelsTable;
    private JTable tagsTable;
    private LevelsTableModel levelsTableModel;
    private TagsTableModel tagsTableModel;
    
    // Buttons
    private JButton addTagsButton;
    private JButton removeTagsButton;
    private JButton hideLevelButton;
    private JButton showLevelButton;
    private JButton hideLevelsWithTagButton;
    private JButton showLevelsWithTagButton;
    private JButton closeButton;
    
    // Collection listener for level changes
    private CollectionListener<Level> levelsListener;
    
    /**
     * Creates a new Level Tags Dialog.
     * 
     * @param homeController the home controller
     * @param home the home containing the levels
     * @param tagsManager the tags manager instance
     */
    public LevelTagsDialog(HomeController homeController, Home home, LevelTagsManager tagsManager) {
        super(SwingUtilities.getWindowAncestor((Component) homeController.getView()), ModalityType.MODELESS);
        
        this.homeController = homeController;
        this.home = home;
        this.tagsManager = tagsManager;
        this.resources = ResourceBundle.getBundle("LevelTagsPlugin");
        
        initializeDialog();
        createComponents();
        layoutComponents();
        setupEventHandlers();
        setupHomeListener();
        refreshData();
        
        pack();
        setLocationRelativeTo(getParent());
    }
    
    /**
     * Initializes the dialog properties.
     */
    private void initializeDialog() {
        setTitle(resources.getString("levelTagsDialog.title"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
        
        // Set minimum size
        setMinimumSize(new Dimension(600, 400));
        setPreferredSize(new Dimension(800, 600));
        
        // Add Esc key handling
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
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Create table models
        levelsTableModel = new LevelsTableModel();
        tagsTableModel = new TagsTableModel();
        
        // Create tables
        levelsTable = new JTable(levelsTableModel);
        tagsTable = new JTable(tagsTableModel);
        
        // Configure tables
        configureTable(levelsTable);
        configureTable(tagsTable);
        
        // Set custom renderer for tags column
        levelsTable.getColumnModel().getColumn(1).setCellRenderer(new TagsCellRenderer());
        
        // Create buttons
        addTagsButton = new JButton(resources.getString("button.addTags"));
        removeTagsButton = new JButton(resources.getString("button.removeTags"));
        hideLevelButton = new JButton(resources.getString("button.hideLevel"));
        showLevelButton = new JButton(resources.getString("button.showLevel"));
        hideLevelsWithTagButton = new JButton(resources.getString("button.hideLevelsWithTag"));
        showLevelsWithTagButton = new JButton(resources.getString("button.showLevelsWithTag"));
        closeButton = new JButton(resources.getString("button.close"));
        
        // Set initial button states
        updateButtonStates();
    }
    
    /**
     * Configures a table with standard settings.
     */
    private void configureTable(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setRowHeight(table.getRowHeight() + 4);
        
        // Auto-resize columns
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }
    
    /**
     * Lays out all components in the dialog.
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Create levels panel
        JPanel levelsPanel = createLevelsPanel();
        tabbedPane.addTab(resources.getString("levelTagsDialog.levelsTab"), levelsPanel);
        
        // Create tags panel
        JPanel tagsPanel = createTagsPanel();
        tabbedPane.addTab(resources.getString("levelTagsDialog.tagsTab"), tagsPanel);
        
        // Add tabbed pane to center
        add(tabbedPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the levels tab panel.
     */
    private JPanel createLevelsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create scroll pane for table
        JScrollPane scrollPane = new JScrollPane(levelsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel for level operations
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addTagsButton);
        buttonPanel.add(removeTagsButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(hideLevelButton);
        buttonPanel.add(showLevelButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates the tags tab panel.
     */
    private JPanel createTagsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create scroll pane for table
        JScrollPane scrollPane = new JScrollPane(tagsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel for tag operations
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(hideLevelsWithTagButton);
        buttonPanel.add(showLevelsWithTagButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates the main button panel at the bottom of the dialog.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(closeButton);
        return panel;
    }
    
    /**
     * Sets up event handlers for UI components.
     */
    private void setupEventHandlers() {
        // Window closing handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        
        // Table selection handlers
        levelsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateButtonStates();
                    // Force table refresh to ensure tags column shows correct data
                    if (levelsTableModel.getRowCount() > 0) {
                        levelsTableModel.fireTableRowsUpdated(0, levelsTableModel.getRowCount() - 1);
                    }
                }
            }
        });
        
        tagsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateButtonStates();
                }
            }
        });
        
        // Button handlers
        addTagsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddTagsDialog();
            }
        });
        
        removeTagsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedTags();
            }
        });
        
        hideLevelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideSelectedLevel();
            }
        });
        
        showLevelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSelectedLevel();
            }
        });
        
        hideLevelsWithTagButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideLevelsWithSelectedTag();
            }
        });
        
        showLevelsWithTagButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLevelsWithSelectedTag();
            }
        });
        
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    /**
     * Sets up listener for level changes.
     */
    private void setupHomeListener() {
        levelsListener = new CollectionListener<Level>() {
            @Override
            public void collectionChanged(CollectionEvent<Level> evt) {
                // Refresh data when levels change
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        refreshData();
                    }
                });
            }
        };
        
        // Listen for level changes
        home.addLevelsListener(levelsListener);
    }
    
    /**
     * Shows the Add Tags dialog for the selected level.
     */
    private void showAddTagsDialog() {
        Level selectedLevel = getSelectedLevel();
        if (selectedLevel == null) {
            JOptionPane.showMessageDialog(this, 
                resources.getString("message.noLevelSelected"),
                getTitle(),
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        AddTagsDialog dialog = new AddTagsDialog(this, selectedLevel, tagsManager, home, resources);
        dialog.setVisible(true);
        
        // Refresh data after dialog closes
        refreshData();
    }
    
    /**
     * Removes selected tags from the currently selected level.
     */
    private void removeSelectedTags() {
        Level selectedLevel = getSelectedLevel();
        if (selectedLevel == null) {
            return;
        }
        
        // For now, show a simple dialog to select tags to remove
        List<String> currentTags = tagsManager.getTagsForLevel(selectedLevel);
        if (currentTags.isEmpty()) {
            return;
        }
        
        String[] tagArray = currentTags.toArray(new String[0]);
        String[] selectedTags = (String[]) JOptionPane.showInputDialog(this,
            "Select tags to remove:",
            "Remove Tags",
            JOptionPane.PLAIN_MESSAGE,
            null,
            tagArray,
            null);
        
        if (selectedTags != null) {
            tagsManager.removeTagsFromLevel(selectedLevel, Arrays.asList(selectedTags));
            refreshData();
        }
    }
    
    /**
     * Hides the selected level.
     */
    private void hideSelectedLevel() {
        Level selectedLevel = getSelectedLevel();
        if (selectedLevel != null) {
            tagsManager.hideLevel(selectedLevel);
            refreshData();
        }
    }
    
    /**
     * Shows the selected level.
     */
    private void showSelectedLevel() {
        Level selectedLevel = getSelectedLevel();
        if (selectedLevel != null) {
            tagsManager.showLevel(selectedLevel);
            refreshData();
        }
    }
    
    /**
     * Hides all levels with the selected tag.
     */
    private void hideLevelsWithSelectedTag() {
        String selectedTag = getSelectedTag();
        if (selectedTag == null) {
            return;
        }
        
        List<Level> levels = tagsManager.getLevelsWithTag(home, selectedTag);
        if (levels.isEmpty()) {
            return;
        }
        
        tagsManager.hideLevelsWithTag(home, selectedTag);
        refreshData();
    }
    
    /**
     * Shows all levels with the selected tag.
     */
    private void showLevelsWithSelectedTag() {
        String selectedTag = getSelectedTag();
        if (selectedTag == null) {
            return;
        }
        
        List<Level> levels = tagsManager.getLevelsWithTag(home, selectedTag);
        if (levels.isEmpty()) {
            return;
        }
        
        tagsManager.showLevelsWithTag(home, selectedTag);
        refreshData();
    }
    
    /**
     * Gets the currently selected level from the levels table.
     */
    private Level getSelectedLevel() {
        int selectedRow = levelsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < levelsTableModel.getRowCount()) {
            return levelsTableModel.getLevelAt(selectedRow);
        }
        return null;
    }
    
    /**
     * Gets the currently selected tag from the tags table.
     */
    private String getSelectedTag() {
        int selectedRow = tagsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < tagsTableModel.getRowCount()) {
            return tagsTableModel.getTagAt(selectedRow);
        }
        return null;
    }
    
    /**
     * Updates the enabled state of buttons based on current selections.
     */
    private void updateButtonStates() {
        Level selectedLevel = getSelectedLevel();
        String selectedTag = getSelectedTag();
        
        // Levels tab buttons
        addTagsButton.setEnabled(selectedLevel != null);
        removeTagsButton.setEnabled(selectedLevel != null && 
            !tagsManager.getTagsForLevel(selectedLevel).isEmpty());
        hideLevelButton.setEnabled(selectedLevel != null && selectedLevel.isViewable());
        showLevelButton.setEnabled(selectedLevel != null && !selectedLevel.isViewable());
        
        // Tags tab buttons
        hideLevelsWithTagButton.setEnabled(selectedTag != null);
        showLevelsWithTagButton.setEnabled(selectedTag != null);
    }
    
    /**
     * Refreshes all data in the tables.
     */
    private void refreshData() {
        SwingUtilities.invokeLater(() -> {
            levelsTableModel.fireTableDataChanged();
            tagsTableModel.fireTableDataChanged();
            levelsTable.clearSelection();
            levelsTable.revalidate();
            levelsTable.repaint();
            tagsTable.clearSelection();
            tagsTable.revalidate();
            tagsTable.repaint();
            updateButtonStates();
        });
    }
    
    /**
     * Disposes the dialog and cleans up resources.
     */
    @Override
    public void dispose() {
        // Remove levels listener
        if (levelsListener != null) {
            home.removeLevelsListener(levelsListener);
        }
        
        super.dispose();
    }
    
    /**
     * Table model for the levels table.
     */
    private class LevelsTableModel extends AbstractTableModel {
        
        @Override
        public int getRowCount() {
            return home.getLevels().size();
        }
        
        @Override
        public int getColumnCount() {
            return 2;
        }
        
        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0: return resources.getString("levelTagsDialog.levelColumn");
                case 1: return resources.getString("levelTagsDialog.tagsColumn");
                default: return "";
            }
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Level level = home.getLevels().get(rowIndex);
            switch (columnIndex) {
                case 0: 
                    String name = level.getName();
                    if (!level.isViewable()) {
                        name += " (hidden)";
                    }
                    return name;
                case 1: 
                    List<String> tags = tagsManager.getTagsForLevel(level);
                    return tags.isEmpty() ? "" : String.join(", ", tags);
                default: 
                    return "";
            }
        }
        
        public Level getLevelAt(int rowIndex) {
            return home.getLevels().get(rowIndex);
        }
    }
    
    /**
     * Table model for the tags table.
     */
    private class TagsTableModel extends AbstractTableModel {
        
        private List<Map.Entry<String, List<Level>>> tagEntries;
        
        public TagsTableModel() {
            updateTagEntries();
        }
        
        private void updateTagEntries() {
            Map<String, List<Level>> tagToLevels = tagsManager.getTagToLevelsMap(home);
            tagEntries = new ArrayList<>(tagToLevels.entrySet());
            
            // Sort by tag name
            tagEntries.sort(Map.Entry.comparingByKey());
        }
        
        @Override
        public int getRowCount() {
            updateTagEntries();
            return tagEntries.size();
        }
        
        @Override
        public int getColumnCount() {
            return 3;
        }
        
        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0: return resources.getString("levelTagsDialog.tagColumn");
                case 1: return resources.getString("levelTagsDialog.levelCountColumn");
                case 2: return resources.getString("levelTagsDialog.levelsColumn");
                default: return "";
            }
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Map.Entry<String, List<Level>> entry = tagEntries.get(rowIndex);
            switch (columnIndex) {
                case 0: 
                    return entry.getKey();
                case 1: 
                    return entry.getValue().size();
                case 2:
                    List<String> levelNames = new ArrayList<>();
                    for (Level level : entry.getValue()) {
                        levelNames.add(level.getName());
                    }
                    return String.join(", ", levelNames);
                default: 
                    return "";
            }
        }
        
        public String getTagAt(int rowIndex) {
            if (rowIndex >= 0 && rowIndex < tagEntries.size()) {
                return tagEntries.get(rowIndex).getKey();
            }
            return null;
        }
    }
    
    /**
     * Custom cell renderer for the tags column to make tags more visually distinct.
     */
    private static class TagsCellRenderer extends DefaultTableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component component = super.getTableCellRendererComponent(table, value, 
                isSelected, hasFocus, row, column);
            
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                String text = label.getText();
                
                if (text != null && !text.isEmpty()) {
                    // Make tags more visually distinct
                    label.setFont(label.getFont().deriveFont(Font.ITALIC));
                    if (!isSelected) {
                        label.setForeground(Color.BLUE.darker());
                    }
                }
            }
            
            return component;
        }
    }
}