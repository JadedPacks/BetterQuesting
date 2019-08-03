package betterquesting.client.gui;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.client.gui.editors.json.scrolling.GuiJsonEditor;
import betterquesting.client.gui.party.GuiManageParty;
import betterquesting.client.gui.party.GuiNoParty;
import betterquesting.network.PacketSender;
import betterquesting.questing.party.PartyInstance;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiHome extends GuiScreenThemed implements INeedsRefresh {
	private final ResourceLocation homeGui = new ResourceLocation("betterquesting:textures/gui/default_title.png");
	private GuiButtonThemed btnSet;

	public GuiHome(GuiScreen parent) {
		super(parent, "");
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		int bw = (sizeX - 32) / 4;
		GuiButtonThemed btn = new GuiButtonThemed(0, guiLeft + 16, guiTop + sizeY - 48, bw, 32, I18n.format("betterquesting.home.exit"), true);
		this.buttonList.add(btn);
		btn = new GuiButtonThemed(1, guiLeft + 16 + bw, guiTop + sizeY - 48, bw, 32, I18n.format("betterquesting.home.quests"), true);
		this.buttonList.add(btn);
		btn = new GuiButtonThemed(2, guiLeft + 16 + bw * 2, guiTop + sizeY - 48, bw, 32, I18n.format("betterquesting.home.party"), true);
		this.buttonList.add(btn);
		btnSet = new GuiButtonThemed(4, guiLeft + 16, guiTop + 16, 16, 16, "", true);
		btnSet.setIcon(new ResourceLocation("betterquesting:textures/gui/editor_icons.png"), 0, 16, 16, 16, false);
		btnSet.enabled = btnSet.visible = QuestSettings.canUserEdit(mc.thePlayer);
		this.buttonList.add(btnSet);
	}

	@Override
	public void refreshGui() {
		this.initGui();
	}

	@Override
	public void drawScreen(int mx, int my, float partialTick) {
		super.drawScreen(mx, my, partialTick);
		mc.renderEngine.bindTexture(homeGui);
		GL11.glPushMatrix();
		float sw = (sizeX - 32) / 256F;
		float sh = (sizeY - 64) / 128F;
		GL11.glTranslatef(guiLeft + 16, guiTop + 16, 0F);
		GL11.glScalef(sw, sh, 1F);
		this.drawTexturedModalRect(0, 0, 0, 0, 256, 128);
		GL11.glPopMatrix();
		int tx = ((sizeX - 32) / 2) - 128 + guiLeft + 16;
		int ty = guiTop + 16;
		this.drawTexturedModalRect(tx, ty, 0, 128, 256, 128);
		btnSet.drawButton(mc, mx, my);
	}

	@Override
	public void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if(button.id == 1) {
			mc.displayGuiScreen(new GuiQuestLinesMain(this));
		} else if(button.id == 2) {
			PartyInstance party = PartyManager.getUserParty(NameCache.getQuestingUUID(mc.thePlayer));
			if(party != null) {
				mc.displayGuiScreen(new GuiManageParty(this, party));
			} else {
				mc.displayGuiScreen(new GuiNoParty(this));
			}
		} else if(button.id == 4) {
			mc.displayGuiScreen(new GuiJsonEditor(this, QuestSettings.writeToJson(new JsonObject()), value -> {
				QuestSettings.readFromJson(value);
				PacketSender.sendToServer(QuestSettings.getSyncPacket());
			}));
		}
	}
}