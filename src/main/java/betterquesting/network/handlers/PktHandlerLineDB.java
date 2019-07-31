package betterquesting.network.handlers;

import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.IPacketHandler;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

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
		PacketSender.INSTANCE.sendToPlayer(QuestLineDatabase.INSTANCE.getSyncPacket(), sender);
	}

	@Override
	public void handleClient(NBTTagCompound tag) {
		QuestLineDatabase.INSTANCE.readPacket(tag);
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
}