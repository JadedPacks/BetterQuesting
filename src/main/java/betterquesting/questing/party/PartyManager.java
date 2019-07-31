package betterquesting.questing.party;

import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.party.IPartyDatabase;
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

public final class PartyManager implements IPartyDatabase {
	public static final PartyManager INSTANCE = new PartyManager();
	private final ConcurrentHashMap<Integer, IParty> partyList = new ConcurrentHashMap<>();

	@Override
	public IParty getUserParty(UUID uuid) {
		for(IParty p : getAllValues()) {
			EnumPartyStatus status = p.getStatus(uuid);
			if(status != null && status != EnumPartyStatus.INVITE) {
				return p;
			}
		}
		return null;
	}

	@Override
	public List<Integer> getPartyInvites(UUID uuid) {
		ArrayList<Integer> invites = new ArrayList<>();
		boolean isOp = NameCache.INSTANCE.isOP(uuid);
		for(Entry<Integer, IParty> entry : partyList.entrySet()) {
			if(isOp || entry.getValue().getStatus(uuid) == EnumPartyStatus.INVITE) {
				invites.add(entry.getKey());
			}
		}
		return invites;
	}

	@Override
	public Integer nextKey() {
		int i = 0;
		while(partyList.containsKey(i)) {
			i++;
		}
		return i;
	}

	@Override
	public void add(IParty party, Integer id) {
		if(party == null || id < 0 || partyList.containsKey(id) || partyList.containsValue(party)) {
			return;
		}
		partyList.put(id, party);
	}

	@Override
	public boolean removeKey(Integer id) {
		return partyList.remove(id) != null;
	}

	@Override
	public boolean removeValue(IParty party) {
		return removeKey(getKey(party));
	}

	@Override
	public IParty getValue(Integer id) {
		return partyList.get(id);
	}

	@Override
	public Integer getKey(IParty party) {
		for(Entry<Integer, IParty> entry : partyList.entrySet()) {
			if(entry.getValue() == party) {
				return entry.getKey();
			}
		}
		return -1;
	}

	@Override
	public int size() {
		return partyList.size();
	}

	@Override
	public void reset() {
		partyList.clear();
	}

	@Override
	public List<IParty> getAllValues() {
		return new ArrayList<>(partyList.values());
	}

	@Override
	public List<Integer> getAllKeys() {
		return new ArrayList<>(partyList.keySet());
	}

	@Override
	public QuestingPacket getSyncPacket() {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json = new JsonObject();
		json.add("parties", writeToJson(new JsonArray(), EnumSaveType.CONFIG));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		return new QuestingPacket(PacketTypeNative.PARTY_DATABASE.GetLocation(), tags);
	}

	@Override
	public void readPacket(NBTTagCompound payload) {
		JsonObject json = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		readFromJson(JsonHelper.GetArray(json, "parties"), EnumSaveType.CONFIG);
	}

	@Override
	public JsonArray writeToJson(JsonArray json, EnumSaveType saveType) {
		if(saveType != EnumSaveType.CONFIG) {
			return json;
		}
		for(Entry<Integer, IParty> entry : partyList.entrySet()) {
			JsonObject jp = entry.getValue().writeToJson(new JsonObject(), saveType);
			jp.addProperty("partyID", entry.getKey());
			json.add(jp);
		}
		return json;
	}

	@Override
	public void readFromJson(JsonArray json, EnumSaveType saveType) {
		if(saveType != EnumSaveType.CONFIG) {
			return;
		}
		partyList.clear();
		for(JsonElement element : json) {
			if(element == null || !element.isJsonObject()) {
				continue;
			}
			JsonObject jp = element.getAsJsonObject();
			int partyID = JsonHelper.GetNumber(jp, "partyID", -1).intValue();
			if(partyID < 0) {
				continue;
			}
			IParty party = new PartyInstance();
			party.readFromJson(jp, EnumSaveType.CONFIG);
			if(party.getMembers().size() > 0) {
				partyList.put(partyID, party);
			}
		}
	}
}