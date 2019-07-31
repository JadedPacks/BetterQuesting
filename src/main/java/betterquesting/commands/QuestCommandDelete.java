package betterquesting.commands;

import betterquesting.api.questing.IQuest;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class QuestCommandDelete extends QuestCommandBase {
	@Override
	public String getUsageSuffix() {
		return "[all|<quest_id>]";
	}

	@Override
	public boolean validArgs(String[] args) {
		return args.length == 2;
	}

	@Override
	public List<String> autoComplete(ICommandSender sender, String[] args) {
		ArrayList<String> list = new ArrayList<>();
		if(args.length == 2) {
			list.add("all");
			for(int i : QuestDatabase.INSTANCE.getAllKeys()) {
				list.add("" + i);
			}
		}
		return list;
	}

	@Override
	public String getCommand() {
		return "delete";
	}

	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args) {
		if(args[1].equalsIgnoreCase("all")) {
			QuestDatabase.INSTANCE.reset();
			QuestLineDatabase.INSTANCE.reset();
			PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
			PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
			sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.delete.all"));
		} else {
			try {
				int id = Integer.parseInt(args[1].trim());
				IQuest quest = QuestDatabase.INSTANCE.getValue(id);
				QuestDatabase.INSTANCE.removeKey(id);
				PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.delete.single", new ChatComponentTranslation(quest.getUnlocalisedName())));
			} catch(Exception e) {
				throw getException(command);
			}
		}
	}
}