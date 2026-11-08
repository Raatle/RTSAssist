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

import data.scripts.plugins.Render.JXDOM.RTS_Node;

import java.util.HashMap;
import java.util.Map;

public abstract class RTS_EventProp extends RTS_Prop {

    public RTS_EventProp(String ID) {
        super(propType.event, null);
        this.ID = ID;
    }

    private String ID;

    @Override
    public Object consolidateWithParentProps(HashMap<String, Object> processedParentProps, Object self) {
        return null;
    }

    @Override
    public void modifySiblingProps(HashMap<String, Object> processedProps, Map<String, Object> rawProps, RTS_Node callingNode) {}

    @Override
    public boolean equals (Object newVal, Object oldVal) {
        return (true);
    }

    @Override
    public String getID () {
        return (this.ID);
    }

    @Override
    public Integer getPriority() {
        return (null);
    }
}
