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

package data.scripts.plugins.Utils;

import com.fs.starfarer.D.A;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import data.scripts.plugins.RTSAssist;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class RTS_SoundsManager extends RTS_StatefulClasses {

    public RTS_SoundsManager (Object state) {
        super(state);
    }

    private Vector2f blankVec = new Vector2f();
    private Vector2f centre;
    private float masterUIVolume;
    private HashMap<String, SoundAPI> soundStore = new HashMap<>();

    public void update () {
        this.masterUIVolume = 2f * (float)this.getDeepState(Arrays.asList(
                RTSAssist.stNames.config,
                RTSAssist.coNames.UICommandVolume)
        ) / 10f;
        this.centre = ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getViewport().getCenter();
        /* Why: any time the camera moves rapidly, audio will be cut. This fixes it. */
        List<String> queueDelete = new ArrayList<>();
        for (Map.Entry<String, SoundAPI> sound : this.soundStore.entrySet())
            if (sound.getValue() != null) {
                if (sound.getValue().isPlaying())
                    sound.getValue().setLocation(this.centre.getX(), this.centre.getY());
                else
                    queueDelete.add(sound.getKey());
            }
        for (String id : queueDelete)
            this.soundStore.put(id, null);
    }

    public SoundAPI playBasicUISound (String id, float pitch, float volume) {
        SoundAPI newSound = Global.getSoundPlayer().playSound(
                id,
                pitch,
                volume * this.masterUIVolume,
                this.centre,
                this.blankVec
        );
        this.soundStore.put(id, newSound);
        return (newSound);
    }
}
