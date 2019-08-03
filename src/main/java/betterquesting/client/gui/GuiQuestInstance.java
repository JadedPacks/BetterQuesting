package betterquesting.client.gui;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingText;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.editors.GuiQuestEditor;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.storage.QuestSettings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

@SideOnly(Side.CLIENT)
public class GuiQuestInstance extends GuiScreenThemed implements INeedsRefresh {
	final int id;
	int selTaskId = 0;
	int selRewardId = 0;
	QuestInstance quest;
	ITask selTask = null;
	IGuiEmbedded taskRender = null, rewardRender;
	IReward selReward = null;
	GuiButtonThemed btnTLeft, btnTRight, btnRLeft, btnRRight, btnClaim;

	public GuiQuestInstance(GuiScreen parent, QuestInstance quest) {
		super(parent, I18n.format(quest.name));
		this.quest = quest;
		this.id = QuestDatabase.getKey(quest);
	}

	@Override
	public void refreshGui() {
		QuestInstance tmp = QuestDatabase.getValue(id);
		if(tmp == quest) {
			return;
		}
		this.quest = tmp;
		if(quest == null) {
			this.mc.displayGuiScreen(parent);
			return;
		}
		initGui();
	}

	@Override
	public void initGui() {
		super.initGui();
		if(QuestSettings.canUserEdit(mc.thePlayer)) {
			((GuiButton) this.buttonList.get(0)).xPosition = this.width / 2 - 100;
			((GuiButton) this.buttonList.get(0)).width = 100;
		}
		GuiButtonThemed btnEdit = new GuiButtonThemed(4, this.width / 2, this.guiTop + this.sizeY - 16, 100, 20, I18n.format("betterquesting.btn.edit"), true);
		btnEdit.enabled = btnEdit.visible = QuestSettings.canUserEdit(mc.thePlayer);
		this.buttonList.add(btnEdit);
		this.setTitle(I18n.format(quest.name));
		this.embedded.add(new GuiScrollingText(mc, this.guiLeft + 16, this.guiTop + 32, sizeX / 2 - 24, quest.getRewards().size() > 0 ? sizeY / 2 - 48 : sizeY - 64, I18n.format(quest.desc)));
		btnTLeft = new GuiButtonThemed(1, this.guiLeft + (sizeX / 4) * 3 - 70, this.guiTop + sizeY - 48, 20, 20, "<", true);
		btnTLeft.enabled = selTaskId > 0;
		btnTRight = new GuiButtonThemed(3, this.guiLeft + (sizeX / 4) * 3 + 50, this.guiTop + sizeY - 48, 20, 20, ">", true);
		btnTRight.enabled = selTaskId < quest.getTasks().size() - 1;
		btnRLeft = new GuiButtonThemed(6, this.guiLeft + (sizeX / 4) - 70, this.guiTop + sizeY - 48, 20, 20, "<", true);
		btnRLeft.visible = quest.getRewards().size() > 0;
		btnRLeft.enabled = btnRLeft.visible && selRewardId > 0;
		btnRRight = new GuiButtonThemed(7, this.guiLeft + (sizeX / 4) + 50, this.guiTop + sizeY - 48, 20, 20, ">", true);
		btnRRight.visible = quest.getRewards().size() > 0;
		btnRRight.enabled = btnRRight.visible && selRewardId < quest.getRewards().size() - 1;
		GuiButtonThemed btnDetect = new GuiButtonThemed(2, this.guiLeft + (sizeX / 4) * 3 - 50, this.guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.detect_submit"), true);
		btnDetect.enabled = quest.canSubmit(mc.thePlayer);
		btnClaim = new GuiButtonThemed(5, this.guiLeft + (sizeX / 4) - 50, this.guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.claim"), true);
		btnClaim.visible = quest.getRewards().size() > 0;
		btnClaim.enabled = btnClaim.visible && quest.canClaim(mc.thePlayer);
		this.buttonList.add(btnTLeft);
		this.buttonList.add(btnTRight);
		this.buttonList.add(btnRLeft);
		this.buttonList.add(btnRRight);
		this.buttonList.add(btnDetect);
		this.buttonList.add(btnClaim);
		refreshEmbedded();
	}

	@Override
	public void drawBackPanel(int mx, int my, float partialTick) {
		super.drawBackPanel(mx, my, partialTick);
		RenderUtils.DrawLine(this.guiLeft + sizeX / 2, this.guiTop + 32, this.guiLeft + sizeX / 2, this.guiTop + sizeY - 24, 1, getTextColor());
		if(selTask != null) {
			int tSize = quest.getTasks().size();
			String tTitle = I18n.format(selTask.getUnlocalisedName());
			if(tSize > 1) {
				tTitle = (selTaskId + 1) + "/" + tSize + " " + tTitle;
			}
			tTitle = EnumChatFormatting.UNDERLINE + tTitle;
			int nameWidth = this.fontRendererObj.getStringWidth(tTitle);
			this.fontRendererObj.drawString(tTitle, this.guiLeft + (sizeX / 4) * 3 - (nameWidth / 2), this.guiTop + 32, getTextColor());
		}
		if(selReward != null) {
			int rSize = quest.getRewards().size();
			String rTitle = I18n.format(selReward.getUnlocalisedName());
			if(rSize > 1) {
				rTitle = (selRewardId + 1) + "/" + rSize + " " + rTitle;
			}
			rTitle = EnumChatFormatting.UNDERLINE + rTitle;
			int nameWidth = this.fontRendererObj.getStringWidth(rTitle);
			this.fontRendererObj.drawString(rTitle, guiLeft + (sizeX / 4) - (nameWidth / 2), guiTop + sizeY / 2 - 12, getTextColor());
		}
	}

	@Override
	public void actionPerformed(GuiButton btn) {
		super.actionPerformed(btn);
		if(btn.id == 1) {
			selTaskId--;
			btnTLeft.enabled = selTaskId > 0;
			btnTRight.enabled = selTaskId < quest.getTasks().size() - 1;
			this.embedded.remove(taskRender);
			taskRender = null;
			refreshEmbedded();
		} else if(btn.id == 2) {
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("questID", QuestDatabase.getKey(quest));
			PacketSender.sendToServer(new QuestingPacket(PacketTypeNative.DETECT.GetLocation(), tags));
		} else if(btn.id == 3) {
			selTaskId++;
			btnTLeft.enabled = selTaskId > 0;
			btnTRight.enabled = selTaskId < quest.getTasks().size() - 1;
			refreshEmbedded();
		} else if(btn.id == 4) {
			mc.displayGuiScreen(new GuiQuestEditor(this, quest));
		} else if(btn.id == 5) {
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("questID", QuestDatabase.getKey(quest));
			PacketSender.sendToServer(new QuestingPacket(PacketTypeNative.CLAIM.GetLocation(), tags));
		} else if(btn.id == 6) {
			selRewardId--;
			btnRLeft.enabled = selRewardId > 0;
			btnRRight.enabled = selRewardId < quest.getRewards().size() - 1;
			refreshEmbedded();
		} else if(btn.id == 7) {
			selRewardId++;
			btnRLeft.enabled = selRewardId > 0;
			btnRRight.enabled = selRewardId < quest.getRewards().size() - 1;
			refreshEmbedded();
		}
	}

	@Override
	protected void keyTyped(char character, int keyCode) {
		super.keyTyped(character, keyCode);
		btnClaim.enabled = quest.canClaim(mc.thePlayer);
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		btnClaim.enabled = quest.canClaim(mc.thePlayer);
	}

	private void refreshEmbedded() {
		this.embedded.remove(taskRender);
		taskRender = null;
		this.embedded.remove(rewardRender);
		rewardRender = null;
		int tSize = quest.getTasks().size();
		if(taskRender == null && tSize > 0) {
			selTask = quest.getTasks().getAllValues().get(selTaskId % tSize);
			if(selTask != null) {
				taskRender = selTask.getTaskGui(guiLeft + sizeX / 2 + 8, guiTop + 48, sizeX / 2 - 24, sizeY - 96, quest);
				if(taskRender != null) {
					this.embedded.add(taskRender);
				}
			}
		}
		int rSize = quest.getRewards().size();
		if(rewardRender == null && rSize > 0) {
			selReward = quest.getRewards().getAllValues().get(selRewardId % rSize);
			if(selReward != null) {
				rewardRender = selReward.getRewardGui(guiLeft + 16, guiTop + sizeY / 2, sizeX / 2 - 24, sizeY / 2 - 48, quest);
				if(rewardRender != null) {
					this.embedded.add(rewardRender);
				}
			}
		}
	}
}