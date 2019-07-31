package betterquesting.core.proxies;

import betterquesting.api.placeholders.EntityPlaceholder;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.QuestNotification;
import betterquesting.client.renderer.EntityPlaceholderRenderer;
import betterquesting.client.toolbox.ToolboxRegistry;
import betterquesting.client.toolbox.ToolboxTabMain;
import betterquesting.core.BetterQuesting;
import betterquesting.misc.QuestResourcesFolder;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;

public class ClientProxy extends CommonProxy {
	@Override
	public boolean isClient() {
		return true;
	}

	@Override
	public void registerHandlers() {
		super.registerHandlers();
		MinecraftForge.EVENT_BUS.register(new QuestNotification());
		BQ_Keybindings.RegisterKeys();
		try {
			// TODO: Use AccessTransformers?
			ArrayList list = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "field_110449_ao");
			QuestResourcesFolder qRes1 = new QuestResourcesFolder();
			list.add(qRes1);
			((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).reloadResourcePack(qRes1);
		} catch(Exception e) {
			BetterQuesting.logger.error("Unable to install questing resource loaders", e);
		}
		RenderingRegistry.registerEntityRenderingHandler(EntityPlaceholder.class, new EntityPlaceholderRenderer());
		ToolboxRegistry.INSTANCE.registerToolbox(ToolboxTabMain.instance);
	}
}