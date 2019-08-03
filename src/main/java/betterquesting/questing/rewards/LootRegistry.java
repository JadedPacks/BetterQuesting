package betterquesting.questing.rewards;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.event.world.WorldEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class LootRegistry {
	public static final CopyOnWriteArrayList<LootGroup> lootGroups = new CopyOnWriteArrayList<>();
	public static boolean updateUI = false;
	static File worldDir = null;

	public static void registerGroup(LootGroup group) {
		if(group == null || lootGroups.contains(group)) {
			return;
		}
		lootGroups.add(group);
	}

	public static LootGroup getWeightedGroup(float weight, Random rand) {
		int total = getTotalWeight();
		if(total <= 0) {
			BetterQuesting.logger.warn("Unable to get random loot group! Reason: No registered groups/weights");
			return null;
		}
		float r = rand.nextFloat() * total / 4F + weight * total * 0.75F;
		int cnt = 0;
		ArrayList<LootGroup> sorted = new ArrayList<>(lootGroups);
		Collections.sort(sorted);
		for(LootGroup entry : sorted) {
			cnt += entry.weight;
			if(cnt >= r) {
				return entry;
			}
		}
		BetterQuesting.logger.warn("Unable to get random loot group! Reason: Unknown");
		return null;
	}

	public static int getTotalWeight() {
		int i = 0;
		for(LootGroup group : lootGroups) {
			i += group.weight;
		}
		return i;
	}

	public static ArrayList<BigItemStack> getStandardLoot(Random rand) {
		ArrayList<BigItemStack> stacks = new ArrayList<>();
		int i = 1 + rand.nextInt(7);
		while(i > 0) {
			stacks.add(new BigItemStack(ChestGenHooks.getOneItem(ChestGenHooks.DUNGEON_CHEST, rand)));
			i--;
		}
		return stacks;
	}

	public static void updateClients() {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json = new JsonObject();
		LootRegistry.writeToJson(json);
		tags.setTag("Database", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		PacketSender.sendToAll(new QuestingPacket(PacketTypeNative.LOOT_SYNC.GetLocation(), tags));
	}

	public static void sendDatabase(EntityPlayerMP player) {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json = new JsonObject();
		LootRegistry.writeToJson(json);
		tags.setTag("Database", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		PacketSender.sendToPlayer(new QuestingPacket(PacketTypeNative.LOOT_SYNC.GetLocation(), tags), player);
	}

	public static void writeToJson(JsonObject json) {
		JsonArray jRew = new JsonArray();
		for(LootGroup entry : lootGroups) {
			JsonObject jGrp = new JsonObject();
			entry.writeToJson(jGrp);
			jRew.add(jGrp);
		}
		json.add("groups", jRew);
	}

	public static void readFromJson(JsonObject json) {
		lootGroups.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "groups")) {
			if(entry == null || !entry.isJsonObject()) {
				continue;
			}
			LootGroup group = new LootGroup();
			group.readFromJson(entry.getAsJsonObject());
			lootGroups.add(group);
		}
		updateUI = true;
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if(event.world.isRemote || worldDir != null) {
			return;
		}
		MinecraftServer server = MinecraftServer.getServer();
		if(server.isSinglePlayer()) {
			worldDir = server.getFile("saves/" + server.getFolderName());
		} else {
			worldDir = server.getFile(server.getFolderName());
		}
		File f1 = new File(worldDir, "QuestLoot.json");
		JsonObject j1 = new JsonObject();
		if(f1.exists()) {
			j1 = JsonHelper.ReadFromFile(f1);
		} else {
			f1 = server.getFile("config/betterquesting/DefaultLoot.json");
			if(f1.exists()) {
				j1 = JsonHelper.ReadFromFile(f1);
			}
		}
		readFromJson(j1);
	}

	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {
		if(!event.world.isRemote && worldDir != null && event.world.provider.dimensionId == 0) {
			JsonObject jsonQ = new JsonObject();
			writeToJson(jsonQ);
			JsonHelper.WriteToFile(new File(worldDir, "QuestLoot.json"), jsonQ);
		}
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if(!event.world.isRemote && !MinecraftServer.getServer().isServerRunning()) {
			worldDir = null;
		}
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if(!event.player.worldObj.isRemote && event.player instanceof EntityPlayerMP) {
			sendDatabase((EntityPlayerMP) event.player);
		}
	}
}