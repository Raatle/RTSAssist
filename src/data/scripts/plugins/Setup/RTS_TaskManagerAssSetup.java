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

    public void buildAssEngine (RTS_StateEngine assEngine) {
        assEngine.addConditionSet(this.assType_moveAndRelease());
        assEngine.addConditionSet(this.assType_moveAndHold());
        assEngine.addConditionSet(this.assType_dynamicHoldOnFlux());
        assEngine.addConditionSet(this.assType_basicAttack());
        assEngine.addConditionSet(this.assType_attackAtAngle());
        assEngine.addConditionSet(this.assType_prioritiseEnemy());
    }

    private RTS_StateEngine.conditionSet assType_moveAndRelease() {
        RTS_StateEngine.conditionSet moveAndRelease = new RTS_StateEngine.conditionSet();
        moveAndRelease.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                return (RTS_TaskManager.assType.moveAndRelease);
            }
        });
        moveAndRelease.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "coOrd";
            }
            @Override
            public Boolean check(Object var) {
                return (var != null);
            }
        });
        moveAndRelease.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "coOrdSec";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        moveAndRelease.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "primaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        moveAndRelease.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "moveAndRelease";
            }
            @Override
            public Boolean check(Object var) {
                return ((boolean)var);
            }
        });
        return moveAndRelease;
    }

    private RTS_StateEngine.conditionSet assType_moveAndHold() {
        RTS_StateEngine.conditionSet moveAndHold = new RTS_StateEngine.conditionSet();
        moveAndHold.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                return (RTS_TaskManager.assType.moveAndHold);
            }
        });
        moveAndHold.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "coOrd";
            }
            @Override
            public Boolean check(Object var) {
                return (var != null);
            }
        });
        moveAndHold.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "coOrdSec";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        moveAndHold.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "primaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        moveAndHold.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "moveAndRelease";
            }
            @Override
            public Boolean check(Object var) {
                return (!(boolean)var);
            }
        });
        return moveAndHold;
    }

    private RTS_StateEngine.conditionSet assType_dynamicHoldOnFlux() {
        RTS_StateEngine.conditionSet dynamicHoldOnFlux = new RTS_StateEngine.conditionSet();
        dynamicHoldOnFlux.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                return (RTS_TaskManager.assType.dynamicHoldOnFlux);
            }
        });
        dynamicHoldOnFlux.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "secondaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        dynamicHoldOnFlux.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "primaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        dynamicHoldOnFlux.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "coOrdSec";
            }
            @Override
            public Boolean check(Object var) {
                return (var != null);
            }
        });
        return dynamicHoldOnFlux;
    }

    private RTS_StateEngine.conditionSet assType_basicAttack () {
        RTS_StateEngine.conditionSet basicAttack = new RTS_StateEngine.conditionSet();
        basicAttack.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                return (RTS_TaskManager.assType.basicAttack);
            }
        });
        basicAttack.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "primaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var != null);
            }
        });
        basicAttack.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "coOrdSec";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        return basicAttack;
    }

    private RTS_StateEngine.conditionSet assType_attackAtAngle() {
        RTS_StateEngine.conditionSet attackAtAngle = new RTS_StateEngine.conditionSet();
        attackAtAngle.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                return (RTS_TaskManager.assType.attackAtAngle);
            }
        });
        attackAtAngle.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "primaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var != null);
            }
        });
        attackAtAngle.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "secondaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        attackAtAngle.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "coOrdSec";
            }
            @Override
            public Boolean check(Object var) {
                return (var != null);
            }
        });
        return attackAtAngle;
    }

    private RTS_StateEngine.conditionSet assType_prioritiseEnemy() {
        RTS_StateEngine.conditionSet prioritiseEnemy = new RTS_StateEngine.conditionSet();
        prioritiseEnemy.setResult(new RTS_StateEngine.action() {
            @Override
            public Object run() {
                return (RTS_TaskManager.assType.prioritiseEnemy);
            }
        });
        prioritiseEnemy.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "primaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var == null);
            }
        });
        prioritiseEnemy.addCondition(new RTS_StateEngine.condition() {
            @Override
            public String identifier() {
                return "secondaryEnemy";
            }
            @Override
            public Boolean check(Object var) {
                return (var != null);
            }
        });
        return prioritiseEnemy;
    }
}