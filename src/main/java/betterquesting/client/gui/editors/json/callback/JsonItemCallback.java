package betterquesting.client.gui.editors.json.callback;

import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonObject;
import net.minecraft.init.Blocks;

public class JsonItemCallback implements ICallback<BigItemStack> {
	private BigItemStack baseStack;
	private final JsonObject json;

	public JsonItemCallback(JsonObject json) {
		this(json, new BigItemStack(Blocks.stone));
	}

	public JsonItemCallback(JsonObject json, BigItemStack stack) {
		this.json = json;
		this.baseStack = stack;
	}

	public void setValue(BigItemStack stack) {
		if(stack != null) {
			this.baseStack = stack;
		} else {
			this.baseStack = new BigItemStack(Blocks.stone);
		}
		json.entrySet().clear();
		JsonHelper.ItemStackToJson(baseStack, json);
	}
}