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

package API;

import data.scripts.plugins.BlockedSystems.RTS_BlockSystemPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class RTS_API_SystemBehaviour {

    private static List<RTS_BlockSystemPlugin> blockedSystems = new ArrayList<>();

    protected static void registerModifier (RTS_BlockSystemPlugin plugin) {
        if (RTS_API_SystemBehaviour.blockedSystems.contains(plugin))
            return;
        RTS_API_SystemBehaviour.blockedSystems.add(plugin);
    }

    protected static HashMap<String, RTS_BlockSystemPlugin> getModifiers () {
        HashMap<String, RTS_BlockSystemPlugin> blockedSystems = new HashMap<>();
        for (RTS_BlockSystemPlugin plugin : RTS_API_SystemBehaviour.blockedSystems)
            blockedSystems.put(plugin.getSystemName(), plugin);
        return blockedSystems;
    }
}