package betterquesting.api.client.gui.controls;

import betterquesting.api.misc.ICallback;
import betterquesting.client.gui.editors.GuiTextEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class GuiBigTextField extends GuiTextField {
	private GuiButtonThemed bigEdit;
	private ICallback<String> host;
	private final FontRenderer fontrenderer;
	private String watermark = "";
	private final Color wmColor = new Color(96, 96, 96);

	public GuiBigTextField(FontRenderer fontrenderer, int posX, int posY, int width, int height) {
		super(fontrenderer, posX, posY, width, height);
		this.fontrenderer = fontrenderer;
	}

	public GuiBigTextField enableBigEdit(ICallback<String> host) {
		this.width -= 20;
		bigEdit = new GuiButtonThemed(0, this.xPosition + width + 1, this.yPosition - 1, 20, height + 2, "Aa", true);
		this.host = host;
		return this;
	}

	public void setWatermark(String text) {
		watermark = text == null ? "" : text;
	}

	@Override
	public void mouseClicked(int mx, int my, int p_146192_3_) {
		Minecraft mc = Minecraft.getMinecraft();

		if(bigEdit != null && bigEdit.mousePressed(mc, mx, my)) {
			bigEdit.playPressSound(mc.getSoundHandler());
			GuiTextEditor gui = new GuiTextEditor(mc.currentScreen, getText());
			gui.setHost(host);
			mc.displayGuiScreen(gui);
		} else {
			super.mouseClicked(mx, my, p_146192_3_);
		}
	}

	@Override
	@Deprecated
	public void drawTextBox() {
		Minecraft mc = Minecraft.getMinecraft();

		int mx = 0;
		int my = 0;

		if(mc.currentScreen != null) {
			mx = Mouse.getEventX() * mc.currentScreen.width / mc.displayWidth;
			my = mc.currentScreen.height - Mouse.getEventY() * mc.currentScreen.height / mc.displayHeight - 1;
		}

		this.drawTextBox(mx, my);
	}

	public void drawTextBox(int mx, int my) {
		if(bigEdit != null) {
			bigEdit.xPosition = this.xPosition + width + 1;
			bigEdit.yPosition = this.yPosition - 1;
			bigEdit.height = this.height + 2;
			bigEdit.drawButton(Minecraft.getMinecraft(), mx, my);
		}

		super.drawTextBox();

		if(getText().length() <= 0 && watermark.length() > 0 && !isFocused()) {
			this.fontrenderer.drawString(watermark, this.xPosition + 4, this.yPosition + height / 2 - 4, wmColor.getRGB(), false);
		}
	}
}
