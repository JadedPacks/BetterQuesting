package betterquesting.network.handlers;

import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.network.IPacketHandler;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.storage.NameCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class PktHandlerClaim implements IPacketHandler {
	@Override
	public ResourceLocation getRegistryName() {
		return PacketTypeNative.CLAIM.GetLocation();
	}

	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender) {
		if(sender == null) {
			return;
		}
		QuestInstance quest = QuestDatabase.getValue(data.getInteger("questID"));
		if(quest != null && !quest.hasClaimed(NameCache.getQuestingUUID(sender)) && quest.canClaim(sender)) {
			quest.claimReward(sender);
		}
	}

	@Override
	public void handleClient(NBTTagCompound data) {
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if(screen instanceof INeedsRefresh) {
			((INeedsRefresh) screen).refreshGui();
		}
	}
}