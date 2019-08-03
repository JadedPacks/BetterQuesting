package betterquesting.client.toolbox.tools;

import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLine;
import betterquesting.questing.QuestLineDatabase;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

public class ToolboxToolRemove implements IToolboxTool {
	private IGuiQuestLine gui;

	@Override
	public void initTool(IGuiQuestLine gui) {
		this.gui = gui;
	}

	@Override
	public void disableTool() {}

	@Override
	public void onMouseClick(int mx, int my, int click) {
		if(click != 0) {
			return;
		}
		QuestLine line = gui.getQuestLine().getQuestLine();
		GuiButtonQuestInstance btn = gui.getQuestLine().getButtonAt(mx, my);
		if(line != null && btn != null) {
			int qID = QuestDatabase.getKey(btn.getQuest());
			line.removeKey(qID);
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", EnumPacketAction.EDIT.ordinal());
			JsonObject base = new JsonObject();
			base.add("line", line.writeToJson(new JsonObject()));
			tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
			tags.setInteger("lineID", QuestLineDatabase.getKey(line));
			PacketSender.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tags));
		}
	}

	@Override
	public void drawTool(int mx, int my) {}

	@Override
	public boolean allowTooltips() {
		return true;
	}

	@Override
	public boolean allowScrolling(int click) {
		return true;
	}

	@Override
	public boolean clampScrolling() {
		return true;
	}
}