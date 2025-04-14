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

import com.fs.starfarer.api.combat.*;
import data.scripts.plugins.Utils.*;
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

    public RTS_TaskManager(Object state) {
        super(state);
        HashMap<String, Object> init = new HashMap<String, Object>() {{
            put("assignments", new HashMap<String, Object>()); // stores assignments
            put("firstFrameInit", false); // flag for firstFrameInit
            put("deployment", null); // initialised in handleDeployment
            put("combatStarted", false); // flag for handleDepolyment
            put("dRestrict", -100000f); // Restricts where the player can place ships
            put("ShipLocAPI", null); // initilised in firstFrameInit
            put("RTSCollisionManager", null); // down (requires state)
            put("commandShuttle", null); // initilised in firstFrameInit
        }};
        init.put("RTSCollisionManager", new RTS_CollisionManager(this.returnState()));
        this.setState(init);
    }

    public void createAssignments (List<ShipAPI> selection, Vector2f coOrd, Vector2f coOrdSec, Boolean moveAndRelease) {
        if (selection == null) return;

        /* Get some important info */
        HashMap<String, Object> assignments = (HashMap<String, Object>)this.getState("assignments");
        HashMap<String, RTS_BlockSystemPlugin> blockedSystems =
                (HashMap<String, RTS_BlockSystemPlugin>)this.getState("blockedSystems");
        if ((boolean)this.getState("combatStarted"))
            for (ShipAPI ship: ((RTS_ShipLocAPI)this.getState("ShipLocAPI")).findEnemyShipNeighbours(coOrd, 100f))
                ship.getExactBounds().update(ship.getLocation(), ship.getFacing());
        List<ShipAPI> targetedEnemyHoldPrimary = CombatUtils.getShipsWithinRange(coOrd, 10f);
        ShipAPI primaryEnemy =
                targetedEnemyHoldPrimary.size() == 1
                && targetedEnemyHoldPrimary.get(0).getOriginalOwner() == 1
                ? targetedEnemyHoldPrimary.get(0)
                : null;
        if ((boolean)this.getState("combatStarted") && coOrdSec != null)
            for (ShipAPI ship: ((RTS_ShipLocAPI)this.getState("ShipLocAPI")).findEnemyShipNeighbours(coOrdSec, 100f))
                ship.getExactBounds().update(ship.getLocation(), ship.getFacing());
        List<ShipAPI> targetedEnemyHoldSecondary =
                coOrdSec != null
                ? CombatUtils.getShipsWithinRange(coOrdSec, 10f) : new ArrayList<ShipAPI>();
        ShipAPI secondaryEnemy =
                targetedEnemyHoldSecondary.size() == 1
                && targetedEnemyHoldSecondary.get(0).getOriginalOwner() == 1
                ? targetedEnemyHoldSecondary.get(0)
                : null;

        /* if we are in deployment restrict where the player can place ships and if they can target enemys */
        if (!(boolean)this.getState("combatStarted")
                && (primaryEnemy != null || secondaryEnemy != null))
            return;
        coOrd.setY(Math.min(coOrd.getY(), (float)this.getState("dRestrict")));
        if (coOrdSec != null)
            coOrdSec.setY(Math.min(coOrdSec.getY(), (float)this.getState("dRestrict")));

        /* main assignment loop */
        for (ShipAPI x: selection) {
            if (x.isPhased())
                continue;
            HashMap<String, Object> shipHold = new HashMap<>();
            shipHold.put("priorityEnemy", // If we assign this ship a new assignment, we want to see if it had a priority enemy and keep that
                    secondaryEnemy != null
                            && shipHold.get("targetEnemy") == null
                            ? secondaryEnemy
                            : assignments != null
                            && assignments.get(x.getId()) != null
                            && ((HashMap<String, Object>)assignments.get(x.getId())).get("priorityEnemy") != null
                            ? ((HashMap<String, Object>)assignments.get(x.getId())).get("priorityEnemy")
                            : null
            );
            if (assignments.containsKey(x.getId()) && assignments.get(x.getId()) != null)
                this.deleteAssignments(x);
            shipHold.put("shipTask", new RTS_ShipMoveToPosition(x, this.returnState()));
            shipHold.put("dest", coOrd);
            shipHold.put("modDest", null);
            shipHold.put("destSec",
                    secondaryEnemy != null
                    ? null
                    : coOrdSec
            );
            shipHold.put("ship", x);
            shipHold.put("blockedSystem",
                    !blockedSystems.containsKey(x.getSystem().getId())
                    ? null
                    : blockedSystems.get(x.getSystem().getId()) == null
                    ? new RTS_Block_alwaystrue_()
                    : blockedSystems.get(x.getSystem().getId())
            );
            shipHold.put("moveAndRelease", moveAndRelease);
            shipHold.put("weaponPosMods", getWeaponPosMods(x));
            shipHold.put("attackAngle",
                    primaryEnemy != null
                    && coOrdSec != null
                    ? new Vector2f(
                            VectorUtils.getAngle(coOrd, coOrdSec),
                            MathUtils.getDistance(coOrd, coOrdSec)
                    )
                    : null
            );
            shipHold.put("angledAttack", null);
            shipHold.put("targetEnemy",
                    primaryEnemy != null
                    ? targetedEnemyHoldPrimary.get(0)
                    : null
            );
            assignments.put(x.getId(), shipHold);
            this.setState("assignments", assignments);
        }
        Global.getSoundPlayer().playUISound(
                primaryEnemy != null ? "ui_channel_comm_local_04" :
                coOrdSec == null && !moveAndRelease ? "ui_channel_intel_assessment_01" :
                        !moveAndRelease ? "ui_channel_news_04" : "ui_channel_default_04",
                1f,
                0.3f
        );
    }

    /* Assignment creation utils */

    public void editPriorityEnemy(List<ShipAPI> selection, Vector2f coOrd, boolean addRemove) {
        if (!(boolean)this.getState("combatStarted"))
            return;
        List<ShipAPI> targetedEnemyHold = CombatUtils.getShipsWithinRange(coOrd, 5f);
        if (selection == null || selection.size() == 0 || (addRemove && (targetedEnemyHold.size() != 1
                || targetedEnemyHold.get(0).getOriginalOwner() != 1)))
            return;
        HashMap<String, Object> pointer;
        for (ShipAPI ship : selection) {
            if (((HashMap<String, Object>)this.getState("assignments")).containsKey(ship.getId())) {
                pointer = (HashMap<String, Object>)((HashMap<String, Object>)this.getState("assignments"))
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

    private HashMap<String, Object> getWeaponPosMods(ShipAPI ship) {
        HashMap<String, Object> hold = new HashMap<>();
        float posModFactor = ((HashMap<String, Object>)this.getState("broadsideData"))
                .containsKey(ship.getHullSpec().getBaseHullId())
                ? (float)FastTrig.cos(Math.abs((float)((HashMap<String, Object>)this.getState("broadsideData"))
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

    private boolean manageDeletions(HashMap<String, Object> assignment) {
        if ((((Boolean)assignment.get("moveAndRelease")
                && (ShipAPI)assignment.get("targetEnemy") == null)
                && MathUtils.getDistanceSquared(
                        (ShipAPI)assignment.get("ship"),
                        (Vector2f)assignment.get("dest")
                ) == 0f)
                || ((ShipAPI)assignment.get("ship")).isHulk()
                || (assignment.get("targetEnemy") != null && ((ShipAPI)assignment.get("targetEnemy")).isHulk())
                || ((boolean)((HashMap<String, Object>)this.getState("config")).get("shipsWillRun")
                && ((ShipAPI)assignment.get("ship")).getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.IN_CRITICAL_DPS_DANGER))
        ) {
            deleteAssignments((ShipAPI)assignment.get("ship"));
            return (true);
        }
        else
            return (false);
    }

    public void deleteAssignments (List<ShipAPI> selection) {
        if (selection == null) return;
        boolean flag = false;
        HashMap<String, Object> assignments = (HashMap<String, Object>)this.getState("assignments");
        HashMap<String, Object> pointer;
        for (ShipAPI x: selection) {
            if (assignments.get(x.getId()) != null) {
                pointer = ((HashMap<String, Object>)assignments.get(x.getId()));
                ((RTS_ShipMoveToPosition)pointer.get("shipTask")).cleanup(pointer);
                ((HashMap<String, Object>)pointer.get("weaponPosMods")).clear();
                pointer.clear();
                assignments.put(x.getId(), null);
                flag = true;
            }
        }
        if (flag) Global.getSoundPlayer().playUISound(
                "ui_channel_default_02", 1f, 0.3f
        );
        this.setState("assignments", assignments);
    }
    public void deleteAssignments(ShipAPI selection) { deleteAssignments(Arrays.asList(selection)); }

    public void translateAssignments (List<ShipAPI> selection, Vector2f grabPoint, Vector2f placePoint, boolean mockUp) {
        if (selection == null || grabPoint == null || placePoint == null) return;
        Vector2f translate = new Vector2f(
                placePoint.getX() - grabPoint.getX(),
                placePoint.getY() - grabPoint.getY()
        );
        HashMap<String, Object> pointer;
        if (mockUp) {
            for (ShipAPI x: selection) {
                pointer = (HashMap<String, Object>)this.getDeepState(Arrays.asList("assignments", x.getId()));
                if (pointer == null || (pointer != null && pointer.get("targetEnemy") != null))
                    continue;
                ((RTS_Draw)this.getState("draw")).drawPrimaryDest(
                        new Vector2f(
                                ((Vector2f)pointer.get("dest")).getX() + translate.getX(),
                                ((Vector2f)pointer.get("dest")).getY() + translate.getY()
                        ),
                        (float)this.getState("targetZoom"),
                        (boolean)pointer.get("moveAndRelease")
                );
            }
        }
        else {
            for (ShipAPI x : selection) {
                pointer = (HashMap<String, Object>)this.getDeepState(Arrays.asList("assignments", x.getId()));
                if (pointer == null || (pointer != null && pointer.get("targetEnemy") != null))
                    continue;
                ((Vector2f)pointer.get("dest")).setX(
                        ((Vector2f)pointer.get("dest")).getX() + translate.getX()
                );
                ((Vector2f)pointer.get("dest")).setY(Math.min(
                        ((Vector2f)pointer.get("dest")).getY() + translate.getY(),
                        (float)this.getState("dRestrict")
                ));
                if (pointer.get("destSec") != null) {
                    ((Vector2f)pointer.get("destSec")).setX(
                            ((Vector2f)pointer.get("destSec")).getX() + translate.getX()
                    );
                    ((Vector2f)pointer.get("destSec")).setY(Math.min(
                            ((Vector2f)pointer.get("destSec")).getY() + translate.getY(),
                            (float)this.getState("dRestrict")
                    ));
                }
                this.setDeepState(Arrays.asList("assignments", x.getId()), pointer);
            }
            Global.getSoundPlayer().playUISound(
                    "ui_channel_sniffer_01", 1f, 0.3f
            );
        }
    }

    public void rotateAssignments(List<ShipAPI> selection, Vector2f grabPoint, Vector2f placePoint, boolean mockUp) {
        if (selection == null || grabPoint == null || placePoint == null) return;
        HashMap<String, Object> pointer;
        Vector2f Vpointer = new Vector2f();
        Vector3f totals = new Vector3f(0, 0, 0);
        Vector2f pivotPoint = new Vector2f();
        for (ShipAPI x : selection) {
            pointer = (HashMap<String, Object>)this.getDeepState(Arrays.asList("assignments", x.getId()));
            if (pointer == null || (pointer != null && pointer.get("targetEnemy") != null))
                continue;
            totals.set(
                    ((Vector2f)pointer.get("dest")).getX() + totals.getX(),
                    ((Vector2f)pointer.get("dest")).getY() + totals.getY(),
                    totals.getZ() + 1
            );
        }
        pivotPoint.set(
                totals.getX() / totals.getZ(),
                totals.getY() / totals.getZ()
        );
        float angle = MathUtils.getShortestRotation(
                VectorUtils.getAngle(pivotPoint, grabPoint),
                VectorUtils.getAngle(pivotPoint, placePoint)
        );
        if (mockUp) {
            for (ShipAPI x : selection) {
                pointer = (HashMap<String, Object>)this.getDeepState(Arrays.asList("assignments", x.getId()));
                if (pointer == null || (pointer != null && pointer.get("targetEnemy") != null))
                    continue;
                Vpointer.set(
                        ((Vector2f)pointer.get("dest")).getX(),
                        ((Vector2f)pointer.get("dest")).getY()
                );
                VectorUtils.rotateAroundPivot(
                        Vpointer,
                        pivotPoint,
                        angle
                );
                Vpointer.setY(Math.min(
                        Vpointer.getY(),
                        (float)this.getState("dRestrict")
                ));
                ((RTS_Draw)this.getState("draw")).drawPrimaryDest(
                        Vpointer,
                        (float)this.getState("targetZoom"),
                        (boolean)pointer.get("moveAndRelease")
                );
            }
        }
        else {
            for (ShipAPI x : selection) {
                pointer = (HashMap<String, Object>)this.getDeepState(Arrays.asList("assignments", x.getId()));
                if (pointer == null || (pointer != null && pointer.get("targetEnemy") != null))
                    continue;
                VectorUtils.rotateAroundPivot(
                        (Vector2f)pointer.get("dest"),
                        pivotPoint,
                        angle
                );
                ((Vector2f)pointer.get("dest")).setY(Math.min(
                        ((Vector2f) pointer.get("dest")).getY(),
                        (float) this.getState("dRestrict")
                ));
                if (pointer.get("destSec") != null) {
                    VectorUtils.rotateAroundPivot(
                            (Vector2f) pointer.get("destSec"),
                            pivotPoint,
                            angle
                    );
                    ((Vector2f) pointer.get("destSec")).setY(Math.min(
                            ((Vector2f) pointer.get("destSec")).getY(),
                            (float) this.getState("dRestrict")
                    ));
                }
                this.setDeepState(Arrays.asList("assignments", x.getId()), pointer);
            }
            Global.getSoundPlayer().playUISound(
                    "survey_result_01", 1f, 0.3f
            );
        }
    }

    public void saveAssignments() {
        if  (Global.getSector().getMemory().get("$RTSASSIST_saveData") == null)
            return;
        HashMap<String, Object> mem = (HashMap<String, Object>)Global.getSector().getMemory().get("$RTSASSIST_saveData");
        Vector2f min = new Vector2f(100000f, 100000f);
        Vector2f max = new Vector2f(-100000f,-100000f);
        Vector2f mid;
        HashMap<String, Object> hold;
        for (Map.Entry<String, Object> x: ((HashMap<String, Object>)this.getState("assignments")).entrySet()) {
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
        for (Map.Entry<String, Object> x: ((HashMap<String, Object>)this.getState("assignments")).entrySet()) {
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

        HashMap<Integer, List<ShipAPI>> controlGroups = (HashMap<Integer, List<ShipAPI>>)this.getState("controlGroups");
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
        for (CombatFleetManagerAPI.AssignmentInfo unsavableTask : ((CombatEngineAPI)this.getState("engine"))
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

    public void loadAssignments(Vector2f worldSpace) {
        if (Global.getSector().getMemory().get("$RTSASSIST_saveData") == null)
            return;

        if (((HashMap<String, Object>)Global.getSector().getMemory().get("$RTSASSIST_saveData"))
                .get("positions") != null) {
            List<HashMap<String, Object>> positions = (List<HashMap<String, Object>>)((HashMap<String, Object>)Global
                    .getSector().getMemory().get("$RTSASSIST_saveData")).get("positions");
            ShipAPI shipHold;
            for (HashMap<String, Object> x : positions) {
                shipHold = ((CombatEngineAPI)this.getState("engine")).
                        getFleetManager(0).getShipFor((FleetMemberAPI)x.get("ship"));
                if (shipHold == null) continue;
                this.createAssignments(
                        Arrays.asList(shipHold),
                        new Vector2f(
                                worldSpace.getX() + ((Vector2f)x.get("relativeCoOrd")).getX(),
                                worldSpace.getY() + ((Vector2f)x.get("relativeCoOrd")).getY()
                        ),
                        x.get("relativeCoOrdSec") == null ? null : new Vector2f(
                                worldSpace.getX() + ((Vector2f)x.get("relativeCoOrdSec")).getX(),
                                worldSpace.getY() + ((Vector2f)x.get("relativeCoOrdSec")).getY()
                        ),
                        (boolean)x.get("moveAndRelease")
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
                    if (((CombatEngineAPI)this.getState("engine")).getFleetManager(0).getShipFor(fleetShip) == null)
                        continue;
                    if (!controlGroups.containsKey(group.getKey()))
                        controlGroups.put(group.getKey(), new ArrayList<>());
                    controlGroups.get(group.getKey()).add(((CombatEngineAPI)this.getState("engine"))
                            .getFleetManager(0).getShipFor(fleetShip));
                }
            }
            if (this.getState("controlGroups") != null)
                ((HashMap<?, ?>)this.getState("controlGroups")).clear();
            this.setState("controlGroups", controlGroups);
        }

        if (((HashMap<String, Object>)Global.getSector().getMemory().get("$RTSASSIST_saveData"))
                .get("tasks") != null) {
            List<HashMap<String, Object>> tasks = (List<HashMap<String, Object>>)((HashMap<String, Object>)Global.getSector().
                    getMemory().get("$RTSASSIST_saveData")).get("tasks");
            CombatFleetManagerAPI fleetApi = ((CombatEngineAPI)this.getState("engine")).getFleetManager(0);
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
            for (ShipAPI x : ((CombatEngineAPI)this.getState("engine")).getShips()) {
                if (x.isFighter() || x.getOriginalOwner() == 1 || x.isHulk() || x.isStation() || x.isAlly()) continue;
                if (!shipsWithOrders.contains(x.getFleetMember()))
                    fleetApi.getTaskManager(false).orderSearchAndDestroy(fleetApi.getDeployedFleetMember(x), false);
            }

        }
        Global.getSoundPlayer().playUISound(
                "ui_channel_comm_local_01", 1f, 0.3f
        );
    }

    /* Draw Stuff */

    private void drawDeploymentLine() {
        ((RTS_Draw)this.getState("draw")).rudeDrawLine(
                new Vector2f(
                        ((CombatEngineAPI)this.getState("engine")).getMapWidth() / -2,
                        CombatUtils.toScreenCoordinates(new Vector2f(
                                0,
                                (float)this.getState("dRestrict")
                        )).getY()
                ),
                new Vector2f(
                        ((CombatEngineAPI)this.getState("engine")).getMapWidth() / 2,
                        CombatUtils.toScreenCoordinates(new Vector2f(
                                0,
                                (float)this.getState("dRestrict")
                        )).getY()
                )
        );
    }

    private void drawAssignments(HashMap<String, Object> assignment) {
        RTS_Draw Draw = ((RTS_Draw)this.getState("draw"));
        if (assignment.get("targetEnemy") == null)
            Draw.drawPrimaryDest(
                    (Vector2f)assignment.get("dest"),
                    (float)this.getState("targetZoom"),
                    (boolean)assignment.get("moveAndRelease")
            );
        if (assignment.get("targetEnemy") != null || assignment.get("priorityEnemy") != null) {
            Draw.drawSelectedEnemy(
                    assignment.get("targetEnemy") == null
                            ? ((ShipAPI)assignment.get("priorityEnemy")).getLocation()
                            : ((ShipAPI)assignment.get("targetEnemy")).getLocation(),
                    (float)this.getState("targetZoom"),
                    (boolean)assignment.get("moveAndRelease")
            );
            if (assignment.get("angledAttack") != null) {
                Draw.drawSecondaryDest(
                        (Vector2f)assignment.get("angledAttack"),
                        (float)this.getState("targetZoom")
                );
            }
            else if (assignment.get("modDest") != null)
                Draw.drawSecondaryDest(
                        (Vector2f)assignment.get("modDest"),
                        (float)this.getState("targetZoom")
                );
        }
        if (assignment.get("destSec") != null && assignment.get("targetEnemy") == null)
            Draw.drawSecondaryDest(
                    (Vector2f)assignment.get("destSec"),
                    (float)this.getState("targetZoom")
            );
    }

    /* Combat Initilisation */

    public void handleDeployment(float elapsedTime) {
        if ((boolean)this.getState("combatStarted"))
            return;
        if (((CombatEngineAPI)this.getState("engine")).isSimulation() ||
                (((CombatEngineAPI)this.getState("engine")).getContext().getPlayerGoal() != null &&
                        ((CombatEngineAPI)this.getState("engine")).getContext().getPlayerGoal().name().contains("ESCAPE"))) {
            ((RTS_ModeManager)this.getState("modeManager")).firstFrameOfBattle();
            this.setState("combatStarted", true);
            this.setState("dRestrict", 100000f);
            return;
        }
        if (this.getState("deployment") == null && elapsedTime > 0f) {
            ((RTS_ModeManager)this.getState("modeManager")).firstFrameOfBattle();
            int flag = 0;
            for (ShipAPI ship : ((CombatEngineAPI)this.getState("engine")).getShips())
                if (ship.isStation() && ship.getOriginalOwner() == 0)
                    flag = 1;
            if (flag == 0) {
                for (ShipAPI x : ((CombatEngineAPI) this.getState("engine")).getShips()) {
                    if (x.isFighter() || x.getOriginalOwner() == 1) continue;
                    this.setState("dRestrict",
                            Math.max((float) this.getState("dRestrict"), x.getLocation().getY()));
                }
                this.setState("dRestrict", (float) this.getState("dRestrict") + 500f);
            }
            else
                this.setState("dRestrict", -3500f);
            this.setState("deployment", elapsedTime);
            ((CombatEngineAPI)this.getState("engine")).setPaused(true);
        }
        else if (this.getState("deployment") != null &&
                (float)this.getState("deployment") != elapsedTime) {
            this.setState("combatStarted", true);
            this.setState("dRestrict", 100000f);
            HashMap<String, Object> pointer;
            for (Map.Entry<String, Object> x : ((HashMap<String, Object>) this.getState("assignments")).entrySet()) {
                if (x.getValue() == null) continue;
                pointer = (HashMap<String, Object>) x.getValue();
                ((Vector2f) pointer.get("dest")).setY(((Vector2f) pointer.get("dest")).getY() +
                        ((CombatEngineAPI) this.getState("engine")).
                                getContext().getInitialDeploymentBurnDuration() * 1250f);
                if (pointer.get("destSec") != null) {
                    ((Vector2f) pointer.get("destSec")).setY(((Vector2f) pointer.get("destSec")).getY() +
                            ((CombatEngineAPI) this.getState("engine")).
                                    getContext().getInitialDeploymentBurnDuration() * 1250f);
                }
            }
        }
    }

    private void firstFrameInit() {
        if (!(boolean)this.getState("firstFrameInit")) {
            if (((CombatEngineAPI)this.getState("engine")).getPlayerShip().getName().equals("Command Shuttle"))
                this.setState("commandShuttle", ((CombatEngineAPI)this.getState("engine")).getPlayerShip());
            this.setState("firstFrameInit", true);
            this.setState("ShipLocAPI", new RTS_ShipLocAPI(this.returnState()));
//            ((RTS_SelectionListener)this.getState("selectionListener")).listenForDeadShips();
        }
    }

    /* The actual Doing, called in RTSAssist->Advance */

    public void update(float elapsedTime) {
        this.firstFrameInit();
        this.handleDeployment(elapsedTime);

        /* Update the pathing infrastructure */
        if ((boolean)this.getState("combatStarted")) {
            ((RTS_ShipLocAPI)this.getState("ShipLocAPI")).update();
            ((RTS_CollisionManager)this.getState("RTSCollisionManager")).update();
            ((RTS_ModeManager)this.getState("modeManager")).makePlayerShipAi();
            ((RTS_ModeManager)this.getState("modeManager")).playerSwitchesToDifferentShip();
        }

        /* If battle hasnt started, draw the deployment line */
        if (this.getState("deployment") != null && !(boolean)this.getState("combatStarted"))
            this.drawDeploymentLine();

        /* Iterate through the current assignments */
        HashMap<String, Object> pointer;
        for (Map.Entry<String, Object> x: ((HashMap<String, Object>)this.getState("assignments")).entrySet()) {
            pointer = (HashMap<String, Object>)x.getValue();
            if (pointer == null) continue;

            /* * If battle has'nt started, instantly move ships.
               * Check if an assignment should be deleted, if so delete it and move on, if not process assignment
                 and continue. */
            if (!(boolean)this.getState("combatStarted"))
                ((ShipAPI)pointer.get("ship")).getLocation().set((Vector2f)pointer.get("dest"));
            else if (!this.manageDeletions(pointer))
                ((RTS_ShipMoveToPosition)pointer.get("shipTask")).moveToPosition(pointer,true);
            else
                continue;

            /* If the ship has a priority enemy, if its dead remove that priority enemy */
            if (pointer.get("priorityEnemy") != null && ((ShipAPI)pointer.get("priorityEnemy")).isHulk())
                pointer.put("priorityEnemy", null);

            /* Check if we should draw this ships assignments, then draw them. */
            if ((boolean)this.getState("isAltDown")
                    || (this.getState("currentSelection") != null
                            && ((List<ShipAPI>)this.getState("currentSelection")).contains(pointer.get("ship")))
            )
                this.drawAssignments(pointer);
        }
    }
}