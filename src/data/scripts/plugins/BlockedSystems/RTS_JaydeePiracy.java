package data.scripts.plugins.BlockedSystems;

import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.plugins.Utils.RTS_Context;

public class RTS_JaydeePiracy {

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "jdp_lidararray";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                if (context.getTargetEnemy() == null && context.getPriorityEnemy() == null)
                    return (true);
                init(context);
                boolean flag = true;
                for (WeaponAPI set : context.getShip().getUsableWeapons()){
                    if (set.getType().compareTo(WeaponAPI.WeaponType.MISSILE) == 0
                            || set.getOriginalSpec().getPrimaryRoleStr() == null
                            || set.getOriginalSpec().getPrimaryRoleStr().equals("Point Defense")
                            || set.getOriginalSpec().getPrimaryRoleStr().equals("Point Defense (Area)"))
                        continue;
                    if (set.isFiring())
                        flag = false;
                }
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "jdp_temporalteleporter";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                float variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2f;
                boolean flag = false;
                this.addCoolDownOnCleanup();
                if (this.notVectoringToModDest(20f))
                    flag = true;
                if (this.withinDistanceOfModDest(variantCalc))
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
                return "jdp_quickdrawlidararray";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                if (context.getTargetEnemy() == null && context.getPriorityEnemy() == null)
                    return (true);
                init(context);
                boolean flag = true;
                for (WeaponAPI set : context.getShip().getUsableWeapons()){
                    if (set.getType().compareTo(WeaponAPI.WeaponType.MISSILE) == 0
                            || set.getOriginalSpec().getPrimaryRoleStr() == null
                            || set.getOriginalSpec().getPrimaryRoleStr().equals("Point Defense")
                            || set.getOriginalSpec().getPrimaryRoleStr().equals("Point Defense (Area)"))
                        continue;
                    if (set.isFiring())
                        flag = false;
                }
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "jdp_combatmasterlidararray";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                if (context.getTargetEnemy() == null && context.getPriorityEnemy() == null)
                    return (true);
                init(context);
                boolean flag = true;
                for (WeaponAPI set : context.getShip().getUsableWeapons()){
                    if (set.getType().compareTo(WeaponAPI.WeaponType.MISSILE) == 0
                            || set.getOriginalSpec().getPrimaryRoleStr() == null
                            || set.getOriginalSpec().getPrimaryRoleStr().equals("Point Defense")
                            || set.getOriginalSpec().getPrimaryRoleStr().equals("Point Defense (Area)"))
                        continue;
                    if (set.isFiring())
                        flag = false;
                }
                if (this.ifOnStayOn())
                    flag = false;
                this.useOnCoolDown(flag);
                return (flag);
            }
        });
    }
}