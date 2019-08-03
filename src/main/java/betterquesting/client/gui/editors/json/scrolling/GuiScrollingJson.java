package betterquesting.client.gui.editors.json.scrolling;

import betterquesting.api.client.gui.lists.GuiScrollingBase;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.util.Map.Entry;

public class GuiScrollingJson extends GuiScrollingBase<ScrollingJsonEntry> {
	private JsonElement json;
	private final int posX;

	public GuiScrollingJson(Minecraft mc, int x, int y, int w, int h) {
		super(mc, x, y, w, h);
		this.posX = x;
	}

	public void setJson(JsonObject json) {
		setJsonInternal(json);
	}

	public void setJson(JsonArray json) {
		setJsonInternal(json);
	}

	private void setJsonInternal(JsonElement json) {
		this.json = json;
		refresh();
	}

	public void refresh() {
		if(json == null) {
			return;
		}
		int width = this.getListWidth();
		this.getEntryList().clear();
		if(json.isJsonArray()) {
			JsonArray ja = json.getAsJsonArray();
			for(int i = 0; i < ja.size(); i++) {
				ScrollingJsonEntry sje = new ScrollingJsonEntry(this, ja, i);
				sje.setupEntry(posX, width);
				this.getEntryList().add(sje);
			}
			ScrollingJsonEntry sje = new ScrollingJsonEntry(this, ja, ja.size());
			sje.setupEntry(posX, width);
			this.getEntryList().add(sje);
		} else if(json.isJsonObject()) {
			JsonObject jo = json.getAsJsonObject();
			for(Entry<String, JsonElement> entry : jo.entrySet()) {
				ScrollingJsonEntry sje = new ScrollingJsonEntry(this, jo, entry.getKey());
				sje.setupEntry(posX, width);
				this.getEntryList().add(sje);
			}
			ScrollingJsonEntry sje = new ScrollingJsonEntry(this, jo, "");
			sje.setupEntry(posX, width);
			this.getEntryList().add(sje);
		}
	}
}