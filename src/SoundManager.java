import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestore audio del gioco (Singleton, come ResourceManager).
 * Effetti one-shot + tracce in loop (sigla menu, battle, aura) + volume + mute.
 * Robusto ai file mancanti: se un WAV non c'è, silenzio invece di crash.
 */
public class SoundManager {

    private static SoundManager instance;

    private final Map<String, byte[]> sfxData   = new HashMap<>();
    private final Map<String, AudioFormat> sfxFormat = new HashMap<>();

    private Clip musicClip;   // sigla (menu)
    private Clip battleClip;  // musica di battaglia
    private Clip auraClip;    // loop aura mentre il boost e' attivo

    private static final float BATTLE_GAIN_DB = -12.0f;

    private boolean muted = false;

    private SoundManager() { loadAllSounds(); }

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    private void loadAllSounds() {
        loadSfx("hit_light",   "hit_light.wav");
        loadSfx("hit_heavy",   "hit_heavy.wav");
        loadSfx("kiblast",     "kiblast.wav");
        loadSfx("kamehameha",  "kamehameha.wav");
        loadSfx("final_flash", "final_flash.wav");
        loadSfx("aura",        "aura.wav");
        loadSfx("teleport",    "teleport.wav");
        loadSfx("ko",          "KO.wav");
        loadSfx("footstep",    "footstep.wav");
        loadSfx("whoosh",      "whoosh.wav");
        loadSfx("ready_fight", "ready_fight.wav");
        loadSfx("landing",     "landing.wav");
        loadSfx("falling",     "falling.wav");
        loadSfx("block",       "block.wav");
        loadSfx("confirm",     "confirm.wav");
        loadSfx("select",      "select.wav");
        loadSfx("cancel",      "cancel.wav");
        loadSfx("victory",     "victory.wav");
        System.out.println("SoundManager: effetti caricati (" + sfxData.size() + ")");
    }

    private void loadSfx(String key, String fileName) {
        try (InputStream raw = getClass().getResourceAsStream("/assets/sounds/" + fileName)) {
            if (raw == null) {
                System.out.println("SoundManager: file non trovato -> " + fileName + " (skip)");
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(raw));
            AudioFormat format = ais.getFormat();
            byte[] data = ais.readAllBytes();
            sfxData.put(key, data);
            sfxFormat.put(key, format);
            ais.close();
        } catch (Exception e) {
            System.out.println("SoundManager: errore caricando " + fileName + " -> " + e.getMessage());
        }
    }

    public void play(String key) {
        if (muted) return;
        byte[] data = sfxData.get(key);
        AudioFormat format = sfxFormat.get(key);
        if (data == null || format == null) return;
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(format, data, 0, data.length);
            clip.addLineListener(event -> { if (event.getType() == LineEvent.Type.STOP) clip.close(); });
            clip.start();
        } catch (Exception e) { }
    }

    public Clip playAndReturn(String key) {
        if (muted) return null;
        byte[] data = sfxData.get(key);
        AudioFormat format = sfxFormat.get(key);
        if (data == null || format == null) return null;
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(format, data, 0, data.length);
            clip.addLineListener(event -> { if (event.getType() == LineEvent.Type.STOP) clip.close(); });
            clip.start();
            return clip;
        } catch (Exception e) { return null; }
    }

    // --- Musica menu (sigla) ---
    public void startMusic() {
        if (musicClip != null && musicClip.isRunning()) return;
        try (InputStream raw = getClass().getResourceAsStream("/assets/sounds/sigla.wav")) {
            if (raw == null) { System.out.println("SoundManager: sigla.wav non trovata"); return; }
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(raw));
            musicClip = AudioSystem.getClip();
            musicClip.open(ais);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            if (muted) musicClip.stop();
            ais.close();
        } catch (Exception e) { System.out.println("SoundManager: errore sigla -> " + e.getMessage()); }
    }

    public void stopMusic() {
        if (musicClip != null) { musicClip.stop(); musicClip.close(); musicClip = null; }
    }

    // --- Musica battaglia (battle, volume basso) ---
    public void startBattleMusic() {
        if (battleClip != null && battleClip.isRunning()) return;
        try (InputStream raw = getClass().getResourceAsStream("/assets/sounds/battle.wav")) {
            if (raw == null) { System.out.println("SoundManager: battle.wav non trovata"); return; }
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(raw));
            battleClip = AudioSystem.getClip();
            battleClip.open(ais);
            applyGain(battleClip, BATTLE_GAIN_DB);
            battleClip.loop(Clip.LOOP_CONTINUOUSLY);
            if (muted) battleClip.stop();
            ais.close();
        } catch (Exception e) { System.out.println("SoundManager: errore battle -> " + e.getMessage()); }
    }

    public void stopBattleMusic() {
        if (battleClip != null) { battleClip.stop(); battleClip.close(); battleClip = null; }
    }

    // --- Loop aura ---
    public void startAuraLoop() {
        if (auraClip != null && auraClip.isRunning()) return;
        try (InputStream raw = getClass().getResourceAsStream("/assets/sounds/aura_loop.wav")) {
            if (raw == null) { System.out.println("SoundManager: aura_loop.wav non trovata"); return; }
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(raw));
            auraClip = AudioSystem.getClip();
            auraClip.open(ais);
            auraClip.loop(Clip.LOOP_CONTINUOUSLY);
            if (muted) auraClip.stop();
            ais.close();
        } catch (Exception e) { System.out.println("SoundManager: errore aura loop -> " + e.getMessage()); }
    }

    public void stopAuraLoop() {
        if (auraClip != null) { auraClip.stop(); auraClip.close(); auraClip = null; }
    }

    private void applyGain(Clip clip, float db) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float clamped = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db));
                gain.setValue(clamped);
            }
        } catch (Exception e) { }
    }

    // --- Mute globale ---
    public void toggleMute() { setMuted(!muted); }

    public void setMuted(boolean value) {
        this.muted = value;
        if (muted) {
            if (musicClip  != null) musicClip.stop();
            if (battleClip != null) battleClip.stop();
            if (auraClip   != null) auraClip.stop();
        } else {
            if (musicClip  != null && !musicClip.isRunning())  musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            if (battleClip != null && !battleClip.isRunning()) battleClip.loop(Clip.LOOP_CONTINUOUSLY);
            if (auraClip   != null && !auraClip.isRunning())   auraClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public boolean isMuted() { return muted; }
}