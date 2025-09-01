# Level Tags Plugin - API Reference

## Overview

This document describes the internal API and architecture of the Level Tags Plugin for developers who want to understand or extend the plugin.

## Core Classes

### LevelTagsPlugin

The main plugin class that extends Sweet Home 3D's `Plugin` class.

```java
public class LevelTagsPlugin extends Plugin {
    public PluginAction[] getActions()
    public void init()
    public void destroy()
}
```

**Methods:**
- `getActions()` - Returns the plugin actions available to the user
- `init()` - Called when plugin is loaded, sets up event handlers
- `destroy()` - Called when plugin is unloaded, cleans up resources

### LevelTagsManager

Core business logic class that handles all tag operations.

```java
public class LevelTagsManager {
    // Tag Management
    public void addTagsToLevel(Level level, List<String> tags)
    public void removeTagsFromLevel(Level level, List<String> tags)
    public List<String> getTagsForLevel(Level level)
    
    // Level Queries
    public List<Level> getLevelsWithTag(String tag)
    public Set<String> getAllTags(Home home)
    public Map<String, List<Level>> getTagToLevelsMap(Home home)
    
    // Visibility Operations
    public void hideLevelsWithTag(String tag)
    public void showLevelsWithTag(String tag) 
    public void hideLevel(Level level)
    public void showLevel(Level level)
    
    // Utility Methods
    public static List<String> parseTagString(String tagString)
    public static String formatTagString(List<String> tags)
    public boolean isValidTag(String tag)
}
```

### LevelTagsDialog

Main UI dialog with split-pane layout providing comprehensive tag management interface.

```java
public class LevelTagsDialog extends JDialog {
    // Constructor
    public LevelTagsDialog(HomeController homeController, Home home)
    
    // UI Setup
    private void createComponents()
    private void createLeftPanel()     // Levels view
    private void createRightPanel()    // Tags view with split sections
    private void layoutComponents()
    private void setupEventHandlers()
    
    // Table Models
    private LevelsTableModel levelsTableModel;
    private TagsTableModel tagsTableModel; 
    private AssociatedLevelsTableModel associatedLevelsTableModel;
    
    // Event Handlers
    private void setupLeftPanelHandlers()
    private void setupRightPanelHandlers()
    private void syncTagsSelection()
    private void syncLevelsSelection()
    
    // Data Operations
    private void addTagToSelectedLevels()
    private void removeTagFromSelectedLevels() 
    private void setVisibilityForTaggedLevels(boolean visible)
    
    // UI Updates
    private void refreshData()
    private void refreshLevelsTable()
    private void refreshTagsList()
    private void refreshAssociatedLevelsTable()
    private void showStatus(String message)
}
```

### TagInfo

Simple data model class representing tag information for UI display.

```java
public static class TagInfo {
    public final String name;
    public final int levelCount;
    
    public TagInfo(String name, int levelCount)
    
    @Override
    public String toString()  // Returns "name · (levelCount levels)"
}
```

### VisibilityFilter

Enum for filtering levels by visibility status.

```java
private enum VisibilityFilter {
    ALL("All"),
    VISIBLE("Visible only"), 
    HIDDEN("Hidden only");
    
    VisibilityFilter(String displayName)
    @Override
    public String toString()
}
```

## Data Storage Format

### Level Properties

Tags are stored as custom properties on `Level` objects using the following format:

```java
// Simple string format (current implementation)
level.setProperty("levelTags.tags", "tag1,tag2,tag3");

// Extended format (for future features)  
level.setProperty("levelTags.version", "1.0");
level.setProperty("levelTags.created", "2024-01-15T10:30:00Z");
level.setProperty("levelTags.modified", "2024-01-15T14:45:00Z");
```

### Property Keys

| Key | Type | Description |
|-----|------|-------------|
| `levelTags.tags` | String | Comma-separated list of tags |
| `levelTags.version` | String | Plugin version that created the tags |
| `levelTags.created` | String | ISO timestamp of tag creation |
| `levelTags.modified` | String | ISO timestamp of last modification |

## Event System

### Plugin Events

The plugin listens to Home events to maintain consistency:

```java
// Level changes
home.addPropertyChangeListener(Home.Property.LEVELS, new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent evt) {
        refreshTaggedLevels();
    }
});

// Selection changes  
home.addPropertyChangeListener(Home.Property.SELECTED_LEVEL, new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent evt) {
        updateSelectedLevelTags();
    }
});
```

### Custom Events

The plugin fires custom events for tag operations:

```java
public interface LevelTagsListener {
    void tagsAdded(Level level, List<String> tags);
    void tagsRemoved(Level level, List<String> tags);
    void levelVisibilityChanged(Level level, boolean visible);
}
```

## UI Components

### Custom Table Models

#### LevelsTableModel
```java
private class LevelsTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Visible", "Level", "Tags"};
    private List<Level> filteredLevels = new ArrayList<>();
    
    public int getColumnCount()  // Returns 3 (Visible, Level, Tags)
    public int getRowCount()     // Returns filteredLevels.size()
    public Object getValueAt(int row, int col)  // Col 0: Boolean, Col 1: String, Col 2: String
    public Class<?> getColumnClass(int column)  // Boolean for visibility, String for others
    public Level getFilteredLevelAt(int row)
    private void updateFilteredLevels()  // Applies level and visibility filters
}
```

#### TagsTableModel
```java  
private class TagsTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Tag", "#", "Levels"};
    private List<TagInfo> filteredTags = new ArrayList<>();
    
    public int getColumnCount()  // Returns 3 (Tag, Count, Levels)
    public int getRowCount()     // Returns filteredTags.size()
    public Object getValueAt(int row, int col)  // Col 0: String, Col 1: Integer, Col 2: String
    public Class<?> getColumnClass(int column)  // String, Integer, String
    public TagInfo getTagInfoAt(int row)
    private void updateFilteredTags()  // Applies tag name filter
}
```

#### AssociatedLevelsTableModel
```java
private class AssociatedLevelsTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Visible", "Level"};  
    private List<Level> associatedLevels = new ArrayList<>();
    
    public int getColumnCount()  // Returns 2 (Visible, Level)
    public int getRowCount()     // Returns associatedLevels.size()
    public Object getValueAt(int row, int col)  // Col 0: Boolean, Col 1: String
    public Class<?> getColumnClass(int column)  // Boolean, String
    private void updateAssociatedLevels()  // Updates based on selected tags and mode
}
```

### Custom Renderers

#### VisibilityCheckboxRenderer
```java
private static class VisibilityCheckboxRenderer extends JCheckBox implements TableCellRenderer {
    public VisibilityCheckboxRenderer()
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
}
```

Renders visibility column as:
- Centered checkboxes for boolean visibility state
- Proper selection highlighting
- Consistent checkbox behavior across tables

#### TagsRenderer  
```java
private static class TagsRenderer extends JPanel implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
}
```

Renders tags column with:
- Button-style tag display for comma-separated tag strings
- Proper text wrapping and spacing
- Selection highlighting support

## UI Layout and Features

### Split Pane Architecture

```java
// Main horizontal split pane
private JSplitPane splitPane;
splitPane.setDividerLocation(750);  // 750px left panel width
splitPane.setResizeWeight(0.65);    // 65/35 split ratio

// Right panel vertical split pane  
private JSplitPane rightSplitPane;
rightSplitPane.setDividerLocation(250);  // Tags table height
rightSplitPane.setResizeWeight(0.4);     // 40% for tags table
```

### Panel Minimum Sizes
```java
leftPanel.setMinimumSize(new Dimension(550, 0));   // Ensures buttons visible
rightPanel.setMinimumSize(new Dimension(300, 0));  // Maintains functionality
```

### Union/Intersection Mode
```java
private JRadioButton unionModeRadio;        // Show levels with ANY selected tags
private JRadioButton intersectionModeRadio; // Show levels with ALL selected tags

// Mode selection affects Associated Levels table content
private void refreshAssociatedLevelsTable() {
    if (unionModeRadio.isSelected()) {
        // Union: levels with any selected tags
    } else {
        // Intersection: levels with all selected tags  
    }
}
```

### Status Bar with Version
```java
private JPanel statusPanel;      // BorderLayout container
private JLabel statusLabel;      // Left side: operation messages
private JLabel versionLabel;     // Right side: "v1.0.5"

private void showStatus(String message) {
    statusLabel.setText(message);
    // Auto-clear after 5 seconds with Timer
}
```

### Filter System
```java
private String levelFilterText = "";  // Left panel filter
private String tagFilterText = "";    // Right panel filter
private VisibilityFilter visibilityFilter = VisibilityFilter.ALL;

// Filters applied in table model updateFiltered*() methods
private void updateLevelFilter()  // Searches level names and tag content
private void updateTagFilter()    // Searches tag names
```

## Extension Points

### Adding Custom Tag Types

To add support for custom tag types:

```java
public enum TagType {
    GENERAL,
    FUNCTIONAL, 
    SPATIAL,
    TEMPORAL,
    CUSTOM
}

public class TypedTag {
    private String name;
    private TagType type;
    private Map<String, String> properties;
}
```

### Custom Tag Operations

Implement the `TagOperation` interface:

```java
public interface TagOperation {
    String getName();
    String getDescription(); 
    boolean isApplicable(List<Level> levels);
    void execute(List<Level> levels, String tag);
}

// Example: Export levels with specific tag
public class ExportTaggedLevelsOperation implements TagOperation {
    public void execute(List<Level> levels, String tag) {
        // Export logic here
    }
}
```

### Custom UI Panels

Extend the dialog with custom panels:

```java
public abstract class TagsPanel extends JPanel {
    protected LevelTagsManager tagsManager;
    protected Home home;
    
    public abstract void refresh();
    public abstract String getPanelName();
}

// Example: Statistics panel
public class TagStatisticsPanel extends TagsPanel {
    // Shows tag usage statistics, charts, etc.
}
```

## Configuration

### Plugin Settings

Settings are stored in user preferences:

```java
UserPreferences prefs = getHome().getUserPreferences();

// UI preferences
prefs.setProperty("levelTags.dialog.width", "800");
prefs.setProperty("levelTags.dialog.height", "600");
prefs.setProperty("levelTags.showTagCount", "true");

// Behavior preferences
prefs.setProperty("levelTags.autoSave", "true");
prefs.setProperty("levelTags.confirmBulkOperations", "true");
```

### Available Settings

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `levelTags.dialog.width` | int | 800 | Dialog width in pixels |
| `levelTags.dialog.height` | int | 600 | Dialog height in pixels |
| `levelTags.showTagCount` | boolean | true | Show level count for each tag |
| `levelTags.autoSave` | boolean | true | Auto-save tags when modified |
| `levelTags.confirmBulkOperations` | boolean | true | Confirm bulk hide/show operations |
| `levelTags.maxRecentTags` | int | 10 | Number of recent tags to remember |

## Error Handling

### Exception Classes

```java
public class LevelTagsException extends Exception {
    public LevelTagsException(String message)
    public LevelTagsException(String message, Throwable cause)
}

public class InvalidTagException extends LevelTagsException {
    private String invalidTag;
    public InvalidTagException(String tag, String reason)
}

public class TagOperationException extends LevelTagsException {
    private String operation;
    private Level level;
    public TagOperationException(String operation, Level level, String message)
}
```

### Error Recovery

The plugin implements graceful error recovery:

1. **Invalid tag data**: Skips corrupted tags, logs warning
2. **Level not found**: Removes references to deleted levels  
3. **Permission errors**: Shows user-friendly error messages
4. **Plugin conflicts**: Detects and handles conflicts with other plugins

## Testing

### Unit Tests

Key test classes:

```java
public class LevelTagsManagerTest {
    @Test public void testAddTagsToLevel()
    @Test public void testRemoveTagsFromLevel() 
    @Test public void testGetLevelsWithTag()
    @Test public void testBulkVisibilityOperations()
}

public class TagDataTest {
    @Test public void testTagSerialization()
    @Test public void testTagValidation()
    @Test public void testTagParsing()
}
```

### Integration Tests

```java
public class PluginIntegrationTest {
    @Test public void testPluginLoading()
    @Test public void testUIIntegration()
    @Test public void testDataPersistence() 
}
```

## Performance Considerations

### Optimization Strategies

1. **Lazy Loading**: Tag data loaded on-demand
2. **Caching**: Recently accessed tags cached in memory
3. **Batch Operations**: Multiple tag changes processed together
4. **Event Debouncing**: UI updates debounced to prevent excessive refreshes

### Memory Usage

- Tag strings are interned to reduce memory usage
- WeakReferences used for event listeners to prevent memory leaks
- UI components disposed properly when dialog closes

### Scalability

The plugin is designed to handle:
- Up to 1000 levels per home
- Up to 100 unique tags per home
- Up to 50 tags per level

## Building and Deployment

### Build Process

1. Compile against Sweet Home 3D JAR, placed in the lib/ directory. Use `ant` to build.
2. Package resources and classes
3. Generate plugin JAR with correct manifest
4. Sign JAR for distribution (optional)

### Dependencies

- Sweet Home 3D 7.5+ (required)
- Java 8+ runtime

### Deployment

Plugin JAR should be placed in user's plugins directory and will be loaded automatically on Sweet Home 3D startup.