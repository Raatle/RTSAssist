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

package data.scripts.plugins.Render.JXDOM.Props;

import java.util.HashMap;

public class RTS_P_Inert extends RTS_ValueProp {

    public RTS_P_Inert() {
        super("inert", null);
    }

    @Override
    public Object consolidateWithParentProps(HashMap<String, Object> processedParentProps, Object self) {
        return (
                self == null
                        ? processedParentProps == null
                                ? false
                                : processedParentProps.getOrDefault(RTS_P_Inert.ID(), false)
                        : self
        );
    }

    @Override
    public boolean equals(Object newVal, Object oldVal) {
        return (newVal.equals(oldVal));
    }

    public static String ID() {
        return ("inert");
    }

    public static Class<?> type() {
        return (Boolean.class);
    }
}

