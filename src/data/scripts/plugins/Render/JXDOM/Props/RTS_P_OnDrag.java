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

import data.scripts.plugins.Render.JXDOM.RTS_ComponentManager;
import data.scripts.plugins.Render.JXDOM.RTS_Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RTS_P_OnDrag extends RTS_EventProp {

    public RTS_P_OnDrag() {
        super("onDrag");
    }

    @Override
    public List<String> getNecSiblingProps() {
        return (List.of(
                RTS_P_Height.ID(),
                RTS_P_Width.ID(),
                RTS_P_Top.ID(),
                RTS_P_Left.ID()
        ));
    }

    private interface dragRequest extends RTS_ComponentManager.postTreeTraversalEvent {
        void setRenderLevel (int renderLevel);
    }

    private dragRequest requestDrag = new dragRequest() {
        int renderLevel = 0;

        @Override
        public void setRenderLevel(int renderLevel) {
            this.renderLevel = renderLevel;
        }

        @Override
        public void run() {
            if (this.renderLevel == RTS_P_OnDrag.deepestDrag) {
                weAreDrag = true;
                RTS_P_OnDrag.dragInProgress = true;
            }
        }
    };

    @Override
    public boolean triggerEventCallBack(HashMap<String, Object> processedProps, Map<String, Object> rawProps, RTS_Node callingNode) {
        this.requestDrag.setRenderLevel(0);
        this.LMBStateHold = LMBState;
        this.LMBState = (boolean)RTS_Prop.getEventResult(RTS_PropEvents.getLMButtonState);
        if (!LMBState) {
            RTS_P_OnDrag.dragInProgress = false;
            this.weAreDrag = false;
            return (false);
        }
        if (RTS_P_OnDrag.dragInProgress)
            return (this.weAreDrag);
        else {
            if (!this.LMBStateHold && RTS_PropEvents.isMouseOver(processedProps)) {
                RTS_P_OnDrag.deepestDrag = RTS_ComponentManager.getRenderLevel();
                this.requestDrag.setRenderLevel(RTS_ComponentManager.getRenderLevel());
                RTS_ComponentManager.registerPoTTEEvent(this.requestDrag);
            }
            return (false);
        }
    }
    boolean LMBStateHold = false;
    boolean LMBState = false;
    static boolean dragInProgress = false;
    static int deepestDrag = 0;
    boolean weAreDrag = false;

    public static String ID() {
        return ("onDrag");
    }
}