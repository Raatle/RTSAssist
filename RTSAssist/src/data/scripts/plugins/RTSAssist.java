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

package data.scripts.plugins;

import java.util.*;

import data.scripts.plugins.BroadsideModifiers.RTS_VanillaBroadsides;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;

import data.scripts.plugins.Utils.RTS_StatefulClasses;
import data.scripts.plugins.Utils.RTS_Draw;
import data.scripts.plugins.BlockedSystems.*;

public class RTSAssist extends BaseEveryFrameCombatPlugin {

    //Java doesnt allow multiple inheritance. Here we instead create a wrapper that functions similar.//

    private HashMap<String, Object> state;
    private RTS_StatefulClasses wrapper;
    public void setState (String identifier, Object value) {this.wrapper.setState(identifier, value);}
    public void setState (HashMap<String, Object> keyValueList) {this.wrapper.setState(keyValueList);}
    public void setDeepState(List<String> identBreadCrumb, Object value) {this.wrapper.setDeepState(identBreadCrumb, value);}
    public Object getState (String identifier) {return(this.wrapper.getState(identifier));}
    public Object getDeepState (List<String> identBreadCrumb) {return (this.wrapper.getDeepState(identBreadCrumb));}

    //Initialise wrapper, which is an instance of statefulClesses and provides state functionality.//
    //Useful for cross class communication.//

    public RTSAssist () {
        this.wrapper = new RTS_StatefulClasses();
        this.state = wrapper.returnState();
    }

    // Initialises all primary classes and retrieves configuration data.//

    @Override
    public void init(CombatEngineAPI engine) {
        HashMap<String, Object> init = new HashMap<String, Object>() {{
            put("amount", new HashMap<String, Object>());
            put("engine", null);
            put("isInit", false);
            put("isEnabled", null);
            put("hotKeys", null);
            put("config", null);
            put("blockedSystems", null);
            put("broadsideData", null);
        }};
        init.put("engine", engine);
        this.setState(init);
        this.setDeepState(Arrays.asList("amount", "start"), 0f);
        this.setDeepState(Arrays.asList("amount", "elapsedPlay"), 0f);
        this.getHotkeys();
        this.getConfig();
        this.getBroadsideShipData();
        this.getBlockedSystems();
        HashMap<String, Object> initClasses = new HashMap<>();
        initClasses.put("cameraRework", new RTS_CameraRework(this.state));
        initClasses.put("selectionListener", new RTS_SelectionListener(this.state));
        initClasses.put("taskManager", new RTS_TaskManager(this.state));
        initClasses.put("parseInput", new RTS_ParseInput(this.state));
        initClasses.put("draw", new RTS_Draw(this.state));
        this.setState(initClasses);
        this.setState("isInit", true);
        inputParser = (RTS_ParseInput)this.getState("parseInput");
    }

    public RTS_ParseInput inputParser = null;

    private void getConfig () {
        if (Global.getSector().getMemory().get("$RTSASSIST_saveData") != null &&
                ((HashMap<String, Object>)Global.getSector().getMemory().get("$RTSASSIST_saveData"))
                        .get("config") != null) {
            this.setState("config", ((HashMap<String, Object>)Global
                    .getSector().getMemory().get("$RTSASSIST_saveData")).get("config"));
            this.setState("isEnabled", ((HashMap<String, Object>)Global
                    .getSector().getMemory().get("$RTSASSIST_saveData")).get("isEnabled"));
            return;
        }
        HashMap<String, Object> defaultConfig = new HashMap<String, Object>() {{
            put("scrollSpeed", 20f);
            put("maxZoom", 3f);
        }};
        this.setState("config", defaultConfig);
        this.setState("isEnabled", true);
    }

    private void getHotkeys () {
        if (Global.getSector().getMemory().get("$RTSASSIST_saveData") != null &&
                ((HashMap<String, Object>)Global.getSector().getMemory().get("$RTSASSIST_saveData"))
                        .get("hotKeys") != null) {
            this.setState("hotKeys", ((HashMap<String, Object>)Global
                    .getSector().getMemory().get("$RTSASSIST_saveData")).get("hotKeys"));
            return;
        }
        HashMap<String, Object> defaultHotKeys = new HashMap<String, Object>() {{
            put("enable_disable", 'E');
            put("saveLayout", 'Q');
            put("loadLayout", 'W');
            put("deleteAssignments", 'X');
            put("vent", 'C');
        }};
        this.setState("hotKeys", defaultHotKeys);
    }

    private void getBlockedSystems() {
        HashMap<String, RTS_BlockSystemPlugin> hold = new HashMap<>();
        hold.putAll(new RTS_VanillaSystems().getBlockedSystems());
        this.setState("blockedSystems", hold);
    }

    private void getBroadsideShipData () {
        HashMap<String, Object> hold = new HashMap<>();
        hold.putAll(new RTS_VanillaBroadsides().getBroadSideData());
        this.setState("broadsideData", hold);
    }

    // Provides input preprocessor hooking.//

    public void processInputPreCoreControls (float amount, List<InputEventAPI> events) {
        if (inputParser != null
                && !Global.getCurrentState().name().equals("TITLE")
                && (boolean)this.getState("isEnabled")
        )
            inputParser.update(amount, events);
    }

    // Writes ingame time progression into state.//

    public void getTimers(float amount) {
        this.setDeepState(
                Arrays.asList("amount", "start"),
                (float)this.getDeepState(Arrays.asList("amount", "start")) + amount
        );
        if (!((CombatEngineAPI)this.getState("engine")).isPaused())
            this.setDeepState(
                    Arrays.asList("amount", "elapsedPlay"),
                    (float)this.getDeepState(Arrays.asList("amount", "elapsedPlay")) + amount
            );
    }

    // This is the primary loop. It is called each frame. This is where all the work is done.//

    @Override
    public void advance(float amount, List <InputEventAPI> events) {
        if ((!(this.state != null
                && this.state.containsKey("isInit")
                && (Boolean)this.state.get("isInit")))
                || !(boolean)this.getState("isEnabled")
        )
            return;
        ((RTS_Draw)this.getState("draw")).checkIfShouldDraw();
        ((RTS_Draw)this.getState("draw")).open();
        this.getTimers(amount);
        ((RTS_CameraRework)this.getState("cameraRework")).update(
                (Vector2f)this.getState("screenSpace"),
                (int)this.getState("isZooming")
        );
        ((RTS_SelectionListener)this.getState("selectionListener")).drawSelection(
                (Vector2f)this.getState("screenSpace"),
                (List<ShipAPI>)this.getState("currentSelection")
        );
        ((RTS_TaskManager)this.getState("taskManager")).update(
                (float)this.getDeepState(Arrays.asList("amount", "elapsedPlay"))
        );
        ((RTS_ParseInput)this.getState("parseInput")).tempInterface();
        ((RTS_Draw)this.getState("draw")).close();
    }
}