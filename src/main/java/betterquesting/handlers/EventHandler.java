package betterquesting.handlers;

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
import betterquesting.questing.party.PartyInstance;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.NameCache;
import betterquesting.storage.PlayerInstance;
import betterquesting.storage.QuestSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
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
			UUID uuid = NameCache.getQuestingUUID(player);
			List<QuestInstance> syncList = new ArrayList<>();
			List<QuestInstance> updateList = new ArrayList<>();
			for(Entry<ITask, QuestInstance> entry : QuestCache.INSTANCE.getActiveTasks(uuid).entrySet()) {
				ITask task = entry.getKey();
				QuestInstance quest = entry.getValue();
				if(!task.isComplete(uuid)) {
					if(task instanceof ITickableTask) {
						((ITickableTask) task).updateTask(player, quest);
					}
					if(task.isComplete(uuid)) {
						if(!syncList.contains(quest)) {
							syncList.add(quest);
						}
						if(!updateList.contains(quest)) {
							updateList.add(quest);
						}
					}
				}
			}
			if(player.ticksExisted % 20 == 0) {
				for(QuestInstance quest : QuestCache.INSTANCE.getActiveQuests(uuid)) {
					quest.update(player);
					if(quest.isComplete(uuid) && !syncList.contains(quest)) {
						syncList.add(quest);
						updateList.remove(quest);
					}
				}
				QuestCache.INSTANCE.updateCache(player);
			} else {
				Iterator<QuestInstance> iterator = syncList.iterator();
				while(iterator.hasNext()) {
					QuestInstance quest = iterator.next();
					quest.update(player);
					if(quest.isComplete(uuid) && !quest.canSubmit(player)) {
						iterator.remove();
						updateList.remove(quest);
					}
				}
			}
			for(QuestInstance quest : syncList) {
				PacketSender.sendToAll(quest.getSyncPacket());
			}
			for(QuestInstance quest : updateList) {
				quest.postPresetNotice(player, 1);
			}
		}
	}

	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {
		if(!event.world.isRemote && curWorldDir != null && event.world.provider.dimensionId == 0) {
			JsonObject jsonP = new JsonObject();
			jsonP.add("parties", PartyManager.writeToJson(new JsonArray()));
			JsonHelper.WriteToFile(new File(curWorldDir, "QuestingParties.json"), jsonP);
			JsonObject jsonN = new JsonObject();
			jsonN.add("nameCache", NameCache.writeToJson(new JsonArray()));
			JsonHelper.WriteToFile(new File(curWorldDir, "NameCache.json"), jsonN);
		}
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if(!event.world.isRemote && !MinecraftServer.getServer().isServerRunning()) {
			curWorldDir = null;
			QuestSettings.reset();
			QuestDatabase.reset();
			QuestLineDatabase.reset();
			NameCache.reset();
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if(event.world.isRemote || curWorldDir != null) {
			return;
		}
		QuestSettings.reset();
		QuestDatabase.reset();
		QuestLineDatabase.reset();
		NameCache.reset();
		MinecraftServer server = MinecraftServer.getServer();
		if(server.isSinglePlayer()) {
			curWorldDir = server.getFile("saves/" + server.getFolderName() + "/betterquesting");
		} else {
			curWorldDir = server.getFile(server.getFolderName() + "/betterquesting");
		}
		File f3 = new File(curWorldDir, "QuestingParties.json");
		JsonObject j3 = new JsonObject();
		if(f3.exists()) {
			j3 = JsonHelper.ReadFromFile(f3);
		}
		PartyManager.readFromJson(JsonHelper.GetArray(j3, "parties"));
		File f4 = new File(curWorldDir, "NameCache.json");
		JsonObject j4 = new JsonObject();
		if(f4.exists()) {
			j4 = JsonHelper.ReadFromFile(f4);
		}
		NameCache.readFromJson(JsonHelper.GetArray(j4, "nameCache"));
		BetterQuesting.logger.info("Loaded " + QuestDatabase.size() + " quests");
		BetterQuesting.logger.info("Loaded " + QuestLineDatabase.size() + " quest lines");
		BetterQuesting.logger.info("Loaded " + PartyManager.size() + " parties");
		BetterQuesting.logger.info("Loaded " + NameCache.size() + " names");
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if(!event.player.worldObj.isRemote && event.player instanceof EntityPlayerMP) {
			EntityPlayerMP mpPlayer = (EntityPlayerMP) event.player;
			NameCache.updateNames(MinecraftServer.getServer());
			PacketSender.sendToPlayer(QuestSettings.getSyncPacket(), mpPlayer);
			PacketSender.sendToPlayer(QuestDatabase.getSyncPacket(), mpPlayer);
			PacketSender.sendToPlayer(QuestLineDatabase.getSyncPacket(), mpPlayer);
			PacketSender.sendToPlayer(PartyManager.getSyncPacket(), mpPlayer);
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
		if(!(event.entityLiving instanceof EntityPlayer) || !QuestSettings.hardcore) {
			return;
		}
		EntityPlayerMP player = (EntityPlayerMP) event.entityLiving;
		UUID uuid = NameCache.getQuestingUUID(player);
		PartyInstance party = PartyManager.getUserParty(uuid);
		PlayerInstance pInst = NameCache.getInstance(party == null || !party.sharedLives ? uuid : party.getOwner());
		int lives = pInst.lives - 1;
		pInst.lives = lives;
		if(lives <= 0) {
			MinecraftServer server = MinecraftServer.getServer();
			if(server == null) {
				return;
			}
			player.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
			if(server.isSinglePlayer() && player.getCommandSenderName().equals(server.getServerOwner())) {
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
				UserListBansEntry userlistbansentry = new UserListBansEntry(player.getGameProfile(), null, "(You just lost the game)", null, "Death in Hardcore");
				server.getConfigurationManager().getBannedPlayers().addEntry(userlistbansentry);
			}
		} else {
			if(lives == 1) {
				ChatComponentText cct = new ChatComponentText(EnumChatFormatting.RED + "This is your last life!");
				cct.getChatStyle().setColor(EnumChatFormatting.RED).setBold(true);
				player.addChatComponentMessage(cct);
			} else {
				ChatComponentText cct = new ChatComponentText(lives + " lives remaining!");
				cct.getChatStyle().setColor(EnumChatFormatting.RED).setBold(true);
				player.addChatComponentMessage(cct);
			}
		}
	}
}