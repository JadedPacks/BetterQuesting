package betterquesting.network.handlers;

import betterquesting.api.network.IPacketHandler;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.NameCache;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class PktHandlerNameCache implements IPacketHandler {
	@Override
	public ResourceLocation getRegistryName() {
		return PacketTypeNative.NAME_CACHE.GetLocation();
	}

	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender) {
		if(sender == null) {
			return;
		}
		PacketSender.INSTANCE.sendToPlayer(NameCache.INSTANCE.getSyncPacket(), sender);
	}

	@Override
	public void handleClient(NBTTagCompound tag) {
		NameCache.INSTANCE.readPacket(tag);
	}
}