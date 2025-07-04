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
 * Register an Event to have it do something later on or when certain conditions are met.
 */

public interface RTS_Event {
    /**
     * Overider only when this event could trigger a listener.
     * Listeners of Identical type will only trigger when this event executes.
     * @return Default is a blank string. Beware that blank listeners will match this.
     */
    default String type() { return (""); };
    /**
     * Defines when this event should trigger.
     * @param state The state (HashMap -> String, Object) that this program operates on.
     * @return If true, event will either execute run() or run(waitForListener).
     */
    default boolean shouldExecute(Object state) { return (true); };
    /**
     * Define functionality that should occur provided shouldExecute returned true
     * and this event did not match a listener.
     */
    default void run() {};
    /**
     * Define functionality that should occur provided shouldExecute returned true
     * and this event did match a listener.
     * @param waitForListener set to true to define listener behaviour.
     * @return This value will passed to the matched listener as an argument.
     */
    default HashMap<String, Object> run(boolean waitForListener) { return (null); };
    /**
     * Define whether or not this event should be erased upon activating.
     * @return If true remove this event, else this event will be evaluated on every frame.
     */
    default boolean removeOnCompletion() { return(true); };
}