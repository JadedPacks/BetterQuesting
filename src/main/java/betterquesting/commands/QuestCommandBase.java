package betterquesting.commands;

import betterquesting.api.api.QuestingAPI;
import betterquesting.storage.NameCache;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class QuestCommandBase {
	public abstract String getCommand();

	public String getUsageSuffix() {
		return "";
	}

	public boolean validArgs(String[] args) {
		return args.length == 1;
	}

	public List<String> autoComplete(ICommandSender sender, String[] args) {
		return new ArrayList<>();
	}

	public abstract void runCommand(CommandBase command, ICommandSender sender, String[] args);

	public final WrongUsageException getException(CommandBase command) {
		String message = command.getCommandName() + " " + getCommand();
		if(getUsageSuffix().length() > 0) {
			message += " " + getUsageSuffix();
		}
		return new WrongUsageException(message);
	}

	protected UUID findPlayerID(MinecraftServer server, String name) {
		UUID playerID;
		EntityPlayerMP player = server.getConfigurationManager().getPlayerByUsername(name);
		if(player == null) {
			try {
				playerID = UUID.fromString(name);
			} catch(Exception e) {
				playerID = NameCache.INSTANCE.getUUID(name);
			}
		} else {
			playerID = QuestingAPI.getQuestingUUID(player);
		}
		return playerID;
	}

	public boolean isArgUsername(String[] args, int index) {
		return false;
	}
}