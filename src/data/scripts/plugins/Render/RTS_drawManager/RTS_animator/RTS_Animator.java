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

package data.scripts.plugins.Render.RTS_drawManager.RTS_animator;

import data.scripts.plugins.RTSAssist;
import data.scripts.plugins.Utils.RTS_StatefulClasses;

import java.util.*;

public class RTS_Animator extends RTS_StatefulClasses {

    public RTS_Animator (Object state) {
        super(state);
    }

    private HashMap<String, RTS_AnimationController> animationQueue = new HashMap<>();
    private List<String> deletionQueue = new ArrayList<>();

    public interface curve {
        float getCalculatedDelta (float delta);
    }

    public String queueAnimation (RTS_AnimationController animation) {
        if (animation.ID != null) {
            animation.reset();
            animation.setPlay(true);
            return (animation.ID);
        }
        String ID = UUID.randomUUID().toString();
        animation.ID = ID;
        this.animationQueue.put(ID, animation);
        return (ID);
    }

    public void removeAnimation (String animationID) {
        this.animationQueue.get(animationID).ID = null;
        this.animationQueue.put(animationID, null);
    }

    public void advance () {
        for (Map.Entry<String, RTS_AnimationController> entry : this.animationQueue.entrySet()) {
            if (entry.getValue() == null)
                continue;
            entry.getValue().advance(
                    (float)(this.getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.deltaFrame)))
            );
            if (entry.getValue().delta > 100f)
                this.deletionQueue.add(entry.getKey());
        }
        for (String animationID : this.deletionQueue) {
            this.animationQueue.get(animationID).ID = null;
            this.animationQueue.put(animationID, null);
        }
        this.deletionQueue.clear();
    }
}
