package betterquesting.api.client.gui.controls;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.themes.ITheme;
import betterquesting.api.placeholders.ThemeDummy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiButtonThemed extends GuiButton {
	public final Minecraft mc;
	private final boolean txtShadow;
	private ResourceLocation icon = null;
	private boolean is = false;
	private int iu = 0;
	private int iv = 0;
	private int iw = 0;
	private int ih = 0;
	private List<String> tooltip = new ArrayList<>();

	public GuiButtonThemed(int id, int posX, int posY, String text) {
		this(id, posX, posY, 200, 20, text, true);
	}

	public GuiButtonThemed(int id, int posX, int posY, int width, int height, String text) {
		this(id, posX, posY, width, height, text, true);
	}

	public GuiButtonThemed(int id, int posX, int posY, int width, int height, String text, boolean shadow) {
		super(id, posX, posY, width, height, text);
		this.mc = Minecraft.getMinecraft();
		this.txtShadow = shadow;
	}

	public void setIcon(ResourceLocation texture, int u, int v, int w, int h, boolean stretch) {
		this.icon = texture;
		this.is = stretch;
		this.iu = u;
		this.iv = v;
		this.iw = w;
		this.ih = h;
	}

	public List<String> getTooltip() {
		return tooltip;
	}

	public void setTooltip(List<String> tooltip) {
		this.tooltip = tooltip;
	}

	@Override
	public void drawButton(Minecraft mc, int mx, int my) {
		if(this.visible) {
			FontRenderer fontrenderer = mc.fontRendererObj;
			mc.getTextureManager().bindTexture(this.currentTheme().getGuiTexture());
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mx >= this.xPosition && my >= this.yPosition && mx < this.xPosition + this.width && my < this.yPosition + this.height;
			int k = this.getHoverState(this.hovered);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glPushMatrix();
			float sh = height / 20F;
			float sw = width >= 196 ? width / 200F : 1F;
			float py = yPosition / sh;
			float px = xPosition / sw;
			GL11.glScalef(sw, sh, 1F);
			if(width >= 196) {
				GL11.glTranslatef(px, py, 0F);
				this.drawTexturedModalRect(0, 0, 48, k * 20, 200, 20);
			} else {
				this.drawTexturedModalRect((int) px, (int) py, 48, k * 20, this.width / 2, 20);
				this.drawTexturedModalRect((int) px + width / 2, (int) py, 248 - this.width / 2, k * 20, this.width / 2, 20);
			}
			GL11.glPopMatrix();
			this.mouseDragged(mc, mx, my);
			int l = 14737632;
			if(packedFGColour != 0) {
				l = packedFGColour;
			} else if(!this.enabled) {
				l = 10526880;
			} else if(this.hovered) {
				l = 16777120;
			}
			if(icon != null) {
				mc.renderEngine.bindTexture(icon);
				if(is) {
					float iScale = Math.min(width / (float) iw, height / (float) ih);
					GL11.glPushMatrix();
					GL11.glScalef(iScale, iScale, 1F);
					GL11.glTranslatef(xPosition / iScale, yPosition / iScale, 0F);
					this.drawTexturedModalRect((int) (width / 2 - (iw * iScale) / 2F), (int) (height / 2 - (ih * iScale) / 2F), iu, iv, iw, ih);
					GL11.glPopMatrix();
				} else {
					this.drawTexturedModalRect(xPosition + width / 2 - iw / 2, yPosition + height / 2 - ih / 2, iu, iv, iw, ih);
				}
			}
			String txt = this.displayString;
			if(fontrenderer.getStringWidth(txt) > width) {
				int dotWidth = fontrenderer.getStringWidth("...");
				txt = fontrenderer.trimStringToWidth(txt, width - dotWidth) + "...";
			}
			this.drawCenteredString(fontrenderer, txt, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l, txtShadow);
		}
	}

	public void drawCenteredString(FontRenderer font, String text, int x, int y, int color, boolean shadow) {
		font.drawString(text, x - font.getStringWidth(text) / 2, y, color, shadow);
	}

	public void drawString(FontRenderer font, String text, int x, int y, int color, boolean shadow) {
		font.drawString(text, x, y, color, shadow);
	}

	@Override
	public void playPressSound(SoundHandler p_146113_1_) {
		p_146113_1_.playSound(PositionedSoundRecord.createPositionedSoundRecord(new ResourceLocation("gui.button.press"), 1.0F));
	}

	public ITheme currentTheme() {
		if(QuestingAPI.getAPI(ApiReference.THEME_REG) != null) {
			return QuestingAPI.getAPI(ApiReference.THEME_REG).getCurrentTheme();
		} else {
			return ThemeDummy.INSTANCE;
		}
	}
}