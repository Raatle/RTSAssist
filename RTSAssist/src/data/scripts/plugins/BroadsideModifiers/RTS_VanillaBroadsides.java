/****************************************************************************************
 * RTSAssist version 0.1.0
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

package data.scripts.plugins.BroadsideModifiers;

import java.util.HashMap;

public class RTS_VanillaBroadsides {

    HashMap<String, Float> broadsideData = new HashMap<>();

    public RTS_VanillaBroadsides() {
        this.broadsideData.put("conquest", -30f);
        this.broadsideData.put("odyssey", -20f);
    }

    public HashMap<String, Float> getBroadSideData() {
        return (this.broadsideData);
    }
}