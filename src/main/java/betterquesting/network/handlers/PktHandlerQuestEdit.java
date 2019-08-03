package betterquesting.network.handlers;

import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.storage.NameCache;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class PktHandlerQuestEdit implements IPacketHandler {
	@Override
	public ResourceLocation getRegistryName() {
		return PacketTypeNative.QUEST_EDIT.GetLocation();
	}

	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender) {
		if(sender == null) {
			return;
		}
		boolean isOP = MinecraftServer.getServer().getConfigurationManager().canSendCommands(sender.getGameProfile());
		if(!isOP) {
			BetterQuesting.logger.warn("Player " + sender.getCommandSenderName() + " (UUID:" + NameCache.getQuestingUUID(sender) + ") tried to edit quest without OP permissions!");
			sender.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "You need to be OP to edit quests!"));
			return;
		}
		int aID = !data.hasKey("action") ? -1 : data.getInteger("action");
		int qID = !data.hasKey("questID") ? -1 : data.getInteger("questID");
		QuestInstance quest = QuestDatabase.getValue(qID);
		EnumPacketAction action;
		if(aID < 0 || aID >= EnumPacketAction.values().length) {
			return;
		}
		action = EnumPacketAction.values()[aID];
		if(action == EnumPacketAction.EDIT && quest != null) {
			quest.readPacket(data);
			PacketSender.sendToAll(quest.getSyncPacket());
		} else if(action == EnumPacketAction.REMOVE) {
			if(quest == null || qID < 0) {
				BetterQuesting.logger.error(sender.getCommandSenderName() + " tried to delete non-existent quest with ID:" + qID);
				return;
			}
			BetterQuesting.logger.info("Player " + sender.getCommandSenderName() + " deleted quest " + quest.name);
			QuestDatabase.removeKey(qID);
			PacketSender.sendToAll(QuestDatabase.getSyncPacket());
		} else if(action == EnumPacketAction.SET && quest != null) {
			if(data.getBoolean("state")) {
				UUID senderID = NameCache.getQuestingUUID(sender);
				quest.setComplete(senderID, 0);
				int done = 0;
				if(!quest.logicTask.getResult(done, quest.getTasks().size())) {
					for(ITask task : quest.getTasks().getAllValues()) {
						task.setComplete(senderID);
						done += 1;
						if(quest.logicTask.getResult(done, quest.getTasks().size())) {
							break;
						}
					}
				}
			} else {
				quest.resetAll(true);
			}
			PacketSender.sendToAll(quest.getSyncPacket());
		} else if(action == EnumPacketAction.ADD) {
			QuestInstance nq = new QuestInstance();
			int nID = QuestDatabase.nextKey();
			if(data.hasKey("data") && data.hasKey("questID")) {
				nID = data.getInteger("questID");
				JsonObject base = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("data"), new JsonObject());
				nq.readFromJson(JsonHelper.GetObject(base, "config"), EnumSaveType.CONFIG);
			}
			QuestDatabase.add(nq, nID);
			PacketSender.sendToAll(nq.getSyncPacket());
		}
	}

	@Override
	public void handleClient(NBTTagCompound data) {}
}