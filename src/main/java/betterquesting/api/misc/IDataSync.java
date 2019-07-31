package betterquesting.api.misc;

import betterquesting.api.network.QuestingPacket;
import net.minecraft.nbt.NBTTagCompound;

public interface IDataSync {
	QuestingPacket getSyncPacket();
	void readPacket(NBTTagCompound payload);
}