package betterquesting.client.gui;

import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.QuestLineButtonTree;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.themes.ThemeStandard;
import betterquesting.questing.QuestInstance;
import betterquesting.questing.QuestLine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiQuestLinesEmbedded extends GuiElement implements IGuiQuestLine {
	private final int posX;
	private final int posY;
	private final int sizeX;
	private final int sizeY;
	private int zoom = 100;
	private int scrollX = 0;
	private int scrollY = 0;
	private int maxX = 0;
	private int maxY = 0;
	private int lastMX = 0;
	private int lastMY = 0;
	private boolean noScroll = false;
	private IToolboxTool curTool = null;
	private QuestLine qLine;
	private final List<GuiButtonQuestInstance> qBtns = new ArrayList<>();
	private QuestLineButtonTree buttonTree = null;
	List<String> curTooltip = null;

	public GuiQuestLinesEmbedded(int posX, int posY, int sizeX, int sizeY) {
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}

	@Override
	public void setActiveTool(IToolboxTool tool) {
		if(curTool != null) {
			curTool.disableTool();
		}
		curTool = tool;
		if(tool != null) {
			tool.initTool(this);
		}
	}

	@Override
	public IToolboxTool getActiveTool() {
		return curTool;
	}

	@Override
	public void copySettings(IGuiQuestLine old) {
		this.zoom = old.getZoom();
		this.scrollX = old.getScrollX();
		this.scrollY = old.getScrollY();
		if(old.getActiveTool() != null) {
			old.getActiveTool().disableTool();
			this.setActiveTool(old.getActiveTool());
		}
	}

	@Override
	public void drawBackground(int mx, int my, float partialTick) {
		mouseDrag(mx, my);
		Minecraft mc = Minecraft.getMinecraft();
		double scaleX = sizeX / 128D;
		double scaleY = sizeY / 128D;
		float zs = zoom / 100F;
		int rmx = getRelativeX(mx);
		int rmy = getRelativeY(my);
		mc.renderEngine.bindTexture(ThemeStandard.getGuiTexture());
		GL11.glColor4f(1F, 1F, 1F, 1F);
		GL11.glPushMatrix();
		GL11.glScaled(scaleX, scaleY, 1F);
		GL11.glTranslated(posX / scaleX, posY / scaleY, 0);
		drawTexturedModalRect(0, 0, 0, 128, 128, 128);
		GL11.glPopMatrix();
		QuestInstance qTooltip = null;
		if(qLine != null) {
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			RenderUtils.guiScissor(mc, posX, posY, sizeX, sizeY);
			GL11.glTranslatef(posX + (scrollX) * zs, posY + (scrollY) * zs, 0);
			GL11.glScalef(zs, zs, 1F);
			for(GuiButtonQuestInstance btnQuest : qBtns) {
				btnQuest.drawButton(mc, rmx, rmy);
				if(btnQuest.visible && isWithin(rmx, rmy, btnQuest.xPosition, btnQuest.yPosition, btnQuest.width, btnQuest.height) && isWithin(mx, my, posX, posY, sizeX, sizeY)) {
					qTooltip = btnQuest.getQuest();
				}
			}
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			GL11.glPopMatrix();
		}
		if(curTool != null) {
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			RenderUtils.guiScissor(mc, posX, posY, sizeX, sizeY);
			GL11.glTranslatef(posX + (scrollX) * zs, posY + (scrollY) * zs, 0);
			GL11.glScalef(zs, zs, 1F);
			curTool.drawTool(rmx, rmy);
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			GL11.glPopMatrix();
		}
		if(qTooltip != null && (curTool == null || curTool.allowTooltips())) {
			curTooltip = qTooltip.getTooltip(mc.thePlayer);
		} else {
			curTooltip = null;
		}
	}

	@Override
	public void drawForeground(int mx, int my, float partialTick) {
		Minecraft mc = Minecraft.getMinecraft();
		if(qLine != null) {
			GL11.glPushMatrix();
			float scale = sizeX > 600 ? 1.5F : 1F;
			GL11.glScalef(scale, scale, scale);
			drawString(mc.fontRendererObj, I18n.format(qLine.name), MathHelper.ceiling_float_int((posX + 4) / scale), MathHelper.ceiling_float_int((posY + 4) / scale), getTextColor(), false);
			drawString(mc.fontRendererObj, zoom + "%", MathHelper.ceiling_float_int((posX + 4) / scale), MathHelper.ceiling_float_int((posY + sizeY - 4 - mc.fontRendererObj.FONT_HEIGHT) / scale), getTextColor(), false);
			GL11.glPopMatrix();
		}
		if(curTooltip != null && curTooltip.size() > 0) {
			drawTooltip(curTooltip, mx, my, mc.fontRendererObj);
			GL11.glDisable(GL11.GL_LIGHTING);
		}
	}

	@Override
	public void onMouseClick(int mx, int my, int click) {
		if(!isWithin(mx, my, posX, posY, sizeX, sizeY)) {
			noScroll = true;
			return;
		}
		if(curTool != null) {
			int rmx = getRelativeX(mx);
			int rmy = getRelativeY(my);
			curTool.onMouseClick(rmx, rmy, click);
		}
		noScroll = curTool != null && !curTool.allowScrolling(click);
	}

	@Override
	public void onMouseScroll(int mx, int my, int SDX) {
		if(SDX != 0 && isWithin(mx, my, posX, posY, sizeX, sizeY)) {
			setZoom(zoom - SDX * 5);
		}
	}

	private void setZoom(int value) {
		zoom = MathHelper.clamp_int(value, 50, 200);
		if(curTool == null) {
			clampScroll();
		}
	}

	@Override
	public int getZoom() {
		return zoom;
	}

	@Override
	public void setQuestLine(QuestLineButtonTree tree, boolean resetView) {
		buttonTree = tree;
		if(tree == null) {
			this.qLine = null;
			this.qBtns.clear();
		} else {
			this.qLine = tree.getQuestLine();
			this.qBtns.clear();
			this.qBtns.addAll(tree.getButtonTree());
			maxX = tree.getWidth();
			maxY = tree.getHeight();
			if(resetView) {
				zoom = 100;
				scrollX = Math.abs(sizeX - maxX) / 2;
				scrollY = 16;
			}
		}
	}

	private void mouseDrag(int mx, int my) {
		int mdx = 0;
		int mdy = 0;
		if(lastMX != 0 || lastMY != 0) {
			mdx = mx - lastMX;
			mdy = my - lastMY;
		}
		lastMX = mx;
		lastMY = my;
		if((Mouse.isButtonDown(0) && !noScroll) || Mouse.isButtonDown(2)) {
			float zs = zoom / 100F;
			scrollX += mdx / zs;
			scrollY += mdy / zs;
			if(curTool == null || curTool.clampScrolling()) {
				clampScroll();
			}
		}
	}

	public void clampScroll() {
		float zs = zoom / 100F;
		int sx2 = (int) (sizeX / zs);
		int sy2 = (int) (sizeY / zs);
		int zmx = Math.abs(sx2 / 2 - (maxX + 32) / 2);
		int zmy = Math.abs(sy2 / 2 - (maxY + 32) / 2);
		int zox = sx2 / 2 - (maxX + 32) / 2 + 16;
		int zoy = sy2 / 2 - (maxY + 32) / 2 + 16;
		scrollX = MathHelper.clamp_int(scrollX, -zmx + zox, zmx + zox);
		scrollY = MathHelper.clamp_int(scrollY, -zmy + zoy, zmy + zoy);
	}

	@Override
	public int getRelativeX(int x) {
		float zs = zoom / 100F;
		return (int) ((x - posX) / zs) - scrollX;
	}

	@Override
	public int getRelativeY(int y) {
		float zs = zoom / 100F;
		return (int) ((y - posY) / zs) - scrollY;
	}

	@Override
	public int getPosX() {
		return posX;
	}

	@Override
	public int getPosY() {
		return posY;
	}

	@Override
	public int getWidth() {
		return sizeX;
	}

	@Override
	public int getHeight() {
		return sizeY;
	}

	@Override
	public int getScrollX() {
		return scrollX;
	}

	@Override
	public int getScrollY() {
		return scrollY;
	}

	@Override
	public QuestLineButtonTree getQuestLine() {
		return buttonTree;
	}
}