package betterquesting.api.questing.tasks;

import betterquesting.api.questing.IQuest;
import net.minecraft.entity.player.EntityPlayer;

public interface ITickableTask {
	void updateTask(EntityPlayer player, IQuest quest);
}