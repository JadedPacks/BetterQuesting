package betterquesting.network.handlers;

import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.network.IPacketHandler;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestLine;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class PktHandlerLineSync implements IPacketHandler {
	@Override
	public ResourceLocation getRegistryName() {
		return PacketTypeNative.LINE_SYNC.GetLocation();
	}

	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender) {
		if(sender == null) {
			return;
		}
		int id = !tag.hasKey("lineID") ? -1 : tag.getInteger("lineID");
		QuestLine questLine = QuestLineDatabase.getValue(id);
		if(questLine != null) {
			PacketSender.sendToPlayer(questLine.getSyncPacket(), sender);
		}
	}

	@Override
	public void handleClient(NBTTagCompound tag) {
		int id = !tag.hasKey("lineID") ? -1 : tag.getInteger("lineID");
		QuestLine questLine = QuestLineDatabase.getValue(id);
		if(questLine == null) {
			questLine = new QuestLine();
			QuestLineDatabase.add(questLine, id);
		}
		questLine.readPacket(tag);
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if(screen instanceof INeedsRefresh) {
			((INeedsRefresh) screen).refreshGui();
		}
	}
}