package betterquesting.api.network;

import net.minecraft.entity.player.EntityPlayerMP;

public interface IPacketSender {
	void sendToPlayer(QuestingPacket payload, EntityPlayerMP player);
	void sendToAll(QuestingPacket payload);
	void sendToServer(QuestingPacket payload);
}