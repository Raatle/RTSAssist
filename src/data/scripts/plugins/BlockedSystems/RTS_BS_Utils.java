/*
  **********************************************************************************************************
  * RTSAssist version 0.2.04exp
  * Copyright (C) 2025-2026, Raatle

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
  **********************************************************************************************************
 */

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
