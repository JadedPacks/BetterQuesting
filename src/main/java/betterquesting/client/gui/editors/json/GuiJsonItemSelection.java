package betterquesting.client.gui.editors.json;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiBigTextField;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.controls.GuiNumberField;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.editors.json.scrolling.GuiScrollingItemGrid;
import betterquesting.client.themes.ThemeStandard;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiJsonItemSelection extends GuiScreenThemed {
	private BigItemStack stackSelect;
	private final ICallback<BigItemStack> callback;
	private GuiBigTextField searchBox;
	private GuiNumberField numberBox;
	private GuiButtonThemed btnOreDict;
	private int oreDictIdx = 0;
	private GuiScrollingItemGrid itemGrid;
	public String searchTxt = "";
	public Iterator<Item> searching = null;

	public GuiJsonItemSelection(GuiScreen parent, ICallback<BigItemStack> callback, BigItemStack stack) {
		super(parent, "betterquesting.title.select_item");
		this.stackSelect = stack;
		this.callback = callback;
		if(stackSelect == null) {
			stackSelect = new BigItemStack(Blocks.stone);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.searchBox = new GuiBigTextField(this.fontRendererObj, guiLeft + sizeX / 2 + 9, guiTop + 33, sizeX / 2 - 26, 14);
		this.searchBox.setWatermark(I18n.format("betterquesting.gui.search"));
		this.searchBox.setMaxStringLength(Integer.MAX_VALUE);
		this.itemGrid = new GuiScrollingItemGrid(mc, guiLeft + sizeX / 2 + 8, guiTop + 48, sizeX / 2 - 24, sizeY - 80);
		this.embedded.add(itemGrid);
		numberBox = new GuiNumberField(fontRendererObj, guiLeft + 77, guiTop + 49, 98, 14);
		if(stackSelect != null) {
			numberBox.setText("" + stackSelect.stackSize);
		}
		searching = Item.itemRegistry.iterator();
		btnOreDict = new GuiButtonThemed(3, guiLeft + 76, guiTop + 64, 100, 20, "OreDict: " + (stackSelect.oreDict.length() <= 0 ? "NONE" : stackSelect.oreDict), true);
		this.buttonList.add(btnOreDict);
	}

	@Override
	public void drawBackPanel(int mx, int my, float partialTick) {
		super.drawBackPanel(mx, my, partialTick);
		ttStack = null;
		int btnWidth = sizeX / 2 - 16;
		GL11.glColor4f(1f, 1f, 1f, 1f);
		this.fontRendererObj.drawString(I18n.format("betterquesting.gui.selection"), guiLeft + 24, guiTop + 36, getTextColor(), false);
		this.fontRendererObj.drawString("x", guiLeft + 65, guiTop + 52, getTextColor(), false);
		this.mc.renderEngine.bindTexture(ThemeStandard.getGuiTexture());
		GL11.glPushMatrix();
		GL11.glScalef(2F, 2F, 1F);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		this.drawTexturedModalRect((guiLeft + 24) / 2, (guiTop + 48) / 2, 0, 48, 18, 18);
		if(this.stackSelect != null) {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderUtils.RenderItemStack(this.mc, this.stackSelect.getBaseStack(), (guiLeft + 26) / 2, (guiTop + 50) / 2, "");
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			if(this.isWithin(mx, my, 25, 49, 32, 32)) {
				ttStack = this.stackSelect;
			}
		}
		GL11.glPopMatrix();
		fontRendererObj.drawString(I18n.format("container.inventory"), this.guiLeft + 24, this.guiTop + sizeY / 2 - 12, getTextColor(), false);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		if(this.mc.thePlayer != null) {
			ItemStack[] invoStacks = this.mc.thePlayer.inventory.mainInventory;
			int isx = (18 * 9);
			int isy = (18 * 4);
			float scale = Math.min((btnWidth - 16) / (float) isx, (sizeY / 2F - 32) / isy);
			int ipx = guiLeft + 16 + btnWidth / 2 - (int) (isx / 2 * scale);
			int ipy = guiTop + sizeY / 2;
			GL11.glPushMatrix();
			GL11.glTranslatef(ipx, ipy, 0F);
			GL11.glScalef(scale, scale, 1F);
			for(int i = 0; i < invoStacks.length && i < 9 * 4; i++) {
				int x = i % 9 * 18;
				int y = (i - i % 9) / 9 * 18;
				this.mc.renderEngine.bindTexture(ThemeStandard.getGuiTexture());
				GL11.glColor4f(1F, 1F, 1F, 1F);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				this.drawTexturedModalRect(x, y, 0, 48, 18, 18);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				ItemStack stack = invoStacks[i];
				if(stack != null) {
					RenderUtils.RenderItemStack(mc, stack, x + 1, y + 1, "" + (stack.stackSize > 1 ? stack.stackSize : ""));
					if(isWithin(mx, my, ipx + (int) ((x + 1) * scale), ipy + (int) ((y + 1) * scale), (int) (16 * scale), (int) (16 * scale), false)) {
						ttStack = new BigItemStack(stack);
					}
				}
			}
			GL11.glPopMatrix();
		}
		RenderUtils.DrawLine(width / 2, guiTop + 32, width / 2, guiTop + sizeY - 32, 2F, getTextColor());
		this.searchBox.drawTextBox(mx, my);
		this.numberBox.drawTextBox();
	}

	private BigItemStack ttStack = null;

	@Override
	public void drawScreen(int mx, int my, float partialTick) {
		doSearch();
		super.drawScreen(mx, my, partialTick);
		if(ttStack != null) {
			GL11.glPushMatrix();
			try {
				this.drawHoveringText(ttStack.getBaseStack().getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips), mx, my, this.fontRendererObj);
			} catch(Exception e) {
				ttStack = null;
			}
			GL11.glColor4f(1f, 1f, 1f, 1f);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if(button.id == 0 && callback != null) {
			callback.setValue(stackSelect);
		} else if(button.id == 3) {
			if(stackSelect != null) {
				int[] oreId = OreDictionary.getOreIDs(stackSelect.getBaseStack());
				oreDictIdx += 1;
				if(oreId.length <= 0 || oreDictIdx >= oreId.length || oreDictIdx < -1) {
					oreDictIdx = -1;
					stackSelect.oreDict = "";
					button.displayString = "OreDict: NONE";
				} else {
					oreDictIdx %= oreId.length;
					stackSelect.oreDict = OreDictionary.getOreName(oreId[oreDictIdx]);
					button.displayString = "OreDict: " + stackSelect.oreDict;
				}
			}
		}
		super.actionPerformed(button);
	}

	@Override
	public void mouseClicked(int mx, int my, int type) {
		ItemStack gStack = itemGrid.getStackUnderMouse(mx, my);
		this.searchBox.mouseClicked(mx, my, type);
		this.numberBox.mouseClicked(mx, my, type);
		int btnWidth = sizeX / 2 - 16;
		int isx = (18 * 9);
		int isy = (18 * 4);
		float scale = Math.min((btnWidth - 16) / (float) isx, (sizeY / 2F - 32) / isy);
		int ipx = guiLeft + 16 + btnWidth / 2 - (int) (isx / 2 * scale);
		int ipy = guiTop + sizeY / 2;
		if(gStack != null) {
			this.stackSelect = new BigItemStack(gStack.copy());
			numberBox.setText("" + stackSelect.stackSize);
			oreDictIdx = -1;
			btnOreDict.displayString = "OreDict: NONE";
			return;
		} else if(this.mc.thePlayer != null && this.isWithin(mx, my, ipx, ipy, (int) (18 * 9 * scale), (int) (18 * 4 * scale), false)) {
			int idxSize = (int) (18 * scale);
			int sx = (mx - ipx) / idxSize;
			int sy = (my - ipy) / idxSize;
			int index = sx + (sy * 9);
			if(index >= 0 && index < this.mc.thePlayer.inventory.mainInventory.length) {
				ItemStack invoStack = this.mc.thePlayer.inventory.mainInventory[index];
				if(invoStack != null) {
					this.stackSelect = new BigItemStack(invoStack.copy());
					numberBox.setText("" + stackSelect.stackSize);
					oreDictIdx = -1;
					btnOreDict.displayString = "OreDict: NONE";
				}
			}
			return;
		} else if(!numberBox.isFocused() && stackSelect != null && stackSelect.stackSize != numberBox.getNumber().intValue()) {
			int i = Math.max(1, numberBox.getNumber().intValue());
			numberBox.setText("" + i);
			stackSelect.stackSize = i;
		}
		super.mouseClicked(mx, my, type);
	}

	public void doSearch() {
		if(searching == null) {
			return;
		} else if(!searching.hasNext()) {
			searching = null;
			return;
		}
		int pass = 0;
		while(searching.hasNext() && pass < 256) {
			Item baseItem = searching.next();
			if(baseItem == null) {
				continue;
			}
			pass++;
			ArrayList<ItemStack> subList = new ArrayList<>();
			if(baseItem == Items.enchanted_book) {
				for(Enchantment enchant : Enchantment.enchantmentsList) {
					if(enchant != null) {
						Items.enchanted_book.func_92113_a(enchant, subList);
					}
				}
			} else {
				try {
					baseItem.getSubItems(baseItem, CreativeTabs.tabAllSearch, subList);
				} catch(Exception e) {
					subList.add(new ItemStack(baseItem));
				}
			}
			if(baseItem.getUnlocalizedName() == null || Item.itemRegistry.getNameForObject(baseItem) == null) {
				continue;
			}
			if(baseItem.getUnlocalizedName().toLowerCase().contains(searchTxt) || StatCollector.translateToLocal(baseItem.getUnlocalizedName()).toLowerCase().contains(searchTxt) || Item.itemRegistry.getNameForObject(baseItem).toLowerCase().contains(searchTxt)) {
				itemGrid.getItemList().addAll(subList);
			} else {
				for(ItemStack subItem : subList) {
					try {
						if(subItem == null || subItem.getUnlocalizedName() == null) {
							continue;
						}
						if(subItem.getUnlocalizedName().toLowerCase().contains(searchTxt) || subItem.getDisplayName().toLowerCase().contains(searchTxt)) {
							itemGrid.getItemList().add(subItem);
						} else {
							List<String> toolTips = subItem.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
							for(String line : toolTips) {
								if(line.toLowerCase().contains(searchTxt)) {
									itemGrid.getItemList().add(subItem);
									break;
								}
							}
						}
					} catch(Exception ignored) {}
				}
			}
		}
	}

	@Override
	protected void keyTyped(char character, int num) {
		super.keyTyped(character, num);
		String prevTxt = searchBox.getText();
		searchBox.textboxKeyTyped(character, num);
		numberBox.textboxKeyTyped(character, num);
		if(!searchBox.getText().equalsIgnoreCase(prevTxt)) {
			itemGrid.getItemList().clear();
			searchTxt = searchBox.getText().toLowerCase();
			searching = Item.itemRegistry.iterator();
		}
	}
}