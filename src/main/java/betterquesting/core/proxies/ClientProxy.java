package betterquesting.core.proxies;

import betterquesting.client.BQ_Keybindings;
import betterquesting.client.QuestNotification;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
	@Override
	public void registerHandlers() {
		super.registerHandlers();
		MinecraftForge.EVENT_BUS.register(new QuestNotification());
		BQ_Keybindings.RegisterKeys();
	}
}