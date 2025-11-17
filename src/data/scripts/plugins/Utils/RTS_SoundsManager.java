package data.scripts.plugins.Utils;

import com.fs.starfarer.D.A;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import data.scripts.plugins.RTSAssist;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class RTS_SoundsManager extends RTS_StatefulClasses {

    public RTS_SoundsManager (Object state) {
        super(state);
    }

    private Vector2f blankVec = new Vector2f();
    private Vector2f centre;
    private float masterUIVolume;
    private HashMap<String, SoundAPI> soundStore = new HashMap<>();

    public void update () {
        this.masterUIVolume = 2f * (float)this.getDeepState(Arrays.asList(
                RTSAssist.stNames.config,
                RTSAssist.coNames.UICommandVolume)
        ) / 10f;
        this.centre = ((CombatEngineAPI)this.getState(RTSAssist.stNames.engine)).getViewport().getCenter();
        /* Why: any time the camera moves rapidly, audio will be cut. This fixes it. */
        List<String> queueDelete = new ArrayList<>();
        for (Map.Entry<String, SoundAPI> sound : this.soundStore.entrySet())
            if (sound.getValue() != null) {
                if (sound.getValue().isPlaying())
                    sound.getValue().setLocation(this.centre.getX(), this.centre.getY());
                else
                    queueDelete.add(sound.getKey());
            }
        for (String id : queueDelete)
            this.soundStore.put(id, null);
    }

    public SoundAPI playBasicUISound (String id, float pitch, float volume) {
        SoundAPI newSound = Global.getSoundPlayer().playSound(
                id,
                pitch,
                volume * this.masterUIVolume,
                this.centre,
                this.blankVec
        );
        this.soundStore.put(id, newSound);
        return (newSound);
    }
}
