package betterquesting.api.questing.party;

import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.IJsonSaveLoad;
import betterquesting.api.properties.IPropertyContainer;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.UUID;

public interface IParty extends IJsonSaveLoad<JsonObject>, IDataSync {
	String getName();
	IPropertyContainer getProperties();
	void inviteUser(UUID uuid);
	void kickUser(UUID uuid);
	void setStatus(UUID uuid, EnumPartyStatus priv);
	EnumPartyStatus getStatus(UUID uuid);
	List<UUID> getMembers();
	@Deprecated
	void setShareLives(boolean state);
	@Deprecated
	void setShareReward(boolean state);
	@Deprecated
	boolean getShareLives();
	@Deprecated
	boolean getShareReward();
	@Deprecated
	void setName(String name);
}