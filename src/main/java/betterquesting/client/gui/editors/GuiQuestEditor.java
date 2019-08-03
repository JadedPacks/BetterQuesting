package betterquesting.client.gui.editors;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiBigTextField;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.misc.ICallback;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.NBTConverter;
import betterquesting.client.gui.editors.rewards.GuiRewardEditor;
import betterquesting.client.gui.editors.tasks.GuiTaskEditor;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

@SideOnly(Side.CLIENT)
public class GuiQuestEditor extends GuiScreenThemed implements ICallback<String>, IVolatileScreen, INeedsRefresh {
	private JsonObject lastEdit;
	private final int id;
	private QuestInstance quest;
	private GuiTextField titleField;
	private GuiBigTextField descField;
	private GuiButtonThemed btnLogic, btnVis;

	public GuiQuestEditor(GuiScreen parent, QuestInstance quest) {
		super(parent, I18n.format("betterquesting.title.edit_quest", I18n.format(quest.name)));
		this.quest = quest;
		this.id = QuestDatabase.getKey(quest);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.setTitle(I18n.format("betterquesting.title.edit_quest", I18n.format(quest.name)));
		if(lastEdit != null) {
			quest.readFromJson(lastEdit);
			lastEdit = null;
			SendChanges();
		}
		titleField = new GuiTextField(this.fontRendererObj, width / 2 - 99, height / 2 - 68 + 1, 198, 18);
		titleField.setMaxStringLength(Integer.MAX_VALUE);
		titleField.setText(quest.name);
		descField = new GuiBigTextField(this.fontRendererObj, width / 2 - 99, height / 2 - 28 + 1, 198, 18).enableBigEdit(this);
		descField.setMaxStringLength(Integer.MAX_VALUE);
		descField.setText(quest.desc);
		GuiButtonThemed btn = new GuiButtonThemed(1, width / 2, height / 2 + 28, 100, 20, I18n.format("betterquesting.btn.rewards"), true);
		this.buttonList.add(btn);
		btn = new GuiButtonThemed(2, width / 2 - 100, height / 2 + 28, 100, 20, I18n.format("betterquesting.btn.tasks"), true);
		this.buttonList.add(btn);
		btn = new GuiButtonThemed(3, width / 2 - 100, height / 2 + 48, 100, 20, I18n.format("betterquesting.btn.requirements"), true);
		this.buttonList.add(btn);
		btnLogic = new GuiButtonThemed(4, width / 2, height / 2 + 48, 100, 20, I18n.format("betterquesting.btn.logic") + ": " + quest.logicQuest, true);
		this.buttonList.add(btnLogic);
		btnVis = new GuiButtonThemed(5, width / 2 - 100, height / 2 + 68, 100, 20, I18n.format("betterquesting.btn.show") + ": " + quest.visibility, true);
		this.buttonList.add(btnVis);
	}

	@Override
	public void refreshGui() {
		this.quest = QuestDatabase.getValue(id);
		if(quest == null) {
			mc.displayGuiScreen(parent);
			return;
		}
		lastEdit = null;
		titleField.setText(quest.name);
		descField.setText(quest.desc);
		btnLogic.displayString = I18n.format("betterquesting.btn.logic") + ": " + quest.logicQuest;
		btnVis.displayString = I18n.format("betterquesting.btn.show") + ": " + quest.visibility;
	}

	@Override
	public void drawScreen(int mx, int my, float partialTick) {
		super.drawScreen(mx, my, partialTick);
		titleField.drawTextBox();
		descField.drawTextBox(mx, my);
		mc.fontRendererObj.drawString(I18n.format("betterquesting.gui.name"), width / 2 - 100, height / 2 - 80, getTextColor(), false);
		mc.fontRendererObj.drawString(I18n.format("betterquesting.gui.description"), width / 2 - 100, height / 2 - 40, getTextColor(), false);
	}

	@Override
	public void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if(button.id == 1) {
			mc.displayGuiScreen(new GuiRewardEditor(this, quest));
		} else if(button.id == 2) {
			mc.displayGuiScreen(new GuiTaskEditor(this, quest));
		} else if(button.id == 3) {
			mc.displayGuiScreen(new GuiPrerequisiteEditor(this, quest));
		} else if(button.id == 4) {
			EnumLogic[] logicList = EnumLogic.values();
			EnumLogic logic = quest.logicQuest;
			logic = logicList[(logic.ordinal() + 1) % logicList.length];
			quest.logicQuest = logic;
			button.displayString = I18n.format("betterquesting.btn.logic") + ": " + logic;
			SendChanges();
		} else if(button.id == 5) {
			EnumQuestVisibility[] visList = EnumQuestVisibility.values();
			EnumQuestVisibility vis = quest.visibility;
			vis = visList[(vis.ordinal() + 1) % visList.length];
			quest.visibility = vis;
			button.displayString = I18n.format("betterquesting.btn.show") + ": " + vis;
			SendChanges();
		}
	}

	@Override
	protected void keyTyped(char character, int keyCode) {
		super.keyTyped(character, keyCode);
		titleField.textboxKeyTyped(character, keyCode);
		descField.textboxKeyTyped(character, keyCode);
	}

	@Override
	protected void mouseClicked(int mx, int my, int click) {
		super.mouseClicked(mx, my, click);
		titleField.mouseClicked(mx, my, click);
		descField.mouseClicked(mx, my, click);
		boolean flag = false;
		if(!titleField.isFocused() && !titleField.getText().equals(quest.name)) {
			quest.name = titleField.getText();
			flag = true;
		}
		if(!descField.isFocused() && !descField.getText().equals(quest.desc)) {
			quest.desc = descField.getText();
			flag = true;
		}
		if(flag) {
			SendChanges();
		}
	}

	public void SendChanges() {
		JsonObject base = new JsonObject();
		base.add("config", quest.writeToJson(new JsonObject()));
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal());
		tags.setInteger("questID", id);
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		PacketSender.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
	}

	@Override
	public void setValue(String text) {
		if(descField != null) {
			descField.setText(text);
		}
		quest.desc = text;
		SendChanges();
	}
}