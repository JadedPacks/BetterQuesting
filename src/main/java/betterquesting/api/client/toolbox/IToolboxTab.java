package betterquesting.api.client.toolbox;

import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.client.gui.misc.IGuiQuestLine;

public interface IToolboxTab {
	String getUnlocalisedName();
	void initTools(IGuiQuestLine gui);
	IGuiEmbedded getTabGui(int x, int y, int w, int h);
}