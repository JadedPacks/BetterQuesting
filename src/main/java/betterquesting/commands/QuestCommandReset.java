package betterquesting.commands;

import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.storage.NameCache;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestCommandReset extends QuestCommandBase {
	@Override
	public String getUsageSuffix() {
		return "[all|<quest_id>] [username|uuid]";
	}

	@Override
	public boolean validArgs(String[] args) {
		return args.length == 2 || args.length == 3;
	}

	@Override
	public List<String> autoComplete(String[] args) {
		ArrayList<String> list = new ArrayList<>();
		if(args.length == 2) {
			list.add("all");
			for(int i : QuestDatabase.getAllKeys()) {
				list.add("" + i);
			}
		} else if(args.length == 3) {
			return getListOfStringsMatchingLastWord(args, NameCache.getAllNames().toArray(new String[0]));
		}
		return list;
	}

	@Override
	public String getCommand() {
		return "reset";
	}

	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args) {
		String action = args[1];
		UUID uuid = null;
		if(args.length == 3) {
			uuid = this.findPlayerID(MinecraftServer.getServer(), args[2]);
			if(uuid == null) {
				throw this.getException(command);
			}
		}
		String pName = uuid == null ? "NULL" : NameCache.getName(uuid);
		if(action.equalsIgnoreCase("all")) {
			for(QuestInstance quest : QuestDatabase.getAllValues()) {
				if(uuid != null) {
					quest.resetUser(uuid, true);
				} else {
					quest.resetAll(true);
				}
			}
			if(uuid != null) {
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.player_all", pName));
			} else {
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.all_all"));
			}
		} else {
			try {
				int id = Integer.parseInt(action.trim());
				QuestInstance quest = QuestDatabase.getValue(id);
				if(uuid != null) {
					quest.resetUser(uuid, true);
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.player_single", new ChatComponentTranslation(quest.name), pName));
				} else {
					quest.resetAll(true);
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.all_single", new ChatComponentTranslation(quest.name)));
				}
			} catch(Exception e) {
				throw getException(command);
			}
		}
		PacketSender.sendToAll(QuestDatabase.getSyncPacket());
	}

	@Override
	public boolean isArgUsername(int index) {
		return index == 2;
	}
}