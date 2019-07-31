package betterquesting.api.questing;

import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.IJsonSaveLoad;
import betterquesting.api.storage.IRegStorageBase;
import com.google.gson.JsonArray;

public interface IQuestLineDatabase extends IRegStorageBase<Integer, IQuestLine>, IJsonSaveLoad<JsonArray>, IDataSync {
	void removeQuest(int lineID);
	int getOrderIndex(int lineID);
	void setOrderIndex(int lineID, int index);
	IQuestLine createNew();
}