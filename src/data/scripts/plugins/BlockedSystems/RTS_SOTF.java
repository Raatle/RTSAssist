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