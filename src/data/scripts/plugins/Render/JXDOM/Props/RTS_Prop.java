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

package data.scripts.plugins.Render.JXDOM.Props;

import data.scripts.plugins.Render.JXDOM.RTS_Node;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class RTS_Prop {

    //------------------------------------------------------------------------------------------------------------------

    public RTS_Prop (RTS_Prop.propType purpose, Object defaultVal) {
        this.value = defaultVal;
        this.defaultValue = defaultVal;
        this.purpose = purpose;
    }

    public enum propType {
        value,
        event,
        modifier
    }

    public interface propEvent {
        void onTrigger (HashMap<String, Object> processedProps, Map<String, Object> rawProps, RTS_Node callingNode);
        default void alwaysTrigger (HashMap<String, Object> processedProps, java.util.Map<String, Object> rawProps, RTS_Node callingNode) {}
    }

    Object value;
    Object defaultValue;
    propType purpose;

    //------------------------------------------------------------------------------------------------------------------

    public abstract Object consolidateWithParentProps (HashMap<String, Object> processedParentProps, Object rawProp);

    public abstract void modifySiblingProps (HashMap<String, Object> processedProps, Map<String, Object> rawProps, RTS_Node callingNode);

    public abstract boolean triggerEventCallBack (HashMap<String, Object> processedProps, Map<String, Object> rawProps, RTS_Node callingNode);

    public abstract List<String> getNecSiblingProps ();

    public abstract Integer getPriority ();

    public abstract boolean equals (Object newVal, Object oldVal);

    public abstract String getID ();

    public void setValue (Object val) {
        this.value = val;
    }

    public Object getValue () {
        return (this.value);
    }

    public Object getDefaultValue () { return (this.defaultValue); }

    public propType getType () {
        return (this.purpose);
    }

    public void reset () {
        this.value = this.defaultValue;
    }

    public void checkForNecProps (HashMap<String, RTS_Prop> props) {
        if (this.getNecSiblingProps() == null)
            return;
        for (String ID : this.getNecSiblingProps()) {
            if (!props.containsKey(ID)) {
                String preface = "JXDOM: " + this.getID() + " Prop requires that this Prop also be present: ";
                StringBuilder exception = new StringBuilder("Exception in JXDOM:\n");
                for (String ID2 : this.getNecSiblingProps()) {
                    if (!props.containsKey(ID2)) {
                        exception.append("  ").append(preface).append(ID2).append("\n");
                    }
                }
                throw new RuntimeException(exception.toString());
            }
        }
    }

    public interface eventFunction {
        String getID();

        Object getEventResult ();
    }

    //------------------------------------------------------------------------------------------------------------------

    public static HashMap<String, Object> eventStore = new HashMap<>();

    public static HashMap<String, RTS_Prop> registerProps (List<RTS_Prop> props) {
        HashMap<String, RTS_Prop> propMap = new HashMap<>();
        for (RTS_Prop prop : props)
            propMap.put(prop.getID(), prop);
        return (propMap);
    }

    public static Object getEventResult (RTS_Prop.eventFunction eventFunction) {
        if (RTS_Prop.eventStore.containsKey(eventFunction.getID()))
            return (RTS_Prop.eventStore.get(eventFunction.getID()));
        RTS_Prop.eventStore.put(eventFunction.getID(), eventFunction.getEventResult());
        return (RTS_Prop.eventStore.get(eventFunction.getID()));
    }

    public static void resetEventStore () {
        RTS_Prop.eventStore.clear();
    }

    public static RTS_Prop.propEvent stackPropEvents (List<RTS_Prop.propEvent> propStack) {
        return (new propEvent() {
            @Override
            public void onTrigger (HashMap<String, Object> processedProps, Map<String, Object> rawProps, RTS_Node callingNode) {
                for (RTS_Prop.propEvent propEvent : propStack)
                    propEvent.onTrigger(processedProps, rawProps, callingNode);
            }

            @Override
            public void alwaysTrigger (HashMap<String, Object> processedProps, Map<String, Object> rawProps, RTS_Node callingNode) {
                for (RTS_Prop.propEvent propEvent : propStack)
                    propEvent.alwaysTrigger(processedProps, rawProps, callingNode);
            }
        });
    }
    public static RTS_Prop.propEvent stackPropEvents (Object storedEvent, RTS_Prop.propEvent newEvent) {
        RTS_Prop.propEvent stored = (RTS_Prop.propEvent)storedEvent;
        return (new propEvent() {
            @Override
            public void onTrigger (HashMap<String, Object> processedProps, Map<String, Object> rawProps, RTS_Node callingNode) {
                if (stored != null)
                    stored.onTrigger(processedProps, rawProps, callingNode);
                newEvent.onTrigger(processedProps,rawProps, callingNode);
            }

            @Override
            public void alwaysTrigger (HashMap<String, Object> processedProps, java.util.Map<String, Object> rawProps, RTS_Node callingNode) {
                if (stored != null)
                    stored.alwaysTrigger(processedProps, rawProps, callingNode);
                newEvent.alwaysTrigger(processedProps, rawProps, callingNode);
            }
        });
    }

    public static Map<String, Object> props (@NotNull Object... props) {
        if (props.length % 2 == 1)
            throw new RuntimeException("JXDOM: RTS_Prop.props must receive an even argument count");
        propMaps.ensureCapacity(propMapIndex + 1);
        while (propMaps.size() <= propMapIndex)
            propMaps.add(new HashMap<>());
        propMaps.get(propMapIndex).clear();
        for (int i = 0; i < props.length; i = i + 2)
            propMaps.get(propMapIndex).put((String)props[i], props[i + 1]);
        return (Collections.unmodifiableMap(propMaps.get(propMapIndex++)));
    }
    private static ArrayList<HashMap<String, Object>> propMaps = new ArrayList<>();
    private static int propMapIndex = 0;

    public static void resetPropMaps () {
        propMapIndex = 0;
    }


    //------------------------------------------------------------------------------------------------------------------
}
