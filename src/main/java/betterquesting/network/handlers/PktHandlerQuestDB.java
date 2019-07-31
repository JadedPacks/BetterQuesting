package betterquesting.network.handlers;

import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.IPacketHandler;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class PktHandlerQuestDB implements IPacketHandler {
	@Override
	public ResourceLocation getRegistryName() {
		return PacketTypeNative.QUEST_DATABASE.GetLocation();
	}

	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender) {
		if(sender == null) {
			return;
		}
		PacketSender.INSTANCE.sendToPlayer(QuestDatabase.INSTANCE.getSyncPacket(), sender);
	}

	@Override
	public void handleClient(NBTTagCompound data) {
		QuestDatabase.INSTANCE.readPacket(data);
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
}