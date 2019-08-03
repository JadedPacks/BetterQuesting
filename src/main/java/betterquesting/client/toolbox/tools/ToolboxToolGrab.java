package betterquesting.client.toolbox.tools;

import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.NBTConverter;
import betterquesting.client.toolbox.ToolboxGuiMain;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLine;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.QuestLineEntry;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

public class ToolboxToolGrab extends GuiElement implements IToolboxTool {
	IGuiQuestLine gui;
	int grabID = -1;
	GuiButtonQuestInstance grabbed;

	@Override
	public void initTool(IGuiQuestLine gui) {
		this.gui = gui;
		grabbed = null;
		grabID = -1;
	}

	@Override
	public void disableTool() {
		if(grabbed != null) {
			QuestLineEntry qle = gui.getQuestLine().getQuestLine().getValue(grabID);
			if(qle != null) {
				grabbed.xPosition = qle.getPosX();
				grabbed.yPosition = qle.getPosY();
			}
		}
		grabbed = null;
		grabID = -1;
	}

	@Override
	public void drawTool(int mx, int my) {
		if(grabbed != null) {
			int snap = ToolboxGuiMain.getSnapValue();
			grabbed.xPosition = mx;
			grabbed.yPosition = my;
			int modX = ((grabbed.xPosition % snap) + snap) % snap;
			int modY = ((grabbed.yPosition % snap) + snap) % snap;
			grabbed.xPosition -= modX;
			grabbed.yPosition -= modY;
		}
		ToolboxGuiMain.drawGrid(gui);
	}

	@Override
	public void onMouseClick(int mx, int my, int click) {
		if(click == 1 && grabbed != null) {
			QuestLineEntry qle = gui.getQuestLine().getQuestLine().getValue(grabID);
			if(qle != null) {
				grabbed.xPosition = qle.getPosX();
				grabbed.yPosition = qle.getPosY();
			}
			grabbed = null;
			return;
		} else if(click != 0) {
			return;
		}
		if(grabbed == null) {
			grabbed = gui.getQuestLine().getButtonAt(mx, my);
			grabID = grabbed == null ? -1 : QuestDatabase.getKey(grabbed.getQuest());
		} else {
			QuestLine qLine = gui.getQuestLine().getQuestLine();
			int lID = QuestLineDatabase.getKey(qLine);
			QuestLineEntry qle = gui.getQuestLine().getQuestLine().getValue(grabID);
			if(qle != null) {
				qle.setPosition(grabbed.xPosition, grabbed.yPosition);
				NBTTagCompound tag2 = new NBTTagCompound();
				JsonObject base2 = new JsonObject();
				base2.add("line", qLine.writeToJson(new JsonObject()));
				tag2.setTag("data", NBTConverter.JSONtoNBT_Object(base2, new NBTTagCompound()));
				tag2.setInteger("action", EnumPacketAction.EDIT.ordinal());
				tag2.setInteger("lineID", lID);
				PacketSender.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tag2));
			}
			grabbed = null;
			grabID = -1;
		}
	}

	@Override
	public boolean allowTooltips() {
		return grabbed == null;
	}

	@Override
	public boolean allowScrolling(int click) {
		return grabbed == null || click == 2;
	}

	@Override
	public boolean clampScrolling() {
		return false;
	}
}