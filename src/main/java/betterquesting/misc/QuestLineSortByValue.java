package betterquesting.misc;

import betterquesting.questing.QuestLine;
import betterquesting.questing.QuestLineDatabase;

import java.util.Comparator;

public class QuestLineSortByValue implements Comparator<QuestLine> {
	@Override
	public int compare(QuestLine ql1, QuestLine ql2) {
		return (int) Math.signum(QuestLineDatabase.getOrderIndex(ql1 == null ? -1 : QuestLineDatabase.getKey(ql1)) - QuestLineDatabase.getOrderIndex(ql2 == null ? -1 : QuestLineDatabase.getKey(ql2)));
	}
}