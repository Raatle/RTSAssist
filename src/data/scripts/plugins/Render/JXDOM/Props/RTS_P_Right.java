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
import java.util.List;
import java.util.Map;

public class RTS_P_Right extends RTS_ModifierProp {

    public RTS_P_Right() { super("right"); }
    
    @Override
    public List<String> getNecSiblingProps() {
        return (List.of(
                RTS_P_Left.ID(),
                RTS_P_Width.ID()
        ));
    }
    
    @Override
    public void modifySiblingProps(HashMap<String, Object> processedProps, Map<String, Object> rawProps, RTS_Node callingNode) {
        if (rawProps.containsKey(RTS_P_Left.ID()))
            throw new RuntimeException("JXDOM: Dom elements cannot have both a >left< and >right< prop");

        HashMap<String, Object> processedParentProps = callingNode.getParentsProcessedProps();
        this.left = processedParentProps == null
                || processedParentProps.get(RTS_P_Left.ID()) == null
                ? 0f
                : (float)processedParentProps.get(RTS_P_Left.ID());
        this.bigWidth = processedParentProps == null
                || processedParentProps.get(RTS_P_Width.ID()) == null
                ? ((Integer)RTS_Prop.getEventResult(RTS_PropEvents.getdisplayWidth)).floatValue()
                : (float)processedParentProps.get(RTS_P_Width.ID());
        this.smallWidth = (float)processedProps.get(RTS_P_Width.ID());
        this.mod = (float)rawProps.get(RTS_P_Right.ID());

        this.left = this.left + this.bigWidth - (this.mod + this.smallWidth);
        processedProps.put(RTS_P_Left.ID(), this.left);
    }
    float left;
    float bigWidth;
    float smallWidth;
    float mod;
    
    @Override
    public Integer getPriority() {
        return (20);
    }
    
    @Override
    public boolean equals (Object newVal, Object oldVal) {
        return (newVal.equals(oldVal));
    }
    
    public static String ID () {
        return ("right");
    }
}