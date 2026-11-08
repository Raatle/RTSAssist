/*
  **********************************************************************************************************
  * RTSAssist version 0.2.12exp
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

package data.scripts.modInitilisation;

import com.fs.starfarer.api.Global;
import data.scripts.RTSAssistModPlugin;
import lunalib.backend.ui.settings.LunaSettingsUISettingsPanel;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import static data.scripts.RTSAssistModPlugin.RTS_Global;

public class RTS_LunaIntegration {

    String modID = "RTSAssist";
    /* Tab names are assigned in init() AFTER the string table is loaded - field initializers
       would run at construction time, when the table is still null (they'd degrade to raw keys
       like "_tab_hotkeys"). */
    String hotKeyTabName = null;
    String configTabName = null;
    String UITabName = null;
    String DevToolsTabName = null;
    HashMap<String, String> hotPointer = (HashMap<String, String>)RTSAssistModPlugin.RTS_Global.get("hotKeys");
    HashMap<String, Object> confPointer = (HashMap<String, Object>)RTSAssistModPlugin.RTS_Global.get("config");


    /* ---- i18n: labels and tooltips come from data/config/RTSAssistStrings.json ---- */
    private org.json.JSONObject stringTable = null;
    private String uiLang = "en";
    /* Loaded once per JVM - onApplicationEnabled can run more than once (and under the fr /
     * Miko startup stack even from different loader threads); the second run must not end up
     * with a worse table than the first. */
    private static org.json.JSONObject CACHED_TABLE = null;
    private static String CACHED_LANG = null;
    private static boolean TABLE_TRIED = false;

    /* UI language: the Luna entry "RTSA_SettingsConfig_uiLanguage" (auto / en / zh) wins;
       "auto" follows the operating system language (zh -> Chinese, else English). */
    private String resolveLanguage() {
        try {
            String setting = LunaSettings.getString(this.modID, "RTSA_SettingsConfig_uiLanguage");
            if ("en".equals(setting) || "zh".equals(setting))
                return (setting);
        } catch (Throwable t) {
        }
        try {
            if (System.getProperty("user.language", "").toLowerCase().startsWith("zh"))
                return ("zh");
        } catch (Throwable t) {
        }
        return ("en");
    }

    /* Loads the string table through three tiers, because resource resolution differs between
     * setups (vanilla launcher vs the Miko/fr stack):
     *   1. SettingsAPI.loadText(filename, modId) - deterministic per-mod lookup;
     *   2. SettingsAPI.loadJSON(filename) - the legacy bare-name path;
     *   3. direct file read via ModSpecAPI.getPath() - bypasses the VFS entirely.
     * The result is cached JVM-wide; diagnostics go through log4j so they land in
     * starsector.log (System.out goes to the launcher console only). */
    private void loadStrings() {
        this.uiLang = this.resolveLanguage();
        if (TABLE_TRIED) {
            this.stringTable = CACHED_TABLE;
            this.uiLang = (CACHED_LANG != null ? CACHED_LANG : this.uiLang);
        }
        else {
            this.stringTable = this.loadStringTable();
            TABLE_TRIED = true;
            CACHED_TABLE = this.stringTable;
            CACHED_LANG = this.uiLang;
        }
        Global.getLogger(RTS_LunaIntegration.class).info(
                "LunaIntegration: language=" + this.uiLang
                        + ", table=" + (this.stringTable == null ? "MISSING" : "loaded")
                        + ", entries=" + this.countEntries()
                        + ", sample=" + this.L("RTSA_SettingsKeybind_attackMove")
                        + ", classFrom=" + this.getClass().getProtectionDomain().getCodeSource().getLocation()
        );
    }

    /* Entry count of the active language section (0 when the table is missing). */
    private int countEntries() {
        try {
            return (this.stringTable.getJSONObject(this.uiLang).length());
        } catch (Throwable t) {
            return (0);
        }
    }

    private org.json.JSONObject loadStringTable() {
        /* Tier 1: deterministic per-mod text load. */
        try {
            String text = Global.getSettings().loadText("data/config/RTSAssistStrings.json", "RTSAssist");
            if (text != null && text.trim().length() > 2)
                return (new org.json.JSONObject(text));
        } catch (Throwable t) {
            Global.getLogger(RTS_LunaIntegration.class).warn(
                    "LunaIntegration: loadText tier failed: " + t.toString());
        }
        /* Tier 2: the legacy bare-name JSON load. */
        try {
            org.json.JSONObject o = Global.getSettings().loadJSON("RTSAssistStrings.json");
            if (o != null && o.length() > 0)
                return (o);
        } catch (Throwable t) {
            Global.getLogger(RTS_LunaIntegration.class).warn(
                    "LunaIntegration: loadJSON tier failed: " + t.toString());
        }
        /* Tier 3: direct file read from the mod's install path. */
        try {
            String path = Global.getSettings().getModManager().getModSpec("RTSAssist").getPath();
            if (path != null) {
                File f = new File(path, "data/config/RTSAssistStrings.json");
                if (f.exists()) {
                    FileInputStream in = new FileInputStream(f);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0)
                        out.write(buf, 0, n);
                    in.close();
                    return (new org.json.JSONObject(new String(out.toByteArray(), "UTF-8")));
                }
            }
        } catch (Throwable t) {
            Global.getLogger(RTS_LunaIntegration.class).warn(
                    "LunaIntegration: direct-file tier failed: " + t.toString());
        }
        return (null);
    }

    /* Label lookup: the active language first, then English, then the field id. */
    private String L(String fieldID) {
        try {
            return (this.stringTable.getJSONObject(this.uiLang).getJSONObject(fieldID).getString("label"));
        } catch (Throwable t) {
        }
        try {
            return (this.stringTable.getJSONObject("en").getJSONObject(fieldID).getString("label"));
        } catch (Throwable t) {
            return (fieldID);
        }
    }

    /* Tooltip lookup: the active language first, then English, then empty. */
    private String T(String fieldID) {
        try {
            return (this.stringTable.getJSONObject(this.uiLang).getJSONObject(fieldID).getString("tooltip"));
        } catch (Throwable t) {
        }
        try {
            return (this.stringTable.getJSONObject("en").getJSONObject(fieldID).getString("tooltip"));
        } catch (Throwable t) {
            return ("");
        }
    }

    public void init () {
        if (!Global.getSettings().getModManager().isModEnabled("lunalib"))
            return;
        this.loadStrings();
        this.hotKeyTabName = L("_tab_hotkeys");
        this.configTabName = L("_tab_settings");
        this.UITabName = L("_tab_render");
        this.DevToolsTabName = L("_tab_devtools");
        this.addHotkeys();
        this.addConfig();
        /* Language selector - its own label/tooltip live in the string table as well
           ("UI Language / 界面语言"). Default "auto" follows the OS language; a change
           applies the next time the game starts. */
        LunaSettings.SettingsCreator.addString(
                this.modID,
                "RTSA_SettingsConfig_uiLanguage",
                L("RTSA_SettingsConfig_uiLanguage"),
                T("RTSA_SettingsConfig_uiLanguage"),
                "auto",
                this.configTabName
        );
        LunaSettings.SettingsCreator.refresh("RTSAssist");
    }

    private void addHotkeys () {
        /* Toggle RTS mode */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_enable_RTSMode",
                L("RTSA_SettingsKeybind_enable_RTSMode"),
                T("RTSA_SettingsKeybind_enable_RTSMode"),
                hotPointer.get("enable_RTSMode") == null
                        ? 58
                        : Keyboard.getKeyIndex(this.hotPointer.get("enable_RTSMode")),
                this.hotKeyTabName
        );
        /* Save Formations */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_saveLayout",
                L("RTSA_SettingsKeybind_saveLayout"),
                T("RTSA_SettingsKeybind_saveLayout"),
                Keyboard.getKeyIndex(this.hotPointer.get("saveLayout")),
                this.hotKeyTabName
        );
        /* Load Formations */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_loadLayout",
                L("RTSA_SettingsKeybind_loadLayout"),
                T("RTSA_SettingsKeybind_loadLayout"),
                Keyboard.getKeyIndex(this.hotPointer.get("loadLayout")),
                this.hotKeyTabName
        );
        /* Remove Assignment */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_deleteAssignments",
                L("RTSA_SettingsKeybind_deleteAssignments"),
                T("RTSA_SettingsKeybind_deleteAssignments"),
                Keyboard.getKeyIndex(this.hotPointer.get("deleteAssignments")),
                this.hotKeyTabName
        );
        /* Vent Ships */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_vent",
                L("RTSA_SettingsKeybind_vent"),
                T("RTSA_SettingsKeybind_vent"),
                Keyboard.getKeyIndex(this.hotPointer.get("vent")),
                this.hotKeyTabName
        );
        /* Use Systems */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_useSystem",
                L("RTSA_SettingsKeybind_useSystem"),
                T("RTSA_SettingsKeybind_useSystem"),
                Keyboard.getKeyIndex(this.hotPointer.get("useSystem")),
                this.hotKeyTabName
        );
        /* Move Together */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_moveTogether",
                L("RTSA_SettingsKeybind_moveTogether"),
                T("RTSA_SettingsKeybind_moveTogether"),
                Keyboard.getKeyIndex(this.hotPointer.get("moveTogether")),
                this.hotKeyTabName
        );
        /* Attack Move */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_attackMove",
                L("RTSA_SettingsKeybind_attackMove"),
                T("RTSA_SettingsKeybind_attackMove"),
                Keyboard.getKeyIndex(this.hotPointer.get("attackMove")),
                this.hotKeyTabName
        );
        /* Radial Orbit */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_orbitRadial",
                L("RTSA_SettingsKeybind_orbitRadial"),
                T("RTSA_SettingsKeybind_orbitRadial"),
                Keyboard.getKeyIndex(this.hotPointer.get("orbitRadial")),
                this.hotKeyTabName
        );
        /* Tangential Orbit */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_orbitTangential",
                L("RTSA_SettingsKeybind_orbitTangential"),
                T("RTSA_SettingsKeybind_orbitTangential"),
                Keyboard.getKeyIndex(this.hotPointer.get("orbitTangential")),
                this.hotKeyTabName
        );
        /* Keep Distance */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_keepDistance",
                L("RTSA_SettingsKeybind_keepDistance"),
                T("RTSA_SettingsKeybind_keepDistance"),
                Keyboard.getKeyIndex(this.hotPointer.get("keepDistance")),
                this.hotKeyTabName
        );
        /* Lock Target (view ship info) */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_lockTarget",
                L("RTSA_SettingsKeybind_lockTarget"),
                T("RTSA_SettingsKeybind_lockTarget"),
                Keyboard.getKeyIndex(this.hotPointer.get("lockTarget")),
                this.hotKeyTabName
        );
        /* Strafe Left */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_strafeLeft",
                L("RTSA_SettingsKeybind_strafeLeft"),
                T("RTSA_SettingsKeybind_strafeLeft"),
                Keyboard.getKeyIndex(this.hotPointer.get("strafeCameraLeft")),
                this.hotKeyTabName
        );
        /* Strafe Right */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_strafeRight",
                L("RTSA_SettingsKeybind_strafeRight"),
                T("RTSA_SettingsKeybind_strafeRight"),
                Keyboard.getKeyIndex(this.hotPointer.get("strafeCameraRight")),
                this.hotKeyTabName
        );
        /* Strafe Up */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_strafeUp",
                L("RTSA_SettingsKeybind_strafeUp"),
                T("RTSA_SettingsKeybind_strafeUp"),
                Keyboard.getKeyIndex(this.hotPointer.get("strafeCameraUp")),
                this.hotKeyTabName
        );
        /* Strafe Down */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_strafeDown",
                L("RTSA_SettingsKeybind_strafeDown"),
                T("RTSA_SettingsKeybind_strafeDown"),
                Keyboard.getKeyIndex(this.hotPointer.get("strafeCameraDown")),
                this.hotKeyTabName
        );
        /* Begin broadside selection */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_broadsideSelection",
                L("RTSA_SettingsKeybind_broadsideSelection"),
                T("RTSA_SettingsKeybind_broadsideSelection"),
                Keyboard.getKeyIndex(this.hotPointer.get("broadsideSelection")),
                this.hotKeyTabName
        );
        /* Show/hide minimap */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_toggleMiniMap",
                L("RTSA_SettingsKeybind_toggleMiniMap"),
                T("RTSA_SettingsKeybind_toggleMiniMap"),
                this.hotPointer.get("toggleMiniMap") == null
                        ? Keyboard.KEY_G
                        : Keyboard.getKeyIndex(this.hotPointer.get("toggleMiniMap")),
                this.hotKeyTabName
        );
    }

    private void addConfig () {
        /* Default mode at the beginning of battle. */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsConfig_defaultModeIsRTS",
                L("RTSA_SettingsConfig_defaultModeIsRTS"),
                T("RTSA_SettingsConfig_defaultModeIsRTS"),
                (boolean)this.confPointer.get("defaultModeIsRTS"),
                this.configTabName
        );
        /* Pause on RTSMode */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsConfig_pauseUnpause",
                L("RTSA_SettingsConfig_pauseUnpause"),
                T("RTSA_SettingsConfig_pauseUnpause"),
                (boolean)this.confPointer.get("pauseUnpause"),
                this.configTabName
        );
        /* Switch right mouse button */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsConfig_switchRightClick",
                L("RTSA_SettingsConfig_switchRightClick"),
                T("RTSA_SettingsConfig_switchRightClick"),
                (boolean)this.confPointer.get("switchRightClick"),
                this.configTabName
        );
        /* Mouse Scroll Speed */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsConfig_scrollSpeed",
                L("RTSA_SettingsConfig_scrollSpeed"),
                T("RTSA_SettingsConfig_scrollSpeed"),
                ((Float)this.confPointer.get("scrollSpeed")).intValue(),
                5,
                100,
                this.configTabName
        );
        /* Mouse Scroll Acceleration */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsConfig_scrollSmoothing",
                L("RTSA_SettingsConfig_scrollSmoothing"),
                T("RTSA_SettingsConfig_scrollSmoothing"),
                ((Float)this.confPointer.get("scrollSmoothing")).intValue(),
                2,
                50,
                this.configTabName
        );
        /* Keyboard Scroll Speed */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsConfig_scrollSpeedKeyboard",
                L("RTSA_SettingsConfig_scrollSpeedKeyboard"),
                T("RTSA_SettingsConfig_scrollSpeedKeyboard"),
                ((Float)this.confPointer.get("scrollSpeedKeyboard")).intValue(),
                5,
                100,
                this.configTabName
        );
        /* Keyboard Scroll Accleration */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsConfig_scrollSmoothingKeyboard",
                L("RTSA_SettingsConfig_scrollSmoothingKeyboard"),
                T("RTSA_SettingsConfig_scrollSmoothingKeyboard"),
                ((Float)this.confPointer.get("scrollSmoothingKeyboard")).intValue(),
                2,
                50,
                this.configTabName
        );
        /* Zoom Sensitivity */
        LunaSettings.SettingsCreator.addDouble(
                this.modID,
                "RTSA_SettingsConfig_zoomSensi",
                L("RTSA_SettingsConfig_zoomSensi"),
                T("RTSA_SettingsConfig_zoomSensi"),
                (float)this.confPointer.get("zoomSensi"),
                0.1d,
                10d,
                this.configTabName
        );
        /* Minimum Zoom */
        LunaSettings.SettingsCreator.addDouble(
                this.modID,
                "RTSA_SettingsConfig_minZoom",
                L("RTSA_SettingsConfig_minZoom"),
                T("RTSA_SettingsConfig_minZoom"),
                (float)this.confPointer.get("minZoom"),
                0.1d,
                10d,
                this.configTabName
        );
        /* Maximum Zoom */
        LunaSettings.SettingsCreator.addDouble(
                this.modID,
                "RTSA_SettingsConfig_maxZoom",
                L("RTSA_SettingsConfig_maxZoom"),
                T("RTSA_SettingsConfig_maxZoom"),
                (float)this.confPointer.get("maxZoom"),
                1d,
                50d,
                this.configTabName
        );
        /* Screen Scaling */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsConfig_screenScaling",
                L("RTSA_SettingsConfig_screenScaling"),
                T("RTSA_SettingsConfig_screenScaling"),
                ((Float)this.confPointer.get("screenScaling")).intValue(),
                1,
                500,
                this.configTabName
        );
        /* Selection Tolerance */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsConfig_selectionTolerance",
                L("RTSA_SettingsConfig_selectionTolerance"),
                T("RTSA_SettingsConfig_selectionTolerance"),
                ((Float)this.confPointer.get("selectionTolerance")).intValue(),
                1,
                10,
                this.configTabName
        );
        /* Remember Zoom */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsConfig_rememberZoom",
                L("RTSA_SettingsConfig_rememberZoom"),
                T("RTSA_SettingsConfig_rememberZoom"),
                (boolean)this.confPointer.get("rememberZoom"),
                this.configTabName
        );
        /* Alternative Rotation */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsConfig_alternativeRotation",
                L("RTSA_SettingsConfig_alternativeRotation"),
                T("RTSA_SettingsConfig_alternativeRotation"),
                (boolean)this.confPointer.get("alternativeRotation"),
                this.configTabName
        );
        /* Render Command Volume */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsUI_CommandVolume",
                L("RTSA_SettingsUI_CommandVolume"),
                T("RTSA_SettingsUI_CommandVolume"),
                ((Float)this.confPointer.get("UICommandVolume")).intValue(),
                0,
                10,
                this.UITabName
        );
        /* Minimap refresh interval */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsUI_miniMapRefreshMs",
                L("RTSA_SettingsUI_miniMapRefreshMs"),
                T("RTSA_SettingsUI_miniMapRefreshMs"),
                ((Float)this.confPointer.get("miniMapRefreshMs")).intValue(),
                16,
                2000,
                this.UITabName
        );
        /* Minimap sweep duration */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsUI_miniMapSweepMs",
                L("RTSA_SettingsUI_miniMapSweepMs"),
                T("RTSA_SettingsUI_miniMapSweepMs"),
                ((Float)this.confPointer.get("miniMapSweepMs")).intValue(),
                16,
                1000,
                this.UITabName
        );
        /* Show minimap */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsUI_showMiniMap",
                L("RTSA_SettingsUI_showMiniMap"),
                T("RTSA_SettingsUI_showMiniMap"),
                (boolean)this.confPointer.get("showMiniMap"),
                this.UITabName
        );
        /* Show fog of war */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsUI_showFogOfWar",
                L("RTSA_SettingsUI_showFogOfWar"),
                T("RTSA_SettingsUI_showFogOfWar"),
                (boolean)this.confPointer.get("showFogOfWar"),
                this.UITabName
        );
        /* Enable RTS Ship testing tools */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsDevTools_enableShipTestSuite",
                L("RTSA_SettingsDevTools_enableShipTestSuite"),
                T("RTSA_SettingsDevTools_enableShipTestSuite"),
                (boolean)this.confPointer.get("enableShipTestSuite"),
                this.DevToolsTabName
        );
        /* modID */
        LunaSettings.SettingsCreator.addString(
                this.modID,
                "RTSA_SettingsDevTools_modID",
                L("RTSA_SettingsDevTools_modID"),
                T("RTSA_SettingsDevTools_modID"),
                (String)this.confPointer.get("modID"),
                this.DevToolsTabName
        );
        /* Toggle between searching ships with custom systems or all ships */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsDevTools_findAllShips",
                L("RTSA_SettingsDevTools_findAllShips"),
                T("RTSA_SettingsDevTools_findAllShips"),
                (boolean)this.confPointer.get("findAllShips"),
                this.DevToolsTabName
        );
    }
}
