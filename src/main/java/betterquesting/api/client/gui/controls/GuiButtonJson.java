package betterquesting.api.client.gui.controls;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraftforge.fluids.FluidStack;

public class GuiButtonJson<T extends JsonElement> extends GuiButtonStorage<T> {
	private T json;
	private boolean isItem = false, isFluid = false, isEntity = false;

	public GuiButtonJson(int id, int posX, int posY, int width, int height, T json, boolean shadow) {
		super(id, posX, posY, width, height, "", shadow);
		this.json = json;
		this.refreshJson();
	}

	@Override
	public T getStored() {
		return json;
	}

	@Override
	public void setStored(T value) {
		this.json = value;
	}

	public void refreshJson() {
		if(json == null) {
			this.displayString = "?";
			return;
		} else if(json.isJsonObject()) {
			if(JsonHelper.isItem(json.getAsJsonObject())) {
				isItem = true;
			} else if(JsonHelper.isFluid(json.getAsJsonObject())) {
				isFluid = true;
			} else if(JsonHelper.isEntity(json.getAsJsonObject())) {
				isEntity = true;
			}
		}
		if(isItem) {
			BigItemStack stack = JsonHelper.JsonToItemStack(json.getAsJsonObject());
			this.displayString = I18n.format("betterquesting.btn.item") + ": " + stack.getBaseStack().getDisplayName();
		} else if(isFluid) {
			FluidStack fluid = JsonHelper.JsonToFluidStack(json.getAsJsonObject());
			this.displayString = I18n.format("betterquesting.btn.fluid") + ": " + fluid.getLocalizedName();
		} else if(isEntity) {
			Entity entity = JsonHelper.JsonToEntity(json.getAsJsonObject(), this.mc.theWorld);
			this.displayString = I18n.format("betterquesting.btn.entity") + ": " + entity.getCommandSenderName();
		} else {
			this.displayString = getJsonName();
		}
	}

	private String getJsonName() {
		if(json == null) {
			return "?";
		} else if(json instanceof JsonObject) {
			return I18n.format("betterquesting.btn.object") + "...";
		} else if(json instanceof JsonArray) {
			return I18n.format("betterquesting.btn.list") + "...";
		} else if(json instanceof JsonPrimitive) {
			JsonPrimitive jPrim = json.getAsJsonPrimitive();
			if(jPrim.isBoolean()) {
				return "" + jPrim.getAsBoolean();
			} else {
				return I18n.format("betterquesting.btn.text");
			}
		}
		return json.getClass().getSimpleName();
	}

	public boolean isItem() {
		return isItem;
	}

	public boolean isEntity() {
		return isEntity;
	}
}