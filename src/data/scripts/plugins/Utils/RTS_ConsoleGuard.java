/*
  **********************************************************************************************************
  * RTSAssist version 0.2.17exp
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

package data.scripts.plugins.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/* While a Console Commands overlay is open its text field shares the same letters as the mod's
 * hotkeys, so RTS input parsing must stand down until it is closed again. Detection is done
 * reflectively so the mod keeps working with or without Console Commands installed, and across
 * its v2 overlay and legacy overlay implementations. Both set their instance field to null on
 * close, which makes "instance != null" a reliable open check. */
public class RTS_ConsoleGuard {

    private static boolean resolved = false;
    private static boolean consolePresent = false;
    private static Object v2Companion = null;
    private static Method v2GetInstance = null;
    private static Field legacyOverlay = null;

    public static boolean isConsoleOpen () {
        if (!resolved)
            resolve();
        if (!consolePresent)
            return (false);
        try {
            if (v2GetInstance != null && v2GetInstance.invoke(v2Companion) != null)
                return (true);
            if (legacyOverlay != null && legacyOverlay.get(null) != null)
                return (true);
        }
        catch (Exception e) {
            /* Reflection failure must never take hotkeys down permanently. */
        }
        return (false);
    }

    private static void resolve () {
        resolved = true;
        try {
            Class<?> panel = Class.forName(
                    "org.lazywizard.console.overlay.v2.panels.ConsoleOverlayPanel"
            );
            v2Companion = panel.getField("Companion").get(null);
            v2GetInstance = v2Companion.getClass().getMethod("getInstance");
        }
        catch (Throwable t) {
            /* v2 overlay not present in this Console Commands build. */
        }
        try {
            Class<?> legacy = Class.forName(
                    "org.lazywizard.console.overlay.legacy.LegacyConsoleOverlay"
            );
            legacyOverlay = legacy.getDeclaredField("overlay");
            legacyOverlay.setAccessible(true);
        }
        catch (Throwable t) {
            /* legacy overlay not present in this Console Commands build. */
        }
        consolePresent = (v2GetInstance != null || legacyOverlay != null);
    }
}
