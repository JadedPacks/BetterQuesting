package betterquesting.api.client.gui.misc;

import betterquesting.api.client.gui.QuestLineButtonTree;
import betterquesting.api.client.toolbox.IToolboxTool;
import net.minecraft.util.ResourceLocation;

public interface IGuiQuestLine extends IGuiEmbedded {
	IToolboxTool getActiveTool();
	void setActiveTool(IToolboxTool tool);
	QuestLineButtonTree getQuestLine();
	void setQuestLine(QuestLineButtonTree line, boolean resetView);
	void setBackground(ResourceLocation image, int size);
	int getRelativeX(int mx);
	int getRelativeY(int my);
	int getZoom();
	int getScrollX();
	int getScrollY();
	int getPosX();
	int getPosY();
	int getWidth();
	int getHeight();
	void copySettings(IGuiQuestLine gui);
}