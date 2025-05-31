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

package data.scripts.plugins.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RTS_StateEngine {

    List<conditionSet> conditionStrings = new ArrayList<>();
    HashMap<String, conditionSet> conditionIdentStore = new HashMap<>();

    public static class conditionSet {
        public List<condition> conditionString = new ArrayList<>();
        public Object setResult = null;
    }

    public interface condition {
        String identifier();
        Boolean check(Object var);
    }

    public String addConditionSet (conditionSet newConditionSet) {
        String identifier = UUID.randomUUID().toString();
        this.conditionIdentStore.put(identifier, newConditionSet);
        this.conditionStrings.add(newConditionSet);
        return (identifier);
    }

    public void removeConditionSet (String identifier) {
        this.conditionStrings.remove(conditionIdentStore.get(identifier));
    }

    Integer x;
    public Object doCheck(HashMap<String, Object> checkList) {
        x = 0;
        for (conditionSet cString : this.conditionStrings) {
            x = 1;
            for (condition instance : cString.conditionString) {
                if (checkList.containsKey(instance.identifier()) && instance.check(checkList.get(instance.identifier())))
                    continue;
                x = 0;
                break;
            }
            if (x == 1)
                return (cString.setResult);
        }
        return (null);
    }
}







































