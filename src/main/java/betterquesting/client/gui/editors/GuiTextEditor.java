package betterquesting.client.gui.editors;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingText;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.misc.ICallback;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiTextEditor extends GuiScreenThemed implements IVolatileScreen {
	private ICallback<String> host;
	private String text;
	private int listScroll = 0, maxRows = 0, cursorPosition;
	private GuiScrollingText scrollingText;

	public GuiTextEditor(GuiScreen parent, String text) {
		super(parent, "betterquesting.title.edit_text");
		this.text = text;
	}

	public void setHost(ICallback<String> host) {
		this.host = host;
	}

	public void initGui() {
		super.initGui();
		maxRows = (sizeY - 48) / 20;
		for(int i = 0; i < maxRows; i++) {
			GuiButtonThemed btn = new GuiButtonThemed(i + 1, guiLeft + 16, guiTop + 32 + (i * 20), 100, 20, "NULL", true);
			this.buttonList.add(btn);
		}
		scrollingText = new GuiScrollingText(mc, guiLeft + 132, guiTop + 32, sizeX - 148, sizeY - 64);
		scrollingText.SetText(text);
		this.embedded.add(scrollingText);
		cursorPosition = text.length();
		RefreshColumns();
	}

	@Override
	public void actionPerformed(GuiButton btn) {
		super.actionPerformed(btn);
		if(btn.id > 0) {
			int n1 = btn.id - 1,
				n2 = n1 / maxRows,
				n3 = n1 % maxRows + listScroll;
			if(n2 == 0) {
				if(n3 >= 0 && n3 < EnumChatFormatting.values().length) {
					String tmp = EnumChatFormatting.values()[n3].toString();
					writeText(tmp);
				}
			}
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		if(host != null) {
			host.setValue(text);
		}
	}

	public void moveCursorBy(int p_146182_1_) {
		this.setCursorPosition(this.cursorPosition + p_146182_1_);
	}

	public void setCursorPosition(int p_146190_1_) {
		this.cursorPosition = p_146190_1_;
		int j = this.text.length();
		if(this.cursorPosition < 0) {
			this.cursorPosition = 0;
		}
		if(this.cursorPosition > j) {
			this.cursorPosition = j;
		}
		this.setSelectionPos(this.cursorPosition);
	}

	public void setCursorPositionZero() {
		this.setCursorPosition(0);
	}

	public void setCursorPositionEnd() {
		this.setCursorPosition(this.text.length());
	}

	public void setSelectionPos(int cursorPosition) {
		int j = this.text.length();
		if(cursorPosition > j) {
			this.cursorPosition = j;
		}
		if(cursorPosition < 0) {
			this.cursorPosition = 0;
		}
	}

	public void deleteFromCursor(int p_146175_1_) {
		if(this.text.length() != 0) {
			boolean flag = p_146175_1_ < 0;
			int j = flag ? this.cursorPosition + p_146175_1_ : this.cursorPosition;
			int k = flag ? this.cursorPosition : this.cursorPosition + p_146175_1_;
			String s = "";
			if(j >= 0) {
				s = this.text.substring(0, j);
			}
			if(k < this.text.length()) {
				s = s + this.text.substring(k);
			}
			this.text = s;
			if(flag) {
				this.moveCursorBy(p_146175_1_);
			}
		}
	}

	@Override
	public void keyTyped(char p_146201_1_, int p_146201_2_) {
		switch(p_146201_1_) {
			case 1:
				this.setCursorPositionEnd();
				this.setSelectionPos(0);
				return;
			case 22:
				this.writeText(GuiScreen.getClipboardString());
				return;
			default:
				switch(p_146201_2_) {
					case 14:
						this.deleteFromCursor(-1);
						return;
					case 28:
					case 156:
						this.writeText("\n");
						return;
					case 199:
						if(GuiScreen.isShiftKeyDown()) {
							this.setSelectionPos(0);
						} else {
							this.setCursorPositionZero();
						}

						return;
					case 203:
						this.moveCursorBy(-1);
						return;
					case 205:
						this.moveCursorBy(1);
						return;
					case 207:
						if(GuiScreen.isShiftKeyDown()) {
							this.setSelectionPos(this.text.length());
						} else {
							this.setCursorPositionEnd();
						}

						return;
					case 211:
						this.deleteFromCursor(1);
						return;
					default:
						this.writeText(ChatAllowedCharacters.filterAllowedCharacters(Character.toString(p_146201_1_)));
				}
		}
	}

	public void writeText(String raw) {
		String s1 = "";
		int i = this.cursorPosition;
		if(this.text.length() > 0) {
			s1 = s1 + this.text.substring(0, i);
		}
		int l;
		s1 = s1 + raw;
		l = raw.length();
		if(this.text.length() > 0 && i < this.text.length()) {
			s1 = s1 + this.text.substring(i);
		}
		this.text = s1;
		this.moveCursorBy(l);
	}

	public void drawScreen(int mx, int my, float partialTick) {
		super.drawScreen(mx, my, partialTick);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(guiLeft + 116, this.guiTop + 32, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20) {
			this.drawTexturedModalRect(guiLeft + 116, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + 116, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + 116, this.guiTop + 32 + (int) Math.max(0, s * (float) listScroll / (EnumChatFormatting.values().length - maxRows)), 248, 60, 8, 20);
		String s1 = text.substring(0, cursorPosition);
		String s2 = text.substring(cursorPosition);
		if(this.fontRendererObj.getBidiFlag()) {
			s1 = s1 + "_";
		} else if((Minecraft.getSystemTime() / 500) % 2 == 0) {
			s1.substring(0, cursorPosition);
			s1 = s1 + "_";
		} else {
			s1 = s1 + " ";
		}
		scrollingText.SetText(s1 + s2);
	}

	@Override
	public void mouseScroll(int mx, int my, int scroll) {
		super.mouseScroll(mx, my, scroll);
		if(scroll != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, 116, sizeY)) {
			listScroll = Math.max(0, MathHelper.clamp_int(listScroll + scroll, 0, EnumChatFormatting.values().length - maxRows));
			RefreshColumns();
		}
	}

	@Override
	public void mouseClicked(int mx, int my, int click) {
		super.mouseClicked(mx, my, click);
		if(isWithin(mx, my, guiLeft + 132, guiTop + 32, sizeX - 148, sizeY - 64, false)) {
			this.setCursorPosition(scrollingText.getCursorPos(mx, my));
		}
	}

	public void RefreshColumns() {
		listScroll = Math.max(0, MathHelper.clamp_int(listScroll, 0, EnumChatFormatting.values().length - maxRows));
		for(GuiButton btn : (List<GuiButton>) this.buttonList) {
			int n1 = btn.id - 1,
				n2 = n1 / maxRows,
				n3 = n1 % maxRows + listScroll;
			if(n2 == 0) {
				if(n3 >= 0 && n3 < EnumChatFormatting.values().length) {
					btn.displayString = EnumChatFormatting.values()[n3].getFriendlyName();
					btn.enabled = btn.visible = true;
				} else {
					btn.displayString = "NULL";
					btn.enabled = btn.visible = false;
				}
			}
		}
	}
}