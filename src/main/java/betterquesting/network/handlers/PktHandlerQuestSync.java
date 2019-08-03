package betterquesting.network.handlers;

import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.network.IPacketHandler;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class PktHandlerQuestSync implements IPacketHandler {
	@Override
	public ResourceLocation getRegistryName() {
		return PacketTypeNative.QUEST_SYNC.GetLocation();
	}

	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender) {}

	@Override
	public void handleClient(NBTTagCompound data) {
		int questID = !data.hasKey("questID") ? -1 : data.getInteger("questID");
		QuestInstance quest = QuestDatabase.getValue(questID);
		if(quest == null) {
			quest = new QuestInstance();
			QuestDatabase.add(quest, questID);
		}
		quest.readPacket(data);
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if(screen instanceof INeedsRefresh) {
			((INeedsRefresh) screen).refreshGui();
		}
	}
}