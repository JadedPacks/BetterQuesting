package betterquesting.questing;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.PacketTypeNative;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public final class QuestDatabase {
	private static final ConcurrentHashMap<Integer, QuestInstance> database = new ConcurrentHashMap<>();

	public static QuestInstance createNew() {
		return new QuestInstance();
	}

	public static Integer nextKey() {
		int id = 0;
		while(database.containsKey(id)) {
			id++;
		}
		return id;
	}

	public static void add(QuestInstance obj, Integer id) {
		if(id < 0 || obj == null || database.containsKey(id) || database.containsValue(obj)) {
			return;
		}
		database.put(id, obj);
	}

	public static boolean removeKey(Integer id) {
		QuestInstance remQ = database.remove(id);
		if(remQ == null) {
			return false;
		}
		for(QuestInstance quest : getAllValues()) {
			quest.getPrerequisites().remove(remQ);
		}
		QuestLineDatabase.removeQuest(id);
		return true;
	}

	public static boolean removeValue(QuestInstance quest) {
		return removeKey(getKey(quest));
	}

	public static QuestInstance getValue(Integer id) {
		return database.get(id);
	}

	public static Integer getKey(QuestInstance quest) {
		for(Entry<Integer, QuestInstance> entry : database.entrySet()) {
			if(entry.getValue() == quest) {
				return entry.getKey();
			}
		}
		return -1;
	}

	public static List<QuestInstance> getAllValues() {
		return new ArrayList<>(database.values());
	}

	public static List<Integer> getAllKeys() {
		return new ArrayList<>(((Map<Integer, QuestInstance>) database).keySet());
	}

	public static int size() {
		return database.size();
	}

	public static void reset() {
		database.clear();
	}

	public static QuestingPacket getSyncPacket() {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("config", writeToJson(new JsonArray(), EnumSaveType.CONFIG));
		base.add("progress", writeToJson(new JsonArray(), EnumSaveType.PROGRESS));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		return new QuestingPacket(PacketTypeNative.QUEST_DATABASE.GetLocation(), tags);
	}

	public static void readPacket(NBTTagCompound payload) {
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		readFromJson(JsonHelper.GetArray(base, "config"), EnumSaveType.CONFIG);
		readFromJson(JsonHelper.GetArray(base, "progress"), EnumSaveType.PROGRESS);
	}

	public static JsonArray writeToJson(JsonArray json, EnumSaveType saveType) {
		switch(saveType) {
			case CONFIG:
				writeToJson_Config(json);
				break;
			case PROGRESS:
				writeToJson_Progress(json);
				break;
			default:
				break;
		}
		return json;
	}

	public static void readFromJson(JsonArray json, EnumSaveType saveType) {
		switch(saveType) {
			case CONFIG:
				readFromJson_Config(json);
				break;
			case PROGRESS:
				readFromJson_Progress(json);
				break;
			default:
				break;
		}
	}

	private static void writeToJson_Config(JsonArray json) {
		for(Entry<Integer, QuestInstance> entry : database.entrySet()) {
			JsonObject jq = new JsonObject();
			entry.getValue().writeToJson(jq, EnumSaveType.CONFIG);
			jq.addProperty("questID", entry.getKey());
			json.add(jq);
		}
	}

	private static void readFromJson_Config(JsonArray json) {
		database.clear();
		for(JsonElement entry : json) {
			if(entry == null || !entry.isJsonObject()) {
				continue;
			}
			int qID = JsonHelper.GetInt(entry.getAsJsonObject(), "questID", -1);
			if(qID < 0) {
				continue;
			}
			QuestInstance quest = getValue(qID);
			quest = quest != null ? quest : createNew();
			quest.readFromJson(entry.getAsJsonObject(), EnumSaveType.CONFIG);
			database.put(qID, quest);
		}
	}

	private static void writeToJson_Progress(JsonArray json) {
		for(Entry<Integer, QuestInstance> entry : database.entrySet()) {
			JsonObject jq = new JsonObject();
			entry.getValue().writeToJson(jq, EnumSaveType.PROGRESS);
			jq.addProperty("questID", entry.getKey());
			json.add(jq);
		}
	}

	private static void readFromJson_Progress(JsonArray json) {
		for(JsonElement entry : json) {
			if(entry == null || !entry.isJsonObject()) {
				continue;
			}
			int qID = JsonHelper.GetInt(entry.getAsJsonObject(), "questID", -1);
			if(qID < 0) {
				continue;
			}
			QuestInstance quest = getValue(qID);
			if(quest != null) {
				quest.readFromJson(entry.getAsJsonObject(), EnumSaveType.PROGRESS);
			}
		}
	}
}