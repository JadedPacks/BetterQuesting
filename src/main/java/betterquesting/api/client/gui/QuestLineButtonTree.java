package betterquesting.api.client.gui;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class QuestLineButtonTree {
	private final IQuestLine line;
	private final ArrayList<GuiButtonQuestInstance> buttonTree = new ArrayList<>();
	private int treeW = 0;
	private int treeH = 0;

	public QuestLineButtonTree(IQuestLine line) {
		this.line = line;
		RebuildTree();
	}

	public int getWidth() {
		return treeW;
	}

	public int getHeight() {
		return treeH;
	}

	public IQuestLine getQuestLine() {
		return line;
	}

	public List<GuiButtonQuestInstance> getButtonTree() {
		return buttonTree;
	}

	public GuiButtonQuestInstance getButtonAt(int x, int y) {
		if(line == null) {
			return null;
		}
		int id = line.getQuestAt(x, y);
		IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(id);
		if(quest == null) {
			return null;
		}
		for(GuiButtonQuestInstance btn : buttonTree) {
			if(btn.getQuest() == quest) {
				return btn;
			}
		}
		return null;
	}

	public void RebuildTree() {
		buttonTree.clear();
		treeW = 0;
		treeH = 0;
		if(line == null) {
			return;
		}
		for(int id : line.getAllKeys()) {
			IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(id);
			IQuestLineEntry entry = line.getValue(id);
			if(quest != null && entry != null) {
				buttonTree.add(new GuiButtonQuestInstance(0, entry.getPosX(), entry.getPosY(), entry.getSize(), entry.getSize(), quest));
			}
		}
		for(GuiButtonQuestInstance btn : buttonTree) {
			if(btn == null) {
				continue;
			}
			treeW = Math.max(btn.xPosition + btn.width, treeW);
			treeH = Math.max(btn.yPosition + btn.height, treeH);
			for(GuiButtonQuestInstance b2 : buttonTree) {
				if(b2 == null || btn == b2 || btn.getQuest() == null) {
					continue;
				}
				if(btn.getQuest().getPrerequisites().contains(b2.getQuest())) {
					btn.addParent(b2);
				}
			}
		}
	}
}