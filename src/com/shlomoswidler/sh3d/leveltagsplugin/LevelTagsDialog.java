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
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main dialog for managing level tags.
 * Single window with split pane showing levels on left and tags on right.
 * 
 * @author Level Tags Plugin Contributors
 */
public class LevelTagsDialog extends JDialog {
    
    private final HomeController homeController;
    private final Home home;
    private final LevelTagsManager tagsManager;
    private final ResourceBundle resources;
    
    // Main layout components
    private JSplitPane splitPane;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel statusPanel;
    private JLabel statusLabel;
    private JLabel versionLabel;
    
    // Left panel - Levels view
    private JTextField levelFilterField;
    private JComboBox<VisibilityFilterItem> visibilityFilterCombo;
    private JTable levelsTable;
    private LevelsTableModel levelsTableModel;
    private JComboBox<String> addTagCombo;
    private JComboBox<String> removeTagCombo;
    private JButton addTagButton;
    private JButton removeTagButton;
    private JButton removeAllTagsButton;
    private JButton selectAllButton;
    private JButton selectNoneButton;
    
    // Right panel - Tags view
    private JTextField tagFilterField;
    private JTable tagsTable;
    private TagsTableModel tagsTableModel;
    private JTable associatedLevelsTable;
    private AssociatedLevelsTableModel associatedLevelsTableModel;
    private JRadioButton unionModeRadio;
    private JRadioButton intersectionModeRadio;
    private JButton showAllTaggedButton;
    private JButton hideAllTaggedButton;
    private JButton renameTagButton;
    private JButton deleteTagButton;
    
    // Collection listener for level changes
    private CollectionListener<Level> levelsListener;
    
    // Filter state
    private String levelFilterText = "";
    private VisibilityFilter visibilityFilter = VisibilityFilter.ALL;
    private String tagFilterText = "";
    private boolean updateInProgress = false;
    
    /**
     * Visibility filter options for levels
     */
    private enum VisibilityFilter {
        ALL("filter.all"),
        VISIBLE("filter.visible"),
        HIDDEN("filter.hidden");
        
        private final String resourceKey;
        
        VisibilityFilter(String resourceKey) {
            this.resourceKey = resourceKey;
        }
        
        public String getDisplayName(ResourceBundle resources) {
            return resources.getString(resourceKey);
        }
        
        @Override
        public String toString() {
            // Fallback for cases where ResourceBundle isn't available
            return resourceKey.substring(resourceKey.lastIndexOf('.') + 1);
        }
    }
    
    /**
     * Wrapper for VisibilityFilter to display localized names in combo box
     */
    private static class VisibilityFilterItem {
        private final VisibilityFilter filter;
        private final String displayName;
        
        public VisibilityFilterItem(VisibilityFilter filter, ResourceBundle resources) {
            this.filter = filter;
            this.displayName = filter.getDisplayName(resources);
        }
        
        public VisibilityFilter getFilter() {
            return filter;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    /**
     * Tag information for the tags list
     */
    private static class TagInfo {
        public final String name;
        public final int levelCount;
        
        public TagInfo(String name, int levelCount) {
            this.name = name;
            this.levelCount = levelCount;
        }
        
        @Override
        public String toString() {
            return name + " · (" + levelCount + " levels)";
        }
    }
    
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
        
        // Set minimum and preferred size
        setMinimumSize(new Dimension(900, 600));
        setPreferredSize(new Dimension(1200, 800));
        
        // Add Esc key handling
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Add Ctrl+F for filter focus
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlF, "FOCUS_FILTER");
        getRootPane().getActionMap().put("FOCUS_FILTER", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                focusActiveFilter();
            }
        });
    }
    
    /**
     * Creates all UI components.
     */
    private void createComponents() {
        // Main split pane
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(750);
        splitPane.setResizeWeight(0.65);
        
        // Create left and right panels
        createLeftPanel();
        createRightPanel();
        
        // Set minimum sizes to ensure buttons remain visible
        leftPanel.setMinimumSize(new Dimension(550, 0));
        rightPanel.setMinimumSize(new Dimension(300, 0));
        
        // Status panel with message on left and version on right
        statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusPanel.setOpaque(true);
        statusPanel.setBackground(Color.LIGHT_GRAY);
        
        statusLabel = new JLabel(" ");
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        versionLabel = new JLabel(resources.getString("label.version"));
        versionLabel.setFont(versionLabel.getFont().deriveFont(Font.PLAIN, 10f));
        versionLabel.setForeground(Color.DARK_GRAY);
        statusPanel.add(versionLabel, BorderLayout.EAST);
    }
    
    /**
     * Creates the left panel (Levels view).
     */
    private void createLeftPanel() {
        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(new TitledBorder(resources.getString("panel.levels")));
        
        // Header toolbar
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.add(new JLabel(resources.getString("label.filter")));
        levelFilterField = new JTextField(6);
        levelFilterField.setToolTipText(resources.getString("tooltip.levelFilter"));
        headerPanel.add(levelFilterField);
        
        headerPanel.add(Box.createHorizontalStrut(10));
        headerPanel.add(new JLabel(resources.getString("label.show")));
        visibilityFilterCombo = new JComboBox<>();
        for (VisibilityFilter filter : VisibilityFilter.values()) {
            visibilityFilterCombo.addItem(new VisibilityFilterItem(filter, resources));
        }
        headerPanel.add(visibilityFilterCombo);
        
        headerPanel.add(Box.createHorizontalStrut(10));
        selectAllButton = new JButton(resources.getString("button.selectAll"));
        selectNoneButton = new JButton(resources.getString("button.selectNone"));
        headerPanel.add(selectAllButton);
        headerPanel.add(selectNoneButton);
        
        leftPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Main table
        levelsTableModel = new LevelsTableModel();
        levelsTable = new JTable(levelsTableModel);
        levelsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        levelsTable.setRowHeight(28);
        levelsTable.getColumnModel().getColumn(0).setMaxWidth(52); // Visibility column
        levelsTable.getColumnModel().getColumn(0).setMinWidth(52);
        levelsTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Level name
        levelsTable.getColumnModel().getColumn(2).setPreferredWidth(250); // Tags
        
        // Custom renderers
        levelsTable.getColumnModel().getColumn(0).setCellRenderer(new VisibilityCheckboxRenderer());
        levelsTable.getColumnModel().getColumn(2).setCellRenderer(new TagsRenderer());
        
        JScrollPane levelsScrollPane = new JScrollPane(levelsTable);
        leftPanel.add(levelsScrollPane, BorderLayout.CENTER);
        
        // Footer bar
        JPanel footerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        footerPanel.add(new JLabel(resources.getString("label.addTag")), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        addTagCombo = new JComboBox<>();
        addTagCombo.setEditable(true);
        addTagCombo.setToolTipText(resources.getString("tooltip.addTag"));
        addTagCombo.setPreferredSize(new Dimension(200, addTagCombo.getPreferredSize().height));
        footerPanel.add(addTagCombo, gbc);
        
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        addTagButton = new JButton(resources.getString("button.addToSelected"));
        footerPanel.add(addTagButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        footerPanel.add(new JLabel(resources.getString("label.removeTag")), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        removeTagCombo = new JComboBox<>();
        removeTagCombo.setPreferredSize(new Dimension(200, removeTagCombo.getPreferredSize().height));
        footerPanel.add(removeTagCombo, gbc);
        
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        removeTagButton = new JButton(resources.getString("button.removeFromSelected"));
        footerPanel.add(removeTagButton, gbc);
        
        gbc.gridx = 3;
        removeAllTagsButton = new JButton(resources.getString("button.removeAllTags"));
        footerPanel.add(removeAllTagsButton, gbc);
        
        leftPanel.add(footerPanel, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(leftPanel);
    }
    
    /**
     * Creates the right panel (Tags view).
     */
    private void createRightPanel() {
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new TitledBorder(resources.getString("panel.tags")));
        
        // Header toolbar
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.add(new JLabel(resources.getString("label.filter")));
        tagFilterField = new JTextField(6);
        tagFilterField.setToolTipText(resources.getString("tooltip.tagFilter"));
        headerPanel.add(tagFilterField);
        
        headerPanel.add(Box.createHorizontalStrut(10));
        headerPanel.add(Box.createHorizontalGlue()); // Push buttons to the right
        renameTagButton = new JButton(resources.getString("button.rename"));
        deleteTagButton = new JButton(resources.getString("button.delete"));
        headerPanel.add(renameTagButton);
        headerPanel.add(deleteTagButton);
        
        rightPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Main content - split vertically
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setDividerLocation(250);
        rightSplitPane.setResizeWeight(0.4);
        
        // Make divider arrows black for better visibility
        rightSplitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        super.paint(g);
                        // Override the arrow colors to black
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(Color.BLACK);
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        int centerX = getWidth() / 2;
                        int centerY = getHeight() / 2;
                        int arrowSize = 4;
                        
                        // Draw up arrow (moved down to avoid clipping)
                        int[] upX = {centerX - arrowSize, centerX + arrowSize, centerX};
                        int[] upY = {centerY - 1, centerY - 1, centerY - 5};
                        g2.fillPolygon(upX, upY, 3);
                        
                        // Draw down arrow (moved up to avoid clipping)
                        int[] downX = {centerX - arrowSize, centerX + arrowSize, centerX};
                        int[] downY = {centerY + 1, centerY + 1, centerY + 5};
                        g2.fillPolygon(downX, downY, 3);
                        
                        g2.dispose();
                    }
                };
            }
        });
        
        // Tags table
        tagsTableModel = new TagsTableModel();
        tagsTable = new JTable(tagsTableModel);
        tagsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tagsTable.setRowHeight(28);
        tagsTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Tag name
        tagsTable.getColumnModel().getColumn(1).setMaxWidth(50); // Count column
        tagsTable.getColumnModel().getColumn(1).setMinWidth(50);
        tagsTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Levels column
        JScrollPane tagsScrollPane = new JScrollPane(tagsTable);
        tagsScrollPane.setBorder(new TitledBorder(resources.getString("panel.tags")));
        rightSplitPane.setTopComponent(tagsScrollPane);
        
        // Associated levels panel
        JPanel associatedPanel = new JPanel(new BorderLayout());
        associatedPanel.setBorder(new TitledBorder(resources.getString("panel.associatedLevels")));
        
        // Mode selection
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        unionModeRadio = new JRadioButton(resources.getString("radio.unionMode"), true);
        intersectionModeRadio = new JRadioButton(resources.getString("radio.intersectionMode"));
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(unionModeRadio);
        modeGroup.add(intersectionModeRadio);
        modePanel.add(unionModeRadio);
        modePanel.add(intersectionModeRadio);
        associatedPanel.add(modePanel, BorderLayout.NORTH);
        
        // Associated levels table
        associatedLevelsTableModel = new AssociatedLevelsTableModel();
        associatedLevelsTable = new JTable(associatedLevelsTableModel);
        associatedLevelsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        associatedLevelsTable.getColumnModel().getColumn(0).setMaxWidth(52);
        associatedLevelsTable.getColumnModel().getColumn(0).setMinWidth(52);
        associatedLevelsTable.getColumnModel().getColumn(0).setCellRenderer(new VisibilityCheckboxRenderer());
        JScrollPane associatedScrollPane = new JScrollPane(associatedLevelsTable);
        associatedPanel.add(associatedScrollPane, BorderLayout.CENTER);
        
        rightSplitPane.setBottomComponent(associatedPanel);
        rightPanel.add(rightSplitPane, BorderLayout.CENTER);
        
        // Footer bar
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        showAllTaggedButton = new JButton(resources.getString("button.showAll"));
        hideAllTaggedButton = new JButton(resources.getString("button.hideAll"));
        footerPanel.add(showAllTaggedButton);
        footerPanel.add(hideAllTaggedButton);
        
        rightPanel.add(footerPanel, BorderLayout.SOUTH);
        
        splitPane.setRightComponent(rightPanel);
    }
    
    /**
     * Lays out all components in the dialog.
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
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
        
        // Left panel event handlers
        setupLeftPanelHandlers();
        
        // Right panel event handlers
        setupRightPanelHandlers();
    }
    
    /**
     * Sets up event handlers for the left panel.
     */
    private void setupLeftPanelHandlers() {
        // Level filter
        levelFilterField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateLevelFilter(); }
            public void removeUpdate(DocumentEvent e) { updateLevelFilter(); }
            public void changedUpdate(DocumentEvent e) { updateLevelFilter(); }
        });
        
        // Visibility filter
        visibilityFilterCombo.addActionListener(e -> {
            VisibilityFilterItem item = (VisibilityFilterItem) visibilityFilterCombo.getSelectedItem();
            visibilityFilter = item != null ? item.getFilter() : VisibilityFilter.ALL;
            refreshLevelsTable();
        });
        
        // Selection buttons
        selectAllButton.addActionListener(e -> levelsTable.selectAll());
        selectNoneButton.addActionListener(e -> levelsTable.clearSelection());
        
        // Levels table selection
        levelsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateLeftPanelButtons();
                syncTagsSelection();
            }
        });
        
        // Levels table visibility toggle
        levelsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = levelsTable.columnAtPoint(e.getPoint());
                if (column == 0) { // Visibility column
                    int row = levelsTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        Level level = levelsTableModel.getFilteredLevelAt(row);
                        boolean newVisibility = !level.isViewable();
                        if (newVisibility) {
                            tagsManager.showLevel(level);
                        } else {
                            tagsManager.hideLevel(level);
                        }
                        String action = newVisibility ? resources.getString("status.shown") : resources.getString("status.hidden");
                        showStatus(MessageFormat.format(resources.getString("status.levelVisibility"), level.getName(), action));
                        refreshData();
                    }
                }
            }
        });
        
        // Add tag
        addTagButton.addActionListener(e -> addTagToSelectedLevels());
        
        // Ctrl+Enter shortcut for adding tags
        KeyStroke ctrlEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK);
        addTagCombo.getInputMap().put(ctrlEnter, "ADD_TAG");
        addTagCombo.getActionMap().put("ADD_TAG", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addTagToSelectedLevels();
            }
        });
        
        // Remove tag
        removeTagButton.addActionListener(e -> removeTagFromSelectedLevels());
        removeAllTagsButton.addActionListener(e -> removeAllTagsFromSelectedLevels());
    }
    
    /**
     * Sets up event handlers for the right panel.
     */
    private void setupRightPanelHandlers() {
        // Tag filter
        tagFilterField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateTagFilter(); }
            public void removeUpdate(DocumentEvent e) { updateTagFilter(); }
            public void changedUpdate(DocumentEvent e) { updateTagFilter(); }
        });
        
        // Tags list selection
        tagsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateRightPanelButtons();
                refreshAssociatedLevelsTable();
                syncLevelsSelection();
            }
        });
        
        // Mode radio buttons
        ActionListener modeListener = e -> {
            refreshAssociatedLevelsTable();
            syncLevelsSelection();
        };
        unionModeRadio.addActionListener(modeListener);
        intersectionModeRadio.addActionListener(modeListener);
        
        // Tag management buttons
        renameTagButton.addActionListener(e -> renameSelectedTag());
        deleteTagButton.addActionListener(e -> deleteSelectedTag());
        
        // Visibility buttons
        showAllTaggedButton.addActionListener(e -> setVisibilityForTaggedLevels(true));
        hideAllTaggedButton.addActionListener(e -> setVisibilityForTaggedLevels(false));
    }
    
    /**
     * Sets up listener for level changes.
     */
    private void setupHomeListener() {
        levelsListener = new CollectionListener<Level>() {
            @Override
            public void collectionChanged(CollectionEvent<Level> evt) {
                SwingUtilities.invokeLater(() -> {
                    if (!updateInProgress) {
                        refreshData();
                    }
                });
            }
        };
        
        home.addLevelsListener(levelsListener);
    }
    
    // Update methods
    private void updateLevelFilter() {
        levelFilterText = levelFilterField.getText().toLowerCase().trim();
        refreshLevelsTable();
    }
    
    private void updateTagFilter() {
        tagFilterText = tagFilterField.getText().toLowerCase().trim();
        refreshTagsList();
    }
    
    private void focusActiveFilter() {
        Component focusOwner = getFocusOwner();
        if (focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, rightPanel)) {
            tagFilterField.requestFocus();
        } else {
            levelFilterField.requestFocus();
        }
    }
    
    // Action methods for left panel
    private void addTagToSelectedLevels() {
        int[] selectedRows = levelsTable.getSelectedRows();
        if (selectedRows.length == 0) {
            showStatus(resources.getString("message.noLevelSelected"));
            return;
        }
        
        String tagName = (String) addTagCombo.getSelectedItem();
        if (tagName == null || tagName.trim().isEmpty()) {
            showStatus(resources.getString("message.noTagsEntered"));
            return;
        }
        
        tagName = tagName.trim();
        List<Level> selectedLevels = new ArrayList<>();
        for (int row : selectedRows) {
            selectedLevels.add(levelsTableModel.getFilteredLevelAt(row));
        }
        
        tagsManager.addTagsToLevel(selectedLevels.get(0), Arrays.asList(tagName));
        for (int i = 1; i < selectedLevels.size(); i++) {
            tagsManager.addTagsToLevel(selectedLevels.get(i), Arrays.asList(tagName));
        }
        
        showStatus(MessageFormat.format(resources.getString("message.tagsAdded"), tagName, selectedLevels.size()));
        refreshData();
        updateTagComboBoxes();
    }
    
    private void removeTagFromSelectedLevels() {
        int[] selectedRows = levelsTable.getSelectedRows();
        if (selectedRows.length == 0) {
            showStatus(resources.getString("message.noLevelSelected"));
            return;
        }
        
        String tagName = (String) removeTagCombo.getSelectedItem();
        if (tagName == null) {
            showStatus(resources.getString("message.noTagSelected"));
            return;
        }
        
        List<Level> selectedLevels = new ArrayList<>();
        for (int row : selectedRows) {
            selectedLevels.add(levelsTableModel.getFilteredLevelAt(row));
        }
        
        for (Level level : selectedLevels) {
            tagsManager.removeTagsFromLevel(level, Arrays.asList(tagName));
        }
        
        showStatus(MessageFormat.format(resources.getString("message.tagsRemoved"), tagName, selectedLevels.size()));
        refreshData();
        updateTagComboBoxes();
    }
    
    private void removeAllTagsFromSelectedLevels() {
        int[] selectedRows = levelsTable.getSelectedRows();
        if (selectedRows.length == 0) {
            showStatus(resources.getString("message.noLevelSelected"));
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this,
            "Remove all tags from " + selectedRows.length + " selected level(s)?",
            "Confirm Remove All Tags",
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            for (int row : selectedRows) {
                Level level = levelsTableModel.getFilteredLevelAt(row);
                List<String> tags = tagsManager.getTagsForLevel(level);
                if (!tags.isEmpty()) {
                    tagsManager.removeTagsFromLevel(level, tags);
                }
            }
            
            showStatus(MessageFormat.format(resources.getString("message.allTagsRemoved"), selectedRows.length));
            refreshData();
            updateTagComboBoxes();
        }
    }
    
    // Action methods for right panel
    private void renameSelectedTag() {
        List<TagInfo> selectedTags = getSelectedTags();
        if (selectedTags.size() != 1) {
            showStatus(resources.getString("message.selectOneTag"));
            return;
        }
        
        String oldName = selectedTags.get(0).name;
        String newName = JOptionPane.showInputDialog(this, resources.getString("prompt.renameTag"), oldName);
        if (newName != null && !newName.trim().isEmpty() && !newName.equals(oldName)) {
            newName = newName.trim();
            
            // Rename by updating all levels that have this tag
            List<Level> affectedLevels = tagsManager.getLevelsWithTag(home, oldName);
            for (Level level : affectedLevels) {
                tagsManager.removeTagsFromLevel(level, Arrays.asList(oldName));
                tagsManager.addTagsToLevel(level, Arrays.asList(newName));
            }
            
            showStatus(MessageFormat.format(resources.getString("message.tagRenamed"), oldName, newName, affectedLevels.size()));
            refreshData();
            updateTagComboBoxes();
        }
    }
    
    private void deleteSelectedTag() {
        List<TagInfo> selectedTags = getSelectedTags();
        if (selectedTags.isEmpty()) {
            showStatus(resources.getString("message.noTagSelected"));
            return;
        }
        
        StringBuilder message = new StringBuilder("Delete the following tag(s)?");
        for (TagInfo tag : selectedTags) {
            message.append("\n- ").append(tag.name).append(" (").append(tag.levelCount).append(" levels)");
        }
        
        int result = JOptionPane.showConfirmDialog(this, message.toString(), "Confirm Delete Tags", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            int totalAffected = 0;
            for (TagInfo tag : selectedTags) {
                List<Level> affectedLevels = tagsManager.getLevelsWithTag(home, tag.name);
                for (Level level : affectedLevels) {
                    tagsManager.removeTagsFromLevel(level, Arrays.asList(tag.name));
                }
                totalAffected += affectedLevels.size();
            }
            
            showStatus(MessageFormat.format(resources.getString("message.tagsDeleted"), selectedTags.size(), totalAffected));
            refreshData();
            updateTagComboBoxes();
        }
    }
    
    private void setVisibilityForTaggedLevels(boolean visible) {
        List<TagInfo> selectedTags = getSelectedTags();
        if (selectedTags.isEmpty()) {
            showStatus(resources.getString("message.noTagSelected"));
            return;
        }
        
        // Preserve the current tags selection
        Set<String> selectedTagNames = new HashSet<>();
        for (TagInfo tag : selectedTags) {
            selectedTagNames.add(tag.name);
        }
        
        Set<Level> affectedLevels = new HashSet<>();
        
        if (unionModeRadio.isSelected()) {
            // Union mode - levels with any of the selected tags
            for (TagInfo tag : selectedTags) {
                affectedLevels.addAll(tagsManager.getLevelsWithTag(home, tag.name));
            }
        } else {
            // Intersection mode - levels with all selected tags
            if (!selectedTags.isEmpty()) {
                affectedLevels.addAll(tagsManager.getLevelsWithTag(home, selectedTags.get(0).name));
                for (int i = 1; i < selectedTags.size(); i++) {
                    affectedLevels.retainAll(tagsManager.getLevelsWithTag(home, selectedTags.get(i).name));
                }
            }
        }
        
        for (Level level : affectedLevels) {
            if (visible) {
                tagsManager.showLevel(level);
            } else {
                tagsManager.hideLevel(level);
            }
        }
        
        String messageKey = visible ? "message.levelsShown" : "message.levelsHidden";
        showStatus(MessageFormat.format(resources.getString(messageKey), affectedLevels.size()));
        refreshData();
        
        // Restore the tags selection after refresh
        SwingUtilities.invokeLater(() -> {
            restoreTagsSelection(selectedTagNames);
        });
    }
    
    private void restoreTagsSelection(Set<String> selectedTagNames) {
        if (selectedTagNames.isEmpty()) return;
        
        List<Integer> indicesToSelect = new ArrayList<>();
        for (int i = 0; i < tagsTableModel.getRowCount(); i++) {
            TagInfo tagInfo = tagsTableModel.getTagInfoAt(i);
            if (selectedTagNames.contains(tagInfo.name)) {
                indicesToSelect.add(i);
            }
        }
        
        if (!indicesToSelect.isEmpty()) {
            int[] indices = indicesToSelect.stream().mapToInt(Integer::intValue).toArray();
            for (int index : indices) {
                tagsTable.addRowSelectionInterval(index, index);
            }
        }
    }
    
    private List<TagInfo> getSelectedTags() {
        List<TagInfo> selectedTags = new ArrayList<>();
        int[] selectedRows = tagsTable.getSelectedRows();
        for (int row : selectedRows) {
            TagInfo tagInfo = tagsTableModel.getTagInfoAt(row);
            if (tagInfo != null) {
                selectedTags.add(tagInfo);
            }
        }
        return selectedTags;
    }
    
    // Synchronization methods
    private void syncTagsSelection() {
        if (updateInProgress) return;
        
        updateInProgress = true;
        try {
            Set<String> selectedLevelTags = new HashSet<>();
            int[] selectedRows = levelsTable.getSelectedRows();
            
            for (int row : selectedRows) {
                Level level = levelsTableModel.getFilteredLevelAt(row);
                selectedLevelTags.addAll(tagsManager.getTagsForLevel(level));
            }
            
            // Select tags in the right panel that are present on selected levels
            tagsTable.clearSelection();
            for (int i = 0; i < tagsTableModel.getRowCount(); i++) {
                TagInfo tag = tagsTableModel.getTagInfoAt(i);
                if (selectedLevelTags.contains(tag.name)) {
                    tagsTable.addRowSelectionInterval(i, i);
                }
            }
        } finally {
            updateInProgress = false;
        }
    }
    
    private void syncLevelsSelection() {
        if (updateInProgress) return;
        
        updateInProgress = true;
        try {
            List<TagInfo> selectedTags = getSelectedTags();
            if (selectedTags.isEmpty()) {
                levelsTable.clearSelection();
                return;
            }
            
            Set<Level> taggedLevels = new HashSet<>();
            
            if (unionModeRadio.isSelected()) {
                // Union mode
                for (TagInfo tag : selectedTags) {
                    taggedLevels.addAll(tagsManager.getLevelsWithTag(home, tag.name));
                }
            } else {
                // Intersection mode
                taggedLevels.addAll(tagsManager.getLevelsWithTag(home, selectedTags.get(0).name));
                for (int i = 1; i < selectedTags.size(); i++) {
                    taggedLevels.retainAll(tagsManager.getLevelsWithTag(home, selectedTags.get(i).name));
                }
            }
            
            // Select corresponding rows in levels table
            levelsTable.clearSelection();
            for (int row = 0; row < levelsTableModel.getRowCount(); row++) {
                Level level = levelsTableModel.getFilteredLevelAt(row);
                if (taggedLevels.contains(level)) {
                    levelsTable.addRowSelectionInterval(row, row);
                }
            }
        } finally {
            updateInProgress = false;
        }
    }
    
    // UI update methods
    private void updateLeftPanelButtons() {
        int selectedCount = levelsTable.getSelectedRowCount();
        addTagButton.setEnabled(selectedCount > 0);
        removeTagButton.setEnabled(selectedCount > 0);
        removeAllTagsButton.setEnabled(selectedCount > 0);
        
        updateTagComboBoxes();
    }
    
    private void updateRightPanelButtons() {
        List<TagInfo> selectedTags = getSelectedTags();
        boolean hasSelection = !selectedTags.isEmpty();
        boolean singleSelection = selectedTags.size() == 1;
        
        renameTagButton.setEnabled(singleSelection);
        deleteTagButton.setEnabled(hasSelection);
        showAllTaggedButton.setEnabled(hasSelection);
        hideAllTaggedButton.setEnabled(hasSelection);
    }
    
    private void updateTagComboBoxes() {
        Set<String> allTags = tagsManager.getAllTags(home);
        
        // Update add tag combo
        String selectedAdd = (String) addTagCombo.getSelectedItem();
        addTagCombo.removeAllItems();
        for (String tag : allTags) {
            addTagCombo.addItem(tag);
        }
        if (selectedAdd != null) {
            addTagCombo.setSelectedItem(selectedAdd);
        }
        
        // Update remove tag combo with common tags of selected levels
        String selectedRemove = (String) removeTagCombo.getSelectedItem();
        removeTagCombo.removeAllItems();
        
        int[] selectedRows = levelsTable.getSelectedRows();
        if (selectedRows.length > 0) {
            Set<String> commonTags = new HashSet<>(tagsManager.getTagsForLevel(
                levelsTableModel.getFilteredLevelAt(selectedRows[0])));
            
            for (int i = 1; i < selectedRows.length; i++) {
                commonTags.retainAll(tagsManager.getTagsForLevel(
                    levelsTableModel.getFilteredLevelAt(selectedRows[i])));
            }
            
            for (String tag : commonTags) {
                removeTagCombo.addItem(tag);
            }
        }
        
        if (selectedRemove != null && removeTagCombo.getItemCount() > 0) {
            removeTagCombo.setSelectedItem(selectedRemove);
        }
        
        removeTagButton.setEnabled(removeTagCombo.getItemCount() > 0);
    }
    
    // Data refresh methods
    public void refreshData() {
        refreshLevelsTable();
        refreshTagsList();
        refreshAssociatedLevelsTable();
        updateLeftPanelButtons();
        updateRightPanelButtons();
    }
    
    private void refreshLevelsTable() {
        SwingUtilities.invokeLater(() -> {
            levelsTableModel.fireTableDataChanged();
            updateLeftPanelButtons();
        });
    }
    
    private void refreshTagsList() {
        SwingUtilities.invokeLater(() -> {
            tagsTableModel.fireTableDataChanged();
            updateRightPanelButtons();
        });
    }
    
    private void refreshAssociatedLevelsTable() {
        SwingUtilities.invokeLater(() -> {
            associatedLevelsTableModel.fireTableDataChanged();
        });
    }
    
    private void showStatus(String message) {
        statusLabel.setText(message);
        
        // Clear status after 5 seconds
        javax.swing.Timer timer = new javax.swing.Timer(5000, e -> statusLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Disposes the dialog and cleans up resources.
     */
    @Override
    public void dispose() {
        if (levelsListener != null) {
            home.removeLevelsListener(levelsListener);
        }
        super.dispose();
    }
    
    // Table Models
    
    /**
     * Table model for the tags table (right panel).
     */
    private class TagsTableModel extends AbstractTableModel {
        
        private final String[] columnNames = {
            resources.getString("column.tag"), 
            resources.getString("column.count"), 
            resources.getString("column.levels")
        };
        private List<TagInfo> filteredTags = new ArrayList<>();
        
        public void fireTableDataChanged() {
            updateFilteredTags();
            super.fireTableDataChanged();
        }
        
        private void updateFilteredTags() {
            filteredTags.clear();
            Map<String, List<Level>> tagToLevels = tagsManager.getTagToLevelsMap(home);
            
            for (Map.Entry<String, List<Level>> entry : tagToLevels.entrySet()) {
                String tagName = entry.getKey().toLowerCase();
                if (tagFilterText.isEmpty() || tagName.contains(tagFilterText)) {
                    filteredTags.add(new TagInfo(entry.getKey(), entry.getValue().size()));
                }
            }
            
            filteredTags.sort(Comparator.comparing(t -> t.name));
        }
        
        @Override
        public int getRowCount() {
            return filteredTags.size();
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0: return String.class;  // Tag
                case 1: return Integer.class; // Count
                case 2: return String.class;  // Levels
                default: return String.class;
            }
        }
        
        @Override
        public Object getValueAt(int row, int column) {
            if (row < 0 || row >= filteredTags.size()) return null;
            
            TagInfo tagInfo = filteredTags.get(row);
            List<Level> levelsWithTag = tagsManager.getLevelsWithTag(home, tagInfo.name);
            
            switch (column) {
                case 0: // Tag name
                    return tagInfo.name;
                case 1: // Count
                    return tagInfo.levelCount;
                case 2: // Levels list
                    return levelsWithTag.stream()
                        .map(Level::getName)
                        .collect(Collectors.joining(", "));
                default:
                    return null;
            }
        }
        
        public TagInfo getTagInfoAt(int row) {
            if (row < 0 || row >= filteredTags.size()) return null;
            return filteredTags.get(row);
        }
    }
    
    /**
     * Table model for the levels table (left panel).
     */
    private class LevelsTableModel extends AbstractTableModel {
        
        private final String[] columnNames = {
            resources.getString("column.visible"), 
            resources.getString("column.level"), 
            resources.getString("column.tags")
        };
        private List<Level> filteredLevels = new ArrayList<>();
        
        public void fireTableDataChanged() {
            updateFilteredLevels();
            super.fireTableDataChanged();
        }
        
        private void updateFilteredLevels() {
            filteredLevels.clear();
            
            for (Level level : home.getLevels()) {
                // Apply visibility filter
                boolean passesVisibilityFilter = 
                    visibilityFilter == VisibilityFilter.ALL ||
                    (visibilityFilter == VisibilityFilter.VISIBLE && level.isViewable()) ||
                    (visibilityFilter == VisibilityFilter.HIDDEN && !level.isViewable());
                
                if (!passesVisibilityFilter) continue;
                
                // Apply text filter
                if (!levelFilterText.isEmpty()) {
                    String levelName = level.getName().toLowerCase();
                    List<String> tags = tagsManager.getTagsForLevel(level);
                    String tagsText = String.join(" ", tags).toLowerCase();
                    
                    if (!levelName.contains(levelFilterText) && !tagsText.contains(levelFilterText)) {
                        continue;
                    }
                }
                
                filteredLevels.add(level);
            }
        }
        
        @Override
        public int getRowCount() {
            return filteredLevels.size();
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public Class<?> getColumnClass(int column) {
            return column == 0 ? Boolean.class : String.class;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Level level = filteredLevels.get(rowIndex);
            switch (columnIndex) {
                case 0: 
                    return level.isViewable();
                case 1: 
                    return level.getName();
                case 2: 
                    List<String> tags = tagsManager.getTagsForLevel(level);
                    return String.join(", ", tags);
                default: 
                    return "";
            }
        }
        
        public Level getFilteredLevelAt(int rowIndex) {
            return filteredLevels.get(rowIndex);
        }
    }
    
    /**
     * Table model for associated levels table (right panel).
     */
    private class AssociatedLevelsTableModel extends AbstractTableModel {
        
        private final String[] columnNames = {
            resources.getString("column.visible"), 
            resources.getString("column.level")
        };
        private List<Level> associatedLevels = new ArrayList<>();
        
        public void fireTableDataChanged() {
            updateAssociatedLevels();
            super.fireTableDataChanged();
        }
        
        private void updateAssociatedLevels() {
            associatedLevels.clear();
            
            List<TagInfo> selectedTags = getSelectedTags();
            if (selectedTags.isEmpty()) return;
            
            Set<Level> levels = new HashSet<>();
            
            if (unionModeRadio.isSelected()) {
                // Union mode
                for (TagInfo tag : selectedTags) {
                    levels.addAll(tagsManager.getLevelsWithTag(home, tag.name));
                }
            } else {
                // Intersection mode
                levels.addAll(tagsManager.getLevelsWithTag(home, selectedTags.get(0).name));
                for (int i = 1; i < selectedTags.size(); i++) {
                    levels.retainAll(tagsManager.getLevelsWithTag(home, selectedTags.get(i).name));
                }
            }
            
            associatedLevels = new ArrayList<>(levels);
            associatedLevels.sort(Comparator.comparing(Level::getName));
        }
        
        @Override
        public int getRowCount() {
            return associatedLevels.size();
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public Class<?> getColumnClass(int column) {
            return column == 0 ? Boolean.class : String.class;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Level level = associatedLevels.get(rowIndex);
            switch (columnIndex) {
                case 0: 
                    return level.isViewable();
                case 1: 
                    return level.getName();
                default: 
                    return "";
            }
        }
    }
    
    // Cell Renderers
    
    /**
     * Renderer for visibility column (eye icon).
     */
    private static class VisibilityCheckboxRenderer extends JCheckBox implements TableCellRenderer {
        public VisibilityCheckboxRenderer() {
            setHorizontalAlignment(JCheckBox.CENTER);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (value instanceof Boolean) {
                setSelected((Boolean) value);
            } else {
                setSelected(false);
            }
            
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            
            return this;
        }
    }
    
    /**
     * Renderer for tags column (pill-style tags).
     */
    private static class TagsRenderer extends JPanel implements TableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            removeAll();
            setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            
            String tagsText = value != null ? value.toString() : "";
            if (!tagsText.isEmpty()) {
                String[] tags = tagsText.split(", ");
                for (String tag : tags) {
                    JLabel tagLabel = new JLabel(tag);
                    tagLabel.setOpaque(true);
                    tagLabel.setBackground(new Color(220, 230, 255));
                    tagLabel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createRaisedBevelBorder(),
                        BorderFactory.createEmptyBorder(2, 6, 2, 6)
                    ));
                    tagLabel.setFont(tagLabel.getFont().deriveFont(Font.PLAIN, 11f));
                    add(tagLabel);
                }
            }
            
            return this;
        }
    }
    
}