package betterquesting.api.client.gui.misc;

public interface IGuiEmbedded {
	void drawBackground(int mx, int my, float partialTick);
	void drawForeground(int mx, int my, float partialTick);
	void onMouseClick(int mx, int my, int click);
	void onMouseScroll(int mx, int my, int scroll);
}