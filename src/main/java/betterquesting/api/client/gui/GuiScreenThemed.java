package betterquesting.api.client.gui;

import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.client.themes.ThemeStandard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import scala.actors.threadpool.Arrays;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GuiScreenThemed extends GuiScreen implements GuiYesNoCallback {
	public final CopyOnWriteArrayList<IGuiEmbedded> embedded = new CopyOnWriteArrayList<>();
	public final GuiScreen parent;
	private String title;
	public int guiLeft = 0, guiTop = 0, sizeX = 0, sizeY = 0;

	public GuiScreenThemed(GuiScreen parent, String title) {
		this.mc = Minecraft.getMinecraft();
		this.fontRendererObj = this.mc.fontRendererObj;
		this.parent = parent;
		this.title = title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void initGui() {
		super.initGui();
		embedded.clear();
		int border = 8;
		this.sizeX = this.width - border * 2;
		this.sizeY = this.height - border * 2;
		this.sizeX = this.sizeX - (this.sizeX % 16);
		this.sizeY = this.sizeY - (this.sizeY % 16);
		this.guiLeft = (this.width - this.sizeX) / 2;
		this.guiTop = (this.height - this.sizeY) / 2;
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.buttonList.add(new GuiButtonThemed(0, guiLeft + sizeX / 2 - 100, guiTop + sizeY - 16, 200, 20, I18n.format("gui.done"), true));
	}

	public void drawBackPanel(int mx, int my, float partialTick) {
		this.drawDefaultBackground();
		ThemeStandard.drawThemedPanel(guiLeft, guiTop, sizeX, sizeY);
		String tmp = I18n.format(title);
		this.fontRendererObj.drawString(EnumChatFormatting.BOLD + tmp, this.guiLeft + (sizeX / 2) - this.fontRendererObj.getStringWidth(tmp) / 2, this.guiTop + 18, getTextColor(), false);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		for(IGuiEmbedded e : embedded) {
			GL11.glPushMatrix();
			e.drawBackground(mx, my, partialTick);
			GL11.glPopMatrix();
			GL11.glColor4f(1F, 1F, 1F, 1F);
		}
	}

	@Override
	public void drawScreen(int mx, int my, float partialTick) {
		GL11.glPushMatrix();
		GL11.glColor4f(1F, 1F, 1F, 1F);
		this.drawBackPanel(mx, my, partialTick);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		super.drawScreen(mx, my, partialTick);
		GL11.glPopMatrix();
		GL11.glColor4f(1F, 1F, 1F, 1F);
		for(IGuiEmbedded e : embedded) {
			GL11.glPushMatrix();
			e.drawForeground(mx, my, partialTick);
			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glPopMatrix();
		}
		this.mc.renderEngine.bindTexture(ThemeStandard.getGuiTexture());
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if(button.id == 0) {
			mc.displayGuiScreen(parent);
		}
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		int i = Mouse.getEventX() * width / mc.displayWidth;
		int j = height - Mouse.getEventY() * height / mc.displayHeight - 1;
		int k = Mouse.getEventButton();
		int SDX = (int) -Math.signum(Mouse.getEventDWheel());
		boolean flag = Mouse.getEventButtonState();
		for(IGuiEmbedded gui : embedded) {
			if(flag) {
				gui.onMouseClick(i, j, k);
			}
		}
		this.mouseScroll(i, j, SDX);
	}

	public void mouseScroll(int mx, int my, int scroll) {
		for(IGuiEmbedded gui : embedded) {
			gui.onMouseScroll(mx, my, scroll);
		}
	}

	@Override
	protected void keyTyped(char character, int keyCode) {
		if(keyCode == 1) {
			if(this instanceof IVolatileScreen) {
				this.mc.displayGuiScreen(new GuiYesNoLocked(this, I18n.format("betterquesting.gui.closing_warning"), I18n.format("betterquesting.gui.closing_confirm"), 0));
			} else {
				this.mc.displayGuiScreen(null);
				this.mc.setIngameFocus();
			}
		}
	}

	@Override
	public void confirmClicked(boolean confirmed, int id) {
		if(confirmed && id == 0) {
			this.mc.displayGuiScreen(null);
			this.mc.setIngameFocus();
		} else {
			this.mc.displayGuiScreen(this);
		}
	}

	public int getTextColor() {
		return ThemeStandard.getTextColor();
	}

	public void drawTooltip(List<String> list, int x, int y) {
		try {
			this.drawHoveringText(list, x, y, fontRendererObj);
		} catch(Exception e) {
			this.drawHoveringText(Arrays.asList(new String[] {"ERROR: " + e.getClass().getSimpleName()}), x, y, fontRendererObj);
		}
		GL11.glDisable(GL11.GL_LIGHTING);
	}

	@Override
	public final void drawCenteredString(FontRenderer font, String text, int x, int y, int color) {
		this.drawCenteredString(font, text, x, y, color, true);
	}

	public void drawCenteredString(FontRenderer font, String text, int x, int y, int color, boolean shadow) {
		font.drawString(text, x - font.getStringWidth(text) / 2, y, color, shadow);
	}

	@Override
	public final void drawString(FontRenderer font, String text, int x, int y, int color) {
		this.drawString(font, text, x, y, color, true);
	}

	public void drawString(FontRenderer font, String text, int x, int y, int color, boolean shadow) {
		font.drawString(text, x, y, color, shadow);
	}

	public boolean isWithin(int mx, int my, int startX, int startY, int sizeX, int sizeY) {
		return isWithin(mx, my, startX, startY, sizeX, sizeY, true);
	}

	public boolean isWithin(int mx, int my, int startX, int startY, int sizeX, int sizeY, boolean relative) {
		if(relative) {
			return mx - this.guiLeft >= startX && my - this.guiTop >= startY && mx - this.guiLeft < startX + sizeX && my - this.guiTop < startY + sizeY;
		} else {
			return mx >= startX && my >= startY && mx < startX + sizeX && my < startY + sizeY;
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}