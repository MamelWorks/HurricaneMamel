package haven;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ND: Per-sound custom volume registry (ported concept from ArdClient's "customsfxvol").
 * Keeps a name->volume(0..100) map keyed by the sound resource name. Sounds auto-register
 * (at 100 = unchanged) the first time they are heard, so the settings list fills up as you
 * play. Only user-adjusted (non-100) values are persisted, so the pref stays small.
 */
public class SfxCustomVolume {
	private static final String PREF = "customSfxVolumes";
	private static final Map<String, Integer> vols = new TreeMap<>();
	private static boolean loaded = false;

	private static synchronized void load() {
		if(loaded)
			return;
		loaded = true;
		String s = Utils.getpref(PREF, "");
		if(s != null && !s.isEmpty()) {
			for(String pair : s.split(";")) {
				int eq = pair.lastIndexOf('=');
				if(eq > 0) {
					try {
						String name = pair.substring(0, eq);
						int v = Integer.parseInt(pair.substring(eq + 1));
						vols.put(name, Math.max(0, Math.min(100, v)));
					} catch(NumberFormatException e) {
						// skip malformed entry
					}
				}
			}
		}
	}

	public static synchronized void save() {
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, Integer> e : vols.entrySet()) {
			if(e.getValue() == 100) // default -> don't persist
				continue;
			if(sb.length() > 0)
				sb.append(';');
			sb.append(e.getKey()).append('=').append(e.getValue());
		}
		Utils.setpref(PREF, sb.toString());
	}

	/** Return the stored volume (0..100) for a sound, registering it at 100 if unseen. */
	public static synchronized int getRegister(String name) {
		if(!loaded)
			load();
		Integer v = vols.get(name);
		if(v == null) {
			vols.put(name, 100);
			return 100;
		}
		return v;
	}

	public static synchronized void set(String name, int v) {
		if(!loaded)
			load();
		vols.put(name, Math.max(0, Math.min(100, v)));
		save();
	}

	public static synchronized void clear() {
		if(!loaded)
			load();
		vols.clear();
		save();
	}

	public static synchronized List<String> names() {
		if(!loaded)
			load();
		return new ArrayList<>(vols.keySet());
	}

	/**
	 * Wrap an audio stream so its volume is scaled by this sound's custom setting.
	 * Auto-registers the sound name. Returns the stream unchanged when the setting is 100
	 * (no allocation / no behaviour change until the user lowers it).
	 */
	public static Audio.CS adjust(Audio.CS stream, String name) {
		if(name == null)
			return stream;
		int v = getRegister(name);
		if(v >= 100)
			return stream;
		return new Audio.VolAdjust(stream, v / 100.0);
	}
}
