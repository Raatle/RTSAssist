package data.scripts.plugins.BlockedSystems;

import data.scripts.plugins.Utils.RTS_Context;

public class RTS_DiableSystems {

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            boolean stateHold;

            @Override
            public String getSystemName() {
                return "diableavionics_transit2";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                return (false);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "diableavionics_drift";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = this.getFloatBasedOnBaseSpeed(1.3f);
//                this.rerouteAcceleration();
                this.addCoolDownOnCleanup();
                /* Default is enable */
                boolean flag = false;
                /* If our ammo is 1 less than maximum block usage */
                if (this.conserveAmmo())
                    flag = true;
                /* If we have an enemy and more than 2 ammo and we are close enough allow usage */
                if (this.engageDiveTactics(variantCalc * 5, 2))
                    flag = false;
                /* If we had an enemy and now we dont, allow usage regardless of ammo for 0.2s */
                if (this.hadEnemy())
                    flag = false;
                /* If we are too close block usage */
                if (this.withinDistanceOfModDest(variantCalc * 3))
                    flag = true;
                /* If battle has just begun block usage */
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                /* If the system is active, do not block system use */
                if (this.ifOnStayOn())
                    flag = false;
                /* If flag is false, use the system regardless of what the AI chooses */
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "diableavionics_evasion";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = this.getFloatBasedOnBaseSpeed(1.3f);
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.withinDistanceOfModDest(variantCalc * 2))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "diableavionics_damperwave";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = this.getFloatBasedOnBaseSpeed(1.3f);
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.conserveAmmo())
                    flag = true;
                if (this.engageDiveTactics(variantCalc * 10, 2))
                    flag = false;
                if (this.hadEnemy())
                    flag = false;
                if (this.withinDistanceOfModDest(variantCalc * 7f))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "diableavionics_heavyflicker";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2.2f;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.conserveAmmo())
                    flag = true;
                if (this.engageDiveTactics(variantCalc * 1.5f, 2))
                    flag = false;
                if (this.hadEnemy())
                    flag = false;
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (this.notVectoringToModDest(20f))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "diableavionics_flicker";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2.5f;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.conserveAmmo())
                    flag = true;
                if (this.engageDiveTactics(variantCalc * 1.5f, 2))
                    flag = false;
                if (this.hadEnemy())
                    flag = false;
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (this.notVectoringToModDest(10f))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "diableavionics_unlockedFlicker";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2.5f;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                if (this.notVectoringToModDest(10f))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "diableavionics_temporalshell";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = this.getFloatBasedOnBaseSpeed(1.3f);
                this.rerouteAcceleration();
                this.addCoolDownOnCleanup();
                boolean flag = false;
                if (this.conserveAmmo())
                    flag = true;
                if (this.engageDiveTactics(variantCalc * 5, context.getShip().getSystem().getMaxAmmo() == 2 ? 1 : 2))
                    flag = false;
                if (this.hadEnemy())
                    flag = false;
                if (this.withinDistanceOfModDest(variantCalc * 3))
                    flag = true;
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });
    }

    /* Riptide */

//    private class diableavionics_drift_staticClass {
//        boolean init = false; // velocity, cooldown management,
//        boolean firstFrameInit = false; // velocity
//        float cooldownAtCleanup = 0f; // cooldown
//        float cleanUpDelta = 0f; // cooldown
//        float firstFrameDelta = 0f; // cooldown
//        boolean hadEnemy = false; // charge control
//        String eventHold = null; // cooldown
//        HashMap<String, Object> variantDistance = new HashMap<>() {{
//            put("diableavionics_pandemonium", 50f);
//            put("diable_riptide", 137f);
//            put("diableavionics_maelstrom", 60f);
//        }};
//    }
//    private RTS_BlockSystemPlugin _diableavionics_drift_IGNORE () {
//        return (new RTS_BlockSystemPlugin() {
//            private diableavionics_drift_staticClass diableavionics_drift_static = new diableavionics_drift_staticClass();
//
//            @Override
//            public String getSystemName() {
//                return "diableavionics_drift";
//            }
//
//            @Override
//            public boolean blockOnNextFrame(RTS_Context context) {
//
//
//                if (context.getNeighboursFast().findEnemyShipNeighbours(context.getShip(), 1500f, false).isEmpty())
//                    diableavionics_drift_static.hadEnemy = false;
//                if (!diableavionics_drift_static.init) {
//                    if (diableavionics_drift_static.eventHold != null) {
//                        context.getEventManager().deleteEvent(diableavionics_drift_static.eventHold);
//                        diableavionics_drift_static.eventHold = null;
//                    }
//                    if (
//                            diableavionics_drift_static.firstFrameDelta != 0f
//                            && context.getShip().getSystem().getCooldownRemaining() > 0f
//                            && !context.getShip().getSystem().isActive()
//                    ) {
//                            context.getShip().getSystem().setCooldownRemaining(MathUtils.clamp(
//                                    diableavionics_drift_static.cooldownAtCleanup
//                                            - (context.getTimePassedIngame()
//                                            - diableavionics_drift_static.cleanUpDelta),
//                                    0.1f,
//                                    255f
//                            ));
//                    }
//                    diableavionics_drift_static.firstFrameDelta = context.getTimePassedIngame();
//                    context.getShip().getSystem().getSpecAPI().setAccelerateAllowed(false);
//                    diableavionics_drift_static.init = true;
//                }
//                if (context.getShip().getSystem().isActive()) {
//                    if (!diableavionics_drift_static.firstFrameInit) {
//                        context.getShip().getVelocity().set(
//                                VectorUtils.resize(
//                                        VectorUtils.getDirectionalVector(
//                                                context.getShip().getLocation(),
//                                                context.getModDest()
//                                        ),
//                                        context.getShip().getMutableStats().getMaxSpeed().modified
//                                )
//                        );
//                        diableavionics_drift_static.firstFrameInit = true;
//                    }
//                }
//                else
//                    diableavionics_drift_static.firstFrameInit = false;
//
//
//                boolean flag = false;
//                if (context.getShip().getSystem().getAmmo() <= (context.getShip().getSystem().getMaxAmmo() - 1))
//                    flag = true;
//                if (context.getTargetEnemy() != null
//                        && context.getShip().getSystem().getAmmo() >= 2
//                ){
//                    if (MathUtils.getDistanceSquared(
//                            context.getShip().getLocation(),
//                            context.getModDest()
//                    ) < Math.pow(((float)diableavionics_drift_static.variantDistance
//                            .get(context.getShip().getHullSpec().getBaseHullId()) * 5), 2))
//                        flag = false;
//                }
//
//
//                if (context.getTargetEnemy() == null && diableavionics_drift_static.hadEnemy)
//                    flag = false;
//
//
//                if (MathUtils.getDistanceSquared(
//                        context.getShip().getLocation(),
//                        context.getModDest()
//                ) < Math.pow(((float)diableavionics_drift_static.variantDistance
//                        .get(context.getShip().getHullSpec().getBaseHullId()) * 3), 2))
//                    flag = true;
//                if (context.getTimePassedIngame() < 3f)
//                    flag = true;
//                if (context.getShip().getSystem().isActive())
//                    flag = false;
//
//
//                diableavionics_drift_static.hadEnemy = context.getTargetEnemy() != null
//                        ? true
//                        : (diableavionics_drift_static.hadEnemy && context.getShip().getSystem().isActive())
//                                || (MathUtils.getDistanceSquared(
//                                        context.getShip().getLocation(),
//                                        context.getModDest()
//                                ) < Math.pow(100f, 2))
//                        ? false
//                        : diableavionics_drift_static.hadEnemy;
//
//
//                return (flag);
//            }
//
//            @Override
//            public void cleanUp(RTS_Context context) {
//                diableavionics_drift_static.cooldownAtCleanup = context.getShip().getSystem().getCooldownRemaining();
//                diableavionics_drift_static.cleanUpDelta = context.getTimePassedIngame();
//                diableavionics_drift_static.eventHold = context.getEventManager().addEvent(new RTS_Event() {
//                    ShipAPI ship = context.getShip();
//
//                    @Override
//                    public boolean shouldExecute(Object state) {
//                        return(!this.ship.getSystem().isActive());
//                    }
//
//                    @Override
//                    public void run() {
//                        this.ship.getSystem().setCooldownRemaining(MathUtils.clamp(
//                                2f - this.ship.getSystem().getCooldownRemaining(), 0.1f, 2f)
//                        );
//                    }
//                });
//                context.getShip().getSystem().getSpecAPI().setAccelerateAllowed(true);
//                diableavionics_drift_static.init = false;
//            }
//        });
//    }
}

