package betterquesting.api.client.toolbox;

import betterquesting.api.client.gui.misc.IGuiQuestLine;

public interface IToolboxTool {
	void initTool(IGuiQuestLine gui);
	void disableTool();
	void drawTool(int mx, int my);
	void onMouseClick(int mx, int my, int click);
	boolean allowTooltips();
	boolean allowScrolling(int click);
	boolean clampScrolling();
}