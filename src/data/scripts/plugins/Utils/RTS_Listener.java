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

import java.util.HashMap;

/**
 * Register a Listener to have it do something when an event of matching type runs.
 */

public interface RTS_Listener {
    /**
     * Override to define listener type. Beware assigning a blank string type.
     * Rather register an event.
     * @return Any event that has a matching type will call this listeners >run< method.
     */
    String type();
    /**
     * Define functionality that should occur provided a matching event has run.
     * @param e matching events will pass e to this function. This allows the event
     *          to pass some relevent information to the listener.
     */
    void run(HashMap<String, Object> e);
    /**
     * Define whether or not this listener should be erased upon activating.
     * @return If true remove this listner, else this event will triggered evertime its
     * corresponding event triggers.
     */
    default boolean removeOnCompletion() { return(true); };
}