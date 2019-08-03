package betterquesting.client.gui.editors.json.scrolling;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.misc.ICallback;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiJsonEditor extends GuiScreenThemed {
	private final JsonElement json;
	private ICallback<JsonObject> joCallback;
	private ICallback<JsonArray> jaCallback;

	public GuiJsonEditor(GuiScreen parent, JsonObject json) {
		this(parent, json, null);
	}

	public GuiJsonEditor(GuiScreen parent, JsonObject json, ICallback<JsonObject> callback) {
		super(parent, "betterquesting.title.json_object");
		this.json = json;
		this.joCallback = callback;
	}

	public GuiJsonEditor(GuiScreen parent, JsonArray json) {
		this(parent, json, null);
	}

	public GuiJsonEditor(GuiScreen parent, JsonArray json, ICallback<JsonArray> callback) {
		super(parent, "betterquesting.title.json_array");
		this.json = json;
		this.jaCallback = callback;
	}

	public void initGui() {
		super.initGui();
		GuiScrollingJson gsj = new GuiScrollingJson(mc, guiLeft + 16, guiTop + 32, sizeX - 32, sizeY - 64);
		if(json.isJsonObject()) {
			gsj.setJson(json.getAsJsonObject());
		} else {
			gsj.setJson(json.getAsJsonArray());
		}
		gsj.refresh();
		this.embedded.add(gsj);
	}

	public void actionPerformed(GuiButton btn) {
		super.actionPerformed(btn);
		if(btn.id == 0) {
			if(json.isJsonObject() && joCallback != null) {
				joCallback.setValue(json.getAsJsonObject());
			} else if(json.isJsonArray() && jaCallback != null) {
				jaCallback.setValue(json.getAsJsonArray());
			}
		}
	}
}