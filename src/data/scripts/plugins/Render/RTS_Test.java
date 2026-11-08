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

package data.scripts.plugins.Render;

import data.scripts.plugins.Render.JXDOM.Div.RTS_Div;

import java.awt.*;

public class RTS_Test implements RTS_Div {
    public RTS_Test() {
    }

    public void render () {
        Point test = MouseInfo.getPointerInfo().getLocation();
        Float testVal = ((Double)test.getX()).floatValue();



/* Margin */

//        Div(Map.of(
//                "bottom", 50f,
//                "right", 50f,
//                "height", 500f,
//                "width", 500f
//        ), Collections.singletonList(
//                Div(Map.of(
//                        "margin", 30f,
//                        "height", 100f,
//                        "width", 100f
//                ), null)
//        ));

/* Bottom, Right, modifierProps */

//        Div(Map.of(
//                "bottom", 50f,
//                "right", 50f,
//                "height", 500f,
//                "width", 500f
//        ), Collections.singletonList(
//                Div(Map.of(
//                        "bottom", 100f,
//                        "right", 100f,
//                        "height", 100f,
//                        "width", 100f
//                ), null)
//        ));

/* Mouse */

//        Div(Map.of(
//                "top", 50f,
//                "left", 50f,
//                "width",200f,
//                "height",200f,
//                "onMouseEnter", (RTS_Prop.propEvent)() -> System.out.println("mouse entered"),
//                "onMouseLeave", (RTS_Prop.propEvent)() -> System.out.println("mouse left"),
//                "onWheel", (RTS_P_OnWheel.wheelEvent)(val) -> System.out.println(val)
//                ),
//                null
//        );
//
//        Div(Map.of(
//                "width",200f,
//                "height",200f,
//                "onClick", (RTS_Prop.propEvent)() -> System.out.println("onClick"),
//                "onLoad", (RTS_Prop.propEvent)() -> System.out.println("the element was loaded"),
//                "onMouseMove", (RTS_Prop.propEvent)() -> System.out.println("the mouse is moving")
//                ),
//                null
//        );

/* Keys */

//        Div(Map.of(
//                "top", 50f,
//                "left", 50f,
//                "height", 200f,
//                "width", 500f,
//                "onKeyDown", new RTS_P_OnKeyDown.onKeyDownEvent() {
//                    @Override
//                    public void onTrigger() {
//                        System.out.println("you pressed a key down");
//                    }
//
//                    @Override
//                    public int specifyKey() {
//                        return (Keyboard.KEY_M);
//                    }
//                },
//                "onKeyUp", new RTS_P_OnkeyUp.onKeyUpEvent() {
//                    @Override
//                    public void onTrigger() {
//                        System.out.println("you released a key");
//                    }
//
//                    @Override
//                    public int specifyKey() {
//                        return (Keyboard.KEY_M);
//                    }
//                }
//        ), null);

/* Drag */

//        Div(Map.of(
//                        "top", 50f,
//                        "left", 50f,
//                        "width",500f,
//                        "height",500f,
//                        "onDrag", (RTS_Prop.propEvent)() -> System.out.println("We are dragging Big Box"),
//                        "onDragStart", (RTS_Prop.propEvent)() -> System.out.println("drag big start"),
//                        "onDragEnd", (RTS_Prop.propEvent)() -> System.out.println("drag big end"),
//                        "onDragLeave", (RTS_Prop.propEvent)() -> System.out.println("an element was dragged out of me")
//                ),
//                Collections.singletonList(
//                        Div(Map.of(
//                                "top", 20f,
//                                "left", 20f,
//                                "width",200f,
//                                "height",200f,
//                                "onDrag", (RTS_Prop.propEvent)() -> System.out.println("We are dragging Small Box"),
//                                "onDragStart", (RTS_Prop.propEvent)() -> System.out.println("drag small start"),
//                                "onDragEnd", (RTS_Prop.propEvent)() -> System.out.println("drag small end"),
//                                "onDragEnter", (RTS_Prop.propEvent)() -> System.out.println("an element was dragged into me")
//                        ), null
//                ))
//        );

//        Div(Map.of("top", 0f, "left", 400f, "height", 300f, "width", 600f,
//                "onHover", new RTS_Prop.propEvent() {
//                        @Override
//                        public void onTrigger() {
//                            System.out.println("yes");
//                        }
//                }), null);

//        Div(Map.of("width", 25f), Arrays.asList(
//                Div(null, Arrays.asList(
//                        Div(Map.of("width", testVal), null)
//                ))
//        ));
//
//        Div(Map.of("width", 25f), Arrays.asList(
//                Div(null, Arrays.asList(
//                        Div(Map.of("width", 22f), null),
//                        Div(null, null),
//                        Div(Map.of("width", testVal), Arrays.asList(
//                                Div(null, null),
//                                Div(Map.of("width", 33f), null)
//                        ))
//                ))
//        ));
//
//        Div(Map.of("width", testVal), null);
//
//        Div(Map.of("width", testVal), Collections.singletonList(
//                Div(null, null)
//        ));
//
//        Div(null, Collections.singletonList(
//                Div(Map.of("width", testVal), null)
//        ));

//        HashMap<String, Object> params = new HashMap<>();
//        params.put("width", 400f);
//        params.put("height", 400f);
//        params.put(RTS_ComponentManager.suppressRender, true);
//        Div(params, null);
    }
}
