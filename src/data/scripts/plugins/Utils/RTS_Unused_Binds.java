/****************************************************************************************
 * RTSAssist version 0.1.5
 * Copyright (C) 2025, Raatle

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
 ****************************************************************************************/

package data.scripts.plugins.Utils;

public class RTS_Unused_Binds extends RTS_StatefulClasses {
    public RTS_Unused_Binds(Object state) {
        super(state);
    }

    public void createBinds(RTS_Unused_InputManager inputManager) {
    }
}


//        inputManager.createNewHotkey(
//            "example name",
//            new HashMap<RTSInputManager.inputType, Object>() {{
//                put(RTSInputManager.inputType.KEYPRESS, 'y');
//                put(RTSInputManager.inputType.MODIFIER, 'k');
//            }},
//            new Callable<Integer>() {
//                @Override
//                public Integer call() throws Exception {
//                    >>do something <<
//                    return 0;
//                }
//            },
//            new Callable<Integer>() {
//                @Override
//                public Integer call() throws Exception {
//                    >>do a check<<
//                    return 0;
//                }
//            }
//        );