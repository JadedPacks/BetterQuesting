package betterquesting.client.gui.editors.json;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiBigTextField;
import betterquesting.api.client.gui.controls.GuiNumberField;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.editors.json.scrolling.GuiScrollingFluidGrid;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;

@SideOnly(Side.CLIENT)
public class GuiJsonFluidSelection extends GuiScreenThemed {
	private FluidStack stackSelect;
	private final ICallback<FluidStack> callback;
	private GuiBigTextField searchBox;
	private GuiNumberField numberBox;
	private GuiScrollingFluidGrid fluidGrid;
	private FluidStack ttStack;
	Iterator<Fluid> searching = null;
	String searchTxt = "";

	public GuiJsonFluidSelection(GuiScreen parent, ICallback<FluidStack> callback, FluidStack stack) {
		super(parent, "betterquesting.title.select_fluid");
		this.stackSelect = stack;
		this.callback = callback;
		if(stackSelect == null) {
			stackSelect = new FluidStack(FluidRegistry.WATER, 1000);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.searchBox = new GuiBigTextField(fontRendererObj, guiLeft + sizeX / 2 + 9, guiTop + 33, sizeX / 2 - 26, 14);
		this.searchBox.setWatermark(I18n.format("betterquesting.gui.search"));
		this.searchBox.setMaxStringLength(Integer.MAX_VALUE);
		this.fluidGrid = new GuiScrollingFluidGrid(mc, guiLeft + sizeX / 2 + 8, guiTop + 48, sizeX / 2 - 24, sizeY - 80);
		this.embedded.add(fluidGrid);
		numberBox = new GuiNumberField(fontRendererObj, guiLeft + 76, guiTop + 57, 100, 16);
		if(stackSelect != null) {
			numberBox.setText("" + stackSelect.amount);
		}
		searching = FluidRegistry.getRegisteredFluids().values().iterator();
	}

	@Override
	public void drawBackPanel(int mx, int my, float partialTick) {
		super.drawBackPanel(mx, my, partialTick);
		ttStack = null;
		int btnWidth = sizeX / 2 - 16;
		GL11.glColor4f(1f, 1f, 1f, 1f);
		this.fontRendererObj.drawString(I18n.format("betterquesting.gui.selection"), guiLeft + 24, guiTop + 36, getTextColor(), false);
		this.fontRendererObj.drawString("x", guiLeft + 64, guiTop + 60, getTextColor(), false);
		this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		GL11.glPushMatrix();
		GL11.glScalef(2F, 2F, 1F);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		this.drawTexturedModalRect((guiLeft + 24) / 2, (guiTop + 48) / 2, 0, 48, 18, 18);
		if(this.stackSelect != null) {
			mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			GL11.glColor4f(1F, 1F, 1F, 1F);
			try {
				if(stackSelect.getFluid().getIcon() != null) {
					RenderUtils.itemRender.renderIcon((guiLeft + 26) / 2, (guiTop + 50) / 2, stackSelect.getFluid().getIcon(), 16, 16);
				} else {
					IIcon missing = ((TextureMap) mc.renderEngine.getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
					RenderUtils.itemRender.renderIcon((guiLeft + 26) / 2, (guiTop + 50) / 2, missing, 16, 16);
				}
			} catch(Exception ignored) {}
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
				this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				this.drawTexturedModalRect(x, y, 0, 48, 18, 18);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				ItemStack stack = invoStacks[i];
				if(stack != null) {
					RenderUtils.RenderItemStack(mc, stack, x + 1, y + 1, "" + (stack.stackSize > 1 ? stack.stackSize : ""));
					if(isWithin(mx, my, ipx + (int) ((x + 1) * scale), ipy + (int) ((y + 1) * scale), (int) (16 * scale), (int) (16 * scale), false) && FluidContainerRegistry.isFilledContainer(stack)) {
						ttStack = FluidContainerRegistry.getFluidForFilledItem(stack);
					}
				}
			}
			GL11.glPopMatrix();
		}
		RenderUtils.DrawLine(width / 2, guiTop + 32, width / 2, guiTop + sizeY - 32, 2F, getTextColor());
		this.searchBox.drawTextBox(mx, my, partialTick);
		this.numberBox.drawTextBox();
	}

	@Override
	public void drawScreen(int mx, int my, float partialTick) {
		doSearch();
		super.drawScreen(mx, my, partialTick);
		if(ttStack != null) {
			ArrayList<String> tTip = new ArrayList<>();
			tTip.add(ttStack.getLocalizedName());
			tTip.add(EnumChatFormatting.GRAY + "" + ttStack.amount + " mB");
			this.drawTooltip(tTip, mx, my);
		}
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if(button.id == 0 && callback != null) {
			callback.setValue(stackSelect);
		}
		super.actionPerformed(button);
	}

	@Override
	public void mouseClicked(int mx, int my, int type) {
		FluidStack gStack = fluidGrid.getStackUnderMouse(mx, my);
		this.searchBox.mouseClicked(mx, my, type);
		this.numberBox.mouseClicked(mx, my, type);
		int btnWidth = sizeX / 2 - 16;
		int isx = (18 * 9);
		int isy = (18 * 4);
		float scale = Math.min((btnWidth - 16) / (float) isx, (sizeY / 2F - 32) / isy);
		int ipx = guiLeft + 16 + btnWidth / 2 - (int) (isx / 2 * scale);
		int ipy = guiTop + sizeY / 2;
		if(gStack != null) {
			this.stackSelect = gStack.copy();
			numberBox.setText("" + stackSelect.amount);
		} else if(this.mc.thePlayer != null && this.isWithin(mx, my, ipx, ipy, (int) (18 * 9 * scale), (int) (18 * 4 * scale), false)) {
			int idxSize = (int) (18 * scale);
			int sx = (mx - ipx) / idxSize;
			int sy = (my - ipy) / idxSize;
			int index = sx + (sy * 9);
			if(index >= 0 && index < this.mc.thePlayer.inventory.mainInventory.length) {
				ItemStack invoStack = this.mc.thePlayer.inventory.mainInventory[index];
				if(FluidContainerRegistry.isFilledContainer(invoStack)) {
					this.stackSelect = FluidContainerRegistry.getFluidForFilledItem(invoStack).copy();
					numberBox.setText("" + stackSelect.amount);
				}
			}
		} else if(!numberBox.isFocused() && stackSelect != null && stackSelect.amount != numberBox.getNumber().intValue()) {
			int i = Math.max(1, numberBox.getNumber().intValue());
			numberBox.setText("" + i);
			stackSelect.amount = i;
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
			Fluid baseFluid = searching.next();
			if(baseFluid == null) {
				continue;
			}
			pass++;
			if(baseFluid.getUnlocalizedName() == null || FluidRegistry.getDefaultFluidName(baseFluid) == null) {
				continue;
			}
			if(baseFluid.getUnlocalizedName().toLowerCase().contains(searchTxt) || StatCollector.translateToLocal(baseFluid.getUnlocalizedName()).toLowerCase().contains(searchTxt) || FluidRegistry.getDefaultFluidName(baseFluid).toLowerCase().contains(searchTxt)) {
				fluidGrid.getFluidList().add(new FluidStack(baseFluid, 1000));
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
			fluidGrid.getFluidList().clear();
			searchTxt = searchBox.getText().toLowerCase();
			searching = FluidRegistry.getRegisteredFluids().values().iterator();
		}
	}
}