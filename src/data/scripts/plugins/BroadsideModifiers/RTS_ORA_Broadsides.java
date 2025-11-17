package data.scripts.plugins.BroadsideModifiers;

import java.util.HashMap;

public class RTS_ORA_Broadsides {

    public RTS_ORA_Broadsides () {
        this.broadsideData.put("ora_beatitude", -60f);
        this.broadsideData.put("ora_bliss", -80f);
        this.broadsideData.put("ora_felicity", -45f);
        this.broadsideData.put("ora_revelation", -60f);
        this.broadsideData.put("ora_sanctuary", -60f);
        this.broadsideData.put("ora_enlightenment", -90f);
        this.broadsideData.put("ora_harmony", -60f);
        this.broadsideData.put("ora_discernment", -80f);

    }

    HashMap<String, Float> broadsideData = new HashMap<>();
    public HashMap<String, Float> getBroadSideData() {
        return (this.broadsideData);
    }
}