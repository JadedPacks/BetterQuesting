package betterquesting.client.toolbox.tools;

import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.client.toolbox.GuiToolIconProxy;
import net.minecraft.client.Minecraft;

public class ToolboxToolIcon implements IToolboxTool {
	IGuiQuestLine gui;

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
		GuiButtonQuestInstance btn = gui.getQuestLine().getButtonAt(mx, my);
		if(btn != null) {
			Minecraft mc = Minecraft.getMinecraft();
			btn.playPressSound(mc.getSoundHandler());
			mc.displayGuiScreen(new GuiToolIconProxy(mc.currentScreen, btn.getQuest()));
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