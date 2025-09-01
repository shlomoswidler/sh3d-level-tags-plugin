# Changelog

All notable changes to the Level Tags Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned Features
- Context menu integration (pending Sweet Home 3D API changes)
- Keyboard shortcuts for common operations (pending Sweet Home 3D support)
- Localization to additional languages

## [1.0.5] - 2025-09-01

### Changed
- **BREAKING UI**: Converted Tags list to full table format for better information display
- Redesigned single-window interface with horizontal split pane layout
- Status bar with message on left and version on right
- Enhanced Tags table with dedicated columns:
  - Tag name column
  - Count column (#) showing number of levels with each tag
  - Levels column showing comma-separated list of level names
- Checkbox-based visibility indicators in all tables
- Updated docs to describe how to use new UI

### Build/Code Changes
- Use Resource Bundle (properties file) instead of hardcoded display stringa
- Cleaned up strings in properties file
- Updated docs to describe how to build

## [1.0.0] - 2025-08-31

### Added
- Core level tagging functionality
- Level Tags Manager dialog with dual-tab interface
- Add/remove tags from levels
- Bulk hide/show operations based on tags
- Persistent tag storage in .sh3d files
- Localized UI strings (English)
- Keyboard shortcuts (Ctrl+Shift+T to open dialog)
- Tag validation and duplicate prevention
- Support for comma-separated tag input
- Visual indicators for level visibility state
- Comprehensive user documentation
- Developer API documentation

### Technical Features
- Integration with Sweet Home 3D plugin architecture
- Custom properties storage for tag persistence
- Event-driven UI updates
- Memory-efficient tag management
- Support for homes with 1000+ levels
- Graceful error handling and recovery
- Cross-platform compatibility (Windows, macOS, Linux)

### UI Components
- Main Level Tags Manager dialog
- Add Tags dialog with existing tag suggestions
- Levels table with tag display
- Tags table with level count display
- Confirmation dialogs for bulk operations
- Status messages and user feedback

### Infrastructure
- Ant-compatible project structure
- Automated build system
- Code documentation
- User guide and tutorials
- API reference documentation

## Development Notes

### Version 1.0.0 Development Milestones

#### Phase 1: Core Infrastructure ✓
- [x] Project structure setup
- [x] Plugin manifest and metadata
- [x] Basic documentation framework
- [x] Main plugin class implementation
- [x] Tag data model and storage

#### Phase 2: Core Functionality
- [x] Level tag management (add/remove)
- [x] Tag persistence in Level properties
- [x] Basic UI framework
- [x] Level and tag query operations

#### Phase 3: User Interface
- [x] Main Level Tags Manager dialog
- [x] Add Tags dialog
- [x] Table models and renderers  
- [x] Event handling and UI updates

#### Phase 4: Advanced Features
- [x] Bulk visibility operations
- [ ] Tag validation and error handling

#### Phase 5: Polish and Testing
- [x] Documentation completion
- [ ] Localization support
- [ ] Final bug fixes and stability

### Known Limitations

#### Version 1.0.0
- No direct context menu integration on level tabs (Sweet Home 3D API limitation)
- Tags limited to String type only (no rich metadata)
- No built-in tag categories or hierarchies
- Single-level undo/redo (not integrated with Sweet Home 3D's undo system)
- No tag import/export functionality
- No visual customization of tags (colors, icons)

#### Sweet Home 3D Integration Constraints
- Plugin actions only appear in main menu/toolbar
- Cannot extend existing context menus
- Limited access to internal UI components
- No hooks for custom level tab rendering
- Must work within Java Swing UI framework

### Future Enhancement Ideas

#### Version 1.1.0 (Planned)
- Additional localization (French, German, Spanish)

#### Version 1.2.0 (Planned)  
- Integration with level copying operations

### Architecture Decisions

#### Data Storage Strategy
**Decision**: Store tags as comma-separated strings in Level custom properties
**Rationale**: 
- Ensures compatibility with Sweet Home 3D file format
- No external dependencies or complex serialization
- Forward-compatible with future versions
- Survives plugin removal

**Alternatives Considered**:
- Separate XML file (rejected: sync issues, file management complexity)
- Binary serialization (rejected: version compatibility issues)  
- Database storage (rejected: deployment complexity)

#### UI Framework Approach
**Decision**: Custom Swing dialogs launched from main menu
**Rationale**:
- Works within Sweet Home 3D plugin architecture constraints
- Provides full control over UI layout and behavior
- Familiar Swing patterns for Sweet Home 3D users
- Allows rich interaction models

**Alternatives Considered**:
- Context menu integration (rejected: API limitations)
- Toolbar-only interface (rejected: too limited)
- Web-based UI (rejected: complexity, security concerns)

#### Tag Storage Format
**Decision**: Simple comma-separated string format
**Rationale**:
- Human-readable in property inspection
- Easy to parse and manipulate
- Minimal storage overhead
- Compatible with CSV export/import

**Alternatives Considered**:
- JSON format (rejected: overkill for simple strings)
- XML format (rejected: verbose, parsing overhead)
- Binary format (rejected: not human-readable)

### Testing Strategy

#### Unit Testing
- Tag parsing and validation logic
- Data model operations (add, remove, query)
- Bulk operation algorithms
- Error handling and edge cases

#### Integration Testing  
- Plugin loading and initialization
- Sweet Home 3D API integration
- File persistence across save/load cycles
- UI component interactions

#### User Acceptance Testing
- Workflow validation with real home files
- Performance testing with large numbers of levels
- Cross-platform compatibility verification
- Accessibility and usability testing

### Release Process

1. **Development** - Feature implementation and unit testing
2. **Alpha Testing** - Internal testing with development team
3. **Beta Testing** - Limited release to volunteer users
4. **Release Candidate** - Feature-complete version for final testing
5. **Release** - Public release with full documentation
6. **Post-Release** - Bug fixes and patch releases as needed