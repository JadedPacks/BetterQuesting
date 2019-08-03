package betterquesting.client.gui.editors.json;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.awt.*;

@SideOnly(Side.CLIENT)
public class GuiJsonAdd extends GuiScreenThemed implements IVolatileScreen {
	private GuiTextField keyText;
	private final JsonElement json;
	private int select = 0, insertIdx = 0;

	public GuiJsonAdd(GuiScreen parent, JsonArray json, int insertIdx) {
		this(parent, json);
		this.insertIdx = insertIdx;
	}

	public GuiJsonAdd(GuiScreen parent, JsonObject json) {
		this(parent, (JsonElement) json);
	}

	private GuiJsonAdd(GuiScreen parent, JsonElement json) {
		super(parent, "betterquesting.title.json_add");
		this.json = json;
	}

	@Override
	public void initGui() {
		super.initGui();
		((GuiButton) this.buttonList.get(0)).xPosition = this.guiLeft + this.sizeX / 2 - 100;
		((GuiButton) this.buttonList.get(0)).width = 100;
		this.buttonList.add(new GuiButtonThemed(1, this.guiLeft + this.sizeX / 2, this.guiTop + this.sizeY - 16, 100, 20, I18n.format("gui.cancel"), true));
		int btnOff = -20;
		if(json.isJsonObject()) {
			keyText = new GuiTextField(this.fontRendererObj, this.guiLeft + this.sizeX / 2 - 100, this.guiTop + this.sizeY / 2 - 48, 200, 16);
			keyText.setMaxStringLength(Integer.MAX_VALUE);
			((GuiButton) this.buttonList.get(0)).enabled = false;
			btnOff = 0;
		}
		GuiButtonThemed buttonStr = new GuiButtonThemed(2, this.guiLeft + this.sizeX / 2 - 100, this.guiTop + this.sizeY / 2 - 20 + btnOff, 200, 20, I18n.format("betterquesting.btn.text"), true);
		GuiButtonThemed buttonNum = new GuiButtonThemed(3, this.guiLeft + this.sizeX / 2 - 100, this.guiTop + this.sizeY / 2 + btnOff, 200, 20, I18n.format("betterquesting.btn.number"), true);
		GuiButtonThemed buttonObj = new GuiButtonThemed(4, this.guiLeft + this.sizeX / 2 - 100, this.guiTop + this.sizeY / 2 + 20 + btnOff, 100, 20, I18n.format("betterquesting.btn.object"), true);
		GuiButtonThemed buttonArr = new GuiButtonThemed(5, this.guiLeft + this.sizeX / 2, this.guiTop + this.sizeY / 2 + 20 + btnOff, 100, 20, I18n.format("betterquesting.btn.list"), true);
		GuiButtonThemed buttonEnt = new GuiButtonThemed(8, this.guiLeft + this.sizeX / 2 - 100, this.guiTop + this.sizeY / 2 + 40 + btnOff, 200, 20, I18n.format("betterquesting.btn.entity"), true);
		GuiButtonThemed buttonItm = new GuiButtonThemed(6, this.guiLeft + this.sizeX / 2 - 100, this.guiTop + this.sizeY / 2 + 60 + btnOff, 100, 20, I18n.format("betterquesting.btn.item"), true);
		GuiButtonThemed buttonFlu = new GuiButtonThemed(7, this.guiLeft + this.sizeX / 2, this.guiTop + this.sizeY / 2 + 60 + btnOff, 100, 20, I18n.format("betterquesting.btn.fluid"), true);
		buttonStr.enabled = false;
		this.buttonList.add(buttonStr);
		this.buttonList.add(buttonNum);
		this.buttonList.add(buttonObj);
		this.buttonList.add(buttonArr);
		this.buttonList.add(buttonItm);
		this.buttonList.add(buttonFlu);
		this.buttonList.add(buttonEnt);
	}

	@Override
	public void drawScreen(int mx, int my, float partialTick) {
		super.drawScreen(mx, my, partialTick);
		if(keyText != null) {
			keyText.drawTextBox();
		}
		if(keyText != null) {
			String txt = I18n.format("betterquesting.gui.key");
			mc.fontRendererObj.drawString(txt, this.guiLeft + (sizeX / 2) - this.fontRendererObj.getStringWidth(txt) / 2, this.guiTop + 52, getTextColor(), false);
			if(keyText.getText().length() <= 0) {
				txt = I18n.format("betterquesting.gui.no_key");
				mc.fontRendererObj.drawString(EnumChatFormatting.BOLD + txt, this.guiLeft + (sizeX / 2) - this.fontRendererObj.getStringWidth(txt) / 2, this.guiTop + this.sizeY / 2 - 30, Color.RED.getRGB(), false);
			} else if(json.getAsJsonObject().has(keyText.getText())) {
				txt = I18n.format("betterquesting.gui.duplicate_key");
				mc.fontRendererObj.drawString(EnumChatFormatting.BOLD + txt, this.guiLeft + (sizeX / 2) - this.fontRendererObj.getStringWidth(txt) / 2, this.guiTop + this.sizeY / 2 - 30, Color.RED.getRGB(), false);
			}
		}
	}

	@Override
	protected void keyTyped(char character, int num) {
		super.keyTyped(character, num);
		if(keyText != null) {
			keyText.textboxKeyTyped(character, num);
			if(keyText.getText().length() <= 0 || json.getAsJsonObject().has(keyText.getText())) {
				keyText.setTextColor(Color.RED.getRGB());
				((GuiButton) this.buttonList.get(0)).enabled = false;
			} else {
				keyText.setTextColor(Color.WHITE.getRGB());
				((GuiButton) this.buttonList.get(0)).enabled = true;
			}
		} else {
			((GuiButton) this.buttonList.get(0)).enabled = true;
		}
	}

	@Override
	public void mouseClicked(int x, int y, int type) {
		super.mouseClicked(x, y, type);
		if(keyText != null) {
			this.keyText.mouseClicked(x, y, type);
		}
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if(button.id == 0 || button.id == 1) {
			if(button.id == 0) {
				JsonElement newObj = null;
				switch(select) {
					case 0: {
						newObj = new JsonPrimitive("");
						break;
					}
					case 1: {
						newObj = new JsonPrimitive(0);
						break;
					}
					case 2: {
						newObj = new JsonObject();
						break;
					}
					case 3: {
						newObj = new JsonArray();
						break;
					}
					case 4: {
						BigItemStack stack = new BigItemStack(Blocks.stone);
						newObj = JsonHelper.ItemStackToJson(stack, new JsonObject());
						break;
					}
					case 5: {
						FluidStack fluid = new FluidStack(FluidRegistry.WATER, 1000);
						newObj = JsonHelper.FluidStackToJson(fluid, new JsonObject());
						break;
					}
					case 6: {
						Entity entity = new EntityPig(mc.theWorld);
						newObj = JsonHelper.EntityToJson(entity, new JsonObject());
						break;
					}
				}
				if(newObj != null) {
					if(json.isJsonObject()) {
						json.getAsJsonObject().add(keyText.getText(), newObj);
					} else if(json.isJsonArray()) {
						JsonHelper.GetUnderlyingArray(json.getAsJsonArray()).add(insertIdx, newObj);
					}
				} else {
					return;
				}
			}
			this.mc.displayGuiScreen(parent);
		} else if(button.id <= 8) {
			((GuiButton) this.buttonList.get(this.select + 2)).enabled = true;
			button.enabled = false;
			this.select = button.id - 2;
		}
	}
}