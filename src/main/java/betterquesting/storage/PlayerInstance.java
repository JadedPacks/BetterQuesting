package betterquesting.storage;

import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonObject;

import java.util.UUID;

public class PlayerInstance {
	public int lives = 3;

	public PlayerInstance(UUID uuid) {
		// TODO: Read from json
	}

	public void readFromJson(JsonObject json) {
		lives = JsonHelper.GetInt(json, "livesDef", 3);
	}

	public JsonObject writeToJson(JsonObject json) {
		json.addProperty("lives", lives);
		return json;
	}
}