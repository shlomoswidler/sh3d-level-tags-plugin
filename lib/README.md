# Dependencies

This directory contains the JAR files needed to compile and test the Level Tags Plugin.

## Required Files

To build the plugin, you need to place the following files in this directory:

### Sweet Home 3D Core JAR
- **File**: `SweetHome3D-7.5.jar` (or later version)
- **Source**: Built from Sweet Home 3D source code
- **Purpose**: Provides the core classes and plugin API

### Building Sweet Home 3D JAR

If you don't have the Sweet Home 3D JAR file, you can build it from source. See the Sweet Home 3D source repository for details.

### Alternative: Manual Installation

If you have Sweet Home 3D installed, you can find the JAR file in:

- **Windows**: `C:\Program Files\Sweet Home 3D\lib\SweetHome3D.jar`
- **macOS**: `/Applications/Sweet Home 3D.app/Contents/Java/SweetHome3D.jar`
- **Linux**: `/opt/sweethome3d/lib/SweetHome3D.jar` (or similar)

Copy this file to the `lib/` directory and rename it to match the expected version.

## Ant Build System

The Ant build system is configured to use JAR files from this directory. The `build.xml` includes:

```xml
<path id="compile.classpath">
    <fileset dir="${lib.dir}">
        <include name="*.jar"/>
    </fileset>
</path>
```

## Building the Plugin

To build the plugin with the dependencies in this directory:

```bash
# Make sure SweetHome3D-7.5.jar is in the lib/ directory
# Then build using Ant
ant build
```

The Ant build system will automatically find and include all JAR files in the `lib/` directory.

## File Structure

After adding dependencies, this directory should contain:

```
lib/
├── README.md (this file)
└── SweetHome3D-7.5.jar
```

## Troubleshooting

### Compilation Errors
If you get compilation errors about missing Sweet Home 3D classes:
1. Verify the JAR file is in the `lib/` directory
2. Check that the JAR file is not corrupted
3. Ensure you're using a compatible version (7.5 or later)

### Ant Build Issues
If Ant can't find the dependencies:
1. Verify the JAR file is in the `lib/` directory
2. Check that the JAR file name matches the expected version
3. Try cleaning and rebuilding: `ant clean build`

### Version Compatibility
The plugin requires Sweet Home 3D version 7.5 or later. Earlier versions may not have all the required plugin APIs.

## Legal Notes

The Sweet Home 3D JAR file is distributed under the GNU General Public License v2. Make sure you comply with the license terms when redistributing or modifying the code.

This plugin is also licensed under GPL v2 to maintain compatibility.