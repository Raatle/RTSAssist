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

public class RTS_P_Margin extends RTS_ModifierProp {

    public RTS_P_Margin() { super("margin"); }
    
    @Override
    public List<String> getNecSiblingProps() {
        return (List.of(
                RTS_P_Height.ID(),
                RTS_P_Width.ID(),
                RTS_P_Top.ID(),
                RTS_P_Left.ID()
        ));
    }
    
    @Override
    public void modifySiblingProps(HashMap<String, Object> processedProps, Map<String, Object> rawProps, RTS_Node callingNode) {
        float mod = (float)rawProps.get(RTS_P_Margin.ID());
        if (!rawProps.containsKey(RTS_P_MarginLeft.ID()))
            RTS_P_Margin.applyMarginLeft(mod, processedProps, callingNode);
        if (!rawProps.containsKey(RTS_P_MarginRight.ID()))
            RTS_P_Margin.applyMarginRight(mod, processedProps, callingNode);
        if (!rawProps.containsKey(RTS_P_MarginTop.ID()))
            RTS_P_Margin.applyMarginTop(mod, processedProps, callingNode);
        if (!rawProps.containsKey(RTS_P_MarginBottom.ID()))
            RTS_P_Margin.applyMarginBottom(mod, processedProps, callingNode);
    }
    
    @Override
    public Integer getPriority() {
        return (50);
    }
    
    @Override
    public boolean equals (Object newVal, Object oldVal) {
        return (newVal.equals(oldVal));
    }
    
    public static String ID () {
        return ("margin");
    }

    public static void applyMarginLeft (float amount, HashMap<String, Object> processedProps, RTS_Node callingNode) {
        HashMap<String, Object> processedParentProps = callingNode.getParentsProcessedProps();
        float parentLeft = processedParentProps == null
                || processedParentProps.get(RTS_P_Left.ID()) == null
                ? 0f
                : (float)processedParentProps.get(RTS_P_Left.ID());
        if ((float)processedProps.get(RTS_P_Left.ID()) < (parentLeft + amount))
            processedProps.put(RTS_P_Left.ID(), parentLeft + amount);
    }

    public static void applyMarginRight (float amount, HashMap<String, Object> processedProps, RTS_Node callingNode) {
        float left = (float)processedProps.get(RTS_P_Left.ID());
        float width = (float)processedProps.get(RTS_P_Width.ID());
        HashMap<String, Object> processedParentProps = callingNode.getParentsProcessedProps();
        float parentWidth = processedParentProps == null
                || processedParentProps.get(RTS_P_Width.ID()) == null
                ? ((Integer)RTS_Prop.getEventResult(RTS_PropEvents.getdisplayWidth)).floatValue()
                : (float)processedParentProps.get(RTS_P_Width.ID());
        float parentLeft = processedParentProps == null
                || processedParentProps.get(RTS_P_Left.ID()) == null
                ? 0f
                : (float)processedParentProps.get(RTS_P_Left.ID());
        if ((left + width + amount) > (parentLeft + parentWidth))
            processedProps.put(
                    RTS_P_Left.ID(),
                    (parentLeft + parentWidth) - (width + amount)
            );
    }

    public static void applyMarginTop (float amount, HashMap<String, Object> processedProps, RTS_Node callingNode) {
        HashMap<String, Object> processedParentProps = callingNode.getParentsProcessedProps();
        float parentTop = processedParentProps == null
                || processedParentProps.get(RTS_P_Top.ID()) == null
                ? 0f
                : (float)processedParentProps.get(RTS_P_Top.ID());
        if ((float)processedProps.get(RTS_P_Top.ID()) < (parentTop + amount))
            processedProps.put(RTS_P_Top.ID(), parentTop + amount);
    }

    public static void applyMarginBottom (float amount, HashMap<String, Object> processedProps, RTS_Node callingNode) {
        float top = (float)processedProps.get(RTS_P_Top.ID());
        float height = (float)processedProps.get(RTS_P_Height.ID());
        HashMap<String, Object> processedParentProps = callingNode.getParentsProcessedProps();
        float parentHeight = processedParentProps == null
                || processedParentProps.get(RTS_P_Height.ID()) == null
                ? ((Integer)RTS_Prop.getEventResult(RTS_PropEvents.getdisplayHeight)).floatValue()
                : (float)processedParentProps.get(RTS_P_Height.ID());
        float parentTop = processedParentProps == null
                || processedParentProps.get(RTS_P_Top.ID()) == null
                ? 0f
                : (float)processedParentProps.get(RTS_P_Top.ID());
        if ((top + height + amount) > (parentTop + parentHeight))
            processedProps.put(
                    RTS_P_Top.ID(),
                    (parentTop + parentHeight) - (height + amount)
            );
    }
}