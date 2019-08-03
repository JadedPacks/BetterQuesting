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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class QuestLine {
	public String name = "New Quest Line", desc = "No Description";
	private final HashMap<Integer, QuestLineEntry> questList = new HashMap<>();

	public int getQuestAt(int x, int y) {
		for(Entry<Integer, QuestLineEntry> entry : questList.entrySet()) {
			int i1 = entry.getValue().getPosX();
			int j1 = entry.getValue().getPosY();
			int i2 = i1 + entry.getValue().getSize();
			int j2 = j1 + entry.getValue().getSize();
			if(x >= i1 && x < i2 && y >= j1 && y < j2) {
				return entry.getKey();
			}
		}
		return -1;
	}

	public void add(QuestLineEntry entry, Integer questID) {
		if(questID < 0 || entry == null || questList.containsKey(questID) || questList.containsValue(entry)) {
			return;
		}
		questList.put(questID, entry);
	}

	public boolean removeKey(Integer questID) {
		return questList.remove(questID) != null;
	}

	public boolean removeValue(QuestLineEntry entry) {
		return removeKey(getKey(entry));
	}

	public QuestLineEntry getValue(Integer questID) {
		return questList.get(questID);
	}

	public Integer getKey(QuestLineEntry entry) {
		for(Entry<Integer, QuestLineEntry> list : questList.entrySet()) {
			if(list.getValue() == entry) {
				return list.getKey();
			}
		}
		return -1;
	}

	public List<QuestLineEntry> getAllValues() {
		return new ArrayList<>(questList.values());
	}

	public List<Integer> getAllKeys() {
		return new ArrayList<>(questList.keySet());
	}

	public int size() {
		return questList.size();
	}

	public void reset() {
		questList.clear();
	}

	public QuestingPacket getSyncPacket() {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("line", writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		tags.setInteger("lineID", QuestLineDatabase.getKey(this));
		return new QuestingPacket(PacketTypeNative.LINE_SYNC.GetLocation(), tags);
	}

	public void readPacket(NBTTagCompound payload) {
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		readFromJson(JsonHelper.GetObject(base, "line"), EnumSaveType.CONFIG);
	}

	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType) {
		if(saveType != EnumSaveType.CONFIG) {
			return json;
		}
		JsonObject jObj = new JsonObject();
		jObj.addProperty("name", name);
		jObj.addProperty("desc", desc);
		json.add("properties", jObj);
		JsonArray jArr = new JsonArray();
		for(Entry<Integer, QuestLineEntry> entry : questList.entrySet()) {
			JsonObject qle = entry.getValue().writeToJson(new JsonObject(), saveType);
			qle.addProperty("id", entry.getKey());
			jArr.add(qle);
		}
		json.add("quests", jArr);
		return json;
	}

	public void readFromJson(JsonObject json, EnumSaveType saveType) {
		if(saveType != EnumSaveType.CONFIG) {
			return;
		}
		JsonObject jObj = JsonHelper.GetObject(json, "properties");
		name = JsonHelper.GetString(jObj, "name", "New Quest Line");
		desc = JsonHelper.GetString(jObj, "desc", "No Description");
		questList.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "quests")) {
			if(entry == null) {
				continue;
			}
			if(entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isNumber()) {
				questList.put(entry.getAsInt(), new QuestLineEntry(0, 0));
			} else if(entry.isJsonObject()) {
				JsonObject jl = entry.getAsJsonObject();
				int id = JsonHelper.GetInt(jl, "id", -1);
				if(id >= 0) {
					questList.put(id, new QuestLineEntry(jl));
				}
			}
		}
	}
}