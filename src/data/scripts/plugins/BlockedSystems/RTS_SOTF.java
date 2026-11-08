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

package data.scripts.plugins.BlockedSystems;

import com.fs.starfarer.api.Global;
import data.scripts.plugins.Utils.RTS_Context;

public class RTS_SOTF {

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            String targetID;

            @Override
            public String getSystemName() {
                return "sotf_entropiccatalyst";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                this.init(context);
                boolean flag = true;
                if (this.hasEnemy()) {
                    this.targetID = context.getTargetEnemy() != null
                            ? context.getTargetEnemy().getId()
                            : context.getPriorityEnemy().getId();
                    if (Global.getCombatEngine().getCustomData().get(this.targetID + "_entropy_target_data") == null)
                        flag = false;
                }
                this.useOnCoolDown(flag);
                return (flag);
            }
        });
    }
}