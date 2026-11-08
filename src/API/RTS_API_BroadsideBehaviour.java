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
