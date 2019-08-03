package betterquesting.api.client.gui.lists;

public interface IScrollingEntry {
	void drawBackground(int mx, int my, int px, int py, int width);
	void drawForeground(int mx, int my);
	void onMouseClick(int mx, int my, int click);
	int getHeight();
	boolean canDrawOutsideBox(boolean isForeground);
}