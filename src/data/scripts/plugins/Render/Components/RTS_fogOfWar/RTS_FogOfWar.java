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

package data.scripts.plugins.Render.Components.RTS_fogOfWar;

import data.scripts.plugins.Render.Components.RTS_miniMap.RTS_MiniMapRenderer;
import data.scripts.plugins.Render.Components.RTS_miniMap.RTS_Minimap;
import data.scripts.plugins.Render.JXDOM.Props.*;
import data.scripts.plugins.Render.JXDOM.RTS_BaseInterface;
import data.scripts.plugins.Render.RTS_Root;
import data.scripts.plugins.Render.RTS_drawManager.RTS_DrawManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RTS_FogOfWar extends RTS_BaseInterface {

    class CAU_muStatState_CAU {
        static RTS_FogOfWarRenderer renderer;
    }

    default Integer fogOfWar (
            Map<String, Object> props,
            RTS_Root root
    ) {
        return (this.finalise(
                props,
                null,
                this.initProps,
                this.render,
                this.everyFrame,
                this.getClass()
        ));
    }

    default void initFogOfWar (RTS_DrawManager drawManager) {
        CAU_muStatState_CAU.renderer = new RTS_FogOfWarRenderer(drawManager);
    }

    static onRender render = new onRender() {
        @Override
        public void render (HashMap<String, Object> props, Map<String, Object> rawProps) {
            /* update() itself no-ops on inert; the flag must flow through everyFrame regardless,
             * because the vanilla draw plugin is engine-attached and has to be told to stand down. */
            CAU_muStatState_CAU.renderer.update(props, rawProps);
        }
    };

    static everyFrameUpdate everyFrame = new everyFrameUpdate() {
        @Override
        public void everyFrame (HashMap<String, Object> props, Map<String, Object> rawProps) {
            boolean disabled = props.get(RTS_P_Inert.ID()) instanceof Boolean && (Boolean)props.get(RTS_P_Inert.ID());
            CAU_muStatState_CAU.renderer.render(
                    (RTS_Root.shipList)rawProps.get(RTS_Root.roNames.shipList),
                    !disabled
            );
        }
    };

    static initProps initProps = new initProps() {
        @Override
        public HashMap<String, RTS_Prop> init () {
            return (RTS_Prop.registerProps(List.of(
                    new RTS_P_Height(),
                    new RTS_P_Width(),
                    new RTS_P_Top(),
                    new RTS_P_Left(),
                    new RTS_P_Inert()
            )));
        }
    };
}
