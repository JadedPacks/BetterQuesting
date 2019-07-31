package betterquesting.api.client.themes;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public interface ITheme {
	ResourceLocation getThemeID();
	String getDisplayName();
	ResourceLocation getGuiTexture();
	int getTextColor();
	IThemeRenderer getRenderer();
}