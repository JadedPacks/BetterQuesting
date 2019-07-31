package betterquesting.client.gui.editors.json;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiBigTextField;
import betterquesting.api.client.gui.controls.GuiButtonJson;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.controls.GuiNumberField;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.core.BetterQuesting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

@Deprecated
@SideOnly(Side.CLIENT)
public class GuiJsonObject extends GuiScreenThemed implements IVolatileScreen {
	private int scrollPos = 0, maxRows = 0;
	private final JsonObject settings;
	private boolean allowEdit = true;
	private final IJsonDoc jdoc;
	private HashMap<String, JsonControlSet> editables = new HashMap<>();

	@Deprecated
	public GuiJsonObject(GuiScreen parent, JsonObject settings) {
		this(parent, settings, null);
	}

	public GuiJsonObject(GuiScreen parent, JsonObject settings, IJsonDoc jdoc) {
		super(parent, "betterquesting.title.json_object");
		this.settings = settings;
		this.jdoc = jdoc;
	}

	public GuiJsonObject SetEditMode(boolean state) {
		this.allowEdit = state;
		return this;
	}

	@Override
	public void initGui() {
		super.initGui();
		if(jdoc != null) {
			String ulTitle = jdoc.getUnlocalisedTitle();
			String lTitle = I18n.format(ulTitle);
			if(!ulTitle.equals(lTitle)) {
				this.setTitle(I18n.format(jdoc.getUnlocalisedTitle()));
			}
		}
		editables = new HashMap<>();
		maxRows = (this.sizeY - 84) / 20;
		((GuiButton) this.buttonList.get(0)).xPosition = this.width / 2 - 100;
		((GuiButton) this.buttonList.get(0)).width = 100;
		this.buttonList.add(new GuiButtonThemed(1, this.width / 2, this.guiTop + this.sizeY - 16, 100, 20, I18n.format("betterquesting.btn.new"), true));
		this.buttonList.add(new GuiButtonThemed(2, this.width / 2, this.guiTop + 32 + (maxRows * 20), 20, 20, "<", true));
		this.buttonList.add(new GuiButtonThemed(3, this.guiLeft + this.sizeX - 36, this.guiTop + 32 + (maxRows * 20), 20, 20, ">", true));
		Keyboard.enableRepeatEvents(true);
		for(Entry<String, JsonElement> entry : settings.entrySet()) {
			if(entry.getValue().isJsonPrimitive()) {
				JsonPrimitive jPrim = entry.getValue().getAsJsonPrimitive();
				GuiTextField txtBox;
				if(jPrim.isNumber()) {
					txtBox = new GuiNumberField(this.fontRendererObj, 32, -9999, 128, 16);
					txtBox.setText("" + jPrim.getAsNumber());
				} else if(jPrim.isBoolean()) {
					GuiButtonJson<JsonPrimitive> button = new GuiButtonJson<>(buttonList.size(), -9999, -9999, 128, 20, jPrim, true);
					this.buttonList.add(button);
					editables.put(entry.getKey(), new JsonControlSet(this.buttonList, button, false, allowEdit));
					continue;
				} else {
					txtBox = new GuiBigTextField(this.fontRendererObj, 32, -9999, 128, 16).enableBigEdit(new TextCallbackJsonObject(settings, entry.getKey()));
					txtBox.setMaxStringLength(Integer.MAX_VALUE);
					txtBox.setText(jPrim.getAsString());
				}
				editables.put(entry.getKey(), new JsonControlSet(this.buttonList, txtBox, false, allowEdit));
			} else {
				GuiButtonJson<JsonElement> button = new GuiButtonJson<>(buttonList.size(), -9999, -9999, 128, 20, entry.getValue(), true);
				this.buttonList.add(button);
				editables.put(entry.getKey(), new JsonControlSet(this.buttonList, button, false, allowEdit));
			}
		}
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if(button.id == 0) {
			this.mc.displayGuiScreen(parent);
		} else if(button.id == 1) {
			this.mc.displayGuiScreen(new GuiJsonAdd(this, this.settings));
		} else if(button.id == 2) {
			if(scrollPos > 0) {
				scrollPos -= 1;
			} else {
				scrollPos = 0;
			}
		} else if(button.id == 3) {
			int maxShow = (this.sizeY - 84) / 20;
			if((scrollPos + 1) * maxShow < editables.size()) {
				scrollPos += 1;
			}
		} else {
			for(String key : editables.keySet()) {
				JsonControlSet controls = editables.get(key);
				if(controls == null) {
					continue;
				}
				if(button == controls.removeButton) {
					settings.remove(key);
					this.buttonList.remove(controls.jsonDisplay);
					this.buttonList.remove(controls.addButton);
					this.buttonList.remove(controls.removeButton);
					editables.remove(key);
					break;
				} else if(button == controls.jsonDisplay && button instanceof GuiButtonJson) {
					GuiButtonJson<JsonElement> jsonButton = (GuiButtonJson<JsonElement>) button;
					JsonElement element = jsonButton.getStored();
					IJsonDoc childDoc = jdoc == null ? null : jdoc.getChildDoc(key);
					if(jsonButton.isItem() || jsonButton.isEntity() || jsonButton.isFluid()) {
						this.mc.displayGuiScreen(new GuiJsonTypeMenu(this, element.getAsJsonObject()));
					} else if(element.isJsonObject()) {
						this.mc.displayGuiScreen(new GuiJsonObject(this, element.getAsJsonObject(), childDoc).SetEditMode(this.allowEdit));
					} else if(element.isJsonArray()) {
						this.mc.displayGuiScreen(new GuiJsonArray(this, element.getAsJsonArray(), childDoc).SetEditMode(this.allowEdit));
					} else if(element.isJsonPrimitive()) {
						if(element.getAsJsonPrimitive().isBoolean()) {
							JsonPrimitive jBool = new JsonPrimitive(!element.getAsBoolean());
							settings.add(key, jBool);
							jsonButton.displayString = "" + jBool.getAsBoolean();
							jsonButton.setStored(jBool);
						}
					}
					break;
				}
			}
		}
	}

	@Override
	public void drawScreen(int mx, int my, float partialTick) {
		super.drawScreen(mx, my, partialTick);
		String[] keys = editables.keySet().toArray(new String[] {});
		String keyDesc = null;
		for(int i = 0; i < keys.length; i++) {
			JsonControlSet controls = editables.get(keys[i]);
			if(controls == null) {
				continue;
			}
			int n = i - (scrollPos * maxRows);
			int posX = this.guiLeft + (sizeX / 2);
			int posY;
			if(n >= 0 && n < maxRows) {
				posY = this.guiTop + 32 + (n * 20);
				controls.drawControls(this, posX, posY, sizeX / 2 - 16, 20, mx, my, partialTick);
			} else {
				controls.Disable();
				continue;
			}
			String keyName = keys[i];
			if(jdoc != null) {
				String ulKey = jdoc.getUnlocalisedName(keys[i]);
				String lKey = I18n.format(ulKey);
				if(!lKey.equalsIgnoreCase(ulKey)) {
					keyName = lKey;
					if(this.isWithin(mx, my, this.guiLeft, posY, sizeX / 2, 20, false)) {
						keyDesc = I18n.format(jdoc.getUnlocalisedDesc(keys[i]));
					}
				}
			}
			this.fontRendererObj.drawString(keyName, this.guiLeft + (sizeX / 2) - this.fontRendererObj.getStringWidth(keyName) - 8, posY + 4, getTextColor(), false);
		}
		int mxPage = Math.max(MathHelper.ceiling_float_int(editables.size() / (float) maxRows), 1);
		String txt = (scrollPos + 1) + "/" + mxPage;
		this.fontRendererObj.drawString(txt, guiLeft + 16 + (sizeX - 32) / 4 * 3 - this.fontRendererObj.getStringWidth(txt) / 2, guiTop + 32 + (maxRows * 20) + 6, getTextColor());
		if(keyDesc != null) {
			List<String> tooltip = new ArrayList<>();
			tooltip.add(keyDesc);
			this.drawTooltip(tooltip, mx, my);
		}
	}

	@Override
	public void mouseClicked(int x, int y, int type) {
		super.mouseClicked(x, y, type);
		for(JsonControlSet controls : editables.values()) {
			controls.mouseClick(this, x, y, type);
		}
	}

	@Override
	protected void keyTyped(char character, int num) {
		super.keyTyped(character, num);
		for(Entry<String, JsonControlSet> entry : editables.entrySet()) {
			if(entry.getValue().jsonDisplay instanceof GuiTextField) {
				GuiTextField textField = (GuiTextField) entry.getValue().jsonDisplay;
				textField.textboxKeyTyped(character, num);
				if(settings.getAsJsonPrimitive(entry.getKey()).isNumber()) {
					if(textField instanceof GuiNumberField) {
						settings.add(entry.getKey(), new JsonPrimitive(((GuiNumberField) textField).getNumber()));
					} else {
						try {
							settings.add(entry.getKey(), new JsonPrimitive(NumberFormat.getInstance().parse(textField.getText())));
						} catch(Exception e) {
							BetterQuesting.logger.error("Unable to parse number format for JsonObject!", e);
							settings.add(entry.getKey(), new JsonPrimitive(textField.getText()));
						}
					}
				} else {
					settings.add(entry.getKey(), new JsonPrimitive(textField.getText()));
				}
			}
		}
	}
}