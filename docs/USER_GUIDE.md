# Level Tags Plugin - User Guide

## Overview

The Level Tags Plugin extends Sweet Home 3D with a powerful tagging system for levels. This allows you to organize your levels using custom tags and perform bulk operations based on those tags.

## Getting Started

### Opening the Level Tags Manager

**Main Menu**: Go to **Tools** → **Level Tags Manager**

### Interface Overview

The Level Tags Manager dialog uses a split-pane layout with two main panels:

- **Left Panel (Levels)**: Shows all levels with their assigned tags, filtering controls, and level management buttons
- **Right Panel (Tags)**: Shows two sections:
  - **Tags Table**: Displays all tags with count (#) and associated level names
  - **Associated Levels**: Shows levels related to selected tags with Union/Intersection mode options

## Managing Tags

### Adding Tags to a Level

1. Select one or more levels in the left panel table
2. Type a tag name in the "Add tag" field at the bottom
3. Click **Add to Selected** button

### Removing Tags from a Level

**Method 1: Quick Remove (Left Panel)**
1. Select one or more levels in the left panel table
2. Select a tag from the "Remove tag" dropdown
3. Click **Remove from Selected** button

**Method 2: Remove All Tags**
1. Select one or more levels in the left panel table  
2. Click **Remove All Tags** button to remove all tags from selected levels

### Viewing Tags

#### Left Panel (Levels Table)
- Shows all levels in your home with three columns:
  - **Visible**: Checkbox showing/controlling level visibility
  - **Level**: Level name  
  - **Tags**: Comma-separated list of assigned tags
- Filter field allows searching by level name or tag
- Visibility filter dropdown: "All", "Visible only", "Hidden only"
- Selection buttons: "Select All" and "Select None"

#### Right Panel (Tags Table)
- Shows all tags currently in use with three columns:
  - **Tag**: Tag name
  - **#**: Number of levels with this tag  
  - **Levels**: Comma-separated list of level names using this tag
- Filter field allows searching by tag name
- Tag management buttons: "Rename", "Delete"

#### Associated Levels Section
- Shows levels associated with selected tags from the Tags table
- **Union mode**: Shows levels with ANY of the selected tags
- **Intersection mode**: Shows levels with ALL selected tags
- "Show All" and "Hide All" buttons for bulk visibility operations

## Bulk Operations

### Hiding/Showing Levels by Tag

The plugin provides powerful bulk operations to control level visibility:

#### From the Associated Levels Section:
1. Select one or more tags from the Tags table (right panel)
2. Choose Union or Intersection mode:
   - **Union**: Affects levels with ANY of the selected tags
   - **Intersection**: Affects levels with ALL selected tags
3. Click **Show All** or **Hide All** to control visibility

#### Individual Level Operations

**From the Levels Table:**
- Click the **Visible** checkbox to toggle individual level visibility
- Select levels and use visibility filter to show/hide groups

**From the Associated Levels Table:**
- Click the **Visible** checkbox for any level shown in the associated levels section

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
- **Quick tagging**: Use the left panel's quick add feature to tag levels immediately
- **Bulk visibility**: Use Union/Intersection modes to show/hide groups of related levels  
- **Project focus**: Filter by tags to work on specific building sections
- **Client presentations**: Hide construction/technical levels, show only finished spaces
- **Design phases**: Use tags to manage different project phases and toggle visibility
- **Real-time feedback**: Watch the status bar for confirmation of all operations

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

**Left Panel (Levels)**
- **Filter field**: Type to search levels by name or tag content
- **Show dropdown**: Filter by visibility status (All/Visible only/Hidden only)

**Right Panel (Tags)**  
- **Filter field**: Type to search tags by name
- **Tags table**: Automatically shows count and associated levels for each tag

### Status Bar

The status bar at the bottom shows:
- **Left side**: Operation feedback messages (e.g., "Added tag 'bedroom' to 3 level(s)")
- **Right side**: Plugin version number
- Messages automatically clear after 5 seconds