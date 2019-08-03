package betterquesting.api.client.gui.lists;

import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import net.minecraft.client.Minecraft;

import java.util.List;

public class GuiScrollingButtons extends GuiScrollingBase<GuiScrollingButtons.ScrollingEntryButtonRow> {
	public GuiScrollingButtons(Minecraft mc, int x, int y, int w, int h) {
		super(mc, x, y, w, h);
	}

	public void addButtonRow(GuiButtonThemed... buttons) {
		this.getEntryList().add(new ScrollingEntryButtonRow(buttons));
	}

	public GuiButtonThemed getButtonUnderMouse(int mx, int my) {
		int idx = this.getEntryUnderMouse(mx, my);
		if(idx < 0) {
			return null;
		}
		for(GuiButtonThemed btn : this.getEntryList().get(idx).getButtons()) {
			if(isWithin(mx, my, btn.xPosition, btn.yPosition, btn.width, btn.height)) {
				return btn;
			}
		}
		return null;
	}

	public static class ScrollingEntryButtonRow extends GuiElement implements IScrollingEntry {
		private final GuiButtonThemed[] buttons;

		private ScrollingEntryButtonRow(GuiButtonThemed... buttons) {
			this.buttons = buttons;
		}

		public GuiButtonThemed[] getButtons() {
			return buttons;
		}

		@Override
		public void drawBackground(int mx, int my, int px, int py, int width) {
			int n = 0;
			for(GuiButtonThemed btn : buttons) {
				btn.xPosition = px + n;
				btn.yPosition = py;
				btn.drawButton(btn.mc, mx, my);
				n += btn.width;
			}
		}

		@Override
		public void drawForeground(int mx, int my) {
			for(GuiButtonThemed btn : buttons) {
				if(!isWithin(mx, my, btn.xPosition, btn.yPosition, btn.width, btn.height)) {
					continue;
				}
				List<String> tooltip = btn.getTooltip();
				if(tooltip != null && tooltip.size() > 0) {
					this.drawTooltip(tooltip, mx, my, btn.mc.fontRendererObj);
					break;
				}
			}
		}

		@Override
		public void onMouseClick(int mx, int my, int click) {}

		@Override
		public int getHeight() {
			int max = 0;
			for(GuiButtonThemed btn : buttons) {
				if(btn.height > max) {
					max = btn.height;
				}
			}
			return max;
		}

		@Override
		public boolean canDrawOutsideBox(boolean isForeground) {
			return isForeground;
		}
	}
}