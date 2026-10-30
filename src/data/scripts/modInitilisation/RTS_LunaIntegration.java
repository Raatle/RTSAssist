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

import java.util.HashMap;

import static data.scripts.RTSAssistModPlugin.RTS_Global;

public class RTS_LunaIntegration {

    String modID = "RTSAssist";
    String hotKeyTabName = "HotKeys";
    String configTabName = "Settings";
    String UITabName = "Render Settings";
    String DevToolsTabName = "Dev Tools";
    HashMap<String, String> hotPointer = (HashMap<String, String>)RTSAssistModPlugin.RTS_Global.get("hotKeys");
    HashMap<String, Object> confPointer = (HashMap<String, Object>)RTSAssistModPlugin.RTS_Global.get("config");

    public void init () {
        if (!Global.getSettings().getModManager().isModEnabled("lunalib"))
            return;
        this.addHotkeys();
        this.addConfig();
        LunaSettings.SettingsCreator.refresh("RTSAssist");
    }

    private void addHotkeys () {
        /* Toggle RTS mode */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_enable_RTSMode",
                "Toggle RTS mode",
                "Switch between vanilla and RTS control schemes.",
                hotPointer.get("enable_RTSMode") == null
                        ? 58
                        : Keyboard.getKeyIndex(this.hotPointer.get("enable_RTSMode")),
                this.hotKeyTabName
        );
        /* Save Formations */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_saveLayout",
                "Save Formations",
                "Save all assignments, control groups and vanilla escort assignments to memory.",
                Keyboard.getKeyIndex(this.hotPointer.get("saveLayout")),
                this.hotKeyTabName
        );
        /* Load Formations */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_loadLayout",
                "Load Formations",
                "Load all assignments, control groups and vanilla escort assignments from memory, positioned at the cursor location.",
                Keyboard.getKeyIndex(this.hotPointer.get("loadLayout")),
                this.hotKeyTabName
        );
        /* Remove Assignment */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_deleteAssignments",
                "Remove Assignment",
                "Selected ships return to vanilla AI control.",
                Keyboard.getKeyIndex(this.hotPointer.get("deleteAssignments")),
                this.hotKeyTabName
        );
        /* Vent Ships */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_vent",
                "Vent Ships",
                "Selected ships will attempt to vent flux.",
                Keyboard.getKeyIndex(this.hotPointer.get("vent")),
                this.hotKeyTabName
        );
        /* Use Systems */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_useSystem",
                "Use Systems",
                "Selected ships will attempt to use their systems",
                Keyboard.getKeyIndex(this.hotPointer.get("useSystem")),
                this.hotKeyTabName
        );
        /* Move Together */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_moveTogether",
                "Move Together",
                "Selected ships with assigments will maintain formation.",
                Keyboard.getKeyIndex(this.hotPointer.get("moveTogether")),
                this.hotKeyTabName
        );
        /* Attack Move */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_attackMove",
                "Attack Move",
                "Selected ships attack move to the designated position. Will convert any move assignments before creating a new assignment.",
                Keyboard.getKeyIndex(this.hotPointer.get("attackMove")),
                this.hotKeyTabName
        );
        /* Radial Orbit */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_orbitRadial",
                "Radial Orbit",
                "Selected ships orbit the enemy ship under the cursor with their nose pointed at the target. Issuing the same order on the same target again reverses the orbit direction.",
                Keyboard.getKeyIndex(this.hotPointer.get("orbitRadial")),
                this.hotKeyTabName
        );
        /* Tangential Orbit */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_orbitTangential",
                "Tangential Orbit",
                "Selected ships orbit the enemy ship under the cursor with their broadside facing the target. Issuing the same order on the same target again reverses the orbit direction.",
                Keyboard.getKeyIndex(this.hotPointer.get("orbitTangential")),
                this.hotKeyTabName
        );
        /* Keep Distance */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_keepDistance",
                "Keep Distance",
                "Selected ships keep a fixed distance from the enemy ship under the cursor (85% of their longest direct-fire weapon range by default).",
                Keyboard.getKeyIndex(this.hotPointer.get("keepDistance")),
                this.hotKeyTabName
        );
        /* Lock Target (view ship info) */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_lockTarget",
                "Lock Target (view ship info)",
                "Lock the ship under the cursor (allies and enemies alike) and fan out its info card, vanilla-R style. Press the key again on the locked ship or over empty space, or make any selection, to release the lock.",
                Keyboard.getKeyIndex(this.hotPointer.get("lockTarget")),
                this.hotKeyTabName
        );
        /* Strafe Left */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_strafeLeft",
                "Strafe Camera Left",
                "Strafe the Camera left using the keyboard",
                Keyboard.getKeyIndex(this.hotPointer.get("strafeCameraLeft")),
                this.hotKeyTabName
        );
        /* Strafe Right */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_strafeRight",
                "Strafe Camera Right",
                "Strafe the Camera right using the keyboard",
                Keyboard.getKeyIndex(this.hotPointer.get("strafeCameraRight")),
                this.hotKeyTabName
        );
        /* Strafe Up */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_strafeUp",
                "Strafe Camera Up",
                "Strafe the Camera up using the keyboard",
                Keyboard.getKeyIndex(this.hotPointer.get("strafeCameraUp")),
                this.hotKeyTabName
        );
        /* Strafe Down */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_strafeDown",
                "Strafe Camera Down",
                "Strafe the Camera down using the keyboard",
                Keyboard.getKeyIndex(this.hotPointer.get("strafeCameraDown")),
                this.hotKeyTabName
        );
        /* Begin broadside selection */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_broadsideSelection",
                "Adjust broadside modifier",
                "Adjust broadside modifiers for a selected ship. Persistant between battles and saves",
                Keyboard.getKeyIndex(this.hotPointer.get("broadsideSelection")),
                this.hotKeyTabName
        );
        /* Show/hide minimap */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_toggleMiniMap",
                "Toggle Minimap",
                "Show or hide the RTSAssist minimap during combat.",
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
                "Default Mode",
                "True / False : RTS mode is enabled/disabled at the beginning of combat. This does not disable the mod, but selects the default mode at the beginning of combat.",
                (boolean)this.confPointer.get("defaultModeIsRTS"),
                this.configTabName
        );
        /* Pause on RTSMode */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsConfig_pauseUnpause",
                "Pause on RTSMode",
                "Pause when changing to RTS mode, unpause on changing to vanilla.",
                (boolean)this.confPointer.get("pauseUnpause"),
                this.configTabName
        );
        /* Switch right mouse button */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsConfig_switchRightClick",
                "Switch Right Mouse Button",
                "Switch single and double right click functionality. Temporarily disabled.",
                (boolean)this.confPointer.get("switchRightClick"),
                this.configTabName
        );
        /* Mouse Scroll Speed */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsConfig_scrollSpeed",
                "Mouse Scroll Speed",
                "Change the speed at which the screen pans, when using the Mouse.",
                ((Float)this.confPointer.get("scrollSpeed")).intValue(),
                5,
                100,
                this.configTabName
        );
        /* Mouse Scroll Acceleration */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsConfig_scrollSmoothing",
                "Mouse Scroll Smoothing",
                "Change the acceleration at which the screen pans, when using the Mouse.",
                ((Float)this.confPointer.get("scrollSmoothing")).intValue(),
                2,
                50,
                this.configTabName
        );
        /* Keyboard Scroll Speed */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsConfig_scrollSpeedKeyboard",
                "Keyboard Scroll Speed",
                "Change the speed at which the screen pans, when using the Keyboard.",
                ((Float)this.confPointer.get("scrollSpeedKeyboard")).intValue(),
                5,
                100,
                this.configTabName
        );
        /* Keyboard Scroll Accleration */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsConfig_scrollSmoothingKeyboard",
                "Keyboard Scroll Smoothing",
                "Change the acceleration at which the screen pans, when using the Keyboard.",
                ((Float)this.confPointer.get("scrollSmoothingKeyboard")).intValue(),
                2,
                50,
                this.configTabName
        );
        /* Zoom Sensitivity */
        LunaSettings.SettingsCreator.addDouble(
                this.modID,
                "RTSA_SettingsConfig_zoomSensi",
                "Zoom Sensitivity",
                "How sensitive the scroll wheel is.",
                (float)this.confPointer.get("zoomSensi"),
                0.1d,
                10d,
                this.configTabName
        );
        /* Minimum Zoom */
        LunaSettings.SettingsCreator.addDouble(
                this.modID,
                "RTSA_SettingsConfig_minZoom",
                "Minimum Zoom",
                "How far the player can zoom in.",
                (float)this.confPointer.get("minZoom"),
                0.1d,
                10d,
                this.configTabName
        );
        /* Maximum Zoom */
        LunaSettings.SettingsCreator.addDouble(
                this.modID,
                "RTSA_SettingsConfig_maxZoom",
                "Maximum Zoom",
                "How far the player can zoom out.",
                (float)this.confPointer.get("maxZoom"),
                1d,
                50d,
                this.configTabName
        );
        /* Screen Scaling */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsConfig_screenScaling",
                "Render Scaling",
                "Manually adjust Render scaling. If using in game Render scaling this should not be necessary. If you are using nvidia upscaling for example, you will likely need to adjust this setting. Match the value with your scaling setting.",
                ((Float)this.confPointer.get("screenScaling")).intValue(),
                1,
                500,
                this.configTabName
        );
        /* Selection Tolerance */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsConfig_selectionTolerance",
                "Selection Tolerance",
                "Adjusts the error tolerance for selecting ships. If you are constantly missing clicks on ships, raising this value will help. Settings to 1 removes this feature.",
                ((Float)this.confPointer.get("selectionTolerance")).intValue(),
                1,
                10,
                this.configTabName
        );
        /* Remember Zoom */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsConfig_rememberZoom",
                "Remember Zoom when switching between Modes",
                "The zoom for either RTSmode or Vanilla is stored and reset upon switching to that mode.",
                (boolean)this.confPointer.get("rememberZoom"),
                this.configTabName
        );
        /* Alternative Rotation */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsConfig_alternativeRotation",
                "Alternative Rotation",
                "An alternative method for rotating formations that requires less mouse movement.",
                (boolean)this.confPointer.get("alternativeRotation"),
                this.configTabName
        );
        /* Render Command Volume */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsUI_CommandVolume",
                "Render Command Volume",
                "Adjust the volume assosciated with the audio feedback for issuing commands.",
                ((Float)this.confPointer.get("UICommandVolume")).intValue(),
                0,
                10,
                this.UITabName
        );
        /* Minimap refresh interval */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsUI_miniMapRefreshMs",
                "Minimap Refresh Interval",
                "Refresh period of the minimap's frame data (ms); also the duration of one full sweep cycle. Lower values mean fresher map data.",
                ((Float)this.confPointer.get("miniMapRefreshMs")).intValue(),
                16,
                2000,
                this.UITabName
        );
        /* Minimap sweep duration */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsUI_miniMapSweepMs",
                "Sweep Duration",
                "How long (ms) each sweep phase (ships / fighters) takes to finish scanning all units. Each cycle goes: sweep ships, hold, sweep fighters, hold.",
                ((Float)this.confPointer.get("miniMapSweepMs")).intValue(),
                16,
                1000,
                this.UITabName
        );
        /* Show minimap */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsUI_showMiniMap",
                "Show Minimap",
                "True / False : whether to show the RTSAssist minimap. Initialized from this setting when combat starts; can also be toggled at any time with the hotkey.",
                (boolean)this.confPointer.get("showMiniMap"),
                this.UITabName
        );
        /* Show fog of war */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsUI_showFogOfWar",
                "Show Fog Of War",
                "True / False : whether to render the black fog of war covering areas of the battlefield your ships cannot see.",
                (boolean)this.confPointer.get("showFogOfWar"),
                this.UITabName
        );
        /* Enable RTS Ship testing tools */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsDevTools_enableShipTestSuite",
                "Enable RTSAssist ship testing tools",
                "See the modding README for details. Requires Starsector Devmode to be true.",
                (boolean)this.confPointer.get("enableShipTestSuite"),
                this.DevToolsTabName
        );
        /* modID */
        LunaSettings.SettingsCreator.addString(
                this.modID,
                "RTSA_SettingsDevTools_modID",
                "modID",
                "See the modding README for details. Entering strings in Luna is currently very buggy. It may be better to set this in the RTSAssist config.",
                (String)this.confPointer.get("modID"),
                this.DevToolsTabName
        );
        /* Toggle between searching ships with custom systems or all ships */
        LunaSettings.SettingsCreator.addBoolean(
                this.modID,
                "RTSA_SettingsDevTools_findAllShips",
                "Search for ALL ships",
                "See the modding README for details.",
                (boolean)this.confPointer.get("findAllShips"),
                this.DevToolsTabName
        );
    }
}
