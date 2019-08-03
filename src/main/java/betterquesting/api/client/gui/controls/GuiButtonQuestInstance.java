package betterquesting.api.client.gui.controls;

import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.client.themes.ThemeStandard;
import betterquesting.questing.QuestInstance;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class GuiButtonQuestInstance extends GuiButtonThemed {
	private final QuestInstance quest;
	private final List<GuiButtonQuestInstance> parents = new ArrayList<>();

	public GuiButtonQuestInstance(int id, int x, int y, int w, int h, QuestInstance quest) {
		super(id, x, y, w, h, "", false);
		this.quest = quest;
	}

	public void addParent(GuiButtonQuestInstance btn) {
		parents.add(btn);
	}

	public List<GuiButtonQuestInstance> getParents() {
		return parents;
	}

	public QuestInstance getQuest() {
		return quest;
	}

	@Override
	public void drawButton(Minecraft mc, int mx, int my) {
		UUID playerID = NameCache.getQuestingUUID(mc.thePlayer);
		if(QuestSettings.hardcore) {
			this.enabled = this.visible = true;
		} else if(mc.thePlayer == null) {
			this.enabled = false;
			this.visible = true;
		} else {
			this.visible = isQuestShown(playerID);
			this.enabled = this.visible && quest.isUnlocked(playerID);
		}
		if(this.visible) {
			mc.getTextureManager().bindTexture(ThemeStandard.getGuiTexture());
			GL11.glColor4f(1F, 1F, 1F, 1F);
			this.hovered = this.mousePressed(mc, mx, my);
			for(GuiButtonQuestInstance p : parents) {
				if(!p.visible) {
					continue;
				}
				float lsx = p.xPosition + p.width / 2F;
				float lsy = p.yPosition + p.height / 2F;
				float lsw = p.width / 2F;
				float lsh = p.height / 2F;
				float lex = xPosition + width / 2F;
				float ley = yPosition + height / 2F;
				float lew = width / 2F;
				float leh = height / 2F;
				double la = Math.atan2(ley - lsy, lex - lsx);
				double dx = Math.cos(la) * 16;
				double dy = Math.sin(la) * 16;
				lsx += MathHelper.clamp_float((float) dx, -lsw, lsw);
				lsy += MathHelper.clamp_float((float) dy, -lsh, lsh);
				la = Math.atan2(lsy - ley, lsx - lex);
				dx = Math.cos(la) * 16;
				dy = Math.sin(la) * 16;
				lex += MathHelper.clamp_float((float) dx, -lew, lew);
				ley += MathHelper.clamp_float((float) dy, -leh, leh);
				ThemeStandard.drawLine(quest, playerID, lsx, lsy, lex, ley);
			}
			ThemeStandard.drawIcon(quest, playerID, xPosition, yPosition, width, height, mx, my);
			this.mouseDragged(mc, mx, my);
		}
	}

	public boolean isQuestShown(UUID uuid) {
		if(QuestSettings.canUserEdit(mc.thePlayer) || quest.visibility == EnumQuestVisibility.ALWAYS) {
			return true;
		} else if(quest.visibility == EnumQuestVisibility.HIDDEN) {
			return false;
		} else if(quest.visibility == EnumQuestVisibility.UNLOCKED) {
			return quest.isUnlocked(uuid) || quest.isComplete(uuid);
		} else if(quest.visibility == EnumQuestVisibility.NORMAL) {
			if(!quest.isComplete(uuid)) {
				for(GuiButtonQuestInstance p : parents) {
					if(!p.quest.isUnlocked(uuid)) {
						return false;
					}
				}
			}
			return true;
		} else if(quest.visibility == EnumQuestVisibility.COMPLETED) {
			return quest.isComplete(uuid);
		} else if(quest.visibility == EnumQuestVisibility.CHAIN) {
			for(GuiButtonQuestInstance q : parents) {
				if(q.isQuestShown(uuid)) {
					return true;
				}
			}
			return parents.size() <= 0;
		}
		return true;
	}
}