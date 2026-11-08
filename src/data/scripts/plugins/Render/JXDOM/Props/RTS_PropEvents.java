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

import net.java.games.input.Component;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Controller;

import java.util.HashMap;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;


public class RTS_PropEvents {
    static {
        RTS_PropEvents.getMouseController();
    }

    private static Controller mouseController;

    private static void getMouseController () {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        for (Controller controller : controllers) {
            if (controller.getType() == Controller.Type.MOUSE) {
                RTS_PropEvents.mouseController = controller;
                break;
            }
        }
    }

    protected static RTS_Prop.eventFunction pollMouse = new RTS_Prop.eventFunction() {
        @Override
        public String getID() {
            return "pollMouse";
        }

        @Override
        public Object getEventResult() {
            RTS_PropEvents.mouseController.poll();
            return null;
        }
    };

    protected static RTS_Prop.eventFunction getMouseWheel = new RTS_Prop.eventFunction() {
        @Override
        public String getID() {
            return "mouseWheel";
        }

        @Override
        public Object getEventResult() {
            RTS_Prop.getEventResult(RTS_PropEvents.pollMouse);
            return (RTS_PropEvents.mouseController.getComponent(Component.Identifier.Axis.Z).getPollData());
        }
    };

    protected static RTS_Prop.eventFunction getMouseX = new RTS_Prop.eventFunction() {
        @Override
        public String getID() {
            return ("mouseX");
        }
        @Override
        public Object getEventResult () {
            return (Mouse.getX());
        }
    };

    protected static RTS_Prop.eventFunction getMouseY = new RTS_Prop.eventFunction() {
        @Override
        public String getID() {
            return ("mouseY");
        }
        @Override
        public Object getEventResult () {
            return (Mouse.getY());
        }
    };

    protected static RTS_Prop.eventFunction getMouseVec = new RTS_Prop.eventFunction() {
        @Override
        public String getID() {
            return "mouseVec";
        }

        @Override
        public Object getEventResult() {
            return (new Vector2f(
                    ((Integer)RTS_Prop.getEventResult(RTS_PropEvents.getMouseX)).floatValue(),
                    ((Integer)(((Integer)RTS_Prop.getEventResult(RTS_PropEvents.getMouseY)
                            - (Integer)RTS_Prop.getEventResult(RTS_PropEvents.getdisplayHeight)) * -1)).floatValue()
            ));
        }
    };

    protected static boolean isMouseOver (HashMap<String, Object> props) {
        if (props == null)
            throw new RuntimeException("JXDOM: Call setProps each time before calling isMouseOver");
        Vector2f mouseLoc = (Vector2f)RTS_Prop.getEventResult(RTS_PropEvents.getMouseVec);
        Vector2f propLoc =  new Vector2f((Float)props.get(RTS_P_Left.ID()), (Float)props.get(RTS_P_Top.ID()));
        Vector2f dimensions = new Vector2f((Float)props.get(RTS_P_Width.ID()), (Float)props.get(RTS_P_Height.ID()));
        return (
                (mouseLoc.getX() >= propLoc.getX() && mouseLoc.getX() <= (propLoc.getX() + dimensions.getX()))
                        && (mouseLoc.getY() >= propLoc.getY() && mouseLoc.getY() <= (propLoc.getY() + dimensions.getY()))
        );
    }

    protected static RTS_Prop.eventFunction getdisplayHeight = new RTS_Prop.eventFunction() {
        @Override
        public String getID() {
            return ("displayHeight");
        }
        @Override
        public Object getEventResult () {
            return (Display.getHeight());
        }
    };
    protected static RTS_Prop.eventFunction getdisplayWidth = new RTS_Prop.eventFunction() {
        @Override
        public String getID() {
            return ("displayWidth");
        }
        @Override
        public Object getEventResult () {
            return (Display.getWidth());
        }
    };


    protected static RTS_Prop.eventFunction getLMButtonState = new RTS_Prop.eventFunction() {
        @Override
        public String getID() {
            return ("LMBState");
        }
        @Override
        public Object getEventResult () {
            return (Mouse.isButtonDown(0));
        }
    };

    protected static RTS_Prop.eventFunction getRMButtonState = new RTS_Prop.eventFunction() {
        @Override
        public String getID() {
            return ("RMBState");
        }
        @Override
        public Object getEventResult () {
            return (Mouse.isButtonDown(1));
        }
    };
}
