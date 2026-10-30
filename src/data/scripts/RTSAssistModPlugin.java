/*
  **********************************************************************************************************
  * RTSAssist version 0.2.14exp
  * Copyright (C) 2025-2026, Raatle

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
  **********************************************************************************************************
 */

package data.scripts;

import data.scripts.modInitilisation.RTS_CommonsControl;
import data.scripts.modInitilisation.RTS_LunaIntegration;
import data.scripts.plugins.Utils.RTS_StatefulClasses;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

public class RTSAssistModPlugin extends BaseModPlugin {

    public static class classIdentifiers {
        public String commonsControl = RTS_StatefulClasses.getUniqueIdentifier();
        public String gameID = RTS_StatefulClasses.getUniqueIdentifier();
        public String inDevelopment = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public static classIdentifiers names = new classIdentifiers();

    HashMap<String, String> hotKeys;
    HashMap<String, Object> config;

    public static HashMap<String, Object> RTS_Global = new HashMap<>();
    public RTS_CommonsControl commonsControl;

    @Override
    public void onApplicationLoad() throws Exception {
        this.commonsControl = new RTS_CommonsControl();
        RTSAssistModPlugin.RTS_Global.put(RTSAssistModPlugin.names.commonsControl, this.commonsControl);

        final JSONObject configFile = Global.getSettings().loadJSON("Config.ini");
        this.config = new HashMap<String, Object>() {{
            put("defaultModeIsRTS", (boolean)configFile.getBoolean("defaultModeIsRTS"));
            put("pauseUnpause", (boolean)configFile.getBoolean("pauseUnpause"));
            put("switchRightClick", (boolean)configFile.getBoolean("switchRightClick"));
            put("scrollSpeed", (float)configFile.getDouble("scrollSpeed"));
            put("scrollSmoothing", (float)configFile.getDouble("scrollSmoothing"));
            put("scrollSpeedKeyboard", (float)configFile.getDouble("scrollSpeedKeyboard"));
            put("scrollSmoothingKeyboard", (float)configFile.getDouble("scrollSmoothingKeyboard"));
            put("zoomSensi", (float)configFile.getDouble("zoomSensi"));
            put("minZoom", (float)configFile.getDouble("minZoom"));
            put("maxZoom", (float)configFile.getDouble("maxZoom"));
            put("screenScaling", (float)configFile.getDouble("screenScaling"));
            put("shipsWillRun", (boolean)configFile.getBoolean("shipsWillRun"));
            put("selectionTolerance", (float)configFile.getDouble("selectionTolerance"));
            put("rememberZoom", (boolean)configFile.getBoolean("rememberZoom"));
            put("alternativeRotation", (boolean)configFile.getBoolean("alternativeRotation"));
            put("UICommandVolume", (float)configFile.getDouble("UICommandVolume"));
            put("enableShipTestSuite", (boolean)configFile.getBoolean("enableShipTestSuite"));
            put("findAllShips", (boolean)configFile.getBoolean("findAllShips"));
            /* Falls back to true for config files predating this option. */
            boolean showMiniMap = true;
            try {
                showMiniMap = (boolean)configFile.getBoolean("showMiniMap");
            } catch (JSONException ignored) {
            }
            put("showMiniMap", showMiniMap);
            /* Falls back to 200 ms for config files predating this option. */
            float miniMapRefreshMs = 1010f;
            try {
                miniMapRefreshMs = (float)configFile.getDouble("miniMapRefreshMs");
            } catch (JSONException ignored) {
            }
            put("miniMapRefreshMs", miniMapRefreshMs);
            /* Falls back to 300 ms for config files predating this option. */
            float miniMapSweepMs = 200f;
            try {
                miniMapSweepMs = (float)configFile.getDouble("miniMapSweepMs");
            } catch (JSONException ignored) {
            }
            put("miniMapSweepMs", miniMapSweepMs);
            /* Falls back to true for config files predating this option. */
            boolean showFogOfWar = true;
            try {
                showFogOfWar = (boolean)configFile.getBoolean("showFogOfWar");
            } catch (JSONException ignored) {
            }
            put("showFogOfWar", showFogOfWar);
            if (configFile.getString("modID").isEmpty())
                put("modID", "");
            else
                put("modID", (String)configFile.getString("modID"));
        }};

        RTSAssistModPlugin.RTS_Global.put(
                RTSAssistModPlugin.names.inDevelopment,
                (boolean)this.config.get("enableShipTestSuite") && Global.getSettings().isDevMode()
        );

        final JSONObject hotKeyFile = Global.getSettings().loadJSON("Hotkeys.ini");
        this.hotKeys = new HashMap<String, String>() {{
            if (hotKeyFile.getString("enable_RTSMode").isEmpty())
                put("enable_RTSMode", null);
            else
                put("enable_RTSMode", ((Character)hotKeyFile.getString("enable_RTSMode").charAt(0)).toString());
            put("strafeCameraLeft", ((Character)hotKeyFile.getString("strafeCameraLeft").charAt(0)).toString());
            put("strafeCameraRight", ((Character)hotKeyFile.getString("strafeCameraRight").charAt(0)).toString());
            put("strafeCameraUp", ((Character)hotKeyFile.getString("strafeCameraUp").charAt(0)).toString());
            put("strafeCameraDown", ((Character)hotKeyFile.getString("strafeCameraDown").charAt(0)).toString());
            put("saveLayout", ((Character)hotKeyFile.getString("saveLayout").charAt(0)).toString());
            put("loadLayout", ((Character)hotKeyFile.getString("loadLayout").charAt(0)).toString());
            put("deleteAssignments", ((Character)hotKeyFile.getString("deleteAssignments").charAt(0)).toString());
            put("vent", ((Character)hotKeyFile.getString("vent").charAt(0)).toString());
            put("useSystem", ((Character)hotKeyFile.getString("useSystem").charAt(0)).toString());
            put("moveTogether", ((Character)hotKeyFile.getString("moveTogether").charAt(0)).toString());
            put("attackMove", ((Character)hotKeyFile.getString("attackMove").charAt(0)).toString());
            put("broadsideSelection", ((Character)hotKeyFile.getString("broadsideSelection").charAt(0)).toString());
            /* Falls back to "G" for hotkey files predating this option; empty disables the binding. */
            String toggleMiniMap = "";
            try {
                toggleMiniMap = hotKeyFile.getString("toggleMiniMap");
            } catch (JSONException ignored) {
            }
            if (toggleMiniMap.isEmpty())
                put("toggleMiniMap", null);
            else
                put("toggleMiniMap", ((Character)toggleMiniMap.charAt(0)).toString());
            /* Target orders (0.2.20exp); fall back to Z/T/O for hotkey files predating them. */
            put("orbitRadial", readHotKey(hotKeyFile, "orbitRadial", "Z"));
            put("orbitTangential", readHotKey(hotKeyFile, "orbitTangential", "T"));
            put("keepDistance", readHotKey(hotKeyFile, "keepDistance", "O"));
            /* Target-info lock (0.2.21exp); falls back to L for older hotkey files. */
            put("lockTarget", readHotKey(hotKeyFile, "lockTarget", "L"));
        }};
        if (true) {
            this.hotKeys.put("param1", "H");
            this.hotKeys.put("param2", "J");
            this.hotKeys.put("param3", "K");
            this.hotKeys.put("param4", "Y");
            this.hotKeys.put("param5", "U");
            this.hotKeys.put("param6", "I");
            this.hotKeys.put("param7", "N");
            this.hotKeys.put("param8", "M");
        }
        RTSAssistModPlugin.RTS_Global.put("hotKeys", this.hotKeys);
        RTSAssistModPlugin.RTS_Global.put("config", this.config);
        RTSAssistModPlugin.RTS_Global.put("isEnabled", (boolean)configFile.getBoolean("isEnabled"));
        new RTS_LunaIntegration().init();
    }

    /* Reads a single hotkey entry; missing keys fall back to the given default, empty disables. */
    private String readHotKey (JSONObject file, String key, String fallback) {
        try {
            String value = file.getString(key);
            return (value.isEmpty() ? null : ((Character)value.charAt(0)).toString());
        } catch (JSONException ignored) {
            return (fallback);
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (Global.getSector().getPlayerFleet() == null)
            return;
        String ID = (String)Global.getSector().getPersistentData().get("$RTSA_SAVEID");
        if (ID == null) {
            Global.getSector().getPersistentData().put("$RTSA_SAVEID", Global.getSector().genUID());
            ID = (String)Global.getSector().getPersistentData().get("$RTSA_SAVEID");
        }
        RTS_Global.put(RTSAssistModPlugin.names.gameID, ID);
    }
}

