package betterquesting.questing;

import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonObject;

public class QuestLineEntry {
	private int posX = 0, posY = 0;

	public QuestLineEntry(JsonObject json) {
		this.readFromJson(json);
	}

	public QuestLineEntry(int x, int y) {
		this.posX = x;
		this.posY = y;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosition(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
	}

	public JsonObject writeToJson(JsonObject json) {
		json.addProperty("x", posX);
		json.addProperty("y", posY);
		return json;
	}

	public void readFromJson(JsonObject json) {
		posX = JsonHelper.GetInt(json, "x", 0);
		posY = JsonHelper.GetInt(json, "y", 0);
	}
}