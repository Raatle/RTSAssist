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

package data.scripts.plugins.Setup;

import data.scripts.plugins.RTS_TaskManager;
import data.scripts.plugins.Utils.RTS_StateEngine;

public class RTS_TaskManagerAssSetup {

    public void buildAssEngine(RTS_StateEngine assEngine) {
        assEngine.addConditionSet(assType_moveAndRelease());
        assEngine.addConditionSet(assType_moveAndHold());
        assEngine.addConditionSet(assType_dynamicHoldOnFlux());
        assEngine.addConditionSet(assType_basicAttack());
        assEngine.addConditionSet(assType_attackAtAngle());
        assEngine.addConditionSet(assType_prioritiseEnemy());
    }

    public RTS_StateEngine.conditionSet assType_moveAndRelease() {
        RTS_StateEngine.conditionSet moveAndRelease = new RTS_StateEngine.conditionSet();
        moveAndRelease.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "coOrd";
            }
            @Override
            public Boolean check(Object var) {
                return (var != null);
            }
        });
        moveAndRelease.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "coOrdSec";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        moveAndRelease.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "primaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        moveAndRelease.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "moveAndRelease";
            }
            @Override
            public Boolean check(Object var) {
                return ((boolean)var);
            }
        });
        moveAndRelease.setResult = RTS_TaskManager.assType.moveAndRelease;
        return moveAndRelease;
    }

    public RTS_StateEngine.conditionSet assType_moveAndHold() {
        RTS_StateEngine.conditionSet moveAndHold = new RTS_StateEngine.conditionSet();
        moveAndHold.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "coOrd";
            }
            @Override
            public Boolean check(Object var) {
                return (var != null);
            }
        });
        moveAndHold.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "coOrdSec";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        moveAndHold.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "primaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        moveAndHold.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "moveAndRelease";
            }
            @Override
            public Boolean check(Object var) {
                return (!(boolean)var);
            }
        });
        moveAndHold.setResult = RTS_TaskManager.assType.moveAndHold;
        return moveAndHold;
    }

    public RTS_StateEngine.conditionSet assType_dynamicHoldOnFlux() {
        RTS_StateEngine.conditionSet dynamicHoldOnFlux = new RTS_StateEngine.conditionSet();
        dynamicHoldOnFlux.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "secondaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        dynamicHoldOnFlux.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "primaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        dynamicHoldOnFlux.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "coOrdSec";
            }
            @Override
            public Boolean check(Object var) {
                return (var != null);
            }
        });
        dynamicHoldOnFlux.setResult = RTS_TaskManager.assType.dynamicHoldOnFlux;
        return dynamicHoldOnFlux;
    }

    public RTS_StateEngine.conditionSet assType_basicAttack() {
        RTS_StateEngine.conditionSet basicAttack = new RTS_StateEngine.conditionSet();
        basicAttack.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "primaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var != null);
            }
        });
        basicAttack.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "coOrdSec";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        basicAttack.setResult = RTS_TaskManager.assType.basicAttack;
        return basicAttack;
    }

    public RTS_StateEngine.conditionSet assType_attackAtAngle() {
        RTS_StateEngine.conditionSet attackAtAngle = new RTS_StateEngine.conditionSet();
        attackAtAngle.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "primaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var != null);
            }
        });
        attackAtAngle.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "secondaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        attackAtAngle.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "coOrdSec";
            }
            @Override
            public Boolean check(Object var) {
                return (var != null);
            }
        });
        attackAtAngle.setResult = RTS_TaskManager.assType.attackAtAngle;
        return attackAtAngle;
    }

    public RTS_StateEngine.conditionSet assType_prioritiseEnemy() {
        RTS_StateEngine.conditionSet prioritiseEnemy = new RTS_StateEngine.conditionSet();
        prioritiseEnemy.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "primaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        prioritiseEnemy.conditionString.add(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "secondaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var != null);
            }
        });
        prioritiseEnemy.setResult = RTS_TaskManager.assType.prioritiseEnemy;
        return prioritiseEnemy;
    }
}