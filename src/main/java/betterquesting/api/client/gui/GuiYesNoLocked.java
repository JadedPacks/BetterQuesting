package betterquesting.api.client.gui;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;

public class GuiYesNoLocked extends GuiYesNo {
	public GuiYesNoLocked(GuiYesNoCallback callback, String txt1, String txt2, int id) {
		super(callback, txt1, txt2, id);
	}

	@Override
	protected void keyTyped(char character, int keyCode) {}
}