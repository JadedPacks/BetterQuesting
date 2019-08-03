package betterquesting.client.toolbox;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.ICallback;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.NBTConverter;
import betterquesting.client.gui.editors.json.GuiJsonItemSelection;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;

public class GuiToolIconProxy extends GuiScreenThemed implements ICallback<BigItemStack> {
	private final QuestInstance quest;
	boolean flag = false;

	public GuiToolIconProxy(GuiScreen parent, QuestInstance quest) {
		super(parent, "");
		this.quest = quest;
	}

	@Override
	public void initGui() {
		if(flag) {
			this.mc.displayGuiScreen(parent);
		} else {
			flag = true;
			mc.displayGuiScreen(new GuiJsonItemSelection(this, this, quest.icon));
		}
	}

	public void SendChanges() {
		JsonObject base = new JsonObject();
		base.add("config", quest.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		base.add("progress", quest.writeToJson(new JsonObject(), EnumSaveType.PROGRESS));
		NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal());
		tags.setInteger("questID", QuestDatabase.getKey(quest));
		PacketSender.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
	}

	@Override
	public void setValue(BigItemStack value) {
		quest.icon = value != null ? value : new BigItemStack(Items.nether_star);
		SendChanges();
	}
}