package betterquesting.storage;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.storage.INameCache;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NameCache implements INameCache {
	public static final NameCache INSTANCE = new NameCache();
	private final ConcurrentHashMap<UUID, JsonObject> cache = new ConcurrentHashMap<>();

	@Override
	public String getName(UUID uuid) {
		if(!cache.containsKey(uuid)) {
			return uuid.toString();
		} else {
			return JsonHelper.GetString(cache.get(uuid), "name", "");
		}
	}

	@Override
	public UUID getUUID(String name) {
		for(Entry<UUID, JsonObject> entry : cache.entrySet()) {
			if(JsonHelper.GetString(entry.getValue(), "name", "").equalsIgnoreCase(name)) {
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	public boolean isOP(UUID uuid) {
		if(!cache.containsKey(uuid)) {
			return false;
		} else {
			return JsonHelper.GetBoolean(cache.get(uuid), "isOP", false);
		}
	}

	@Override
	public void updateNames(MinecraftServer server) {
		String[] names = server.getPlayerProfileCache().func_152654_a();
		for(String name : names) {
			EntityPlayerMP player = server.getConfigurationManager().getPlayerByUsername(name);
			GameProfile prof = player == null ? null : player.getGameProfile();
			if(prof != null) {
				UUID oldID = getUUID(prof.getName());
				while(oldID != null) {
					cache.remove(oldID);
					oldID = getUUID(prof.getName());
				}
				JsonObject json = new JsonObject();
				json.addProperty("name", prof.getName());
				json.addProperty("isOP", server.getConfigurationManager().canSendCommands(prof));
				cache.put(prof.getId(), json);
			}
		}
		PacketSender.INSTANCE.sendToAll(getSyncPacket());
	}

	@Override
	public int size() {
		return cache.size();
	}

	@Override
	public QuestingPacket getSyncPacket() {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json = new JsonObject();
		json.add("cache", this.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		return new QuestingPacket(PacketTypeNative.NAME_CACHE.GetLocation(), tags);
	}

	@Override
	public void readPacket(NBTTagCompound payload) {
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		readFromJson(JsonHelper.GetArray(base, "cache"), EnumSaveType.CONFIG);
	}

	@Override
	public JsonArray writeToJson(JsonArray json, EnumSaveType saveType) {
		if(saveType != EnumSaveType.CONFIG) {
			return json;
		}
		for(Entry<UUID, JsonObject> entry : cache.entrySet()) {
			JsonObject jn = new JsonObject();
			jn.addProperty("uuid", entry.getKey().toString());
			jn.addProperty("name", JsonHelper.GetString(entry.getValue(), "name", ""));
			jn.addProperty("isOP", JsonHelper.GetBoolean(entry.getValue(), "isOP", false));
			json.add(jn);
		}
		return json;
	}

	@Override
	public void readFromJson(JsonArray json, EnumSaveType saveType) {
		if(saveType != EnumSaveType.CONFIG) {
			return;
		}
		cache.clear();
		for(JsonElement element : json) {
			if(element == null || !element.isJsonObject()) {
				continue;
			}
			JsonObject jn = element.getAsJsonObject();
			try {
				UUID uuid = UUID.fromString(JsonHelper.GetString(jn, "uuid", ""));
				String name = JsonHelper.GetString(jn, "name", "");
				boolean isOP = JsonHelper.GetBoolean(jn, "isOP", false);

				JsonObject j2 = new JsonObject();
				j2.addProperty("name", name);
				j2.addProperty("isOP", isOP);
				cache.put(uuid, j2);
			} catch(Exception ignored) {}
		}
	}

	public void reset() {
		cache.clear();
	}

	@Override
	public List<String> getAllNames() {
		List<String> list = new ArrayList<>();
		for(JsonObject json : cache.values()) {
			if(json != null && json.has("name")) {
				list.add(JsonHelper.GetString(json, "name", ""));
			}
		}
		return list;
	}
}