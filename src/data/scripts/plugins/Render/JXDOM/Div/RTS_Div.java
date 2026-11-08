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

package data.scripts.plugins.Render.JXDOM.Div;

import data.scripts.plugins.Render.JXDOM.Props.*;
import data.scripts.plugins.Render.JXDOM.RTS_BaseInterface;
import data.scripts.plugins.Render.RTS_Root;
import data.scripts.plugins.Utils.RTS_Draw;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RTS_Div extends RTS_BaseInterface {

    default Integer div (Map<String, Object> props) {
        return (this.div(props, (List<Integer>)null));
    }
    default Integer div (Map<String, Object> props, Integer child) {
        return (this.div(props, Collections.singletonList(child)));
    }
    default Integer div (Map<String, Object> props, List<Integer> children) {
        return (this.finalise(
                props,
                children,
                this.initProps,
                this.render,
                this.update,
                this.getClass()
        ));
    }

    everyFrameUpdate update = (props, rawProps) -> {
        if (rawProps == null)
            return;
        if (rawProps.containsKey("debug") && (boolean)rawProps.get("debug"))
            RTS_Div.drawSquare(props);
    };

    onRender render = null;

    initProps initProps = () -> (RTS_Prop.registerProps(RTS_Div.getProps()));

    private static List<RTS_Prop> getProps () {
        return (
                List.of(
                        new RTS_P_Top(),
                        new RTS_P_Left(),
                        new RTS_P_Width(),
                        new RTS_P_Height(),
                        new RTS_P_OnHover(),
                        new RTS_P_OnClick(),
                        new RTS_P_OnRightClick(),
                        new RTS_P_Hidden(),
                        new RTS_P_Muted(),
                        new RTS_P_Inert(),
                        new RTS_P_OnDrag(),
                        new RTS_P_OnDragStart(),
                        new RTS_P_OnDragEnd(),
                        new RTS_P_OnDragEnter(),
                        new RTS_P_OnDragLeave(),
                        new RTS_P_OnKeyDown(),
                        new RTS_P_OnkeyUp(),
                        new RTS_P_OnLoad(),
                        new RTS_P_OnMouseMove(),
                        new RTS_P_OnMouseEnter(),
                        new RTS_P_OnMouseLeave(),
                        new RTS_P_OnWheel(),
                        new RTS_P_Bottom(),
                        new RTS_P_Right(),
                        new RTS_P_Margin(),
                        new RTS_P_MarginLeft(),
                        new RTS_P_MarginRight(),
                        new RTS_P_MarginTop(),
                        new RTS_P_MarginBottom(),
                        new RTS_P_MaxHeight(),
                        new RTS_P_MaxWidth(),
                        new RTS_P_MinHeight(),
                        new RTS_P_MinWidth()
                )
        );
    }

    private static void drawSquare(HashMap<String, Object> props) {
        Vector2f leftTop = new Vector2f((float)props.get("left"), ((float)props.get("top") - RTS_Root.screenDim.getY()) * -1f);
        Vector2f widthHeight = new Vector2f((float)props.get("width"), (float)props.get("height") * -1f);
        RTS_Draw temp = new RTS_Draw(null);
        temp.open();
        GL11.glColor4ub((byte)155,(byte)189, (byte)0, (byte)150);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex2d(
                ((Float)(leftTop.getX())).doubleValue(),
                ((Float)(leftTop.getY())).doubleValue()
        );
        GL11.glVertex2d(
                ((Float)(leftTop.getX())).doubleValue(),
                ((Float)((Float)leftTop.getY() + (Float)widthHeight.getY())).doubleValue()
        );
        GL11.glVertex2d(
                ((Float)((Float)leftTop.getX() + widthHeight.getX())).doubleValue(),
                ((Float)((Float)leftTop.getY() + (Float)widthHeight.getY())).doubleValue()
        );
        GL11.glVertex2d(
                ((Float)((Float)leftTop.getX() + widthHeight.getX())).doubleValue(),
                ((Float)(leftTop.getY())).doubleValue()
        );
        GL11.glVertex2d(
                ((Float)(leftTop.getX())).doubleValue(),
                ((Float)(leftTop.getY())).doubleValue()
        );
        GL11.glEnd();
        temp.close();
    }
}