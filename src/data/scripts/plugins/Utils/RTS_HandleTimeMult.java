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

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableStat;
import data.scripts.plugins.RTSAssist;
import data.scripts.plugins.RTS_ParseInput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RTS_HandleTimeMult extends RTS_StatefulClasses {
    public RTS_HandleTimeMult (Object state) {
        super(state);
    }

    public void update () {
        MutableStat timeMult = Global.getCombatEngine().getTimeMult();
        List<String> hold = new ArrayList<>();
        if ((boolean)this.getState(RTS_ParseInput.stNames.RTSMode) && timeMult.getModifiedValue() != 1f) {
            for (Map.Entry<String, MutableStat.StatMod> entry : timeMult.getMultMods().entrySet())
                if (!this.onWhiteList(entry.getKey(), entry.getValue()))
                    hold.add(entry.getKey());
        }
        for (String entry : hold)
            timeMult.unmodify(entry);
    }

    private boolean onWhiteList (String multID, MutableStat.StatMod mod) {
        if (multID.contains("SU_SpeedUpEveryFrame"))
            return (true);
        return (false);
    }
}
