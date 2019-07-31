package betterquesting.api.storage;

import betterquesting.api.misc.IDataSync;
import betterquesting.api.properties.IPropertyContainer;
import net.minecraft.entity.player.EntityPlayer;

public interface IQuestSettings extends IPropertyContainer, IDataSync {
	boolean canUserEdit(EntityPlayer player);
}