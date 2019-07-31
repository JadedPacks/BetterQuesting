package betterquesting.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.placeholders.FluidPlaceholder;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.questing.tasks.ITickableTask;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.QuestCache;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.gui.GuiHome;
import betterquesting.client.gui.GuiQuestLinesMain;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class EventHandler {
	private File curWorldDir;

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKey(InputEvent.KeyInputEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if(BQ_Keybindings.openQuests.isPressed()) {
			if(GuiQuestLinesMain.bookmarked != null) {
				mc.displayGuiScreen(GuiQuestLinesMain.bookmarked);
			} else {
				mc.displayGuiScreen(new GuiHome(mc.currentScreen));
			}
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		if(event.entityLiving.worldObj.isRemote) {
			return;
		}
		if(event.entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entityLiving;
			UUID uuid = QuestingAPI.getQuestingUUID(player);
			List<IQuest> syncList = new ArrayList<>();
			List<QuestInstance> updateList = new ArrayList<>();
			for(Entry<ITask, IQuest> entry : QuestCache.INSTANCE.getActiveTasks(uuid).entrySet()) {
				ITask task = entry.getKey();
				IQuest quest = entry.getValue();
				if(!task.isComplete(uuid)) {
					if(task instanceof ITickableTask) {
						((ITickableTask) task).updateTask(player, quest);
					}
					if(task.isComplete(uuid)) {
						if(!syncList.contains(quest)) {
							syncList.add(quest);
						}
						if(quest instanceof QuestInstance && !updateList.contains(quest)) {
							updateList.add((QuestInstance) quest);
						}
					}
				}
			}
			if(player.ticksExisted % 20 == 0) {
				for(IQuest quest : QuestCache.INSTANCE.getActiveQuests(uuid)) {
					quest.update(player);
					if(quest.isComplete(uuid) && !syncList.contains(quest)) {
						syncList.add(quest);
						updateList.remove(quest);
					}
				}
				QuestCache.INSTANCE.updateCache(player);
			} else {
				Iterator<IQuest> iterator = syncList.iterator();
				while(iterator.hasNext()) {
					IQuest quest = iterator.next();
					quest.update(player);
					if(quest.isComplete(uuid) && !quest.canSubmit(player)) {
						iterator.remove();
						updateList.remove(quest);
					}
				}
			}
			for(IQuest quest : syncList) {
				PacketSender.INSTANCE.sendToAll(quest.getSyncPacket());
			}
			for(QuestInstance quest : updateList) {
				quest.postPresetNotice(player, 1);
			}
		}
	}

	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {
		if(!event.world.isRemote && curWorldDir != null && event.world.provider.dimensionId == 0) {
			JsonObject jsonCon = new JsonObject();
			jsonCon.add("questSettings", QuestSettings.INSTANCE.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
			jsonCon.add("questDatabase", QuestDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			jsonCon.add("questLines", QuestLineDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			JsonHelper.WriteToFile(new File(curWorldDir, "QuestDatabase.json"), jsonCon);
			JsonObject jsonProg = new JsonObject();
			jsonProg.add("questProgress", QuestDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.PROGRESS));
			JsonHelper.WriteToFile(new File(curWorldDir, "QuestProgress.json"), jsonProg);
			JsonObject jsonP = new JsonObject();
			jsonP.add("parties", PartyManager.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			JsonHelper.WriteToFile(new File(curWorldDir, "QuestingParties.json"), jsonP);
			JsonObject jsonN = new JsonObject();
			jsonN.add("nameCache", NameCache.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			JsonHelper.WriteToFile(new File(curWorldDir, "NameCache.json"), jsonN);
			JsonObject jsonL = new JsonObject();
			jsonL.add("lifeDatabase", LifeDatabase.INSTANCE.writeToJson(new JsonObject(), EnumSaveType.PROGRESS));
			JsonHelper.WriteToFile(new File(curWorldDir, "LifeDatabase.json"), jsonL);
			MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Save());
		}
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if(!event.world.isRemote && !MinecraftServer.getServer().isServerRunning()) {
			curWorldDir = null;
			QuestSettings.INSTANCE.reset();
			QuestDatabase.INSTANCE.reset();
			QuestLineDatabase.INSTANCE.reset();
			LifeDatabase.INSTANCE.reset();
			NameCache.INSTANCE.reset();
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if(event.world.isRemote || curWorldDir != null) {
			return;
		}
		QuestSettings.INSTANCE.reset();
		QuestDatabase.INSTANCE.reset();
		QuestLineDatabase.INSTANCE.reset();
		LifeDatabase.INSTANCE.reset();
		NameCache.INSTANCE.reset();
		if(BetterQuesting.proxy.isClient()) {
			GuiQuestLinesMain.bookmarked = null;
		}
		MinecraftServer server = MinecraftServer.getServer();
		File readDir;
		if(BetterQuesting.proxy.isClient()) {
			curWorldDir = server.getFile("saves/" + server.getFolderName() + "/betterquesting");
		} else {
			curWorldDir = server.getFile(server.getFolderName() + "/betterquesting");
		}
		File f2 = new File(curWorldDir, "QuestProgress.json");
		JsonObject j2 = new JsonObject();
		if(f2.exists()) {
			j2 = JsonHelper.ReadFromFile(f2);
		}
		QuestDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(j2, "questProgress"), EnumSaveType.PROGRESS);
		File f3 = new File(curWorldDir, "QuestingParties.json");
		JsonObject j3 = new JsonObject();
		if(f3.exists()) {
			j3 = JsonHelper.ReadFromFile(f3);
		}
		PartyManager.INSTANCE.readFromJson(JsonHelper.GetArray(j3, "parties"), EnumSaveType.CONFIG);
		File f4 = new File(curWorldDir, "NameCache.json");
		JsonObject j4 = new JsonObject();
		if(f4.exists()) {
			j4 = JsonHelper.ReadFromFile(f4);
		}
		NameCache.INSTANCE.readFromJson(JsonHelper.GetArray(j4, "nameCache"), EnumSaveType.CONFIG);
		File f5 = new File(curWorldDir, "LifeDatabase.json");
		JsonObject j5 = new JsonObject();
		if(f5.exists()) {
			j5 = JsonHelper.ReadFromFile(f5);
		}
		LifeDatabase.INSTANCE.readFromJson(JsonHelper.GetObject(j5, "lifeDatabase"), EnumSaveType.CONFIG);
		LifeDatabase.INSTANCE.readFromJson(JsonHelper.GetObject(j5, "lifeDatabase"), EnumSaveType.PROGRESS);
		BetterQuesting.logger.info("Loaded " + QuestDatabase.INSTANCE.size() + " quests");
		BetterQuesting.logger.info("Loaded " + QuestLineDatabase.INSTANCE.size() + " quest lines");
		BetterQuesting.logger.info("Loaded " + PartyManager.INSTANCE.size() + " parties");
		BetterQuesting.logger.info("Loaded " + NameCache.INSTANCE.size() + " names");
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Load());
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if(!event.player.worldObj.isRemote && event.player instanceof EntityPlayerMP) {
			EntityPlayerMP mpPlayer = (EntityPlayerMP) event.player;
			NameCache.INSTANCE.updateNames(MinecraftServer.getServer());
			PacketSender.INSTANCE.sendToPlayer(QuestSettings.INSTANCE.getSyncPacket(), mpPlayer);
			PacketSender.INSTANCE.sendToPlayer(QuestDatabase.INSTANCE.getSyncPacket(), mpPlayer);
			PacketSender.INSTANCE.sendToPlayer(QuestLineDatabase.INSTANCE.getSyncPacket(), mpPlayer);
			PacketSender.INSTANCE.sendToPlayer(LifeDatabase.INSTANCE.getSyncPacket(), mpPlayer);
			PacketSender.INSTANCE.sendToPlayer(PartyManager.INSTANCE.getSyncPacket(), mpPlayer);
		}
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE) && event.player instanceof EntityPlayerMP && !((EntityPlayerMP) event.player).playerConqueredTheEnd) {
			EntityPlayerMP mpPlayer = (EntityPlayerMP) event.player;
			IParty party = PartyManager.INSTANCE.getUserParty(QuestingAPI.getQuestingUUID(mpPlayer));
			int lives = (party == null || !party.getProperties().getProperty(NativeProps.PARTY_LIVES)) ? LifeDatabase.INSTANCE.getLives(QuestingAPI.getQuestingUUID(mpPlayer)) : LifeDatabase.INSTANCE.getLives(party);
			if(lives <= 0) {
				MinecraftServer server = MinecraftServer.getServer();
				if(server == null) {
					return;
				}
				if(server.isSinglePlayer() && mpPlayer.getCommandSenderName().equals(server.getServerOwner())) {
					mpPlayer.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
					File path = server.getEntityWorld().getSaveHandler().getWorldDirectory(),
						target = new File(path + "/../_gameover/" + server.getEntityWorld().getSaveHandler().getWorldDirectoryName());
					server.stopServer();
					if(target.exists()) {
						target.delete();
					}
					target.mkdir();
					try {
						copyDirectory(path, target);
						path.delete();
					} catch(IOException e) {
						e.printStackTrace();
					}
				} else {
					UserListBansEntry userlistbansentry = new UserListBansEntry(mpPlayer.getGameProfile(), null, "(You just lost the game)", null, "Death in Hardcore");
					server.getConfigurationManager().getBannedPlayers().addEntry(userlistbansentry);
					mpPlayer.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
				}
			} else {
				if(lives == 1) {
					ChatComponentText cct = new ChatComponentText(EnumChatFormatting.RED + "This is your last life!");
					cct.getChatStyle().setColor(EnumChatFormatting.RED).setBold(true);
					mpPlayer.addChatComponentMessage(cct);
				} else {
					ChatComponentText cct = new ChatComponentText(lives + " lives remaining!");
					cct.getChatStyle().setColor(EnumChatFormatting.RED).setBold(true);
					mpPlayer.addChatComponentMessage(cct);
				}
			}
		}
	}

	private void copyDirectory(final File sourceLocation, final File targetLocation) throws IOException {
		if(sourceLocation.isDirectory()) {
			if(!targetLocation.exists()) {
				targetLocation.mkdir();
			}
			final String[] children = sourceLocation.list();
			if(children != null) {
				for(String child : children) {
					copyDirectory(new File(sourceLocation, child), new File(targetLocation, child));
				}
			}
		} else {
			final InputStream in = new FileInputStream(sourceLocation);
			final OutputStream out = new FileOutputStream(targetLocation);
			final byte[] buf = new byte[1024];
			int len;
			while((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		if(event.entityLiving.worldObj.isRemote || !QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE)) {
			return;
		}
		if(event.entityLiving instanceof EntityPlayer) {
			UUID uuid = QuestingAPI.getQuestingUUID(((EntityPlayer) event.entityLiving));
			IParty party = PartyManager.INSTANCE.getUserParty(uuid);
			if(party == null || !party.getProperties().getProperty(NativeProps.PARTY_LIVES)) {
				int lives = LifeDatabase.INSTANCE.getLives(uuid);
				LifeDatabase.INSTANCE.setLives(uuid, lives - 1);
			} else {
				int lives = LifeDatabase.INSTANCE.getLives(party);
				LifeDatabase.INSTANCE.setLives(party, lives - 1);
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		if(event.map.getTextureType() == 0) {
			IIcon icon = event.map.registerIcon("betterquesting:fluid_placeholder");
			FluidPlaceholder.fluidPlaceholder.setIcons(icon);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onDataUpdated(DatabaseEvent.Update event) {
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if(screen instanceof INeedsRefresh) {
			((INeedsRefresh) screen).refreshGui();
		}
	}

	@SubscribeEvent
	public void onCommand(CommandEvent event) {
		MinecraftServer server = MinecraftServer.getServer();
		if(server != null && (event.command.getCommandName().equalsIgnoreCase("op") || event.command.getCommandName().equalsIgnoreCase("deop"))) {
			NameCache.INSTANCE.updateNames(server);
		}
	}
}