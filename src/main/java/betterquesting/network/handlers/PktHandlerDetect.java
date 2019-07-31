package betterquesting.network.handlers;

import betterquesting.api.network.IPacketHandler;
import betterquesting.api.questing.IQuest;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class PktHandlerDetect implements IPacketHandler {
	@Override
	public ResourceLocation getRegistryName() {
		return PacketTypeNative.DETECT.GetLocation();
	}

	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender) {
		if(sender == null) {
			return;
		}
		IQuest quest = QuestDatabase.INSTANCE.getValue(data.getInteger("questID"));
		if(quest != null) {
			quest.detect(sender);
		}
	}

	@Override
	public void handleClient(NBTTagCompound data) {}
}