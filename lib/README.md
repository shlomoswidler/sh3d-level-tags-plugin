# Dependencies

This directory contains the JAR files needed to compile and test the Level Tags Plugin.

## Required Files

To build the plugin, you need to place the following files in this directory:

### Sweet Home 3D Core JAR
- **File**: `SweetHome3D-7.5.jar` (or later version)
- **Source**: Built from Sweet Home 3D source code
- **Purpose**: Provides the core classes and plugin API

### Building Sweet Home 3D JAR

If you don't have the Sweet Home 3D JAR file, you can build it from source:

1. Navigate to the Sweet Home 3D source directory:
   ```bash
   cd ../SweetHome3D-7.5-src
   ```

2. Build the JAR file:
   ```bash
   ant jarExecutable
   ```

3. Copy the JAR to this directory:
   ```bash
   cp install/SweetHome3D-7.5.jar ../level-tags-plugin/lib/
   ```

### Alternative: Manual Installation

If you have Sweet Home 3D installed, you can find the JAR file in:

- **Windows**: `C:\Program Files\Sweet Home 3D\lib\SweetHome3D.jar`
- **macOS**: `/Applications/Sweet Home 3D.app/Contents/Java/SweetHome3D.jar`
- **Linux**: `/opt/sweethome3d/lib/SweetHome3D.jar` (or similar)

Copy this file to the `lib/` directory and rename it to match the expected version.

## Maven Local Repository

The Maven build is configured to look for dependencies in this directory as a local repository. The `pom.xml` includes:

```xml
<repositories>
    <repository>
        <id>local-repo</id>
        <url>file://${project.basedir}/lib</url>
    </repository>
</repositories>
```

## Installing Dependencies Manually

If you prefer to install the Sweet Home 3D dependency to your local Maven repository:

```bash
mvn install:install-file \
    -Dfile=lib/SweetHome3D-7.5.jar \
    -DgroupId=com.eteks \
    -DartifactId=sweethome3d \
    -Dversion=7.5 \
    -Dpackaging=jar
```

Then you can remove the local repository configuration from `pom.xml`.

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

### Maven Build Issues
If Maven can't find the dependencies:
1. Check the file path in the local repository configuration
2. Verify the JAR file name matches the Maven artifact version
3. Try clearing the Maven cache: `mvn clean`

### Version Compatibility
The plugin requires Sweet Home 3D version 7.5 or later. Earlier versions may not have all the required plugin APIs.

## Legal Notes

The Sweet Home 3D JAR file is distributed under the GNU General Public License v2. Make sure you comply with the license terms when redistributing or modifying the code.

This plugin is also licensed under GPL v2 to maintain compatibility.