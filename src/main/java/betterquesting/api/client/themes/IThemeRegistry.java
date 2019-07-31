package betterquesting.api.client.themes;

import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface IThemeRegistry {
	ITheme getTheme(ResourceLocation name);
	List<ITheme> getAllThemes();
	void registerLoader(IThemeLoader loader);
	IThemeLoader getLoader(ResourceLocation name);
	ITheme getCurrentTheme();
	void reloadThemes();
}