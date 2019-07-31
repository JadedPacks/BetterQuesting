package betterquesting.client.gui.editors.json;

import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;

public class TextCallbackJsonArray implements ICallback<String> {
	private final JsonArray json;
	private final int index;

	public TextCallbackJsonArray(JsonArray json, int index) {
		this.json = json;
		this.index = index;
	}

	@Override
	public void setValue(String text) {
		ArrayList<JsonElement> list = JsonHelper.GetUnderlyingArray(json);
		if(list == null || index < 0 || index >= list.size()) {
			return;
		}
		list.set(index, new JsonPrimitive(text));
	}
}