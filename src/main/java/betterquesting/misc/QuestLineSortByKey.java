package betterquesting.misc;

import betterquesting.api.questing.IQuestLineDatabase;

import java.util.Comparator;

public class QuestLineSortByKey implements Comparator<Integer> {
	private final IQuestLineDatabase parentDB;

	public QuestLineSortByKey(IQuestLineDatabase parentDB) {
		this.parentDB = parentDB;
	}

	@Override
	public int compare(Integer ql1, Integer ql2) {
		return (int) Math.signum(parentDB.getOrderIndex(ql1 == null ? -1 : ql1) - parentDB.getOrderIndex(ql2 == null ? -1 : ql2));
	}
}