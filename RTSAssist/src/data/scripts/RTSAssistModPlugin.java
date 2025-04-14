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

    boolean inDevelopment = false;
    
    HashMap<String, Object> hotKeys;
    HashMap<String, Object> config;
    HashMap<String, Object> blockedSystems;
    String version;
    boolean isEnabled;
    boolean uninstall;

    @Override
    public void onApplicationLoad() throws Exception {

        final JSONObject hotKeyFile = Global.getSettings().loadJSON("Hotkeys.ini");
        this.hotKeys = new HashMap<String, Object>() {{
            if (hotKeyFile.getString("enable_RTSMode").isEmpty())
                put("enable_RTSMode", null);
            else
                put("enable_RTSMode", hotKeyFile.getString("enable_disable").charAt(0));
//            put("strafeCameraLeft", hotKeyFile.getString("strafeCameraLeft").charAt(0));
//            put("strafeCameraRight", hotKeyFile.getString("strafeCameraRight").charAt(0));
//            put("strafeCameraUp", hotKeyFile.getString("strafeCameraUp").charAt(0));
//            put("strafeCameraDown", hotKeyFile.getString("strafeCameraDown").charAt(0));
            put("saveLayout", hotKeyFile.getString("saveLayout").charAt(0));
            put("loadLayout", hotKeyFile.getString("loadLayout").charAt(0));
            put("deleteAssignments", hotKeyFile.getString("deleteAssignments").charAt(0));
            put("vent", hotKeyFile.getString("vent").charAt(0));
        }};
        if (this.inDevelopment) {
            this.hotKeys.put("param1", 'H');
            this.hotKeys.put("param2", 'J');
            this.hotKeys.put("param3", 'K');
        }

        final JSONObject configFile = Global.getSettings().loadJSON("Config.ini");
        this.config = new HashMap<String, Object>() {{
            put("defaultModeIsRTS", (boolean)configFile.getBoolean("defaultModeIsRTS"));
            put("pauseUnpause", (boolean)configFile.getBoolean("pauseUnpause"));
            put("scrollSpeed", (float)configFile.getDouble("scrollSpeed"));
            put("scrollSmoothing", (float)configFile.getDouble("scrollSmoothing"));
            put("scrollSpeedKeyboard", (float)configFile.getDouble("scrollSpeedKeyboard"));
            put("scrollSmoothingKeyboard", (float)configFile.getDouble("scrollSmoothingKeyboard"));
            put("maxZoom", (float)configFile.getDouble("maxZoom"));
            put("shipsWillRun", (boolean)configFile.getBoolean("shipsWillRun"));
        }};
        this.version = (String)configFile.get("version");
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
            else if (((HashMap<String, Object>)Global.getSector().getMemory()
                            .get("$RTSASSIST_saveData")).get("version") == null
                    || !((String)((HashMap<String, Object>)Global.getSector()
                            .getMemory().get("$RTSASSIST_saveData")).get("version")).equals(this.version))
                ((HashMap<String, Object>)Global.getSector().getMemory().get("$RTSASSIST_saveData")).clear();
            HashMap<String, Object> mem = (HashMap<String, Object>)Global.getSector().getMemory().get("$RTSASSIST_saveData");
            mem.put("inDevelopment", this.inDevelopment);
            mem.put("version", this.version);
            if (mem.get("hotkeys") != null)
                ((HashMap<?, ?>)mem.get("hotkeys")).clear();
            mem.put("hotKeys", this.hotKeys);
            if (mem.get("config") != null)
                ((HashMap<?, ?>)mem.get("config")).clear();
            mem.put("config", this.config);
            mem.put("isEnabled", this.isEnabled);
            mem.put("blockedSystems", this.blockedSystems);
            Global.getSector().getMemory().set("$RTSASSIST_saveData", mem);
        }
    }
}

