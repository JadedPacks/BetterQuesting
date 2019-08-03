package betterquesting.questing.rewards;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Random;

public class LootGroup implements Comparable<LootGroup> {
	public String name = "Loot Group";
	public int weight = 1;
	public ArrayList<LootEntry> lootEntry = new ArrayList<>();

	public ArrayList<BigItemStack> getRandomReward(Random rand) {
		int total = getTotalWeight();
		float r = rand.nextFloat() * total;
		int cnt = 0;
		for(LootEntry entry : lootEntry) {
			cnt += entry.weight;
			if(cnt >= r) {
				return entry.items;
			}
		}
		return new ArrayList<>();
	}

	public int getTotalWeight() {
		int i = 0;
		for(LootEntry entry : lootEntry) {
			i += entry.weight;
		}
		return i;
	}

	public void readFromJson(JsonObject json) {
		name = JsonHelper.GetString(json, "name", "Loot Group");
		weight = JsonHelper.GetInt(json, "weight", 1);
		weight = Math.max(1, weight);
		lootEntry = new ArrayList<>();
		JsonArray jRew = JsonHelper.GetArray(json, "rewards");
		for(JsonElement entry : jRew) {
			if(entry == null || !entry.isJsonObject()) {
				continue;
			}
			LootEntry loot = new LootEntry();
			loot.readFromJson(entry.getAsJsonObject());
			lootEntry.add(loot);
		}
	}

	public void writeToJson(JsonObject json) {
		json.addProperty("name", name);
		json.addProperty("weight", weight);
		JsonArray jRew = new JsonArray();
		for(LootEntry entry : lootEntry) {
			if(entry == null) {
				continue;
			}
			JsonObject jLoot = new JsonObject();
			entry.writeToJson(jLoot);
			jRew.add(jLoot);
		}
		json.add("rewards", jRew);
	}

	@Override
	public int compareTo(LootGroup group) {
		return (int) Math.signum(group.weight - weight);
	}

	public static class LootEntry implements Comparable<LootEntry> {
		public int weight = 1;
		public ArrayList<BigItemStack> items = new ArrayList<>();
		public void readFromJson(JsonObject json) {
			weight = JsonHelper.GetInt(json, "weight", 0);
			weight = Math.max(1, weight);
			items = new ArrayList<>();
			JsonArray jItm = JsonHelper.GetArray(json, "items");
			for(JsonElement entry : jItm) {
				if(entry == null || !entry.isJsonObject()) {
					continue;
				}
				BigItemStack stack = JsonHelper.JsonToItemStack(entry.getAsJsonObject());
				if(stack != null) {
					items.add(stack);
				}
			}
		}

		public void writeToJson(JsonObject json) {
			json.addProperty("weight", weight);
			JsonArray jItm = new JsonArray();
			for(BigItemStack stack : items) {
				jItm.add(JsonHelper.ItemStackToJson(stack, new JsonObject()));
			}
			json.add("items", jItm);
		}

		@Override
		public int compareTo(LootEntry entry) {
			return (int) Math.signum(entry.weight - weight);
		}
	}
}