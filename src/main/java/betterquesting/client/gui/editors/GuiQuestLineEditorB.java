package betterquesting.client.gui.editors;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiBigTextField;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingButtons;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.GuiQuestInstance;
import betterquesting.client.themes.ThemeStandard;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.*;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiQuestLineEditorB extends GuiScreenThemed implements IVolatileScreen, INeedsRefresh {
	private final int lineID;
	private QuestLine line;
	private GuiBigTextField searchBox;
	private final List<Integer> searchResults = new ArrayList<>();
	private List<Integer> lineQuests = new ArrayList<>();
	private GuiScrollingButtons dbBtnList,
		qlBtnList;

	public GuiQuestLineEditorB(GuiScreen parent, QuestLine line) {
		super(parent, I18n.format("betterquesting.title.edit_line2", line == null ? "?" : I18n.format(line.name)));
		this.line = line;
		this.lineID = QuestLineDatabase.getKey(line);
	}

	@Override
	public void initGui() {
		super.initGui();
		int btnWidth = sizeX / 2 - 16;
		int sx = sizeX - 32;
		this.searchBox = new GuiBigTextField(mc.fontRendererObj, guiLeft + sizeX / 2 + 9, guiTop + 49, btnWidth - 18, 18);
		this.searchBox.setWatermark(I18n.format("betterquesting.gui.search"));
		this.buttonList.add(new GuiButtonThemed(1, guiLeft + 16 + sx / 4 * 3 - 50, guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.new"), true));
		qlBtnList = new GuiScrollingButtons(mc, guiLeft + 16, guiTop + 48, btnWidth - 8, sizeY - 96);
		dbBtnList = new GuiScrollingButtons(mc, guiLeft + sizeX / 2 + 8, guiTop + 68, btnWidth - 8, sizeY - 116);
		this.embedded.add(qlBtnList);
		this.embedded.add(dbBtnList);
		RefreshSearch();
		RefreshColumns();
	}

	@Override
	public void refreshGui() {
		this.line = QuestLineDatabase.getValue(lineID);
		if(lineID >= 0 && line == null) {
			mc.displayGuiScreen(parent);
			return;
		}
		setTitle(I18n.format("betterquesting.title.edit_line2", line == null ? "?" : I18n.format(line.name)));
		RefreshSearch();
		RefreshColumns();
	}

	@Override
	public void drawScreen(int mx, int my, float partialTick) {
		super.drawScreen(mx, my, partialTick);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(ThemeStandard.getGuiTexture());
		RenderUtils.DrawLine(width / 2, guiTop + 32, width / 2, guiTop + sizeY - 32, 2F, getTextColor());
		int sx = sizeX - 32;
		String txt = I18n.format("betterquesting.gui.quest_line");
		mc.fontRendererObj.drawString(txt, guiLeft + 16 + sx / 4 - mc.fontRendererObj.getStringWidth(txt) / 2, guiTop + 32, getTextColor(), false);
		txt = I18n.format("betterquesting.gui.database");
		mc.fontRendererObj.drawString(txt, guiLeft + 16 + sx / 4 * 3 - mc.fontRendererObj.getStringWidth(txt) / 2, guiTop + 32, getTextColor(), false);
		searchBox.drawTextBox(mx, my);
	}

	@Override
	public void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if(button.id == 1) {
			createQuest();
		} else if(button.id > 1) {
			int column = button.id & 7;
			int id = (button.id >> 3) - 2;
			if(column == 0 || column == 3) {
				if(id >= 0) {
					QuestInstance q = QuestDatabase.getValue(id);
					if(q != null) {
						mc.displayGuiScreen(new GuiQuestInstance(this, q));
					}
				}
			} else if(column == 1 && line != null) {
				line.removeKey(id);
				SendChanges(EnumPacketAction.EDIT);
			} else if(column == 4 && id >= 0) {
				NBTTagCompound tags = new NBTTagCompound();
				tags.setInteger("action", EnumPacketAction.REMOVE.ordinal());
				tags.setInteger("questID", id);
				PacketSender.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
			} else if(column == 2 && line != null && id >= 0) {
				QuestLineEntry qe = new QuestLineEntry(0, 0);
				int x1 = 0;
				int y1 = 0;
				topLoop:
				while(true) {
					for(QuestLineEntry qe2 : line.getAllValues()) {
						int x2 = qe2.getPosX();
						int y2 = qe2.getPosY();
						if(x1 >= x2 && x1 < x2 + 24 && y1 >= y2 && y1 < y2 + 24) {
							x1 += 24;
							continue topLoop;
						}
					}
					break;
				}
				qe.setPosition(x1, y1);
				line.add(qe, id);
				RefreshColumns();
				SendChanges(EnumPacketAction.EDIT);
			}
		}
	}

	public void createQuest() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("action", EnumPacketAction.ADD.ordinal());
		PacketSender.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tag));
	}

	public void SendChanges(EnumPacketAction action) {
		if(action == null) {
			return;
		}
		NBTTagCompound tags = new NBTTagCompound();
		if(action == EnumPacketAction.EDIT && line != null) {
			JsonObject base = new JsonObject();
			base.add("line", line.writeToJson(new JsonObject()));
			tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		}
		tags.setInteger("action", action.ordinal());
		tags.setInteger("lineID", QuestLineDatabase.getKey(line));
		PacketSender.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tags));
	}

	public void RefreshColumns() {
		if(line == null) {
			lineQuests.clear();
		} else {
			lineQuests = line.getAllKeys();
		}
		qlBtnList.getEntryList().clear();
		for(int qID : lineQuests) {
			QuestInstance quest = QuestDatabase.getValue(qID);
			if(quest == null) {
				continue;
			}
			int bWidth = qlBtnList.getListWidth(),
				bID = (2 + qID) << 3;
			GuiButtonThemed btn1 = new GuiButtonThemed(bID, 0, 0, bWidth - 20, 20, I18n.format(quest.name));
			GuiButtonThemed btn2 = new GuiButtonThemed(bID + 1, 0, 0, 20, 20, EnumChatFormatting.YELLOW + ">");
			qlBtnList.addButtonRow(btn1, btn2);
		}
		dbBtnList.getEntryList().clear();
		for(int qID : searchResults) {
			QuestInstance quest = QuestDatabase.getValue(qID);
			if(quest == null) {
				continue;
			}
			int bWidth = dbBtnList.getListWidth();
			int bID = (2 + qID) << 3;
			GuiButtonThemed btn3 = new GuiButtonThemed(bID + 2, 0, 0, 20, 20, EnumChatFormatting.GREEN + "<");
			btn3.enabled = line != null && !lineQuests.contains(qID);
			GuiButtonThemed btn4 = new GuiButtonThemed(bID + 3, 0, 0, bWidth - 40, 20, I18n.format(quest.name));
			GuiButtonThemed btn5 = new GuiButtonThemed(bID + 4, 0, 0, 20, 20, "" + EnumChatFormatting.BOLD + EnumChatFormatting.RED + "x");
			dbBtnList.addButtonRow(btn3, btn4, btn5);
		}
	}

	@Override
	protected void keyTyped(char character, int num) {
		super.keyTyped(character, num);
		String prevTxt = searchBox.getText();
		searchBox.textboxKeyTyped(character, num);
		if(!searchBox.getText().equalsIgnoreCase(prevTxt)) {
			RefreshSearch();
			RefreshColumns();
		}
	}

	public void RefreshSearch() {
		searchResults.clear();
		String query = searchBox.getText().toLowerCase();
		for(int id : QuestDatabase.getAllKeys()) {
			QuestInstance q = QuestDatabase.getValue(id);
			if(query.length() <= 0 || q.name.toLowerCase().contains(query) || I18n.format(q.name).toLowerCase().contains(query) || query.equalsIgnoreCase("" + id)) {
				searchResults.add(id);
			}
		}
	}

	@Override
	public void mouseClicked(int mx, int my, int type) {
		super.mouseClicked(mx, my, type);
		this.searchBox.mouseClicked(mx, my, type);
		if(type != 0) {
			return;
		}
		GuiButtonThemed btn1 = qlBtnList.getButtonUnderMouse(mx, my);
		if(btn1 != null && btn1.mousePressed(mc, mx, my)) {
			btn1.playPressSound(mc.getSoundHandler());
			this.actionPerformed(btn1);
			return;
		}
		GuiButtonThemed btn2 = dbBtnList.getButtonUnderMouse(mx, my);
		if(btn2 != null && btn2.mousePressed(mc, mx, my)) {
			btn2.playPressSound(mc.getSoundHandler());
			this.actionPerformed(btn2);
		}
	}
}