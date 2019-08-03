package betterquesting.questing;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.misc.QuestLineSortByKey;
import betterquesting.misc.QuestLineSortByValue;
import betterquesting.network.PacketTypeNative;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public final class QuestLineDatabase {
	private static final ConcurrentHashMap<Integer, QuestLine> questLines = new ConcurrentHashMap<>();
	private static final List<Integer> lineOrder = new ArrayList<>();

	public static int getOrderIndex(int lineID) {
		if(!questLines.containsKey(lineID)) {
			return -1;
		} else if(!lineOrder.contains(lineID)) {
			lineOrder.add(lineID);
		}
		return lineOrder.indexOf(lineID);
	}

	public static void setOrderIndex(int lineID, int index) {
		lineOrder.remove((Integer) lineID);
		lineOrder.add(index, lineID);
	}

	public QuestLine createNew() {
		return new QuestLine();
	}

	public static void removeQuest(int questID) {
		for(QuestLine ql : getAllValues()) {
			ql.removeKey(questID);
		}
	}

	public static Integer nextKey() {
		int id = 0;
		while(questLines.containsKey(id)) {
			id += 1;
		}
		return id;
	}

	public static void add(QuestLine questLine, Integer id) {
		if(id < 0 || questLine == null || questLines.containsValue(questLine) || questLines.containsKey(id)) {
			return;
		}
		questLines.put(id, questLine);
	}

	public static boolean removeKey(Integer lineId) {
		return questLines.remove(lineId) != null;
	}

	public boolean removeValue(QuestLine quest) {
		return removeKey(getKey(quest));
	}

	public static Integer getKey(QuestLine questLine) {
		for(Entry<Integer, QuestLine> entry : questLines.entrySet()) {
			if(entry.getValue() == questLine) {
				return entry.getKey();
			}
		}
		return -1;
	}

	public static QuestLine getValue(Integer lineId) {
		return questLines.get(lineId);
	}

	public static List<QuestLine> getAllValues() {
		List<QuestLine> list = new ArrayList<>(questLines.values());
		list.sort(new QuestLineSortByValue());
		return list;
	}

	public static List<Integer> getAllKeys() {
		List<Integer> list = new ArrayList<>(((Map<Integer, QuestLine>) questLines).keySet());
		list.sort(new QuestLineSortByKey());
		return list;
	}

	public static int size() {
		return questLines.size();
	}

	public static void reset() {
		questLines.clear();
	}

	public static QuestingPacket getSyncPacket() {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("questLines", writeToJson(new JsonArray(), EnumSaveType.CONFIG));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		return new QuestingPacket(PacketTypeNative.LINE_DATABASE.GetLocation(), tags);
	}

	public static void readPacket(NBTTagCompound payload) {
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		readFromJson(JsonHelper.GetArray(base, "questLines"), EnumSaveType.CONFIG);
	}

	public static JsonArray writeToJson(JsonArray json, EnumSaveType saveType) {
		if(saveType != EnumSaveType.CONFIG) {
			return json;
		}
		for(Entry<Integer, QuestLine> entry : questLines.entrySet()) {
			if(entry.getValue() == null) {
				continue;
			}
			int id = entry.getKey();
			JsonObject jObj = entry.getValue().writeToJson(new JsonObject(), saveType);
			jObj.addProperty("lineID", id);
			jObj.addProperty("order", getOrderIndex(id));
			json.add(jObj);
		}
		return json;
	}

	public static void readFromJson(JsonArray json, EnumSaveType saveType) {
		if(saveType != EnumSaveType.CONFIG) {
			return;
		}
		questLines.clear();
		ArrayList<QuestLine> unassigned = new ArrayList<>();
		HashMap<Integer, Integer> orderMap = new HashMap<>();
		for(JsonElement entry : json) {
			if(entry == null || !entry.isJsonObject()) {
				continue;
			}
			JsonObject jql = entry.getAsJsonObject();
			int id = JsonHelper.GetInt(jql, "lineID", -1);
			int order = JsonHelper.GetInt(jql, "order", -1);
			QuestLine line = new QuestLine();
			line.readFromJson(entry.getAsJsonObject(), saveType);
			if(id >= 0) {
				questLines.put(id, line);
			} else {
				unassigned.add(line);
			}
			if(order >= 0) {
				orderMap.put(order, id);
			}
		}
		for(QuestLine q : unassigned) {
			questLines.put(nextKey(), q);
		}
		List<Integer> orderKeys = new ArrayList<>(orderMap.keySet());
		Collections.sort(orderKeys);
		lineOrder.clear();
		for(int o : orderKeys) {
			lineOrder.add(orderMap.get(o));
		}
	}
}