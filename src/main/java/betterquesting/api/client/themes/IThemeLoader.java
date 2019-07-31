package betterquesting.api.client.themes;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

public interface IThemeLoader {
	ResourceLocation getLoaderID();
	ITheme loadTheme(JsonObject json, String domain);
}