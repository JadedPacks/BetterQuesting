package betterquesting.client.gui.editors.json.callback;

import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonObject;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class JsonFluidCallback implements ICallback<FluidStack> {
	private FluidStack baseStack;
	private final JsonObject json;

	public JsonFluidCallback(JsonObject json) {
		this(json, new FluidStack(FluidRegistry.WATER, 1000));
	}

	public JsonFluidCallback(JsonObject json, FluidStack stack) {
		this.json = json;
		this.baseStack = stack;
	}

	public void setValue(FluidStack stack) {
		if(stack != null) {
			this.baseStack = stack;
		} else {
			this.baseStack = new FluidStack(FluidRegistry.WATER, 1000);
		}
		json.entrySet().clear();
		JsonHelper.FluidStackToJson(baseStack, json);
	}
}