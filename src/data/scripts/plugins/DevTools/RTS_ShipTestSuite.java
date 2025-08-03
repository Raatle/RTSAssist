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

package data.scripts.plugins.DevTools;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import data.scripts.RTSAssistModPlugin;
import data.scripts.modInitilisation.RTS_CommonsControl;
import data.scripts.plugins.RTSAssist;
import data.scripts.plugins.RTS_ParseInput;
import data.scripts.plugins.RTS_SelectionListener;
import data.scripts.plugins.Utils.RTS_Event;
import data.scripts.plugins.Utils.RTS_EventManager;
import data.scripts.plugins.Utils.RTS_StatefulClasses;
import org.json.JSONArray;
import org.json.JSONException;
import org.lwjgl.util.vector.Vector2f;
import java.io.*;
import java.util.*;
import java.util.List;

import static data.scripts.RTSAssistModPlugin.RTS_Global;

public class RTS_ShipTestSuite extends RTS_StatefulClasses {

    /* Set to Mod ID to test that mods ships. Set Empty to test Vanilla ships */
    String testPack = "diableavionics";

    String comArray = "shipTestSuiteShipList";
    String comTest = "shipTestSuiteTestShip";

    public RTS_ShipTestSuite(Object state) {
        super(state, true);
        this.commonsControl = (RTS_CommonsControl)RTS_Global.get(RTSAssistModPlugin.names.commonsControl);
        this.retreiveCommonsShipList(false);
        this.getAllTestPackVariantIds();
        this.collateShipLists();
        this.commonsShiplistIterHold = new ArrayList<>(this.commonsShipList);
        this.commonsShipListIter = this.commonsShiplistIterHold.iterator();
        this.colshipListIterHold = new ArrayList<>(this.collatedShipList);
        this.colShipListIter = this.colshipListIterHold.iterator();
        this.inCampaign = Global.getSector().getPlayerFleet() != null;
        this.replaceDefaultShipWithTestShip();
    }

    RTS_CommonsControl commonsControl;
    boolean inCampaign = false;

    List<String> shipList = new ArrayList<>();
    List<String> commonsShipList = new ArrayList<>();
    List<String> collatedShipList = null;

    List<String> colshipListIterHold;
    Iterator<String> colShipListIter;
    List<String> commonsShiplistIterHold;
    Iterator<String> commonsShipListIter;

    public void spawnShipIter(Vector2f worldSpace) {
        if (this.colShipListIter.hasNext())
            ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine, true))
                    .getFleetManager(0).spawnShipOrWing(
                            this.colShipListIter.next(),
                            worldSpace, 90f
            );
        else {
            this.colshipListIterHold.clear();
            this.colshipListIterHold = new ArrayList<>(this.collatedShipList);
            this.colShipListIter = this.colshipListIterHold.iterator();
        }
    }

    public void spawnCommonsShipIter (Vector2f worldSpace) {
        if (this.commonsShipListIter.hasNext())
            ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine, true))
                    .getFleetManager(0).spawnShipOrWing(
                    this.commonsShipListIter.next(),
                    worldSpace, 90f
            );
        else {
            this.commonsShiplistIterHold.clear();
            this.commonsShiplistIterHold = new ArrayList<>(this.commonsShipList);
            this.commonsShipListIter = this.commonsShiplistIterHold.iterator();
        }
    }

    public void setTestShip (List<ShipAPI> selection) {
        String shipToStore = (selection == null || selection.size() != 1)
                ? ""
                : selection.get(0).getVariant().getHullVariantId();
        try {
            this.commonsControl.set(
                    this.comTest,
                    shipToStore
            );
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void spawnTestShip(Vector2f worldSpace, boolean setAsPlayer) {
        String testShip;
        try {
            testShip = (String)this.commonsControl.get(this.comTest, RTS_CommonsControl.JSONType.STRING);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if (testShip == null || testShip.isEmpty())
            return;
        ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine, true))
                .getFleetManager(0).spawnShipOrWing(
                        testShip,
                        worldSpace,
                        90f
                );
        if (setAsPlayer) {
            ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager, true)).addEvent(new RTS_Event() {
                float hold = (float)getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.start), true);

                @Override
                public boolean shouldExecute(Object state) {
                    return ((float)getDeepState(Arrays.asList(RTSAssist.stNames.amount, RTSAssist.amNames.start), true) - hold > 0.2);
                }

                @Override
                public void run() {
                    for (ShipAPI ship : ((CombatEngineAPI)getState(RTSAssist.stNames.engine, true)).getShips())
                        if (ship.getVariant().getHullVariantId().equals(testShip)) {

                            ((CombatEngineAPI)getState(RTSAssist.stNames.engine, true)).setPlayerShipExternal(ship);
                            setState(RTS_ParseInput.stNames.playerShipHold, ship, true);
                            break;
                        }
                }
            });
        }
    }

    public void soutShipDetails (List<ShipAPI> currentSelection) {
        if (currentSelection == null)
            return;
        for (ShipAPI ship : currentSelection) {
            System.out.println("START");
            System.out.println("getName: " + ship.getName());
            System.out.println("getId: " + ship.getId());
            System.out.println("getHullSize -> name: " + ship.getHullSize().name());
            System.out.println("getHullSpec -> getBaseHullId: " + ship.getHullSpec().getBaseHullId());
            System.out.println("getSystem -> getId: " + ship.getSystem().getId());
            System.out.println("END");
        }
    }

    public void deleteShips(List<ShipAPI> selection) {
        if (selection == null || selection.isEmpty())
            return;
        for (ShipAPI ship : selection)
            ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine, true)).removeEntity(ship);
    }

    public void getAllTestPackVariantIds () {
        HashMap<ShipHullSpecAPI, List<String>> rawMap = new HashMap<>();
        List<String> blockedSystems = this.getVanillaSystemStrings();

        for (String x:  Global.getSettings().getAllVariantIds()) {
            ShipHullSpecAPI SHSAPointer = Global.getSettings().getVariant(x).getHullSpec();
            if (
                    (SHSAPointer.getSourceMod() == null && !this.testPack.isEmpty())
                    || SHSAPointer.getHullSize() == ShipAPI.HullSize.FIGHTER
                    || (blockedSystems.contains(SHSAPointer.getShipSystemId()) && !this.testPack.isEmpty())
                    || SHSAPointer.getShipSystemId() == null
            )
                continue;
            if (
                    this.testPack.isEmpty() && SHSAPointer.getSourceMod() == null
                    || SHSAPointer.getSourceMod().getId().equals(this.testPack)
            ){
                if (!rawMap.containsKey(SHSAPointer))
                    rawMap.put(SHSAPointer, new ArrayList());
                rawMap.get(SHSAPointer).add(x);
            }
        }
        for (Map.Entry<ShipHullSpecAPI, List<String>> variList : rawMap.entrySet())
            this.shipList.add(variList.getValue().get(0));
    }

    public List<String> getVanillaSystemStrings () {
        List<String> systemList = new ArrayList<>();
        for (ShipHullSpecAPI ship : Global.getSettings().getAllShipHullSpecs()) {
            if (ship.getSourceMod() != null)
                continue;
            if (!systemList.contains(ship.getShipSystemId()))
                systemList.add(ship.getShipSystemId());
        }
        return (systemList);
    }

    public void collateShipLists () {
        if (this.collatedShipList != null)
            this.collatedShipList.clear();
        this.collatedShipList = new ArrayList<>(this.shipList);
        this.collatedShipList.removeAll(this.commonsShipList);
    }

    public void retreiveCommonsShipList (boolean update){
        try {
            this.__retreiveCommonsShipList(update);
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void __retreiveCommonsShipList (boolean update) throws JSONException, IOException {
        if (update)
            this.commonsControl.updateCache();
        if (this.commonsControl.get(this.comArray, RTS_CommonsControl.JSONType.JSONARRAY) == null)
            this.commonsControl.set(this.comArray, new JSONArray());
        JSONArray extraction = (JSONArray)this.commonsControl.get(this.comArray, RTS_CommonsControl.JSONType.JSONARRAY);
        this.commonsShipList.clear();
        if (extraction.length() == 0)
            return;
        for (int i = 0 ; i < extraction.length() ; i++)
            this.commonsShipList.add((String)extraction.get(i));
    }

    public void addShipsToCommons (List<ShipAPI> selection) {
        if (selection == null || selection.isEmpty())
            return;
        for (ShipAPI ship : selection) {
            this.addShipToCommons(ship.getVariant().getHullVariantId());
        }
        this.retreiveCommonsShipList(true);
        this.collateShipLists();
    }
    private void addShipToCommons (String shipVariId) {
        try {
            this.__addShipToCommons(shipVariId);
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void __addShipToCommons (String shipVariId) throws JSONException, IOException {
        if (this.commonsControl.get(this.comArray, RTS_CommonsControl.JSONType.JSONARRAY) == null)
            this.commonsControl.set(this.comArray, new JSONArray());
        JSONArray extraction = (JSONArray)this.commonsControl.get(this.comArray, RTS_CommonsControl.JSONType.JSONARRAY);
        for (int i = 0; i < extraction.length(); i++) {
            if (((String)extraction.get(i)).equals(shipVariId))
                return;
        }
        extraction.put(shipVariId);
        this.commonsControl.set(this.comArray, extraction);
    }

    public void removeShipsFromCommons (List<ShipAPI> selection) {
        if (selection == null || selection.isEmpty())
            this.removeShipFromCommons("emptyCommons");
        else {
            for (ShipAPI ship : selection) {
                this.removeShipFromCommons(ship.getVariant().getHullVariantId());
            }
        }
        this.retreiveCommonsShipList(true);
        this.collateShipLists();
    }
    private void removeShipFromCommons (String shipVariId){
        try {
            this.__removeShipFromCommons(shipVariId);
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void __removeShipFromCommons (String shipVariId) throws JSONException, IOException {
        if (shipVariId.equals("emptyCommons")) {
            this.commonsControl.set(this.comArray, new JSONArray());
            return;
        }
        if (this.commonsControl.get(this.comArray, RTS_CommonsControl.JSONType.JSONARRAY) == null)
            this.commonsControl.set(this.comArray, new JSONArray());
        JSONArray extraction = (JSONArray)this.commonsControl.get(this.comArray, RTS_CommonsControl.JSONType.JSONARRAY);
        boolean flag = false;
        for (int i = 0 ; i < extraction.length() ; i++) {
            if (((String)extraction.get(i)).equals(shipVariId)) {
                extraction.remove(i);
                flag = true;
                break;
            }
        }
        if (flag)
            this.commonsControl.set(this.comArray, extraction);
    }

    private void replaceDefaultShipWithTestShip () {
        if (this.inCampaign)
            return;
        try {
            String testShip = (String)this.commonsControl.get(this.comTest, RTS_CommonsControl.JSONType.STRING);
            if (testShip == null || testShip.isEmpty())
                return;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        ((RTS_EventManager)this.getState(RTSAssist.stNames.eventManager, true)).addEvent(new RTS_Event() {
            @Override
            public boolean shouldExecute(Object state) {
                float hold = (float)getDeepState(Arrays.asList(
                        RTSAssist.stNames.amount,
                        RTSAssist.amNames.start
                ), true);
                return (hold > 0.1f);
            }

            @Override
            public void run() {
                ShipAPI replace = null;
                for (ShipAPI ship : ((CombatEngineAPI)getState(RTSAssist.stNames.engine, true)).getShips()) {
                    if (((RTS_SelectionListener)getState(RTSAssist.stNames.selectionListener, true)).dontSelect(ship))
                        continue;
                    replace = ship;
                    break;
                }
                if (replace == null)
                    return;
                deleteShips(Arrays.asList(replace));
                spawnTestShip(replace.getLocation(), true);

            }
        });


    }
}



















