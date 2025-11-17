package API;

import data.scripts.plugins.BlockedSystems.RTS_BlockSystemPlugin;
import data.scripts.plugins.BlockedSystems.RTS_CompatibilityTools;
import data.scripts.plugins.Utils.RTS_Context;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.List;

/**
 * A collection of methods for adding compatibilty with RTSAssist to your mod.
 */
public class RTS_API {

    /**
     * Register a new system modifier. Refer to readme for help on how set this up correctly.
     * @param plugin Class instance found in API.RTS_API.
     * @return String ID used for deregisterShipSystemModifier.
     */
    public static String registerShipSystemModifier (RTS_SystemWrapper plugin) {
        return (RTS_API_SystemBehaviour.registerModifier(plugin));
    }

    /**
     * Remove a created system modifier.
     * @param ID Return value of registerShipSystemModifier.
     */
    public static void deregisterShipSystemModifier (String ID) {
        RTS_API_SystemBehaviour.unregisterSystemModifier(ID);
    }

    /**
     * Used Internally. Returns a new hashmap of created system modfiers added through the API.
     */
    public static HashMap<String, RTS_BlockSystemPlugin> getSystemModifiers () {
        return (RTS_API_SystemBehaviour.getSystemModifiers());
    }

    /**
     * Broadside modifiers are only used when the ship has an assignment that has an assigned
     * enemy.
     * Register a new broadside modifer. Two configurations: 1.) x set to -180 >= x =< 180.
     * Y set to > 360. In this configuration the ship will always try to match its broadside
     * angle to x with negative vals facing its left side to the enemy. 2.) x set to 0 >= x <= 180.
     * y set 0 >= y >= 180. In this configuration the ship will decide, based on arc length, which is
     * the easier side to broadside to and try and match it broadsides angle to x. In the future
     * x will be used for left facing and y for right but not right now but still feel free
     * to set it in such a way that it will be used.
     * @param BaseHullId Return value of ShipAPI.getHullSpec().getBaseHullId().
     * @param modifier See description
     */
    public static void registerBroadsideModifier (String BaseHullId, Vector2f modifier) {
        RTS_API_BroadsideBehaviour.registerBroadsideModifer(BaseHullId, modifier);
    }

    /**
     * Remove a registered broadside modifier. Not neccesary if you are simply trying to replace an
     * existing moodifier.
     * @param BaseHullId Return value of ShipAPI.getHullSpec().getBaseHullId().
     */
    public static void deregisterBroadsideModifier (String BaseHullId) {
        RTS_API_BroadsideBehaviour.deregisterBroadsideModifer(BaseHullId);
    }

    /**
     * Used Internally. Returns a new hashmap of created broadside modfiers added through the API.
     */
    public static HashMap<String, Vector2f> getBroadsideModifiers () {
        return (RTS_API_BroadsideBehaviour.getBroadsideModifiers());
    }

    public static class RTS_ContextWrapper extends RTS_Context {
        public RTS_ContextWrapper (RTS_Context context) {
            super(context);
        }
    }

    public interface RTS_SystemWrapper extends RTS_CompatibilityTools {
        String getSystemName ();
        boolean blockOnNextFrame (RTS_ContextWrapper context);
    }
}