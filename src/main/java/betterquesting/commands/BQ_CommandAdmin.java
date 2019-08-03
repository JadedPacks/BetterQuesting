package betterquesting.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

import java.util.ArrayList;
import java.util.List;

public class BQ_CommandAdmin extends CommandBase {
	final ArrayList<QuestCommandBase> coms = new ArrayList<>();

	public BQ_CommandAdmin() {
		coms.add(new QuestCommandEdit());
		coms.add(new QuestCommandHardcore());
		coms.add(new QuestCommandReset());
		coms.add(new QuestCommandComplete());
		coms.add(new QuestCommandLives());
	}

	@Override
	public String getCommandName() {
		return "bq_admin";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		StringBuilder txt = new StringBuilder();
		for(int i = 0; i < coms.size(); i++) {
			QuestCommandBase c = coms.get(i);
			txt.append("/bq_admin ").append(c.getCommand());
			if(c.getUsageSuffix().length() > 0) {
				txt.append(" ").append(c.getUsageSuffix());
			}
			if(i < coms.size() - 1) {
				txt.append(", ");
			}
		}
		return txt.toString();
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] strings) {
		if(strings.length == 1) {
			ArrayList<String> base = new ArrayList<>();
			for(QuestCommandBase c : coms) {
				base.add(c.getCommand());
			}
			return getListOfStringsMatchingLastWord(strings, base.toArray(new String[0]));
		} else if(strings.length > 1) {
			for(QuestCommandBase c : coms) {
				if(c.getCommand().equalsIgnoreCase(strings[0])) {
					return c.autoComplete(strings);
				}
			}
		}
		return new ArrayList<>();
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(args.length < 1) {
			throw new WrongUsageException(this.getCommandUsage(sender));
		}
		for(QuestCommandBase c : coms) {
			if(c.getCommand().equalsIgnoreCase(args[0])) {
				if(c.validArgs(args)) {
					c.runCommand(this, sender, args);
					return;
				} else {
					throw c.getException(this);
				}
			}
		}
		throw new WrongUsageException(this.getCommandUsage(sender));
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		if(args.length < 1) {
			return false;
		}
		for(QuestCommandBase c : coms) {
			if(c.getCommand().equalsIgnoreCase(args[0])) {
				return c.isArgUsername(index);
			}
		}
		return false;
	}
}