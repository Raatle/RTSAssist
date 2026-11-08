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
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RTS_P_OnKeyDown extends RTS_EventProp {

    public RTS_P_OnKeyDown() {
        super("onKeyDown");
    }

    @Override
    public List<String> getNecSiblingProps() {
        return (null); //(List.of());
    }

    //this is trickier than you think.. consider multipile simultaneous key presses
    @Override
    public boolean triggerEventCallBack(HashMap<String, Object> processedProps, Map<String, Object> rawProps, RTS_Node callingNode) {
        try {
            this.key = ((RTS_P_OnKeyDown.onKeyDownEvent)rawProps.get("onKeyDown")).specifyKey();
        } catch (RuntimeException e) {
            throw new RuntimeException("JXDOM: onKeyDown event must override RTS_P_OnKeyDown.onKeyDownEvent.specifyKey.");
        }
        if (Keyboard.isKeyDown(this.key)) {
            if (!this.isKeyDown) {
                this.isKeyDown = true;
                return (true);
            }
        }
        else
            this.isKeyDown = false;
        return (false);
    }
    Integer key = null;
    boolean isKeyDown = false;

    public static String ID() {
        return ("onKeyDown");
    }

    public interface onKeyDownEvent extends RTS_Prop.propEvent {
        int specifyKey ();
    }
}