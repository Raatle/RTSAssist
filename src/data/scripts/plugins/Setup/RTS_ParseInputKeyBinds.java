package data.scripts.plugins.Setup;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.RTSAssistModPlugin;
import data.scripts.plugins.*;
import data.scripts.plugins.DevTools.RTS_ShipTestSuite;
import data.scripts.plugins.Utils.RTS_StateEngine;
import data.scripts.plugins.Utils.RTS_StatefulClasses;
import data.scripts.plugins.Utils.RTS_SystemManager;
import data.scripts.plugins.Utils.RTS_VentManager;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.util.vector.Vector2f;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RTS_ParseInputKeyBinds extends RTS_StatefulClasses {


    public static class classidentifiers {
        public String eventChar = RTS_StatefulClasses.getUniqueIdentifier();
        public String eventValue = RTS_StatefulClasses.getUniqueIdentifier();
        public String inDevelopment = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public static RTS_ParseInputKeyBinds.classidentifiers kiNames = new RTS_ParseInputKeyBinds.classidentifiers();

    public RTS_ParseInputKeyBinds (HashMap<String, Object> state) {
        super(state);
        this.buildKeyBinds();
        this.dev = (boolean)this.getState(RTSAssist.stNames.inDevelopment);
    }

    private HashMap<String, Object> hotKeys = (HashMap<String, Object>)this.getState(RTSAssist.stNames.hotKeys);
    InputEventAPI x = null;
    RTS_StateEngine keyEngine = new RTS_StateEngine();
    boolean dev;

    public void buildKeyBinds () {
        this.keyEngine.addConditionSet(this.param1());
        this.keyEngine.addConditionSet(this.param2());
        this.keyEngine.addConditionSet(this.param3());
        this.keyEngine.addConditionSet(this.param4());
        this.keyEngine.addConditionSet(this.param5());
        this.keyEngine.addConditionSet(this.param6());
        this.keyEngine.addConditionSet(this.param7());
        this.keyEngine.addConditionSet(this.param8());
        this.keyEngine.addConditionSet(this.strafeLeft());
        this.keyEngine.addConditionSet(this.strafeRight());
        this.keyEngine.addConditionSet(this.strafeUp());
        this.keyEngine.addConditionSet(this.strafeDown());
        this.keyEngine.addConditionSet(this.saveLayout());
        this.keyEngine.addConditionSet(this.loadLayout());
        this.keyEngine.addConditionSet(this.moveTogether());
        this.keyEngine.addConditionSet(this.attackMove());
        this.keyEngine.addConditionSet(this.deleteAssignments());
        this.keyEngine.addConditionSet(this.vent());
        this.keyEngine.addConditionSet(this.useSystem());
        this.keyEngine.addConditionSet(this.eventVal45());
        this.keyEngine.addConditionSet(this.leftControl());
        this.keyEngine.addConditionSet(this.controlGroups());
    }

    public void execBinds (HashMap<String, Object> checkList) {
        this.keyEngine.doCheck(checkList);
    }

    public void setEventAPI (InputEventAPI x) {
        this.x = x;
    }

    /* TEMPLATE */
    private RTS_StateEngine.conditionSet temp () {
        RTS_StateEngine.conditionSet temp = new RTS_StateEngine.conditionSet();
        temp.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                return null;
            }
        });
        temp.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "";
            }

            @Override
            public Boolean check(Object var) {
                return null;
            }
        });
        return (temp);
    }

    /* PARAM1_DEV :: H */
    private RTS_StateEngine.conditionSet param1 () {
        RTS_StateEngine.conditionSet param1 = new RTS_StateEngine.conditionSet();
        param1.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((RTS_ShipTestSuite)getState(RTSAssist.stNames.testSuite)).deleteShips(
                        (List<ShipAPI>)getState(RTS_ParseInput.stNames.currentSelection)
                );
                x.consume();
                return (null);
            }
        });
        param1.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }

            @Override
            public Boolean check(Object var) {
                if (!dev)
                    return (false);
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.param1)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.param1) + 32));
            }
        });
        return (param1);
    }

    /* PARAM2_DEV :: J */
    private RTS_StateEngine.conditionSet param2 () {
        RTS_StateEngine.conditionSet param2 = new RTS_StateEngine.conditionSet();
        param2.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((RTS_ShipTestSuite)getState(RTSAssist.stNames.testSuite)).soutShipDetails(
                        (List<ShipAPI>)getState(RTS_ParseInput.stNames.currentSelection)
                );
                x.consume();
                return (null);
            }
        });
        param2.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }

            @Override
            public Boolean check(Object var) {
                if (!dev)
                    return (false);
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.param2)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.param2) + 32));
            }
        });
        return (param2);
    }

    /* PARAM3_DEV :: K */
    private RTS_StateEngine.conditionSet param3 () {
        RTS_StateEngine.conditionSet param3 = new RTS_StateEngine.conditionSet();
        param3.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((RTS_ShipTestSuite)getState(RTSAssist.stNames.testSuite)).spawnShipIter(
                        (Vector2f)getState(RTS_ParseInput.stNames.worldSpace)
                );
                x.consume();
                return (null);
            }
        });
        param3.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }

            @Override
            public Boolean check(Object var) {
                if (!dev)
                    return (false);
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.param3)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.param3) + 32));
            }
        });
        return (param3);
    }

    /* PARAM4_DEV :: Y */
    private RTS_StateEngine.conditionSet param4 () {
        RTS_StateEngine.conditionSet param4 = new RTS_StateEngine.conditionSet();
        param4.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((RTS_ShipTestSuite)getState(RTSAssist.stNames.testSuite)).addShipsToCommons(
                        (List<ShipAPI>)getState(RTS_ParseInput.stNames.currentSelection)
                );
                x.consume();
                return (null);
            }
        });
        param4.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }

            @Override
            public Boolean check(Object var) {
                if (!dev)
                    return (false);
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.param4)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.param4) + 32));
            }
        });
        return (param4);
    }

    /* PARAM5_DEV :: U */
    private RTS_StateEngine.conditionSet param5 () {
        RTS_StateEngine.conditionSet param5 = new RTS_StateEngine.conditionSet();
        param5.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((RTS_ShipTestSuite)getState(RTSAssist.stNames.testSuite)).removeShipsFromCommons(
                        (List<ShipAPI>)getState(RTS_ParseInput.stNames.currentSelection)
                );
                x.consume();
                return (null);
            }
        });
        param5.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }

            @Override
            public Boolean check(Object var) {
                if (!dev)
                    return (false);
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.param5)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.param5) + 32));
            }
        });
        return (param5);
    }

    /* PARAM6_DEV :: I */
    private RTS_StateEngine.conditionSet param6 () {
        RTS_StateEngine.conditionSet param6 = new RTS_StateEngine.conditionSet();
        param6.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((RTS_ShipTestSuite)getState(RTSAssist.stNames.testSuite)).spawnCommonsShipIter(
                        (Vector2f)getState(RTS_ParseInput.stNames.worldSpace)
                );
                x.consume();
                return (null);
            }
        });
        param6.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }

            @Override
            public Boolean check(Object var) {
                if (!dev)
                    return (false);
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.param6)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.param6) + 32));
            }
        });
        return (param6);
    }

    /* PARAM7_DEV :: N */
    private RTS_StateEngine.conditionSet param7 () {
        RTS_StateEngine.conditionSet param7 = new RTS_StateEngine.conditionSet();
        param7.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((RTS_ShipTestSuite)getState(RTSAssist.stNames.testSuite)).spawnTestShip(
                        (Vector2f)getState(RTS_ParseInput.stNames.worldSpace),
                        false
                );
                x.consume();
                return (null);
            }
        });
        param7.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }

            @Override
            public Boolean check(Object var) {
                if (!dev)
                    return (false);
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.param7)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.param7) + 32));
            }
        });
        return (param7);
    }

    /* PARAM8_DEV :: M */
    private RTS_StateEngine.conditionSet param8 () {
        RTS_StateEngine.conditionSet param8 = new RTS_StateEngine.conditionSet();
        param8.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((RTS_ShipTestSuite)getState(RTSAssist.stNames.testSuite)).setTestShip(
                        (List<ShipAPI>)getState(RTS_ParseInput.stNames.currentSelection)
                );
                x.consume();
                return (null);
            }
        });
        param8.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }

            @Override
            public Boolean check(Object var) {
                if (!dev)
                    return (false);
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.param8)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.param8) + 32));
            }
        });
        return (param8);
    }

    /* STRAFE LEFT */
    private RTS_StateEngine.conditionSet strafeLeft () {
        RTS_StateEngine.conditionSet strafeLeft = new RTS_StateEngine.conditionSet();
        strafeLeft.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((HashMap<String, Boolean>)getState(RTS_ParseInput.stNames.strafeKeys))
                        .put(RTS_ParseInput.skNames.left, !x.isKeyUpEvent());
                x.consume();
                return (null);
            }
        });
        strafeLeft.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventValue;
            }

            @Override
            public Boolean check(Object var) {
                return ((int)var == 30);
            }
        });
        return (strafeLeft);
    }

    /* STRAFE RIGHT */
    private RTS_StateEngine.conditionSet strafeRight () {
        RTS_StateEngine.conditionSet strafeRight = new RTS_StateEngine.conditionSet();
        strafeRight.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((HashMap<String, Boolean>)getState(RTS_ParseInput.stNames.strafeKeys))
                        .put(RTS_ParseInput.skNames.right, !x.isKeyUpEvent());
                x.consume();
                return (null);
            }
        });
        strafeRight.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventValue;
            }

            @Override
            public Boolean check(Object var) {
                return ((int)var == 32);
            }
        });
        return (strafeRight);
    }

    /* STRAFE UP */
    private RTS_StateEngine.conditionSet strafeUp () {
        RTS_StateEngine.conditionSet strafeUp = new RTS_StateEngine.conditionSet();
        strafeUp.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((HashMap<String, Boolean>)getState(RTS_ParseInput.stNames.strafeKeys))
                        .put(RTS_ParseInput.skNames.up, !x.isKeyUpEvent());
                x.consume();
                return (null);
            }
        });
        strafeUp.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventValue;
            }

            @Override
            public Boolean check(Object var) {
                return ((int)var == 17);
            }
        });
        return (strafeUp);
    }

    /* STRAFE DOWN */
    private RTS_StateEngine.conditionSet strafeDown () {
        RTS_StateEngine.conditionSet strafeDown = new RTS_StateEngine.conditionSet();
        strafeDown.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((HashMap<String, Boolean>)getState(RTS_ParseInput.stNames.strafeKeys))
                        .put(RTS_ParseInput.skNames.down, !x.isKeyUpEvent());
                x.consume();
                return (null);
            }
        });
        strafeDown.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventValue;
            }

            @Override
            public Boolean check(Object var) {
                return ((int)var == 31);
            }
        });
        return (strafeDown);
    }

    /* SAVE LAYOUT */
    private RTS_StateEngine.conditionSet saveLayout () {
        RTS_StateEngine.conditionSet saveLayout = new RTS_StateEngine.conditionSet();
        saveLayout.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((RTS_TaskManager)getState(RTSAssist.stNames.taskManager)).saveAssignments();
                x.consume();
                return (null);
            }
        });
        saveLayout.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }

            @Override
            public Boolean check(Object var) {
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.saveLayout)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.saveLayout) + 32));
            }
        });
        return (saveLayout);
    }

    /* LOAD LAYOUT */
    private RTS_StateEngine.conditionSet loadLayout () {
        RTS_StateEngine.conditionSet loadLayout = new RTS_StateEngine.conditionSet();
        loadLayout.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((RTS_TaskManager)getState(RTSAssist.stNames.taskManager))
                        .loadAssignments((Vector2f)getState(RTS_ParseInput.stNames.worldSpace));
                x.consume();
                return (null);
            }
        });
        loadLayout.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }

            @Override
            public Boolean check(Object var) {
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.loadLayout)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.loadLayout) + 32));
            }
        });
        return (loadLayout);
    }

    /* MOVE TOGETHER */
    private RTS_StateEngine.conditionSet moveTogether () {
        RTS_StateEngine.conditionSet moveTogether = new RTS_StateEngine.conditionSet();
        moveTogether.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((RTS_TaskManager)getState(RTSAssist.stNames.taskManager)).addNewTogaInstance(
                        (List<ShipAPI>)getState(RTS_ParseInput.stNames.currentSelection)
                );
                x.consume();
                return null;
            }
        });
        moveTogether.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }

            @Override
            public Boolean check(Object var) {
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.moveTogether)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.moveTogether) + 32));
            }
        });
        return (moveTogether);
    }

    /* ATTACK MOVE */
    private RTS_StateEngine.conditionSet attackMove () {
        RTS_StateEngine.conditionSet attackMove = new RTS_StateEngine.conditionSet();
        attackMove.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                if (getState(RTS_ParseInput.stNames.currentSelection) != null)
                    for(ShipAPI ship: (List<ShipAPI>)getState(RTS_ParseInput.stNames.currentSelection)) {
                        if (!((RTS_TaskManager)getState(RTSAssist.stNames.taskManager))
                                .convertAssignmentOrQueueToAttackMove(
                                        ship,
                                        (boolean)getState(RTS_ParseInput.stNames.isShiftDown)
                                )
                        ) {
                            if (!(boolean)getState(RTS_ParseInput.stNames.isShiftDown))
                                ((RTS_TaskManager)getState(RTSAssist.stNames.taskManager)).createAssignments(
                                        null,
                                        Arrays.asList(ship),
                                        (Vector2f)getState(RTS_ParseInput.stNames.worldSpace),
                                        null,
                                        true,
                                        true,
                                        true,
                                        false
                                );
                            else
                                ((RTS_TaskManager)getState(RTSAssist.stNames.taskManager)).queueAssignments(
                                        Arrays.asList(ship),
                                        (Vector2f)getState(RTS_ParseInput.stNames.worldSpace),
                                        null,
                                        true,
                                        true
                                );
                        }
                    }
                x.consume();
                return null;
            }
        });
        attackMove.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }

            @Override
            public Boolean check(Object var) {
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.attackMove)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.attackMove) + 32));
            }
        });
        return (attackMove);
    }

    /* DELETE ASSIGNMENTS */
    private RTS_StateEngine.conditionSet deleteAssignments () {
        RTS_StateEngine.conditionSet deleteAssignments = new RTS_StateEngine.conditionSet();
        deleteAssignments.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                if (!(boolean)getState(RTS_ParseInput.stNames.isShiftDown))
                    ((RTS_TaskManager)getState(RTSAssist.stNames.taskManager)).deleteAssignments(
                            (List<ShipAPI>)getState(RTS_ParseInput.stNames.currentSelection),
                            true
                    );
                else
                    ((RTS_TaskManager)getState(RTSAssist.stNames.taskManager)).popAssignmentQueue(
                            (List<ShipAPI>)getState(RTS_ParseInput.stNames.currentSelection)
                    );
                x.consume();
                return null;
            }
        });
        deleteAssignments.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }

            @Override
            public Boolean check(Object var) {
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.deleteAssignments)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.deleteAssignments) + 32));
            }
        });
        return (deleteAssignments);
    }

    /* VENT SELECTED SHIPS */
    private RTS_StateEngine.conditionSet vent () {
        RTS_StateEngine.conditionSet vent = new RTS_StateEngine.conditionSet();
        vent.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                if (getState(RTS_ParseInput.stNames.currentSelection) != null)
                    for(ShipAPI ship: (List<ShipAPI>)getState(RTS_ParseInput.stNames.currentSelection))
                        ((RTS_VentManager)getState(RTS_TaskManager.stNames.ventManager)).ventShip(ship);
                x.consume();
                return (null);
            }
        });
        vent.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }
            @Override
            public Boolean check(Object var) {
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.vent)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.vent) + 32));
            }
        });
        return (vent);
    }

    /* USE SYSTEM */
    private RTS_StateEngine.conditionSet useSystem () {
        RTS_StateEngine.conditionSet useSystem = new RTS_StateEngine.conditionSet();
        useSystem.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                if (getState(RTS_ParseInput.stNames.currentSelection) != null)
                    for(ShipAPI ship: (List<ShipAPI>)getState(RTS_ParseInput.stNames.currentSelection))
                        if (ship.getSystem().canBeActivated())
                            ((RTS_SystemManager)getState(RTS_TaskManager.stNames.systemManager))
                                    .queueSystemActivation(ship);
                x.consume();
                return null;
            }
        });
        useSystem.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventChar;
            }

            @Override
            public Boolean check(Object var) {
                return ((char)var == (char)hotKeys.get(RTSAssist.hoNames.useSystem)
                        || (char)var == (char)((char)hotKeys.get(RTSAssist.hoNames.useSystem) + 32));
            }
        });
        return (useSystem);
    }

    /* ???? */
    private RTS_StateEngine.conditionSet eventVal45 () {
        RTS_StateEngine.conditionSet temp = new RTS_StateEngine.conditionSet();
        temp.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                ((RTS_TaskManager)getState(RTSAssist.stNames.taskManager)).editPriorityEnemy(
                        (List<ShipAPI>)getState(RTS_ParseInput.stNames.currentSelection),
                        (Vector2f)getState(RTS_ParseInput.stNames.worldSpace),
                        false
                );
                x.consume();
                return null;
            }
        });
        temp.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventValue;
            }

            @Override
            public Boolean check(Object var) {
                return ((int)var == 45);
            }
        });
        return (temp);
    }

    /* LEFT CONTROL */
    private RTS_StateEngine.conditionSet leftControl () {
        RTS_StateEngine.conditionSet leftControl = new RTS_StateEngine.conditionSet();
        leftControl.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                if ((boolean)getState(RTS_ParseInput.stNames.isAltDown)) {
                    if (getState(RTS_ParseInput.stNames.leftClickStoreSec) == null) {
                        setState(
                                RTS_ParseInput.stNames.leftClickStoreSec,
                                getState(RTS_ParseInput.stNames.worldSpace)
                        );
                        setState(RTS_ParseInput.stNames.isCtrlDown, true);
                    }
                }
                x.consume();
                return null;
            }
        });
        leftControl.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventValue;
            }

            @Override
            public Boolean check(Object var) {
                return ((int)var == 29);
            }
        });
        return (leftControl);
    }

    /* CONTROL GROUPS */
    private RTS_StateEngine.conditionSet controlGroups () {
        RTS_StateEngine.conditionSet controlGroups = new RTS_StateEngine.conditionSet();
        controlGroups.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                if ((boolean)getState(RTS_ParseInput.stNames.isCtrlDown))
                    ((RTS_SelectionListener)getState(RTSAssist.stNames.selectionListener))
                            .controlGroupHandler((Integer)x.getEventValue(), true);
                else {
                    ((RTS_SelectionListener)getState(RTSAssist.stNames.selectionListener))
                            .controlGroupHandler((Integer)x.getEventValue(), false);
                    if (getState(RTS_ParseInput.stNames.controlGroupStore) != null) {
                        if (((Vector2f)getState(RTS_ParseInput.stNames.controlGroupStore)).getX() == (float)x.getEventValue()
                                && ((float)getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.start))
                                - ((Vector2f)getState(RTS_ParseInput.stNames.controlGroupStore)).getY() < 0.2f))
                            ((RTS_CameraRework)getState(RTSAssist.stNames.cameraRework))
                                    .zoomToControlGroup((Integer)x.getEventValue());
                    }
                    if (getState(RTS_ParseInput.stNames.controlGroupStore) == null)
                        setState(RTS_ParseInput.stNames.controlGroupStore, new Vector2f());
                    ((Vector2f)getState(RTS_ParseInput.stNames.controlGroupStore)).set(
                            x.getEventValue(),
                            (float)getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.start))
                    );
                }
                x.consume();
                return null;
            }
        });
        controlGroups.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return RTS_ParseInputKeyBinds.kiNames.eventValue;
            }

            @Override
            public Boolean check(Object var) {
                return ((int)var >= 2 && (int)var <= 6 && x.isKeyDownEvent());
            }
        });
        return (controlGroups);
    }

}
