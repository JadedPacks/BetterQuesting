package betterquesting.client.gui.editors.json.scrolling;

import betterquesting.api.client.gui.lists.GuiScrollingBase;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GuiScrollingItemGrid extends GuiScrollingBase<ScrollingItemGrid> {
	private final int posY, height;

	public GuiScrollingItemGrid(Minecraft mc, int x, int y, int w, int h) {
		super(mc, x, y, w, h);
		this.posY = y;
		this.height = h;
		this.getEntryList().add(new ScrollingItemGrid(y, y + h));
	}

	@Override
	public void drawBackground(int mx, int my, float partialTick) {
		super.drawBackground(mx, my, partialTick);
		if(this.getEntryList().size() <= 0) {
			this.getEntryList().add(new ScrollingItemGrid(posY, posY + height));
		} else if(this.getEntryList().size() > 1) {
			while(this.getEntryList().size() > 1) {
				this.getEntryList().remove(1);
			}
		}
	}

	public List<ItemStack> getItemList() {
		if(this.getEntryList().size() > 0) {
			return this.getEntryList().get(0).getItemList();
		}
		return new ArrayList<>();
	}

	public ItemStack getStackUnderMouse(int mx, int my) {
		int idx = this.getEntryUnderMouse(mx, my);
		if(idx >= 0) {
			return this.getEntryList().get(idx).getStackUnderMouse(mx, my);
		}
		return null;
	}
}