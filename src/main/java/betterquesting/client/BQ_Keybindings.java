package betterquesting.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class BQ_Keybindings {
	public static KeyBinding openQuests;

	public static void RegisterKeys() {
		openQuests = new KeyBinding("key.betterquesting.quests", Keyboard.KEY_GRAVE, "BetterQuesting");
		ClientRegistry.registerKeyBinding(openQuests);
	}
}