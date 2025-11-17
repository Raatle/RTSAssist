package data.scripts.plugins.BlockedSystems;

import data.scripts.plugins.Utils.RTS_Context;

public class RTS_Template_Bs {

    /*
    *
    * */

    public void init () {
        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                return (false);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                return (false);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                return (false);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                return (false);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                return (false);
            }
        });

        RTS_BS_Utils.registerModifier(new RTS_BlockSystemPlugin() {
            @Override
            public String getSystemName() {
                return "";
            }

            @Override
            public boolean blockOnNextFrame(RTS_Context context) {
                return (false);
            }
        });
    }
}