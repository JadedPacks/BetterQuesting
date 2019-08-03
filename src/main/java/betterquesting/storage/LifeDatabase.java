package betterquesting.storage;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyInstance;
import betterquesting.questing.party.PartyManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public final class LifeDatabase {
	private static final HashMap<UUID, Integer> playerLives = new HashMap<>();
	private static final HashMap<Integer, Integer> partyLives = new HashMap<>();

	public static int getLives(UUID uuid) {
		if(uuid == null) {
			return 0;
		}
		if(playerLives.containsKey(uuid)) {
			return playerLives.get(uuid);
		} else {
			int def = QuestSettings.livesDef;
			playerLives.put(uuid, def);
			return def;
		}
	}

	public static void setLives(UUID uuid, int value) {
		if(uuid == null) {
			return;
		}
		playerLives.put(uuid, MathHelper.clamp_int(value, 0, QuestSettings.livesMax));
	}

	public static int getLives(PartyInstance party) {
		int id = party == null ? -1 : PartyManager.getKey(party);
		if(id < 0) {
			return 0;
		}
		if(partyLives.containsKey(id)) {
			return partyLives.get(id);
		} else {
			int def = QuestSettings.livesDef;
			partyLives.put(id, def);
			return def;
		}
	}

	public static void setLives(PartyInstance party, int value) {
		int id = party == null ? -1 : PartyManager.getKey(party);
		if(id < 0) {
			return;
		}
		partyLives.put(id, MathHelper.clamp_int(value, 0, QuestSettings.livesMax));
	}

	public static QuestingPacket getSyncPacket() {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("lives", writeToJson(new JsonObject()));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		return new QuestingPacket(PacketTypeNative.LIFE_DATABASE.GetLocation(), tags);
	}

	public static void readPacket(NBTTagCompound payload) {
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		readFromJson(JsonHelper.GetObject(base, "lives"));
	}

	public static JsonObject writeToJson(JsonObject json) {
		JsonArray jul = new JsonArray();
		for(Entry<UUID, Integer> entry : playerLives.entrySet()) {
			JsonObject j = new JsonObject();
			j.addProperty("uuid", entry.getKey().toString());
			j.addProperty("lives", entry.getValue());
			jul.add(j);
		}
		json.add("playerLives", jul);
		JsonArray jpl = new JsonArray();
		for(Entry<Integer, Integer> entry : partyLives.entrySet()) {
			JsonObject j = new JsonObject();
			j.addProperty("partyID", entry.getKey());
			j.addProperty("lives", entry.getValue());
			jpl.add(j);
		}
		json.add("partyLives", jpl);
		return json;
	}

	public static void readFromJson(JsonObject json) {
		playerLives.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "playerLives")) {
			if(entry == null || !entry.isJsonObject()) {
				continue;
			}
			JsonObject j = entry.getAsJsonObject();
			try {
				UUID uuid = UUID.fromString(JsonHelper.GetString(j, "uuid", ""));
				int lives = JsonHelper.GetInt(j, "lives", QuestSettings.livesDef);
				playerLives.put(uuid, lives);
			} catch(Exception ignored) {}
		}

		partyLives.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "partyLives")) {
			if(entry == null || !entry.isJsonObject()) {
				continue;
			}
			JsonObject j = entry.getAsJsonObject();
			int partyID = JsonHelper.GetInt(j, "partyID", -1);
			int lives = JsonHelper.GetInt(j, "lives", QuestSettings.livesDef);
			if(partyID >= 0) {
				partyLives.put(partyID, lives);
			}
		}
	}

	public static void reset() {
		playerLives.clear();
		partyLives.clear();
	}
}