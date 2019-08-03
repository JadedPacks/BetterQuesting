package betterquesting.questing.rewards;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.questing.rewards.IReward;
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

public class RewardStorage implements IRegStorageBase<Integer, IReward> {
	// TODO: Convert to ArrayList ??
	private final HashMap<Integer, IReward> database = new HashMap<>();

	@Override
	public Integer nextKey() {
		int id = 0;
		while(database.containsKey(id)) {
			id++;
		}
		return id;
	}

	@Override
	public void add(IReward obj, Integer id) {
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
	public boolean removeValue(IReward reward) {
		return removeKey(getKey(reward));
	}

	@Override
	public IReward getValue(Integer id) {
		return database.get(id);
	}

	@Override
	public Integer getKey(IReward obj) {
		int id = -1;
		for(Entry<Integer, IReward> entry : database.entrySet()) {
			if(entry.getValue() == obj) {
				return entry.getKey();
			}
		}
		return id;
	}

	@Override
	public List<IReward> getAllValues() {
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
		if(saveType != EnumSaveType.CONFIG) {
			return json;
		}
		for(Entry<Integer, IReward> rew : database.entrySet()) {
			ResourceLocation rewardID = rew.getValue().getFactoryID();
			JsonObject rJson = rew.getValue().writeToJson(new JsonObject(), EnumSaveType.CONFIG);
			rJson.addProperty("rewardID", rewardID.toString());
			rJson.addProperty("index", rew.getKey());
			json.add(rJson);
		}
		return json;
	}

	public void readFromJson(JsonArray json, EnumSaveType saveType) {
		if(saveType != EnumSaveType.CONFIG) {
			return;
		}
		database.clear();
		for(JsonElement entry : json) {
			if(entry == null || !entry.isJsonObject()) {
				continue;
			}
			JsonObject jsonReward = entry.getAsJsonObject();
			IReward reward = RewardRegistry.INSTANCE.createReward(new ResourceLocation(JsonHelper.GetString(jsonReward, "rewardID", "")));
			if(reward != null) {
				reward.readFromJson(jsonReward, EnumSaveType.CONFIG);
				add(reward, nextKey());
			}
		}
	}
}