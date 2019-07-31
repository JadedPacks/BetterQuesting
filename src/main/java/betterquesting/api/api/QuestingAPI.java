package betterquesting.api.api;

import net.minecraft.entity.player.EntityPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.UUID;

public class QuestingAPI {
	private static final HashMap<ApiKey, Object> apis = new HashMap<>();

	public static <T> T getAPI(ApiKey<T> key) {
		Object obj = apis.get(key);
		if(obj == null) {
			return null;
		} else {
			return (T) obj;
		}
	}

	public static UUID getQuestingUUID(EntityPlayer player) {
		if(player == null) {
			return null;
		}
		if(player.worldObj.isRemote) {
			UUID uuid = getAPI(ApiReference.NAME_CACHE).getUUID(player.getGameProfile().getName());
			if(uuid != null) {
				return uuid;
			}
		}
		return player.getGameProfile().getId();
	}

	private static Logger logger = null;

	public static Logger getLogger() {
		if(logger == null) {
			logger = LogManager.getLogger("betterquesting");
		}

		return logger;
	}
}