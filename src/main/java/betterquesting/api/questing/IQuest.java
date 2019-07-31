package betterquesting.api.questing;

import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.IJsonSaveLoad;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.storage.IRegStorageBase;
import betterquesting.api.utils.BigItemStack;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;
import java.util.UUID;

public interface IQuest extends IJsonSaveLoad<JsonObject>, IDataSync {
	String getUnlocalisedName();
	String getUnlocalisedDescription();
	void setParentDatabase(IQuestDatabase questDB);
	@SideOnly(Side.CLIENT)
	List<String> getTooltip(EntityPlayer player);
	BigItemStack getItemIcon();
	IPropertyContainer getProperties();
	EnumQuestState getState(UUID uuid);
	void update(EntityPlayer player);
	void detect(EntityPlayer player);
	boolean isUnlocked(UUID uuid);
	boolean canSubmit(EntityPlayer player);
	boolean isComplete(UUID uuid);
	void setComplete(UUID uuid, long timeStamp);
	boolean canClaim(EntityPlayer player);
	boolean hasClaimed(UUID uuid);
	void claimReward(EntityPlayer player);
	void resetUser(UUID uuid, boolean fullReset);
	void resetAll(boolean fullReset);
	IRegStorageBase<Integer, ITask> getTasks();
	IRegStorageBase<Integer, IReward> getRewards();
	List<IQuest> getPrerequisites();
}