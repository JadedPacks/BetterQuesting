package betterquesting.commands;

import betterquesting.network.PacketSender;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
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
		return "[add|set] <value> [username|uuid]";
	}

	@Override
	public boolean validArgs(String[] args) {
		return args.length == 4 || args.length == 3;
	}

	@Override
	public List<String> autoComplete(String[] args) {
		ArrayList<String> list = new ArrayList<>();
		if(args.length == 4) {
			return getListOfStringsMatchingLastWord(args, NameCache.getAllNames().toArray(new String[0]));
		} else if(args.length == 2) {
			return getListOfStringsMatchingLastWord(args, "add", "set");
		}
		return list;
	}

	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args) {
		String action = args[1];
		int value;
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
		String pName = playerID == null ? "NULL" : NameCache.getName(playerID);
		if(action.equalsIgnoreCase("set")) {
			value = Math.max(1, value);
			if(playerID != null) {
				LifeDatabase.setLives(playerID, value);
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.set_player", pName, value));
			} else {
				for(Object p : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
					LifeDatabase.setLives(NameCache.getQuestingUUID((EntityPlayer) p), value);
				}
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.set_all", value));
			}
			PacketSender.sendToAll(LifeDatabase.getSyncPacket());
		} else if(action.equalsIgnoreCase("add")) {
			if(playerID != null) {
				int lives = LifeDatabase.getLives(playerID);
				LifeDatabase.setLives(playerID, lives + value);
				lives = LifeDatabase.getLives(playerID);
				if(value >= 0) {
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.add_player", value, pName, lives));
				} else {
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.remove_player", Math.abs(value), pName, lives));
				}
			} else {
				for(Object p : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
					EntityPlayer pl = (EntityPlayer) p;
					int lives = LifeDatabase.getLives(NameCache.getQuestingUUID(pl));
					LifeDatabase.setLives(NameCache.getQuestingUUID(pl), lives + value);
				}
				if(value >= 0) {
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.add_all", value));
				} else {
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.remove_all", Math.abs(value)));
				}
			}
			PacketSender.sendToAll(LifeDatabase.getSyncPacket());
		} else {
			throw getException(command);
		}
	}

	@Override
	public boolean isArgUsername(int index) {
		return index == 3;
	}
}