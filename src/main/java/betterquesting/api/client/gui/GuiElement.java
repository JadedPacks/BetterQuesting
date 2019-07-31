package betterquesting.api.client.gui;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.themes.ITheme;
import betterquesting.api.placeholders.ThemeDummy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

public abstract class GuiElement {
	public float zLevel = 0F;

	public static ITheme currentTheme() {
		if(QuestingAPI.getAPI(ApiReference.THEME_REG) != null) {
			return QuestingAPI.getAPI(ApiReference.THEME_REG).getCurrentTheme();
		} else {
			return ThemeDummy.INSTANCE;
		}
	}

	public static int getTextColor() {
		return currentTheme().getTextColor();
	}

	public void drawCenteredString(FontRenderer font, String text, int x, int y, int color, boolean shadow) {
		font.drawString(text, x - font.getStringWidth(text) / 2, y, color, shadow);
	}

	public void drawString(FontRenderer font, String text, int x, int y, int color, boolean shadow) {
		font.drawString(text, x, y, color, shadow);
	}

	public void drawTexturedModalRect(int x, int y, int u, int v, int w, int h) {
		float f = 0.00390625F;
		float f1 = 0.00390625F;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, y + h, this.zLevel, (float) (u) * f, (float) (v + h) * f1);
		tessellator.addVertexWithUV(x + w, y + h, this.zLevel, (float) (u + w) * f, (float) (v + h) * f1);
		tessellator.addVertexWithUV(x + w, y, this.zLevel, (float) (u + w) * f, (float) (v) * f1);
		tessellator.addVertexWithUV(x, y, this.zLevel, (float) (u) * f, (float) (v) * f1);
		tessellator.draw();
	}

	public void drawTexturedModelRectFromIcon(int x, int y, IIcon icon, int w, int h) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, y + h, this.zLevel, icon.getMinU(), icon.getMaxV());
		tessellator.addVertexWithUV(x + w, y + h, this.zLevel, icon.getMaxU(), icon.getMaxV());
		tessellator.addVertexWithUV(x + w, y, this.zLevel, icon.getMaxU(), icon.getMinV());
		tessellator.addVertexWithUV(x, y, this.zLevel, icon.getMinU(), icon.getMinV());
		tessellator.draw();
	}

	public void drawGradientRect(int x1, int y1, int x2, int y2, int color1, int color2) {
		float f = (float) (color1 >> 24 & 255) / 255.0F;
		float f1 = (float) (color1 >> 16 & 255) / 255.0F;
		float f2 = (float) (color1 >> 8 & 255) / 255.0F;
		float f3 = (float) (color1 & 255) / 255.0F;
		float f4 = (float) (color2 >> 24 & 255) / 255.0F;
		float f5 = (float) (color2 >> 16 & 255) / 255.0F;
		float f6 = (float) (color2 >> 8 & 255) / 255.0F;
		float f7 = (float) (color2 & 255) / 255.0F;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(f1, f2, f3, f);
		tessellator.addVertex(x2, y1, this.zLevel);
		tessellator.addVertex(x1, y1, this.zLevel);
		tessellator.setColorRGBA_F(f5, f6, f7, f4);
		tessellator.addVertex(x1, y2, this.zLevel);
		tessellator.addVertex(x2, y2, this.zLevel);
		tessellator.draw();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public void drawTooltip(List<String> list, int x, int y, FontRenderer font) {
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		int sw = scaledresolution.getScaledWidth();
		int sh = scaledresolution.getScaledHeight();
		if(!list.isEmpty()) {
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			int k = 0;
			for(String s : list) {
				if(s == null) {
					continue;
				}
				int l = font.getStringWidth(s);

				if(l > k) {
					k = l;
				}
			}
			int j2 = x + 12;
			int k2 = y - 12;
			int i1 = 8;
			if(list.size() > 1) {
				i1 += 2 + (list.size() - 1) * 10;
			}
			if(j2 + k > sw) {
				j2 -= 28 + k;
			}
			if(k2 + i1 + 6 > sh) {
				k2 = sh - i1 - 6;
			}
			this.zLevel = 300.0F;
			int j1 = -267386864;
			this.drawGradientRect(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j1, j1);
			this.drawGradientRect(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j1, j1);
			this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j1, j1);
			this.drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j1, j1);
			this.drawGradientRect(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j1, j1);
			int k1 = 1347420415;
			int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
			this.drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k1, l1);
			this.drawGradientRect(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k1, l1);
			this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k1, k1);
			this.drawGradientRect(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l1, l1);
			for(int i2 = 0; i2 < list.size(); ++i2) {
				String s1 = list.get(i2);
				font.drawStringWithShadow(s1, j2, k2, -1);
				if(i2 == 0) {
					k2 += 2;
				}
				k2 += 10;
			}
			this.zLevel = 0.0F;
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableStandardItemLighting();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		}
	}

	public boolean isWithin(int xIn, int yIn, int x, int y, int w, int h) {
		return xIn >= x && xIn < x + w && yIn >= y && yIn < y + h;
	}
}