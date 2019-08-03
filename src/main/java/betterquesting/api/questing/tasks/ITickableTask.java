package betterquesting.api.questing.tasks;

import betterquesting.questing.QuestInstance;
import net.minecraft.entity.player.EntityPlayer;

public interface ITickableTask {
	void updateTask(EntityPlayer player, QuestInstance quest);
}