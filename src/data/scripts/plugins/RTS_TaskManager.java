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

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import data.scripts.RTSAssistModPlugin;
import data.scripts.modInitilisation.RTS_CommonsControl;
import data.scripts.plugins.AISystems.RTS_AIInjector;
import data.scripts.plugins.Setup.RTS_TaskManagerAssSetup;
import data.scripts.plugins.Utils.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.CollisionUtils;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.io.IOException;
import java.util.*;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.FastTrig;

import data.scripts.plugins.BlockedSystems.*;

import static data.scripts.RTSAssistModPlugin.RTS_Global;

public class RTS_TaskManager extends RTS_StatefulClasses {

    public static class classidentifiers {
        public String assignments = RTS_StatefulClasses.getUniqueIdentifier();
        public String assignmentQueue = RTS_StatefulClasses.getUniqueIdentifier();
        public String translateStore = RTS_StatefulClasses.getUniqueIdentifier();
        public String totalTranslation = RTS_StatefulClasses.getUniqueIdentifier();
        public String rotationStore = RTS_StatefulClasses.getUniqueIdentifier();
        public String totalRotation = RTS_StatefulClasses.getUniqueIdentifier();
        public String totalRotationHold = RTS_StatefulClasses.getUniqueIdentifier();
        public String togaStore = RTS_StatefulClasses.getUniqueIdentifier();
        public String firstFrameInit = RTS_StatefulClasses.getUniqueIdentifier();
        public String deployment = RTS_StatefulClasses.getUniqueIdentifier();
        public String combatStarted = RTS_StatefulClasses.getUniqueIdentifier();
        public String dRestrict = RTS_StatefulClasses.getUniqueIdentifier();
        public String ShipLocAPI = RTS_StatefulClasses.getUniqueIdentifier();
        public String RTSCollisionManager = RTS_StatefulClasses.getUniqueIdentifier();
        public String commandShuttle = RTS_StatefulClasses.getUniqueIdentifier();
        public String ventManager = RTS_StatefulClasses.getUniqueIdentifier();
        public String systemManager = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public static classidentifiers stNames = new RTS_TaskManager.classidentifiers();

    public RTS_TaskManager (Object state) {
        super(state);
        HashMap<String, Object> init = new HashMap<String, Object>() {{
            put(RTS_TaskManager.stNames.assignments, new HashMap<String, Object>()); // stores assignments
            put(RTS_TaskManager.stNames.assignmentQueue, new HashMap<>());
            put(RTS_TaskManager.stNames.translateStore, new HashMap<>());
            put(RTS_TaskManager.stNames.totalTranslation, null);
            put(RTS_TaskManager.stNames.rotationStore, new HashMap<>());
            put(RTS_TaskManager.stNames.totalRotation, null);
            put(RTS_TaskManager.stNames.totalRotationHold, null);
            put(RTS_TaskManager.stNames.togaStore, new HashMap<>());
            put(RTS_TaskManager.stNames.firstFrameInit, false); // flag for firstFrameInit
            put(RTS_TaskManager.stNames.deployment, null); // initialised in handleDeployment
            put(RTS_TaskManager.stNames.combatStarted, false); // flag for handleDepolyment
            put(RTS_TaskManager.stNames.dRestrict, -100000f); // Restricts where the player can place ships
            put(RTS_TaskManager.stNames.ShipLocAPI, null); // initilised in firstFrameInit
            put(RTS_TaskManager.stNames.RTSCollisionManager, null); // down (requires state)
            put(RTS_TaskManager.stNames.commandShuttle, null); // initilised in firstFrameInit
        }};

        init.put(RTS_TaskManager.stNames.RTSCollisionManager, new RTS_CollisionManager(this.returnState()));
        init.put(RTS_TaskManager.stNames.systemManager, new RTS_SystemManager(this.returnState()));
        init.put(RTS_TaskManager.stNames.ventManager, new RTS_VentManager(this.returnState()));
        this.setState(init);
        init.clear();
        RTS_TaskManagerAssSetup assSetup = new RTS_TaskManagerAssSetup();
        assSetup.buildAssEngine(this.assEngine);
    }

    RTS_StateEngine assEngine = new RTS_StateEngine();

    public enum assType {
        moveAndRelease,
        moveAndHold,
        dynamicHoldOnFlux,
        basicAttack,
        attackAtAngle,
        prioritiseEnemy,
        attackMove,
    }

    public void buildListeners ()  {
        RTS_Listener deadShipListener = new RTS_Listener() {
            @Override
            public String type() {
                return (RTS_ShipLocAPI.evNames.shipsNoLongerActive);
            }

            @Override
            public void run(HashMap<String, Object> e) {
                if (getState(RTS_TaskManager.stNames.assignments) != null)
                    deleteAssignments((List<ShipAPI>)e.get(RTS_ShipLocAPI.evNames.shipsNoLongerActive), true, false);
            }

            @Override
            public boolean removeOnCompletion() {
                return (false);
            }
        };
        ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager)).addListener(deadShipListener);
    }

    public void createAssignments (
            HashMap<String, Object> queueInfo,
            List<ShipAPI> selection,
            Vector2f coOrd,
            Vector2f coOrdSec,
            Boolean moveAndRelease,
            boolean clearQueue,
            boolean attackMove,
            boolean wasTranslated,
            boolean notify
    ) {
        if (selection == null) return;

        /* Get some important info */
        HashMap<String, Object> assignments = (HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments);
        HashMap<String, RTS_BlockSystemPlugin> blockedSystems =
                (HashMap<String, RTS_BlockSystemPlugin>)this.getState(RTSAssist.stNames.blockedSystems);
        ShipAPI primaryEnemy = null;
        ShipAPI secondaryEnemy = null;
        if (!wasTranslated) {
            primaryEnemy = queueInfo == null
                    ? this.getEnemies("primary", coOrd, coOrdSec)
                    : queueInfo.get("primaryEnemy") != null
                    ? (ShipAPI)queueInfo.get("primaryEnemy")
                    : null;
            secondaryEnemy = queueInfo == null
                    ? this.getEnemies("secondary", coOrd, coOrdSec)
                    : queueInfo.get("secondaryEnemy") != null && !((ShipAPI)queueInfo.get("secondaryEnemy")).isHulk()
                    ? (ShipAPI)queueInfo.get("secondaryEnemy")
                    : null;
        }
        assType assignmentType;
        if (queueInfo == null) {
            if (attackMove)
                assignmentType = assType.attackMove;
            else {
                HashMap<String, Object> checkList = new HashMap<>();
                checkList.put("selection", selection);
                checkList.put("coOrd", coOrd);
                checkList.put("coOrdSec", coOrdSec);
                checkList.put("moveAndRelease", (boolean)this.getDeepState(Arrays.asList(
                                RTSAssist.stNames.config,
                                RTSAssist.coNames.switchRightClick)
                        )!= moveAndRelease);
                checkList.put("primaryEnemy", primaryEnemy);
                checkList.put("secondaryEnemy", secondaryEnemy);
                assignmentType = (assType)this.assEngine.doCheck(checkList);
                checkList.clear();
            }
        }
        else
            assignmentType = (assType)queueInfo.get("assType");

        /* if we are in deployment restrict where the player can place ships and if they can target enemys */
        if (!(boolean)this.getState(RTS_TaskManager.stNames.combatStarted)
                && (primaryEnemy != null || secondaryEnemy != null))
            return;
        coOrd.setY(Math.min(coOrd.getY(), (float)this.getState(RTS_TaskManager.stNames.dRestrict)));
        if (coOrdSec != null)
            coOrdSec.setY(Math.min(coOrdSec.getY(), (float)this.getState(RTS_TaskManager.stNames.dRestrict)));

        /* main assignment loop */
        for (ShipAPI x: selection) {
            if (clearQueue && (((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue))
                    .get(x.getId()) != null
                    && !((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue))
                    .get(x.getId()).isEmpty())) {
                for (HashMap<String, Object> queue :
                        ((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue)).get(x.getId()))
                    queue.clear();
                ((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue)).get(x.getId()).clear();
            }
            HashMap<String, Object> shipHold = new HashMap<>();
            shipHold.put("priorityEnemy", // If we assign this ship a new assignment, we want to see if it had a priority enemy and keep that
                    secondaryEnemy != null && shipHold.get("targetEnemy") == null
                            ? secondaryEnemy
                            : assignments != null
                            && assignments.get(x.getId()) != null
                            && ((HashMap<String, Object>)assignments.get(x.getId())).get("priorityEnemy") != null
                            ? ((HashMap<String, Object>)assignments.get(x.getId())).get("priorityEnemy")
                            : null
            );
            if (assignments.containsKey(x.getId()) && assignments.get(x.getId()) != null)
                this.deleteAssignments(Arrays.asList(x), true, false);
            shipHold.put("UUID", UUID.randomUUID().toString());
            shipHold.put("assType", assignmentType);
            shipHold.put("shipTask", new RTS_ShipMoveToPosition(x, this.returnState()));
            shipHold.put("dest", coOrd);
            shipHold.put("modDest", null);
            shipHold.put("destSec",
                    secondaryEnemy != null
                    ? null
                    : coOrdSec
            );
            shipHold.put("assLocation", new Vector2f(x.getLocation()));
            shipHold.put("iniDisToAssignment", RTS_AssortedFunctions.fastGetDistance(x.getLocation(), coOrd));
            shipHold.put("ship", x);
            shipHold.put("blockedSystem",
                    x.getSystem() == null || !blockedSystems.containsKey(x.getSystem().getId())
                            ? null
                            : blockedSystems.get(x.getSystem().getId()) == null
                            ? new RTS_Block_alwaystrue_()
                            : blockedSystems.get(x.getSystem().getId())
            );
            shipHold.put(
                    "moveAndRelease",
                            assignmentType == assType.attackMove
                                    ? true
                                    : primaryEnemy != null || secondaryEnemy != null || assignmentType == assType.dynamicHoldOnFlux
                                            ? moveAndRelease
                                            : assignmentType == assType.moveAndRelease
            );
            shipHold.put("weaponPosMods", getWeaponPosMods(x));
            shipHold.put("attackAngle", assignmentType == assType.attackAtAngle
                    ? new Vector2f(
                            VectorUtils.getAngle(coOrd, coOrdSec),
                            MathUtils.getDistance(coOrd, coOrdSec)
                    )
                    : null
            );
            shipHold.put("angledAttack", null);
            shipHold.put("targetEnemy", primaryEnemy);
            shipHold.put("closestEnemy", null);
            assignments.put(x.getId(), shipHold);
            this.setState(RTS_TaskManager.stNames.assignments, assignments);
        }
        String sound = switch (assignmentType) {
            case moveAndRelease -> "CreateTemporaryAssignment";
            case moveAndHold -> "CreateStandardAssignment";
            case basicAttack ->  moveAndRelease ? "TargetEnemy" : "TargetEnemyLong";
            case dynamicHoldOnFlux -> "CreateSecondaryAssignment";
            case attackAtAngle -> "AttackAtAngle";
            case prioritiseEnemy -> null;
            case attackMove -> primaryEnemy != null ? "AttackMove" : "AttackMoveShip";
        };
        float volume = switch (assignmentType) {
            case moveAndRelease -> 0.8f;
            case moveAndHold -> 0.6f;
            case basicAttack -> 1f;
            case dynamicHoldOnFlux -> 1f;
            case attackAtAngle -> 1f;
            case prioritiseEnemy -> 0.9f;
            case attackMove -> 0.7f;
        };

        float pitch = switch (assignmentType) {
            case moveAndRelease -> 2f;
            case moveAndHold -> 1.5f;
            case basicAttack -> 1f;
            case dynamicHoldOnFlux -> 1f;
            case attackAtAngle -> 1f;
            case prioritiseEnemy -> 1f;
            case attackMove -> 1f;
        };

        if (sound != null && notify)
            ((RTS_SoundsManager)this.getState(RTSAssist.stNames.soundsManager)).playBasicUISound(
                    sound,
                    pitch,
                    volume * 0.3f
            );
    }

    // Dont queue duplicates(they show when drawing).
    public void queueAssignments (List<ShipAPI> selection, Vector2f coOrd, Vector2f coOrdSec, boolean moveAndRelease, boolean attackMove) {
        if (!(boolean)this.getState(RTS_TaskManager.stNames.combatStarted))
            return;
        ((RTS_SoundsManager)this.getState(RTSAssist.stNames.soundsManager)).playBasicUISound(
                "QueueAssignment",
                1.5f,
                0.5f
        );
        ShipAPI primaryEnemy = this.getEnemies("primary", coOrd, coOrdSec);
        ShipAPI secondaryEnemy = this.getEnemies("secondary", coOrd, coOrdSec);
        assType assignmentType;
        if (attackMove)
            assignmentType = assType.attackMove;
        else {
            HashMap<String, Object> checkList = new HashMap<>();
            checkList.put("selection", selection);
            checkList.put("coOrd", coOrd);
            checkList.put("coOrdSec", coOrdSec);
            checkList.put("moveAndRelease", moveAndRelease
                    && !(boolean)this.getDeepState(Arrays.asList(RTSAssist.stNames.config, RTSAssist.coNames.switchRightClick)));
            checkList.put("primaryEnemy", primaryEnemy);
            checkList.put("secondaryEnemy", secondaryEnemy);
            assignmentType = (assType)this.assEngine.doCheck(checkList);
            checkList.clear();
        }

        HashMap<String, List<HashMap<String, Object>>> assignmentQueue =
                (HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue);
        List<HashMap<String, Object>> queuePointer;
        HashMap<String, Object> assignmentPointer;
        assType prevAssPointer;
        for (ShipAPI ship : selection) {
            if (assignmentQueue.get(ship.getId()) == null)
                assignmentQueue.put(ship.getId(), new ArrayList<>());
            queuePointer = assignmentQueue.get(ship.getId());
            assignmentPointer = (HashMap<String, Object>)((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments)).get(ship.getId());
            prevAssPointer = queuePointer != null && !queuePointer.isEmpty()
                    ? (assType)queuePointer.get(queuePointer.size() - 1).get("assType")
                    : assignmentPointer != null // check this
                    ? (assType)assignmentPointer.get("assType")
                    : null;
            if (
                    prevAssPointer == assType.moveAndHold
                    || prevAssPointer == assType.dynamicHoldOnFlux
                    || prevAssPointer == assType.prioritiseEnemy
            )
                continue;
            if (queuePointer.isEmpty() && assignmentPointer != null) {
                if ((assType)assignmentPointer.get("assType") == assType.basicAttack) {
                    if (assignmentType == assType.attackAtAngle || assignmentType == assType.basicAttack)
                        if (((ShipAPI)assignmentPointer.get("targetEnemy")).getId().equals(primaryEnemy.getId())) {
                            createAssignments(
                                    null,
                                    selection,
                                    coOrd,
                                    coOrdSec,
                                    moveAndRelease,
                                    true,
                                    false,
                                    false,
                                    true
                            );
                            return;
                        }
                }
            }
            HashMap<String, Object> newQueuedAssignment = new HashMap<>();
            newQueuedAssignment.put("selection", Arrays.asList(ship));
            newQueuedAssignment.put("coOrd", coOrd);
            newQueuedAssignment.put("coOrdSec", coOrdSec);
            newQueuedAssignment.put("moveAndRelease", moveAndRelease);
            newQueuedAssignment.put("assType", assignmentType);
            newQueuedAssignment.put("primaryEnemy", primaryEnemy);
            newQueuedAssignment.put("secondaryEnemy", secondaryEnemy);
            newQueuedAssignment.put("ship", ship);
            if (!queuePointer.isEmpty()) {
                if ((assType)(queuePointer.get(queuePointer.size() - 1)).get("assType")
                        == assType.basicAttack) {
                    if (assignmentType == assType.attackAtAngle || assignmentType == assType.basicAttack) {
                        if (((ShipAPI)(queuePointer.get(queuePointer.size() - 1)).get("primaryEnemy"))
                                .getId().equals(primaryEnemy.getId()))
                            queuePointer.remove(queuePointer.size() - 1);
                    }
                }
            }
            queuePointer.add(queuePointer.size(), newQueuedAssignment);
        }
    }

    /* Assignment creation utils */

    public ShipAPI getEnemies (String type, Vector2f coOrd, Vector2f coOrdSec) {
        float missedClickUIHelper =  25f * (((float)this.getDeepState(Arrays.asList(RTSAssist.stNames.config,RTSAssist.coNames.selectionTolerance)) - 1f) * 2f)
                * ((float)this.getState(RTS_CameraRework.stNames.targetZoom) * 0.3f);
        if (type.equals("primary")) {
            List<ShipAPI> targetedEnemyHoldPrimary = (boolean)this.getState(RTS_TaskManager.stNames.combatStarted)
                    ? ((RTS_ShipLocAPI)this.getState(RTS_TaskManager.stNames.ShipLocAPI)).findEnemyShipNeighbours(
                            coOrd,
                            MathUtils.clamp(missedClickUIHelper, 500f, 20000),
                            true
                    )
                    : new ArrayList<>();
            ShipAPI primaryEnemy = null;
            for (ShipAPI ship : targetedEnemyHoldPrimary) {
                ship.getExactBounds().update(ship.getLocation(), ship.getFacing());
                if (CollisionUtils.isPointWithinBounds(coOrd, (CombatEntityAPI)ship)) {
                    primaryEnemy = ship;
                    break;
                }
            }
            if (primaryEnemy == null && !targetedEnemyHoldPrimary.isEmpty()) {
                float shortestD = 400000000;
                float dPointer;
                for (ShipAPI ship : targetedEnemyHoldPrimary) {
                    dPointer = ((Double)RTS_AssortedFunctions.getDistanceSquared(
                            coOrd,
                            CollisionUtils.getNearestPointOnBounds(coOrd, ship))
                    ).floatValue();
                    if (dPointer < Math.pow(missedClickUIHelper, 2)) {
                        if (dPointer < shortestD) {
                            shortestD = dPointer;
                            primaryEnemy = ship;
                        }
                    }
                }
            }
            return (primaryEnemy);
        }
        else {
            List<ShipAPI> targetedEnemyHoldSecondary = (boolean)this.getState(RTS_TaskManager.stNames.combatStarted) && coOrdSec != null
                    ? ((RTS_ShipLocAPI)this.getState(RTS_TaskManager.stNames.ShipLocAPI)).findEnemyShipNeighbours(
                            coOrdSec,
                            MathUtils.clamp(missedClickUIHelper, 500f, 20000),
                            true
                    )
                    : new ArrayList<>();
            ShipAPI secondaryEnemy = null;
            for (ShipAPI ship : targetedEnemyHoldSecondary) {
                ship.getExactBounds().update(ship.getLocation(), ship.getFacing());
                if (CollisionUtils.isPointWithinBounds(coOrdSec, (CombatEntityAPI)ship)) {
                    secondaryEnemy = ship;
                    break;
                }
            }
            if (secondaryEnemy == null && !targetedEnemyHoldSecondary.isEmpty()) {
                float shortestD = 400000000;
                float dPointer;
                for (ShipAPI ship : targetedEnemyHoldSecondary) {
                    dPointer = ((Double)RTS_AssortedFunctions.getDistanceSquared(
                            coOrdSec,
                            CollisionUtils.getNearestPointOnBounds(coOrd, ship))
                    ).floatValue();
                    if (dPointer < Math.pow(missedClickUIHelper, 2)) {
                        if (dPointer < shortestD) {
                            shortestD = dPointer;
                            secondaryEnemy = ship;
                        }
                    }
                }
            }
            return (secondaryEnemy);
        }
    }

    public void editPriorityEnemy (List<ShipAPI> selection, Vector2f coOrd, boolean addRemove) {
        if (!(boolean)this.getState(RTS_TaskManager.stNames.combatStarted))
            return;
        List<ShipAPI> targetedEnemyHold = CombatUtils.getShipsWithinRange(coOrd, 5f);
        if (selection == null || selection.size() == 0 || (addRemove && (targetedEnemyHold.size() != 1
                || targetedEnemyHold.get(0).getOriginalOwner() != 1)))
            return;
        HashMap<String, Object> pointer;
        for (ShipAPI ship : selection) {
            if (((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments)).containsKey(ship.getId())) {
                pointer = (HashMap<String, Object>)((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments))
                        .get(ship.getId());
                if (pointer != null && pointer.get("targetEnemy") == null)
                    pointer.put("priorityEnemy", addRemove ? targetedEnemyHold.get(0) : null);
            }
        }
    }

    private Vector2f getDestFromRatio (Vector2f coOrd, Vector2f coOrdSec, float ratio) {
        return(new Vector2f(
                coOrd.getX() + ((coOrdSec.getX() - coOrd.getX()) * ratio),
                coOrd.getY() + ((coOrdSec.getY() - coOrd.getY()) * ratio)
        ));
    }

    private HashMap<String, Object> getWeaponPosMods (ShipAPI ship) {
        HashMap<String, Object> hold = new HashMap<>();
        HashMap<String, Object> modifiers = (HashMap<String, Object>)this.getState(RTSAssist.stNames.broadsideData);
        float posModFactor;
        if (modifiers.containsKey(ship.getHullSpec().getBaseHullId())) {
            float broadsideMod = Math.abs(((Vector2f)modifiers.get(ship.getHullSpec().getBaseHullId())).getX());
            posModFactor = (float)FastTrig.cos(broadsideMod / 57f);
        }
        else
            posModFactor = 1f;
        for (WeaponAPI set : ship.getAllWeapons()) {
            hold.put(
                    set.getId(),
                    (ship.getLocation().getY() - (VectorUtils.rotateAroundPivot(
                            set.getLocation(),
                            ship.getLocation(),
                            (ship.getFacing() * -1) + 90f
                    ).getY())) * posModFactor
            );
        }
        return (hold);
    }

    /* Managing assignments */

    public void drawAndAssignAssignmentFromQueue (String ship) {
        List<HashMap<String, Object>> queuePointer;
        if (
                ((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue))
                        .get(ship) != null
                && !((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue))
                        .get(ship).isEmpty()
        ) {
            queuePointer = ((HashMap<String, List<HashMap<String, Object>>>)this
                    .getState(RTS_TaskManager.stNames.assignmentQueue)).get(ship);
            this.drawAssignmentQueue(queuePointer);
        }
        else
            return;
        if (((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments)).get(ship) != null)
            return;
        HashMap<String, Object> assPointer = queuePointer.get(0);
        if (assPointer.get("primaryEnemy") != null && ((ShipAPI)assPointer.get("primaryEnemy")).isHulk()) {
            queuePointer.remove(0);
            return;
        }
        this.createAssignments(
                assPointer,
                (List<ShipAPI>)assPointer.get("selection"),
                (Vector2f)assPointer.get("coOrd"),
                assPointer.get("coOrdSec") != null ? (Vector2f)assPointer.get("coOrdSec") : null,
                assPointer.get("moveAndRelease") != null ? (boolean)assPointer.get("moveAndRelease") : null,
                false,
                false,
                false,
                false
        );
        queuePointer.remove(0);
    }

    public void popAssignmentQueue (List<ShipAPI> selection) {
        if (selection == null) return;
       List<HashMap<String, Object>> queuePointer;
        for (ShipAPI x: selection) {
            queuePointer = ((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue))
                    .get(x.getId());
            if (queuePointer != null) {
                if (!queuePointer.isEmpty()) {
                    queuePointer.get(queuePointer.size() - 1).clear();
                    queuePointer.remove(queuePointer.size() - 1);
                }
                else
                    this.deleteAssignments(Arrays.asList(x), false, true);
            }
        }
    }

    private boolean manageDeletions (HashMap<String, Object> assignment) {
        if ((((Boolean)assignment.get("moveAndRelease")
                && (ShipAPI)assignment.get("targetEnemy") == null)
                && MathUtils.getDistanceSquared(
                        (ShipAPI)assignment.get("ship"),
                        (Vector2f)assignment.get("dest")
                ) == 0f)
                || ((ShipAPI)assignment.get("ship")).isHulk()
                || (assignment.get("targetEnemy") != null && ((ShipAPI)assignment.get("targetEnemy")).isHulk())
                || ((boolean)((HashMap<String, Object>)this.getState(RTSAssist.stNames.config)).get(RTSAssist.coNames.shipsWillRun)
                && ((ShipAPI)assignment.get("ship")).getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.IN_CRITICAL_DPS_DANGER))
        ) {
            deleteAssignments(Arrays.asList((ShipAPI)assignment.get("ship")), false, true);
            return (true);
        }
        else
            return (false);
    }

    public void deleteAssignments (List<ShipAPI> selection, boolean clearQueue, boolean notify) {
        if (selection == null) return;
        boolean flag = false;
        HashMap<String, Object> assignments = (HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments);
        HashMap<String, Object> pointer;
        for (ShipAPI x: selection) {
            if (assignments.get(x.getId()) != null) {
                pointer = ((HashMap<String, Object>)assignments.get(x.getId()));
                ((RTS_ShipMoveToPosition)pointer.get("shipTask")).cleanup(pointer);
                if (pointer.get("weaponPosMods") != null)
                    ((HashMap<String, Object>)pointer.get("weaponPosMods")).clear();
                pointer.clear();
                assignments.put(x.getId(), null);
                flag = true;
            }
            if (
                    clearQueue
                    && (((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue))
                            .get(x.getId()) != null
                    && !((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue))
                            .get(x.getId()).isEmpty())
            ) {
                for (HashMap<String, Object> queue : ((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue)).get(x.getId()))
                    queue.clear();
                ((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue)).get(x.getId()).clear();
            }
        }
        if (flag && notify)
            ((RTS_SoundsManager)this.getState(RTSAssist.stNames.soundsManager)).playBasicUISound(
                "DeleteAssignment",
                1f,
                0.3f
        );
        this.setState(RTS_TaskManager.stNames.assignments, assignments);
    }

    public void translateAssignments (List<ShipAPI> selection, Vector2f grabPoint, Vector2f placePoint, boolean mockUp, boolean queue) {
        if (selection == null || grabPoint == null || placePoint == null) return;
        if (!mockUp) {
            this.setAssignmentsMockUps(queue);
            return;
        }
        Vector2f translate = new Vector2f(
                placePoint.getX() - grabPoint.getX(),
                placePoint.getY() - grabPoint.getY()
        );
        HashMap<String, Object> assPointer;
        for (ShipAPI ship : selection) {
            assPointer = (HashMap<String, Object>)this.getDeepState(Arrays.asList(RTS_TaskManager.stNames.assignments, ship.getId()));
            if (assPointer == null || assPointer.get("targetEnemy") != null)
                continue;
            ((HashMap<String, Vector2f>)this.getState(RTS_TaskManager.stNames.translateStore)).put(ship.getId(), new Vector2f(
                    translate.getX(),
                    translate.getY()
            ));
        }
        this.setState(RTS_TaskManager.stNames.totalTranslation, translate);
    }

    public void rotateAssignments (List<ShipAPI> selection, Vector2f grabPoint, Vector2f placePoint, boolean mockUp, boolean queue) {
        if (selection == null || grabPoint == null || placePoint == null) return;
        if (!mockUp) {
            this.setAssignmentsMockUps(queue);
            return;
        }
        HashMap<String, Float> rotationStore = (HashMap<String, Float>)this.getState(RTS_TaskManager.stNames.rotationStore);
        Vector2f totalTranslation = (Vector2f)this.getState(RTS_TaskManager.stNames.totalTranslation);
        float totalRotationHold = this.getState(RTS_TaskManager.stNames.totalRotationHold) == null
                ? 0f
                : (float)this.getState(RTS_TaskManager.stNames.totalRotationHold);

        HashMap<String, Object> assPointer;
        Vector3f totals = new Vector3f(0, 0, 0);
        Vector2f pivotPoint = new Vector2f();
        float angle;
        for (ShipAPI ship : selection) {
            assPointer = (HashMap<String, Object>)this.getDeepState(Arrays.asList(RTS_TaskManager.stNames.assignments, ship.getId()));
            if (assPointer == null || assPointer.get("targetEnemy") != null)
                continue;
            totals.set(
                    ((Vector2f)assPointer.get("dest")).getX() + totals.getX(),
                    ((Vector2f)assPointer.get("dest")).getY() + totals.getY(),
                    totals.getZ() + 1
            );
        }
        if (totalTranslation != null)
            pivotPoint.set(
                    (totals.getX() / totals.getZ()) + totalTranslation.getX(),
                    (totals.getY() / totals.getZ()) + totalTranslation.getY()
            );
        else
            pivotPoint.set(
                    totals.getX() / totals.getZ(),
                    totals.getY() / totals.getZ()
            );
        angle = MathUtils.getShortestRotation(
                VectorUtils.getAngle(pivotPoint, grabPoint),
                VectorUtils.getAngle(pivotPoint, placePoint)
        );
        this.setState(RTS_TaskManager.stNames.totalRotation, angle);
        if ((boolean)this.getDeepState(Arrays.asList(RTSAssist.stNames.config, RTSAssist.coNames.alternativeRotation))) {
            angle = MathUtils.clamp((angle / 3f), -5f, 5f);
            for (ShipAPI ship : selection)
                rotationStore.put(
                        ship.getId(),
                        rotationStore.get(ship.getId()) == null
                                ? angle
                                : rotationStore.get(ship.getId()) + angle
                );
        }
        else
            for (ShipAPI ship : selection)
                rotationStore.put(
                        ship.getId(),
                        angle + totalRotationHold
                );
    }

    /* WIP */
//    public void AllinOneRTM () {
//
//    }

    public void setAssignmentsMockUps (boolean queue) {
        if (((HashMap<String, Vector2f>)this.getState(RTS_TaskManager.stNames.translateStore)).isEmpty()
                && ((HashMap<String, Vector2f>)this.getState(RTS_TaskManager.stNames.rotationStore)).isEmpty())
            return;
        HashMap<String, Vector2f> translateStore = (HashMap<String, Vector2f>)this.getState(RTS_TaskManager.stNames.translateStore);
        HashMap<String, Float> rotationStore = (HashMap<String, Float>)this.getState(RTS_TaskManager.stNames.rotationStore);
        HashMap<String, Object> assPointer;
        HashMap<String, Vector2f> destHold = new HashMap<>();
        HashMap<String, Vector2f> destSecHold = new HashMap<>();
        Vector3f totals = new Vector3f(0, 0, 0);
        Vector2f pivotPoint = new Vector2f();

        if (!translateStore.isEmpty())
            for (Map.Entry<String, Vector2f> shipStore : translateStore.entrySet()) {
                assPointer = (HashMap<String, Object>)((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments))
                        .get(shipStore.getKey());
                if (assPointer == null)
                    continue;
                destHold.put(shipStore.getKey(), new Vector2f(
                        ((Vector2f)assPointer.get("dest")).getX() + shipStore.getValue().getX(),
                        ((Vector2f)assPointer.get("dest")).getY() + shipStore.getValue().getY()
                ));
                if (assPointer.get("destSec") != null)
                    destSecHold.put(shipStore.getKey(), new Vector2f(
                            ((Vector2f)assPointer.get("destSec")).getX() + shipStore.getValue().getX(),
                            ((Vector2f)assPointer.get("destSec")).getY() + shipStore.getValue().getY()
                    ));
            }
        else {
            for (Map.Entry<String, Float> shipStore : rotationStore.entrySet()) {
                assPointer = (HashMap<String, Object>)((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments))
                        .get(shipStore.getKey());
                if (assPointer == null)
                    continue;
                destHold.put(shipStore.getKey(), new Vector2f(
                        ((Vector2f)assPointer.get("dest")).getX(),
                        ((Vector2f)assPointer.get("dest")).getY()
                ));
                if (assPointer.get("destSec") != null)
                    destSecHold.put(shipStore.getKey(), new Vector2f(
                            ((Vector2f)assPointer.get("destSec")).getX(),
                            ((Vector2f)assPointer.get("destSec")).getY()
                    ));
            }
        }
        if (!rotationStore.isEmpty()) {
            for (Map.Entry<String, Vector2f> shipStore : destHold.entrySet()) {
                totals.set(
                        shipStore.getValue().getX() + totals.getX(),
                        shipStore.getValue().getY() + totals.getY(),
                        totals.getZ() + 1
                );
            }
            pivotPoint.set(
                    totals.getX() / totals.getZ(),
                    totals.getY() / totals.getZ()
            );
            for (Map.Entry<String, Vector2f> shipStore : destHold.entrySet()) {
                VectorUtils.rotateAroundPivot(
                        shipStore.getValue(),
                        pivotPoint,
                        rotationStore.get(shipStore.getKey())
                );
            }
            for (Map.Entry<String, Vector2f> shipStore : destSecHold.entrySet()) {
                VectorUtils.rotateAroundPivot(
                        shipStore.getValue(),
                        pivotPoint,
                        rotationStore.get(shipStore.getKey())
                );
            }
        }
        for (Map.Entry<String, Vector2f> shipStore : destHold.entrySet()) {
            assPointer = (HashMap<String, Object>)((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments))
                    .get(shipStore.getKey());
            if (!queue)
                this.createAssignments(
                        null,
                        Arrays.asList((ShipAPI)assPointer.get("ship")),
                        shipStore.getValue(),
                        destSecHold.get(shipStore.getKey()) == null ? null : destSecHold.get(shipStore.getKey()),
                        (boolean)assPointer.get("moveAndRelease"),
                        true,
                        (assType)assPointer.get("assType") == assType.attackMove,
                        true,
                        false
                );
            else
                this.queueAssignments(
                        Arrays.asList((ShipAPI)assPointer.get("ship")),
                        shipStore.getValue(),
                        destSecHold.get(shipStore.getKey()) == null ? null : destSecHold.get(shipStore.getKey()),
                        (boolean)assPointer.get("moveAndRelease"),
                        (assType)assPointer.get("assType") == assType.attackMove
                );
        }
        ((RTS_SoundsManager)this.getState(RTSAssist.stNames.soundsManager)).playBasicUISound(
                "RotateAssignments",
                1.4f,
                0.3f
        );
        translateStore.clear();
        rotationStore.clear();
        destHold.clear();
        destSecHold.clear();
    }

    public void saveAssignments () {
        if  (Global.getSector().getPlayerFleet() == null)
            return;
        /* Save move and hold/release assignments */
        HashMap<String, Object> mem = new HashMap<>();
        HashMap<String, Object> hold;
        List<Vector2f> vecColl = new ArrayList<>();
        for (Map.Entry<String, Object> x: ((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments)).entrySet()) {
            if (x.getValue() == null) continue;
            vecColl.add((Vector2f)((HashMap<String, Object>)x.getValue()).get("dest"));
        }
        Vector2f mid = RTS_AssortedFunctions.getCollectiveCentre(vecColl);
        List<HashMap<String, Object>> positions = new ArrayList<>();
        HashMap<String, Object> hold2;
        for (Map.Entry<String, Object> x: ((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments)).entrySet()) {
            if (x.getValue() == null) continue;
            hold = (HashMap<String, Object>) x.getValue();
            hold2 = new HashMap<>();
            hold2.put("ship", ((ShipAPI)hold.get("ship")).getFleetMember());
            hold2.put("relativeCoOrd", new Vector2f(
                    ((Vector2f)hold.get("dest")).getX() - mid.getX(),
                    ((Vector2f)hold.get("dest")).getY() - mid.getY()
            ));
            hold2.put("relativeCoOrdSec", hold.get("destSec") == null ? null : new Vector2f(
                    ((Vector2f)hold.get("destSec")).getX() - mid.getX(),
                    ((Vector2f)hold.get("destSec")).getY() - mid.getY()
            ));
            hold2.put("moveAndRelease", hold.get("moveAndRelease"));
            positions.add(hold2);
        }
        mem.put("positions", positions);
        /* Save RTS control groups */
        HashMap<Integer, List<ShipAPI>> controlGroups = (HashMap<Integer, List<ShipAPI>>)this.getState(RTS_SelectionListener.stNames.controlGroups);
        HashMap<Integer, List<FleetMemberAPI>> controlGroupStorage = new HashMap<>();
        List<FleetMemberAPI> pointer;
        for (HashMap.Entry<Integer, List<ShipAPI>> group: controlGroups.entrySet()) {
            if (group.getValue() == null || group.getValue().isEmpty())
                continue;
            pointer = new ArrayList<>();
            for (ShipAPI ship : group.getValue())
                pointer.add(ship.getFleetMember());
            controlGroupStorage.put(group.getKey(), pointer);
        }
        mem.put("controlGroups", controlGroupStorage);
        /* Save escort assignments */
        List<HashMap<String, Object>> tasks = new ArrayList<>();
        HashMap<String, Object> taskPointer;
        List<FleetMemberAPI> shipsPointer;
        for (CombatFleetManagerAPI.AssignmentInfo unsavableTask : ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine))
                     .getFleetManager(0).getTaskManager(false).getAllAssignments()) {
            if (!unsavableTask.getType().equals(CombatAssignmentType.LIGHT_ESCORT)
                    && !unsavableTask.getType().equals(CombatAssignmentType.MEDIUM_ESCORT)
                    && !unsavableTask.getType().equals(CombatAssignmentType.HEAVY_ESCORT)
            )
                continue;
            taskPointer = new HashMap<>();
            shipsPointer = new ArrayList<>();
            for (DeployedFleetMemberAPI fShip: unsavableTask.getAssignedMembers())
                shipsPointer.add(fShip.getMember());
            taskPointer.put("members", shipsPointer);
            taskPointer.put("target", ((DeployedFleetMemberAPI)unsavableTask.getTarget()).getMember());
            taskPointer.put("type", unsavableTask.getType());
            tasks.add(taskPointer);
        }
        mem.put("tasks", tasks);
        this.saveDataToMemory(mem);

        ((RTS_SoundsManager)this.getState(RTSAssist.stNames.soundsManager)).playBasicUISound(
                "SaveAssignments",
                1f,
                0.3f
        );
    }

    public void saveDataToMemory (HashMap<String, Object> mem) {
        RTS_CommonsControl commonsControl = (RTS_CommonsControl)RTS_Global.get(RTSAssistModPlugin.names.commonsControl);
        try {
            JSONObject data = new JSONObject();
            /* Save move and hold/release assignments */
            List<HashMap<String, Object>> positions = (List<HashMap<String, Object>>)mem.get("positions");
            JSONArray pos = new JSONArray();
            JSONObject posPointer;
            for (HashMap<String, Object> posSet : positions) {
                posPointer = new JSONObject();
                posPointer.put("ship", ((FleetMemberAPI)posSet.get("ship")).getId());
                posPointer.put("relativeCoOrd", new JSONObject());
                ((JSONObject)posPointer.get("relativeCoOrd")).put("x", ((Vector2f)posSet.get("relativeCoOrd")).getX());
                ((JSONObject)posPointer.get("relativeCoOrd")).put("y", ((Vector2f)posSet.get("relativeCoOrd")).getY());
                posPointer.put("relativeCoOrdSec", new JSONObject());
                if (posSet.get("relativeCoOrdSec") != null) {
                    ((JSONObject)posPointer.get("relativeCoOrdSec")).put("x", ((Vector2f)posSet.get("relativeCoOrdSec")).getX());
                    ((JSONObject)posPointer.get("relativeCoOrdSec")).put("y", ((Vector2f)posSet.get("relativeCoOrdSec")).getY());
                }
                posPointer.put("moveAndRelease", (boolean)posSet.get("moveAndRelease"));
                pos.put(posPointer);
            }
            data.put("positions", pos);
            /* Save Control Groups */
            HashMap<Integer, List<FleetMemberAPI>> controlGroupStorage = (HashMap<Integer, List<FleetMemberAPI>>)mem.get("controlGroups");
            JSONObject groups = new JSONObject();
            JSONArray groupPointer;
            for (Map.Entry<Integer, List<FleetMemberAPI>> group : controlGroupStorage.entrySet()) {
                groupPointer = new JSONArray();
                for (FleetMemberAPI member : group.getValue())
                    groupPointer.put(member.getId());
                groups.put(group.getKey().toString(), groupPointer);
            }
            data.put("controlGroups", groups);
            /* Save Escort Assignments */
            List<HashMap<String, Object>> tasks = (List<HashMap<String, Object>>)mem.get("tasks");
            JSONArray escorts = new JSONArray();
            JSONObject escPointer;
            JSONArray membPointer;
            for (HashMap<String, Object> escortAss : tasks) {
                escPointer = new JSONObject();
                membPointer = new JSONArray();
                for (FleetMemberAPI member : (List<FleetMemberAPI>)escortAss.get("members"))
                    membPointer.put(member.getId());
                escPointer.put("members", membPointer);
                escPointer.put("target", ((FleetMemberAPI)escortAss.get("target")).getId());
                switch ((CombatAssignmentType)escortAss.get("type")) {
                    case HEAVY_ESCORT -> escPointer.put("type", "HEAVY_ESCORT");
                    case MEDIUM_ESCORT -> escPointer.put("type", "MEDIUM_ESCORT");
                    case LIGHT_ESCORT -> escPointer.put("type", "LIGHT_ESCORT");
                }
                escorts.put(escPointer);
            }
            data.put("tasks", escorts);
            /* clean up previous json */

            /* Write to storage */
            commonsControl.set("qSaveData_" + (String)RTS_Global.get(RTSAssistModPlugin.names.gameID), data);
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* ??Does this check if player still has saved ship in fleet?? */
    public void loadAssignments (Vector2f worldSpace) {
        HashMap<String, Object> mem = this.loadDataFromMemory();
        if (mem == null)
            return;
        /* Load all move and hold/release assignments */
        List<HashMap<String, Object>> positions = (List<HashMap<String, Object>>)mem.get("positions");
        if (positions != null) {
            ShipAPI shipHold;
            for (HashMap<String, Object> x : positions) {
                shipHold = ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).
                        getFleetManager(0).getShipFor((FleetMemberAPI)x.get("ship"));
                if (shipHold == null) continue;
                this.createAssignments(
                        null,
                        Arrays.asList(shipHold),
                        new Vector2f(
                                worldSpace.getX() + ((Vector2f)x.get("relativeCoOrd")).getX(),
                                worldSpace.getY() + ((Vector2f)x.get("relativeCoOrd")).getY()
                        ),
                        x.get("relativeCoOrdSec") == null ? null : new Vector2f(
                                worldSpace.getX() + ((Vector2f)x.get("relativeCoOrdSec")).getX(),
                                worldSpace.getY() + ((Vector2f)x.get("relativeCoOrdSec")).getY()
                        ),
                        (boolean)x.get("moveAndRelease"),
                        false,
                        false,
                        true,
                        false
                );
            }
        }
        /* Load all RTS control groups */
        HashMap<Integer, List<FleetMemberAPI>> controlGroupStorage =
                (HashMap<Integer, List<FleetMemberAPI>>)mem.get("controlGroups");
        if (controlGroupStorage != null) {
            HashMap<Integer, List<ShipAPI>> controlGroups = new HashMap<>();
            for (HashMap.Entry<Integer, List<FleetMemberAPI>> group: controlGroupStorage.entrySet()) {
                for (FleetMemberAPI fleetShip : group.getValue()) {
                    if (((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getFleetManager(0).getShipFor(fleetShip) == null)
                        continue;
                    if (!controlGroups.containsKey(group.getKey()))
                        controlGroups.put(group.getKey(), new ArrayList<>());
                    controlGroups.get(group.getKey()).add(((CombatEngineAPI)this.getState(RTSAssist.stNames.engine))
                            .getFleetManager(0).getShipFor(fleetShip));
                }
            }
            if (this.getState(RTS_SelectionListener.stNames.controlGroups) != null)
                ((HashMap<?, ?>)this.getState(RTS_SelectionListener.stNames.controlGroups)).clear();
            this.setState(RTS_SelectionListener.stNames.controlGroups, controlGroups);
        }
        /* Load all escort assignments */
        List<HashMap<String, Object>> tasks = (List<HashMap<String, Object>>)mem.get("tasks");
        if (tasks != null) {
            CombatFleetManagerAPI fleetApi = ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getFleetManager(0);
            CombatFleetManagerAPI.AssignmentInfo infoPointer;
            List<FleetMemberAPI> shipsWithOrders = new ArrayList<>();
            for (HashMap<String, Object> task : tasks) {
                if (fleetApi.getShipFor((FleetMemberAPI)task.get("target")) == null)
                    continue;
                infoPointer = fleetApi.getTaskManager(false).createAssignment(
                        (CombatAssignmentType)task.get("type"),
                        fleetApi.getDeployedFleetMember(
                                fleetApi.getShipFor((FleetMemberAPI)task.get("target"))
                        ),
                        false
                );
                for (FleetMemberAPI fShip: (List<FleetMemberAPI>)task.get("members")) {
                    if (fleetApi.getShipFor(fShip) == null)
                        continue;
                    fleetApi.getTaskManager(false).giveAssignment(
                            fleetApi.getDeployedFleetMember(fleetApi.getShipFor(fShip)),
                            infoPointer,
                            false
                    );
                    shipsWithOrders.add(fShip);
                }
            }
            for (ShipAPI x : ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getShips()) {
                if (x.isFighter() || x.getOriginalOwner() == 1 || x.isHulk() || x.isStation() || x.isAlly()) continue;
                if (!shipsWithOrders.contains(x.getFleetMember()))
                    fleetApi.getTaskManager(false).orderSearchAndDestroy(fleetApi.getDeployedFleetMember(x), false);
            }
        }

        ((RTS_SoundsManager)this.getState(RTSAssist.stNames.soundsManager)).playBasicUISound(
                "LoadAssignments",
                1f,
                0.3f
        );
    }

    public HashMap<String, Object> loadDataFromMemory () {
        RTS_CommonsControl commonsControl = (RTS_CommonsControl)RTS_Global.get(RTSAssistModPlugin.names.commonsControl);
        commonsControl.updateCache();
        HashMap<String, Object> mem = new HashMap<>();
        HashMap<String, FleetMemberAPI> memberHold = new HashMap<>();
        for (FleetMemberAPI member : ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getFleetManager(0).getDeployedCopy())
            memberHold.put(member.getId(), member);
        FleetMemberAPI member;
        try {
            JSONObject qSaveData = (JSONObject)commonsControl.get(
                    "qSaveData_" + (String)RTS_Global.get(RTSAssistModPlugin.names.gameID),
                    RTS_CommonsControl.JSONType.JSONOBJECT
            );
            if (qSaveData == null)
                return (null);
            /* Load Positions */
            List<HashMap<String, Object>> positions = new ArrayList<>();
            HashMap<String, Object> pointer;
            JSONObject jPointer;
            JSONArray pos = qSaveData.getJSONArray("positions");
            for (int i = 0; i < pos.length(); i++) {
                jPointer = pos.getJSONObject(i);
                if (memberHold.containsKey(jPointer.getString("ship"))) {
                    member = memberHold.get(jPointer.getString("ship"));
                    pointer = new HashMap<>();
                    pointer.put("ship", member);
                    pointer.put("relativeCoOrd", new Vector2f(
                            ((Double)jPointer.getJSONObject("relativeCoOrd").getDouble("x")).floatValue(),
                            ((Double)jPointer.getJSONObject("relativeCoOrd").getDouble("y")).floatValue()
                    ));
                    if (((JSONObject)jPointer.get("relativeCoOrdSec")).has("x"))
                        pointer.put("relativeCoOrdSec", new Vector2f(
                                ((Double)jPointer.getJSONObject("relativeCoOrdSec").getDouble("x")).floatValue(),
                                ((Double)jPointer.getJSONObject("relativeCoOrdSec").getDouble("y")).floatValue()
                        ));
                    pointer.put("moveAndRelease", jPointer.getBoolean("moveAndRelease"));
                    positions.add(pointer);
                }

            }
            mem.put("positions", !positions.isEmpty() ? positions : null);
            /* Load Control Groups */
            JSONObject groups = qSaveData.getJSONObject("controlGroups");
            HashMap<Integer, List<FleetMemberAPI>> controlGroupStorage = new HashMap<>();
            JSONArray groupPointer;
            for (int i = 0; i < 10; i++) {
                if (groups.has(((Integer)i).toString())) {
                    groupPointer = (JSONArray)groups.get(((Integer)i).toString());
                    for (int j = 0; j < groupPointer.length(); j++) {
                        if (memberHold.containsKey((String)groupPointer.get(j))) {
                            member = memberHold.get((String)groupPointer.get(j));
                            if (!controlGroupStorage.containsKey(i))
                                controlGroupStorage.put(i, new ArrayList<>());
                            controlGroupStorage.get(i).add(member);
                        }
                    }
                }
            }
            mem.put("controlGroups", controlGroupStorage);
            /* Load Vanilla Escorts */
            List<HashMap<String, Object>> tasks = new ArrayList<>();
            JSONArray escorts = qSaveData.getJSONArray("tasks");
            HashMap<String, Object> taskPointer;
            JSONObject escPointer;
            JSONArray followerPointer;
            List<FleetMemberAPI> followerList;
            for (int i = 0; i < escorts.length(); i++) {
                escPointer = (JSONObject)escorts.get(i);
                if (memberHold.containsKey((String)escPointer.get("target"))) {
                    taskPointer = new HashMap<>();
                    followerList = new ArrayList<>();
                    taskPointer.put("target", memberHold.get((String)escPointer.get("target")));
                    followerPointer = (JSONArray)escPointer.get("members");
                    for (int j = 0; j < followerPointer.length(); j++) {
                        if (memberHold.containsKey((String)followerPointer.get(j)))
                           followerList.add(memberHold.get((String)followerPointer.get(j)));
                    }
                    taskPointer.put("members", followerList);
                    switch ((String)escPointer.get("type")) {
                        case "HEAVY_ESCORT" -> taskPointer.put("type", CombatAssignmentType.HEAVY_ESCORT);
                        case "MEDIUM_ESCORT" -> taskPointer.put("type", CombatAssignmentType.MEDIUM_ESCORT);
                        case "LIGHT_ESCORT" -> taskPointer.put("type", CombatAssignmentType.LIGHT_ESCORT);
                    }
                    tasks.add(taskPointer);
                }
            }
            mem.put("tasks", tasks);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return (mem);
    }

    /* Assignment modifiers */

    public boolean convertAssignmentOrQueueToAttackMove(ShipAPI ship, boolean isShiftDown, ShipAPI enemy) {
        HashMap<String, Object> assPointer =
                (HashMap<String, Object>)((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments)).get(ship.getId());
        if (assPointer == null)
            return (false);
        HashMap<String, List<HashMap<String, Object>>> assignmentQueue =
                (HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue);
        List<HashMap<String, Object>> queuePointer = assignmentQueue.get(ship.getId());
        if (!isShiftDown && (queuePointer == null || queuePointer.isEmpty())) {
            if (
                    (assType)assPointer.get("assType") == assType.moveAndRelease
                    || (assType)assPointer.get("assType") == assType.moveAndHold
                    || (assType)assPointer.get("assType") == assType.dynamicHoldOnFlux
            ) {
                assPointer.put("assType", assType.attackMove);
                assPointer.put("moveAndRelease", true);
                ((RTS_SoundsManager)this.getState(RTSAssist.stNames.soundsManager)).playBasicUISound(
                        "AttackMoveConversion",
                        1f,
                        0.2f
                );
                return (true);
            }
        }
        else if (queuePointer != null && !queuePointer.isEmpty() && enemy == null) {
            if (
                    (assType)queuePointer.get(queuePointer.size() - 1).get("assType") == assType.moveAndRelease
                    || (assType)queuePointer.get(queuePointer.size() - 1).get("assType") == assType.moveAndHold
                    || (assType)assPointer.get("assType") == assType.dynamicHoldOnFlux
            ) {
                queuePointer.get(queuePointer.size() - 1).put("assType", assType.attackMove);
                queuePointer.get(queuePointer.size() - 1).put("moveAndRelease", true);
                return (true);
            }
        }

        return (false);
    }

    public void addNewTogaInstance (List<ShipAPI> selection) {
        if (selection == null)
            return;
        ((RTS_SoundsManager)this.getState(RTSAssist.stNames.soundsManager)).playBasicUISound(
                "GeneralUIFeedback01",
                1f,
                0.2f
        );
        List<ShipAPI> viableShips = new ArrayList<>();
        for (ShipAPI ship : selection)
            if (((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments)).get(ship.getId()) != null)
                viableShips.add(ship);
        if (viableShips.size() < 2)
            return;
        Integer instanceFlag = 0; // 0 do nothing, 1 create new instance, 2 use old instance, 3 create new instance and delete old, 4 delete instance
        boolean newInstanceDeleteOld = false;
        boolean oldInstance = false;
        boolean deleteInstance = false;
        RTS_MoveTogetherManager instancePointer = null;
        for (ShipAPI ship : viableShips) {
            if (((HashMap<String, RTS_MoveTogetherManager>)this.getState(RTS_TaskManager.stNames.togaStore)).get(ship.getId()) != null) {
                if (instancePointer == null) {
                    instancePointer = ((HashMap<String, RTS_MoveTogetherManager>)this.getState(RTS_TaskManager.stNames.togaStore)).get(ship.getId());
                    deleteInstance = true;
                }
                if (instancePointer != ((HashMap<String, RTS_MoveTogetherManager>)this.getState(RTS_TaskManager.stNames.togaStore)).get(ship.getId())) {
                    newInstanceDeleteOld = true;
                    break;
                }
            }
            else
                oldInstance = true;
        }
        instancePointer = null;
        if (newInstanceDeleteOld) {
            RTS_MoveTogetherManager instanceHold = new RTS_MoveTogetherManager(viableShips, (HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments));
            for (ShipAPI ship : viableShips) {
                instancePointer = ((HashMap<String, RTS_MoveTogetherManager>)this.getState(RTS_TaskManager.stNames.togaStore)).get(ship.getId());
                if (instancePointer != null)
                    instancePointer.removeShip(Arrays.asList(ship));
                if (instancePointer.queueForDeletion())
                    instancePointer.cleanup();
                ((HashMap<String, RTS_MoveTogetherManager>)this.getState(RTS_TaskManager.stNames.togaStore)).put(ship.getId(), instanceHold);
            }
            instanceFlag = 3;
        }
        else if (deleteInstance && oldInstance) {
            RTS_MoveTogetherManager instanceHold = null;
            for (ShipAPI ship : viableShips) {
                instancePointer = ((HashMap<String, RTS_MoveTogetherManager>)this.getState(RTS_TaskManager.stNames.togaStore)).get(ship.getId());
                if (instancePointer != null) {
                    instanceHold = ((HashMap<String, RTS_MoveTogetherManager>)this.getState(RTS_TaskManager.stNames.togaStore)).get(ship.getId());
                    break;
                }
            }
            instanceHold.addShip(viableShips);
            for (ShipAPI ship : viableShips)
                ((HashMap<String, RTS_MoveTogetherManager>)this.getState(RTS_TaskManager.stNames.togaStore)).put(ship.getId(), instanceHold);
            instanceFlag = 2;
        }
        else if (deleteInstance && !oldInstance) {
            for (ShipAPI ship : viableShips) {
                instancePointer = ((HashMap<String, RTS_MoveTogetherManager>)this.getState(RTS_TaskManager.stNames.togaStore)).get(ship.getId());
                if (instancePointer != null)
                    break;
            }
            instancePointer.removeShip(viableShips);
            if (instancePointer.queueForDeletion())
                instancePointer.cleanup();
            for (ShipAPI ship : viableShips)
                ((HashMap<String, RTS_MoveTogetherManager>)this.getState(RTS_TaskManager.stNames.togaStore)).put(ship.getId(), null);
            instanceFlag = 4;
        }
        else {
            RTS_MoveTogetherManager instanceHold = new RTS_MoveTogetherManager(viableShips, (HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments));
            for (ShipAPI ship : viableShips)
                ((HashMap<String, RTS_MoveTogetherManager>)this.getState(RTS_TaskManager.stNames.togaStore)).put(ship.getId(), instanceHold);
            instanceFlag = 1;
        }
    }

    /* Draw Stuff */

    private void drawDeploymentLine () {
        ((RTS_Draw)this.getState(RTSAssist.stNames.draw)).rudeDrawLine(
                new Vector2f(
                        ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getMapWidth() / -2,
                        CombatUtils.toScreenCoordinates(new Vector2f(
                                0,
                                (float)this.getState(RTS_TaskManager.stNames.dRestrict)
                        )).getY()
                ),
                new Vector2f(
                        ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getMapWidth() / 2,
                        CombatUtils.toScreenCoordinates(new Vector2f(
                                0,
                                (float)this.getState(RTS_TaskManager.stNames.dRestrict)
                        )).getY()
                )
        );
    }

    private void drawAssignments (HashMap<String, Object> assignment) {
        RTS_Draw Draw = ((RTS_Draw)this.getState(RTSAssist.stNames.draw));
        if (assignment.get("targetEnemy") == null) {
            if ((assType)assignment.get("assType") == assType.attackMove) {
                Draw.drawSelectedEnemy(
                        (Vector2f)assignment.get("dest"),
                        (float)this.getState(RTS_CameraRework.stNames.targetZoom),
                        true
                );
                if (assignment.get("modDest") != null)
                    Draw.drawSecondaryDest(
                            (Vector2f)assignment.get("modDest"),
                            (float)this.getState(RTS_CameraRework.stNames.targetZoom)
                    );
                if (assignment.get("closestEnemy") != null)
                    Draw.drawSelectedEnemy(
                            ((ShipAPI)assignment.get("closestEnemy")).getLocation(),
                            (float)this.getState(RTS_CameraRework.stNames.targetZoom),
                            false
                    );
            }
            else
                Draw.drawPrimaryDest(
                        (Vector2f)assignment.get("dest"),
                        (float)this.getState(RTS_CameraRework.stNames.targetZoom),
                        (boolean)assignment.get("moveAndRelease")
                );
        }
        if (assignment.get("targetEnemy") != null || assignment.get("priorityEnemy") != null) {
            Draw.drawSelectedEnemy(
                    assignment.get("targetEnemy") == null
                            ? ((ShipAPI)assignment.get("priorityEnemy")).getLocation()
                            : ((ShipAPI)assignment.get("targetEnemy")).getLocation(),
                    (float)this.getState(RTS_CameraRework.stNames.targetZoom),
                    (boolean)assignment.get("moveAndRelease")
            );
            if (assignment.get("angledAttack") != null) {
                Draw.drawSecondaryDest(
                        (Vector2f)assignment.get("angledAttack"),
                        (float)this.getState(RTS_CameraRework.stNames.targetZoom)
                );
            }
            else if (assignment.get("modDest") != null)
                Draw.drawSecondaryDest(
                        (Vector2f)assignment.get("modDest"),
                        (float)this.getState(RTS_CameraRework.stNames.targetZoom)
                );
        }
        if (assignment.get("destSec") != null && assignment.get("targetEnemy") == null)
            Draw.drawSecondaryDest(
                    (Vector2f)assignment.get("destSec"),
                    (float)this.getState(RTS_CameraRework.stNames.targetZoom)
            );
    }

    public void drawAssignmentQueue (List<HashMap<String, Object>> queuePointer) {
        RTS_Draw Draw = ((RTS_Draw)this.getState(RTSAssist.stNames.draw));
        HashMap<String, Object> assignment;
        Vector2f lineHoldPrev = null;
        Vector2f lineHoldNext = null;
        for (HashMap<String, Object> qInst : queuePointer){
            assignment = (HashMap<String, Object>)((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments)).get(((ShipAPI)qInst.get("ship")).getId());
            if (assignment == null
                    || !((boolean)this.getState(RTS_ParseInput.stNames.isAltDown)
                    || (this.getState(RTS_ParseInput.stNames.currentSelection) != null
                    && ((List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)).contains(((List<ShipAPI>)qInst.get("selection")).get(0)))
            ))
                return;
            if ((assType)qInst.get("assType") == assType.basicAttack) {
                Draw.drawSelectedEnemy(
                        ((ShipAPI)qInst.get("primaryEnemy")).getLocation(),
                        (float)this.getState(RTS_CameraRework.stNames.targetZoom),
                        (boolean)qInst.get("moveAndRelease")
                );
                lineHoldNext = ((ShipAPI)qInst.get("primaryEnemy")).getLocation();
            }
            else if ((assType)qInst.get("assType") == assType.attackAtAngle) {
                {
                    Draw.drawSelectedEnemy(
                            ((ShipAPI)qInst.get("primaryEnemy")).getLocation(),
                            (float)this.getState(RTS_CameraRework.stNames.targetZoom),
                            (boolean)qInst.get("moveAndRelease")
                    );
                    Draw.drawSecondaryDest(
                            (Vector2f)qInst.get("coOrdSec"),
                            (float)this.getState(RTS_CameraRework.stNames.targetZoom)
                    );
                    lineHoldNext = ((ShipAPI)qInst.get("primaryEnemy")).getLocation();
                }
            } else {
                if ((assType)qInst.get("assType") == assType.attackMove)
                    Draw.drawSelectedEnemy(
                            qInst.get("primaryEnemy") == null
                                    ? (Vector2f)qInst.get("coOrd")
                                    : ((ShipAPI)qInst.get("primaryEnemy")).getLocation(),
                            (float)this.getState(RTS_CameraRework.stNames.targetZoom),
                            false
                    );
                else
                    Draw.drawPrimaryDest(
                            (Vector2f)qInst.get("coOrd"),
                            (float)this.getState(RTS_CameraRework.stNames.targetZoom),
                            (boolean)qInst.get("moveAndRelease")
                    );
                lineHoldNext = qInst.get("primaryEnemy") == null
                        ? (Vector2f)qInst.get("coOrd")
                        : ((ShipAPI)qInst.get("primaryEnemy")).getLocation();
            }
            if (lineHoldPrev == null) {
                Draw.rudeDrawLine(
                        CombatUtils.toScreenCoordinates(((ShipAPI)qInst.get("ship")).getLocation()),
                        assignment.get("targetEnemy") == null
                                ? CombatUtils.toScreenCoordinates((Vector2f)assignment.get("dest"))
                                : CombatUtils.toScreenCoordinates(((ShipAPI)assignment.get("targetEnemy")).getLocation())
                );
                lineHoldPrev = assignment.get("targetEnemy") == null
                        ? (Vector2f)assignment.get("dest")
                        : ((ShipAPI)assignment.get("targetEnemy")).getLocation();
            }
            Draw.rudeDrawLine(
                    CombatUtils.toScreenCoordinates(lineHoldPrev),
                    CombatUtils.toScreenCoordinates(lineHoldNext)
            );
            lineHoldPrev = lineHoldNext;
        }
    }

    private void drawTOrRassignments () {
        if (((HashMap<String, Vector2f>)this.getState(RTS_TaskManager.stNames.translateStore)).isEmpty()
                && ((HashMap<String, Vector2f>)this.getState(RTS_TaskManager.stNames.rotationStore)).isEmpty())
            return;
        RTS_Draw Draw = ((RTS_Draw)this.getState(RTSAssist.stNames.draw));
        HashMap<String, Vector2f> translateStore = (HashMap<String, Vector2f>)this.getState(RTS_TaskManager.stNames.translateStore);
        HashMap<String, Float> rotationStore = (HashMap<String, Float>)this.getState(RTS_TaskManager.stNames.rotationStore);
        HashMap<String, Object> assPointer;
        HashMap<String, Vector2f> destHold = new HashMap<>();
        Vector3f totals = new Vector3f(0, 0, 0);
        Vector2f pivotPoint = new Vector2f();

        if (!translateStore.isEmpty())
            for (Map.Entry<String, Vector2f> shipStore : translateStore.entrySet()) {
                assPointer = (HashMap<String, Object>)((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments))
                        .get(shipStore.getKey());
                if (assPointer == null)
                    continue;
                destHold.put(shipStore.getKey(), new Vector2f(
                        ((Vector2f)assPointer.get("dest")).getX() + shipStore.getValue().getX(),
                        ((Vector2f)assPointer.get("dest")).getY() + shipStore.getValue().getY()
                ));
            }
        else {
            for (Map.Entry<String, Float> shipStore : rotationStore.entrySet()) {
                assPointer = (HashMap<String, Object>)((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments))
                        .get(shipStore.getKey());
                if (assPointer == null)
                    continue;
                destHold.put(shipStore.getKey(), new Vector2f(
                        ((Vector2f)assPointer.get("dest")).getX(),
                        ((Vector2f)assPointer.get("dest")).getY()
                ));
            }
        }
        if (!rotationStore.isEmpty()) {
            for (Map.Entry<String, Vector2f> shipStore : destHold.entrySet()) {
                totals.set(
                        shipStore.getValue().getX() + totals.getX(),
                        shipStore.getValue().getY() + totals.getY(),
                        totals.getZ() + 1
                );
            }
            pivotPoint.set(
                    totals.getX() / totals.getZ(),
                    totals.getY() / totals.getZ()
            );
            for (Map.Entry<String, Vector2f> shipStore : destHold.entrySet()) {
                VectorUtils.rotateAroundPivot(
                        shipStore.getValue(),
                        pivotPoint,
                        rotationStore.get(shipStore.getKey())
                );
            }
        }
        for (Map.Entry<String, Vector2f> shipStore : destHold.entrySet()) {
            assPointer = (HashMap<String, Object>)((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments))
                    .get(shipStore.getKey());
            Draw.drawPrimaryDest(
                    shipStore.getValue(),
                    (float)this.getState(RTS_CameraRework.stNames.targetZoom),
                    (boolean)assPointer.get("moveAndRelease")
            );
        }
        destHold.clear();
    }

    /* Combat Initilisation */

    public void handleDeployment (float elapsedTime) {
        if ((boolean)this.getState(RTS_TaskManager.stNames.combatStarted))
            return;
        if (((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).isSimulation() ||
                (((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getContext().getPlayerGoal() != null &&
                        ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getContext().getPlayerGoal().name().contains("ESCAPE"))) {
            this.postDeploymentInit();
            this.setState(RTS_TaskManager.stNames.combatStarted, true);
            this.setState(RTS_TaskManager.stNames.dRestrict, 100000f);
            return;
        }
        if (this.getState(RTS_TaskManager.stNames.deployment) == null && elapsedTime > 0f) {
            this.postDeploymentInit();
            List<ShipAPI> shipList = ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getShips();
            int flag = 0;
            for (ShipAPI ship : shipList)
                if (ship.isStation() && ship.getOriginalOwner() == 0)
                    flag = 1;
            if (flag == 1)
                this.setState(RTS_TaskManager.stNames.dRestrict, -3500f);
            else {
                float enemyLowest = 1000000;
                for (ShipAPI ship : shipList) {
                    if ((ship.getOriginalOwner() != 0 && !ship.isAlly()) && ship.getLocation().getY() < enemyLowest)
                        enemyLowest = ship.getLocation().getY();
                }
                List<Vector2f> shipGroup = new ArrayList<>();
                for (ShipAPI ship : shipList) {
                    if (
                            ((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener)).dontSelect(ship)
                            || ship.getLocation().getY() > enemyLowest
                    )
                        continue;
                    shipGroup.add(ship.getLocation());
                }
                if (!shipGroup.isEmpty()) {
                    shipGroup.sort(RTS_AssortedFunctions.Vector2fComparator('y'));
                    this.setState(RTS_TaskManager.stNames.dRestrict, shipGroup.get(shipGroup.size() - 1).getY() + 500f);
                }
                else
                    this.setState(RTS_TaskManager.stNames.dRestrict, -3500f);
            }
            this.setState(RTS_TaskManager.stNames.deployment, elapsedTime);
            ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).setPaused(true);
        }
        else if (this.getState(RTS_TaskManager.stNames.deployment) != null &&
                (float)this.getState(RTS_TaskManager.stNames.deployment) != elapsedTime) {
            this.setState(RTS_TaskManager.stNames.combatStarted, true);
            this.setState(RTS_TaskManager.stNames.dRestrict, 100000f);
            HashMap<String, Object> pointer;
            for (Map.Entry<String, Object> x : ((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments)).entrySet()) {
                if (x.getValue() == null) continue;
                pointer = (HashMap<String, Object>) x.getValue();
                ((Vector2f) pointer.get("dest")).setY(((Vector2f) pointer.get("dest")).getY() +
                        ((CombatEngineAPI) this.getState(RTSAssist.stNames.engine)).
                                getContext().getInitialDeploymentBurnDuration() * 1250f);
                if (pointer.get("destSec") != null) {
                    ((Vector2f)pointer.get("destSec")).setY(((Vector2f) pointer.get("destSec")).getY() +
                            ((CombatEngineAPI) this.getState(RTSAssist.stNames.engine)).
                                    getContext().getInitialDeploymentBurnDuration() * 1250f);
                }
            }


        }
    }

    private void hookFriendlyAIs () {
        ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager)).addListener(new RTS_Listener() {
            @Override
            public String type() { return(RTS_ShipLocAPI.evNames.newShipsDeployed); };

            @Override
            public void run(HashMap<String, Object> e) {
                List<ShipAPI> newShips = (List<ShipAPI>)e.get(RTS_ShipLocAPI.evNames.newShipsDeployed);
                for (ShipAPI ship : newShips) {
                    if (((RTS_SelectionListener)getState(RTSAssist.stNames.selectionListener)).dontSelect(ship))
                        continue;
                    ((RTS_AIInjector)getState(RTSAssist.stNames.AIInjector)).hookAI(ship);
                }
            }

            @Override
            public boolean removeOnCompletion() { return(false); };
        });
    }

    private void postDeploymentInit () {
        /* Bind our wrapper onto friendly ship AI. */
        ((RTS_ModeManager)this.getState(RTSAssist.stNames.modeManager)).firstFrameOfBattle();
        for (ShipAPI ship : ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getShips()) {
            if (((RTS_SelectionListener)getState(RTSAssist.stNames.selectionListener)).dontSelect(ship))
                continue;
            ((RTS_AIInjector)getState(RTSAssist.stNames.AIInjector)).hookAI(ship);
        }
        /* Automatically assign the player as captain of a neural linked ship if the player has neural
         * link and their is a viable ship. */
        this.handleNeuralLinkAtDeployment();
    }

    private void handleNeuralLinkAtDeployment () {
        if (
                Global.getSector() != null
                        && Global.getSector().getPlayerFleet() != null
                        && Global.getSector().getPlayerStats().getSkillLevel("neural_link") == 1
        ) {
            PersonAPI player = null;
            List<ShipAPI> affectedShips = new ArrayList<>();
            for (ShipAPI ship: ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getShips())
                if (!((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener)).dontSelect(ship)) {
                    if (ship.getCaptain().isPlayer())
                        player = ship.getCaptain();
                    if (ship.getVariant().getHullMods().contains("neural_interface"))
                        affectedShips.add(ship);
                }
            if (player != null && affectedShips.size() == 2)
                for (ShipAPI ship: affectedShips)
                    if (!ship.getCaptain().isPlayer())
                        ship.setCaptain(player);
//            /* Add a listener that ckecks if newly added ships are the player and and then checks if their is
//             * a viable neural linked  ship. BUG If the viable ship is currently selected, ss core will overwrite
//             * ship.setCaptain and if the even if the captain is set every frame, neural link UI seems bugged./
//            ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager)).addListener(new RTS_Listener() {
//                @Override
//                public String type () {
//                    return (RTS_ShipLocAPI.evNames.newShipsDeployed);
//                }
//
//                @Override
//                public void run (HashMap<String, Object> e) {
//                    List<ShipAPI> newShips = (List<ShipAPI>)e.get(RTS_ShipLocAPI.evNames.newShipsDeployed);
//                    for (ShipAPI ship: newShips)
//                        if (ship.getCaptain().isPlayer()) {
//                            List<ShipAPI> affectedShips = new ArrayList<>();
//                            for (ShipAPI affectedShip: ((CombatEngineAPI)getState(RTSAssist.stNames.engine)).getShips())
//                                if (!((RTS_SelectionListener)getState(RTSAssist.stNames.selectionListener)).dontSelect(ship))
//                                    if (affectedShip.getVariant().getHullMods().contains("neural_interface"))
//                                        affectedShips.add(affectedShip);
//                            if (affectedShips.size() == 2)
//                                for (ShipAPI affectedShip : affectedShips)
//                                    if (!affectedShip.getCaptain().isPlayer()) {
//                                        ((RTS_EventManager)getState(RTSAssist.stNames.eventManager)).addEvent(new RTS_Event() {
//
//                                            @Override
//                                            public boolean shouldExecute(Object state) {
//                                                affectedShip.setCaptain(ship.getCaptain());
//                                                return (false);
//                                            }
//                                        });
//                                    }
//                            break;
//                        }
//                }
//
//                @Override
//                public boolean removeOnCompletion() { return(false); }
//            });
        }
    }

    private void firstFrameInit () {
        if (!(boolean)this.getState(RTS_TaskManager.stNames.firstFrameInit)) {
            if (((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getPlayerShip().getName().equals("Command Shuttle"))
                this.setState(RTS_TaskManager.stNames.commandShuttle, ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getPlayerShip());
            this.setState(RTS_TaskManager.stNames.firstFrameInit, true);
            this.setState(RTS_TaskManager.stNames.ShipLocAPI, new RTS_ShipLocAPI(this.returnState()));
            this.hookFriendlyAIs();
            ((RTS_SelectionListener)this.getState(RTSAssist.stNames.selectionListener)).buildListeners();
            this.buildListeners();
        }
    }

    /* The actual Doing, called in RTSAssist->Advance */

    public void update (float elapsedTime) {
        this.firstFrameInit();
        this.handleDeployment(elapsedTime);

        /* EveryFrame updates */
        ((RTS_ShipLocAPI)this.getState(RTS_TaskManager.stNames.ShipLocAPI)).update();
        if ((boolean)this.getState(RTS_TaskManager.stNames.combatStarted)) {
            ((RTS_CollisionManager)this.getState(RTS_TaskManager.stNames.RTSCollisionManager)).update();
            ((RTS_ModeManager)this.getState(RTSAssist.stNames.modeManager)).handlePlayerShipAi();
            ((RTS_ModeManager)this.getState(RTSAssist.stNames.modeManager)).playerSwitchesToDifferentShip();
            ((RTS_AIInjector)this.getState(RTSAssist.stNames.AIInjector)).update();
            ((RTS_VentManager)this.getState(RTS_TaskManager.stNames.ventManager)).update();

            List<RTS_MoveTogetherManager> hold = new ArrayList<>();
            List<String> sHold = new ArrayList<>();

            for(Map.Entry<String, RTS_MoveTogetherManager> instance : ((HashMap<String, RTS_MoveTogetherManager>)this.getState(RTS_TaskManager.stNames.togaStore)).entrySet()) {
                if (instance.getValue() == null)
                    continue;
                if (instance.getValue().queueForDeletion()) {
                    instance.getValue().cleanup();
                    sHold.add(instance.getKey());
                }
                else if (!hold.contains(instance.getValue()))
                    hold.add(instance.getValue());
            }
            for (String id : sHold)
                ((HashMap<String, RTS_MoveTogetherManager>)this.getState(RTS_TaskManager.stNames.togaStore)).put(id, null);
            for (RTS_MoveTogetherManager instance : hold)
                instance.update();
        }

        /* Needs to go somewhere */
        this.drawTOrRassignments();

        /* If battle hasnt started, draw the deployment line */
        if (this.getState(RTS_TaskManager.stNames.deployment) != null && !(boolean)this.getState(RTS_TaskManager.stNames.combatStarted))
            this.drawDeploymentLine();

        /* Iterate through the current assignments */
        HashMap<String, Object> pointer;
        for (Map.Entry<String, Object> x: ((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments)).entrySet()) {
            pointer = (HashMap<String, Object>)x.getValue();
            if (pointer == null) continue;

            /* * If battle has'nt started, instantly move ships.
               * Check if an assignment should be deleted, if so delete it and move on, if not process assignment
                 and continue. */
            if (!(boolean)this.getState(RTS_TaskManager.stNames.combatStarted))
                ((ShipAPI)pointer.get("ship")).getLocation().set((Vector2f)pointer.get("dest"));
            else if (!this.manageDeletions(pointer))
                ((RTS_ShipMoveToPosition)pointer.get("shipTask")).moveToPosition(pointer,true);
            else
                continue;

            /* If the ship has a priority enemy, if its dead remove that priority enemy */
            if (pointer.get("priorityEnemy") != null && ((ShipAPI)pointer.get("priorityEnemy")).isHulk())
                pointer.put("priorityEnemy", null);

            /* Check if we should draw this ships assignments, then draw them. */
            if ((boolean)this.getState(RTS_ParseInput.stNames.isAltDown)
                    || (this.getState(RTS_ParseInput.stNames.currentSelection) != null
                            && ((List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection)).contains(pointer.get("ship")))
            )
                this.drawAssignments(pointer);
        }

        /*check for queued assignments and create them and draw them. */
        for (HashMap.Entry<String, List<HashMap<String, Object>>> x
                : ((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue)).entrySet())
            this.drawAndAssignAssignmentFromQueue(x.getKey());
    }
}