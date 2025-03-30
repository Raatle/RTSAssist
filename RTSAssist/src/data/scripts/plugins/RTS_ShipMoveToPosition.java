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

import java.util.Arrays;
import java.util.HashMap;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.*;

import org.lazywizard.lazylib.*;

import data.scripts.plugins.Utils.RTS_Context;
import data.scripts.plugins.Utils.RTS_Pathing;
import data.scripts.plugins.Utils.RTS_StatefulClasses;
import data.scripts.plugins.BlockedSystems.RTS_BlockSystemPlugin;

public class RTS_ShipMoveToPosition extends RTS_StatefulClasses {

    public RTS_ShipMoveToPosition(final ShipAPI ship, Object state) {
        super(state, true);
        final HashMap<String, Float> shipHullStrafeMod = new HashMap<String, Float>(){{
            put("FRIGATE", 1f);
            put("DESTROYER", 0.75f);
            put("CRUISER", 0.5f);
            put("CAPITAL_SHIP", 0.25f);
        }};
        HashMap<String, Object> init = new HashMap<String, Object>() {{
            put("ship", ship);
            put("thrust", (HashMap<String, Integer>)null);
            put("rotation", 0);
            put("vData", new HashMap<String, Object>());
            put("shipHullStrafeMod", shipHullStrafeMod);
        }};
        init.put("pathing", new RTS_Pathing(this.returnState(true)));
        this.setState(init);
    }

    public void moveToPosition(HashMap<String, Object> assignment, Boolean update) {
//        if (((ShipAPI)assignment.get("ship")).getShipAI().getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) != null)
//            System.out.println(((ShipAPI)((ShipAPI)assignment.get("ship")).getShipAI().getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET)).getName());
        if (this.getState("ship") != null) {
            if (assignment.get("targetEnemy") != null) {
                ((ShipAPI)assignment.get("ship")).setShipTarget((ShipAPI)assignment.get("targetEnemy"));
//                ((ShipAPI)assignment.get("ship")).getShipAI().getAIFlags().setFlag(
//                        ShipwideAIFlags.AIFlags.MANEUVER_TARGET,
//                        1f,
//                        (ShipAPI)assignment.get("targetEnemy")
//                );
            }
            if (update) this.update(
                    assignment,
                    (ShipAPI)this.getState("ship"),
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
                    assignment.get("priorityEnemy") != null ? (ShipAPI)assignment.get("priorityEnemy") : null
            );
            this.applyThrust(
                    (ShipAPI)this.getState("ship"),
                    (HashMap<String, Integer>)this.getState("thrust")
            );
            this.applyRotation(
                    (ShipAPI)this.getState("ship"),
                    (Vector2f)assignment.get("dest"),
                    (int)this.getState("rotation"),
                    (assignment.get("targetEnemy") != null || assignment.get("priorityEnemy") != null)
            );
            this.blockSystem(
                    (RTS_BlockSystemPlugin)assignment.get("blockedSystem"),
                    assignment
            );
            this.blockPhase(true);
        }
    }

    public void update (HashMap<String, Object> assignment, ShipAPI ship, Vector2f coOrd, ShipAPI targetEnemy, ShipAPI priorityEnemy) {
        coOrd = ((RTS_Pathing)(this.getState("pathing"))).adjustDestCoord(ship, coOrd, targetEnemy);
        assignment.put("modDest", coOrd);
        HashMap<String, Object> hold = (HashMap<String, Object>)this.getState("vData");
        hold.put("maxSpeed", ship.getMaxSpeed());
        hold.put("distanceToPoint", MathUtils.getDistance(ship.getLocation(), coOrd));
        hold.put("currentBearing", ship.getFacing());
        hold.put("relativeVelocity", new Vector2f(
                ship.getVelocity().getX(),
                ship.getVelocity().getY()
        ));
        VectorUtils.rotate(
                (Vector2f)hold.get("relativeVelocity"),
                MathUtils.clampAngle(-1 * ((float)hold.get("currentBearing") - 90))
        );
        hold.put("relativeTargetVector", VectorUtils.getDirectionalVector(ship.getLocation(), coOrd));
        VectorUtils.rotate(
                (Vector2f)hold.get("relativeTargetVector"),
                MathUtils.clampAngle(-1 * ((float)hold.get("currentBearing") - 90))
        );
        hold.put(
                "adjMaxSpeed",
                this.adjustMaxSpeed(
                        (Vector2f)hold.get("relativeTargetVector"),
                        (float)hold.get("maxSpeed"),
                        (float)hold.get("distanceToPoint"),
                        ship,
                        targetEnemy == null ? null : targetEnemy
                )

        );
        VectorUtils.resize(
                (Vector2f)hold.get("relativeTargetVector"),
                (float)hold.get("adjMaxSpeed")
        );
        this.getRotation(
                ship.getFacing(),
                (VectorUtils.getAngle(
                        ship.getLocation(),
                        targetEnemy == null
                                ? priorityEnemy == null
                                ? coOrd
                                : priorityEnemy.getLocation()
                                : targetEnemy.getLocation()
                        ) + (float)((((HashMap<String, Object>)this.getState("broadsideData", true)).
                                        containsKey(ship.getHullSpec().getBaseHullId())
                                && (targetEnemy != null || priorityEnemy != null))
                                ? (((HashMap<String, Object>)this.getState("broadsideData", true)).
                                        get(ship.getHullSpec().getBaseHullId()))
                                : 0f
                )),
                ship.getAngularVelocity(),
                assignment
        );
        this.setState("vData", hold);
        this.getThrust(
                (Vector2f)this.getDeepState(Arrays.asList("vData", "relativeVelocity")),
                (Vector2f)this.getDeepState(Arrays.asList("vData", "relativeTargetVector"))
        );
    }

    private Vector2f setRetreatCoOrd(ShipAPI ship, Vector2f coOrd, Vector2f coOrdSec) {
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

    private Vector2f findAttackCoOrd(ShipAPI ship, ShipAPI target, Boolean isAgressive, HashMap<String, Object> assignment) {
        Vector2f weaponRange = this.getWeaponRanges(ship, (HashMap<String, Object>)assignment.get("weaponPosMods"));
        Vector2f D2CHold = CollisionUtils.getCollisionPoint(
                ship.getLocation(),
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
                ((HashMap<String, Float>)this.getState("shipHullStrafeMod")).get(ship.getHullSize().name())
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
        HashMap<String, Integer> thrust = new HashMap<>();
        thrust.put("x", 0);
        thrust.put("y", 0);
        if (relativeTargetVector.getX() > 0 && relativeVelocity.getX() < relativeTargetVector.getX()) thrust.put("x", 1);
        else if (relativeTargetVector.getX() > 0 && relativeVelocity.getX() > relativeTargetVector.getX()) thrust.put("x", -1);
        else if (relativeTargetVector.getX() < 0 && relativeVelocity.getX() > relativeTargetVector.getX()) thrust.put("x", -1);
        else if (relativeTargetVector.getX() < 0 && relativeVelocity.getX() < relativeTargetVector.getX()) thrust.put("x", 1);
        if (relativeTargetVector.getY() > 0 && relativeVelocity.getY() < relativeTargetVector.getY()) thrust.put("y", 1);
        else if (relativeTargetVector.getY() > 0 && relativeVelocity.getY() > relativeTargetVector.getY()) thrust.put("y", -1);
        else if (relativeTargetVector.getY() < 0 && relativeVelocity.getY() > relativeTargetVector.getY()) thrust.put("y", -1);
        else if (relativeTargetVector.getY() < 0 && relativeVelocity.getY() < relativeTargetVector.getY()) thrust.put("y", 1);
        this.setState("thrust", thrust);
    }

    private void applyThrust(ShipAPI ship, HashMap<String, Integer> thrust) {
        if (thrust.get("y") == 1) {
            ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
            ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
        }
        else if (thrust.get("y") == -1) {
            ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
            ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
        }
        else {
            ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
            ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
        }
        if (thrust.get("x") == 1) {
            ship.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
            ship.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT);
        }
        else if (thrust.get("x") == -1) {
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
        ShipAPI enemy = assignment.get("targetEnemy") == null
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
        this.setState("rotation", flag);
    }

    private void applyRotation (ShipAPI ship, Vector2f coOrd, int rotation, boolean hasEnemy) {
        if (!hasEnemy && (MathUtils.getDistanceSquared(ship, coOrd) < Math.pow(200f, 2f) ||
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
        if (blockSystem != null && blockSystem.blockOnNextFrame(
                new RTS_Context(this.returnState(), assignment)
        )) {
            ((ShipAPI) this.getState("ship")).blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
            if (((ShipAPI) this.getState("ship")).getSystem().isActive())
                ((ShipAPI) this.getState("ship")).getSystem().deactivate();
        }
    }

    private void blockPhase (boolean blockPhase) {
        if (((ShipAPI)this.getState("ship")).getPhaseCloak() != null)
            ((ShipAPI)this.getState("ship")).getPhaseCloak().deactivate();
    }
}