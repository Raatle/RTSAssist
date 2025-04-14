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
import java.util.concurrent.Callable;

import com.fs.starfarer.api.input.InputEventAPI;

public class RTS_Unused_InputManager extends RTS_StatefulClasses {
    public RTS_Unused_InputManager(Object state) {
        super(state);
        eventStorage.modifiers = new ArrayList<Character>();
        eventStorage.keyPresses = new ArrayList<Character>();
        eventStorage.LMB = 0;
        eventStorage.RMB = 0;
    }

    private HashMap<String, hotKey> hotKeys = new HashMap<>();

    public enum inputType {
        MODIFIER,
        KEYPRESS,
        LMB,
        RMB
    }

    private class hotKey {
        char modifier;
        char keyPress;
        int LMB;
        int RMB;
        Callable<Integer> job;
        Callable<Integer> additionalCheck;
    }

    private class eventStorageClass {
        ArrayList<Character> modifiers;
        ArrayList<Character> keyPresses;
        int LMB;
        int RMB;
    }

    private eventStorageClass eventStorage = new eventStorageClass();

    public void createNewHotkey(String name, HashMap<inputType, Object> input, Callable<Integer> job) {
        hotKey hold = new hotKey();
        hold.modifier = (char)216;
        hold.keyPress = (char)216;
        hold.LMB = 0;
        hold.RMB = 0;
        hold.additionalCheck = null;
        for (HashMap.Entry<inputType, Object> set: input.entrySet()) {
            if (set.getKey() == inputType.MODIFIER) hold.modifier = (char)set.getValue();
            if (set.getKey() == inputType.KEYPRESS) hold.keyPress = (char)set.getValue();
            if (set.getKey() == inputType.LMB) hold.LMB = (int)set.getValue();
            if (set.getKey() == inputType.RMB) hold.RMB = (int)set.getValue();
        }
        hold.job = job;
        hotKeys.put(name, hold);
    }

    public void createNewHotkey(String name, HashMap<inputType, Object> input, Callable<Integer> job, Callable<Integer> additionalCheck) {
        hotKey hold = new hotKey();
        hold.modifier = (char)216;
        hold.keyPress = (char)216;
        hold.LMB = 0;
        hold.RMB = 0;
        for (HashMap.Entry<inputType, Object> set: input.entrySet()) {
            if (set.getKey() == inputType.MODIFIER) hold.modifier = (char)set.getValue();
            if (set.getKey() == inputType.KEYPRESS) hold.keyPress = (char)set.getValue();
            if (set.getKey() == inputType.LMB) hold.LMB = (int)set.getValue();
            if (set.getKey() == inputType.RMB) hold.RMB = (int)set.getValue();
        }
        hold.job = job;
        hold.additionalCheck = additionalCheck;
        hotKeys.put(name, hold);
    }

    public void listenAndExecute(float amount, List<InputEventAPI> events) {
        eventStorage.keyPresses.clear();
        eventStorage.LMB = 0;
        eventStorage.RMB = 0;
        for (InputEventAPI x : events) {
            if (x.isKeyDownEvent()) {
                if (!(eventStorage.modifiers.contains((Character)x.getEventChar()))
                        && !eventStorage.modifiers.contains((Character)((char)(x.getEventChar() + 32)))) {
                    eventStorage.modifiers.add((Character)(x.getEventChar()));
                    eventStorage.modifiers.add((Character)((char)(x.getEventChar() + 32)));
                }
                if (!(eventStorage.keyPresses.contains((Character)x.getEventChar()))
                        && !eventStorage.keyPresses.contains((Character)((char)(x.getEventChar() + 32)))) {
                    eventStorage.keyPresses.add((Character) x.getEventChar());
                    eventStorage.keyPresses.add((Character)((char)(x.getEventChar() + 32)));
                }
            }
            if (x.isKeyUpEvent()) {
                eventStorage.modifiers.clear();
//                if ((eventStorage.modifiers.contains((Character) x.getEventChar()))
//                        || eventStorage.modifiers.contains((Character) ((char) (x.getEventChar() + 32)))) {
//                    eventStorage.modifiers.remove((Character) x.getEventChar());
//                    eventStorage.modifiers.remove((Character) ((char) (x.getEventChar() + 32)));
//                }
            }
            if (x.isLMBDownEvent()) eventStorage.LMB = 1;
            if (x.isLMBUpEvent()) eventStorage.LMB = 2;
            if (x.isRMBDownEvent()) eventStorage.RMB = 1;
            if (x.isRMBUpEvent()) eventStorage.RMB = 2;
        }
        for (HashMap.Entry<String, hotKey> set: hotKeys.entrySet()) {
            if ((set.getValue().modifier == (char)216) || eventStorage.modifiers.contains(set.getValue().modifier))
                if ((set.getValue().keyPress == (char)216) || eventStorage.keyPresses.contains(set.getValue().keyPress))
                    if ((set.getValue().LMB == 0) || eventStorage.LMB == set.getValue().LMB)
                        if ((set.getValue().RMB == 0 ) || eventStorage.RMB == set.getValue().RMB)
                            try {
                                if ((set.getValue().additionalCheck == null) || (set.getValue().additionalCheck.call() == 1))
                                    set.getValue().job.call();
                            } catch (Exception ignore){}
        }
    }
}