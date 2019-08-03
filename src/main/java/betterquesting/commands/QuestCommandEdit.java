package betterquesting.commands;

import betterquesting.network.PacketSender;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class QuestCommandEdit extends QuestCommandBase {
	@Override
	public String getCommand() {
		return "edit";
	}

	@Override
	public String getUsageSuffix() {
		return "[true|false]";
	}

	@Override
	public boolean validArgs(String[] args) {
		return args.length == 1 || args.length == 2;
	}

	@Override
	public List<String> autoComplete(String[] args) {
		ArrayList<String> list = new ArrayList<>();
		if(args.length == 2) {
			return getListOfStringsMatchingLastWord(args, "true", "false");
		}
		return list;
	}

	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args) {
		boolean flag = !QuestSettings.edit;
		if(args.length == 2) {
			try {
				if(args[1].equalsIgnoreCase("on")) {
					flag = true;
				} else if(args[1].equalsIgnoreCase("off")) {
					flag = false;
				} else {
					flag = Boolean.parseBoolean(args[1]);
				}
			} catch(Exception e) {
				throw this.getException(command);
			}
		}
		if(flag) {
			NameCache.updateNames(MinecraftServer.getServer());
		}
		QuestSettings.edit = flag;
		sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.edit", new ChatComponentTranslation(flag ? "options.on" : "options.off")));
		PacketSender.sendToAll(QuestSettings.getSyncPacket());
	}
}