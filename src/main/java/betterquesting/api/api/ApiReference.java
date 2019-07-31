package betterquesting.api.api;

import betterquesting.api.client.gui.misc.IGuiHelper;
import betterquesting.api.client.themes.IThemeRegistry;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.storage.INameCache;
import betterquesting.api.storage.IQuestSettings;

public class ApiReference {
	public static final ApiKey<IQuestDatabase> QUEST_DB = new ApiKey<>();
	public static final ApiKey<IQuestLineDatabase> LINE_DB = new ApiKey<>();
	public static final ApiKey<IQuestSettings> SETTINGS = new ApiKey<>();
	public static final ApiKey<INameCache> NAME_CACHE = new ApiKey<>();
	public static final ApiKey<IThemeRegistry> THEME_REG = new ApiKey<>();
	public static final ApiKey<IGuiHelper> GUI_HELPER = new ApiKey<>();
}
