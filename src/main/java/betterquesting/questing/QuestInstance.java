package betterquesting.questing;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.IProgression;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.storage.IRegStorageBase;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.core.BetterQuesting;
import betterquesting.misc.UserEntry;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyManager;
import betterquesting.questing.rewards.RewardStorage;
import betterquesting.questing.tasks.TaskStorage;
import betterquesting.storage.PropertyContainer;
import betterquesting.storage.QuestSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestInstance implements IQuest {
	private final TaskStorage tasks = new TaskStorage();
	private final RewardStorage rewards = new RewardStorage();
	private final ArrayList<UserEntry> completeUsers = new ArrayList<>();
	private final ArrayList<IQuest> preRequisites = new ArrayList<>();
	private final PropertyContainer qInfo = new PropertyContainer();
	private IQuestDatabase parentDB;

	public QuestInstance() {
		parentDB = QuestingAPI.getAPI(ApiReference.QUEST_DB);
		this.setupProps();
	}

	private void setupProps() {
		setupValue(NativeProps.NAME, "New Quest");
		setupValue(NativeProps.DESC, "No Description");
		setupValue(NativeProps.ICON, new BigItemStack(Items.nether_star));
		setupValue(NativeProps.SOUND_COMPLETE);
		setupValue(NativeProps.SOUND_UPDATE);
		setupValue(NativeProps.LOGIC_QUEST, EnumLogic.AND);
		setupValue(NativeProps.LOGIC_TASK, EnumLogic.AND);
		setupValue(NativeProps.REPEAT_TIME, -1);
		setupValue(NativeProps.LOCKED_PROGRESS, false);
		setupValue(NativeProps.AUTO_CLAIM, false);
		setupValue(NativeProps.SILENT, false);
		setupValue(NativeProps.MAIN, false);
		// TODO: Make a global setting
		setupValue(NativeProps.PARTY_LOOT, false);
		setupValue(NativeProps.GLOBAL_SHARE, false);
		setupValue(NativeProps.SIMULTANEOUS, false);
	}

	private <T> void setupValue(IPropertyType<T> prop) {
		this.setupValue(prop, prop.getDefault());
	}

	private <T> void setupValue(IPropertyType<T> prop, T def) {
		qInfo.setProperty(prop, qInfo.getProperty(prop, def));
	}

	@Override
	public void setParentDatabase(IQuestDatabase questDB) {
		this.parentDB = questDB;
	}

	@Override
	public String getUnlocalisedName() {
		String def = "New Quest";
		if(!qInfo.hasProperty(NativeProps.NAME)) {
			qInfo.setProperty(NativeProps.NAME, def);
			return def;
		}
		return qInfo.getProperty(NativeProps.NAME, def);
	}

	@Override
	public String getUnlocalisedDescription() {
		String def = "No Description";
		if(!qInfo.hasProperty(NativeProps.DESC)) {
			qInfo.setProperty(NativeProps.DESC, def);
			return def;
		}
		return qInfo.getProperty(NativeProps.DESC, def);
	}

	@Override
	public BigItemStack getItemIcon() {
		BigItemStack def = new BigItemStack(Items.nether_star);
		if(!qInfo.hasProperty(NativeProps.ICON)) {
			qInfo.setProperty(NativeProps.ICON, def);
			return def;
		}
		return qInfo.getProperty(NativeProps.ICON, def);
	}

	@Override
	public IPropertyContainer getProperties() {
		return qInfo;
	}

	@Override
	public void update(EntityPlayer player) {
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		if(isComplete(playerID)) {
			UserEntry entry = GetUserEntry(playerID);
			if(!hasClaimed(playerID)) {
				if(canClaim(player)) {
					if(qInfo.getProperty(NativeProps.AUTO_CLAIM) && player.ticksExisted % 20 == 0) {
						claimReward(player);
					}
					return;
				} else if(qInfo.getProperty(NativeProps.REPEAT_TIME).intValue() < 0 || rewards.size() <= 0) {
					return;
				}
			} else if(rewards.size() > 0 && qInfo.getProperty(NativeProps.REPEAT_TIME).intValue() >= 0 && player.worldObj.getTotalWorldTime() - entry.getTimestamp() >= qInfo.getProperty(NativeProps.REPEAT_TIME).intValue()) {
				if(qInfo.getProperty(NativeProps.GLOBAL)) {
					resetAll(false);
				} else {
					resetUser(playerID, false);
				}
				if(!QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE) && !qInfo.getProperty(NativeProps.SILENT)) {
					postPresetNotice(player, 1);
				}
				PacketSender.INSTANCE.sendToAll(getSyncPacket());
				return;
			} else {
				return;
			}
		}
		if(isUnlocked(playerID) || qInfo.getProperty(NativeProps.LOCKED_PROGRESS)) {
			int done = 0;
			for(ITask tsk : tasks.getAllValues()) {
				if(tsk.isComplete(playerID)) {
					IParty party = PartyManager.INSTANCE.getUserParty(playerID);
					if(party != null) {
						for(UUID mem : party.getMembers()) {
							tsk.setComplete(mem);
						}
					}
					done += 1;
				}
			}
			if(!isUnlocked(playerID)) {
				return;
			}
			if((tasks.size() > 0 || !QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)) && qInfo.getProperty(NativeProps.LOGIC_TASK).getResult(done, tasks.size())) {
				setComplete(playerID, player.worldObj.getTotalWorldTime());
				PacketSender.INSTANCE.sendToAll(getSyncPacket());
				if(!QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE) && !qInfo.getProperty(NativeProps.SILENT)) {
					postPresetNotice(player, 2);
				}
			} else if(done > 0 && qInfo.getProperty(NativeProps.SIMULTANEOUS)) {
				resetUser(playerID, false);
				PacketSender.INSTANCE.sendToAll(getSyncPacket());
			}
		}
	}

	@Override
	public void detect(EntityPlayer player) {
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		if(isComplete(playerID) && (qInfo.getProperty(NativeProps.REPEAT_TIME).intValue() < 0 || rewards.size() <= 0)) {
			return;
		} else if(!canSubmit(player)) {
			return;
		}
		if(isUnlocked(playerID) || QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)) {
			int done = 0;
			boolean update = false;
			for(ITask tsk : tasks.getAllValues()) {
				if(!tsk.isComplete(playerID)) {
					tsk.detect(player, this);
					if(tsk.isComplete(playerID)) {
						IParty party = PartyManager.INSTANCE.getUserParty(playerID);
						if(party != null) {
							for(UUID mem : party.getMembers()) {
								tsk.setComplete(mem);
							}
						}
						done += 1;
						update = true;
					}
				} else {
					done += 1;
				}
			}
			if((tasks.size() > 0 || !QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)) && qInfo.getProperty(NativeProps.LOGIC_TASK).getResult(done, tasks.size())) {
				setComplete(playerID, player.worldObj.getTotalWorldTime());
				if(!QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE) && !qInfo.getProperty(NativeProps.SILENT)) {
					postPresetNotice(player, 2);
				}
			} else if(update && qInfo.getProperty(NativeProps.SIMULTANEOUS)) {
				resetUser(playerID, false);
				PacketSender.INSTANCE.sendToAll(getSyncPacket());
			} else if(update) {
				if(!QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE) && !qInfo.getProperty(NativeProps.SILENT)) {
					postPresetNotice(player, 1);
				}
			}
			PacketSender.INSTANCE.sendToAll(getSyncPacket());
		}
	}

	public void postPresetNotice(EntityPlayer player, int preset) {
		switch(preset) {
			case 0:
				postNotice(player, "betterquesting.notice.unlock", getUnlocalisedName(), qInfo.getProperty(NativeProps.SOUND_UNLOCK), getItemIcon());
				break;
			case 1:
				postNotice(player, "betterquesting.notice.update", getUnlocalisedName(), qInfo.getProperty(NativeProps.SOUND_UPDATE), getItemIcon());
				break;
			case 2:
				postNotice(player, "betterquesting.notice.complete", getUnlocalisedName(), qInfo.getProperty(NativeProps.SOUND_COMPLETE), getItemIcon());
				break;
		}
	}

	public void postNotice(EntityPlayer player, String mainTxt, String subTxt, String sound, BigItemStack icon) {
		NBTTagCompound tags = new NBTTagCompound();
		tags.setString("Main", mainTxt);
		tags.setString("Sub", subTxt);
		tags.setString("Sound", sound);
		tags.setTag("Icon", icon.writeToNBT(new NBTTagCompound()));
		QuestingPacket payload = new QuestingPacket(PacketTypeNative.NOTIFICATION.GetLocation(), tags);
		if(qInfo.getProperty(NativeProps.GLOBAL)) {
			PacketSender.INSTANCE.sendToAll(payload);
		} else if(player instanceof EntityPlayerMP) {
			List<EntityPlayerMP> tarList = getPartyPlayers((EntityPlayerMP) player);

			for(EntityPlayerMP p : tarList) {
				PacketSender.INSTANCE.sendToPlayer(payload, p);
			}
		}
	}

	private List<EntityPlayerMP> getPartyPlayers(EntityPlayerMP player) {
		List<EntityPlayerMP> list = new ArrayList<>();
		IParty party = PartyManager.INSTANCE.getUserParty(QuestingAPI.getQuestingUUID(player));
		if(party == null) {
			list.add(player);
			return list;
		} else {
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			for(UUID mem : party.getMembers()) {
				for(EntityPlayerMP p : (List<EntityPlayerMP>) server.getConfigurationManager().playerEntityList) {
					if(p != null && QuestingAPI.getQuestingUUID(p).equals(mem)) {
						list.add(p);
					}
				}
			}
			return list;
		}
	}

	@Override
	public boolean hasClaimed(UUID uuid) {
		if(rewards.size() <= 0) {
			return true;
		}
		if(qInfo.getProperty(NativeProps.GLOBAL)) {
			if(GetParticipation(uuid) < qInfo.getProperty(NativeProps.PARTICIPATION).floatValue()) {
				return true;
			} else if(!qInfo.getProperty(NativeProps.GLOBAL_SHARE)) {
				for(UserEntry entry : completeUsers) {
					if(entry.hasClaimed()) {
						return true;
					}
				}
				return false;
			}
		}
		UserEntry entry = GetUserEntry(uuid);
		if(entry == null) {
			return false;
		}
		return entry.hasClaimed();
	}

	@Override
	public boolean canClaim(EntityPlayer player) {
		UserEntry entry = GetUserEntry(QuestingAPI.getQuestingUUID(player));
		if(entry == null || hasClaimed(QuestingAPI.getQuestingUUID(player))) {
			return false;
		} else if(canSubmit(player)) {
			return false;
		} else {
			for(IReward rew : rewards.getAllValues()) {
				if(!rew.canClaim(player, this)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void claimReward(EntityPlayer player) {
		for(IReward rew : rewards.getAllValues()) {
			rew.claimReward(player, this);
		}
		UUID pID = QuestingAPI.getQuestingUUID(player);
		IParty party = PartyManager.INSTANCE.getUserParty(pID);
		if(party != null && this.qInfo.getProperty(NativeProps.PARTY_LOOT)) {
			for(UUID mem : party.getMembers()) {
				EnumPartyStatus pStat = party.getStatus(mem);
				if(pStat == null || pStat == EnumPartyStatus.INVITE) {
					continue;
				}
				UserEntry entry = GetUserEntry(mem);
				if(entry == null) {
					entry = new UserEntry(mem);
					this.completeUsers.add(entry);
				}
				entry.setClaimed(true, player.worldObj.getTotalWorldTime());
			}
		} else {
			UserEntry entry = GetUserEntry(pID);
			if(entry == null) {
				entry = new UserEntry(pID);
				this.completeUsers.add(entry);
			}
			entry.setClaimed(true, player.worldObj.getTotalWorldTime());
		}
		PacketSender.INSTANCE.sendToAll(getSyncPacket());
	}

	@Override
	public boolean canSubmit(EntityPlayer player) {
		if(player == null) {
			return false;
		}
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		UserEntry entry = this.GetUserEntry(playerID);
		if(entry == null) {
			return true;
		} else if(!entry.hasClaimed() && getProperties().getProperty(NativeProps.REPEAT_TIME).intValue() >= 0) {
			int done = 0;
			for(ITask tsk : tasks.getAllValues()) {
				if(tsk.isComplete(playerID)) {
					done += 1;
				}
			}
			return !qInfo.getProperty(NativeProps.LOGIC_TASK).getResult(done, tasks.size());
		} else {
			return false;
		}
	}

	private float GetParticipation(UUID uuid) {
		if(tasks.size() <= 0) {
			return 0F;
		}
		float total = 0F;
		for(ITask t : tasks.getAllValues()) {
			if(t instanceof IProgression) {
				total += ((IProgression) t).getParticipation(uuid);
			}
		}
		return total / tasks.size();
	}

	@Override
	public List<String> getTooltip(EntityPlayer player) {
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
			return this.getAdvancedTooltip();
		} else {
			return this.getStandardTooltip(player);
		}
	}

	@SideOnly(Side.CLIENT)
	private List<String> getStandardTooltip(EntityPlayer player) {
		ArrayList<String> list = new ArrayList<>();
		list.add(StatCollector.translateToLocalFormatted(getUnlocalisedName()));
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		if(isComplete(playerID)) {
			list.add(EnumChatFormatting.GREEN + StatCollector.translateToLocalFormatted("betterquesting.tooltip.complete"));
			if(!hasClaimed(playerID)) {
				list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.rewards_pending"));
			} else if(qInfo.getProperty(NativeProps.REPEAT_TIME).intValue() > 0) {
				long time = getRepeatSeconds(player);
				DecimalFormat df = new DecimalFormat("00");
				String timeTxt = "";
				if(time >= 3600) {
					timeTxt += (time / 3600) + "h " + df.format((time % 3600) / 60) + "m ";
				} else if(time >= 60) {
					timeTxt += (time / 60) + "m ";
				}
				timeTxt += df.format(time % 60) + "s";
				list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.repeat", timeTxt));
			}
		} else if(!isUnlocked(playerID)) {
			list.add(EnumChatFormatting.RED + "" + EnumChatFormatting.UNDERLINE + StatCollector.translateToLocalFormatted("betterquesting.tooltip.requires") + " (" + qInfo.getProperty(NativeProps.LOGIC_QUEST).toString().toUpperCase() + ")");
			for(IQuest req : preRequisites) {
				if(!req.isComplete(playerID)) {
					list.add(EnumChatFormatting.RED + "- " + StatCollector.translateToLocalFormatted(req.getUnlocalisedName()));
				}
			}
		} else {
			int n = 0;
			for(ITask task : tasks.getAllValues()) {
				if(task.isComplete(playerID)) {
					n++;
				}
			}
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.tasks_complete", n, tasks.size()));
		}
		list.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.shift_advanced"));
		return list;
	}

	@SideOnly(Side.CLIENT)
	private List<String> getAdvancedTooltip() {
		ArrayList<String> list = new ArrayList<>();
		list.add(StatCollector.translateToLocalFormatted(getUnlocalisedName()) + " #" + parentDB.getKey(this));
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.main_quest", qInfo.getProperty(NativeProps.MAIN)));
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.global_quest", qInfo.getProperty(NativeProps.GLOBAL)));
		if(qInfo.getProperty(NativeProps.GLOBAL)) {
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.global_share", qInfo.getProperty(NativeProps.GLOBAL_SHARE)));
		}
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.task_logic", qInfo.getProperty(NativeProps.LOGIC_QUEST).toString().toUpperCase()));
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.simultaneous", qInfo.getProperty(NativeProps.SIMULTANEOUS)));
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.auto_claim", qInfo.getProperty(NativeProps.AUTO_CLAIM)));
		if(qInfo.getProperty(NativeProps.REPEAT_TIME).intValue() >= 0) {
			long time = qInfo.getProperty(NativeProps.REPEAT_TIME).intValue() / 20;
			DecimalFormat df = new DecimalFormat("00");
			String timeTxt = "";
			if(time >= 3600) {
				timeTxt += (time / 3600) + "h " + df.format((time % 3600) / 60) + "m ";
			} else if(time >= 60) {
				timeTxt += (time / 60) + "m ";
			}
			timeTxt += df.format(time % 60) + "s";
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.repeat", timeTxt));
		} else {
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.repeat", false));
		}
		return list;
	}

	@SideOnly(Side.CLIENT)
	public long getRepeatSeconds(EntityPlayer player) {
		if(qInfo.getProperty(NativeProps.REPEAT_TIME).intValue() < 0) {
			return -1;
		}
		UserEntry ue = GetUserEntry(QuestingAPI.getQuestingUUID(player));
		if(ue == null) {
			return 0;
		} else {
			return (qInfo.getProperty(NativeProps.REPEAT_TIME).intValue() - (player.worldObj.getTotalWorldTime() - ue.getTimestamp())) / 20L;
		}
	}

	@Override
	public QuestingPacket getSyncPacket() {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("config", writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		base.add("progress", writeToJson(new JsonObject(), EnumSaveType.PROGRESS));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		tags.setInteger("questID", parentDB.getKey(this));
		return new QuestingPacket(PacketTypeNative.QUEST_SYNC.GetLocation(), tags);
	}

	@Override
	public void readPacket(NBTTagCompound payload) {
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		readFromJson(JsonHelper.GetObject(base, "config"), EnumSaveType.CONFIG);
		readFromJson(JsonHelper.GetObject(base, "progress"), EnumSaveType.PROGRESS);
	}

	public boolean isUnlocked(UUID uuid) {
		int A = 0;
		int B = preRequisites.size();
		if(B <= 0) {
			return true;
		}
		for(IQuest quest : preRequisites) {
			if(quest != null && quest.isComplete(uuid)) {
				A++;
			}
		}
		return qInfo.getProperty(NativeProps.LOGIC_QUEST).getResult(A, B);
	}

	@Override
	public void setComplete(UUID uuid, long timestamp) {
		IParty party = PartyManager.INSTANCE.getUserParty(uuid);
		if(party == null) {
			UserEntry entry = this.GetUserEntry(uuid);
			if(entry != null) {
				entry.setClaimed(false, timestamp);
			} else {
				completeUsers.add(new UserEntry(uuid, timestamp));
			}
		} else {
			for(UUID mem : party.getMembers()) {
				UserEntry entry = this.GetUserEntry(mem);
				if(entry != null) {
					entry.setClaimed(false, timestamp);
				} else {
					completeUsers.add(new UserEntry(mem, timestamp));
				}
			}
		}
	}

	@Override
	public boolean isComplete(UUID uuid) {
		if(qInfo.getProperty(NativeProps.GLOBAL)) {
			return completeUsers.size() > 0;
		} else {
			return GetUserEntry(uuid) != null;
		}
	}

	private void RemoveUserEntry(UUID... uuid) {
		boolean flag = false;
		for(int i = completeUsers.size() - 1; i >= 0; i--) {
			UserEntry entry = completeUsers.get(i);
			for(UUID id : uuid) {
				if(entry.getUUID().equals(id)) {
					completeUsers.remove(i);
					flag = true;
					break;
				}
			}
		}
		if(flag) {
			PacketSender.INSTANCE.sendToAll(getSyncPacket());
		}
	}

	@Override
	public EnumQuestState getState(UUID uuid) {
		if(this.isComplete(uuid)) {
			if(this.hasClaimed(uuid)) {
				return EnumQuestState.COMPLETED;
			} else {
				return EnumQuestState.UNCLAIMED;
			}
		} else if(this.isUnlocked(uuid)) {
			return EnumQuestState.UNLOCKED;
		}
		return EnumQuestState.LOCKED;
	}

	private UserEntry GetUserEntry(UUID uuid) {
		for(UserEntry entry : completeUsers) {
			if(entry.getUUID().equals(uuid)) {
				return entry;
			}
		}
		return null;
	}

	@Override
	public void resetUser(UUID uuid, boolean fullReset) {
		if(fullReset) {
			this.RemoveUserEntry(uuid);
		} else {
			UserEntry entry = GetUserEntry(uuid);
			if(entry != null) {
				entry.setClaimed(false, 0);
			}
		}
		for(ITask t : tasks.getAllValues()) {
			t.resetUser(uuid);
		}
	}

	@Override
	public void resetAll(boolean fullReset) {
		if(fullReset) {
			completeUsers.clear();
		} else {
			for(UserEntry entry : completeUsers) {
				entry.setClaimed(false, 0);
			}
		}
		for(ITask t : tasks.getAllValues()) {
			t.resetAll();
		}
	}

	@Override
	public IRegStorageBase<Integer, ITask> getTasks() {
		return tasks;
	}

	@Override
	public IRegStorageBase<Integer, IReward> getRewards() {
		return rewards;
	}

	@Override
	public List<IQuest> getPrerequisites() {
		return preRequisites;
	}

	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType) {
		switch(saveType) {
			case CONFIG:
				writeToJson_Config(json);
				break;
			case PROGRESS:
				writeToJson_Progress(json);
				break;
			default:
				break;
		}
		return json;
	}

	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType) {
		switch(saveType) {
			case CONFIG:
				readFromJson_Config(json);
				break;
			case PROGRESS:
				readFromJson_Progress(json);
				break;
			default:
				break;
		}
	}

	private void writeToJson_Config(JsonObject jObj) {
		jObj.add("properties", qInfo.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		jObj.add("tasks", tasks.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
		jObj.add("rewards", rewards.writeToJson(new JsonArray(), EnumSaveType.CONFIG));

		JsonArray reqJson = new JsonArray();
		for(IQuest quest : preRequisites) {
			int prID = parentDB.getKey(quest);

			if(prID >= 0) {
				reqJson.add(new JsonPrimitive(prID));
			}
		}
		jObj.add("preRequisites", reqJson);
	}

	private void readFromJson_Config(JsonObject jObj) {
		this.qInfo.readFromJson(JsonHelper.GetObject(jObj, "properties"), EnumSaveType.CONFIG);
		this.tasks.readFromJson(JsonHelper.GetArray(jObj, "tasks"), EnumSaveType.CONFIG);
		this.rewards.readFromJson(JsonHelper.GetArray(jObj, "rewards"), EnumSaveType.CONFIG);
		preRequisites.clear();
		for(JsonElement entry : JsonHelper.GetArray(jObj, "preRequisites")) {
			if(entry == null || !entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isNumber()) {
				continue;
			}
			int prID = entry.getAsInt();
			if(prID < 0) {
				continue;
			}
			IQuest tmp = parentDB.getValue(prID);
			if(tmp == null) {
				tmp = parentDB.createNew();
				parentDB.add(tmp, prID);
			}
			preRequisites.add(tmp);
		}
		this.setupProps();
	}

	private void writeToJson_Progress(JsonObject json) {
		JsonArray comJson = new JsonArray();
		for(UserEntry entry : completeUsers) {
			comJson.add(entry.writeToJson(new JsonObject()));
		}
		json.add("completed", comJson);
		JsonArray tskJson = tasks.writeToJson(new JsonArray(), EnumSaveType.PROGRESS);
		json.add("tasks", tskJson);
	}

	private void readFromJson_Progress(JsonObject json) {
		completeUsers.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "completed")) {
			if(entry == null || !entry.isJsonObject()) {
				continue;
			}
			try {
				UUID uuid = UUID.fromString(JsonHelper.GetString(entry.getAsJsonObject(), "uuid", ""));
				UserEntry user = new UserEntry(uuid);
				user.readFromJson(entry.getAsJsonObject());
				completeUsers.add(user);
			} catch(Exception e) {
				BetterQuesting.logger.error("Unable to load UUID for quest", e);
			}
		}
		tasks.readFromJson(JsonHelper.GetArray(json, "tasks"), EnumSaveType.PROGRESS);
	}
}