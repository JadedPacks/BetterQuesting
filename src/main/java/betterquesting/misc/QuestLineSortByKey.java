package betterquesting.misc;

import betterquesting.questing.QuestLineDatabase;

import java.util.Comparator;

public class QuestLineSortByKey implements Comparator<Integer> {
	@Override
	public int compare(Integer ql1, Integer ql2) {
		return (int) Math.signum(QuestLineDatabase.getOrderIndex(ql1 == null ? -1 : ql1) - QuestLineDatabase.getOrderIndex(ql2 == null ? -1 : ql2));
	}
}