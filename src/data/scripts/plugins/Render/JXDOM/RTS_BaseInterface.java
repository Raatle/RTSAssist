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

package data.scripts.plugins.Render.JXDOM;

import data.scripts.plugins.Render.JXDOM.Props.RTS_Prop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RTS_BaseInterface {

    public class internalProps {
        Integer nodeIndex;
        Class<?> nodeID;
        initProps initPropz;
        onRender renderNode;
        everyFrameUpdate everyFrame;
        Map<String, Object> calledProps;
        List<Integer> children;
    }
    public internalProps internalProps = new internalProps();

    public static interface onRender {
        void render (HashMap<String, Object> props, Map<String, Object> rawProps);
    }

    public static interface everyFrameUpdate {
        void everyFrame (HashMap<String, Object> props, Map<String, Object> rawProps);
    }

    public static interface initProps {
        HashMap<String, RTS_Prop> init ();
    }

    public default Integer finalise (
            Map<String, Object> calledProps,
            List<Integer> children,
            initProps initPropz,
            onRender renderNode,
            everyFrameUpdate everyFrame,
            Class<?> nodeID
    ) {
        this.internalProps.children = children;
        this.internalProps.nodeID = nodeID;
        this.internalProps.initPropz = initPropz;
        this.internalProps.renderNode = renderNode;
        this.internalProps.everyFrame = everyFrame;
        this.internalProps.calledProps = calledProps;
        this.internalProps.nodeIndex = RTS_ComponentManager.reserveNode(this.internalProps);
        return (this.internalProps.nodeIndex);
    }
 }
