package snownee.jade.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.loading.FMLPaths;
import mcp.mobius.waila.Waila;
import snownee.jade.JadePlugin;
import mcp.mobius.waila.api.impl.config.PluginConfig;
import mcp.mobius.waila.utils.WailaExceptionHandler;

public final class UsernameCache {

	private static final HashMap<UUID, String> map = new HashMap<>();
	private static final Set<UUID> downloadingList = Collections.synchronizedSet(new HashSet<>());

	private static final Path saveFile = FMLPaths.CONFIGDIR.get().resolve(Waila.MODID + "/usernamecache.json");
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static boolean loading = false;

	private UsernameCache() {
	}

	/**
	 * Set a player's current username
	 *
	 * @param uuid     the player's {@link java.util.UUID UUID}
	 * @param username the player's username
	 */
	public static void setUsername(UUID uuid, String username) {
		Objects.requireNonNull(uuid);
		Objects.requireNonNull(username);

		if (!isValidName(username))
			return;

		String prev = map.put(uuid, username);
		if (!loading && !Objects.equals(prev, username)) {
			save();
		}
	}

	public static boolean isValidName(String name) {
		return !name.isEmpty() && !name.contains("§");
	}

	/**
	 * Remove a player's username from the cache
	 *
	 * @param uuid the player's {@link java.util.UUID UUID}
	 * @return if the cache contained the user
	 */
	public static boolean removeUsername(UUID uuid) {
		Objects.requireNonNull(uuid);

		if (map.remove(uuid) != null) {
			save();
			return true;
		}

		return false;
	}

	/**
	 * Get the player's last known username
	 * <p>
	 * <b>May be <code>null</code></b>
	 *
	 * @param uuid the player's {@link java.util.UUID UUID}
	 * @return the player's last known username, or <code>null</code> if the
	 * cache doesn't have a record of the last username
	 */
	@Nullable
	public static String getLastKnownUsername(UUID uuid) {
		Objects.requireNonNull(uuid);
		return map.get(uuid);
	}

	/**
	 * Check if the cache contains the given player's username
	 *
	 * @param uuid the player's {@link java.util.UUID UUID}
	 * @return if the cache contains a username for the given player
	 */
	public static boolean containsUUID(UUID uuid) {
		Objects.requireNonNull(uuid);
		return map.containsKey(uuid);
	}

	/**
	 * Get an immutable copy of the cache's underlying map
	 *
	 * @return the map
	 */
	public static Map<UUID, String> getMap() {
		return ImmutableMap.copyOf(map);
	}

	/**
	 * Save the cache to file
	 */
	public static void save() {
		new SaveThread(gson.toJson(map)).start();
	}

	/**
	 * Load the cache from file
	 */
	public static void load() {
		if (!Files.exists(saveFile))
			return;

		loading = true;
		try (final BufferedReader reader = Files.newBufferedReader(saveFile, Charsets.UTF_8)) {
			@SuppressWarnings("serial")
			Type type = new TypeToken<Map<UUID, String>>() {
			}.getType();
			Map<UUID, String> tempMap = gson.fromJson(reader, type);
			if (tempMap != null) {
				map.clear();
				tempMap.forEach(UsernameCache::setUsername);
			}
		} catch (Exception e) {
			Waila.LOGGER.error("Could not parse username cache file as valid json, deleting file {}", saveFile, e);
			WailaExceptionHandler.handleErr(e, null, null);
			try {
				Files.delete(saveFile);
			} catch (IOException e1) {
				Waila.LOGGER.error("Could not delete file {}", saveFile.toString());
			}
		} finally {
			loading = false;
		}
	}

	/**
	 * Used for saving the {@link com.google.gson.Gson#toJson(Object) Gson}
	 * representation of the cache to disk
	 */
	private static class SaveThread extends Thread {

		/**
		 * The data that will be saved to disk
		 */
		private final String data;

		public SaveThread(String data) {
			this.data = data;
		}

		@Override
		public void run() {
			try {
				// Make sure we don't save when another thread is still saving
				synchronized (saveFile) {
					Files.write(saveFile, data.getBytes(StandardCharsets.UTF_8));
				}
			} catch (IOException e) {
				Waila.LOGGER.error("Failed to save username cache to file!");
			}
		}
	}
}
