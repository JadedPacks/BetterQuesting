package betterquesting.api.client.gui.controls;

import betterquesting.api.client.gui.QuestLineButtonTree;
import betterquesting.api.questing.IQuestLine;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.resources.I18n;

@SideOnly(Side.CLIENT)
public class GuiButtonQuestLine extends GuiButtonThemed {
	private final IQuestLine line;
	private final QuestLineButtonTree tree;

	public GuiButtonQuestLine(int id, int x, int y, int width, int height, IQuestLine line) {
		super(id, x, y, width, height, I18n.format(line.getUnlocalisedName()), true);
		this.line = line;
		this.tree = new QuestLineButtonTree(line);
	}

	public IQuestLine getQuestLine() {
		return line;
	}

	public QuestLineButtonTree getButtonTree() {
		return tree;
	}
}