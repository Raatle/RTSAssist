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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RTS_P_OnDragStart extends RTS_EventProp {

    public RTS_P_OnDragStart() {
        super("onDragStart");
    }

    @Override
    public List<String> getNecSiblingProps() {
        return (Collections.singletonList(
                RTS_P_OnDrag.ID()
        ));
    }

    @Override
    public boolean triggerEventCallBack(HashMap<String, Object> processedProps, Map<String, Object> rawProps, RTS_Node callingNode) {
        if (!this.dragHold && ((RTS_P_OnDrag)callingNode.getProps().get(RTS_P_OnDrag.ID())).weAreDrag) {
            this.dragHold = ((RTS_P_OnDrag)callingNode.getProps().get(RTS_P_OnDrag.ID())).weAreDrag;
            return (true);
        }
        this.dragHold = ((RTS_P_OnDrag)callingNode.getProps().get(RTS_P_OnDrag.ID())).weAreDrag;
        return (false);
    }
    boolean dragHold = false;

    public static String ID() {
        return ("onDragStart");
    }
}