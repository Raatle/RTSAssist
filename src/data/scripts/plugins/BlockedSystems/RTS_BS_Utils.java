package data.scripts.plugins.BlockedSystems;

import java.util.HashMap;

public class RTS_BS_Utils {

    private static HashMap<String, RTS_BlockSystemPlugin> blockedSystems = new HashMap<>();

    public static void registerModifier (RTS_BlockSystemPlugin plugin) {
        if (RTS_BS_Utils.blockedSystems.containsKey(plugin.getSystemName()))
            return;
        RTS_BS_Utils.blockedSystems.put(plugin.getSystemName(), plugin);
    }

    public static HashMap<String, RTS_BlockSystemPlugin> getModifiers () {
        return RTS_BS_Utils.blockedSystems;
    }
}
