/****************************************************************************************
 * RTSAssist version 0.1.5
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

import data.scripts.modInitilisation.RTS_CommonsControl;
import data.scripts.plugins.Utils.RTS_StatefulClasses;
import org.json.JSONObject;
import java.util.HashMap;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

public class RTSAssistModPlugin extends BaseModPlugin {

    public static class classIdentifiers {
        public String commonsControl = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public static classIdentifiers names = new classIdentifiers();

    boolean inDevelopment = false;
    
    HashMap<String, Object> hotKeys;
    HashMap<String, Object> config;
    String version;
    boolean isEnabled;
    boolean uninstall;

    public static HashMap<String, Object> RTS_Global = new HashMap<>();
    public RTS_CommonsControl commonsControl;

    @Override
    public void onApplicationLoad() throws Exception {
        this.inDevelopment = this.inDevelopment && Global.getSettings().isDevMode();
        RTSAssistModPlugin.RTS_Global.put("inDevelopment", this.inDevelopment);
        if (this.inDevelopment) {
            this.commonsControl = new RTS_CommonsControl();
            RTSAssistModPlugin.RTS_Global.put(RTSAssistModPlugin.names.commonsControl, this.commonsControl);
        }

        final JSONObject hotKeyFile = Global.getSettings().loadJSON("Hotkeys.ini");
        this.hotKeys = new HashMap<String, Object>() {{
            if (hotKeyFile.getString("enable_RTSMode").isEmpty())
                put("enable_RTSMode", null);
            else
                put("enable_RTSMode", hotKeyFile.getString("enable_RTSMode").charAt(0));
//            put("strafeCameraLeft", hotKeyFile.getString("strafeCameraLeft").charAt(0));
//            put("strafeCameraRight", hotKeyFile.getString("strafeCameraRight").charAt(0));
//            put("strafeCameraUp", hotKeyFile.getString("strafeCameraUp").charAt(0));
//            put("strafeCameraDown", hotKeyFile.getString("strafeCameraDown").charAt(0));
            put("saveLayout", hotKeyFile.getString("saveLayout").charAt(0));
            put("loadLayout", hotKeyFile.getString("loadLayout").charAt(0));
            put("deleteAssignments", hotKeyFile.getString("deleteAssignments").charAt(0));
            put("vent", hotKeyFile.getString("vent").charAt(0));
            put("useSystem", hotKeyFile.getString("useSystem").charAt(0));
            put("moveTogether", hotKeyFile.getString("moveTogether").charAt(0));
            put("attackMove", hotKeyFile.getString("attackMove").charAt(0));
        }};
        if (this.inDevelopment) {
            this.hotKeys.put("param1", 'H');
            this.hotKeys.put("param2", 'J');
            this.hotKeys.put("param3", 'K');
            this.hotKeys.put("param4", 'Y');
            this.hotKeys.put("param5", 'U');
            this.hotKeys.put("param6", 'I');
            this.hotKeys.put("param7", 'N');
            this.hotKeys.put("param8", 'M');
        }
        RTSAssistModPlugin.RTS_Global.put("hotKeys", this.hotKeys);

        final JSONObject configFile = Global.getSettings().loadJSON("Config.ini");
        this.config = new HashMap<String, Object>() {{
            put("defaultModeIsRTS", (boolean)configFile.getBoolean("defaultModeIsRTS"));
            put("pauseUnpause", (boolean)configFile.getBoolean("pauseUnpause"));
            put("switchRightClick", (boolean)configFile.getBoolean("switchRightClick"));
            put("scrollSpeed", (float)configFile.getDouble("scrollSpeed"));
            put("scrollSmoothing", (float)configFile.getDouble("scrollSmoothing"));
            put("scrollSpeedKeyboard", (float)configFile.getDouble("scrollSpeedKeyboard"));
            put("scrollSmoothingKeyboard", (float)configFile.getDouble("scrollSmoothingKeyboard"));
            put("maxZoom", (float)configFile.getDouble("maxZoom"));
            put("screenScaling", (float)configFile.getDouble("screenScaling"));
            put("shipsWillRun", (boolean)configFile.getBoolean("shipsWillRun"));
        }};
        this.version = (String)configFile.get("version");
        this.isEnabled = (boolean)configFile.get("enabled");
        this.uninstall = (boolean)configFile.get("uninstall");
        RTSAssistModPlugin.RTS_Global.put("config", this.config);
        RTSAssistModPlugin.RTS_Global.put("isEnabled", this.isEnabled);
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
                            .getMemory().get("$RTSASSIST_saveData")).get("version")).equals(this.version)) {
                HashMap<String, Object> saveData = (HashMap<String, Object>)Global.getSector()
                        .getMemory().get("$RTSASSIST_saveData");
                saveData.clear();
            }
            HashMap<String, Object> mem = (HashMap<String, Object>)Global.getSector().getMemory().get("$RTSASSIST_saveData");
            mem.put("version", this.version);
            Global.getSector().getMemory().set("$RTSASSIST_saveData", mem);
        }
    }
}

