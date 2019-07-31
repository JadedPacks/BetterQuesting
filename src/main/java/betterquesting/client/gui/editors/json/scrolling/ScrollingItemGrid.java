package betterquesting.client.gui.editors.json.scrolling;

import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.lists.IScrollingEntry;
import betterquesting.api.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ScrollingItemGrid extends GuiElement implements IScrollingEntry {
	private final List<ItemStack> itemList = new ArrayList<>();
	private final Minecraft mc;
	private int posX = 0;
	private int posY = 0;
	private int columns = 1;
	private int rows = 1;
	private final int upperBounds;
	private final int lowerBounds;

	public ScrollingItemGrid(int top, int bottom) {
		this.mc = Minecraft.getMinecraft();
		this.upperBounds = top;
		this.lowerBounds = bottom;
	}

	public List<ItemStack> getItemList() {
		return itemList;
	}

	@Override
	public void drawBackground(int mx, int my, int px, int py, int width) {
		this.posX = px;
		this.posY = py;
		columns = Math.max(1, width / 18);
		rows = MathHelper.ceiling_float_int(itemList.size() / (float) columns);
		int sr = (upperBounds - py) / 18;
		int er = MathHelper.ceiling_float_int((lowerBounds - py) / 18F);
		for(int j = sr; j < rows && j < er; j++) {
			for(int i = 0; i < columns; i++) {
				int idx = (j * columns) + i;
				if(idx >= itemList.size()) {
					break;
				}
				ItemStack stack = itemList.get(idx);
				GL11.glPushMatrix();
				GL11.glColor4f(1F, 1F, 1F, 1F);
				mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
				drawTexturedModalRect(px + i * 18, py + j * 18, 0, 48, 18, 18);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				RenderUtils.RenderItemStack(mc, stack, px + i * 18 + 1, py + j * 18 + 1, stack.stackSize > 1 ? "" + stack.stackSize : "");
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				GL11.glPopMatrix();
			}
		}
	}

	@Override
	public void drawForeground(int mx, int my, int px, int py, int width) {
		ItemStack stack = getStackUnderMouse(mx, my);
		if(stack != null) {
			this.drawTooltip(stack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips), mx, my, mc.fontRendererObj);
		}
	}

	@Override
	public void onMouseClick(int mx, int my, int px, int py, int click, int index) {}

	public ItemStack getStackUnderMouse(int mx, int my) {
		int idx = mouseToIndex(mx, my);
		if(idx < 0 || idx >= itemList.size()) {
			return null;
		}
		return itemList.get(idx);
	}

	private int mouseToIndex(int mx, int my) {
		int ii = (mx - posX) / 18;
		int jj = (my - posY) / 18;
		if(ii >= columns || jj >= rows) {
			return -1;
		}
		return (jj * columns) + ii;
	}

	@Override
	public int getHeight() {
		return rows * 18;
	}

	@Override
	public boolean canDrawOutsideBox(boolean isForeground) {
		return isForeground;
	}
}