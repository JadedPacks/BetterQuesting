package betterquesting.core.proxies;

import betterquesting.handlers.EventHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
	public boolean isClient() {
		return false;
	}

	public void registerHandlers() {
		EventHandler handler = new EventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		MinecraftForge.TERRAIN_GEN_BUS.register(handler);
		FMLCommonHandler.instance().bus().register(handler);
	}
}