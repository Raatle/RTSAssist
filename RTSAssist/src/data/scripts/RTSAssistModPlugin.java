/****************************************************************************************
 * RTSAssist version 0.1.0
 * Copyright (C) 2025, Raatle

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 ****************************************************************************************/

package data.scripts;

import org.json.JSONObject;
import java.util.HashMap;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

public class RTSAssistModPlugin extends BaseModPlugin {
    
    HashMap<String, Object> hotKeys;
    HashMap<String, Object> config;
    HashMap<String, Object> blockedSystems;
    boolean isEnabled;
    boolean uninstall;

    @Override
    public void onApplicationLoad() throws Exception {

        final JSONObject hotKeyFile = Global.getSettings().loadJSON("Hotkeys.ini");
        this.hotKeys = new HashMap<String, Object>() {{
            put("enable_disable", hotKeyFile.getString("enable_disable").charAt(0));
            put("saveLayout", hotKeyFile.getString("saveLayout").charAt(0));
            put("loadLayout", hotKeyFile.getString("loadLayout").charAt(0));
            put("deleteAssignments", hotKeyFile.getString("deleteAssignments").charAt(0));
            put("vent", hotKeyFile.getString("vent").charAt(0));
        }};

        final JSONObject configFile = Global.getSettings().loadJSON("Config.ini");
        this.config = new HashMap<String, Object>() {{
            put("scrollSpeed", (float)configFile.getDouble("scrollSpeed"));
            put("maxZoom", (float)configFile.getDouble("maxZoom"));
        }};

        this.blockedSystems = new HashMap<>();
        this.isEnabled = (boolean)configFile.get("enabled");
        this.uninstall = (boolean)configFile.get("uninstall");
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (this.uninstall) {
            if (Global.getSector() != null) {
                if (Global.getSector().getMemory().get("$RTSASSIST_saveData") == null)
                    throw new RuntimeException("RTSASSIST: no saveData to remove, disable mod or change _uninstall_ param to false in the Config.ini.");
                Global.getSector().getMemory().unset("$RTSASSIST_saveData");
                throw new RuntimeException("RTSASSIST: saveData removed, disable mod or change _uninstall_ param to false in the Config.ini.");
            }
            throw new RuntimeException("RTSASSIST: Uninstall failed?");
        }
        if (Global.getSector() != null) {
            if (Global.getSector().getMemory().get("$RTSASSIST_saveData") == null)
                Global.getSector().getMemory().set("$RTSASSIST_saveData", new HashMap<String, Object>());
            HashMap<String, Object> mem = (HashMap<String, Object>)Global.getSector().getMemory().get("$RTSASSIST_saveData");
            mem.put("hotKeys", this.hotKeys);
            mem.put("config", this.config);
            mem.put("isEnabled", this.isEnabled);
            mem.put("blockedSystems", this.blockedSystems);
            Global.getSector().getMemory().set("$RTSASSIST_saveData", mem);
        }
    }
}

