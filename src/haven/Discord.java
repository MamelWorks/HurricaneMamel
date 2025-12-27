/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;

import java.time.OffsetDateTime;

public class Discord {
	// Discord Application ID for Haven & Hearth
	private static final long APPLICATION_ID = 1454227144874922178L;

	// World genus to name mapping
	private static final java.util.Map<String, String> WORLD_NAMES = new java.util.HashMap<String, String>() {{
		put("b7c199a4557503a8", "World 16.1");
		put("c646473983afec09", "World 16");
	}};

	private IPCClient client;
	private RichPresence.Builder presenceBuilder;
	private boolean connected = false;
	private String currentWorld = null;

	private Discord() {
		try {
			client = new IPCClient(APPLICATION_ID);
			client.setListener(new IPCListener() {
				@Override
				public void onReady(IPCClient client) {
					connected = true;
					System.out.println("Discord Rich Presence connected");
					// Set initial presence
					updatePresence();
				}

				@Override
				public void onClose(IPCClient client, org.json.JSONObject json) {
					connected = false;
					System.out.println("Discord Rich Presence disconnected");
				}

				@Override
				public void onDisconnect(IPCClient client, Throwable t) {
					connected = false;
					System.err.println("Discord Rich Presence error: " + t.getMessage());
				}
			});

			// Connect to Discord
			client.connect();

			// Initialize presence builder with default values
			presenceBuilder = new RichPresence.Builder()
				.setState("In Menus")
				.setDetails("Playing Haven & Hearth")
				.setStartTimestamp(OffsetDateTime.now())
				.setLargeImage("hafen_logo", "Haven & Hearth");

		} catch (NoDiscordClientException e) {
			System.err.println("Discord client not found. Rich Presence will not be available.");
			connected = false;
		} catch (Exception e) {
			System.err.println("Failed to initialize Discord Rich Presence: " + e.getMessage());
			e.printStackTrace();
			connected = false;
		}
	}

	private static Discord instance = null;

	public static synchronized Discord get() {
		if (MainFrame.runningThroughDiscord) {
			if (instance == null) {
				try {
					instance = new Discord();
				} catch (NoClassDefFoundError e) {
					System.err.println("Discord IPC library not found: " + e.getMessage());
					return null;
				} catch (Exception e) {
					System.err.println("Failed to create Discord instance: " + e.getMessage());
					e.printStackTrace();
					return null;
				}
			}
		}
		return instance;
	}

	private void updatePresence() {
		if (connected && client != null && presenceBuilder != null) {
			try {
				client.sendRichPresence(presenceBuilder.build());
			} catch (Exception e) {
				System.err.println("Failed to update Discord presence: " + e.getMessage());
			}
		}
	}

	public synchronized void setState(String state) {
		if (presenceBuilder != null) {
			presenceBuilder.setState(state);
			updatePresence();
		}
	}

	public synchronized void setDetails(String details) {
		if (presenceBuilder != null) {
			presenceBuilder.setDetails(details);
			updatePresence();
		}
	}

	public synchronized void setParty(String partyId, int partySize, int partyMax) {
		if (presenceBuilder != null) {
			if (partyId != null) {
				presenceBuilder.setParty(partyId, partySize, partyMax);
				setState("In a party (" + partySize + " members)");
			} else {
				presenceBuilder.setParty(null, 0, 0);
				setState("Playing solo");
			}
			updatePresence();
		}
	}

	public synchronized void clearParty() {
		setParty(null, 0, 0);
	}

	public synchronized void setWorld(String genus) {
		if (presenceBuilder != null) {
			String worldName = WORLD_NAMES.getOrDefault(genus, "Unknown World");
			currentWorld = worldName;
			setDetails("Playing on " + worldName);
		}
	}

	public synchronized void setLargeImage(String key, String text) {
		if (presenceBuilder != null) {
			presenceBuilder.setLargeImage(key, text);
			updatePresence();
		}
	}

	public synchronized void setSmallImage(String key, String text) {
		if (presenceBuilder != null) {
			presenceBuilder.setSmallImage(key, text);
			updatePresence();
		}
	}

	public void shutdown() {
		if (client != null && connected) {
			try {
				client.close();
			} catch (Exception e) {
				System.err.println("Error closing Discord client: " + e.getMessage());
			}
		}
	}

	public boolean isConnected() {
		return connected;
	}
}
