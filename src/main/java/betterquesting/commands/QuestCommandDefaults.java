package betterquesting.commands;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.utils.JsonHelper;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.storage.QuestSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class QuestCommandDefaults extends QuestCommandBase {
	@Override
	public String getUsageSuffix() {
		return "[save|load]";
	}

	@Override
	public boolean validArgs(String[] args) {
		return args.length == 2 || args.length == 3;
	}

	@Override
	public List<String> autoComplete(ICommandSender sender, String[] args) {
		ArrayList<String> list = new ArrayList<>();
		if(args.length == 2) {
			return CommandBase.getListOfStringsMatchingLastWord(args, "save", "load");
		}
		return list;
	}

	@Override
	public String getCommand() {
		return "default";
	}

	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args) {
		File qFile = new File("config/betterquesting/DefaultQuests.json");
		if(args[1].equalsIgnoreCase("save")) {
			JsonObject base = new JsonObject();
			base.add("questSettings", QuestSettings.INSTANCE.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
			base.add("questDatabase", QuestDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			base.add("questLines", QuestLineDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			JsonHelper.WriteToFile(qFile, base);
			if(args.length == 3 && !args[2].equalsIgnoreCase("DefaultQuests")) {
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.default.save2", args[2] + ".json"));
			} else {
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.default.save"));
			}
		} else if(args[1].equalsIgnoreCase("load")) {
			if(qFile.exists()) {
				JsonArray jsonP = QuestDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.PROGRESS);
				JsonObject j1 = JsonHelper.ReadFromFile(qFile);
				QuestSettings.INSTANCE.readFromJson(JsonHelper.GetObject(j1, "questSettings"), EnumSaveType.CONFIG);
				QuestDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(j1, "questDatabase"), EnumSaveType.CONFIG);
				QuestLineDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(j1, "questLines"), EnumSaveType.CONFIG);
				QuestDatabase.INSTANCE.readFromJson(jsonP, EnumSaveType.PROGRESS);
				if(args.length == 3 && !args[2].equalsIgnoreCase("DefaultQuests")) {
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.default.load2", args[2] + ".json"));
				} else {
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.default.load"));
				}
				PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
				PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
			} else {
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.default.none"));
			}
		} else {
			throw getException(command);
		}
	}
}