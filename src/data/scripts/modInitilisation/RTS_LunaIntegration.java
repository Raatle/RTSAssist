package data.scripts.modInitilisation;

import com.fs.starfarer.api.Global;
import data.scripts.RTSAssistModPlugin;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;

import static data.scripts.RTSAssistModPlugin.RTS_Global;

public class RTS_LunaIntegration {

    String modID = "RTSAssist";
    String hotKeyTabName = "HotKeys";
    String configTabName = "Settings";

    HashMap<String, Object> hotPointer = (HashMap<String, Object>)RTSAssistModPlugin.RTS_Global.get("hotKeys");
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
                        : Keyboard.getKeyIndex(Character.toString((char)this.hotPointer.get("enable_RTSMode"))),
                this.hotKeyTabName
        );
        /* Save Formations */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_saveLayout",
                "Save Formations",
                "Save all assignments, control groups and vanilla escort assignments to memory.",
                Keyboard.getKeyIndex(Character.toString((char)this.hotPointer.get("saveLayout"))),
                this.hotKeyTabName
        );
        /* Load Formations */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_loadLayout",
                "Load Formations",
                "Load all assignments, control groups and vanilla escort assignments from memory, positioned at the cursor location.",
                Keyboard.getKeyIndex(Character.toString((char)this.hotPointer.get("loadLayout"))),
                this.hotKeyTabName
        );
        /* Remove Assignment */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_deleteAssignments",
                "Remove Assignment",
                "Selected ships return to vanilla AI control.",
                Keyboard.getKeyIndex(Character.toString((char)this.hotPointer.get("deleteAssignments"))),
                this.hotKeyTabName
        );
        /* Vent Ships */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_vent",
                "Vent Ships",
                "Selected ships will attempt to vent flux.",
                Keyboard.getKeyIndex(Character.toString((char)this.hotPointer.get("vent"))),
                this.hotKeyTabName
        );
        /* Use Systems */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_useSystem",
                "Use Systems",
                "Selected ships will attempt to use their systems",
                Keyboard.getKeyIndex(Character.toString((char)this.hotPointer.get("useSystem"))),
                this.hotKeyTabName
        );
        /* Move Together */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_moveTogether",
                "Move Together",
                "Selected ships with assigments will maintain formation.",
                Keyboard.getKeyIndex(Character.toString((char)this.hotPointer.get("moveTogether"))),
                this.hotKeyTabName
        );
        /* Attack Move */
        LunaSettings.SettingsCreator.addKeybind(
                this.modID,
                "RTSA_SettingsKeybind_attackMove",
                "Attack Move",
                "Selected ships attack move to the designated position. Will convert any move assignments before creating a new assignment.",
                Keyboard.getKeyIndex(Character.toString((char)this.hotPointer.get("attackMove"))),
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
        /* Maximum Zoom */
        LunaSettings.SettingsCreator.addDouble(
                this.modID,
                "RTSA_SettingsConfig_maxZoom",
                "Maximum Zoom",
                "How far the player can zoom out.",
                (float)this.confPointer.get("maxZoom"),
                1d,
                10d,
                this.configTabName
        );
        /* Screen Scaling */
        LunaSettings.SettingsCreator.addInt(
                this.modID,
                "RTSA_SettingsConfig_screenScaling",
                "UI Scaling",
                "Manually adjust UI scaling. If using in game UI scaling this should not be necessary. If you are using nvidia upscaling for example, you will likely need to adjust this setting. Match the value with your scaling setting.",
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
    }
}
