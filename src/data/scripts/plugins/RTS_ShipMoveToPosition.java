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

import java.util.Arrays;
import java.util.HashMap;
import data.scripts.plugins.AISystems.RTS_AIInjectedEvent;
import data.scripts.plugins.AISystems.RTS_AIInjector;
import data.scripts.plugins.Utils.*;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import java.awt.geom.Line2D;
import java.util.List;

import com.fs.starfarer.api.combat.*;

import org.lazywizard.lazylib.*;

import data.scripts.plugins.BlockedSystems.RTS_BlockSystemPlugin;

public class RTS_ShipMoveToPosition extends RTS_StatefulClasses {

    public class classidentifiers {
        public String ship  = RTS_StatefulClasses.getUniqueIdentifier();
        public String thrust  = RTS_StatefulClasses.getUniqueIdentifier();
        public String rotation  = RTS_StatefulClasses.getUniqueIdentifier();
        public String vData  = RTS_StatefulClasses.getUniqueIdentifier();
        public String shipHullStrafeMod  = RTS_StatefulClasses.getUniqueIdentifier();
        public String closestEnemy = RTS_StatefulClasses.getUniqueIdentifier();
        public String AIHookHold = RTS_StatefulClasses.getUniqueIdentifier();
        public String pathing = RTS_StatefulClasses.getUniqueIdentifier();
        public String context = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public classidentifiers stNames = new classidentifiers();

    public class vDataIdentifiers {
        public String maxSpeed = RTS_StatefulClasses.getUniqueIdentifier();
        public String distanceToPoint = RTS_StatefulClasses.getUniqueIdentifier();
        public String currentBearing = RTS_StatefulClasses.getUniqueIdentifier();
        public String relativeVelocity = RTS_StatefulClasses.getUniqueIdentifier();
        public String relativeTargetVector = RTS_StatefulClasses.getUniqueIdentifier();
        public String adjMaxSpeed = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public vDataIdentifiers vDNames = new vDataIdentifiers();

    public class thrustIdentifiers {
        public String x = RTS_StatefulClasses.getUniqueIdentifier();
        public String y = RTS_StatefulClasses.getUniqueIdentifier();
    }
    public thrustIdentifiers thNames = new thrustIdentifiers();

    public RTS_ShipMoveToPosition (final ShipAPI shipInstance, Object state) {
        super(state, true);
        final HashMap<String, Float> shipHullStrafeMod = new HashMap<String, Float>(){{
            put("FRIGATE", 1f);
            put("DESTROYER", 0.75f);
            put("CRUISER", 0.5f);
            put("CAPITAL_SHIP", 0.25f);
        }};
        HashMap<String, Object> init = new HashMap<String, Object>() {{
            put(stNames.ship, shipInstance);
            put(stNames.thrust, new HashMap<String, Integer>());
            put(stNames.rotation, 0);
            put(stNames.vData, new HashMap<String, Object>());
            put(stNames.shipHullStrafeMod, shipHullStrafeMod);
            put(stNames.closestEnemy, null);
            put(stNames.AIHookHold, null);
            put(stNames.pathing, null);
            put(stNames.context, null);
        }};
        init.put(this.stNames.pathing, new RTS_Pathing(this.returnState(true)));
        this.setState(init);
        init.clear();
    }

    public void cleanup (HashMap<String, Object> assignment) {
        if (assignment.get("blockedSystem") != null && this.getState(this.stNames.context) != null)
            ((RTS_BlockSystemPlugin)assignment.get("blockedSystem")).execCleanUp(
                    (RTS_Context)this.getState(this.stNames.context)
            );
        ((HashMap<String, Object>)this.getState(this.stNames.thrust)).clear();
        ((HashMap<String, Object>)this.getState(this.stNames.vData)).clear();
        ((HashMap<String, Object>)this.getState(this.stNames.shipHullStrafeMod)).clear();
        ((RTS_AIInjector)this.getState(RTSAssist.stNames.AIInjector, true)).removeInjection(
                (ShipAPI)this.getState(this.stNames.ship),
                (String)this.getState(this.stNames.AIHookHold),
                RTS_AIInjector.eventType.PREADVANCE
        );
        ((RTS_Pathing)this.getState(stNames.pathing)).cleanUp();
        this.cleanState();
    }

    public void moveToPosition (HashMap<String, Object> assignment, Boolean update) {
//        if (((ShipAPI)assignment.get("ship")).getShipAI().getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) != null)
//            System.out.println(((ShipAPI)((ShipAPI)assignment.get("ship")).getShipAI().getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET)).getName());

        if (this.getState(this.stNames.ship) != null) {
            if (this.getState(this.stNames.context) == null)
                this.setState(
                        this.stNames.context,
                        new RTS_Context(
                                this.returnState(true),
                                this.returnState(),
                                assignment)
                );
            if (assignment.get("targetEnemy") != null
                    || assignment.get("priorityEnemy") != null
                    || this.getState(this.stNames.closestEnemy) != null
            ) {
                ShipAPI enemy = assignment.get("targetEnemy") != null
                        ? (ShipAPI)assignment.get("targetEnemy")
                        : (ShipAPI)assignment.get("priorityEnemy") != null
                        ? (ShipAPI)assignment.get("priorityEnemy")
                        : (ShipAPI)this.getState(this.stNames.closestEnemy);
                ((ShipAPI)assignment.get("ship")).setShipTarget(enemy);
                if (((ShipAPI)assignment.get("ship")).getShipAI() != null)
                    ((ShipAPI)assignment.get("ship")).getShipAI().setTargetOverride(enemy);
            }
            this.setState(this.stNames.closestEnemy, null);
            if (update) this.update(
                    assignment,
                    (ShipAPI)this.getState(this.stNames.ship),
                    assignment.get("targetEnemy") != null
                            ? assignment.get("attackAngle") != null
                            ? findAngledAttackCoOrd(
                                    (ShipAPI)assignment.get("targetEnemy"),
                                    (Vector2f)assignment.get("attackAngle"),
                                    assignment
                            )
                            : findAttackCoOrd(
                                    (ShipAPI)assignment.get("ship"),
                                    (ShipAPI)assignment.get("targetEnemy"),
                                    (Boolean)assignment.get("moveAndRelease"),
                                    assignment
                            )
                            : assignment.get("destSec") == null
                            ? (Vector2f)assignment.get("dest")
                            : setRetreatCoOrd(
                                    (ShipAPI)assignment.get("ship"),
                                    (Vector2f)assignment.get("dest"),
                                    (Vector2f)assignment.get("destSec")
                            ),
                    assignment.get("targetEnemy") != null ? (ShipAPI)assignment.get("targetEnemy") : null,
                    assignment.get("priorityEnemy") != null ? (ShipAPI)assignment.get("priorityEnemy") : null,
                    ((HashMap<String, RTS_MoveTogetherManager>)this.getState(RTS_TaskManager.stNames.togaStore, true))
                            .get(((ShipAPI)this.getState(this.stNames.ship)).getId())
            );
            if (this.getState(this.stNames.AIHookHold) == null) {
                RTS_AIInjectedEvent injection = new RTS_AIInjectedEvent() {
                    @Override
                    public boolean shouldExecute() {
                        return (getState(stNames.thrust) != null);
                    }

                    @Override
                    public void run() {
                        applyThrust(
                                (ShipAPI)getState(stNames.ship),
                                (HashMap<String, Integer>)getState(stNames.thrust)
                        );
                        applyRotation(
                                (ShipAPI)getState(stNames.ship),
                                (Vector2f)assignment.get("modDest"),
                                (int)getState(stNames.rotation),
                                (assignment.get("targetEnemy") != null
                                        || assignment.get("priorityEnemy") != null
                                        || getState(stNames.closestEnemy) != null)
                        );
                        blockSystem(
                                (RTS_BlockSystemPlugin)assignment.get("blockedSystem"),
                                assignment
                        );
                    }

                    @Override
                    public boolean removeOnCompletion() {
                        return (false);
                    }
                };
                injection.run();
                String hookHold = ((RTS_AIInjector)this.getState(RTSAssist.stNames.AIInjector, true)).registerInjection(
                        (ShipAPI)this.getState(this.stNames.ship),
                        RTS_AIInjector.eventType.PREADVANCE,
                        injection
                );
                this.setState(this.stNames.AIHookHold, hookHold);
            }
        }
    }

    public void update (
            HashMap<String, Object> assignment,
            ShipAPI ship,
            Vector2f coOrd,
            ShipAPI targetEnemy,
            ShipAPI priorityEnemy,
            RTS_MoveTogetherManager togaInstance
    ) {
        if ((RTS_TaskManager.assType)assignment.get("assType") == RTS_TaskManager.assType.attackMove) {
            coOrd = this.findClosestEnemy(ship, coOrd, assignment);
        }
        else if (togaInstance != null)
            coOrd = togaInstance.returnNewCoOrd(ship, coOrd, assignment);
        coOrd = ((RTS_Pathing)(this.getState(stNames.pathing))).adjustDestCoord(ship, coOrd, targetEnemy);

        assignment.put("modDest", coOrd);
        HashMap<String, Object> hold = (HashMap<String, Object>)this.getState(this.stNames.vData);
        hold.put(this.vDNames.maxSpeed, ship.getMaxSpeed());
        hold.put(this.vDNames.distanceToPoint, RTS_AssortedFunctions.fastGetDistance(ship.getLocation(), coOrd));
        hold.put(this.vDNames.currentBearing, ship.getFacing());
        hold.put(this.vDNames.relativeVelocity, new Vector2f(
                ship.getVelocity().getX(),
                ship.getVelocity().getY()
        ));
        VectorUtils.rotate(
                (Vector2f)hold.get(this.vDNames.relativeVelocity),
                MathUtils.clampAngle(-1 * ((float)hold.get(this.vDNames.currentBearing) - 90))
        );
        hold.put(this.vDNames.relativeTargetVector, VectorUtils.getDirectionalVector(ship.getLocation(), coOrd));
        VectorUtils.rotate(
                (Vector2f)hold.get(this.vDNames.relativeTargetVector),
                MathUtils.clampAngle(-1 * ((float)hold.get(this.vDNames.currentBearing) - 90))
        );
        hold.put(
                this.vDNames.adjMaxSpeed,
                this.adjustMaxSpeed(
                        (Vector2f)hold.get(this.vDNames.relativeTargetVector),
                        (float)hold.get(this.vDNames.maxSpeed),
                        (float)((Integer)hold.get(this.vDNames.distanceToPoint)),
                        ship,
                        targetEnemy == null ? null : targetEnemy
                )

        );
        VectorUtils.resize(
                (Vector2f)hold.get(this.vDNames.relativeTargetVector),
                (float)hold.get(this.vDNames.adjMaxSpeed)
        );
        Vector2f target;
        if (targetEnemy != null)
            target = targetEnemy.getLocation();
        else if (priorityEnemy != null)
            target = priorityEnemy.getLocation();
        else if (this.getState(this.stNames.closestEnemy) != null)
            target = ((ShipAPI)this.getState(this.stNames.closestEnemy)).getLocation();
        else
            target = coOrd;
        this.getRotation(
                ship.getFacing(),
                (VectorUtils.getAngle(
                        ship.getLocation(),
                        target
                        ) + (float)((((HashMap<String, Object>)this.getState(RTSAssist.stNames.broadsideData, true)).
                                        containsKey(ship.getHullSpec().getBaseHullId())
                                && (targetEnemy != null || priorityEnemy != null))
                                ? (((HashMap<String, Object>)this.getState(RTSAssist.stNames.broadsideData, true)).
                                        get(ship.getHullSpec().getBaseHullId()))
                                : 0f
                )),
                ship.getAngularVelocity(),
                assignment
        );
        this.setState(this.stNames.vData, hold);
        this.getThrust(
                (Vector2f)this.getDeepState(Arrays.asList(this.stNames.vData, this.vDNames.relativeVelocity)),
                (Vector2f)this.getDeepState(Arrays.asList(this.stNames.vData, this.vDNames.relativeTargetVector))
        );
    }

    private Vector2f findClosestEnemy(ShipAPI ship, Vector2f coOrd, HashMap<String, Object> assignment) {
        float attackRange = this.getWeaponRanges(ship, (HashMap<String, Object>)assignment.get("weaponPosMods")).getY();
        List<ShipAPI> targets = ((RTS_ShipLocAPI)this.getState(RTS_TaskManager.stNames.ShipLocAPI, true)).findEnemyShipNeighbours(
                ship,
                MathUtils.clamp(attackRange * 1.5f, 1000f, 1000000f),
                true
        );
        ShipAPI closest = null;
        float dHold = 0f;
        float dTHold = 0f;
        for (ShipAPI targetShip : targets) {
            if (closest == null) {
                closest = targetShip;
                dHold = (float)RTS_AssortedFunctions.getDistanceSquared(ship.getLocation(), closest.getLocation());
                continue;
            }
            dTHold = (float)RTS_AssortedFunctions.getDistanceSquared(ship.getLocation(), targetShip.getLocation());
            if (dHold > dTHold) {
                closest = targetShip;
                dHold = dTHold;
            }
        }
        if (closest == null)
            return (coOrd);
        this.setState(this.stNames.closestEnemy, closest);

        closest.getExactBounds().update(closest.getLocation(), closest.getFacing());
        Vector2f D2CHold = CollisionUtils.getCollisionPoint(
                ship.getLocation(),
                closest.getLocation(),
                closest
        );
        float targetCollsionD2C = D2CHold == null ? 0 : RTS_AssortedFunctions.fastGetDistance(
                D2CHold,
                closest.getLocation()
        );
        float velModifier = 0.2f * MathUtils.clamp(
                closest.getVelocity().length(),
                0f, 100f
        ) / 100f;
        velModifier = velModifier / MathUtils.clamp(
                ship.getAcceleration() / (closest.getAcceleration() / 2),
                1f, 5f
        );
        attackRange = (attackRange + (targetCollsionD2C -5f));
        float finalAttackRange = attackRange;
        ShipAPI finalClosest = closest;
        float distance =  RTS_AssortedFunctions.lastResortSolver(
                10000f,
                new RTS_AssortedFunctions.LRSTest() {
                    @Override
                    public int test(float test) {
                        Vector2f testHold = VectorUtils.resize(VectorUtils.getDirectionalVector(coOrd, ship.getLocation()), test);
                        Vector2f testCoOrd = new Vector2f(
                                coOrd.getX() + testHold.getX(),
                                coOrd.getY() + testHold.getY()
                        );
                        float dHold = (float)(Math.pow(finalAttackRange, 2) - RTS_AssortedFunctions.getDistanceSquared(
                                finalClosest.getLocation(),
                                testCoOrd
                        ));
                        return (dHold == 0
                                ? 0
                                : dHold > 0
                                ? 1
                                : -1
                        );
                    }
                },
                15,
                0
        );
        Vector2f vAdjust = VectorUtils.resize(
                VectorUtils.getDirectionalVector(coOrd, ship.getLocation()),
                Math.min(distance, (float)((Integer)assignment.get("iniDisToAssignment")))
        );
        return (new Vector2f(
                coOrd.getX() + vAdjust.getX(),
                coOrd.getY() + vAdjust.getY()
        ));
    }

    private Vector2f setRetreatCoOrd (ShipAPI ship, Vector2f coOrd, Vector2f coOrdSec) {
        float fluxPointer;
        fluxPointer = ship.getFluxLevel() < 0.7f ? ship.getHardFluxLevel() :
                ship.getFluxLevel();
        float ratio = ship.getFluxTracker().isOverloaded() ? 1f : fluxPointer / 0.8f;
        return(new Vector2f(
                coOrd.getX() + ((coOrdSec.getX() - coOrd.getX()) * ratio),
                coOrd.getY() + ((coOrdSec.getY() - coOrd.getY()) * ratio)
        ));
    }

    private Vector2f findAngledAttackCoOrd (ShipAPI target, Vector2f attackAngle, HashMap<String, Object> assignment) {
        Vector2f weaponRange = this.getWeaponRanges((ShipAPI)assignment.get("ship"), (HashMap<String, Object>)assignment.get("weaponPosMods"));
        target.getExactBounds().update(target.getLocation(), target.getFacing());
        Vector2f D2CHold = CollisionUtils.getCollisionPoint(
                ((ShipAPI)assignment.get("ship")).getLocation(),
                target.getLocation(),
                target
        );
        float targetCollsionD2C = D2CHold == null ? 0 : MathUtils.getDistance(
                D2CHold,
                target.getLocation()
        );
        float velModifier = 0.1f * MathUtils.clamp(
                target.getVelocity().length(),
                0f, 100f
        ) / 100f;
        velModifier = velModifier / MathUtils.clamp(
                ((ShipAPI)assignment.get("ship")).getAcceleration() / (target.getAcceleration() / 2),
                1f, 5f
        );
        velModifier = 1f - velModifier;
        Vector2f hold = new Vector2f(
                MathUtils.clamp(attackAngle.getY(),
                        ((ShipAPI)assignment.get("ship")).getShieldRadiusEvenIfNoShield()
                                + target.getShieldRadiusEvenIfNoShield() + 5f,
                        (weaponRange.getX() + targetCollsionD2C - 5f) * velModifier
                ),
                0
        );
        hold = VectorUtils.rotate(hold, attackAngle.getX());
        Vector2f finalCoOrd = new Vector2f(
                target.getLocation().getX() + hold.getX(),
                target.getLocation().getY() + hold.getY()
        );
        assignment.put("angledAttack", finalCoOrd);
        return(finalCoOrd);
    }

    private Vector2f findAttackCoOrd (ShipAPI ship, ShipAPI target, Boolean isAgressive, HashMap<String, Object> assignment) {
        Vector2f weaponRange = this.getWeaponRanges(ship, (HashMap<String, Object>)assignment.get("weaponPosMods"));
        target.getExactBounds().update(target.getLocation(), target.getFacing());
        Vector2f D2CHold = CollisionUtils.getCollisionPoint(
                ship.getLocation(),
                target.getLocation(),
                target
        );
        float targetCollsionD2C = D2CHold == null ? 0 : RTS_AssortedFunctions.fastGetDistance(
                D2CHold,
                target.getLocation()
        );
        float velModifier = 0.2f * MathUtils.clamp(
                target.getVelocity().length(),
                0f, 100f
        ) / 100f;
        velModifier = velModifier / MathUtils.clamp(
                ship.getAcceleration() / (target.getAcceleration() / 2),
                1f, 5f
        );
        velModifier = 1f - velModifier;
        Vector2f hold = VectorUtils.getDirectionalVector(target.getLocation(), ship.getLocation());
        hold = VectorUtils.resize(
                hold,
                ((isAgressive
                        ? weaponRange.getY()
                        : weaponRange.getX())
                        + targetCollsionD2C -5f)
                        * velModifier
        );
        return(new Vector2f(
                (target.getLocation().getX() + hold.getX()),
                (target.getLocation().getY() + hold.getY())
        ));
    }

    private void drawBounds (ShipAPI ship) {
        RTS_Draw draw = ((RTS_Draw)this.getState(RTSAssist.stNames.draw, true));
        for (BoundsAPI.SegmentAPI seg : ship.getExactBounds().getSegments()) {
            draw.rudeDrawLine(CombatUtils.toScreenCoordinates(seg.getP1()), CombatUtils.toScreenCoordinates(seg.getP2()));
        }
    }

    private void getCollPoint (ShipAPI ship, Vector2f lnStart, Vector2f lnEnd) {
        float flag = 0;
        Line2D cast = new Line2D.Float();
        cast.setLine(
                lnStart.getX(),
                lnStart.getY(),
                lnEnd.getX(),
                lnEnd.getY()
        );
        Line2D hold =  new Line2D.Float();
        Vector2f intersection = null;
        for (BoundsAPI.SegmentAPI seg : ship.getExactBounds().getSegments()) {
            hold.setLine(
                    seg.getP1().getX(),
                    seg.getP1().getY(),
                    seg.getP2().getX(),
                    seg.getP2().getY()
            );
            if (cast.intersectsLine(hold))
                flag = 1;
        }
        if (flag == 1)
            System.out.println("intersect");
        else
            System.out.println("nope");
    }

    private Vector2f getWeaponRanges(ShipAPI ship, HashMap<String, Object> weaponPosMods) {
        Vector2f hold = new Vector2f(
                0f,
                1000000f
        );
        float posModPointer;
        float flag = 0;
        while (flag < 2) {
            for (WeaponAPI set : ship.getUsableWeapons()) {
                posModPointer = (float)weaponPosMods.get(set.getId());
                if (flag == 0 &&
                        (set.getType().compareTo(WeaponAPI.WeaponType.MISSILE) == 0
                                || set.getOriginalSpec().getPrimaryRoleStr() == null
                                || set.getOriginalSpec().getPrimaryRoleStr().equals("Point Defense")
                                || set.getOriginalSpec().getPrimaryRoleStr().equals("Point Defense (Area)"))
                )
                    continue;
                if (set.getRange() - posModPointer > hold.getX())
                    hold.setX(set.getRange() - posModPointer);
                if (set.getRange() - posModPointer < hold.getY())
                    hold.setY(set.getRange() - posModPointer);
            }
            flag = flag == 1f
                    ? 2f
                    : hold.getY() == 1000000f
                    ? 1f
                    : 2f;
        }
        return (hold);
    }

    private Vector2f getDestFromRatio (Vector2f coOrd, Vector2f coOrdSec, float ratio) {
        return(new Vector2f(
                coOrd.getX() + ((coOrdSec.getX() - coOrd.getX()) * ratio),
                coOrd.getY() + ((coOrdSec.getY() - coOrd.getY()) * ratio)
        ));
    }

    private float adjustMaxSpeed (Vector2f relativeTargetVector, float maxSpeed, float distanceToPoint, ShipAPI ship, ShipAPI targetEnemy) {
        float forceDiff = new Vector2f(
                ((HashMap<String, Float>)this.getState(this.stNames.shipHullStrafeMod)).get(ship.getHullSize().name())
                        * relativeTargetVector.getX(),
                relativeTargetVector.getY() <= 0f ? relativeTargetVector.getY() : relativeTargetVector.getY() * 0.75f
        ).length();
        float resultantSpeed = ((distanceToPoint / (maxSpeed / ship.getAcceleration())) * forceDiff) >= (maxSpeed * 0.5f) ? maxSpeed :
                distanceToPoint < 1f ? 1f : maxSpeed * (((distanceToPoint / (maxSpeed / ship.getAcceleration())) * forceDiff) / (maxSpeed * 0.5f));
        return (targetEnemy == null
                ? resultantSpeed
                : (Math.abs(MathUtils.getShortestRotation(
                        VectorUtils.getFacing(ship.getVelocity()),
                        VectorUtils.getFacing(targetEnemy.getVelocity())
                )) > 90f
                ? resultantSpeed
                : resultantSpeed + targetEnemy.getVelocity().length()
        ));
    }

    private void getThrust (Vector2f relativeVelocity, Vector2f relativeTargetVector) {
        HashMap<String, Integer> thrust = (HashMap<String, Integer>)this.getState(this.stNames.thrust);
        thrust.clear();
        thrust.put(this.thNames.x, 0);
        thrust.put(this.thNames.y, 0);
        if (relativeTargetVector.getX() > 0 && relativeVelocity.getX() < relativeTargetVector.getX())
            thrust.put(this.thNames.x, 1);
        else if (relativeTargetVector.getX() > 0 && relativeVelocity.getX() > relativeTargetVector.getX())
            thrust.put(this.thNames.x, -1);
        else if (relativeTargetVector.getX() < 0 && relativeVelocity.getX() > relativeTargetVector.getX())
            thrust.put(this.thNames.x, -1);
        else if (relativeTargetVector.getX() < 0 && relativeVelocity.getX() < relativeTargetVector.getX())
            thrust.put(this.thNames.x, 1);
        if (relativeTargetVector.getY() > 0 && relativeVelocity.getY() < relativeTargetVector.getY())
            thrust.put(this.thNames.y, 1);
        else if (relativeTargetVector.getY() > 0 && relativeVelocity.getY() > relativeTargetVector.getY())
            thrust.put(this.thNames.y, -1);
        else if (relativeTargetVector.getY() < 0 && relativeVelocity.getY() > relativeTargetVector.getY())
            thrust.put(this.thNames.y, -1);
        else if (relativeTargetVector.getY() < 0 && relativeVelocity.getY() < relativeTargetVector.getY())
            thrust.put(this.thNames.y, 1);
        this.setState(this.stNames.thrust, thrust);
    }

    private void applyThrust (ShipAPI ship, HashMap<String, Integer> thrust) {
        if (ship.getCustomData().containsKey(RTSAssist.shipCNames.overrideThrust)
                && ship.getCustomData().get(RTSAssist.shipCNames.overrideThrust) != null) {
            Vector2f override = (Vector2f)ship.getCustomData().get(RTSAssist.shipCNames.overrideThrust);
            thrust.put(this.thNames.y, (Integer)((int)override.getY()));
            thrust.put(this.thNames.x, (Integer)((int)override.getX()));
        }
        if (thrust.get(this.thNames.y) == 1) {
            ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
            ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
        }
        else if (thrust.get(this.thNames.y) == -1) {
            ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
            ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
        }
        else {
            ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
            ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
        }
        if (thrust.get(this.thNames.x) == 1) {
            ship.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
            ship.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT);
        }
        else if (thrust.get(this.thNames.x) == -1) {
            ship.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
            ship.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT);
        }
        else {
            ship.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT);
            ship.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT);
        }
    }

    private void getRotation (float ship, float target, float angularVelocity, HashMap<String, Object> assignment) {
        int flag = 0;
        float delta = MathUtils.getShortestRotation(ship, target);
        if (Math.abs(delta) < 1)
            flag = 0;
        else if (delta > 0)
            flag = -1;
        else
            flag = 1;
        ShipAPI shipQ = (ShipAPI)assignment.get("ship");
        ShipAPI enemy;
        if (this.getState(this.stNames.closestEnemy) != null)
            enemy = (ShipAPI)this.getState(this.stNames.closestEnemy);
        else
            enemy = assignment.get("targetEnemy") == null
                    ? assignment.get("priorityEnemy") == null
                    ? null
                    : (ShipAPI)assignment.get("priorityEnemy")
                    : (ShipAPI)assignment.get("targetEnemy");
        if (enemy != null) {
            float deltaR =  MathUtils.getShortestRotation(VectorUtils.getAngle(
                    new Vector2f(
                        shipQ.getLocation().getX() + shipQ.getVelocity().getX(),
                        shipQ.getLocation().getY() + shipQ.getVelocity().getY()
                    ),
                    new Vector2f(
                            enemy.getLocation().getX() + enemy.getVelocity().getX(),
                            enemy.getLocation().getY() + enemy.getVelocity().getY()
                    )),
                    VectorUtils.getAngle(shipQ.getLocation(), enemy.getLocation()));
            angularVelocity = angularVelocity + deltaR;
        }
        if (Math.abs(delta) < Math.abs(angularVelocity) && !(delta * angularVelocity < 0)) {
            flag = flag * -1;
        }
        this.setState(this.stNames.rotation, flag);
    }

    private void applyRotation (ShipAPI ship, Vector2f coOrd, int rotation, boolean hasEnemy) {
        if (ship.getCustomData().containsKey(RTSAssist.shipCNames.overrideFacing)
                && ship.getCustomData().get(RTSAssist.shipCNames.overrideFacing) != null) {
            rotation = (int)ship.getCustomData().get(RTSAssist.shipCNames.overrideFacing);
        }
        else if (!hasEnemy && (MathUtils.getDistanceSquared(ship, coOrd) < Math.pow(200f, 2f) ||
                ship.areAnyEnemiesInRange())) return;
        switch (rotation) {
            case 0: ship.blockCommandForOneFrame(ShipCommand.TURN_LEFT);
                ship.blockCommandForOneFrame(ShipCommand.TURN_RIGHT); break;
            case 1: ship.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
                ship.blockCommandForOneFrame(ShipCommand.TURN_LEFT); break;
            case -1: ship.giveCommand(ShipCommand.TURN_LEFT, null, 0);
                ship.blockCommandForOneFrame(ShipCommand.TURN_RIGHT); break;
        }
    }

    private void blockSystem (RTS_BlockSystemPlugin blockSystem, HashMap<String, Object> assignment) {
        if (
                blockSystem != null
                && blockSystem.execBlockOnNextFrame((RTS_Context)this.getState(this.stNames.context))
                && ((ShipAPI)this.getState(this.stNames.ship)).getCustomData().get(RTSAssist.shipCNames.overrideBlockSystem) == null
        ) {
            if (
                    ((ShipAPI)this.getState(this.stNames.ship)).getSystem().getState() == ShipSystemAPI.SystemState.IN
                    || ((ShipAPI)this.getState(this.stNames.ship)).getSystem().getState() == ShipSystemAPI.SystemState.OUT
                    || ((ShipAPI)this.getState(this.stNames.ship)).getSystem().getState() == ShipSystemAPI.SystemState.COOLDOWN
            )
                return;
            if (((ShipAPI)this.getState(this.stNames.ship)).getSystem().isActive())
                ((ShipAPI)this.getState(this.stNames.ship)).getSystem().forceState(ShipSystemAPI.SystemState.OUT, 0f);
            ((ShipAPI)this.getState(this.stNames.ship)).blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
        }
    }
}