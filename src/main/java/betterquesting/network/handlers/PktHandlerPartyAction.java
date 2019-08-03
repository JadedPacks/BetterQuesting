package betterquesting.network.handlers;

import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.network.IPacketHandler;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyInstance;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.NameCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
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
		PartyInstance tarParty;
		EnumPartyStatus status = null;
		UUID senderID = NameCache.getQuestingUUID(sender);
		if(isOp) {
			tarParty = PartyManager.getValue(partyID);
			status = EnumPartyStatus.OWNER;
		} else {
			if(action == EnumPacketAction.JOIN) {
				tarParty = PartyManager.getValue(partyID);
			} else {
				tarParty = PartyManager.getUserParty(senderID);
			}
			if(tarParty != null) {
				status = tarParty.getStatus(senderID);
			}
		}
		try {
			tarUser = UUID.fromString(data.getString("target"));
		} catch(Exception e) {
			tarUser = NameCache.getUUID(data.getString("target"));
		}
		if(action == EnumPacketAction.ADD && tarParty == null) {
			String name = data.getString("name");
			name = name.length() > 0 ? name : "New Party";
			PartyInstance nParty = new PartyInstance();
			nParty.name = name;
			nParty.inviteUser(senderID);
			PartyManager.add(nParty, PartyManager.nextKey());
			PacketSender.sendToAll(PartyManager.getSyncPacket());
		} else if(action == EnumPacketAction.REMOVE && tarParty != null && status == EnumPartyStatus.OWNER) {
			PartyManager.removeKey(partyID);
			PacketSender.sendToAll(PartyManager.getSyncPacket());
		} else if(action == EnumPacketAction.KICK && tarUser != null && tarParty != null && status != null && (status.ordinal() >= 2 || tarUser == senderID)) {
			tarParty.kickUser(tarUser);
			PacketSender.sendToAll(tarParty.getSyncPacket());
		} else if(action == EnumPacketAction.EDIT && tarParty != null && status == EnumPartyStatus.OWNER) {
			tarParty.readPacket(data);
			PacketSender.sendToAll(tarParty.getSyncPacket());
		} else if(action == EnumPacketAction.JOIN && tarParty != null && (isOp || status == EnumPartyStatus.INVITE)) {
			if(isOp) {
				tarParty.inviteUser(senderID);
			}
			tarParty.setStatus(senderID, EnumPartyStatus.MEMBER);
			PacketSender.sendToAll(tarParty.getSyncPacket());
		} else if(action == EnumPacketAction.INVITE && tarParty != null && tarUser != null && status.ordinal() >= 2) {
			tarParty.inviteUser(tarUser);
			PacketSender.sendToAll(tarParty.getSyncPacket());
		}
	}

	@Override
	public void handleClient(NBTTagCompound data) {
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if(screen instanceof INeedsRefresh) {
			((INeedsRefresh) screen).refreshGui();
		}
	}
}