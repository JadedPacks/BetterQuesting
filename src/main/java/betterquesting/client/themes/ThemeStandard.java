package betterquesting.client.themes;

import betterquesting.api.client.themes.ITheme;
import betterquesting.api.client.themes.IThemeRenderer;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class ThemeStandard implements ITheme {
	private final ThemeRenderStandard renderer = new ThemeRenderStandard();
	private final ResourceLocation regName, guiTexture;
	private final String name;
	private int txtColor = Color.BLACK.getRGB();

	public ThemeStandard(String name, ResourceLocation texture, ResourceLocation regName) {
		this.regName = regName;
		this.name = name;
		this.guiTexture = texture;
	}

	@Override
	public ResourceLocation getThemeID() {
		return regName;
	}

	@Override
	public String getDisplayName() {
		return name;
	}

	@Override
	public ResourceLocation getGuiTexture() {
		return guiTexture;
	}

	public void setTextColor(int c) {
		txtColor = c;
	}

	public void setLineColors(int locked, int incomplete, int complete) {
		renderer.setLineColors(locked, incomplete, complete);
	}

	public void setIconColors(int locked, int incomplete, int pending, int complete) {
		renderer.setIconColors(locked, incomplete, pending, complete);
	}

	@Override
	public int getTextColor() {
		return txtColor;
	}

	@Override
	public IThemeRenderer getRenderer() {
		return renderer;
	}
}