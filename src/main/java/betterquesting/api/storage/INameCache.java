package betterquesting.api.storage;

import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.IJsonSaveLoad;
import com.google.gson.JsonArray;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.UUID;

public interface INameCache extends IJsonSaveLoad<JsonArray>, IDataSync {
	String getName(UUID uuid);
	UUID getUUID(String name);
	List<String> getAllNames();
	boolean isOP(UUID uuid);
	void updateNames(MinecraftServer server);
	int size();
}