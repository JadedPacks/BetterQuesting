package betterquesting.api.questing;

import betterquesting.api.misc.IJsonSaveLoad;
import com.google.gson.JsonObject;

public interface IQuestLineEntry extends IJsonSaveLoad<JsonObject> {
	int getSize();
	int getPosX();
	int getPosY();
	void setPosition(int posX, int posY);
	void setSize(int size);
}