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

package data.scripts.plugins.BlockedSystems;

import data.scripts.plugins.Utils.RTS_Context;

public interface RTS_BlockSystemPlugin extends RTS_CompatibilityTools {
    String getSystemName ();
    boolean blockOnNextFrame (RTS_Context context);
    default boolean execBlockOnNextFrame (RTS_Context context) {
        if (queueForEveryFrame.containsKey(context.getShip()))
            for (everyFrame update : queueForEveryFrame.get(context.getShip())) {
                update.run();
            }
        return (this.blockOnNextFrame(context));
    }
    default void cleanUp (RTS_Context context) {};
    default void execCleanUp (RTS_Context context) {
        for (atCleanup clean : queueForCleaning.get(context.getShip())) {
            clean.run();
        }
        this.cleanUp(context);
        queueForCleaning.get(context.getShip()).clear();
        queueForEveryFrame.get(context.getShip()).clear();
    }
}