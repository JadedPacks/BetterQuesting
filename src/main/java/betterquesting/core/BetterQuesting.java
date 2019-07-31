package betterquesting.core;

import betterquesting.api.placeholders.EntityPlaceholder;
import betterquesting.api.placeholders.FluidPlaceholder;
import betterquesting.api.placeholders.ItemPlaceholder;
import betterquesting.client.CreativeTabQuesting;
import betterquesting.commands.BQ_CommandAdmin;
import betterquesting.core.proxies.CommonProxy;
import betterquesting.items.ItemExtraLife;
import betterquesting.network.PacketQuesting;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = "betterquesting", version = "@VERSION@", name = "BetterQuesting")
public class BetterQuesting {
	@Instance("betterquesting")
	public static BetterQuesting instance;
	@SidedProxy(clientSide = "betterquesting.core.proxies.ClientProxy", serverSide = "betterquesting.core.proxies.CommonProxy")
	public static CommonProxy proxy;
	public SimpleNetworkWrapper network;
	public static Logger logger;
	public static final CreativeTabs tabQuesting = new CreativeTabQuesting();
	public static final Item extraLife = new ItemExtraLife();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		network = NetworkRegistry.INSTANCE.newSimpleChannel("BQ_NET_CHAN");
		proxy.registerHandlers();
		network.registerMessage(PacketQuesting.HandleClient.class, PacketQuesting.class, 0, Side.CLIENT);
		network.registerMessage(PacketQuesting.HandleServer.class, PacketQuesting.class, 0, Side.SERVER);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		FluidRegistry.registerFluid(FluidPlaceholder.fluidPlaceholder);
		GameRegistry.registerItem(ItemPlaceholder.placeholder, "placeholder");
		GameRegistry.registerItem(extraLife, "extra_life");
		GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 1, 0), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2));
		GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 1, 0), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 1, 0), new ItemStack(extraLife, 1, 1), new ItemStack(extraLife, 1, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 2, 1), new ItemStack(extraLife, 1, 0));
		GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 1, 1), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2));
		GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 2, 2), new ItemStack(extraLife, 1, 1));
		EntityRegistry.registerModEntity(EntityPlaceholder.class, "placeholder", 0, this, 16, 1, false);
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new BQ_CommandAdmin());
	}
}