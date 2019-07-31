package betterquesting.questing;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
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

public final class QuestDatabase implements IQuestDatabase {
	public static final QuestDatabase INSTANCE = new QuestDatabase();
	private final ConcurrentHashMap<Integer, IQuest> database = new ConcurrentHashMap<>();

	@Override
	public IQuest createNew() {
		IQuest q = new QuestInstance();
		q.setParentDatabase(this);
		return q;
	}

	@Override
	public Integer nextKey() {
		int id = 0;
		while(database.containsKey(id)) {
			id++;
		}
		return id;
	}

	@Override
	public void add(IQuest obj, Integer id) {
		if(id < 0 || obj == null || database.containsKey(id) || database.containsValue(obj)) {
			return;
		}

		obj.setParentDatabase(this);
		database.put(id, obj);
	}

	@Override
	public boolean removeKey(Integer id) {
		IQuest remQ = database.remove(id);
		if(remQ == null) {
			return false;
		}
		for(IQuest quest : this.getAllValues()) {
			quest.getPrerequisites().remove(remQ);
		}
		QuestLineDatabase.INSTANCE.removeQuest(id);
		return true;
	}

	@Override
	public boolean removeValue(IQuest quest) {
		return removeKey(getKey(quest));
	}

	@Override
	public IQuest getValue(Integer id) {
		return database.get(id);
	}

	@Override
	public Integer getKey(IQuest quest) {
		for(Entry<Integer, IQuest> entry : database.entrySet()) {
			if(entry.getValue() == quest) {
				return entry.getKey();
			}
		}
		return -1;
	}

	@Override
	public List<IQuest> getAllValues() {
		return new ArrayList<>(database.values());
	}

	@Override
	public List<Integer> getAllKeys() {
		return new ArrayList<>(((Map<Integer, IQuest>) database).keySet());
	}

	@Override
	public int size() {
		return database.size();
	}

	@Override
	public void reset() {
		database.clear();
	}

	@Override
	public QuestingPacket getSyncPacket() {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("config", writeToJson(new JsonArray(), EnumSaveType.CONFIG));
		base.add("progress", writeToJson(new JsonArray(), EnumSaveType.PROGRESS));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		return new QuestingPacket(PacketTypeNative.QUEST_DATABASE.GetLocation(), tags);
	}

	@Override
	public void readPacket(NBTTagCompound payload) {
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		readFromJson(JsonHelper.GetArray(base, "config"), EnumSaveType.CONFIG);
		readFromJson(JsonHelper.GetArray(base, "progress"), EnumSaveType.PROGRESS);
	}

	@Override
	public JsonArray writeToJson(JsonArray json, EnumSaveType saveType) {
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

	@Override
	public void readFromJson(JsonArray json, EnumSaveType saveType) {
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

	private void writeToJson_Config(JsonArray json) {
		for(Entry<Integer, IQuest> entry : database.entrySet()) {
			JsonObject jq = new JsonObject();
			entry.getValue().writeToJson(jq, EnumSaveType.CONFIG);
			jq.addProperty("questID", entry.getKey());
			json.add(jq);
		}
	}

	private void readFromJson_Config(JsonArray json) {
		database.clear();
		for(JsonElement entry : json) {
			if(entry == null || !entry.isJsonObject()) {
				continue;
			}
			int qID = JsonHelper.GetNumber(entry.getAsJsonObject(), "questID", -1).intValue();
			if(qID < 0) {
				continue;
			}
			IQuest quest = getValue(qID);
			quest = quest != null ? quest : this.createNew();
			quest.readFromJson(entry.getAsJsonObject(), EnumSaveType.CONFIG);
			database.put(qID, quest);
		}
	}

	private void writeToJson_Progress(JsonArray json) {
		for(Entry<Integer, IQuest> entry : database.entrySet()) {
			JsonObject jq = new JsonObject();
			entry.getValue().writeToJson(jq, EnumSaveType.PROGRESS);
			jq.addProperty("questID", entry.getKey());
			json.add(jq);
		}
	}

	private void readFromJson_Progress(JsonArray json) {
		for(JsonElement entry : json) {
			if(entry == null || !entry.isJsonObject()) {
				continue;
			}
			int qID = JsonHelper.GetNumber(entry.getAsJsonObject(), "questID", -1).intValue();
			if(qID < 0) {
				continue;
			}
			IQuest quest = getValue(qID);
			if(quest != null) {
				quest.readFromJson(entry.getAsJsonObject(), EnumSaveType.PROGRESS);
			}
		}
	}
}