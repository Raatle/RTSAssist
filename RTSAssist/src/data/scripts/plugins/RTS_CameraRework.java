/****************************************************************************************
 * RTSAssist version 0.1.0
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

package data.scripts.plugins;

import java.util.HashMap;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.Global;

import org.lazywizard.lazylib.MathUtils;

import data.scripts.plugins.Utils.RTS_StatefulClasses;

public class RTS_CameraRework extends RTS_StatefulClasses {

    public RTS_CameraRework(Object state) {
        super(state);
        HashMap<String, Object> init = new HashMap<String, Object>() {{
            put("sDimensions", (Vector2f)null);
            put("mDimensions", new Vector2f(Global.getSettings().getScreenWidth(), Global.getSettings().getScreenHeight()));
            put("targetLLVec", (Vector2f)null);
            put("LLVec", (Vector2f)null);
            put("targetZoom", 1f);
            put("zoom", 1f);
            put("zoomVelocity", 0.1);
            put("panVelocity", null);
        }};
        init.put("panVelocity", MathUtils.clamp((float)((HashMap<String, Object>)this.getState("config"))
                .get("scrollSpeed"), 1f, 100f));
        init.put("viewPort", ((CombatEngineAPI)this.getState("engine")).getViewport());
        this.setState(init);
    }

    public void update (Vector2f screenSpace, int isZooming) {
        if (screenSpace == null) return;
        if (this.getState("sDimensions") == null)
            this.setState(
                    "sDimensions",
                    new Vector2f(
                            ((CombatEngineAPI)this.getState("engine")).getViewport().getVisibleWidth(),
                            ((CombatEngineAPI)this.getState("engine")).getViewport().getVisibleHeight()
                    )
            );
        if (this.getState("LLVec") == null &&
                !((CombatEngineAPI)this.getState("engine")).getCombatUI().isShowingCommandUI()) {
            initCameraXY();
        }
        if (!((CombatEngineAPI)this.getState("engine")).getCombatUI().isShowingCommandUI()) {
            this.zoomManager(isZooming);
            this.panManager(screenSpace);
        }
        this.render();
    }

    private void initCameraXY () {
        Vector2f init = null;
        int flag = 0;
        for (ShipAPI ship : ((CombatEngineAPI)this.getState("engine")).getShips())
            if (ship.isStation() && ship.getOriginalOwner() == 0)
                flag = 1;
        if (flag == 1)
            init = new Vector2f(0f, -4000f);
        else
            for (ShipAPI x: ((CombatEngineAPI)this.getState("engine")).getShips()) {
                if (x.getOriginalOwner() != 1) {
                    init = x.getLocation();
                    break;
                }
            }
        Vector2f sDimensions = ((Vector2f)this.getState("sDimensions"));
        if (init != null) {
            this.setState("LLVec", new Vector2f(
                    init.getX() - (sDimensions.getX() / 2),
                    init.getY() - (sDimensions.getY() / 2)
            ));
            this.setState("targetLLVec", new Vector2f(
                    init.getX() - (sDimensions.getX() / 2),
                    init.getY() - (sDimensions.getY() / 2)
            ));
        }
        ((CombatEngineAPI)this.getState("engine")).getViewport().setExternalControl(true);
    }

    private void panManager (Vector2f screenSpace) {
        if (((CombatEngineAPI)this.getState("engine")).getCombatUI().isShowingCommandUI() ||
                ((CombatEngineAPI)this.getState("engine")).isUIShowingDialog())
            return;
        Vector2f hold = (Vector2f)this.getState("targetLLVec");
        if (screenSpace.getX() <= 6)
            hold.setX(hold.getX() - ((float)this.getState("panVelocity") * (float)this.getState("targetZoom")));
        else if (screenSpace.getX() >= (((Vector2f)this.getState("mDimensions")).getX() - 6))
            hold.setX(hold.getX() + ((float)this.getState("panVelocity") * (float)this.getState("targetZoom")));

        if (screenSpace.getY() <= 6)
            hold.setY(hold.getY() - ((float)this.getState("panVelocity") * (float)this.getState("targetZoom")));
        else if (screenSpace.getY() >= (((Vector2f)this.getState("mDimensions")).getY() - 6))
            hold.setY(hold.getY() + ((float)this.getState("panVelocity") * (float)this.getState("targetZoom")));

        this.setState("targetLLVec", hold);
        this.setState("LLVec", hold);
    }

    private void zoomManager (int zoom) {
        if (((CombatEngineAPI)this.getState("engine")).getCombatUI().isShowingCommandUI() ||
                ((CombatEngineAPI)this.getState("engine")).isUIShowingDialog())
            return;
        switch(zoom) {
            case 0: break;
            case 1: if ((float)this.getState("targetZoom") > 1)
                this.setState("targetZoom", (float)this.getState("targetZoom") - .25f); break;
            case -1: if ((float)this.getState("targetZoom") <
                    MathUtils.clamp(
                            (float)(((HashMap<String, Object>)this.getState("config")).get("maxZoom")),
                            1f, 8f
                    )
            )
                this.setState("targetZoom", (float)this.getState("targetZoom") + .25f); break;
        }
    }

    private void render() {
        if (this.getState("LLVec") == null) return;
        ((ViewportAPI)this.getState("viewPort")).set(
                ((Vector2f)this.getState("LLVec")).getX()
                        - (((Vector2f)this.getState("sDimensions")).getX() * ((float)this.getState("targetZoom") / 2f))
                        + (((Vector2f)this.getState("sDimensions")).getX() / 2f),
                ((Vector2f)this.getState("LLVec")).getY()
                        - (((Vector2f)this.getState("sDimensions")).getY() * ((float)this.getState("targetZoom") / 2f))
                        + (((Vector2f)this.getState("sDimensions")).getY() / 2f),
                ((Vector2f)this.getState("sDimensions")).getX() * (float)this.getState("targetZoom"),
                ((Vector2f)this.getState("sDimensions")).getY() * (float)this.getState("targetZoom")
        );
    }
}