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

import java.util.*;
import java.lang.Object;

public class RTS_StatefulClasses {

    public RTS_StatefulClasses() {
        this.isRestricted = false;
        this.state = new HashMap<>();
        this.state.put("intHold", 1); // replace intHold with 0
    }
    public RTS_StatefulClasses(Object adoptState) {
        this.isRestricted = false;
        this.state = (HashMap<String, Object>)adoptState;
    }
    public RTS_StatefulClasses(Object adoptState, Object makeRestricted) {
        this.isRestricted = true;
        this.restrictedState = (HashMap<String, Object>)adoptState;
        String internalStateName = this.getClass().getName() + UUID.randomUUID();
        this.classStateIdentifier = internalStateName;
        this.restrictedState.put(internalStateName, new HashMap<>());
        this.state = (HashMap<String, Object>)this.restrictedState.get(internalStateName);
    }

    private HashMap<String, Object> state;
    private HashMap<String, Object> restrictedState;
    private HashMap<String, Object> pointer;
    private String classStateIdentifier;
    public boolean isRestricted;
    private static int uniqueIdentifier = 0;

    public static String getUniqueIdentifier () {
        RTS_StatefulClasses.uniqueIdentifier++;
        int ident = RTS_StatefulClasses.uniqueIdentifier;
        //return (ident);
        return (UUID.randomUUID().toString());
    }

    public HashMap<String, Object> returnState() {return(this.state);}
    public HashMap<String, Object> returnState(Object accessRestricted) {return(this.restrictedState);}

    public String returnClassStateIdentifier() {
        if (this.classStateIdentifier != null) return (this.classStateIdentifier);
        else return(null);
    }

    public void setState (String identifier, Object value) {
        this.state.put(identifier, value);
    }
    public void setState (String identifier, Object value, Object accessRestricted) {
        this.restrictedState.put(identifier, value);
    }
    public void setState (HashMap<String, Object> keyValueList) {
        for(Map.Entry<String, Object> x : keyValueList.entrySet()) {
            this.setState(x.getKey(), x.getValue());
        }
    }
    public void setState (HashMap<String, Object> keyValueList, Object accessRestricted) {
        for(Map.Entry<String, Object> x : keyValueList.entrySet()) {
            this.setState(x.getKey(), x.getValue(), true);
        }
    }

    public void setDeepState(List<String> identBreadCrumb, Object value) {
        pointer = this.state;
        for (int i = 0; i < identBreadCrumb.size() - 1; i++) {
            pointer = (HashMap<String, Object>)pointer.get(identBreadCrumb.get(i));
        }
        pointer.put(identBreadCrumb.get(identBreadCrumb.size() -1), value);
    }
    public void setDeepState(List<String> identBreadCrumb, Object value, Object accessRestricted) {
        pointer = this.restrictedState;
        for (int i = 0; i < identBreadCrumb.size() - 1; i++) {
            pointer = (HashMap<String, Object>)pointer.get(identBreadCrumb.get(i));
        }
        pointer.put(identBreadCrumb.get(identBreadCrumb.size() -1), value);
    }

    public Object getState (String identifier) {
        return(this.state.get(identifier));
    }
    public Object getState (String identifier, Object accessRestricted) {
        return(this.restrictedState.get(identifier));
    }
    public ArrayList<Object> getState (List<String> identifiers) {
        ArrayList<Object> pointer = new ArrayList<>();
        for(int i = 0; i < identifiers.size(); i++) {
            pointer.add(this.state.get(identifiers.get(i)));
        }
        return(pointer);
    }
    public ArrayList<Object> getState (List<String> identifiers, Object accessRestricted) {
        ArrayList<Object> pointer = new ArrayList<>();
        for(int i = 0; i < identifiers.size(); i++) {
            pointer.add(this.restrictedState.get(identifiers.get(i)));
        }
        return(pointer);
    }

    public Object getDeepState (List<String> identBreadCrumb) {
        HashMap<String, Object> pointer = this.state;
        for (int i = 0; i < identBreadCrumb.size() - 1; i++) {
            pointer = (HashMap<String, Object>)pointer.get(identBreadCrumb.get(i));
        }
        return(pointer.get(identBreadCrumb.get(identBreadCrumb.size() -1)));
    }
    public Object getDeepState (List<String> identBreadCrumb, Object accessRestricted) {
        HashMap<String, Object> pointer = this.restrictedState;
        for (int i = 0; i < identBreadCrumb.size() - 1; i++) {
            pointer = (HashMap<String, Object>)pointer.get(identBreadCrumb.get(i));
        }
        return(pointer.get(identBreadCrumb.get(identBreadCrumb.size() -1)));
    }

    public void cleanState() {
        this.state.clear();
    }
    public void cleanState(Object accessRestricted) {
        if (this.isRestricted) {
            this.state.clear();
            this.restrictedState.clear();
        }
    }
}