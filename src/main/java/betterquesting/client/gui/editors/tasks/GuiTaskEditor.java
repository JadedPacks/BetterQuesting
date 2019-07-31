package betterquesting.client.gui.editors.tasks;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingButtons;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.RenderUtils;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.tasks.TaskRegistry;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiTaskEditor extends GuiScreenThemed implements IVolatileScreen, INeedsRefresh {
	private List<IFactory<? extends ITask>> taskTypes = new ArrayList<>();
	private List<Integer> taskIDs = new ArrayList<>();
	private IQuest quest;
	private final int qId;
	private GuiScrollingButtons btnsLeft, btnsRight;

	public GuiTaskEditor(GuiScreen parent, IQuest quest) {
		super(parent, I18n.format("betterquesting.title.edit_tasks", I18n.format(quest.getUnlocalisedName())));
		this.quest = quest;
		this.qId = QuestDatabase.INSTANCE.getKey(quest);
	}

	@Override
	public void initGui() {
		super.initGui();
		taskTypes = TaskRegistry.INSTANCE.getAll();
		taskIDs = quest.getTasks().getAllKeys();
		btnsLeft = new GuiScrollingButtons(mc, guiLeft + 16, guiTop + 32, sizeX / 2 - 24, sizeY - 64);
		btnsRight = new GuiScrollingButtons(mc, guiLeft + sizeX / 2 + 8, guiTop + 32, sizeX / 2 - 24, sizeY - 64);
		this.embedded.add(btnsLeft);
		this.embedded.add(btnsRight);
		RefreshColumns();
	}

	@Override
	public void refreshGui() {
		IQuest tmp = QuestDatabase.INSTANCE.getValue(qId);
		if(tmp == null) {
			mc.displayGuiScreen(parent);
			return;
		}
		this.quest = tmp;
		this.taskIDs = quest.getTasks().getAllKeys();
		RefreshColumns();
	}

	@Override
	public void drawBackPanel(int mx, int my, float partialTick) {
		super.drawBackPanel(mx, my, partialTick);
		RenderUtils.DrawLine(width / 2, guiTop + 32, width / 2, guiTop + sizeY - 32, 2F, getTextColor());
	}

	@Override
	public void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		int column = button.id & 3,
			id = (button.id >> 2) - 1;
		if(id < 0) {
			return;
		}
		if(column == 0) {
			ITask task = quest.getTasks().getValue(id);
			GuiScreen editor = task.getTaskEditor(this, quest);
			if(editor != null) {
				mc.displayGuiScreen(editor);
			} else {
				mc.displayGuiScreen(new GuiTaskEditDefault(this, quest, task));
			}
		} else if(column == 1) {
			quest.getTasks().removeKey(id);
			SendChanges();
		} else if(column == 2) {
			if(id < taskTypes.size()) {
				quest.getTasks().add(TaskRegistry.INSTANCE.createTask(taskTypes.get(id).getRegistryName()), quest.getTasks().nextKey());
				SendChanges();
			}
		}
	}

	@Override
	public void mouseClicked(int mx, int my, int click) {
		super.mouseClicked(mx, my, click);
		if(click != 0) {
			return;
		}
		GuiButtonThemed btn1 = btnsLeft.getButtonUnderMouse(mx, my);
		if(btn1 != null && btn1.mousePressed(mc, mx, my)) {
			btn1.playPressSound(mc.getSoundHandler());
			this.actionPerformed(btn1);
			return;
		}
		GuiButtonThemed btn2 = btnsRight.getButtonUnderMouse(mx, my);
		if(btn2 != null && btn2.mousePressed(mc, mx, my)) {
			btn2.playPressSound(mc.getSoundHandler());
			this.actionPerformed(btn2);
		}
	}

	public void SendChanges() {
		JsonObject base = new JsonObject();
		base.add("config", quest.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		base.add("progress", quest.writeToJson(new JsonObject(), EnumSaveType.PROGRESS));
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal()); // Action: Update data
		tags.setInteger("questID", QuestDatabase.INSTANCE.getKey(quest));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
	}

	public void RefreshColumns() {
		btnsLeft.getEntryList().clear();
		btnsRight.getEntryList().clear();
		for(int tID : taskIDs) {
			int btnWidth = btnsLeft.getListWidth(),
				bID = (1 + tID) << 2;
			GuiButtonThemed btn1 = new GuiButtonThemed(bID, 0, 0, btnWidth - 20, 20, I18n.format(quest.getTasks().getValue(tID).getUnlocalisedName()));
			GuiButtonThemed btn2 = new GuiButtonThemed(bID + 1, 0, 0, 20, 20, "" + ChatFormatting.RED + ChatFormatting.BOLD + "x");
			btnsLeft.addButtonRow(btn1, btn2);
		}

		for(int i = 0; i < taskTypes.size(); i++) {
			int btnWidth = btnsRight.getListWidth(),
				bID = (1 + i) << 2;
			GuiButtonThemed btn1 = new GuiButtonThemed(bID + 2, 0, 0, btnWidth, 20, taskTypes.get(i).getRegistryName().toString());
			btnsRight.addButtonRow(btn1);
		}
	}
}