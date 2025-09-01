# Level Tags Plugin - User Guide

## Overview

The Level Tags Plugin extends Sweet Home 3D with a powerful tagging system for levels. This allows you to organize your levels using custom tags and perform bulk operations based on those tags.

## Getting Started

### Opening the Level Tags Manager

**Main Menu**: Go to **Tools** → **Level Tags Manager**

### Interface Overview

The Level Tags Manager dialog has two main tabs:

- **Levels Tab**: Shows all levels with their assigned tags
- **Tags Tab**: Shows all tags with the levels that use them

## Managing Tags

### Adding Tags to a Level

1. Select a level in the Levels tab
2. Click the **Add Tags...** button
3. In the dialog that appears:
   - Current tags for the level are shown at the top
   - Enter new tags in the text field (separate multiple tags with commas)
   - Select from existing tags by clicking on them
4. Click **Add** to apply the new tags

**Example**: To add tags "residential", "ground floor", and "public" to a level, enter:
```
residential, ground floor, public
```

### Removing Tags from a Level

1. Select a level in the Levels tab
2. The level's current tags will be highlighted
3. Select the tags you want to remove
4. Click **Remove Tags**

### Viewing Tags

#### Levels Tab
- Shows all levels in your home
- Each level displays its assigned tags as colored labels
- Empty levels (no tags) are clearly indicated

#### Tags Tab  
- Shows all tags currently in use
- Each tag shows how many levels use it
- Click on a tag to see which levels have that tag

## Bulk Operations

### Hiding/Showing Levels by Tag

The plugin provides powerful bulk operations to control level visibility:

#### From the Tags Tab:
1. Select a tag from the list
2. Click **Hide All with Tag** to hide all levels with that tag
3. Click **Show All with Tag** to make all levels with that tag visible

#### From the Context Menu:
1. Right-click on any tag (in either tab)
2. Choose from the context menu:
   - **Hide All Levels with This Tag**
   - **Show All Levels with This Tag**

### Individual Level Operations

You can also control individual levels:

1. Select a level in the Levels tab
2. Click **Hide Level** or **Show Level**
3. Or right-click the level for context menu options

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

## Data Storage

Tags are stored as custom properties on each Level object and are automatically saved with your Sweet Home 3D file. This means:

- Tags persist when you save and reload your project
- Tags are included when you share your .sh3d files
- The plugin data is forward-compatible with future Sweet Home 3D versions
- Removing the plugin won't corrupt your files (tags just won't be displayed)

## Troubleshooting

### Tags Not Appearing
- Ensure the plugin is properly installed and enabled
- Check that you're using Sweet Home 3D version 7.5 or later
- Restart Sweet Home 3D if you just installed the plugin

### Performance with Many Levels
- The plugin is optimized for homes with hundreds of levels
- If performance is slow, try closing and reopening the dialog
- Consider using more specific tags to reduce the number of levels per tag

### Tags Not Saving
- Tags are automatically saved with your home file
- Ensure you save your project after adding tags
- Check file permissions if save operations fail

## Advanced Features

### Tag Validation
- Duplicate tags are automatically prevented
- Empty tags are ignored
- Tag names are automatically trimmed of whitespace

### Bulk Selection
- Hold Ctrl (Cmd on Mac) to select multiple levels
- Hold Shift to select a range of levels
- Use Ctrl+A (Cmd+A) to select all levels

### Search and Filter
- Type in the tag field to filter existing tags
- Use the search functionality to quickly find levels with specific tags