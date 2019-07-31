package betterquesting.api.storage;

import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.IJsonSaveLoad;
import betterquesting.api.questing.party.IParty;
import com.google.gson.JsonObject;

import java.util.UUID;

public interface ILifeDatabase extends IJsonSaveLoad<JsonObject>, IDataSync {
	@Deprecated
	int getDefaultLives();
	@Deprecated
	int getMaxLives();
	int getLives(UUID uuid);
	void setLives(UUID uuid, int value);
	int getLives(IParty party);
	void setLives(IParty party, int value);
}