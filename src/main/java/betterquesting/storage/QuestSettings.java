package betterquesting.storage;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.PacketTypeNative;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class QuestSettings {
	public static boolean edit = false, hardcore = true;
	public static int livesDef = 3, livesMax = 10;

	public static QuestingPacket getSyncPacket() {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("settings", writeToJson(new JsonObject()));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		return new QuestingPacket(PacketTypeNative.SETTINGS.GetLocation(), tags);
	}

	public static void readPacket(NBTTagCompound payload) {
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		readFromJson(JsonHelper.GetObject(base, "settings"));
	}

	public static void readFromJson(JsonObject json) {
		edit = JsonHelper.GetBoolean(json, "editMode", false);
		hardcore = JsonHelper.GetBoolean(json, "hardcore", true);
		livesDef = JsonHelper.GetInt(json, "livesDef", 3);
		livesMax = JsonHelper.GetInt(json, "livesMax", 10);
	}

	public static JsonObject writeToJson(JsonObject json) {
		json.addProperty("editMode", edit);
		json.addProperty("hardcore", hardcore);
		json.addProperty("livesDef", livesDef);
		json.addProperty("livesMax", livesMax);
		return json;
	}

	public static boolean canUserEdit(EntityPlayer player) {
		if(player == null) {
			return false;
		}
		return edit && NameCache.isOP(NameCache.getQuestingUUID(player));
	}

	public static void reset() {
		readFromJson(new JsonObject());
	}
}