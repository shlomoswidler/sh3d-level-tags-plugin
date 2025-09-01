# Sweet Home 3D Level Tags Plugin

A plugin for Sweet Home 3D that adds tagging functionality to levels, allowing users to organize, manage, and control level visibility through a flexible tagging system.

## Features

- **Level Tagging**: Add multiple text tags to any level
- **Tag Management**: Add, remove, and edit tags through an intuitive interface  
- **Bulk Operations**: Hide/show multiple levels based on shared tags
- **Level Organization**: View all levels organized by their tags
- **Persistent Storage**: Tags are saved with the home file and persist across sessions

## Installation

### macOS
1. Download or build the plugin JAR file (`LevelTagsPlugin.jar`)
2. Rename the JAR file to have a `.sh3p` extension (`LevelTagsPlugin.sh3p`)
3. Double-click the `.sh3p` file to import it into Sweet Home 3D
4. Quit and relaunch Sweet Home 3D for the plugin to become active
5. The plugin will appear in the **Tools** menu as "Manage Level Tags"

### Windows & Linux
1. Download the plugin JAR file from the releases section
2. Place the JAR file in your Sweet Home 3D plugins directory:
   - **Windows**: `%APPDATA%\eTeks\Sweet Home 3D\plugins\`
   - **Linux**: `~/.eteks/sweethome3d/plugins/`
3. Restart Sweet Home 3D
4. The plugin will appear in the **Tools** menu as "Manage Level Tags"

## Usage

### Opening the Level Tags Manager
- Go to **Tools** → **Level Tags Manager** in the main menu

### Managing Tags
- **Add Tags**: Select levels and use the "Add tag" field or "Add Tags..." button
- **Remove Tags**: Select levels and use the "Remove tag" dropdown or "Remove All Tags" button  
- **View by Tags**: Use the Tags table showing tag names, counts, and associated levels
- **Bulk Operations**: Select tags and use "Show All"/"Hide All" buttons with Union/Intersection modes

### Tag Operations
- **Union Mode**: Show/hide levels with ANY of the selected tags
- **Intersection Mode**: Show/hide levels with ALL selected tags
- **Visibility Control**: Click checkboxes in the Visible column to toggle individual levels
- **Filter Levels**: Use filter fields to search levels by name or tag content

## Practical Use Cases

### Organizing by Function
Tag levels by their purpose:
- `residential` - Living spaces
- `commercial` - Business areas  
- `mechanical` - Utility areas
- `storage` - Storage spaces

### Organizing by Floor
Tag levels by floor number:
- `ground`, `first`, `second`, `basement`
- `floor-1`, `floor-2`, `floor-3`

### Organizing by Access Level
Tag levels by who can access them:
- `public` - Open to everyone
- `private` - Restricted access
- `staff-only` - Employee areas
- `executive` - Management areas

### Project Phases
Tag levels by construction or design phase:
- `phase-1`, `phase-2`, `phase-3`
- `existing`, `proposed`, `future`
- `demo`, `new-construction`

## Tips and Best Practices

### Tag Naming
- Use descriptive, consistent names
- Avoid spaces if possible (use hyphens or underscores)
- Consider using a naming convention like `category-value`
- Keep tag names short but meaningful

### Organization Strategies
- Create a tag hierarchy: `building-north`, `building-south`
- Use status tags: `complete`, `in-progress`, `planned`
- Combine functional and spatial tags: `residential-tower-a`

### Workflow Integration
- Tag levels as you create them
- Use bulk operations to focus on specific areas during design
- Hide irrelevant levels to reduce visual complexity
- Show only relevant levels when presenting to clients

## Architecture

The plugin uses Sweet Home 3D's extensible data model by storing tags as custom properties on `Level` objects:

```java
level.setProperty("levelTags.tags", "residential,floor1,private");
```

This ensures compatibility with the native file format and persistence across save/load cycles.

## Development

### Prerequisites
- Java 8 or higher
- Sweet Home 3D 7.5+ JAR file (for compilation)
- Apache Ant (for build automation)

### Building
```bash
# Copy the Sweet Home 3D JAR file to the lib directory
cp /path/to/SweetHome3D-7.5.jar lib/

# Build the plugin using Ant
ant build

# This creates both LevelTagsPlugin.jar and LevelTagsPlugin.sh3p
```

### Project Structure
```
sh3d-level-tags-plugin/
├── src/com/shlomoswidler/sh3d/leveltagsplugin/
│   ├── LevelTagsPlugin.java          # Main plugin class
│   ├── LevelTagsManager.java         # Core tag management logic
│   ├── LevelTagsDialog.java          # Main UI dialog with split-pane layout
│   ├── AddTagsDialog.java            # Add tags dialog
│   └── RemoveTagsDialog.java         # Remove tags dialog
├── src/
│   ├── ApplicationPlugin.properties  # Plugin metadata
│   └── LevelTagsPlugin.properties    # Localized strings
├── docs/
│   ├── USER_GUIDE.md                 # Detailed user guide
│   ├── API_REFERENCE.md              # Developer API reference
│   └── CHANGELOG.md                  # Version history
├── lib/                              # External dependencies (place SweetHome3D-7.5.jar here before building)
├── build.xml                         # Ant build configuration
└── build/                            # Build output directory
```

## License

This plugin is distributed under the GNU General Public License v2, consistent with Sweet Home 3D's licensing.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable  
5. Submit a pull request

## Support

For bug reports and feature requests, please open an issue in the project repository.