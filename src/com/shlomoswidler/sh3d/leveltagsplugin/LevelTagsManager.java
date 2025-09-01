/*
 * LevelTagsManager.java
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

import java.util.*;

/**
 * Core business logic for managing level tags.
 * Handles all tag operations including adding, removing, querying, and bulk operations.
 * 
 * @author Level Tags Plugin Contributors
 */
public class LevelTagsManager {
    
    /** Property key used to store tags in Level custom properties */
    public static final String TAGS_PROPERTY_KEY = "levelTags.tags";
    
    /** Separator used between tags in the stored string */
    public static final String TAG_SEPARATOR = ",";
    
    /**
     * Creates a new LevelTagsManager.
     */
    public LevelTagsManager() {
    }
    
    /**
     * Adds one or more tags to a level.
     * Existing tags are preserved, and duplicate tags are automatically prevented.
     * 
     * @param level the level to add tags to
     * @param tags the tags to add (will be trimmed and validated)
     */
    public void addTagsToLevel(Level level, List<String> tags) {
        if (level == null || tags == null || tags.isEmpty()) {
            return;
        }
        
        Set<String> currentTags = new LinkedHashSet<>(getTagsForLevel(level));
        
        // Add new tags (LinkedHashSet preserves order and prevents duplicates)
        for (String tag : tags) {
            String cleanTag = cleanTag(tag);
            if (isValidTag(cleanTag)) {
                currentTags.add(cleanTag);
            }
        }
        
        // Store the updated tags
        setTagsForLevel(level, new ArrayList<>(currentTags));
    }
    
    /**
     * Removes one or more tags from a level.
     * 
     * @param level the level to remove tags from
     * @param tags the tags to remove
     */
    public void removeTagsFromLevel(Level level, List<String> tags) {
        if (level == null || tags == null || tags.isEmpty()) {
            return;
        }
        
        List<String> currentTags = getTagsForLevel(level);
        Set<String> tagsToRemove = new HashSet<>();
        
        // Clean the tags to remove for consistent matching
        for (String tag : tags) {
            String cleanTag = cleanTag(tag);
            if (!cleanTag.isEmpty()) {
                tagsToRemove.add(cleanTag);
            }
        }
        
        // Remove the specified tags
        currentTags.removeAll(tagsToRemove);
        
        // Store the updated tags
        setTagsForLevel(level, currentTags);
    }
    
    /**
     * Gets all tags assigned to a level.
     * 
     * @param level the level to get tags for
     * @return list of tags (empty list if no tags)
     */
    public List<String> getTagsForLevel(Level level) {
        if (level == null) {
            return new ArrayList<>();
        }
        
        String tagsString = level.getProperty(TAGS_PROPERTY_KEY);
        return parseTagString(tagsString);
    }
    
    /**
     * Sets the tags for a level, replacing any existing tags.
     * 
     * @param level the level to set tags for
     * @param tags the tags to set (null or empty list removes all tags)
     */
    public void setTagsForLevel(Level level, List<String> tags) {
        if (level == null) {
            return;
        }
        
        if (tags == null || tags.isEmpty()) {
            // Remove the tags property
            level.setProperty(TAGS_PROPERTY_KEY, null);
        } else {
            // Store as comma-separated string
            String tagsString = formatTagString(tags);
            level.setProperty(TAGS_PROPERTY_KEY, tagsString);
        }
    }
    
    /**
     * Gets all levels in the home that have the specified tag.
     * 
     * @param home the home to search
     * @param tag the tag to search for
     * @return list of levels with the tag (empty list if none found)
     */
    public List<Level> getLevelsWithTag(Home home, String tag) {
        if (home == null || tag == null) {
            return new ArrayList<>();
        }
        
        String cleanTag = cleanTag(tag);
        if (cleanTag.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Level> matchingLevels = new ArrayList<>();
        
        for (Level level : home.getLevels()) {
            if (levelHasTag(level, cleanTag)) {
                matchingLevels.add(level);
            }
        }
        
        return matchingLevels;
    }
    
    /**
     * Gets all unique tags used across all levels in the home.
     * 
     * @param home the home to scan for tags
     * @return set of all unique tags (empty set if no tags)
     */
    public Set<String> getAllTags(Home home) {
        if (home == null) {
            return new HashSet<>();
        }
        
        Set<String> allTags = new HashSet<>();
        
        for (Level level : home.getLevels()) {
            allTags.addAll(getTagsForLevel(level));
        }
        
        return allTags;
    }
    
    /**
     * Gets a map of tags to the levels that use them.
     * 
     * @param home the home to analyze
     * @return map where keys are tag names and values are lists of levels using that tag
     */
    public Map<String, List<Level>> getTagToLevelsMap(Home home) {
        if (home == null) {
            return new HashMap<>();
        }
        
        Map<String, List<Level>> tagToLevels = new HashMap<>();
        
        for (Level level : home.getLevels()) {
            List<String> tags = getTagsForLevel(level);
            for (String tag : tags) {
                tagToLevels.computeIfAbsent(tag, k -> new ArrayList<>()).add(level);
            }
        }
        
        return tagToLevels;
    }
    
    /**
     * Checks if a level has a specific tag.
     * 
     * @param level the level to check
     * @param tag the tag to look for
     * @return true if the level has the tag, false otherwise
     */
    public boolean levelHasTag(Level level, String tag) {
        if (level == null || tag == null) {
            return false;
        }
        
        String cleanTag = cleanTag(tag);
        return getTagsForLevel(level).contains(cleanTag);
    }
    
    /**
     * Hides all levels that have the specified tag.
     * 
     * @param home the home containing the levels
     * @param tag the tag to match
     * @return number of levels that were hidden
     */
    public int hideLevelsWithTag(Home home, String tag) {
        List<Level> levels = getLevelsWithTag(home, tag);
        int hiddenCount = 0;
        
        for (Level level : levels) {
            if (level.isViewable()) {
                level.setViewable(false);
                hiddenCount++;
            }
        }
        
        return hiddenCount;
    }
    
    /**
     * Shows (makes viewable) all levels that have the specified tag.
     * 
     * @param home the home containing the levels
     * @param tag the tag to match
     * @return number of levels that were made viewable
     */
    public int showLevelsWithTag(Home home, String tag) {
        List<Level> levels = getLevelsWithTag(home, tag);
        int shownCount = 0;
        
        for (Level level : levels) {
            if (!level.isViewable()) {
                level.setViewable(true);
                shownCount++;
            }
        }
        
        return shownCount;
    }
    
    /**
     * Hides a single level.
     * 
     * @param level the level to hide
     * @return true if the level was hidden, false if it was already hidden
     */
    public boolean hideLevel(Level level) {
        if (level != null && level.isViewable()) {
            level.setViewable(false);
            return true;
        }
        return false;
    }
    
    /**
     * Shows (makes viewable) a single level.
     * 
     * @param level the level to show
     * @return true if the level was shown, false if it was already visible
     */
    public boolean showLevel(Level level) {
        if (level != null && !level.isViewable()) {
            level.setViewable(true);
            return true;
        }
        return false;
    }
    
    /**
     * Parses a comma-separated string of tags into a list.
     * 
     * @param tagString the string to parse (can be null or empty)
     * @return list of cleaned tags (empty list if input was null/empty)
     */
    public static List<String> parseTagString(String tagString) {
        if (tagString == null || tagString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String[] tagArray = tagString.split(TAG_SEPARATOR);
        List<String> tags = new ArrayList<>();
        
        for (String tag : tagArray) {
            String cleanTag = cleanTag(tag);
            if (isValidTag(cleanTag)) {
                tags.add(cleanTag);
            }
        }
        
        return tags;
    }
    
    /**
     * Formats a list of tags into a comma-separated string for storage.
     * 
     * @param tags the tags to format
     * @return comma-separated string, or null if tags is null/empty
     */
    public static String formatTagString(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        
        List<String> cleanTags = new ArrayList<>();
        for (String tag : tags) {
            String cleanTag = cleanTag(tag);
            if (isValidTag(cleanTag)) {
                cleanTags.add(cleanTag);
            }
        }
        
        if (cleanTags.isEmpty()) {
            return null;
        }
        
        return String.join(TAG_SEPARATOR, cleanTags);
    }
    
    /**
     * Cleans a tag by trimming whitespace and converting to lowercase.
     * 
     * @param tag the tag to clean
     * @return cleaned tag, or empty string if input was null
     */
    private static String cleanTag(String tag) {
        if (tag == null) {
            return "";
        }
        return tag.trim().toLowerCase();
    }
    
    /**
     * Validates that a tag is acceptable.
     * Currently just checks that it's not empty and doesn't contain the separator.
     * 
     * @param tag the tag to validate (should already be cleaned)
     * @return true if the tag is valid, false otherwise
     */
    public static boolean isValidTag(String tag) {
        if (tag == null || tag.isEmpty()) {
            return false;
        }
        
        // Tag cannot contain the separator character
        if (tag.contains(TAG_SEPARATOR)) {
            return false;
        }
        
        // Tag cannot be just whitespace
        if (tag.trim().isEmpty()) {
            return false;
        }
        
        return true;
    }
}