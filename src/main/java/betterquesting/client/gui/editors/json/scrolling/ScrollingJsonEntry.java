package betterquesting.client.gui.editors.json.scrolling;

import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.controls.GuiBigTextField;
import betterquesting.api.client.gui.controls.GuiButtonJson;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.controls.GuiNumberField;
import betterquesting.api.client.gui.lists.IScrollingEntry;
import betterquesting.api.utils.JsonHelper;
import betterquesting.client.gui.editors.json.*;
import betterquesting.client.gui.editors.json.callback.JsonEntityCallback;
import betterquesting.client.gui.editors.json.callback.JsonFluidCallback;
import betterquesting.client.gui.editors.json.callback.JsonItemCallback;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ScrollingJsonEntry extends GuiElement implements IScrollingEntry {
	private final GuiScrollingJson host;
	private final JsonElement json;
	private final String name;
	private String key = "";
	private int idx = -1;
	private final JsonElement je;
	private GuiTextField txtMain;
	private final Minecraft mc;
	private final List<GuiButtonThemed> btnList = new ArrayList<>();

	public ScrollingJsonEntry(GuiScrollingJson host, JsonObject json, String key) {
		this.mc = Minecraft.getMinecraft();
		this.host = host;
		this.json = json;
		this.key = key;
		this.name = key;
		this.je = json.get(key);
	}

	public ScrollingJsonEntry(GuiScrollingJson host, JsonArray json, int index) {
		this.mc = Minecraft.getMinecraft();
		this.host = host;
		this.json = json;
		this.idx = index;
		this.je = index < 0 || index >= json.size() ? null : json.get(index);
		this.name = je == null ? "" : ("#" + index);
	}

	public void setupEntry(int px, int width) {
		btnList.clear();
		int margin = px + (width / 3);
		int ctrlSpace = MathHelper.ceiling_float_int((width / 3F) * 2F);
		int n = 40;
		if(je != null) {
			GuiButtonThemed btnDel = new GuiButtonThemed(3, px + width - 20, 0, 20, 20, "x");
			btnDel.packedFGColour = Color.RED.getRGB();
			btnList.add(btnDel);
		}
		GuiButtonThemed btnAdd = new GuiButtonThemed(2, px + width - n, 0, 20, 20, "+");
		btnAdd.packedFGColour = Color.GREEN.getRGB();
		btnList.add(btnAdd);
		GuiButtonThemed btnMain;
		if(je == null) {
			return;
		}
		if(je.isJsonArray()) {
			btnMain = new GuiButtonJson<>(0, margin, 0, ctrlSpace - n, 20, je.getAsJsonArray(), false);
			btnList.add(btnMain);
		} else if(je.isJsonObject()) {
			JsonObject jo = je.getAsJsonObject();
			if(JsonHelper.isItem(jo) || JsonHelper.isFluid(jo) || JsonHelper.isEntity(jo)) {
				n += 20;
				GuiButtonThemed btnAdv = new GuiButtonThemed(1, px + width - n, 0, 20, 20, "...");
				btnList.add(btnAdv);
			}
			btnMain = new GuiButtonJson<>(0, margin, 0, ctrlSpace - n, 20, je.getAsJsonObject(), false);
			btnList.add(btnMain);
		} else if(je.isJsonPrimitive()) {
			JsonPrimitive jp = je.getAsJsonPrimitive();
			if(jp.isBoolean()) {
				btnMain = new GuiButtonJson<>(0, margin, 0, ctrlSpace - n, 20, jp, false);
				btnList.add(btnMain);
			} else if(jp.isNumber()) {
				GuiNumberField num = new GuiNumberField(mc.fontRendererObj, margin + 1, 0, ctrlSpace - n - 2, 18);
				num.setMaxStringLength(Integer.MAX_VALUE);
				num.setText(jp.getAsNumber().toString());
				txtMain = num;
			} else {
				GuiBigTextField txt = new GuiBigTextField(mc.fontRendererObj, margin + 1, 1, ctrlSpace - n - 2, 18);
				txt.setMaxStringLength(Integer.MAX_VALUE);
				txt.setText(jp.getAsString());
				if(json.isJsonObject()) {
					txt.enableBigEdit(new TextCallbackJsonObject(json.getAsJsonObject(), key));
				} else if(json.isJsonArray()) {
					txt.enableBigEdit(new TextCallbackJsonArray(json.getAsJsonArray(), idx));
				}
				txtMain = txt;
			}
		}
	}

	@Override
	public void drawBackground(int mx, int my, int px, int py, int width) {
		int margin = px + (width / 3);
		for(GuiButtonThemed btn : btnList) {
			btn.yPosition = py;
			btn.drawButton(mc, mx, my);
		}
		if(txtMain != null) {
			txtMain.yPosition = py + 1;
			txtMain.drawTextBox();
		}
		int length = mc.fontRendererObj.getStringWidth(name);
		mc.fontRendererObj.drawString(name, margin - 8 - length, py + 6, getTextColor(), false);
	}

	@Override
	public void drawForeground(int mx, int my) {}

	@Override
	public void onMouseClick(int mx, int my, int click) {
		if(txtMain != null) {
			txtMain.mouseClicked(mx, my, click);
		}
		for(GuiButtonThemed btn : btnList) {
			if(btn.mousePressed(mc, mx, my)) {
				btn.playPressSound(mc.getSoundHandler());
				onActionPerformed(btn);
			}
		}
	}

	public void onActionPerformed(GuiButtonThemed btn) {
		if(je != null && btn.id == 3) {
			if(json.isJsonObject()) {
				json.getAsJsonObject().remove(key);
				host.getEntryList().remove(this);
				host.refresh();
				return;
			} else if(json.isJsonArray()) {
				JsonHelper.GetUnderlyingArray(json.getAsJsonArray()).remove(idx);
				host.getEntryList().remove(this);
				host.refresh();
				return;
			}
		}
		if(btn.id == 2) {
			if(json.isJsonArray()) {
				mc.displayGuiScreen(new GuiJsonAdd(mc.currentScreen, json.getAsJsonArray(), idx));
			} else if(json.isJsonObject()) {
				mc.displayGuiScreen(new GuiJsonAdd(mc.currentScreen, json.getAsJsonObject()));
			}
		} else if(je != null && je.isJsonObject()) {
			JsonObject jo = je.getAsJsonObject();
			if(btn.id == 0) {
				if(!(JsonHelper.isItem(jo) || JsonHelper.isFluid(jo) || JsonHelper.isEntity(jo))) {
					mc.displayGuiScreen(new GuiJsonEditor(mc.currentScreen, jo));
				} else if(JsonHelper.isItem(jo)) {
					mc.displayGuiScreen(new GuiJsonItemSelection(mc.currentScreen, new JsonItemCallback(jo), JsonHelper.JsonToItemStack(jo)));
				} else if(JsonHelper.isFluid(jo)) {
					mc.displayGuiScreen(new GuiJsonFluidSelection(mc.currentScreen, new JsonFluidCallback(jo), JsonHelper.JsonToFluidStack(jo)));
				} else if(JsonHelper.isEntity(jo)) {
					mc.displayGuiScreen(new GuiJsonEntitySelection(mc.currentScreen, new JsonEntityCallback(jo), JsonHelper.JsonToEntity(jo, mc.theWorld)));
				}
			} else if(btn.id == 1) {
				mc.displayGuiScreen(new GuiJsonTypeMenu(mc.currentScreen, jo));
			}
		} else if(je != null && je.isJsonArray()) {
			mc.displayGuiScreen(new GuiJsonEditor(mc.currentScreen, je.getAsJsonArray()));
		} else if(je != null && je.isJsonPrimitive() && je.getAsJsonPrimitive().isBoolean()) {
			if(json.isJsonObject()) {
				json.getAsJsonObject().addProperty(key, !je.getAsBoolean());
				host.refresh();
			} else if(json.isJsonArray()) {
				JsonHelper.GetUnderlyingArray(json.getAsJsonArray()).set(idx, new JsonPrimitive(!je.getAsBoolean()));
				host.refresh();
			}
		}
	}

	public void onKeyTyped(char c, int keyCode) {
		if(txtMain != null) {
			txtMain.textboxKeyTyped(c, keyCode);
			if(json.isJsonArray()) {
				ArrayList<JsonElement> list = JsonHelper.GetUnderlyingArray(json.getAsJsonArray());
				if(txtMain instanceof GuiNumberField) {
					list.set(idx, new JsonPrimitive(((GuiNumberField) txtMain).getNumber()));
				} else {
					list.set(idx, new JsonPrimitive(txtMain.getText()));
				}
			} else if(json.isJsonObject()) {
				if(txtMain instanceof GuiNumberField) {
					json.getAsJsonObject().addProperty(key, ((GuiNumberField) txtMain).getNumber());
				} else {
					json.getAsJsonObject().addProperty(key, txtMain.getText());
				}
			}
		}
	}

	@Override
	public int getHeight() {
		return 20;
	}

	@Override
	public boolean canDrawOutsideBox(boolean isForeground) {
		return isForeground;
	}
}