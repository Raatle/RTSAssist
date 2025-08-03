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

package data.scripts.modInitilisation;

import com.fs.starfarer.api.Global;
import data.scripts.RTSAssistModPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static data.scripts.RTSAssistModPlugin.RTS_Global;

public class RTS_CommonsControl {
    public RTS_CommonsControl () throws JSONException, IOException {
        if (!Global.getSettings().fileExistsInCommon("RTS_common"))
            Global.getSettings().writeJSONToCommon("RTS_common", new JSONObject(), false);
        this.commons = Global.getSettings().readJSONFromCommon("RTS_common", false);
    }

    public static enum JSONType {
        INT,
        DOUBLE,
        STRING,
        JSONOBJECT,
        JSONARRAY,
    }

    public JSONObject commons;

    public void updateCache () {
        try {
            this.commons = Global.getSettings().readJSONFromCommon("RTS_common", false);
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    // public void update commons

    public Object get (String ident, RTS_CommonsControl.JSONType type) throws JSONException {
        if (!this.commons.has(ident))
            return (null);
        return (switch (type) {
            case INT -> this.commons.getInt(ident);
            case DOUBLE -> this.commons.getDouble(ident);
            case STRING -> this.commons.getString(ident);
            case JSONOBJECT -> this.commons.getJSONObject(ident);
            case JSONARRAY -> this.commons.getJSONArray(ident);
        });
    }

    public void set (String ident, int x) throws JSONException, IOException {
        this.commons.put(ident, x);
        Global.getSettings().writeJSONToCommon("RTS_common", this.commons, false);
    }
    public void set (String ident, float x) throws JSONException, IOException {
        this.commons.put(ident, x);
        Global.getSettings().writeJSONToCommon("RTS_common", this.commons, false);
    }
    public void set (String ident, double x) throws JSONException, IOException {
        this.commons.put(ident, x);
        Global.getSettings().writeJSONToCommon("RTS_common", this.commons, false);
    }
    public void set (String ident, String x) throws JSONException, IOException {
        this.commons.put(ident, x);
        Global.getSettings().writeJSONToCommon("RTS_common", this.commons, false);
    }
    public void set (String ident, JSONObject x) throws JSONException, IOException {
        this.commons.put(ident, x);
        Global.getSettings().writeJSONToCommon("RTS_common", this.commons, false);
    }
    public void set (String ident, JSONArray x) throws JSONException, IOException {
        this.commons.put(ident, x);
        Global.getSettings().writeJSONToCommon("RTS_common", this.commons, false);
    }
}