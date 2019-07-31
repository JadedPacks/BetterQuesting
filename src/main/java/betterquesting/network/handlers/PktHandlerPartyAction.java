package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyInstance;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.NameCache;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class PktHandlerPartyAction implements IPacketHandler {
	@Override
	public ResourceLocation getRegistryName() {
		return PacketTypeNative.PARTY_EDIT.GetLocation();
	}

	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender) {
		if(sender == null) {
			return;
		}
		boolean isOp = MinecraftServer.getServer().getConfigurationManager().canSendCommands(sender.getGameProfile());
		int aID = !data.hasKey("action") ? -1 : data.getInteger("action");
		if(aID < 0 || aID >= EnumPacketAction.values().length) {
			return;
		}
		EnumPacketAction action = EnumPacketAction.values()[aID];
		int partyID = !data.hasKey("partyID") ? -1 : data.getInteger("partyID");
		UUID tarUser;
		IParty tarParty;
		EnumPartyStatus status = null;
		UUID senderID = QuestingAPI.getQuestingUUID(sender);
		if(isOp) {
			tarParty = PartyManager.INSTANCE.getValue(partyID);
			status = EnumPartyStatus.OWNER;
		} else {
			if(action == EnumPacketAction.JOIN) {
				tarParty = PartyManager.INSTANCE.getValue(partyID);
			} else {
				tarParty = PartyManager.INSTANCE.getUserParty(senderID);
			}
			if(tarParty != null) {
				status = tarParty.getStatus(senderID);
			}
		}
		try {
			tarUser = UUID.fromString(data.getString("target"));
		} catch(Exception e) {
			tarUser = NameCache.INSTANCE.getUUID(data.getString("target"));
		}
		if(action == EnumPacketAction.ADD && tarParty == null) {
			String name = data.getString("name");
			name = name.length() > 0 ? name : "New Party";
			IParty nParty = new PartyInstance();
			nParty.getProperties().setProperty(NativeProps.NAME, name);
			nParty.inviteUser(senderID);
			PartyManager.INSTANCE.add(nParty, PartyManager.INSTANCE.nextKey());
			PacketSender.INSTANCE.sendToAll(PartyManager.INSTANCE.getSyncPacket());
		} else if(action == EnumPacketAction.REMOVE && tarParty != null && status == EnumPartyStatus.OWNER) {
			PartyManager.INSTANCE.removeKey(partyID);
			PacketSender.INSTANCE.sendToAll(PartyManager.INSTANCE.getSyncPacket());
		} else if(action == EnumPacketAction.KICK && tarUser != null && tarParty != null && status != null && (status.ordinal() >= 2 || tarUser == senderID)) {
			tarParty.kickUser(tarUser);
			PacketSender.INSTANCE.sendToAll(tarParty.getSyncPacket());
		} else if(action == EnumPacketAction.EDIT && tarParty != null && status == EnumPartyStatus.OWNER) {
			tarParty.readPacket(data);
			PacketSender.INSTANCE.sendToAll(tarParty.getSyncPacket());
		} else if(action == EnumPacketAction.JOIN && tarParty != null && (isOp || status == EnumPartyStatus.INVITE)) {
			if(isOp) {
				tarParty.inviteUser(senderID);
			}
			tarParty.setStatus(senderID, EnumPartyStatus.MEMBER);
			PacketSender.INSTANCE.sendToAll(tarParty.getSyncPacket());
		} else if(action == EnumPacketAction.INVITE && tarParty != null && tarUser != null && status.ordinal() >= 2) {
			tarParty.inviteUser(tarUser);
			PacketSender.INSTANCE.sendToAll(tarParty.getSyncPacket());
		}
	}

	@Override
	public void handleClient(NBTTagCompound data) {}
}