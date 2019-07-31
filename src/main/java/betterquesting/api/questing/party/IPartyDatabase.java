package betterquesting.api.questing.party;

import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.IJsonSaveLoad;
import betterquesting.api.storage.IRegStorageBase;
import com.google.gson.JsonArray;

import java.util.List;
import java.util.UUID;

public interface IPartyDatabase extends IRegStorageBase<Integer, IParty>, IJsonSaveLoad<JsonArray>, IDataSync {
	IParty getUserParty(UUID uuid);

	List<Integer> getPartyInvites(UUID uuid);
}