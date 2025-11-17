package API;

import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;

public class RTS_API_BroadsideBehaviour {

    private static HashMap<String, Vector2f> broadsideMods = new HashMap<>();

    protected static void registerBroadsideModifer (String BaseHullId, Vector2f modifier) {
        RTS_API_BroadsideBehaviour.broadsideMods.put(BaseHullId, modifier);
    }

    protected static void deregisterBroadsideModifer (String BaseHullId) {
        RTS_API_BroadsideBehaviour.broadsideMods.remove(BaseHullId);
    }

    protected static HashMap<String, Vector2f> getBroadsideModifiers () {
        return (RTS_API_BroadsideBehaviour.broadsideMods);
    }
}
