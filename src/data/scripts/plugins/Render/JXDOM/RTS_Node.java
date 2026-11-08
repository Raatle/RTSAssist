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

package data.scripts.plugins.Render.JXDOM;

import data.scripts.plugins.Render.JXDOM.Props.RTS_Prop;

import java.util.*;

public class RTS_Node {

    //------------------------------------------------------------------------------------------------------------------

    Class<?> type;

    HashMap<String, Object> rawProps = new HashMap<>();
    HashMap<String, Object> processedProps = new HashMap<>();
    RTS_BaseInterface.onRender onRender;
    RTS_BaseInterface.everyFrameUpdate everyFrame;
    HashMap<String, RTS_Prop> props;
    List<RTS_Prop> modProps;
    public RTS_Node parent = null;

    //------------------------------------------------------------------------------------------------------------------

    public void render (Map<String, Object> rawProps, boolean update) {
        this.rawProps.clear();
        if (rawProps != null)
            this.rawProps.putAll(rawProps);
        boolean forceRender = (boolean)this.rawProps.getOrDefault(
                RTS_ComponentManager.forceRender,
                false
        );
        boolean suppressRender = (boolean)this.rawProps.getOrDefault(
                RTS_ComponentManager.suppressRender,
                false
        );
        if (update || forceRender)
            this.buildProps();
        this.processPropEvents();
        if (
                this.onRender != null
                        && (update || forceRender)
                        && !suppressRender
        )
            this.onRender.render(this.processedProps, this.rawProps);
        if (this.everyFrame != null)
            this.everyFrame.everyFrame(this.processedProps, this.rawProps);
    }

    //------------------------------------------------------------------------------------------------------------------

    private void buildProps () {
        RTS_Prop propPointer;
        for (Map.Entry<String, RTS_Prop> value : this.props.entrySet()) {
            propPointer = value.getValue();
            if (propPointer.getType().equals(RTS_Prop.propType.value))
                this.processedProps.put(value.getKey(), propPointer.consolidateWithParentProps(
                        this.parent != null
                                ? this.parent.processedProps
                                : null,
                        !this.rawProps.containsKey(value.getKey())
                                ? propPointer.getDefaultValue()
                                : this.rawProps.get(value.getKey())
                ));
        }
        if (!this.rawProps.isEmpty())
            for (RTS_Prop modProp : this.modProps) {
                if (this.rawProps.containsKey(modProp.getID()))
                    modProp.modifySiblingProps(this.processedProps, this.rawProps, this);
            }
    }

    private void processPropEvents () {
        if (this.rawProps.isEmpty())
            return;
        RTS_Prop propPointer;
        for (Map.Entry<String, RTS_Prop> value : this.props.entrySet()) {
            propPointer = value.getValue();
            if (
                       this.rawProps.containsKey(value.getKey())
                    && propPointer.getType().equals(RTS_Prop.propType.event)
            ) {
                if (propPointer.triggerEventCallBack(this.processedProps, this.rawProps, this))
                    ((RTS_Prop.propEvent)this.rawProps.get(value.getKey())).onTrigger(this.processedProps, this.rawProps, this);
                ((RTS_Prop.propEvent)this.rawProps.get(value.getKey())).alwaysTrigger(this.processedProps, this.rawProps, this);
            }
        }
    }

    public void buildNode (RTS_ComponentManager.templateNode tempNode, RTS_Node parent) {
        this.onRender = tempNode.onRender;
        this.everyFrame = tempNode.everyFrame;
        this.type = tempNode.type;
        this.parent = parent;
        this.props = tempNode.initProps.init();
        this.buildPropModifiers();
    }

    private void buildPropModifiers () {
        RTS_Prop propPointer;
        List<RTS_Prop> modProps = new ArrayList<>();
        for (Map.Entry<String, RTS_Prop> allProps : this.props.entrySet()) {
            propPointer = allProps.getValue();
            propPointer.checkForNecProps(this.props);
            if (propPointer.getType().equals(RTS_Prop.propType.modifier))
                modProps.add(propPointer);
        }
        modProps.sort(Comparator.comparing(RTS_Prop::getPriority));
        this.modProps = modProps;
    }

    public void reset () {
        this.processedProps.clear();
        for (Map.Entry<String, RTS_Prop> entry : this.props.entrySet())
            entry.getValue().reset();
    }

    public void prepareForDeletion () {
        this.processedProps.clear();
        this.props.clear();
    }

    public boolean nodeIsMatch (Map<String, Object> rawProps) {
        if (rawProps == null || rawProps.isEmpty())
            return (this.rawProps.isEmpty());
        if (!rawProps.keySet().equals(this.rawProps.keySet()))
            return (false);
        for (Map.Entry<String, Object> value : rawProps.entrySet()) {
            if (!this.props.containsKey(value.getKey()))
                continue;
            if (!this.props.get(value.getKey()).equals(
                    value.getValue(),
                    this.rawProps.get(value.getKey())
            ))
                return (false);
        }
        return (true);
    }

    public HashMap<String, RTS_Prop> getProps () {
        return (this.props);
    }

    public HashMap<String, Object> getParentsProcessedProps () {
        return (this.parent == null ? null : this.parent.processedProps);
    }

    //------------------------------------------------------------------------------------------------------------------
}
