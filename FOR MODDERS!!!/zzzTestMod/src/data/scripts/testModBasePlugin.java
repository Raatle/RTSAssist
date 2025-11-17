package data.scripts;

import API.RTS_API;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.lwjgl.util.vector.Vector2f;


/* A standard mod plugin */
public class testModBasePlugin extends BaseModPlugin {

    /* Method belonging to BaseModPlugin that we override*/
    @Override
    public void onApplicationLoad() {
        /* Check to see if RTSAssist is enabled before using RTSAssist API */
        if (Global.getSettings().getModManager().isModEnabled("RTSAssist"))
            this.addRTSAssistSupport();
    }

    private void addRTSAssistSupport () {

        /* RTS_API.registerShipSystemModifier() can be called anywhere. RTSAssist will check for added
         * system modifiers on the first frame of combat. */

        /* A basic example, a simple script for ships that utilise "burndrive", such as the Dominator.
         * We will force these ships to use their system only when they have been assigned a basic attack
         * assignment. */
        /*------------------------------------------------------------------------------------------------------------*/
        RTS_API.registerShipSystemModifier(new RTS_API.RTS_SystemWrapper() {
            /* See %%%% for a cautionary tale. Not used here */
            String example = null;

            /* Return the name of the ships system i.e., the return value of ShipAPI.getSystem().getId(). */
            @Override
            public String getSystemName() {
                return ("burndrive");
            }

            /* Every frame, if a ship has an assignment and a system with matching ID, this we be called for that ship.
             * If it returns true, the ship will be unable to use its system. All exceptions handled by RTSAssist and
             * written to console. */
            @Override
            public boolean blockOnNextFrame(RTS_API.RTS_ContextWrapper context) {
                                                    /* This must always be called. */
                this.init(context);
                                                    /* We create a flag which we will change depending on how we want
                                                     * the system to behave. We want to block the system by default so
                                                     * we start by setting the flag to true. */
                boolean flag = true;
                                                    /* context hooks into the RTSAssist core and lets us get information
                                                     * from it. Here we check to see if the ship in question has been
                                                     * given a direct attack assignment. If true we set the flag to
                                                     * false. */
                if (context.getTargetEnemy() != null)
                    flag = false;
                                                    /* If we return true, the ship will not use its system. */
                return (flag);
            }
        });
        /*------------------------------------------------------------------------------------------------------------*/
        /* %%% CAUTION: This variable exists in a class instance. This same instance is used for
         * all ships that share the same ID as this. Therefore one must be take care that any class
         * variables are calculated for each ship that utilises this instance and on each frame. */


        /* A more involved example, for ships that use microburn. We want these ships to only use
         * their system when they are a certain distance from where they are trying to go and are
         * also facing that point. Additionally we want these ships to suspend system use for a
         * few seconds when resuming default behavior and also to use their systems on cooldown.
         * Lastly ships shouldn't use their ships in the first few seconds of combat. */
        /*------------------------------------------------------------------------------------------------------------*/
        RTS_API.registerShipSystemModifier(new RTS_API.RTS_SystemWrapper() {
            float variantCalc;

            @Override
            public String getSystemName() {
                return "microburn";
            }

            @Override
            public boolean blockOnNextFrame(RTS_API.RTS_ContextWrapper context) {
                this.init(context);
                                                    /* A distance based on ship shield radius */
                this.variantCalc = context.getShip().getShieldRadiusEvenIfNoShield() * 2.2f;
                boolean flag = false;
                                                    /* For 3 seconds, when a ship no longer has an assignment, system
                                                     * use will be blocked. This gives the player a small window whereby
                                                     * they can reissue assignments before the the ship immediately uses
                                                     * its system. */
                this.addCoolDownOnCleanup();
                                                    /* Returns true when the ship is within a given distance of its
                                                     * calculated destination i.e., where it is trying to go after
                                                     * pathfinding calculations etc. */
                if (this.withinDistanceOfModDest(variantCalc))
                    flag = true;
                                                    /* Returns true when the ship is not facing its calculated
                                                     * destination i.e., where it is trying to go after pathfinding
                                                     * calculations etc. */
                if (this.notFacingModDest(20f))
                    flag = true;
                                                    /* Prevents ships from using its system in the first 3 real time
                                                     * seconds of the game */
                if (context.getTimePassedIngame() < 3f)
                    flag = true;
                                                    /* If we return false, the ship wont necessarily use its system,
                                                     * unless the AI chooses to. Included in this class are as set of
                                                     * utils. UseOnCooldown takes a boolean and will force the ship to
                                                     * use its system if the boolean is false and it is able to do so. */
                this.useOnCoolDown(flag);
                return (flag);
            }
        });
        /*------------------------------------------------------------------------------------------------------------*/

        /* In the RTSAssist source files, in Starsector\mods\RTSAssist\src\data\scripts\plugins\BlockedSystems,
         * implementations used for many other mods can be found. The API is different but blockOnNextFrame
         * methods will be identical in implementation. Feel free to copy and paste the code found inside these
         * methods. */


        /* Here we add a broadside modifier for the Odyssey. This will cause it to always face the left side of
         * its hull towards the enemy when given an assignment that has an enemy.
         * Straight forward. See registerBroadsideModifier doc for more how to. */
        /*------------------------------------------------------------------------------------------------------------*/
        RTS_API.registerBroadsideModifier("odyssey", new Vector2f(-90, 361));
        /*------------------------------------------------------------------------------------------------------------*/

        /* Here we add a broadside modifier for the Conquest. This causes the ship to broadside with the side that is
         * most suitable. */
        /*------------------------------------------------------------------------------------------------------------*/
        RTS_API.registerBroadsideModifier("conquest", new Vector2f(-30, 30));
        /*------------------------------------------------------------------------------------------------------------*/
    }
}