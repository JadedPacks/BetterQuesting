package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.IPacketHandler;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.QuestSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class PktHandlerSettings implements IPacketHandler {
	@Override
	public ResourceLocation getRegistryName() {
		return PacketTypeNative.SETTINGS.GetLocation();
	}

	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender) {
		if(sender == null) {
			return;
		}
		boolean isOP = MinecraftServer.getServer().getConfigurationManager().canSendCommands(sender.getGameProfile());
		if(!isOP) {
			BetterQuesting.logger.warn("Player " + sender.getCommandSenderName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to edit settings without OP permissions!");
			sender.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "You need to be OP to edit quests!"));
			return;
		}
		QuestSettings.INSTANCE.readPacket(tag);
		PacketSender.INSTANCE.sendToAll(QuestSettings.INSTANCE.getSyncPacket());
	}

	@Override
	public void handleClient(NBTTagCompound tag) {
		QuestSettings.INSTANCE.readPacket(tag);
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
}