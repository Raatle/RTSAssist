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

public class RTS_StateEngine {

    List<conditionSet> conditionSets = new ArrayList<>();
    HashMap<String, conditionSet> conditionIdentStore = new HashMap<>();
    HashMap<conditionSet, List<String>> conditionSetStrings = new HashMap<>();

    public static class conditionSet {
        private List<condition> conditionString = new ArrayList<>();
        private action action = null;

        public void addCondition (condition condition) {
            conditionString.add(condition);
        }

        public void setResult (action action) {
            this.action = action;
        }

        public Object getResult () {
            return (this.action.run());
        }
    }

    public interface action {
        Object run ();
    }

    public interface condition {
        String identifier();
        Boolean check(Object var);
    }

    public String addConditionSet (conditionSet newConditionSet) {
        String identifier = UUID.randomUUID().toString();
        this.conditionIdentStore.put(identifier, newConditionSet);
        this.conditionSets.add(newConditionSet);
        this.conditionSetStrings.put(newConditionSet, new ArrayList<>());
        for (condition condition : newConditionSet.conditionString)
            this.conditionSetStrings.get(newConditionSet).add(condition.identifier());
        return (identifier);
    }

    public void removeConditionSet (String identifier) {
        this.conditionSets.remove(conditionIdentStore.get(identifier));
        this.conditionSetStrings.remove(conditionIdentStore.get(identifier));
    }

    public Object doCheck (HashMap<String, Object> checkList) {
        return (this.doCheck(checkList, false));
    }
    public Object doCheck (HashMap<String, Object> checkList, Boolean allowMultipleHits) {
        List<Object> hits = null;
        if (allowMultipleHits)
            hits = new ArrayList<>();
        int x;
        for (conditionSet conditionSet : this.conditionSets) {
            x = 1;
            if (!checkList.keySet().containsAll(this.conditionSetStrings.get(conditionSet)))
                continue;
            for (condition conditionInstance : conditionSet.conditionString) {
                if (conditionInstance.check(checkList.get(conditionInstance.identifier())))
                    continue;
                x = 0;
                break;
            }
            if (x == 1) {
                if (allowMultipleHits) {
                    hits.add(conditionSet.getResult());
                }
                else
                    return (conditionSet.getResult());
            }
        }
        return (allowMultipleHits && !hits.isEmpty() ? hits : null);
    }
}







































