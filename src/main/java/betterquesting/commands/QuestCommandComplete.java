package betterquesting.commands;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.NameCache;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestCommandComplete extends QuestCommandBase {
	@Override
	public String getUsageSuffix() {
		return "<quest_id> [username|uuid]";
	}

	@Override
	public boolean validArgs(String[] args) {
		return args.length == 2 || args.length == 3;
	}

	@Override
	public List<String> autoComplete(ICommandSender sender, String[] args) {
		ArrayList<String> list = new ArrayList<>();
		if(args.length == 2) {
			for(int i : QuestDatabase.INSTANCE.getAllKeys()) {
				list.add("" + i);
			}
		} else if(args.length == 3) {
			return CommandBase.getListOfStringsMatchingLastWord(args, NameCache.INSTANCE.getAllNames().toArray(new String[0]));
		}
		return list;
	}

	@Override
	public String getCommand() {
		return "complete";
	}

	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args) {
		UUID uuid;
		if(args.length >= 3) {
			uuid = this.findPlayerID(MinecraftServer.getServer(), args[2]);
			if(uuid == null) {
				throw this.getException(command);
			}
		} else {
			uuid = this.findPlayerID(MinecraftServer.getServer(), sender.getCommandSenderName());
		}
		String pName = uuid == null ? "NULL" : NameCache.INSTANCE.getName(uuid);
		try {
			int id = Integer.parseInt(args[1].trim());
			IQuest quest = QuestDatabase.INSTANCE.getValue(id);
			quest.setComplete(uuid, 0);
			int done = 0;
			if(!quest.getProperties().getProperty(NativeProps.LOGIC_TASK).getResult(done, quest.getTasks().size())) {
				for(ITask task : quest.getTasks().getAllValues()) {
					task.setComplete(uuid);
					done += 1;
					if(quest.getProperties().getProperty(NativeProps.LOGIC_TASK).getResult(done, quest.getTasks().size())) {
						break;
					}
				}
			}
			sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.complete", new ChatComponentTranslation(quest.getUnlocalisedName()), pName));
		} catch(Exception e) {
			throw getException(command);
		}
		PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
	}

	@Override
	public boolean isArgUsername(String[] args, int index) {
		return index == 2;
	}
}