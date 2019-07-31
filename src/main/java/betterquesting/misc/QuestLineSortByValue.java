package betterquesting.misc;

import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;

import java.util.Comparator;

public class QuestLineSortByValue implements Comparator<IQuestLine> {
	private final IQuestLineDatabase parentDB;

	public QuestLineSortByValue(IQuestLineDatabase parentDB) {
		this.parentDB = parentDB;
	}

	@Override
	public int compare(IQuestLine ql1, IQuestLine ql2) {
		return (int) Math.signum(parentDB.getOrderIndex(ql1 == null ? -1 : parentDB.getKey(ql1)) - parentDB.getOrderIndex(ql2 == null ? -1 : parentDB.getKey(ql2)));
	}
}