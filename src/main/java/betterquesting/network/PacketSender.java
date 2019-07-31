package betterquesting.network;

import betterquesting.api.network.IPacketSender;
import betterquesting.api.network.QuestingPacket;
import betterquesting.core.BetterQuesting;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class PacketSender implements IPacketSender {
	public static final PacketSender INSTANCE = new PacketSender();

	@Override
	public void sendToPlayer(QuestingPacket payload, EntityPlayerMP player) {
		payload.getPayload().setString("ID", payload.getHandler().toString());
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload())) {
			BetterQuesting.instance.network.sendTo(new PacketQuesting(p), player);
		}
	}

	@Override
	public void sendToAll(QuestingPacket payload) {
		payload.getPayload().setString("ID", payload.getHandler().toString());
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload())) {
			BetterQuesting.instance.network.sendToAll(new PacketQuesting(p));
		}
	}

	@Override
	public void sendToServer(QuestingPacket payload) {
		payload.getPayload().setString("ID", payload.getHandler().toString());
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload())) {
			BetterQuesting.instance.network.sendToServer(new PacketQuesting(p));
		}
	}
}