package betterquesting.storage;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NameCache {
	private static final ConcurrentHashMap<UUID, JsonObject> cache = new ConcurrentHashMap<>();
	private static HashMap<UUID, PlayerInstance> players = new HashMap<>();

	public static UUID getQuestingUUID(EntityPlayer player) {
		if(player == null) {
			return null;
		}
		return player.getGameProfile().getId();
	}

	public static boolean isOP(EntityPlayer player) {
		return player.canCommandSenderUseCommand(2, "");
	}

	public static PlayerInstance getInstance(UUID uuid) {
		return players.get(uuid);
	}

	public static String getName(UUID uuid) {
		if(!cache.containsKey(uuid)) {
			return uuid.toString();
		} else {
			return JsonHelper.GetString(cache.get(uuid), "name", "");
		}
	}

	public static UUID getUUID(String name) {
		for(Entry<UUID, JsonObject> entry : cache.entrySet()) {
			if(JsonHelper.GetString(entry.getValue(), "name", "").equalsIgnoreCase(name)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static void updateNames(MinecraftServer server) {
		for(String name : server.getPlayerProfileCache().func_152654_a()) {
			EntityPlayerMP player = server.getConfigurationManager().getPlayerByUsername(name);
			if(player != null) {
				UUID oldID;
				while((oldID = getUUID(player.getDisplayName())) != null) {
					cache.remove(oldID);
				}
				JsonObject json = new JsonObject();
				json.addProperty("name", player.getDisplayName());
				cache.put(player.getUniqueID(), json);
			}
		}
		PacketSender.sendToAll(getSyncPacket());
	}

	public static int size() {
		return cache.size();
	}

	public static QuestingPacket getSyncPacket() {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json = new JsonObject();
		json.add("cache", writeToJson(new JsonArray()));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		return new QuestingPacket(PacketTypeNative.NAME_CACHE.GetLocation(), tags);
	}

	public static void readPacket(NBTTagCompound payload) {
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		readFromJson(JsonHelper.GetArray(base, "cache"));
	}

	public static JsonArray writeToJson(JsonArray json) {
		for(Entry<UUID, JsonObject> entry : cache.entrySet()) {
			JsonObject jn = new JsonObject();
			jn.addProperty("uuid", entry.getKey().toString());
			jn.addProperty("name", JsonHelper.GetString(entry.getValue(), "name", ""));
			json.add(jn);
		}
		return json;
	}

	public static void readFromJson(JsonArray json) {
		cache.clear();
		for(JsonElement element : json) {
			if(element == null || !element.isJsonObject()) {
				continue;
			}
			JsonObject jn = element.getAsJsonObject();
			try {
				JsonObject j2 = new JsonObject();
				j2.addProperty("name", JsonHelper.GetString(jn, "name", ""));
				UUID uuid = UUID.fromString(JsonHelper.GetString(jn, "uuid", ""));
				cache.put(uuid, j2);
				players.put(uuid, new PlayerInstance(uuid));
			} catch(Exception ignored) {}
		}
	}

	public static void reset() {
		cache.clear();
	}

	public static List<String> getAllNames() {
		List<String> list = new ArrayList<>();
		for(JsonObject json : cache.values()) {
			if(json != null && json.has("name")) {
				list.add(JsonHelper.GetString(json, "name", ""));
			}
		}
		return list;
	}
}