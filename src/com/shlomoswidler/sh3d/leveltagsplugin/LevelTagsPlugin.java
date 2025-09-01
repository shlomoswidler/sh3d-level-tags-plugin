/*
 * LevelTagsPlugin.java
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

import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.viewcontroller.HomeController;

/**
 * Main plugin class for the Level Tags Plugin.
 * Provides tagging functionality for Sweet Home 3D levels.
 * 
 * @author Level Tags Plugin Contributors
 */
public class LevelTagsPlugin extends Plugin {
    
    private LevelTagsManager tagsManager;
    private LevelTagsDialog dialog;
    
    /**
     * Called when the plugin is initialized.
     * Sets up the tags manager and prepares the plugin for use.
     */
    @Override
    public void init() {
        super.init();
        this.tagsManager = new LevelTagsManager();
    }
    
    /**
     * Called when the plugin is being destroyed.
     * Cleans up resources and closes any open dialogs.
     */
    @Override
    public void destroy() {
        if (this.dialog != null && this.dialog.isDisplayable()) {
            this.dialog.dispose();
            this.dialog = null;
        }
        this.tagsManager = null;
        super.destroy();
    }
    
    /**
     * Returns the actions available to the user through this plugin.
     * Currently provides one action: "Manage Level Tags"
     */
    @Override
    public PluginAction[] getActions() {
        return new PluginAction[] {
            new ManageLevelTagsAction()
        };
    }
    
    /**
     * Action that opens the Level Tags Manager dialog.
     */
    private class ManageLevelTagsAction extends PluginAction {
        
        public ManageLevelTagsAction() {
            super("LevelTagsPlugin", "MANAGE_LEVEL_TAGS", getPluginClassLoader());
        }
        
        @Override
        public void execute() {
            showLevelTagsDialog();
        }
        
        @Override
        public boolean isEnabled() {
            return getHome() != null && getHomeController() != null;
        }
    }
    
    /**
     * Shows the Level Tags Manager dialog.
     * Creates a new dialog if one doesn't exist, or brings the existing one to front.
     */
    private void showLevelTagsDialog() {
        Home home = getHome();
        HomeController homeController = getHomeController();
        
        if (home == null || homeController == null) {
            // Should not happen in normal operation, but guard against it
            return;
        }
        
        if (this.dialog == null || !this.dialog.isDisplayable()) {
            // Create new dialog
            this.dialog = new LevelTagsDialog(homeController, home, this.tagsManager);
        }
        
        // Show and bring to front
        this.dialog.setVisible(true);
        this.dialog.toFront();
        this.dialog.requestFocus();
    }
    
    /**
     * Returns the tags manager instance for this plugin.
     * Used by other components to access tag functionality.
     */
    public LevelTagsManager getTagsManager() {
        return this.tagsManager;
    }
}