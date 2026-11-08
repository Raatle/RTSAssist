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

import com.fs.starfarer.api.Global;

public class RTS_AnimationController {

    public RTS_AnimationController (float duration) {
        this.duration = duration;
    }

    protected float duration;
    public String ID = null;
    protected float delta = 0;
    private RTS_Animator.curve curve = null;
    protected boolean isPaused = false;
    protected boolean duringPause = false;
    protected boolean loop = false;

    public void reset () {
        this.delta = 0;
    }

    public void end () {
        this.delta = 100;
    }

    public void defineCurve (RTS_Animator.curve curve) {
        this.curve = curve;
    }

    public float getCalculatedDelta () {
        if (this.curve != null)
            return (this.curve.getCalculatedDelta(this.delta));
        else
            return (delta);
    }

    public boolean setPlay (boolean playPause) {
        boolean hold = this.isPaused;
        this.isPaused = !playPause;
        return (this.isPaused != hold);
    }

    public boolean setAnimateDuringPause (boolean duringPause) {
        boolean hold = this.duringPause;
        this.duringPause = duringPause;
        return (this.duringPause != hold);
    }

    public boolean setLoop (boolean loop) {
        boolean hold = this.loop;
        this.loop = loop;
        return (this.loop != hold);
    }

    public boolean isAnimating () {
        return (this.ID != null && !this.isPaused);
    }

    protected void advance (float amount) {
        if (this.isPaused)
            return;
        if (!this.duringPause && Global.getCombatEngine().isPaused())
            return;
        this.delta = this.delta + (100f * (amount/this.duration));
    }
}
