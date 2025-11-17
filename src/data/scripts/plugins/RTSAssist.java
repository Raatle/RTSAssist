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

package data.scripts.plugins;

import java.util.*;
import java.util.List;

import API.RTS_API;
import API.RTS_API_BroadsideBehaviour;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.launcher.StarfarerLauncherUI;
import data.scripts.RTSAssistModPlugin;
import data.scripts.modInitilisation.RTS_CommonsControl;
import data.scripts.plugins.AISystems.RTS_AIInjector;
import data.scripts.plugins.BroadsideModifiers.*;
import data.scripts.plugins.DevTools.RTS_ShipTestSuite;
import data.scripts.plugins.Utils.*;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;

import data.scripts.plugins.BlockedSystems.*;

import static data.scripts.RTSAssistModPlugin.RTS_Global;

public class RTSAssist extends BaseEveryFrameCombatPlugin {

    /* Identifiers. Insures that set/getState will never have collisions and speeds up development. */
    public static class classidentifiers {
        public String amount = RTS_StatefulClasses.getUniqueIdentifier();
        public String engine = RTS_StatefulClasses.getUniqueIdentifier();
        public String isInit = RTS_StatefulClasses.getUniqueIdentifier();
        public String postInit = RTS_StatefulClasses.getUniqueIdentifier();
        public String isEnabled = RTS_StatefulClasses.getUniqueIdentifier();
        public String inDevelopment = RTS_StatefulClasses.getUniqueIdentifier();
        public String hotKeys = RTS_StatefulClasses.getUniqueIdentifier();
        public String config = RTS_StatefulClasses.getUniqueIdentifier();
        public String blockedSystems = RTS_StatefulClasses.getUniqueIdentifier();
        public String broadsideData = RTS_StatefulClasses.getUniqueIdentifier();
        public String cameraRework = RTS_StatefulClasses.getUniqueIdentifier();
        public String selectionListener = RTS_StatefulClasses.getUniqueIdentifier();
        public String taskManager = RTS_StatefulClasses.getUniqueIdentifier();
        public String parseInput = RTS_StatefulClasses.getUniqueIdentifier();
        public String draw = RTS_StatefulClasses.getUniqueIdentifier();
        public String eventManager = RTS_StatefulClasses.getUniqueIdentifier();
        public String modeManager = RTS_StatefulClasses.getUniqueIdentifier();
        public String AIInjector = RTS_StatefulClasses.getUniqueIdentifier();
        public String handleTimeMult = RTS_StatefulClasses.getUniqueIdentifier();
        public String testSuite = RTS_StatefulClasses.getUniqueIdentifier();
        public String soundsManager = RTS_StatefulClasses.getUniqueIdentifier();
        public String broadsideSelection = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public static classidentifiers stNames = new classidentifiers();
    /**/
    public static class amountIdentifiers {
        public String deltaFrame = RTS_StatefulClasses.getUniqueIdentifier();
        public String start = RTS_StatefulClasses.getUniqueIdentifier();
        public String elapsedPlay = RTS_StatefulClasses.getUniqueIdentifier();
        public String dilation = RTS_StatefulClasses.getUniqueIdentifier();
        public String frameMult = RTS_StatefulClasses.getUniqueIdentifier();
        public String rightClickDelta = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public static amountIdentifiers amNames = new amountIdentifiers();
    /**/
    public static class configIdentifiers {
        public String defaultModeIsRTS = RTS_StatefulClasses.getUniqueIdentifier();
        public String pauseUnpause = RTS_StatefulClasses.getUniqueIdentifier();
        public String scrollSpeed = RTS_StatefulClasses.getUniqueIdentifier();
        public String scrollSmoothing = RTS_StatefulClasses.getUniqueIdentifier();
        public String scrollSpeedKeyboard = RTS_StatefulClasses.getUniqueIdentifier();
        public String scrollSmoothingKeyboard = RTS_StatefulClasses.getUniqueIdentifier();
        public String maxZoom = RTS_StatefulClasses.getUniqueIdentifier();
        public String screenScaling = RTS_StatefulClasses.getUniqueIdentifier();
        public String shipsWillRun = RTS_StatefulClasses.getUniqueIdentifier();
        public String switchRightClick = RTS_StatefulClasses.getUniqueIdentifier();
        public String selectionTolerance = RTS_StatefulClasses.getUniqueIdentifier();
        public String rememberZoom = RTS_StatefulClasses.getUniqueIdentifier();
        public String alternativeRotation = RTS_StatefulClasses.getUniqueIdentifier();
        public String UICommandVolume = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public static configIdentifiers coNames = new configIdentifiers();
    /**/
    public static class hotKeyIdentifiers {
        public String enable_RTSMode = RTS_StatefulClasses.getUniqueIdentifier();
        public String saveLayout = RTS_StatefulClasses.getUniqueIdentifier();
        public String loadLayout = RTS_StatefulClasses.getUniqueIdentifier();
        public String deleteAssignments = RTS_StatefulClasses.getUniqueIdentifier();
        public String vent = RTS_StatefulClasses.getUniqueIdentifier();
        public String useSystem = RTS_StatefulClasses.getUniqueIdentifier();
        public String moveTogether = RTS_StatefulClasses.getUniqueIdentifier();
        public String attackMove = RTS_StatefulClasses.getUniqueIdentifier();
        public String strafeCameraLeft = RTS_StatefulClasses.getUniqueIdentifier();
        public String strafeCameraRight = RTS_StatefulClasses.getUniqueIdentifier();
        public String strafeCameraUp = RTS_StatefulClasses.getUniqueIdentifier();
        public String strafeCameraDown = RTS_StatefulClasses.getUniqueIdentifier();
        public String broadsideSelection = RTS_StatefulClasses.getUniqueIdentifier();
        public String param1 = RTS_StatefulClasses.getUniqueIdentifier();
        public String param2 = RTS_StatefulClasses.getUniqueIdentifier();
        public String param3 = RTS_StatefulClasses.getUniqueIdentifier();
        public String param4 = RTS_StatefulClasses.getUniqueIdentifier();
        public String param5 = RTS_StatefulClasses.getUniqueIdentifier();
        public String param6 = RTS_StatefulClasses.getUniqueIdentifier();
        public String param7 = RTS_StatefulClasses.getUniqueIdentifier();
        public String param8 = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public static hotKeyIdentifiers hoNames = new hotKeyIdentifiers();
    /**/
    public static class DevToolIdentifiers {
        public String modID = RTS_StatefulClasses.getUniqueIdentifier();
        public String findAllShips = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public static DevToolIdentifiers devNames = new DevToolIdentifiers();
    /**/
    public static class shipCustomIdentifiers {
        public String blockAi = "RTS_BLOCKAI_ForYourOwnSakeDontUseThis";
        public String overrideThrust = "RTS_OVERRIDETHRUST_ForYourOwnSakeDontUseThis";
        public String overrideFacing = "RTS_OVERRIDEFACING_ForYourOwnSakeDontUseThis";
        public String overrideBlockSystem = "RTS_OVERRIDEBLOCKSYSTEM_ForYourOwnSakeDontUseThis";
    }
    public static shipCustomIdentifiers shipCNames = new shipCustomIdentifiers();

    /* Java doesnt allow multiple inheritance. Here we instead create a wrapper that functions similar to inheriting
    *  RTS_StatefulClassses. Should be replaced with an interface, possibly. */
    private HashMap<String, Object> state;
    private RTS_StatefulClasses wrapper;
    public void setState (String identifier, Object value) {this.wrapper.setState(identifier, value);}
    public void setState (HashMap<String, Object> keyValueList) {this.wrapper.setState(keyValueList);}
    public void setDeepState(List<String> identBreadCrumb, Object value) {this.wrapper.setDeepState(identBreadCrumb, value);}
    public Object getState (String identifier) {return(this.wrapper.getState(identifier));}
    public Object getDeepState (List<String> identBreadCrumb) {return (this.wrapper.getDeepState(identBreadCrumb));}

    /* Initialise wrapper, which is an instance of statefulClesses and provides state functionality.
    *  Useful for cross class communication. */
    public RTSAssist () {
        this.wrapper = new RTS_StatefulClasses();
        this.state = wrapper.returnState();
    }

    /* Initialises all primary classes and retrieves configuration data and hotkeys. */
    @Override
    public void init(CombatEngineAPI engine) {
        HashMap<String, Object> init = new HashMap<String, Object>() {{
            put(RTSAssist.stNames.amount, new HashMap<String, Object>());
            put(RTSAssist.stNames.engine, null);
            put(RTSAssist.stNames.isInit, false);
            put(RTSAssist.stNames.postInit, false);
            put(RTSAssist.stNames.isEnabled, null);
            put(RTSAssist.stNames.inDevelopment, null);
            put(RTSAssist.stNames.hotKeys, null);
            put(RTSAssist.stNames.config, null);
            put(RTSAssist.stNames.blockedSystems, null);
            put(RTSAssist.stNames.broadsideData, null);
        }};
        init.put(RTSAssist.stNames.engine, engine);
        this.setState(init);
        this.setDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.start), 0f);
        this.setDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.elapsedPlay), 0f);
        this.getHotkeys();
        this.getConfig();
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
            LunaSettings.addSettingsListener(new LunaSettingsListener() {
                @Override
                public void settingsChanged(@NotNull String s) {
                    getHotkeys();
                    getConfig();
                }
            });
        this.setState(RTSAssist.stNames.eventManager, new RTS_EventManager(this.state));
        HashMap<String, Object> initClasses = new HashMap<>();
        initClasses.put(RTSAssist.stNames.cameraRework, new RTS_CameraRework(this.state));
        initClasses.put(RTSAssist.stNames.selectionListener, new RTS_SelectionListener(this.state));
        initClasses.put(RTSAssist.stNames.taskManager, new RTS_TaskManager(this.state));
        initClasses.put(RTSAssist.stNames.parseInput, new RTS_ParseInput(this.state));
        initClasses.put(RTSAssist.stNames.draw, new RTS_Draw(this.state));
        initClasses.put(RTSAssist.stNames.modeManager, new RTS_ModeManager(this.state));
        initClasses.put(RTSAssist.stNames.AIInjector, new RTS_AIInjector(this.state));
        initClasses.put(RTSAssist.stNames.handleTimeMult, new RTS_HandleTimeMult(this.state));
        initClasses.put(RTSAssist.stNames.soundsManager, new RTS_SoundsManager(this.state));
        initClasses.put(RTSAssist.stNames.broadsideSelection, new RTS_BroadsideSelection(this.state));
        initClasses.put(
                RTSAssist.stNames.testSuite,
                (boolean)this.getState(RTSAssist.stNames.inDevelopment) ? new RTS_ShipTestSuite(this.state) : null
        );
        this.setState(initClasses);
        this.setState(RTSAssist.stNames.isInit, true);
        inputParser = (RTS_ParseInput)this.getState(RTSAssist.stNames.parseInput);
    }

    public RTS_ParseInput inputParser = null;

    /* Write config into state */
    private void getConfig () {
        HashMap<String, Object> confPointer = (HashMap<String, Object>)RTS_Global.get("config");
        HashMap<String, Object> initConfig = new HashMap<String, Object>() {{
            if (!Global.getSettings().getModManager().isModEnabled("lunalib")) {
                put(RTSAssist.coNames.defaultModeIsRTS, confPointer.get("defaultModeIsRTS"));
                put(RTSAssist.coNames.pauseUnpause, confPointer.get("pauseUnpause"));
                put(RTSAssist.coNames.scrollSpeed, confPointer.get("scrollSpeed")); //5f
                put(RTSAssist.coNames.scrollSmoothing, confPointer.get("scrollSmoothing")); //7f
                put(RTSAssist.coNames.scrollSpeedKeyboard, confPointer.get("scrollSpeedKeyboard")); //20f
                put(RTSAssist.coNames.scrollSmoothingKeyboard, confPointer.get("scrollSmoothingKeyboard")); //7f
                put(RTSAssist.coNames.maxZoom, confPointer.get("maxZoom")); //3f
                put(RTSAssist.coNames.screenScaling, confPointer.get("screenScaling"));
                put(RTSAssist.coNames.switchRightClick, confPointer.get("switchRightClick"));
                put(RTSAssist.coNames.selectionTolerance, confPointer.get("selectionTolerance"));
                put(RTSAssist.coNames.rememberZoom, confPointer.get("rememberZoom"));
                put(RTSAssist.coNames.alternativeRotation, confPointer.get("alternativeRotation"));
                put(RTSAssist.coNames.UICommandVolume, confPointer.get("UICommandVolume"));
                put(RTSAssist.devNames.modID, confPointer.get("modID"));
                put(RTSAssist.devNames.findAllShips, confPointer.get("findAllShips"));
            }
            else {
                put(RTSAssist.coNames.defaultModeIsRTS, LunaSettings.getBoolean("RTSAssist", "RTSA_SettingsConfig_defaultModeIsRTS"));
                put(RTSAssist.coNames.pauseUnpause, LunaSettings.getBoolean("RTSAssist", "RTSA_SettingsConfig_pauseUnpause"));
                put(RTSAssist.coNames.scrollSpeed, LunaSettings.getInt("RTSAssist", "RTSA_SettingsConfig_scrollSpeed").floatValue()); //5f
                put(RTSAssist.coNames.scrollSmoothing, LunaSettings.getInt("RTSAssist", "RTSA_SettingsConfig_scrollSmoothing").floatValue()); //7f
                put(RTSAssist.coNames.scrollSpeedKeyboard, LunaSettings.getInt("RTSAssist", "RTSA_SettingsConfig_scrollSpeedKeyboard").floatValue()); //20f
                put(RTSAssist.coNames.scrollSmoothingKeyboard, LunaSettings.getInt("RTSAssist", "RTSA_SettingsConfig_scrollSmoothingKeyboard").floatValue()); //7f
                put(RTSAssist.coNames.maxZoom, MathUtils.clamp(LunaSettings.getDouble("RTSAssist", "RTSA_SettingsConfig_maxZoom").floatValue(), 1f,  50f)); //3f
                put(RTSAssist.coNames.screenScaling, LunaSettings.getInt("RTSAssist", "RTSA_SettingsConfig_screenScaling").floatValue());
                put(RTSAssist.coNames.switchRightClick, LunaSettings.getBoolean("RTSAssist", "RTSA_SettingsConfig_switchRightClick"));
                put(RTSAssist.coNames.selectionTolerance, LunaSettings.getInt("RTSAssist", "RTSA_SettingsConfig_selectionTolerance").floatValue());
                put(RTSAssist.coNames.rememberZoom, LunaSettings.getBoolean("RTSAssist", "RTSA_SettingsConfig_rememberZoom"));
                put(RTSAssist.coNames.alternativeRotation, LunaSettings.getBoolean("RTSAssist", "RTSA_SettingsConfig_alternativeRotation"));
                put(RTSAssist.coNames.UICommandVolume, LunaSettings.getInt("RTSAssist", "RTSA_SettingsUI_CommandVolume").floatValue());
                put(RTSAssist.devNames.modID, LunaSettings.getString("RTSAssist","RTSA_SettingsDevTools_modID"));
                put(RTSAssist.devNames.findAllShips, LunaSettings.getBoolean("RTSAssist", "RTSA_SettingsDevTools_findAllShips"));
            }
            put(RTSAssist.coNames.shipsWillRun, confPointer.get("shipsWillRun"));
        }};
        this.setState(RTSAssist.stNames.config, initConfig);
        this.setState(RTSAssist.stNames.isEnabled, RTS_Global.get("isEnabled"));
        this.setState(RTSAssist.stNames.inDevelopment,
                (boolean)RTS_Global.get(RTSAssistModPlugin.names.inDevelopment)
                || (Global.getSettings().getModManager().isModEnabled("lunalib")
                && LunaSettings.getBoolean("RTSAssist", "RTSA_SettingsDevTools_enableShipTestSuite"))
        );
    }

    /* Write hotkeys into state */
    private void getHotkeys () {
        HashMap<String, Object> hotPointer = (HashMap<String, Object>)RTS_Global.get("hotKeys");
        HashMap<String, Object> initHotkeys = new HashMap<String, Object>(){{
            if (!Global.getSettings().getModManager().isModEnabled("lunalib")) {
                put(RTSAssist.hoNames.enable_RTSMode, hotPointer.get("enable_RTSMode"));
                put(RTSAssist.hoNames.saveLayout, hotPointer.get("saveLayout"));
                put(RTSAssist.hoNames.loadLayout, hotPointer.get("loadLayout"));
                put(RTSAssist.hoNames.deleteAssignments, hotPointer.get("deleteAssignments"));
                put(RTSAssist.hoNames.vent, hotPointer.get("vent"));
                put(RTSAssist.hoNames.useSystem, hotPointer.get("useSystem"));
                put(RTSAssist.hoNames.moveTogether, hotPointer.get("moveTogether"));
                put(RTSAssist.hoNames.attackMove, hotPointer.get("attackMove"));
                put(RTSAssist.hoNames.strafeCameraLeft, hotPointer.get("strafeCameraLeft"));
                put(RTSAssist.hoNames.strafeCameraRight, hotPointer.get("strafeCameraRight"));
                put(RTSAssist.hoNames.strafeCameraUp, hotPointer.get("strafeCameraUp"));
                put(RTSAssist.hoNames.strafeCameraDown, hotPointer.get("strafeCameraDown"));
                put(RTSAssist.hoNames.broadsideSelection, hotPointer.get("broadsideSelection"));
            }
            else {
                put(RTSAssist.hoNames.enable_RTSMode, LunaSettings.getInt(
                        "RTSAssist",
                        "RTSA_SettingsKeybind_enable_RTSMode").equals(Keyboard.KEY_CAPITAL)
                                ? null
                                : Keyboard.getKeyName(LunaSettings.getInt("RTSAssist", "RTSA_SettingsKeybind_enable_RTSMode"))
                );
                put(RTSAssist.hoNames.saveLayout, Keyboard.getKeyName(LunaSettings.getInt("RTSAssist", "RTSA_SettingsKeybind_saveLayout")));
                put(RTSAssist.hoNames.loadLayout, Keyboard.getKeyName(LunaSettings.getInt("RTSAssist", "RTSA_SettingsKeybind_loadLayout")));
                put(RTSAssist.hoNames.deleteAssignments, Keyboard.getKeyName(LunaSettings.getInt("RTSAssist", "RTSA_SettingsKeybind_deleteAssignments")));
                put(RTSAssist.hoNames.vent, Keyboard.getKeyName(LunaSettings.getInt("RTSAssist", "RTSA_SettingsKeybind_vent")));
                put(RTSAssist.hoNames.useSystem, Keyboard.getKeyName(LunaSettings.getInt("RTSAssist", "RTSA_SettingsKeybind_useSystem")));
                put(RTSAssist.hoNames.moveTogether, Keyboard.getKeyName(LunaSettings.getInt("RTSAssist", "RTSA_SettingsKeybind_moveTogether")));
                put(RTSAssist.hoNames.attackMove, Keyboard.getKeyName(LunaSettings.getInt("RTSAssist", "RTSA_SettingsKeybind_attackMove")));
                put(RTSAssist.hoNames.strafeCameraLeft, Keyboard.getKeyName(LunaSettings.getInt("RTSAssist", "RTSA_SettingsKeybind_strafeLeft")));
                put(RTSAssist.hoNames.strafeCameraRight, Keyboard.getKeyName(LunaSettings.getInt("RTSAssist", "RTSA_SettingsKeybind_strafeRight")));
                put(RTSAssist.hoNames.strafeCameraUp, Keyboard.getKeyName(LunaSettings.getInt("RTSAssist", "RTSA_SettingsKeybind_strafeUp")));
                put(RTSAssist.hoNames.strafeCameraDown, Keyboard.getKeyName(LunaSettings.getInt("RTSAssist", "RTSA_SettingsKeybind_strafeDown")));
                put(RTSAssist.hoNames.broadsideSelection, Keyboard.getKeyName(LunaSettings.getInt("RTSAssist", "RTSA_SettingsKeybind_broadsideSelection")));
            }
            put(RTSAssist.hoNames.param1, hotPointer.get("param1"));
            put(RTSAssist.hoNames.param2, hotPointer.get("param2"));
            put(RTSAssist.hoNames.param3, hotPointer.get("param3"));
            put(RTSAssist.hoNames.param4, hotPointer.get("param4"));
            put(RTSAssist.hoNames.param5, hotPointer.get("param5"));
            put(RTSAssist.hoNames.param6, hotPointer.get("param6"));
            put(RTSAssist.hoNames.param7, hotPointer.get("param7"));
            put(RTSAssist.hoNames.param8, hotPointer.get("param8"));
        }};
        this.setState(RTSAssist.stNames.hotKeys, initHotkeys);
    }

    /* Write system modifications into state */
    private void getBlockedSystems() {
        HashMap<String, RTS_BlockSystemPlugin> hold = new HashMap<>();
        new RTS_VanillaSystems().init();
        new RTS_Indies_Expansion_Pack().init();
        new RTS_DiableSystems().init();
        new RTS_IronShellSystems().init();
        new RTS_KoLSystems().init();
        new RTS_UAF().init();
        new RTS_Tahlaan().init();
        new RTS_JaydeePiracy().init();
        new RTS_CSP().init();
        new RTS_Volantian().init();
        new RTS_Valkyrie().init();
        new RTS_Xhan().init();
        new RTS_HazardMiningBase().init();
        new RTS_HazardMiningBrighton().init();
        new RTS_HazardMiningSuperVillans().init();
        new RTS_RAT().init();
        new RTS_StarWars().init();
        new RTS_Koc().init();
        new RTS_ApexDesign().init();
        new RTS_MachinaVoid().init();
        new RTS_HaloDynamics().init();
        new RTS_InterstellarImperium().init();
        new RTS_PhilAndrada().init();
        new RTS_ScalarTech().init();
        new RTS_ScyNation().init();
        new RTS_SOTF().init();
        new RTS_Volkov().init();
        new RTS_ORA().init();
        hold.putAll(RTS_BS_Utils.getModifiers());
        hold.putAll(RTS_API.getSystemModifiers());
        this.setState(RTSAssist.stNames.blockedSystems, hold);
    }

    /* Write broadside modifications into state */
    private void getBroadsideShipData () {
        HashMap<String, Object> hold = new HashMap<>();
        hold.putAll(new RTS_VanillaBroadsides().getBroadSideData());
        hold.putAll(new RTS_UAF_Broadsides().getBroadSideData());
        hold.putAll(new RTS_Tahlaan_Broadsides().getBroadSideData());
        hold.putAll(new RTS_Jaydee_Broadsides().getBroadSideData());
        hold.putAll(new RTS_Valkyrie_Broadsides().getBroadSideData());
        hold.putAll(new RTS_Xhan_Broadsides().getBroadSideData());
        hold.putAll(new RTS_HMIBase_Broadsides().getBroadSideData());
        hold.putAll(new RTS_ApexDesign_Broadsides().getBroadSideData());
        hold.putAll(new RTS_ScyNation_Broadsides().getBroadSideData());
        hold.putAll(new RTS_Imperial_Broadsides().getBroadSideData());
        hold.putAll(new RTS_ORA_Broadsides().getBroadSideData());

        /* temporary */
        HashMap<String, Object> hold2 = new HashMap<>();
        for (Map.Entry<String, Object> broadsideMod : hold.entrySet()) {
            hold2.put(broadsideMod.getKey(), new Vector2f(
                    (float)broadsideMod.getValue(),
                    (float)broadsideMod.getValue() * -1f
            ));
        }

        hold2.putAll(RTS_API.getBroadsideModifiers());

        RTS_CommonsControl commonsControl = (RTS_CommonsControl)RTS_Global.get(RTSAssistModPlugin.names.commonsControl);
        commonsControl.updateCache();
        JSONObject broadsideData;
        try {
            broadsideData = (JSONObject)commonsControl.get(
                    "broadsides_" + (String)RTS_Global.get(RTSAssistModPlugin.names.gameID),
                    RTS_CommonsControl.JSONType.JSONOBJECT
            );
            if (broadsideData != null) {
                CombatFleetManagerAPI fleetManager = ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getFleetManager(0);
                Iterator<String> E = broadsideData.keys();
                String mapName;
                JSONObject modPointer;
                String shipID;
                HashMap<String, FleetMemberAPI> memberHold = new HashMap<>();
                for (FleetMemberAPI member : fleetManager.getDeployedCopy())
                    memberHold.put(member.getId(), member);
                while (E.hasNext()) {
                    mapName = E.next();
                    if (memberHold.containsKey(mapName)) {
                        modPointer = (JSONObject)broadsideData.get(mapName);
                        shipID = fleetManager.getShipFor(memberHold.get(mapName)).getId();
                        hold2.put(shipID, new Vector2f(
                                ((Double)modPointer.getDouble("angle")).floatValue(),
                                modPointer.getBoolean("reflected")
                                        ? ((Double)modPointer.getDouble("angle")).floatValue()
                                        : 361f
                        ));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        this.setState(RTSAssist.stNames.broadsideData, hold2);
    }

    /* Writes ingame time progression into state. */
    private void getTimers(float amount) {
        this.setDeepState(
                Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.deltaFrame),
                amount
        );
        this.setDeepState(
                Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.start),
                (float)this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.start)) + amount
        );
        if (!((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).isPaused())
            this.setDeepState(
                    Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.elapsedPlay),
                    (float)this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.elapsedPlay)) + amount
            );
        this.setDeepState(
                Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.dilation),
                Global.getCombatEngine().getTimeMult().getModifiedValue()
        );
        this.setDeepState(
                Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.frameMult),
                0.01666f / amount
        );
    }

    /* Provides input preprocessor hooking. */
    public void processInputPreCoreControls (float amount, List<InputEventAPI> events) {
        if (
                inputParser != null
                && !Global.getCurrentState().name().equals("TITLE")
                && this.getState(RTSAssist.stNames.isInit) != null && (boolean)this.getState(RTSAssist.stNames.isInit)
                && (boolean)this.getState(RTSAssist.stNames.isEnabled)
                && ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getCombatUI() != null
        )
            inputParser.update(amount, events);
    }

    /* Checks to see if RTSAssist should be enabled and function */
    private boolean dontDoIt () {
        return (
                (!(this.state != null
                        && this.state.containsKey(RTSAssist.stNames.isInit)
                        && (Boolean)this.state.get(RTSAssist.stNames.isInit)))
                || !(boolean)this.getState(RTSAssist.stNames.isEnabled)
                || ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getCombatUI() == null
                || (((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getMissionId() != null
                        && ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getMissionId().equals("gl_benchmark"))
        );
    }

    /* Called on the very first call of this.advance */
    public void postInit () {
        this.getBroadsideShipData();
        this.getBlockedSystems();
        this.setState(RTSAssist.stNames.postInit, true);
    }

    /* This is the primary loop. It is called each frame by the starsectore core engine. This is where all RTSAssists
    *  work is done.*/
    @Override
    public void advance(float amount, List <InputEventAPI> events) {
        if (this.dontDoIt())
            return;
        if (!(boolean)this.getState(RTSAssist.stNames.postInit))
            this.postInit();
        ((RTS_HandleTimeMult)this.getState(RTSAssist.stNames.handleTimeMult)).update();
        this.getTimers(amount);

        ((RTS_Draw)this.getState(RTSAssist.stNames.draw)).checkIfShouldDraw();
        ((RTS_Draw)this.getState(RTSAssist.stNames.draw)).open();
        ((RTS_SoundsManager)this.getState(RTSAssist.stNames.soundsManager)).update();
        ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager)).update();
        ((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener)).drawSelection(
                (Vector2f)this.getState(RTS_ParseInput.stNames.screenSpace),
                (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)
        );
        ((RTS_BroadsideSelection)this.getState(RTSAssist.stNames.broadsideSelection)).drawBSSSelection();
        ((RTS_TaskManager)this.getState(RTSAssist.stNames.taskManager)).update(
                (float)this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.elapsedPlay))
        );
        ((RTS_CameraRework)this.getState(RTSAssist.stNames.cameraRework)).update(
                (Vector2f)this.getState(RTS_ParseInput.stNames.screenSpace),
                (int)this.getState(RTS_ParseInput.stNames.isZooming)
        );
        ((RTS_ParseInput)this.getState(RTSAssist.stNames.parseInput)).tempInterface();
        ((RTS_Draw)this.getState(RTSAssist.stNames.draw)).close();

        ((RTS_CommonsControl)RTS_Global.get(RTSAssistModPlugin.names.commonsControl)).handleWriteParallel();
    }

}