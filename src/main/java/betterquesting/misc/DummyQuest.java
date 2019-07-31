package betterquesting.misc;

import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.storage.IRegStorageBase;
import betterquesting.api.utils.BigItemStack;
import betterquesting.storage.PropertyContainer;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DummyQuest implements IQuest {
	public static final DummyQuest dummyQuest = new DummyQuest();
	public static final UUID dummyID = UUID.randomUUID();
	private final PropertyContainer propContainer = new PropertyContainer();

	@Override
	public void setParentDatabase(IQuestDatabase questDB) {}

	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType) {
		return json;
	}

	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType) {}

	@Override
	public QuestingPacket getSyncPacket() {
		return null;
	}

	@Override
	public void readPacket(NBTTagCompound payload) {}

	@Override
	public String getUnlocalisedName() {
		return "Dummy";
	}

	@Override
	public String getUnlocalisedDescription() {
		return "Dummy";
	}

	@Override
	public List<String> getTooltip(EntityPlayer player) {
		return new ArrayList<>();
	}

	@Override
	public BigItemStack getItemIcon() {
		return new BigItemStack(Items.nether_star);
	}

	@Override
	public IPropertyContainer getProperties() {
		return propContainer;
	}

	@Override
	public EnumQuestState getState(UUID uuid) {
		switch((int) (Minecraft.getSystemTime() / 1000) % 4) {
			case 0:
				return EnumQuestState.LOCKED;
			case 1:
				return EnumQuestState.UNLOCKED;
			case 2:
				return EnumQuestState.UNCLAIMED;
			case 3:
				return EnumQuestState.COMPLETED;
		}
		return EnumQuestState.COMPLETED;
	}

	@Override
	public void update(EntityPlayer player) {}

	@Override
	public void detect(EntityPlayer player) {}

	@Override
	public boolean isUnlocked(UUID uuid) {
		return false;
	}

	@Override
	public boolean canSubmit(EntityPlayer player) {
		return false;
	}

	@Override
	public boolean isComplete(UUID uuid) {
		return false;
	}

	@Override
	public void setComplete(UUID uuid, long timeStamp) {
	}

	@Override
	public boolean canClaim(EntityPlayer player) {
		return false;
	}

	@Override
	public boolean hasClaimed(UUID uuid) {
		return false;
	}

	@Override
	public void claimReward(EntityPlayer player) {}

	@Override
	public void resetUser(UUID uuid, boolean fullReset) {}

	@Override
	public void resetAll(boolean fullReset) {}

	@Override
	public IRegStorageBase<Integer, ITask> getTasks() {
		return null;
	}

	@Override
	public IRegStorageBase<Integer, IReward> getRewards() {
		return null;
	}

	@Override
	public List<IQuest> getPrerequisites() {
		return null;
	}
}