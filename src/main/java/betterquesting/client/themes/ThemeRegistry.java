package betterquesting.client.themes;

import betterquesting.api.client.themes.ITheme;
import betterquesting.api.client.themes.IThemeLoader;
import betterquesting.api.client.themes.IThemeRegistry;
import betterquesting.api.utils.JsonHelper;
import betterquesting.core.BetterQuesting;
import com.google.gson.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ThemeRegistry implements IThemeRegistry {
	public static final ThemeRegistry INSTANCE = new ThemeRegistry();
	private final ITheme fallbackTheme = new ThemeStandard("Standard", new ResourceLocation("betterquesting", "textures/gui/editor_gui.png"), new ResourceLocation("betterquesting:fallback"));
	private ITheme currentTheme = null;
	private final HashMap<ResourceLocation, ITheme> themeList = new HashMap<>(),
		resThemes = new HashMap<>();
	private final HashMap<ResourceLocation, IThemeLoader> themeLoaders = new HashMap<>();

	private ThemeRegistry() {
		registerLoader(new ThemeLoaderStandard());
	}

	@Override
	public ITheme getTheme(ResourceLocation name) {
		ITheme tmp = themeList.get(name);
		tmp = tmp != null ? tmp : resThemes.get(name);
		return tmp;
	}

	@Override
	public List<ITheme> getAllThemes() {
		ArrayList<ITheme> list = new ArrayList<>();
		list.addAll(themeList.values());
		list.addAll(resThemes.values());
		return list;
	}

	@Override
	public void registerLoader(IThemeLoader loader) {
		if(loader == null) {
			throw new NullPointerException("Tried to register null theme loader");
		} else if(loader.getLoaderID() == null) {
			throw new IllegalArgumentException("Tried to register a theme loader with a null name");
		}
		if(themeLoaders.containsKey(loader.getLoaderID()) || themeList.containsValue(loader)) {
			throw new IllegalArgumentException("Cannot register dupliate theme loader '" + loader.getLoaderID() + "'");
		}
		themeLoaders.put(loader.getLoaderID(), loader);
	}

	@Override
	public IThemeLoader getLoader(ResourceLocation name) {
		return themeLoaders.get(name);
	}

	@Override
	public ITheme getCurrentTheme() {
		if(currentTheme == null) {
			currentTheme = this.getTheme(new ResourceLocation("betterquesting:light"));
		}
		return currentTheme != null ? currentTheme : fallbackTheme;
	}

	@Override
	public void reloadThemes() {
		resThemes.clear();
		IResourceManager resManager = Minecraft.getMinecraft().getResourceManager();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		for(String domain : (Iterable<String>) resManager.getResourceDomains()) {
			try {
				ResourceLocation res = new ResourceLocation(domain, "bq_themes.json");
				for(IResource iresource : (Iterable<IResource>) resManager.getAllResources(res)) {
					try {
						InputStreamReader isr = new InputStreamReader(iresource.getInputStream());
						JsonArray jAry = gson.fromJson(isr, JsonArray.class);
						isr.close();
						for(JsonElement je : jAry) {
							if(je == null || !je.isJsonObject()) {
								BetterQuesting.logger.warn("Invalid theme in " + domain);
								continue;
							}
							JsonObject jThm = je.getAsJsonObject();
							ResourceLocation loadID = new ResourceLocation(JsonHelper.GetString(jThm, "themeType", "betterquesting:standard"));
							IThemeLoader loader = getLoader(loadID);
							if(loader == null) {
								continue;
							}
							ITheme theme = loader.loadTheme(jThm, domain);
							if(theme != null) {
								resThemes.put(theme.getThemeID(), theme);
							}
						}
					} catch(Exception e) {
						BetterQuesting.logger.error("Error reading bq_themes.json from " + domain, e);
					}
				}
			} catch(Exception ignored) {}
		}
	}
}