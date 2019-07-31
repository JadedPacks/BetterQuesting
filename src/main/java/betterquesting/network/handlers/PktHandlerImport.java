package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketHandler;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.List;

public class PktHandlerImport implements IPacketHandler {
	@Override
	public ResourceLocation getRegistryName() {
		return PacketTypeNative.IMPORT.GetLocation();
	}

	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender) {
		if(sender == null) {
			return;
		}
		boolean isOP = MinecraftServer.getServer().getConfigurationManager().canSendCommands(sender.getGameProfile());
		if(!isOP) {
			BetterQuesting.logger.warn("Player " + sender.getCommandSenderName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to import quests without OP permissions!");
			sender.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "You need to be OP to edit quests!"));
			return;
		}
		PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
		PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
	}

	@Override
	public void handleClient(NBTTagCompound tag) {}

	private HashMap<Integer, Integer> getRemappedIDs(List<Integer> idList) {
		List<Integer> existing = QuestDatabase.INSTANCE.getAllKeys();
		HashMap<Integer, Integer> remapped = new HashMap<>();
		int n = 0;
		for(int id : idList) {
			while(existing.contains(n) || remapped.containsValue(n)) {
				n++;
			}
			remapped.put(id, n);
		}
		return remapped;
	}
}