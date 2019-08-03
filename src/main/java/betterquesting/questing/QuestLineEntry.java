package betterquesting.questing;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonObject;

public class QuestLineEntry {
	private int size = 0, posX = 0, posY = 0;

	public QuestLineEntry(JsonObject json) {
		this.readFromJson(json, EnumSaveType.CONFIG);
	}

	public QuestLineEntry(int x, int y) {
		this(x, y, 24);
	}

	public QuestLineEntry(int x, int y, int size) {
		this.size = size;
		this.posX = x;
		this.posY = y;
	}

	public int getSize() {
		return size;
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

	public void setSize(int size) {
		this.size = size;
	}

	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType) {
		if(saveType != EnumSaveType.CONFIG) {
			return json;
		}
		json.addProperty("size", size);
		json.addProperty("x", posX);
		json.addProperty("y", posY);
		return json;
	}

	public void readFromJson(JsonObject json, EnumSaveType saveType) {
		if(saveType != EnumSaveType.CONFIG) {
			return;
		}
		size = JsonHelper.GetInt(json, "size", 24);
		posX = JsonHelper.GetInt(json, "x", 0);
		posY = JsonHelper.GetInt(json, "y", 0);
	}
}