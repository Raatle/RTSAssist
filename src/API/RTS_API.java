package API;

import data.scripts.plugins.BlockedSystems.RTS_BlockSystemPlugin;

import java.util.HashMap;

public class RTS_API {

    public static void registerShipSystemModifier (RTS_BlockSystemPlugin plugin) {
        RTS_API_SystemBehaviour.registerModifier(plugin);
    }

    public static HashMap<String, RTS_BlockSystemPlugin> getSystemModifiers () {
        return (RTS_API_SystemBehaviour.getModifiers());
    }
}