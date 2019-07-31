package betterquesting.client.toolbox;

import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTab;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.client.toolbox.tools.*;

public class ToolboxTabMain implements IToolboxTab {
	public static final ToolboxTabMain instance = new ToolboxTabMain();
	private IGuiQuestLine gui;
	public IToolboxTool toolOpen, toolNew, toolGrab, toolLink, toolCopy, toolRem, toolDel, toolCom, toolRes, toolIco, toolSca;

	@Override
	public String getUnlocalisedName() {
		return "betterquesting.toolbox.tab.main";
	}

	@Override
	public void initTools(IGuiQuestLine designer) {
		this.gui = designer;
		toolOpen = new ToolboxToolOpen();
		toolNew = new ToolboxToolNew();
		toolGrab = new ToolboxToolGrab();
		toolLink = new ToolboxToolLink();
		toolCopy = new ToolboxToolCopy();
		toolRem = new ToolboxToolRemove();
		toolDel = new ToolboxToolDelete();
		toolCom = new ToolboxToolComplete();
		toolRes = new ToolboxToolReset();
		toolIco = new ToolboxToolIcon();
		toolSca = new ToolboxToolScale();
	}

	@Override
	public IGuiEmbedded getTabGui(int posX, int posY, int sizeX, int sizeZ) {
		if(gui == null) {
			return null;
		}
		return new ToolboxGuiMain(gui, posX, posY, sizeX, sizeZ);
	}
}