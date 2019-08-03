package betterquesting.questing.party;

import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.NameCache;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PartyManager {
	private static final ConcurrentHashMap<Integer, PartyInstance> partyList = new ConcurrentHashMap<>();

	public static PartyInstance getUserParty(UUID uuid) {
		for(PartyInstance p : getAllValues()) {
			EnumPartyStatus status = p.getStatus(uuid);
			if(status != null && status != EnumPartyStatus.INVITE) {
				return p;
			}
		}
		return null;
	}

	public static List<Integer> getPartyInvites(UUID uuid) {
		ArrayList<Integer> invites = new ArrayList<>();
		boolean isOp = NameCache.isOP(uuid);
		for(Entry<Integer, PartyInstance> entry : partyList.entrySet()) {
			if(isOp || entry.getValue().getStatus(uuid) == EnumPartyStatus.INVITE) {
				invites.add(entry.getKey());
			}
		}
		return invites;
	}

	public static Integer nextKey() {
		int i = 0;
		while(partyList.containsKey(i)) {
			i++;
		}
		return i;
	}

	public static void add(PartyInstance party, Integer id) {
		if(party == null || id < 0 || partyList.containsKey(id) || partyList.containsValue(party)) {
			return;
		}
		partyList.put(id, party);
	}

	public static void removeKey(Integer id) {
		partyList.remove(id);
	}

	public static void removeValue(PartyInstance party) {
		removeKey(getKey(party));
	}

	public static PartyInstance getValue(Integer id) {
		return partyList.get(id);
	}

	public static Integer getKey(PartyInstance party) {
		for(Entry<Integer, PartyInstance> entry : partyList.entrySet()) {
			if(entry.getValue() == party) {
				return entry.getKey();
			}
		}
		return -1;
	}

	public static int size() {
		return partyList.size();
	}

	public void reset() {
		partyList.clear();
	}

	public static List<PartyInstance> getAllValues() {
		return new ArrayList<>(partyList.values());
	}

	public List<Integer> getAllKeys() {
		return new ArrayList<>(partyList.keySet());
	}

	public static QuestingPacket getSyncPacket() {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json = new JsonObject();
		json.add("parties", writeToJson(new JsonArray(), EnumSaveType.CONFIG));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		return new QuestingPacket(PacketTypeNative.PARTY_DATABASE.GetLocation(), tags);
	}

	public static void readPacket(NBTTagCompound payload) {
		JsonObject json = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		readFromJson(JsonHelper.GetArray(json, "parties"), EnumSaveType.CONFIG);
	}

	public static JsonArray writeToJson(JsonArray json, EnumSaveType saveType) {
		if(saveType != EnumSaveType.CONFIG) {
			return json;
		}
		for(Entry<Integer, PartyInstance> entry : partyList.entrySet()) {
			JsonObject jp = entry.getValue().writeToJson(new JsonObject(), saveType);
			jp.addProperty("partyID", entry.getKey());
			json.add(jp);
		}
		return json;
	}

	public static void readFromJson(JsonArray json, EnumSaveType saveType) {
		if(saveType != EnumSaveType.CONFIG) {
			return;
		}
		partyList.clear();
		for(JsonElement element : json) {
			if(element == null || !element.isJsonObject()) {
				continue;
			}
			JsonObject jp = element.getAsJsonObject();
			int partyID = JsonHelper.GetInt(jp, "partyID", -1);
			if(partyID < 0) {
				continue;
			}
			PartyInstance party = new PartyInstance();
			party.readFromJson(jp, EnumSaveType.CONFIG);
			if(party.getMembers().size() > 0) {
				partyList.put(partyID, party);
			}
		}
	}
}