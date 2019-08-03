package betterquesting.network.handlers;

import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.network.IPacketHandler;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class PktHandlerLineDB implements IPacketHandler {
	@Override
	public ResourceLocation getRegistryName() {
		return PacketTypeNative.LINE_DATABASE.GetLocation();
	}

	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender) {
		if(sender == null) {
			return;
		}
		PacketSender.sendToPlayer(QuestLineDatabase.getSyncPacket(), sender);
	}

	@Override
	public void handleClient(NBTTagCompound tag) {
		QuestLineDatabase.readPacket(tag);
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if(screen instanceof INeedsRefresh) {
			((INeedsRefresh) screen).refreshGui();
		}
	}
}