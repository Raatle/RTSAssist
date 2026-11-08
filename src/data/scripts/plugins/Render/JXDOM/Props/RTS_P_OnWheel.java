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

public class RTS_P_OnWheel extends RTS_EventProp {

    public RTS_P_OnWheel() {
        super("onWheel");
    }

    @Override
    public List<String> getNecSiblingProps() {
        return (null); //(List.of());

    }

    @Override
    public boolean triggerEventCallBack(HashMap<String, Object> processedProps, Map<String, Object> rawProps, RTS_Node callingNode) {
        this.eventResult = (float)RTS_Prop.getEventResult(RTS_PropEvents.getMouseWheel);
        if (this.eventResult != 0f)
            return (RTS_PropEvents.isMouseOver(processedProps));
        return (false);
    }

    public static String ID() {
        return ("onWheel");
    }

    private static Float eventResult = null ;

    private static float getEventResult () {
        float hold = RTS_P_OnWheel.eventResult;
        RTS_P_OnWheel.eventResult = null;
        return (hold);
    }

    public interface wheelEvent extends RTS_Prop.propEvent {
        void onTrigger (float val);

        @Override
        default void onTrigger(HashMap<String, Object> processedProps, java.util.Map<String, Object> rawProps, RTS_Node callingNode) {
            this.onTrigger(RTS_P_OnWheel.getEventResult());
        }
    }






}