package betterquesting.questing.party;

import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;
import java.util.Map.Entry;

public class PartyInstance {
	public String name = "New Party";
	public boolean sharedLives = false;
	private final HashMap<UUID, EnumPartyStatus> members = new HashMap<>();

	public void inviteUser(UUID uuid) {
		if(uuid == null || members.containsKey(uuid)) {
			return;
		}
		if(members.size() == 0) {
			members.put(uuid, EnumPartyStatus.OWNER);
		} else {
			members.put(uuid, EnumPartyStatus.INVITE);
		}
		PacketSender.sendToAll(getSyncPacket());
	}

	public void kickUser(UUID uuid) {
		if(!members.containsKey(uuid)) {
			return;
		}
		EnumPartyStatus old = members.get(uuid);
		members.remove(uuid);
		if(members.size() <= 0) {
			PartyManager.removeValue(this);
			PacketSender.sendToAll(PartyManager.getSyncPacket());
		} else if(old == EnumPartyStatus.OWNER) {
			hostMigrate();
		}
		PacketSender.sendToAll(getSyncPacket());
	}

	public void setStatus(UUID uuid, EnumPartyStatus priv) {
		if(!members.containsKey(uuid)) {
			return;
		}
		EnumPartyStatus old = members.get(uuid);
		if(old == priv) {
			return;
		}
		members.put(uuid, priv);
		if(priv == EnumPartyStatus.OWNER) {
			for(UUID mem : getMembers()) {
				if(mem != uuid && members.get(mem) == EnumPartyStatus.OWNER) {
					members.put(mem, EnumPartyStatus.ADMIN);
					break;
				}
			}
		} else if(old == EnumPartyStatus.OWNER) {
			UUID migrate = null;
			for(UUID mem : getMembers()) {
				if(mem == uuid) {
					continue;
				}
				if(members.get(mem) == EnumPartyStatus.ADMIN) {
					migrate = mem;
					break;
				} else if(migrate == null) {
					migrate = mem;
				}
			}
			if(migrate == null) {
				members.put(uuid, old);
				return;
			} else {
				members.put(migrate, EnumPartyStatus.OWNER);
			}
		}
		PacketSender.sendToAll(getSyncPacket());
	}

	public EnumPartyStatus getStatus(UUID uuid) {
		return members.get(uuid);
	}

	public List<UUID> getMembers() {
		return new ArrayList<>(members.keySet());
	}

	public UUID getOwner() {
		for(HashMap.Entry<UUID, EnumPartyStatus> member : members.entrySet()) {
			if(member.getValue() == EnumPartyStatus.OWNER) {
				return member.getKey();
			}
		}
		return null;
	}

	private void hostMigrate() {
		List<UUID> tmp = getMembers();
		for(UUID uuid : tmp) {
			if(members.get(uuid) == EnumPartyStatus.OWNER) {
				return;
			}
		}
		UUID migrate = null;
		for(UUID mem : getMembers()) {
			if(members.get(mem) == EnumPartyStatus.ADMIN) {
				migrate = mem;
				break;
			} else if(migrate == null) {
				migrate = mem;
			}
		}
		if(migrate != null) {
			members.put(migrate, EnumPartyStatus.OWNER);
		}
	}

	public QuestingPacket getSyncPacket() {
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("party", writeToJson(new JsonObject()));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		tags.setInteger("partyID", PartyManager.getKey(this));
		return new QuestingPacket(PacketTypeNative.PARTY_SYNC.GetLocation(), tags);
	}

	public void readPacket(NBTTagCompound payload) {
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		readFromJson(JsonHelper.GetObject(base, "party"));
	}

	public JsonObject writeToJson(JsonObject json) {
		JsonArray memJson = new JsonArray();
		for(Entry<UUID, EnumPartyStatus> mem : members.entrySet()) {
			JsonObject jm = new JsonObject();
			jm.addProperty("uuid", mem.getKey().toString());
			jm.addProperty("status", mem.getValue().toString());
			memJson.add(jm);
		}
		json.add("members", memJson);
		JsonObject jObj = new JsonObject();
		jObj.addProperty("name", name);
		jObj.addProperty("sharedLives", sharedLives);
		json.add("properties", jObj);
		return json;
	}

	public void readFromJson(JsonObject json) {
		JsonObject jObj = JsonHelper.GetObject(json, "properties");
		name = JsonHelper.GetString(jObj, "name", "New Party");
		sharedLives = JsonHelper.GetBoolean(jObj, "sharedLives", false);
		members.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "members")) {
			if(entry == null || !entry.isJsonObject()) {
				continue;
			}
			JsonObject jMem = entry.getAsJsonObject();
			UUID uuid;
			EnumPartyStatus priv;
			try {
				uuid = UUID.fromString(JsonHelper.GetString(jMem, "uuid", ""));
			} catch(Exception e) {
				uuid = null;
			}
			try {
				priv = EnumPartyStatus.valueOf(JsonHelper.GetString(jMem, "status", EnumPartyStatus.INVITE.toString()));
			} catch(Exception e) {
				priv = EnumPartyStatus.INVITE;
			}
			if(uuid != null) {
				members.put(uuid, priv);
			}
		}
	}
}