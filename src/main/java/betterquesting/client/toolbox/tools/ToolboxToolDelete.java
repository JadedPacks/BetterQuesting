package betterquesting.client.toolbox.tools;

import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import net.minecraft.nbt.NBTTagCompound;

public class ToolboxToolDelete implements IToolboxTool {
	IGuiQuestLine gui;

	@Override
	public void initTool(IGuiQuestLine gui) {
		this.gui = gui;
	}

	@Override
	public void disableTool() {}

	@Override
	public void drawTool(int mx, int my) {}

	@Override
	public void onMouseClick(int mx, int my, int click) {
		if(click != 0) {
			return;
		}
		GuiButtonQuestInstance btn = gui.getQuestLine().getButtonAt(mx, my);
		if(btn != null) {
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", EnumPacketAction.REMOVE.ordinal());
			tags.setInteger("questID", QuestDatabase.getKey(btn.getQuest()));
			PacketSender.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
		}
	}

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