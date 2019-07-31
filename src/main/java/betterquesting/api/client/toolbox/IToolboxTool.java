package betterquesting.api.client.toolbox;

import betterquesting.api.client.gui.misc.IGuiQuestLine;

public interface IToolboxTool {
	void initTool(IGuiQuestLine gui);
	void disableTool();
	void drawTool(int mx, int my, float partialTick);
	void onMouseClick(int mx, int my, int click);
	void onMouseScroll(int mx, int my, int scroll);
	void onKeyPressed(char c, int key);
	boolean allowTooltips();
	boolean allowScrolling(int click);
	boolean allowZoom();
	boolean clampScrolling();
}