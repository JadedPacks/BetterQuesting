package betterquesting.questing.tasks;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.storage.IRegStorageBase;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class TaskStorage implements IRegStorageBase<Integer, ITask> {
	private final HashMap<Integer, ITask> database = new HashMap<>();

	@Override
	public Integer nextKey() {
		int id = 0;
		while(database.containsKey(id)) {
			id++;
		}
		return id;
	}

	@Override
	public void add(ITask obj, Integer id) {
		if(obj == null || database.containsKey(id)) {
			return;
		}
		database.put(id, obj);
	}

	@Override
	public boolean removeKey(Integer id) {
		return database.remove(id) != null;
	}

	@Override
	public boolean removeValue(ITask task) {
		return removeKey(getKey(task));
	}

	@Override
	public ITask getValue(Integer id) {
		return database.get(id);
	}

	@Override
	public Integer getKey(ITask obj) {
		int id = -1;
		for(Entry<Integer, ITask> entry : database.entrySet()) {
			if(entry.getValue() == obj) {
				return entry.getKey();
			}
		}
		return id;
	}

	@Override
	public List<ITask> getAllValues() {
		return new ArrayList<>(database.values());
	}

	@Override
	public List<Integer> getAllKeys() {
		return new ArrayList<>(database.keySet());
	}

	@Override
	public int size() {
		return database.size();
	}

	@Override
	public void reset() {
		database.clear();
	}

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
		for(Entry<Integer, ITask> entry : database.entrySet()) {
			ResourceLocation taskID = entry.getValue().getFactoryID();
			JsonObject qJson = entry.getValue().writeToJson(new JsonObject(), EnumSaveType.CONFIG);
			qJson.addProperty("taskID", taskID.toString());
			json.add(qJson);
		}
	}

	private void readFromJson_Config(JsonArray json) {
		database.clear();
		for(JsonElement entry : json) {
			if(entry == null || !entry.isJsonObject()) {
				continue;
			}
			JsonObject jsonTask = entry.getAsJsonObject();
			ITask task = TaskRegistry.INSTANCE.createTask(new ResourceLocation(JsonHelper.GetString(jsonTask, "taskID", "")));
			if(task != null) {
				task.readFromJson(jsonTask, EnumSaveType.CONFIG);
				add(task, nextKey());
			}
		}
	}

	private void writeToJson_Progress(JsonArray json) {
		for(Entry<Integer, ITask> entry : database.entrySet()) {
			ResourceLocation taskID = entry.getValue().getFactoryID();
			JsonObject qJson = entry.getValue().writeToJson(new JsonObject(), EnumSaveType.PROGRESS);
			qJson.addProperty("taskID", taskID.toString());
			json.add(qJson);
		}
	}

	private void readFromJson_Progress(JsonArray json) {
		for(int i = 0; i < json.size(); i++) {
			JsonElement entry = json.get(i);
			if(entry == null || !entry.isJsonObject()) {
				continue;
			}
			JsonObject jsonTask = entry.getAsJsonObject();
			ITask task = getValue(i);
			if(task != null) {
				if(task.getFactoryID().equals(new ResourceLocation(JsonHelper.GetString(jsonTask, "taskID", "")))) {
					task.readFromJson(jsonTask, EnumSaveType.PROGRESS);
				}
			}
		}
	}
}