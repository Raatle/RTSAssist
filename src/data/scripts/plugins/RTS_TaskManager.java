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

import com.fs.starfarer.api.combat.*;
import data.scripts.plugins.AISystems.RTS_AIInjector;
import data.scripts.plugins.Setup.RTS_TaskManagerAssSetup;
import data.scripts.plugins.Utils.*;
import org.lazywizard.lazylib.CollisionUtils;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.util.*;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.FastTrig;

import data.scripts.plugins.BlockedSystems.*;

public class RTS_TaskManager extends RTS_StatefulClasses {

    public static class classidentifiers {
        public String assignments = RTS_StatefulClasses.getUniqueIdentifier();
        public String assignmentQueue = RTS_StatefulClasses.getUniqueIdentifier();
        public String translateStore = RTS_StatefulClasses.getUniqueIdentifier();
        public String rotationStore = RTS_StatefulClasses.getUniqueIdentifier();
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
            put(RTS_TaskManager.stNames.rotationStore, new HashMap<>());
            put(RTS_TaskManager.stNames.togaStore, new HashMap<>());
            put(RTS_TaskManager.stNames.firstFrameInit, false); // flag for firstFrameInit
            put(RTS_TaskManager.stNames.deployment, null); // initialised in handleDeployment
            put(RTS_TaskManager.stNames.combatStarted, false); // flag for handleDepolyment
            put(RTS_TaskManager.stNames.dRestrict, -100000f); // Restricts where the player can place ships
            put(RTS_TaskManager.stNames.ShipLocAPI, null); // initilised in firstFrameInit
            put(RTS_TaskManager.stNames.RTSCollisionManager, null); // down (requires state)
            put(RTS_TaskManager.stNames.commandShuttle, null); // initilised in firstFrameInit
            put(RTS_TaskManager.stNames.ventManager, new RTS_VentManager());
        }};

        init.put(RTS_TaskManager.stNames.RTSCollisionManager, new RTS_CollisionManager(this.returnState()));
        init.put(RTS_TaskManager.stNames.systemManager, new RTS_SystemManager(this.returnState()));
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
                    deleteAssignments((List<ShipAPI>)e.get(RTS_ShipLocAPI.evNames.shipsNoLongerActive), true);
                e.clear();
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
            boolean wasTranslated
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
                checkList.put("moveAndRelease", moveAndRelease
                        && !(boolean)this.getDeepState(Arrays.asList(RTSAssist.stNames.config, RTSAssist.coNames.switchRightClick)));
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
                this.deleteAssignments(Arrays.asList(x), true);
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
            assignments.put(x.getId(), shipHold);
            this.setState(RTS_TaskManager.stNames.assignments, assignments);
        }
        Global.getSoundPlayer().playUISound(
                primaryEnemy != null ? "ui_channel_comm_local_04" :
                coOrdSec == null && !moveAndRelease ? "ui_channel_intel_assessment_01" :
                        !moveAndRelease ? "ui_channel_news_04" : "ui_channel_default_04",
                1f,
                0.3f
        );
    }

    // Dont queue duplicates(they show when drawing).
    public void queueAssignments (List<ShipAPI> selection, Vector2f coOrd, Vector2f coOrdSec, boolean moveAndRelease, boolean attackMove) {
        if (!(boolean)this.getState(RTS_TaskManager.stNames.combatStarted))
            return;

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
                            createAssignments(null, selection, coOrd, coOrdSec, moveAndRelease, true, false, false);
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
        if (type.equals("primary")) {
            List<ShipAPI> targetedEnemyHoldPrimary = (boolean)this.getState(RTS_TaskManager.stNames.combatStarted)
                    ? ((RTS_ShipLocAPI)this.getState(RTS_TaskManager.stNames.ShipLocAPI)).findEnemyShipNeighbours(coOrd, 500f, true)
                    : new ArrayList<>();
            ShipAPI primaryEnemy = null;
            for (ShipAPI ship : targetedEnemyHoldPrimary) {
                ship.getExactBounds().update(ship.getLocation(), ship.getFacing());
                if (CollisionUtils.isPointWithinBounds(coOrd, (CombatEntityAPI)ship)) {
                    primaryEnemy = ship;
                    break;
                }
            }
            return (primaryEnemy);
        }
        else {
            List<ShipAPI> targetedEnemyHoldSecondary = (boolean)this.getState(RTS_TaskManager.stNames.combatStarted) && coOrdSec != null
                    ? ((RTS_ShipLocAPI)this.getState(RTS_TaskManager.stNames.ShipLocAPI)).findEnemyShipNeighbours(coOrdSec, 500f, true)
                    : new ArrayList<>();
            ShipAPI secondaryEnemy = null;
            for (ShipAPI ship : targetedEnemyHoldSecondary) {
                ship.getExactBounds().update(ship.getLocation(), ship.getFacing());
                if (CollisionUtils.isPointWithinBounds(coOrdSec, (CombatEntityAPI)ship)) {
                    secondaryEnemy = ship;
                    break;
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
        float posModFactor = ((HashMap<String, Object>)this.getState(RTSAssist.stNames.broadsideData))
                .containsKey(ship.getHullSpec().getBaseHullId())
                ? (float)FastTrig.cos(Math.abs((float)((HashMap<String, Object>)this.getState(RTSAssist.stNames.broadsideData))
                .get(ship.getHullSpec().getBaseHullId())) / 57f)
                : 1f;
        for (WeaponAPI set : ship.getUsableWeapons()) {
            hold.put(
                    set.getId(),
                    ship.getLocation().getY() - (VectorUtils.rotateAroundPivot(
                            set.getLocation(),
                            ship.getLocation(),
                            (ship.getFacing() * -1) + 90f
                    ).getY() * posModFactor)
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
                    this.deleteAssignments(Arrays.asList(x), false);
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
            deleteAssignments(Arrays.asList((ShipAPI)assignment.get("ship")), false);
            return (true);
        }
        else
            return (false);
    }

    public void deleteAssignments (List<ShipAPI> selection, boolean clearQueue) {
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
            if (clearQueue && (((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue))
                    .get(x.getId()) != null
                    && !((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue))
                    .get(x.getId()).isEmpty())) {
                for (HashMap<String, Object> queue : ((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue)).get(x.getId()))
                    queue.clear();
                ((HashMap<String, List<HashMap<String, Object>>>)this.getState(RTS_TaskManager.stNames.assignmentQueue)).get(x.getId()).clear();
            }
        }
        if (flag) Global.getSoundPlayer().playUISound(
                "ui_channel_default_02", 1f, 0.3f
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
    }

    public void rotateAssignments (List<ShipAPI> selection, Vector2f grabPoint, Vector2f placePoint, boolean mockUp, boolean queue) {
        if (selection == null || grabPoint == null || placePoint == null) return;
        if (!mockUp) {
            this.setAssignmentsMockUps(queue);
            return;
        }
        HashMap<String, Float> rotationStore = (HashMap<String, Float>)this.getState(RTS_TaskManager.stNames.rotationStore);
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
        pivotPoint.set(
                totals.getX() / totals.getZ(),
                totals.getY() / totals.getZ()
        );
        angle = MathUtils.getShortestRotation(
                VectorUtils.getAngle(pivotPoint, grabPoint),
                VectorUtils.getAngle(pivotPoint, placePoint)
        );
        for (ShipAPI ship : selection)
            rotationStore.put(
                    ship.getId(),
//                  keep this and integrate it as a pro feature   rotationStore.get(ship.getId()) == null ? angle : rotationStore.get(ship.getId()) + angle //
                    angle
            );
    }

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
                        true
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
        Global.getSoundPlayer().playUISound(
                "ui_channel_sniffer_01", 1f, 0.3f
        );
        translateStore.clear();
        rotationStore.clear();
        destHold.clear();
        destSecHold.clear();
    }

    public void saveAssignments () {
        if  (Global.getSector().getMemory().get("$RTSASSIST_saveData") == null)
            return;
        HashMap<String, Object> mem = (HashMap<String, Object>)Global.getSector().getMemory().get("$RTSASSIST_saveData");
        Vector2f min = new Vector2f(100000f, 100000f);
        Vector2f max = new Vector2f(-100000f,-100000f);
        Vector2f mid;
        HashMap<String, Object> hold;
        for (Map.Entry<String, Object> x: ((HashMap<String, Object>)this.getState(RTS_TaskManager.stNames.assignments)).entrySet()) {
            if (x.getValue() == null) continue;
            hold = (HashMap<String, Object>) x.getValue();
            min.setX(Math.min(min.getX(), ((Vector2f)hold.get("dest")).getX()));
            min.setY(Math.min(min.getY(), ((Vector2f)hold.get("dest")).getY()));
            max.setX(Math.max(max.getX(), ((Vector2f)hold.get("dest")).getX()));
            max.setY(Math.max(max.getY(), ((Vector2f)hold.get("dest")).getY()));
        }
        mid = new Vector2f(
                (min.getX() + max.getX()) / 2f,
                (min.getY() + max.getY()) / 2f
        );
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
        if (mem.get("positions") != null) {
            for (HashMap<String, Object> x : (ArrayList<HashMap<String, Object>>)mem.get("positions"))
                x.clear();
            ((ArrayList<HashMap<String, Object>>)mem.get("positions")).clear();
        }
        mem.put("positions", positions);

        HashMap<Integer, List<ShipAPI>> controlGroups = (HashMap<Integer, List<ShipAPI>>)this.getState(RTS_SelectionListener.stNames.controlGroups);
        HashMap<Integer, List<FleetMemberAPI>> controlGroupStorage = new HashMap<>();
        List<FleetMemberAPI> pointer;
        for (HashMap.Entry<Integer, List<ShipAPI>> group: controlGroups.entrySet()) {
            if (group.getValue().isEmpty())
                continue;
            pointer = new ArrayList<>();
            for (ShipAPI ship : group.getValue())
                pointer.add(ship.getFleetMember());
            controlGroupStorage.put(group.getKey(), pointer);
        }
        if (mem.get("controlGroups") != null) {
            for (HashMap.Entry<Integer, List<FleetMemberAPI>> group : ((HashMap<Integer, List<FleetMemberAPI>>)mem.get("controlGroups")).entrySet())
                group.getValue().clear();
            ((HashMap<Integer, List<FleetMemberAPI>>)mem.get("controlGroups")).clear();
        }
        mem.put("controlGroups", controlGroupStorage);

        ArrayList<HashMap<String, Object>> tasks = new ArrayList<>();
        HashMap<String, Object> taskPointer;
        List<FleetMemberAPI> shipPointer;
        for (CombatFleetManagerAPI.AssignmentInfo unsavableTask : ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine))
                     .getFleetManager(0).getTaskManager(false).getAllAssignments()) {
            if (!unsavableTask.getType().equals(CombatAssignmentType.LIGHT_ESCORT)
                    && !unsavableTask.getType().equals(CombatAssignmentType.MEDIUM_ESCORT)
                    && !unsavableTask.getType().equals(CombatAssignmentType.HEAVY_ESCORT)
            )
                continue;
            taskPointer = new HashMap<>();
            shipPointer = new ArrayList<>();
            for (DeployedFleetMemberAPI fShip: unsavableTask.getAssignedMembers())
                shipPointer.add(fShip.getMember());
            taskPointer.put("members", shipPointer);
            taskPointer.put("target", ((DeployedFleetMemberAPI)unsavableTask.getTarget()).getMember());
            taskPointer.put("type", unsavableTask.getType());
            tasks.add(taskPointer);
        }
        if (mem.get("tasks") != null) {
            for (HashMap<String, Object> task : (ArrayList<HashMap<String, Object>>)mem.get("tasks")) {
                ((ArrayList<?>)task.get("members")).clear();
                task.clear();
            }
            ((ArrayList<?>)mem.get("tasks")).clear();
        }
        mem.put("tasks", tasks);

        Global.getSoundPlayer().playUISound(
                "ui_channel_comm_secure_01", 1f, 0.3f
        );
    }

    public void loadAssignments (Vector2f worldSpace) {
        if (Global.getSector().getMemory().get("$RTSASSIST_saveData") == null)
            return;

        if (((HashMap<String, Object>)Global.getSector().getMemory().get("$RTSASSIST_saveData"))
                .get("positions") != null) {
            List<HashMap<String, Object>> positions = (List<HashMap<String, Object>>)((HashMap<String, Object>)Global
                    .getSector().getMemory().get("$RTSASSIST_saveData")).get("positions");
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
                        true
                );
            }
        }

        if (((HashMap<String, Object>)Global.getSector().getMemory().get("$RTSASSIST_saveData"))
                .get("controlGroups") != null) {
            HashMap<Integer, List<ShipAPI>> controlGroups = new HashMap<>();
            HashMap<Integer, List<FleetMemberAPI>> controlGroupStorage =
                    (HashMap<Integer, List<FleetMemberAPI>>)((HashMap<String, Object>)Global.getSector().getMemory()
                            .get("$RTSASSIST_saveData")).get("controlGroups");
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

        if (((HashMap<String, Object>)Global.getSector().getMemory().get("$RTSASSIST_saveData"))
                .get("tasks") != null) {
            List<HashMap<String, Object>> tasks = (List<HashMap<String, Object>>)((HashMap<String, Object>)Global.getSector().
                    getMemory().get("$RTSASSIST_saveData")).get("tasks");
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
        Global.getSoundPlayer().playUISound(
                "ui_channel_comm_local_01", 1f, 0.3f
        );
    }

    /* Assignment modifiers */

    public boolean convertAssignmentOrQueueToAttackMove(ShipAPI ship, boolean isShiftDown) {
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
                return (true);
            }
        }
        else if (queuePointer != null && !queuePointer.isEmpty()) {
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
            if ((assType)assignment.get("assType") == assType.attackMove)
                Draw.drawSelectedEnemy(
                        (Vector2f)assignment.get("dest"),
                        (float)this.getState(RTS_CameraRework.stNames.targetZoom),
                        true
                );
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
                            (Vector2f)qInst.get("coOrd"),
                            (float)this.getState(RTS_CameraRework.stNames.targetZoom),
                            false
                    );
                else
                    Draw.drawPrimaryDest(
                            (Vector2f)qInst.get("coOrd"),
                            (float)this.getState(RTS_CameraRework.stNames.targetZoom),
                            (boolean)qInst.get("moveAndRelease")
                    );
                lineHoldNext = (Vector2f)qInst.get("coOrd");
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
            int flag = 0;
            for (ShipAPI ship : ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getShips())
                if (ship.isStation() && ship.getOriginalOwner() == 0)
                    flag = 1;
            if (flag == 0) {
                for (ShipAPI x : ((CombatEngineAPI) this.getState(RTSAssist.stNames.engine)).getShips()) {
                    if (x.isFighter() || x.getOriginalOwner() == 1) continue;
                    this.setState(RTS_TaskManager.stNames.dRestrict,
                            Math.max((float) this.getState(RTS_TaskManager.stNames.dRestrict), x.getLocation().getY()));
                }
                this.setState(RTS_TaskManager.stNames.dRestrict, (float) this.getState(RTS_TaskManager.stNames.dRestrict) + 500f);
            }
            else
                this.setState(RTS_TaskManager.stNames.dRestrict, -3500f);
            this.setState(RTS_TaskManager.stNames.deployment, elapsedTime);
            ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).setPaused(true);
        }
        else if (this.getState(RTS_TaskManager.stNames.deployment) != null &&
                (float)this.getState(RTS_TaskManager.stNames.deployment) != elapsedTime) {
            this.setState(RTS_TaskManager.stNames.combatStarted, true);
            this.setState(RTS_TaskManager.stNames.dRestrict, 100000f);
            HashMap<String, Object> pointer;
            for (Map.Entry<String, Object> x : ((HashMap<String, Object>) this.getState(RTS_TaskManager.stNames.assignments)).entrySet()) {
                if (x.getValue() == null) continue;
                pointer = (HashMap<String, Object>) x.getValue();
                ((Vector2f) pointer.get("dest")).setY(((Vector2f) pointer.get("dest")).getY() +
                        ((CombatEngineAPI) this.getState(RTSAssist.stNames.engine)).
                                getContext().getInitialDeploymentBurnDuration() * 1250f);
                if (pointer.get("destSec") != null) {
                    ((Vector2f) pointer.get("destSec")).setY(((Vector2f) pointer.get("destSec")).getY() +
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
                e.clear();
            }

            @Override
            public boolean removeOnCompletion() { return(false); };
        });
    }

    private void postDeploymentInit () {
        ((RTS_ModeManager)this.getState(RTSAssist.stNames.modeManager)).firstFrameOfBattle();
        for (ShipAPI ship : ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getShips()){
            if (((RTS_SelectionListener)getState(RTSAssist.stNames.selectionListener)).dontSelect(ship))
                continue;
            ((RTS_AIInjector)getState(RTSAssist.stNames.AIInjector)).hookAI(ship);
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
            ((RTS_ModeManager)this.getState(RTSAssist.stNames.modeManager)).makePlayerShipAi();
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