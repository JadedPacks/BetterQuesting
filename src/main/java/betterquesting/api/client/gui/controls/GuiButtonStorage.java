package betterquesting.api.client.gui.controls;

public class GuiButtonStorage<T> extends GuiButtonThemed {
	private T storage = null;

	public GuiButtonStorage(int id, int posX, int posY, int width, int height, String text) {
		this(id, posX, posY, width, height, text, true);
	}

	public GuiButtonStorage(int id, int posX, int posY, int width, int height, String text, boolean shadow) {
		super(id, posX, posY, width, height, text, shadow);
	}

	public T getStored() {
		return storage;
	}

	public void setStored(T value) {
		this.storage = value;
	}
}