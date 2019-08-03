package betterquesting.network.handlers;

import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestLine;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.storage.NameCache;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class PktHandlerLineEdit implements IPacketHandler {
	@Override
	public ResourceLocation getRegistryName() {
		return PacketTypeNative.LINE_EDIT.GetLocation();
	}

	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender) {
		if(sender == null) {
			return;
		}
		boolean isOP = MinecraftServer.getServer().getConfigurationManager().canSendCommands(sender.getGameProfile());
		if(!isOP) {
			BetterQuesting.logger.warn("Player " + sender.getCommandSenderName() + " (UUID:" + NameCache.getQuestingUUID(sender) + ") tried to edit quest lines without OP permissions!");
			sender.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "You need to be OP to edit quests!"));
			return;
		}
		int aID = !data.hasKey("action") ? -1 : data.getInteger("action");
		int lID = !data.hasKey("lineID") ? -1 : data.getInteger("lineID");
		int idx = !data.hasKey("order") ? -1 : data.getInteger("order");
		QuestLine questLine = QuestLineDatabase.getValue(lID);
		if(aID < 0 || aID >= EnumPacketAction.values().length) {
			return;
		}
		EnumPacketAction action = EnumPacketAction.values()[aID];
		if(action == EnumPacketAction.ADD) {
			QuestLine nq = new QuestLine();
			int nID = QuestLineDatabase.nextKey();
			if(data.hasKey("data") && lID >= 0) {
				nID = lID;
				JsonObject base = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("data"), new JsonObject());
				nq.readFromJson(JsonHelper.GetObject(base, "line"));
			}
			QuestLineDatabase.add(nq, nID);
			PacketSender.sendToAll(nq.getSyncPacket());
		} else if(action == EnumPacketAction.EDIT && questLine != null) {
			questLine.readPacket(data);
			if(idx >= 0 && QuestLineDatabase.getOrderIndex(lID) != idx) {
				QuestLineDatabase.setOrderIndex(lID, idx);
				PacketSender.sendToAll(QuestLineDatabase.getSyncPacket());
			} else {
				PacketSender.sendToAll(questLine.getSyncPacket());
			}
		} else if(action == EnumPacketAction.REMOVE && questLine != null) {
			QuestLineDatabase.removeKey(lID);
			PacketSender.sendToAll(QuestLineDatabase.getSyncPacket());
		}
	}

	@Override
	public void handleClient(NBTTagCompound data) {}
}