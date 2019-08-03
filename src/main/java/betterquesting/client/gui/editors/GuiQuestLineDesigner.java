package betterquesting.client.gui.editors;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.QuestLineButtonTree;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.GuiQuestLinesEmbedded;
import betterquesting.client.themes.ThemeStandard;
import betterquesting.client.toolbox.ToolboxGuiMain;
import betterquesting.questing.QuestLine;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

public class GuiQuestLineDesigner extends GuiScreenThemed implements IVolatileScreen, INeedsRefresh {
	private final int lineID;
	private QuestLine qLine;
	private GuiQuestLinesEmbedded qlGui;

	public GuiQuestLineDesigner(GuiScreen parent, QuestLine qLine) {
		super(parent, "betterquesting.title.designer");
		this.qLine = qLine;
		this.lineID = QuestLineDatabase.getKey(qLine);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.sizeX -= 96;
		((GuiButton) this.buttonList.get(0)).xPosition = guiLeft + sizeX / 2 - 100;
		GuiQuestLinesEmbedded oldGui = qlGui;
		qlGui = new GuiQuestLinesEmbedded(guiLeft + 16, guiTop + 16, sizeX - 32, sizeY - 32);
		qlGui.setQuestLine(new QuestLineButtonTree(qLine), true);
		if(oldGui != null) {
			embedded.remove(oldGui);
			qlGui.copySettings(oldGui);
		}
		qlGui.clampScroll();
		embedded.add(qlGui);
		embedded.add(new ToolboxGuiMain(qlGui, guiLeft + sizeX + 16, guiTop + 32, 64, sizeY - 48));
	}

	@Override
	public void drawBackPanel(int mx, int my, float partialTick) {
		this.drawDefaultBackground();
		ThemeStandard.drawThemedPanel(guiLeft + sizeX, guiTop, 96, sizeY);
		ThemeStandard.drawThemedPanel(guiLeft, guiTop, sizeX, sizeY);
		String tmp = I18n.format("betterquesting.title.designer");
		this.fontRendererObj.drawString(EnumChatFormatting.BOLD + tmp, this.guiLeft + (sizeX / 2) - this.fontRendererObj.getStringWidth(tmp) / 2, this.guiTop + 18, getTextColor(), false);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		for(IGuiEmbedded e : embedded) {
			GL11.glPushMatrix();
			e.drawBackground(mx, my, partialTick);
			GL11.glPopMatrix();
			GL11.glColor4f(1F, 1F, 1F, 1F);
		}
		RenderUtils.DrawLine(guiLeft + sizeX + 16, guiTop + 32, guiLeft + sizeX + 80, guiTop + 32, partialTick, getTextColor());
		String tabTitle = EnumChatFormatting.UNDERLINE + I18n.format("betterquesting.toolbox.tab.main");
		this.fontRendererObj.drawString(tabTitle, guiLeft + sizeX + 48 - fontRendererObj.getStringWidth(tabTitle) / 2, guiTop + 16 + 2, getTextColor(), false);
	}

	@Override
	public void refreshGui() {
		qLine = QuestLineDatabase.getValue(lineID);
		if(qLine == null) {
			mc.displayGuiScreen(parent);
			return;
		}
		qlGui.setQuestLine(new QuestLineButtonTree(qLine), false);
		qlGui.setActiveTool(qlGui.getActiveTool());
	}
}