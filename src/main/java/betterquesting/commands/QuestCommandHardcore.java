package betterquesting.commands;

import betterquesting.network.PacketSender;
import betterquesting.storage.QuestSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class QuestCommandHardcore extends QuestCommandBase {
	@Override
	public String getCommand() {
		return "hardcore";
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
		boolean flag = !QuestSettings.hardcore;
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
		QuestSettings.hardcore = flag;
		sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.hardcore", new ChatComponentTranslation(flag ? "options.on" : "options.off")));
		PacketSender.sendToAll(QuestSettings.getSyncPacket());
	}
}