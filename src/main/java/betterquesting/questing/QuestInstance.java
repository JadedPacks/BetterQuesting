package betterquesting.questing;

import betterquesting.api.enums.*;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.storage.IRegStorageBase;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.misc.UserEntry;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyInstance;
import betterquesting.questing.party.PartyManager;
import betterquesting.questing.rewards.RewardStorage;
import betterquesting.questing.tasks.TaskStorage;
import betterquesting.storage.NameCache;
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

public class QuestInstance {
	// TODO: Global setting
	public final boolean partyLoot = false;
	public EnumLogic logicQuest = EnumLogic.AND, logicTask = EnumLogic.AND;
	public EnumQuestVisibility visibility = EnumQuestVisibility.NORMAL;
	public String name = "New Quest", desc = "No Description";
	public BigItemStack icon = new BigItemStack(Items.nether_star);
	public int repeat = -1;
	private final TaskStorage tasks = new TaskStorage();
	private final RewardStorage rewards = new RewardStorage();
	private final ArrayList<UserEntry> completeUsers = new ArrayList<>();
	private final ArrayList<QuestInstance> preRequisites = new ArrayList<>();

	public void update(EntityPlayer player) {
		UUID playerID = NameCache.getQuestingUUID(player);
		if(isComplete(playerID)) {
			UserEntry entry = GetUserEntry(playerID);
			if(!hasClaimed(playerID)) {
				if(canClaim(player)) {
					return;
				} else if(repeat < 0 || rewards.size() <= 0) {
					return;
				}
			} else if(rewards.size() > 0 && repeat >= 0 && player.worldObj.getTotalWorldTime() - entry.getTimestamp() >= repeat) {
				resetUser(playerID, false);
				if(!QuestSettings.edit) {
					postPresetNotice(player, 1);
				}
				PacketSender.sendToAll(getSyncPacket());
				return;
			} else {
				return;
			}
		}
		if(isUnlocked(playerID)) {
			int done = 0;
			for(ITask tsk : tasks.getAllValues()) {
				if(tsk.isComplete(playerID)) {
					PartyInstance party = PartyManager.getUserParty(playerID);
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
			if((tasks.size() > 0 || !QuestSettings.edit) && logicTask.getResult(done, tasks.size())) {
				setComplete(playerID, player.worldObj.getTotalWorldTime());
				PacketSender.sendToAll(getSyncPacket());
				if(!QuestSettings.edit) {
					postPresetNotice(player, 2);
				}
			}
		}
	}

	public void detect(EntityPlayer player) {
		UUID playerID = NameCache.getQuestingUUID(player);
		if(isComplete(playerID) && (repeat < 0 || rewards.size() <= 0)) {
			return;
		} else if(!canSubmit(player)) {
			return;
		}
		if(isUnlocked(playerID) || QuestSettings.edit) {
			int done = 0;
			boolean update = false;
			for(ITask tsk : tasks.getAllValues()) {
				if(!tsk.isComplete(playerID)) {
					tsk.detect(player, this);
					if(tsk.isComplete(playerID)) {
						PartyInstance party = PartyManager.getUserParty(playerID);
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
			if((tasks.size() > 0 || !QuestSettings.edit) && logicTask.getResult(done, tasks.size())) {
				setComplete(playerID, player.worldObj.getTotalWorldTime());
				if(!QuestSettings.edit) {
					postPresetNotice(player, 2);
				}
			} else if(update) {
				if(!QuestSettings.edit) {
					postPresetNotice(player, 1);
				}
			}
			PacketSender.sendToAll(getSyncPacket());
		}
	}

	public void postPresetNotice(EntityPlayer player, int preset) {
		switch(preset) {
			case 0:
				postNotice(player, "betterquesting.notice.unlock", name, "random.click", icon);
				break;
			case 1:
				postNotice(player, "betterquesting.notice.update", name, "random.levelup", icon);
				break;
			case 2:
				postNotice(player, "betterquesting.notice.complete", name, "random.levelup", icon);
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
		if(player instanceof EntityPlayerMP) {
			List<EntityPlayerMP> tarList = getPartyPlayers((EntityPlayerMP) player);
			for(EntityPlayerMP p : tarList) {
				PacketSender.sendToPlayer(payload, p);
			}
		}
	}

	private List<EntityPlayerMP> getPartyPlayers(EntityPlayerMP player) {
		List<EntityPlayerMP> list = new ArrayList<>();
		PartyInstance party = PartyManager.getUserParty(NameCache.getQuestingUUID(player));
		if(party == null) {
			list.add(player);
			return list;
		} else {
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			for(UUID mem : party.getMembers()) {
				for(Object p : server.getConfigurationManager().playerEntityList) {
					EntityPlayerMP pl = (EntityPlayerMP) p;
					if(pl != null && NameCache.getQuestingUUID(pl).equals(mem)) {
						list.add(pl);
					}
				}
			}
			return list;
		}
	}

	public boolean hasClaimed(UUID uuid) {
		if(rewards.size() <= 0) {
			return true;
		}
		UserEntry entry = GetUserEntry(uuid);
		if(entry == null) {
			return false;
		}
		return entry.hasClaimed();
	}

	public boolean canClaim(EntityPlayer player) {
		UserEntry entry = GetUserEntry(NameCache.getQuestingUUID(player));
		if(entry == null || hasClaimed(NameCache.getQuestingUUID(player))) {
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

	public void claimReward(EntityPlayer player) {
		for(IReward rew : rewards.getAllValues()) {
			rew.claimReward(player, this);
		}
		UUID pID = NameCache.getQuestingUUID(player);
		PartyInstance party = PartyManager.getUserParty(pID);
		if(party != null && partyLoot) {
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
		PacketSender.sendToAll(getSyncPacket());
	}

	public boolean canSubmit(EntityPlayer player) {
		if(player == null) {
			return false;
		}
		UUID playerID = NameCache.getQuestingUUID(player);
		UserEntry entry = this.GetUserEntry(playerID);
		if(entry == null) {
			return true;
		} else if(!entry.hasClaimed() && repeat >= 0) {
			int done = 0;
			for(ITask tsk : tasks.getAllValues()) {
				if(tsk.isComplete(playerID)) {
					done += 1;
				}
			}
			return !logicTask.getResult(done, tasks.size());
		} else {
			return false;
		}
	}

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
		list.add(StatCollector.translateToLocalFormatted(name));
		UUID playerID = NameCache.getQuestingUUID(player);
		if(isComplete(playerID)) {
			list.add(EnumChatFormatting.GREEN + StatCollector.translateToLocalFormatted("betterquesting.tooltip.complete"));
			if(!hasClaimed(playerID)) {
				list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.rewards_pending"));
			} else if(repeat > 0) {
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
			list.add(EnumChatFormatting.RED + "" + EnumChatFormatting.UNDERLINE + StatCollector.translateToLocalFormatted("betterquesting.tooltip.requires") + " (" + logicQuest.toString().toUpperCase() + ")");
			for(QuestInstance req : preRequisites) {
				if(!req.isComplete(playerID)) {
					list.add(EnumChatFormatting.RED + "- " + StatCollector.translateToLocalFormatted(req.name));
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
		list.add(StatCollector.translateToLocalFormatted(name) + " #" + QuestDatabase.getKey(this));
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.task_logic", logicQuest.toString().toUpperCase()));
		if(repeat >= 0) {
			long time = repeat / 20;
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
		if(repeat < 0) {
			return -1;
		}
		UserEntry ue = GetUserEntry(NameCache.getQuestingUUID(player));
		if(ue == null) {
			return 0;
		} else {
			return (repeat - (player.worldObj.getTotalWorldTime() - ue.getTimestamp())) / 20L;
		}
	}

	public QuestingPacket getSyncPacket() {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("config", writeToJson(new JsonObject()));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		tags.setInteger("questID", QuestDatabase.getKey(this));
		return new QuestingPacket(PacketTypeNative.QUEST_SYNC.GetLocation(), tags);
	}

	public void readPacket(NBTTagCompound payload) {
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		readFromJson(JsonHelper.GetObject(base, "config"));
	}

	public boolean isUnlocked(UUID uuid) {
		int A = 0;
		int B = preRequisites.size();
		if(B <= 0) {
			return true;
		}
		for(QuestInstance quest : preRequisites) {
			if(quest != null && quest.isComplete(uuid)) {
				A++;
			}
		}
		return logicQuest.getResult(A, B);
	}

	public void setComplete(UUID uuid, long timestamp) {
		PartyInstance party = PartyManager.getUserParty(uuid);
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

	public boolean isComplete(UUID uuid) {
		return GetUserEntry(uuid) != null;
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
			PacketSender.sendToAll(getSyncPacket());
		}
	}

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

	public IRegStorageBase<Integer, ITask> getTasks() {
		return tasks;
	}

	public IRegStorageBase<Integer, IReward> getRewards() {
		return rewards;
	}

	public List<QuestInstance> getPrerequisites() {
		return preRequisites;
	}

	public void readFromJson(JsonObject json) {
		JsonObject jObj = JsonHelper.GetObject(json, "properties");
		name = JsonHelper.GetString(jObj, "name", "New Quest");
		desc = JsonHelper.GetString(jObj, "desc", "No Description");
		repeat = JsonHelper.GetInt(jObj, "repeat", -1);
		icon = JsonHelper.JsonToItemStack(JsonHelper.GetObject(jObj, "icon"));
		logicQuest = JsonHelper.GetEnum(jObj, "logicQuest", EnumLogic.AND);
		logicTask = JsonHelper.GetEnum(jObj, "logicTask", EnumLogic.AND);
		visibility = JsonHelper.GetEnum(jObj, "visibility", EnumQuestVisibility.NORMAL);
		this.tasks.readFromJson(JsonHelper.GetArray(json, "tasks"));
		this.rewards.readFromJson(JsonHelper.GetArray(json, "rewards"));
		preRequisites.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "preRequisites")) {
			if(entry == null || !entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isNumber()) {
				continue;
			}
			int prID = entry.getAsInt();
			if(prID < 0) {
				continue;
			}
			QuestInstance tmp = QuestDatabase.getValue(prID);
			if(tmp == null) {
				tmp = QuestDatabase.createNew();
				QuestDatabase.add(tmp, prID);
			}
			preRequisites.add(tmp);
		}
	}

	public JsonObject writeToJson(JsonObject json) {
		JsonObject jObj = new JsonObject();
		jObj.addProperty("name", name);
		jObj.addProperty("desc", desc);
		jObj.add("icon", JsonHelper.ItemStackToJson(icon, new JsonObject()));
		if(logicQuest != EnumLogic.AND) {
			jObj.add("logicQuest", new JsonPrimitive(logicQuest.toString()));
		}
		if(logicTask != EnumLogic.AND) {
			jObj.add("logicTask", new JsonPrimitive(logicTask.toString()));
		}
		if(visibility != EnumQuestVisibility.NORMAL) {
			jObj.add("visibility", new JsonPrimitive(visibility.toString()));
		}
		if(repeat != -1) {
			jObj.addProperty("repeat", repeat);
		}
		json.add("properties", jObj);
		json.add("tasks", tasks.writeToJson(new JsonArray()));
		json.add("rewards", rewards.writeToJson(new JsonArray()));
		JsonArray reqJson = new JsonArray();
		for(QuestInstance quest : preRequisites) {
			int prID = QuestDatabase.getKey(quest);

			if(prID >= 0) {
				reqJson.add(new JsonPrimitive(prID));
			}
		}
		json.add("preRequisites", reqJson);
		return json;
	}
}