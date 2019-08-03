package betterquesting.client.toolbox.tools;

import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.RenderUtils;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

import java.awt.*;

public class ToolboxToolLink extends GuiElement implements IToolboxTool {
	IGuiQuestLine gui;
	GuiButtonQuestInstance b1;

	@Override
	public void initTool(IGuiQuestLine gui) {
		this.gui = gui;
		b1 = null;
	}

	@Override
	public void disableTool() {
		b1 = null;
	}

	@Override
	public void drawTool(int mx, int my) {
		if(b1 == null) {
			return;
		}
		RenderUtils.DrawLine(b1.xPosition + b1.width / 2, b1.yPosition + b1.height / 2, mx, my, 4F, Color.GREEN.getRGB());
	}

	@Override
	public void onMouseClick(int mx, int my, int click) {
		if(click == 1) {
			b1 = null;
			return;
		} else if(click != 0) {
			return;
		}
		if(b1 == null) {
			b1 = gui.getQuestLine().getButtonAt(mx, my);
		} else {
			GuiButtonQuestInstance b2 = gui.getQuestLine().getButtonAt(mx, my);
			if(b1 == b2) {
				b1 = null;
			} else if(b2 != null) {
				if(!b2.getParents().contains(b1) && !b2.getQuest().getPrerequisites().contains(b1.getQuest()) && !b1.getParents().contains(b2) && !b1.getQuest().getPrerequisites().contains(b2.getQuest())) {
					b2.addParent(b1);
					b2.getQuest().getPrerequisites().add(b1.getQuest());
				} else {
					b2.getParents().remove(b1);
					b1.getParents().remove(b2);
					b2.getQuest().getPrerequisites().remove(b1.getQuest());
					b1.getQuest().getPrerequisites().remove(b2.getQuest());
				}
				NBTTagCompound tag1 = new NBTTagCompound();
				JsonObject base1 = new JsonObject();
				base1.add("config", b1.getQuest().writeToJson(new JsonObject()));
				tag1.setTag("data", NBTConverter.JSONtoNBT_Object(base1, new NBTTagCompound()));
				tag1.setInteger("action", EnumPacketAction.EDIT.ordinal());
				tag1.setInteger("questID", QuestDatabase.getKey(b1.getQuest()));
				NBTTagCompound tag2 = new NBTTagCompound();
				JsonObject base2 = new JsonObject();
				base2.add("config", b2.getQuest().writeToJson(new JsonObject()));
				tag2.setTag("data", NBTConverter.JSONtoNBT_Object(base2, new NBTTagCompound()));
				tag2.setInteger("action", EnumPacketAction.EDIT.ordinal());
				tag2.setInteger("questID", QuestDatabase.getKey(b2.getQuest()));
				PacketSender.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tag1));
				PacketSender.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tag2));
				b1 = null;
			}
		}
	}

	@Override
	public boolean allowTooltips() {
		return true;
	}

	@Override
	public boolean allowScrolling(int click) {
		return b1 == null || click == 2;
	}

	@Override
	public boolean clampScrolling() {
		return true;
	}
}