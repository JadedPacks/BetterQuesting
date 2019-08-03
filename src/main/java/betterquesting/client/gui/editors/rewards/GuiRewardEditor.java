package betterquesting.client.gui.editors.rewards;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingButtons;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.misc.IFactory;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.RenderUtils;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.questing.rewards.RewardRegistry;
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
public class GuiRewardEditor extends GuiScreenThemed implements IVolatileScreen, INeedsRefresh {
	private List<IFactory<? extends IReward>> rewardTypes = new ArrayList<>();
	private List<Integer> rewardIDs = new ArrayList<>();
	private QuestInstance quest;
	private final int qID;
	private GuiScrollingButtons btnsLeft, btnsRight;

	public GuiRewardEditor(GuiScreen parent, QuestInstance quest) {
		super(parent, I18n.format("betterquesting.title.edit_rewards", I18n.format(quest.name)));
		this.quest = quest;
		this.qID = QuestDatabase.getKey(quest);
	}

	@Override
	public void initGui() {
		super.initGui();
		rewardTypes = new ArrayList<>(RewardRegistry.INSTANCE.rewardRegistry.values());
		rewardIDs = quest.getRewards().getAllKeys();
		btnsLeft = new GuiScrollingButtons(mc, guiLeft + 16, guiTop + 32, sizeX / 2 - 24, sizeY - 64);
		btnsRight = new GuiScrollingButtons(mc, guiLeft + sizeX / 2 + 8, guiTop + 32, sizeX / 2 - 24, sizeY - 64);
		this.embedded.add(btnsLeft);
		this.embedded.add(btnsRight);
		RefreshColumns();
	}

	@Override
	public void refreshGui() {
		QuestInstance tmp = QuestDatabase.getValue(qID);
		if(tmp == null) {
			mc.displayGuiScreen(parent);
			return;
		}
		this.quest = tmp;
		this.rewardIDs = quest.getRewards().getAllKeys();
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
		int column = button.id & 3;
		int id = (button.id >> 2) - 1;
		if(id < 0) {
			return;
		}
		if(column == 0) {
			IReward reward = quest.getRewards().getValue(id);
			GuiScreen editor = reward.getRewardEditor(this, quest);
			if(editor != null) {
				mc.displayGuiScreen(editor);
			}
		} else if(column == 1) {
			quest.getRewards().removeKey(id);
			SendChanges();
		} else if(column == 2) {
			if(id < rewardTypes.size()) {
				quest.getRewards().add(RewardRegistry.INSTANCE.createReward(rewardTypes.get(id).getRegistryName()), quest.getRewards().nextKey());
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
		base.add("config", quest.writeToJson(new JsonObject()));
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal());
		tags.setInteger("questID", QuestDatabase.getKey(quest));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		PacketSender.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
	}

	public void RefreshColumns() {
		btnsLeft.getEntryList().clear();
		btnsRight.getEntryList().clear();
		for(int tID : rewardIDs) {
			int btnWidth = btnsLeft.getListWidth(),
				bID = (1 + tID) << 2;
			GuiButtonThemed btn1 = new GuiButtonThemed(bID, 0, 0, btnWidth - 20, 20, I18n.format(quest.getRewards().getValue(tID).getUnlocalisedName()));
			GuiButtonThemed btn2 = new GuiButtonThemed(bID + 1, 0, 0, 20, 20, "" + ChatFormatting.RED + ChatFormatting.BOLD + "x");
			btnsLeft.addButtonRow(btn1, btn2);
		}
		for(int i = 0; i < rewardTypes.size(); i++) {
			int btnWidth = btnsRight.getListWidth(),
				bID = (1 + i) << 2;
			GuiButtonThemed btn1 = new GuiButtonThemed(bID + 2, 0, 0, btnWidth, 20, rewardTypes.get(i).getRegistryName().toString());
			btnsRight.addButtonRow(btn1);
		}
	}
}