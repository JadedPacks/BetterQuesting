package betterquesting.commands;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.PacketSender;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestCommandLives extends QuestCommandBase {
	@Override
	public String getCommand() {
		return "lives";
	}

	@Override
	public String getUsageSuffix() {
		return "[add|set|max|default] <value> [username|uuid]";
	}

	@Override
	public boolean validArgs(String[] args) {
		return args.length == 4 || args.length == 3;
	}

	@Override
	public List<String> autoComplete(ICommandSender sender, String[] args) {
		ArrayList<String> list = new ArrayList<>();
		if(args.length == 4 && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("set"))) {
			return CommandBase.getListOfStringsMatchingLastWord(args, NameCache.INSTANCE.getAllNames().toArray(new String[0]));
		} else if(args.length == 2) {
			return CommandBase.getListOfStringsMatchingLastWord(args, "add", "set", "max", "default");
		}
		return list;
	}

	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args) {
		String action = args[1];
		int value = 0;
		UUID playerID = null;
		try {
			value = Integer.parseInt(args[2]);
		} catch(Exception e) {
			throw getException(command);
		}
		if(args.length >= 4) {
			playerID = this.findPlayerID(MinecraftServer.getServer(), args[3]);

			if(playerID == null) {
				throw getException(command);
			}
		}
		String pName = playerID == null ? "NULL" : NameCache.INSTANCE.getName(playerID);
		if(action.equalsIgnoreCase("set")) {
			value = Math.max(1, value);
			if(playerID != null) {
				LifeDatabase.INSTANCE.setLives(playerID, value);
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.set_player", pName, value));
			} else if(args.length == 3) {
				for(EntityPlayer p : (List<EntityPlayer>) MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
					LifeDatabase.INSTANCE.setLives(QuestingAPI.getQuestingUUID(p), value);
				}
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.set_all", value));
			}
			PacketSender.INSTANCE.sendToAll(LifeDatabase.INSTANCE.getSyncPacket());
		} else if(action.equalsIgnoreCase("add")) {
			if(playerID != null) {
				int lives = LifeDatabase.INSTANCE.getLives(playerID);
				LifeDatabase.INSTANCE.setLives(playerID, lives + value);
				lives = LifeDatabase.INSTANCE.getLives(playerID);
				if(value >= 0) {
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.add_player", value, pName, lives));
				} else {
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.remove_player", Math.abs(value), pName, lives));
				}
			} else {
				for(EntityPlayer p : (List<EntityPlayer>) MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
					int lives = LifeDatabase.INSTANCE.getLives(QuestingAPI.getQuestingUUID(p));
					LifeDatabase.INSTANCE.setLives(QuestingAPI.getQuestingUUID(p), lives + value);
				}
				if(value >= 0) {
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.add_all", value));
				} else {
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.remove_all", Math.abs(value)));
				}
			}
			PacketSender.INSTANCE.sendToAll(LifeDatabase.INSTANCE.getSyncPacket());
		} else if(action.equalsIgnoreCase("max")) {
			value = Math.max(1, value);
			QuestSettings.INSTANCE.setProperty(NativeProps.LIVES_MAX, value);
			sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.max", value));
			PacketSender.INSTANCE.sendToAll(LifeDatabase.INSTANCE.getSyncPacket());
		} else if(action.equalsIgnoreCase("default")) {
			value = Math.max(1, value);
			QuestSettings.INSTANCE.setProperty(NativeProps.LIVES_DEF, value);
			sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.default" + value));
			PacketSender.INSTANCE.sendToAll(LifeDatabase.INSTANCE.getSyncPacket());
		} else {
			throw getException(command);
		}
	}

	@Override
	public boolean isArgUsername(String[] args, int index) {
		return index == 3;
	}
}