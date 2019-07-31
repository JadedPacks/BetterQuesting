package betterquesting.client.gui.editors;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiBigTextField;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingButtons;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.ICallback;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.RenderUtils;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestLineDatabase;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiQuestLineEditorA extends GuiScreenThemed implements ICallback<String>, IVolatileScreen, INeedsRefresh {
	private GuiButtonThemed btnDesign;
	private GuiTextField lineTitle;
	private GuiBigTextField lineDesc;
	private IQuestLine selected;
	int selID = -1;
	private GuiScrollingButtons btnList;

	public GuiQuestLineEditorA(GuiScreen parent) {
		super(parent, "betterquesting.title.edit_line1");
	}

	@Override
	public void initGui() {
		super.initGui();
		int btnWidth = sizeX / 2 - 16;
		int sx = sizeX - 32;
		lineTitle = new GuiTextField(mc.fontRendererObj, guiLeft + sizeX / 2 + 9, guiTop + sizeY / 2 - 59, btnWidth - 18, 18);
		lineTitle.setMaxStringLength(Integer.MAX_VALUE);
		lineDesc = new GuiBigTextField(mc.fontRendererObj, guiLeft + sizeX / 2 + 9, guiTop + sizeY / 2 - 19, btnWidth - 18, 18).enableBigEdit(this);
		lineDesc.setMaxStringLength(Integer.MAX_VALUE);
		this.buttonList.add(new GuiButtonThemed(1, guiLeft + 16, guiTop + sizeY - 48, (btnWidth - 16) / 2, 20, I18n.format("betterquesting.btn.new"), true));
		GuiButtonThemed btnImport = new GuiButtonThemed(3, guiLeft + 16 + (btnWidth - 16) / 2, guiTop + sizeY - 48, (btnWidth - 16) / 2, 20, I18n.format("betterquesting.btn.import"), true);
		this.buttonList.add(btnImport);
		this.buttonList.add(new GuiButtonThemed(2, guiLeft + 16 + sx / 4 * 3 - 75, guiTop + sizeY / 2 + 20, 150, 20, I18n.format("betterquesting.btn.add_remove_quests"), true));
		btnDesign = new GuiButtonThemed(4, guiLeft + 16 + sx / 4 * 3 - 75, guiTop + sizeY / 2 + 40, 150, 20, I18n.format("betterquesting.btn.designer"), true);
		this.buttonList.add(btnDesign);
		btnList = new GuiScrollingButtons(mc, guiLeft + 16, guiTop + 32, btnWidth - 8, sizeY - 80);
		this.embedded.add(btnList);
		if(selected != null) {
			lineTitle.setText(selected.getUnlocalisedName());
			lineDesc.setText(selected.getUnlocalisedDescription());
		}
		RefreshColumns();
	}

	@Override
	public void refreshGui() {
		selected = QuestLineDatabase.INSTANCE.getValue(selID);
		if(selected != null) {
			lineTitle.setText(selected.getUnlocalisedName());
			lineDesc.setText(selected.getUnlocalisedDescription());
		}
		RefreshColumns();
	}

	@Override
	public void drawScreen(int mx, int my, float partialTick) {
		super.drawScreen(mx, my, partialTick);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		RenderUtils.DrawLine(width / 2, guiTop + 32, width / 2, guiTop + sizeY - 48, 2F, getTextColor());
		mc.fontRendererObj.drawString(I18n.format("betterquesting.gui.name"), guiLeft + sizeX / 2 + 8, guiTop + sizeY / 2 - 72, getTextColor(), false);
		mc.fontRendererObj.drawString(I18n.format("betterquesting.gui.description"), guiLeft + sizeX / 2 + 8, guiTop + sizeY / 2 - 32, getTextColor(), false);
		lineTitle.drawTextBox();
		lineDesc.drawTextBox(mx, my, partialTick);
	}

	public void SendChanges(EnumPacketAction action, IQuestLine questLine) {
		SendChanges(action, QuestLineDatabase.INSTANCE.getKey(questLine));
	}

	public void SendChanges(EnumPacketAction action, int lineID) {
		SendChanges(action, lineID, QuestLineDatabase.INSTANCE.getOrderIndex(lineID));
	}

	public void SendChanges(EnumPacketAction action, int lineID, int order) {
		IQuestLine questLine = QuestLineDatabase.INSTANCE.getValue(lineID);
		if(action == null) {
			return;
		}
		NBTTagCompound tags = new NBTTagCompound();
		if(action == EnumPacketAction.EDIT && questLine != null) {
			JsonObject base = new JsonObject();
			base.add("line", questLine.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
			tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		}
		tags.setInteger("lineID", questLine == null ? -1 : QuestLineDatabase.INSTANCE.getKey(questLine));
		tags.setInteger("order", order);
		tags.setInteger("action", action.ordinal());
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tags));
	}

	@Override
	public void actionPerformed(GuiButton btn) {
		super.actionPerformed(btn);
		if(btn.id == 1) {
			SendChanges(EnumPacketAction.ADD, -1);
			RefreshColumns();
		} else if(btn.id == 2) {
			mc.displayGuiScreen(new GuiQuestLineEditorB(this, selected));
		} else if(btn.id == 4 && selected != null) {
			mc.displayGuiScreen(new GuiQuestLineDesigner(this, selected));
		} else if(btn.id > 4) {
			int n2 = btn.id & 3,
				n3 = (btn.id >> 2) - 5;
			if(n2 == 0) {
				if(n3 >= 0) {
					selected = QuestLineDatabase.INSTANCE.getValue(n3);
					selID = n3;
					if(selected != null) {
						lineTitle.setText(selected.getUnlocalisedName());
						lineDesc.setText(selected.getUnlocalisedDescription());
					}
				} else {
					selected = null;
					selID = -1;
				}
				RefreshColumns();
			} else if(n2 == 1) {
				if(n3 >= 0) {
					SendChanges(EnumPacketAction.REMOVE, n3);
				}
			} else if(n2 == 2) {
				if(n3 >= 0) {
					int order = QuestLineDatabase.INSTANCE.getOrderIndex(n3);
					if(order > 0) {
						SendChanges(EnumPacketAction.EDIT, n3, order - 1);
					}
				}
			}
		}
	}

	@Override
	protected void keyTyped(char character, int keyCode) {
		super.keyTyped(character, keyCode);
		if(selected != null) {
			lineDesc.textboxKeyTyped(character, keyCode);
			lineTitle.textboxKeyTyped(character, keyCode);
			if(keyCode == Keyboard.KEY_RETURN) {
				boolean flag = false;
				if(lineTitle.isFocused()) {
					if(!lineTitle.getText().equals(selected.getUnlocalisedName())) {
						selected.getProperties().setProperty(NativeProps.NAME, lineTitle.getText());
						flag = true;
					}
					lineTitle.setFocused(false);
				}
				if(lineDesc.isFocused()) {
					if(!lineDesc.getText().equals(selected.getUnlocalisedDescription())) {
						selected.getProperties().setProperty(NativeProps.DESC, lineDesc.getText());
						flag = true;
					}
					lineDesc.setFocused(false);
				}
				if(flag) {
					SendChanges(EnumPacketAction.EDIT, selected);
				}
			}
		}
	}

	@Override
	protected void mouseClicked(int mx, int my, int click) {
		lineTitle.mouseClicked(mx, my, click);
		lineDesc.mouseClicked(mx, my, click);
		if(selected != null) {
			boolean flag = false;
			if(!lineTitle.isFocused() && !lineTitle.getText().equals(selected.getUnlocalisedName())) {
				selected.getProperties().setProperty(NativeProps.NAME, lineTitle.getText());
				flag = true;
			}
			if(!lineDesc.isFocused() && !lineDesc.getText().equals(selected.getUnlocalisedDescription())) {
				selected.getProperties().setProperty(NativeProps.DESC, lineDesc.getText());
				flag = true;
			}
			if(flag) {
				SendChanges(EnumPacketAction.EDIT, selected);
			}
		}
		super.mouseClicked(mx, my, click);
		GuiButtonThemed btn = click != 0 ? null : btnList.getButtonUnderMouse(mx, my);
		if(btn != null && btn.mousePressed(mc, mx, my)) {
			btn.playPressSound(mc.getSoundHandler());
			this.actionPerformed(btn);
		}
	}

	public void RefreshColumns() {
		List<Integer> questList = QuestLineDatabase.INSTANCE.getAllKeys();
		if(btnDesign != null) {
			btnDesign.enabled = selected != null;
		}
		btnList.getEntryList().clear();
		for(int qlid : questList) {
			IQuestLine line = QuestLineDatabase.INSTANCE.getValue(qlid);
			if(line == null) {
				continue;
			}
			int bWidth = btnList.getListWidth(),
				bID = (5 + qlid) << 2;
			GuiButtonThemed btn1 = new GuiButtonThemed(bID, 0, 0, bWidth - 40, 20, I18n.format(line.getUnlocalisedName()));
			btn1.enabled = line != selected;
			GuiButtonThemed btn2 = new GuiButtonThemed(bID + 1, 0, 0, 20, 20, "" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "x");
			GuiButtonThemed btn3 = new GuiButtonThemed(bID + 2, 0, 0, 20, 20, "" + EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD + "^");
			btnList.addButtonRow(btn1, btn2, btn3);
		}
	}

	@Override
	public void setValue(String text) {
		if(lineDesc != null) {
			lineDesc.setText(text);
		}
		if(selected != null) {
			selected.getProperties().setProperty(NativeProps.DESC, text);
			SendChanges(EnumPacketAction.EDIT, selected);
		}
	}
}