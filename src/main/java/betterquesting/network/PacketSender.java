package betterquesting.network;

import betterquesting.api.network.QuestingPacket;
import betterquesting.core.BetterQuesting;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class PacketSender {
	public static void sendToPlayer(QuestingPacket payload, EntityPlayerMP player) {
		payload.getPayload().setString("ID", payload.getHandler().toString());
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload())) {
			BetterQuesting.instance.network.sendTo(new PacketQuesting(p), player);
		}
	}

	public static void sendToAll(QuestingPacket payload) {
		payload.getPayload().setString("ID", payload.getHandler().toString());
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload())) {
			BetterQuesting.instance.network.sendToAll(new PacketQuesting(p));
		}
	}

	public static void sendToServer(QuestingPacket payload) {
		payload.getPayload().setString("ID", payload.getHandler().toString());
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload())) {
			BetterQuesting.instance.network.sendToServer(new PacketQuesting(p));
		}
	}
}