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

package data.scripts.plugins.BlockedSystems;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.plugins.RTSAssist;
import data.scripts.plugins.Utils.RTS_AssortedFunctions;
import data.scripts.plugins.Utils.RTS_Context;
import data.scripts.plugins.Utils.RTS_Event;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface RTS_CompatibilityTools {

    class internalContext {
        RTS_Context store = null;
    }
    internalContext context = new internalContext();

    interface atCleanup {
        void run();
    }
    interface everyFrame {
        void run();
    }

    enum typeIdentifiers {
        rerouteAccelerationData,
        hadEnemyData,
        cooldownData,
        overrideThrustData,
        overrideFacingData,
        manualData,
    }

    HashMap<ShipAPI, List<atCleanup>> queueForCleaning = new HashMap<>();
    HashMap<ShipAPI, List<everyFrame>> queueForEveryFrame = new HashMap<>();
    HashMap<ShipAPI, HashMap<typeIdentifiers, Object>> staticClassStore = new HashMap<>();


    default void init (RTS_Context parentContext) {
        this.context.store = parentContext;
        if (!this.queueForCleaning.containsKey(parentContext.getShip()))
            this.queueForCleaning.put(parentContext.getShip(), new ArrayList<>());
        if (!this.queueForEveryFrame.containsKey(parentContext.getShip()))
            this.queueForEveryFrame.put(parentContext.getShip(), new ArrayList<>());
    }

    default float getFloatBasedOnBaseSpeed (float multiplier) {
        float variantCalc = this.context.store.getShip().getMutableStats().getMaxSpeed().getBaseValue() * multiplier;
        if (this.context.store.getShip().getVariant().hasHullMod("safetyoverrides"))
            variantCalc = variantCalc * 2;
        return (variantCalc);
    }

    default float getFloatBasedOnVariant (HashMap<String, Float> map) {
        return (map.get(this.context.store.getShip().getHullSpec().getBaseHullId()));
    }

    default void useOnCoolDown (boolean flag) {
        if (!flag && this.context.store.getShip().getSystem().canBeActivated() && !this.context.store.getShip().getSystem().isActive())
            this.context.store.getShip().giveCommand(ShipCommand.USE_SYSTEM, this.context.store.getModDest(), 0);
    }

    default boolean ifOnStayOn () {
        return (this.context.store.getShip().getSystem().isActive());
    }

    default boolean approachingModDest () {
        if (MathUtils.getDistanceSquared(
                this.context.store.getShip().getLocation(),
                this.context.store.getModDest()
        ) < Math.pow(this.context.store.getShip().getShieldRadiusEvenIfNoShield(), 2))
            return (true);
        return (false);
    }

    default boolean withinDistanceOfModDest (float distance) {
        if (MathUtils.getDistanceSquared(
                this.context.store.getShip().getLocation(),
                this.context.store.getModDest()
        ) < Math.pow(distance, 2))
            return (true);
        return (false);
    }

    default boolean notVectoringToModDest (float allowance) {
        if (Math.abs(MathUtils.getShortestRotation(
                VectorUtils.getAngle(
                        this.context.store.getShip().getLocation(),
                        this.context.store.getModDest()
                ),
                VectorUtils.getFacing(this.context.store.getShip().getVelocity())
        )) > allowance)
            return (true);
        return (false);
    }

    default boolean notFacingModDest (float allowance) {
        if (Math.abs(MathUtils.getShortestRotation(
                VectorUtils.getAngle(
                        this.context.store.getShip().getLocation(),
                        this.context.store.getModDest()
                ),
                this.context.store.getShip().getFacing()
        )) > allowance)
            return (true);
        return (false);
    }

    default boolean conserveAmmo () {
        return (this.context.store.getShip().getSystem().getAmmo() <= (this.context.store.getShip().getSystem().getMaxAmmo() - 1));
    }

    default boolean isAttackWeaponfireing () {
        for (WeaponAPI set : this.context.store.getShip().getUsableWeapons()){
            if (set.getType().compareTo(WeaponAPI.WeaponType.MISSILE) == 0
                    || set.getOriginalSpec().getPrimaryRoleStr() == null
                    || set.getOriginalSpec().getPrimaryRoleStr().equals("Point Defense")
                    || set.getOriginalSpec().getPrimaryRoleStr().equals("Point Defense (Area)"))
                continue;
            if (set.isFiring())
                return (true);
        }
        return (false);
    }

    default boolean hasEnemy () {
        return (this.context.store.getTargetEnemy() != null || this.context.store.getPriorityEnemy() != null);
    }

    default boolean keepChargeInReserve () {
        return (this.context.store.getShip().getSystem().getAmmo() <= 1f);
    }

    default boolean engageDiveTactics (float approach, float minAmmoReq) {
        return (
                this.context.store.getTargetEnemy() != null
                && this.context.store.getShip().getSystem().getAmmo() >= minAmmoReq
                && this.withinDistanceOfModDest(approach)
        );
    }

    default void rerouteAcceleration () {
        rerouteAccelerationData data = (rerouteAccelerationData)this.getData(typeIdentifiers.rerouteAccelerationData);
        if (!data.init) {
            data.init = true;
            this.context.store.getShip().getSystem().getSpecAPI().setAccelerateAllowed(false);
            queueForCleaning.get(this.context.store.getShip()).add(new atCleanup() {
                @Override
                public void run() {
                    context.store.getShip().getSystem().getSpecAPI().setAccelerateAllowed(true);
                    data.init = false;
                }
            });
        }
        if (this.context.store.getShip().getSystem().isActive()) {
            if (!data.firstFrameInit) {
                this.context.store.getShip().getVelocity().set(
                        VectorUtils.resize(
                                VectorUtils.getDirectionalVector(
                                        this.context.store.getShip().getLocation(),
                                        this.context.store.getModDest()
                                ),
                                this.context.store.getShip().getMutableStats().getMaxSpeed().modified
                        )
                );
                data.firstFrameInit = true;
            }
        }
        else
            data.firstFrameInit = false;
    }
    class rerouteAccelerationData {
        boolean init = false;
        boolean firstFrameInit = false;
    }

    default boolean playerActivatedSystemManually () {
        manualData data = (manualData)this.getData(typeIdentifiers.manualData);
        if (!data.init) {
            data.init = true;
            if (data.tHold == 0f || this.context.store.getTimePassed() - data.tHold > 0.2f)
                data.isManual = false;
            this.context.store.getEventManager().deleteEvent(data.eventHold);
            data.eventHold = this.context.store.getEventManager().addEvent(new RTS_Event() {
                @Override
                public boolean shouldExecute(Object state) {
                    data.tHold = context.store.getTimePassed();
                    return (false);
                }
            });
            queueForCleaning.get(this.context.store.getShip()).add(new atCleanup() {
                @Override
                public void run() {
                    data.init = false;
                    context.store.getEventManager().deleteEvent(data.eventHold);
                }
            });
        }
        if (this.context.store.getShip().getCustomData().get(RTSAssist.shipCNames.overrideBlockSystem) != null)
            data.isManual = true;
        return (data.isManual);
    }
    class manualData {
        boolean init = false;
        float tHold = 0f;
        boolean isManual = false;
        String eventHold = "";
    }

    default boolean hadEnemy () {
        hadEnemyData data = (hadEnemyData)this.getData(typeIdentifiers.hadEnemyData);
        if (!data.init) {
            queueForEveryFrame.get(this.context.store.getShip()).add(new everyFrame() {
                ShipAPI ship = context.store.getShip();

                @Override
                public void run() {
                    if (context.store.getNeighboursFast().findEnemyShipNeighbours(ship, 1500f, false).isEmpty())
                        data.hadEnemy = false;
                }
            });
            queueForCleaning.get(this.context.store.getShip()).add(new atCleanup() {
                @Override
                public void run() {
                    data.init = false;
                    if (context.store.getShip().isHulk())
                        context.store.getEventManager().deleteEvent(data.eventHold);

                }
            });
            data.init = true;
        }
        data.hadEnemy = data.hadEnemy || this.context.store.getTargetEnemy() != null;
        if (data.hadEnemy && this.context.store.getTargetEnemy() == null) {
            data.eventHold = this.context.store.getEventManager().addEvent(new RTS_Event() {
                float holdTime = context.store.getTimePassedIngame();
                ShipAPI ship = context.store.getShip();
                boolean systemWasUsed = true;

                @Override
                public boolean shouldExecute(Object state) {
                    if (ship.getSystem().isActive() || ship.getSystem().isCoolingDown() && systemWasUsed)
                        holdTime = context.store.getTimePassedIngame();
                    else
                        systemWasUsed = false;
                    return ((context.store.getTimePassedIngame() - holdTime) > 0.2f || ship.isHulk());
                }

                @Override
                public void run() {
                    data.hadEnemy = false;
                    data.eventHold = null;
                }
            });
        }
        if (data.eventHold != null && this.context.store.getTargetEnemy() != null) {
            this.context.store.getEventManager().deleteEvent(data.eventHold);
            data.eventHold = null;
        }
        return (this.context.store.getTargetEnemy() == null && data.hadEnemy);
    }
    class hadEnemyData {
        boolean init = false;
        boolean hadEnemy = false;
        String eventHold = null;
    }

    default void overrideThrust (Vector2f override) {
        overrideThrustData data = (overrideThrustData)this.getData(typeIdentifiers.overrideThrustData);
        this.context.store.getShip().setCustomData(RTSAssist.shipCNames.overrideThrust, override);
        if (data.eventHold == null)
            data.eventHold = this.context.store.getEventManager().addEvent(new RTS_Event() {
                ShipAPI ship = context.store.getShip();
                float tHold = context.store.getTimePassedIngame();

                @Override
                public boolean shouldExecute(Object state) {
                    if (this.ship.getSystem().isActive())
                        tHold = context.store.getTimePassedIngame();
                    return (context.store.getTimePassedIngame() - tHold > 0.1f);
                }

                @Override
                public void run() {
                    ship.setCustomData(RTSAssist.shipCNames.overrideThrust, null);
                    data.eventHold = null;
                }
            });
        queueForCleaning.get(this.context.store.getShip()).add(new atCleanup() {
            ShipAPI ship = context.store.getShip();

            @Override
            public void run() {
                if (ship.isHulk())
                    context.store.getEventManager().deleteEvent(data.eventHold);
            }
        });
    }
    class overrideThrustData {
        String eventHold = null;
    }
    
    default void overrideFacing (int override) {
        overrideFacingData data = (overrideFacingData)this.getData((typeIdentifiers.overrideFacingData));
        this.context.store.getShip().setCustomData(RTSAssist.shipCNames.overrideFacing, override);
        if (data.eventHold == null)
            data.eventHold = this.context.store.getEventManager().addEvent(new RTS_Event() {
                ShipAPI ship = context.store.getShip();

                float tHold = context.store.getTimePassedIngame();

                @Override
                public boolean shouldExecute(Object state) {
                    if (this.ship.getSystem().isActive())
                        tHold = context.store.getTimePassedIngame();
                    return (context.store.getTimePassedIngame() - tHold > 0.1f);
                }

                @Override
                public void run() {
                    ship.setCustomData(RTSAssist.shipCNames.overrideFacing, null);
                    data.eventHold = null;
                }
            });
        queueForCleaning.get(this.context.store.getShip()).add(new atCleanup() {
            ShipAPI ship = context.store.getShip();

            @Override
            public void run() {
                if (ship.isHulk())
                    context.store.getEventManager().deleteEvent(data.eventHold);
            }
        });
    }
    class overrideFacingData {
        String eventHold = null;
    }

    default void addCoolDownOnCleanup () {
        cooldownData data = (cooldownData)this.getData(typeIdentifiers.cooldownData);
        if (!data.init) {
            if (data.eventHold != null) {
                this.context.store.getEventManager().deleteEvent(data.eventHold);
                data.eventHold = null;
            }
            if (data.secondaryEventHold != null) {
                this.context.store.getEventManager().deleteEvent(data.secondaryEventHold);
                data.secondaryEventHold = null;
            }
            if (!this.context.store.getShip().getSystem().isActive()) {
                this.context.store.getShip().getSystem().setCooldownRemaining(MathUtils.clamp(
                        data.cooldownAtCleanup
                                - (this.context.store.getTimePassedIngame()
                                - data.cleanUpDelta),
                        0f,
                        255f
                ));
            }
            queueForCleaning.get(this.context.store.getShip()).add(new atCleanup() {
                ShipAPI ship = context.store.getShip();

                @Override
                public void run() {
                    if (this.ship.isHulk())
                        return;
                    data.cooldownAtCleanup = this.ship.getSystem().getCooldownRemaining();
                    data.cleanUpDelta = context.store.getTimePassedIngame();
                    this.ship.setCustomData(RTSAssist.shipCNames.blockAi, true);
                    data.eventHold = context.store.getEventManager().addEvent(new RTS_Event() {

                        @Override
                        public boolean shouldExecute(Object state) {
                            if (ship == null || ship.isHulk()) {
                                context.store.getEventManager().deleteEvent(data.eventHold);
                                return (false);
                            }
                            ship.setCustomData(RTSAssist.shipCNames.blockAi, null);
                            return(!ship.getSystem().isActive());
                        }

                        @Override
                        public void run() {
                            if (context.store.isRTSMode())
                                ship.getSystem().setCooldownRemaining(Math.max(2f, ship.getSystem().getCooldownRemaining()));
                            else if (ship.getId().equals(context.store.getPlayerShip().getId()))
                                ship.getSystem().setCooldownRemaining(Math.max(0.1f, ship.getSystem().getCooldownRemaining()));
                        }
                    });
                    data.secondaryEventHold = context.store.getEventManager().addEvent(new RTS_Event() {
                        float timeHold = context.store.getTimePassedIngame();

                        @Override
                        public boolean shouldExecute(Object state) {
                            if (ship == null || ship.isHulk()) {
                                context.store.getEventManager().deleteEvent(data.secondaryEventHold);
                                return (false);
                            }
                            if (ship.getSystem().isActive()) {
                                timeHold = context.store.getTimePassedIngame();
                                return (false);
                            }
                            if (context.store.isRTSMode())
                                return (context.store.getTimePassedIngame() - timeHold > 2f);
                            else {
                                if (ship.getId().equals(context.store.getPlayerShip().getId()))
                                    return (context.store.getTimePassedIngame() - timeHold > 0.1f);
                                return (context.store.getTimePassedIngame() - timeHold > 2f);
                            }
                        }

                        @Override
                        public void run() {
                            ship.getSystem().setCooldownRemaining(MathUtils.clamp(
                                    data.cooldownAtCleanup
                                            - (context.store.getTimePassedIngame()
                                            - data.cleanUpDelta),
                                    0.1f,
                                    255f
                            ));
                        }
                    });
                    data.init = false;
                }
            });
            data.init = true;
        }
    }
    class cooldownData {
        boolean init = false;
        float cooldownAtCleanup = 0f;
        float cleanUpDelta = 0f;
        float firstFrameDelta = 0f;
        String eventHold = null;
        String secondaryEventHold = null;
    }

    default Vector2f getHeadingInQuad (boolean precise) {
        Vector2f heading = new Vector2f();
        if (RTS_AssortedFunctions.getDistanceSquared(
                this.context.store.getShip().getLocation(),
                this.context.store.getModDest()
        ) < 100f) {
            heading.set(0f, 0f);
            return (heading);
        }
        float angle = MathUtils.getShortestRotation(
                VectorUtils.getAngle(this.context.store.getShip().getLocation(), this.context.store.getModDest()),
                this.context.store.getShip().getFacing()
        );
        if (precise) {
            if (angle == 0f)
                heading.set(0f, 1f);
            else if (angle == 180f)
                heading.set(0f, -1f);
            else if (angle < 0f) {
                if (angle == -90f)
                    heading.set(-1f, 0f);
                else if (angle > -90f)
                    heading.set(-1f, 1f);
                else
                    heading.set(-1f, -1f);
            } else {
                if (angle == 90f)
                    heading.set(1f, 0f);
                else if (angle < 90f)
                    heading.set(1f, 1f);
                else
                    heading.set(1f, -1f);
            }
        }
        else {
            if (angle <= 20f && angle >= -20f)
                heading.set(0f, 1f);
            else if (angle <= -160f || angle >= 160f)
                heading.set(0f, -1f);
            else if (angle < -20f) {
                if (angle > -70f)
                    heading.set(-1f, 1f);
                else if (angle >= -110f)
                    heading.set(-1f, 0f);
                else
                    heading.set(-1f, -1f);
            }
            else {
                if (angle < 70f)
                    heading.set(1f, 1f);
                else if (angle <= 110f)
                    heading.set(1f, 0f);
                else
                    heading.set(1f, -1f);
            }
        }
        return (heading);
    }

    private Object getData (typeIdentifiers type) {
        if (!this.staticClassStore.containsKey(this.context.store.getShip()))
            this.staticClassStore.put(this.context.store.getShip(), new HashMap<>());
        if (!this.staticClassStore.get(this.context.store.getShip()).containsKey(type))
            this.staticClassStore.get(this.context.store.getShip()).put(type, getDataStructure(type));
        return (this.staticClassStore.get(this.context.store.getShip()).get(type));
    }
    private Object getDataStructure (typeIdentifiers type) {
        /*
        else if (type == typeIdentifiers.)
            return (new ());
         */
        if (type == typeIdentifiers.cooldownData)
            return (new cooldownData());
        else if (type == typeIdentifiers.hadEnemyData)
            return (new hadEnemyData());
        else if (type == typeIdentifiers.rerouteAccelerationData)
            return (new rerouteAccelerationData());
        else if (type == typeIdentifiers.overrideThrustData)
            return (new overrideThrustData());
        else if (type == typeIdentifiers.overrideFacingData)
            return (new overrideFacingData());
        else if (type == typeIdentifiers.manualData)
            return (new manualData());
        else
            return (null);
    }
}