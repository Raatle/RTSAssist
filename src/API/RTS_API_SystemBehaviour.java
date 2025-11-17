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

package API;

import data.scripts.plugins.BlockedSystems.RTS_BlockSystemPlugin;
import data.scripts.plugins.Utils.RTS_Context;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

class RTS_API_SystemBehaviour {

    private static HashMap<String, RTS_BlockSystemPlugin> blockedSystems = new HashMap<>();

    protected static String registerModifier (RTS_API.RTS_SystemWrapper plugin) {
        RTS_BlockSystemPlugin hold = new RTS_BlockSystemPlugin() {
            RTS_API.RTS_ContextWrapper wrappedContext = null;
            boolean flag = false;
            String error = null;

            @Override
            public String getSystemName() {
                try {
                    return (plugin.getSystemName());
                } catch (Exception e) {
                    return ("");
                }
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                this.wrappedContext = new RTS_API.RTS_ContextWrapper(context);
                try {
                    this.flag = plugin.blockOnNextFrame(this.wrappedContext);
                } catch (Exception e) {
                    if (error == null) {
                        this.error =        "*  *  *  *  *"
                                   + "\n" + "* RTSAssist Handled Exception."
                                   + "\n" + "* Message: " + e.getLocalizedMessage()
                                   + "\n" + "* Line #: " + e.getStackTrace()[0].getLineNumber()
                                   + "\n" + "* Affected system: " + plugin.getSystemName()
                                   + "\n" + "*  *  *  *  *";
                        System.out.println(error);
                    }
                }
                return (flag);
            }
        };

        if (RTS_API_SystemBehaviour.blockedSystems.containsKey(hold.getSystemName()))
            return (null);
        String ID = UUID.randomUUID().toString();
        RTS_API_SystemBehaviour.blockedSystems.put(ID, hold);
        return (ID);
    }

    protected static void unregisterSystemModifier (String ID) {
        RTS_API_SystemBehaviour.blockedSystems.remove(ID);
    }

    protected static HashMap<String, RTS_BlockSystemPlugin> getSystemModifiers () {
        HashMap <String, RTS_BlockSystemPlugin> hold = new HashMap<>();
        for (Map.Entry<String, RTS_BlockSystemPlugin> entry : RTS_API_SystemBehaviour.blockedSystems.entrySet())
            hold.put(entry.getValue().getSystemName(), entry.getValue());
        return (hold);
    }
}