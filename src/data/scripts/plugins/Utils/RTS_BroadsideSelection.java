package data.scripts.plugins.Utils;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.RTSAssistModPlugin;
import data.scripts.modInitilisation.RTS_CommonsControl;
import data.scripts.plugins.RTSAssist;
import data.scripts.plugins.RTS_CameraRework;
import data.scripts.plugins.RTS_ParseInput;
import data.scripts.plugins.RTS_TaskManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static data.scripts.RTSAssistModPlugin.RTS_Global;

public class RTS_BroadsideSelection extends RTS_StatefulClasses {

    public RTS_BroadsideSelection (Object state) {
        super(state);
    }

    private ShipAPI shipBeingAdjusted = null;
    private boolean doubleTapped = false;
    float doubleTapWaitDelay = 0.15f;
    Vector2f mockup = new Vector2f();
    String eventHold = null;

    public void drawBSSSelection () {
        if (this.shipBeingAdjusted == null || this.eventHold != null)
            return;
        Vector2f adjustmentAngles = this.getAdjustmentAngles();
        this.drawArc(
                this.shipBeingAdjusted,
                adjustmentAngles.getX() - 10f
        );
        if (adjustmentAngles.getY() <= 360)
            this.drawArc(
                    this.shipBeingAdjusted,
                    adjustmentAngles.getY() - 10f
            );

    }

    public void beginBSSelection () {
        if (this.eventHold == null) {
            List<ShipAPI> currentSelection = (List<ShipAPI>)this.getState(RTS_ParseInput.stNames.currentSelection);
            if (currentSelection == null || currentSelection.size() != 1)
                return;
            this.shipBeingAdjusted = currentSelection.get(0);
            this.eventHold = ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager)).addEvent(new RTS_Event() {
                float tHold = (float)getDeepState(Arrays.asList(
                                RTSAssist.stNames.amount,
                                RTSAssist.amNames.start
                        )
                );

                @Override
                public boolean shouldExecute(Object state) {
                    float time = (float)getDeepState(Arrays.asList(
                                    RTSAssist.stNames.amount,
                                    RTSAssist.amNames.start
                            )
                    );
                    return (time - this.tHold > doubleTapWaitDelay);
                }

                @Override
                public void run() {
                    ((RTS_EventManager)getState(RTSAssist.stNames.eventManager)).deleteEvent(eventHold);
                    eventHold = null;
                }
            });
        }
        else {
            ((RTS_EventManager)getState(RTSAssist.stNames.eventManager)).deleteEvent(eventHold);
            eventHold = null;
            this.doubleTapped = true;
        }
    }

    public void finaliseBSSelection () {
        if (this.eventHold != null) {
            ((RTS_EventManager)getState(RTSAssist.stNames.eventManager)).deleteEvent(eventHold);
            eventHold = null;
            this.eventHold = ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager)).addEvent(new RTS_Event() {
                float tHold = (float)getDeepState(Arrays.asList(
                                RTSAssist.stNames.amount,
                                RTSAssist.amNames.start
                        )
                );

                @Override
                public boolean shouldExecute(Object state) {
                    float time = (float)getDeepState(Arrays.asList(
                                    RTSAssist.stNames.amount,
                                    RTSAssist.amNames.start
                            )
                    );
                    return (time - this.tHold > doubleTapWaitDelay);
                }

                @Override
                public void run() {
                    ((RTS_EventManager)getState(RTSAssist.stNames.eventManager)).deleteEvent(eventHold);
                    eventHold = null;
                    shipBeingAdjusted = null;
                }
            });
        }
        else {
            if (this.shipBeingAdjusted != null)
                this.createBroadSideMod();
            this.shipBeingAdjusted = null;
            this.doubleTapped = false;
        }
    }

    private void createBroadSideMod () {
        Vector2f adjA = new Vector2f(this.mockup);
        ((HashMap<String, Object>)this.getState(RTSAssist.stNames.broadsideData)).put(
                this.shipBeingAdjusted.getId(),
                adjA
        );
        if (!RTS_Global.containsKey(RTSAssistModPlugin.names.gameID))
            return;
        RTS_CommonsControl commonsControl = (RTS_CommonsControl)RTS_Global.get(RTSAssistModPlugin.names.commonsControl);
        commonsControl.updateCache();
        JSONObject broadsideData;
        try {
            broadsideData = (JSONObject)commonsControl.get(
                    "broadsides_" + (String)RTS_Global.get(RTSAssistModPlugin.names.gameID),
                    RTS_CommonsControl.JSONType.JSONOBJECT
            );
            if (broadsideData == null)
                broadsideData = new JSONObject();
            JSONObject storage = new JSONObject();
            if (Math.abs(adjA.getX()) > 5f) {
                storage.put("angle", adjA.getX());
                storage.put("reflected", adjA.getY() <= 360f);
                broadsideData.put(this.shipBeingAdjusted.getFleetMember().getId(), storage);
            }
            else
                broadsideData.remove(this.shipBeingAdjusted.getFleetMember().getId());
            commonsControl.set(
                    "broadsides_" + (String)RTS_Global.get(RTSAssistModPlugin.names.gameID),
                    broadsideData
            );
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Vector2f getAdjustmentAngles () {
        float adjustmentAngle = MathUtils.getShortestRotation(
                this.shipBeingAdjusted.getFacing(),
                VectorUtils.getAngle(
                        CombatUtils.toScreenCoordinates(this.shipBeingAdjusted.getLocation()),
                        (Vector2f)this.getState(RTS_ParseInput.stNames.screenSpace)
                )
        );
        Vector2f adjA = new Vector2f();
        adjA.setX(this.shipBeingAdjusted.getFacing() + adjustmentAngle);
        this.mockup.setX(adjustmentAngle * -1);
        adjA.setY(361f);
        this.mockup.setY(361f);
        if (!this.doubleTapped)
            return (adjA);
        adjustmentAngle = Math.abs(adjustmentAngle);
        adjA.setX(this.shipBeingAdjusted.getFacing() + adjustmentAngle);
        this.mockup.setX(adjustmentAngle * -1);
        adjA.setY(361f);
        this.mockup.setY(361f);
        if (adjustmentAngle < 12f && adjustmentAngle > -12f) {
            adjA.setX(this.shipBeingAdjusted.getFacing());
            this.mockup.setX(0f);
        }
        else if (adjustmentAngle > (180f - 12f)) {
            adjA.setX((this.shipBeingAdjusted.getFacing() + 180f) % 360f);
            this.mockup.setX(180f);
        }
        else {
            adjA.setY(this.shipBeingAdjusted.getFacing() - adjustmentAngle);
            this.mockup.setY(adjustmentAngle);
        }
        return (adjA);
    }

    private void drawArc (ShipAPI ship, float angle) {
        ((RTS_Draw)this.getState(RTSAssist.stNames.draw)).drawCircleFragment(
                ship,
                (float)this.getState(RTS_CameraRework.stNames.targetZoom),
                angle
        );
    }
}
