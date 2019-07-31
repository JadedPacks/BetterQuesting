package betterquesting.api.questing;

import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.IJsonSaveLoad;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.storage.IRegStorageBase;
import com.google.gson.JsonObject;

public interface IQuestLine extends IDataSync, IJsonSaveLoad<JsonObject>, IRegStorageBase<Integer, IQuestLineEntry> {
	String getUnlocalisedName();
	String getUnlocalisedDescription();
	void setParentDatabase(IQuestLineDatabase questDB);
	IPropertyContainer getProperties();
	int getQuestAt(int x, int y);
}