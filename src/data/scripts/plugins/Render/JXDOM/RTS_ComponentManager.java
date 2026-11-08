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
import org.jetbrains.annotations.NotNull;

import java.util.*;

// JXDOM
public class RTS_ComponentManager {

    //------------------------------------------------------------------------------------------------------------------

    public static class templateNode {
        Class<?> type;
        Map<String, Object> rawProps;
        Integer self;
        Integer parent = null;
        List<Integer> children;
        RTS_BaseInterface.onRender onRender;
        RTS_BaseInterface.everyFrameUpdate everyFrame;
        RTS_BaseInterface.initProps initProps;
    }

    private static List<templateNode> templateReserve = new ArrayList<>();
    private static List<templateNode> activeTemplates = new ArrayList<>();

    public static String forceRender = "forceRender";
    public static String suppressRender = "suppressRender";

    private ArrayList<RTS_Node> activeNodes = new ArrayList<>();
    private ArrayList<RTS_Node> prevTree = new ArrayList<>();

    private static HashMap<Integer, postTreeTraversalEvent> queuedPoTTEs = new HashMap<>();
    private static HashMap<Integer, preTreeTraversalEvent> queuedPreTTEs = new HashMap<>();
    private static Integer renderLevel;
    private static Integer PTTEEventIDCounter;
    private static boolean isTraversing = false;
    public interface preTreeTraversalEvent {
        void run ();
    }
    public interface postTreeTraversalEvent {
        void run ();
    }

    /* Core functionality */
    //------------------------------------------------------------------------------------------------------------------

    public void advance () {
        this.traverseTree();
        this.resetLists();
        RTS_Prop.resetEventStore();
        RTS_Prop.resetPropMaps();
    }

    public static Integer reserveNode (RTS_BaseInterface.internalProps call) {
        /* Allocate template node */
        if (RTS_ComponentManager.templateReserve.isEmpty())
            RTS_ComponentManager.templateReserve.add(new templateNode());
        int reserveIndex = RTS_ComponentManager.templateReserve.size() - 1;
        templateNode newNode = RTS_ComponentManager.templateReserve.get(reserveIndex);
        RTS_ComponentManager.templateReserve.remove(reserveIndex);
        RTS_ComponentManager.activeTemplates.add(newNode);
        int activeIndex = RTS_ComponentManager.activeTemplates.size() -1;
        /* write template node */
        newNode.type = call.nodeID;
        newNode.rawProps = call.calledProps;
        newNode.onRender = call.renderNode;
        newNode.everyFrame = call.everyFrame;
        newNode.initProps = call.initPropz;
        newNode.children = call.children;
        newNode.self = activeIndex;
        /* Inform children who their parents are.. */
        if (newNode.children != null && !newNode.children.isEmpty())
            for (Integer index : newNode.children)
                RTS_ComponentManager.activeTemplates.get(index).parent = activeIndex;
        /* Index is used to establish parent child relationship */
        return (activeIndex);
    }

    private void traverseTree () {
        RTS_ComponentManager.PTTEEventIDCounter = 0;
        for (Map.Entry<Integer, preTreeTraversalEvent> preEvent : RTS_ComponentManager.queuedPreTTEs.entrySet())
            preEvent.getValue().run();
        RTS_ComponentManager.isTraversing = true;
        for (templateNode tempNode : RTS_ComponentManager.activeTemplates)
            if (tempNode.parent == null)
                this.treeTraverser(tempNode, 0, 0);
        RTS_ComponentManager.isTraversing = false;
        for (Map.Entry<Integer, postTreeTraversalEvent> preEvent : RTS_ComponentManager.queuedPoTTEs.entrySet())
            preEvent.getValue().run();
        RTS_ComponentManager.queuedPoTTEs.clear();
    }

    private void treeTraverser (templateNode tempNode, int renderFlag, Integer level) {
        RTS_ComponentManager.renderLevel = level;
        this.ensureSize(this.activeNodes, tempNode.self + 1);
        RTS_Node nodePointer;
        /* Parent is fresh node */
        if (renderFlag == 2) {
            nodePointer = new RTS_Node();
            this.activeNodes.set(tempNode.self, nodePointer);
            nodePointer.buildNode(
                    tempNode,
                    tempNode.parent == null ? null : this.activeNodes.get(tempNode.parent)
            );
            nodePointer.render(tempNode.rawProps,true);
            if (tempNode.children != null)
                for (Integer index : tempNode.children)
                    this.treeTraverser(RTS_ComponentManager.activeTemplates.get(index), 2, level + 1);
        }
        /* Parent node matched node only */
        else if (renderFlag == 1) {
            nodePointer = this.prevTree.size() - 1 >= tempNode.self ? this.prevTree.get(tempNode.self) : null;
            /* Node matches */
            if (nodePointer != null && nodePointer.type.equals(tempNode.type)) {
                this.activeNodes.set(tempNode.self, nodePointer);
                nodePointer.reset();
                nodePointer.render(tempNode.rawProps,true);
                if (tempNode.children != null)
                    for (Integer index : tempNode.children)
                        this.treeTraverser(RTS_ComponentManager.activeTemplates.get(index), 1, level + 1);
            }
            /* Node does not match */
            else {
                nodePointer = new RTS_Node();
                this.activeNodes.set(tempNode.self, nodePointer);
                nodePointer.buildNode(
                        tempNode,
                        tempNode.parent == null ? null : this.activeNodes.get(tempNode.parent)
                );
                nodePointer.render(tempNode.rawProps,true);
                if (tempNode.children != null)
                    for (Integer index : tempNode.children)
                        this.treeTraverser(RTS_ComponentManager.activeTemplates.get(index), 2, level + 1);
            }
        }
        /* Parent node matched node and props */
        else {
            nodePointer = this.prevTree.size() - 1 >= tempNode.self ? this.prevTree.get(tempNode.self) : null;
            /* Node matches */
            if (nodePointer != null && nodePointer.type.equals(tempNode.type)) {
                this.activeNodes.set(tempNode.self, nodePointer);
                /* Node and props match */
                if (nodePointer.nodeIsMatch(tempNode.rawProps)) {
                    nodePointer.render(tempNode.rawProps, false);
                    if (tempNode.children != null)
                        for (Integer index : tempNode.children)
                            this.treeTraverser(RTS_ComponentManager.activeTemplates.get(index), 0, level + 1);
                }
                /* Only node matches */
                else {
                    nodePointer.reset();
                    nodePointer.render(tempNode.rawProps,true);
                    if (tempNode.children != null)
                        for (Integer index : tempNode.children)
                            this.treeTraverser(RTS_ComponentManager.activeTemplates.get(index), 1, level + 1);
                }
            }
            /* Node does not match */
            else {
                nodePointer = new RTS_Node();
                this.activeNodes.set(tempNode.self, nodePointer);
                nodePointer.buildNode(
                        tempNode,
                        tempNode.parent == null ? null : this.activeNodes.get(tempNode.parent)
                );
                nodePointer.render(tempNode.rawProps ,true);
                if (tempNode.children != null)
                    for (Integer index : tempNode.children)
                        this.treeTraverser(RTS_ComponentManager.activeTemplates.get(index), 2, level + 1);
            }
        }
        tempNode.parent = null;
    }

    private void resetLists () {
        this.prevTree.removeAll(this.activeNodes);
        for (RTS_Node node : this.prevTree)
            if (node != null)
                node.prepareForDeletion();
        prevTree.clear();
        this.prevTree.addAll(this.activeNodes);
        this.activeNodes.clear();
        RTS_ComponentManager.templateReserve.clear();
        RTS_ComponentManager.templateReserve.addAll(RTS_ComponentManager.activeTemplates);
        RTS_ComponentManager.activeTemplates.clear();
    }

    // https://stackoverflow.com/questions/7688151/java-arraylist-ensurecapacity-not-working
    private void ensureSize (ArrayList<?> list, int size) {
        list.ensureCapacity(size);
        while (list.size() < size)
            list.add(null);
    }

    /* Prop and element utilities */
    //------------------------------------------------------------------------------------------------------------------

    public static Integer getRenderLevel () {
        if (!RTS_ComponentManager.isTraversing)
            throw new RuntimeException("JXDOM: getRenderLevel can only be called during JXDOM traversal");
        return (RTS_ComponentManager.renderLevel);
    }

    public static Integer registerPoTTEEvent (RTS_ComponentManager.postTreeTraversalEvent event) {
        if (!RTS_ComponentManager.isTraversing)
            throw new RuntimeException("JXDOM: registerPoTTEEvent can only be called during JXDOM traversal");
        RTS_ComponentManager.PTTEEventIDCounter++;
        RTS_ComponentManager.queuedPoTTEs.put(RTS_ComponentManager.PTTEEventIDCounter, event);
        return (RTS_ComponentManager.PTTEEventIDCounter);
    }

    public static void deregisterPoTTEEvent (Integer eventID) {
        if (!RTS_ComponentManager.isTraversing)
            throw new RuntimeException("JXDOM: deregisterPoTTEEvent can only be called during JXDOM traversal");
        RTS_ComponentManager.queuedPoTTEs.remove(eventID);
    }

    public static Integer registerPreTTEEvent (RTS_ComponentManager.preTreeTraversalEvent event) {
        if (RTS_ComponentManager.isTraversing)
            throw new RuntimeException("JXDOM: registerPreTTEEvent can not be called during JXDOM traversal");
        RTS_ComponentManager.PTTEEventIDCounter++;
        RTS_ComponentManager.queuedPreTTEs.put(RTS_ComponentManager.PTTEEventIDCounter, event);
        return (RTS_ComponentManager.PTTEEventIDCounter);
    }

    public static void deregisterPreTTEEvent (Integer eventID) {
        if (RTS_ComponentManager.isTraversing)
            throw new RuntimeException("JXDOM: deregisterPreTTEEvent can not be called during JXDOM traversal");
        RTS_ComponentManager.queuedPreTTEs.remove(eventID);
    }

    public static List<Integer> children (@NotNull Integer... children) {
        childList.clear();
        Collections.addAll(childList, children);
        return (Collections.unmodifiableList(childList));
    }
    private static List<Integer> childList = new ArrayList<>();

    //------------------------------------------------------------------------------------------------------------------
}
