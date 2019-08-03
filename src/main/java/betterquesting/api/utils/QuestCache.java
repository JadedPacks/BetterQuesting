package betterquesting.api.utils;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.storage.NameCache;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;
import java.util.Map.Entry;

public class QuestCache {
	public static final QuestCache INSTANCE = new QuestCache();
	private final HashMap<UUID, HashMap<Integer, List<Integer>>> rawCache = new HashMap<>();

	public void updateCache(EntityPlayer player) {
		if(player == null) {
			return;
		}
		UUID uuid = NameCache.getQuestingUUID(player);
		HashMap<Integer, List<Integer>> pCache = new HashMap<>();
		List<Integer> idList = QuestDatabase.getAllKeys();
		for(int qID : idList) {
			QuestInstance quest = QuestDatabase.getValue(qID);
			if(quest == null || (!quest.isUnlocked(uuid))) {
				continue;
			} else if((!quest.canSubmit(player) || quest.repeat < 0) || !quest.hasClaimed(uuid)) {
				continue;
			}
			List<Integer> tList = new ArrayList<>();
			for(int tID : quest.getTasks().getAllKeys()) {
				ITask task = quest.getTasks().getValue(tID);
				if(task != null && !task.isComplete(uuid)) {
					tList.add(tID);
				}
			}
			pCache.put(qID, tList);
		}
		rawCache.put(uuid, pCache);
	}

	public List<QuestInstance> getActiveQuests(UUID uuid) {
		List<QuestInstance> list = new ArrayList<>();
		HashMap<Integer, List<Integer>> pCache = rawCache.get(uuid);
		pCache = pCache != null ? pCache : new HashMap<>();
		for(int id : pCache.keySet()) {
			QuestInstance quest = QuestDatabase.getValue(id);
			if(quest != null) {
				list.add(quest);
			}
		}
		return list;
	}

	public Map<ITask, QuestInstance> getActiveTasks(UUID uuid) {
		return getActiveTasks(uuid, ITask.class);
	}

	public <T extends ITask> Map<T, QuestInstance> getActiveTasks(UUID uuid, Class<T> type) {
		Map<T, QuestInstance> list = new HashMap<>();
		HashMap<Integer, List<Integer>> pCache = rawCache.get(uuid);
		pCache = pCache != null ? pCache : new HashMap<>();
		for(Entry<Integer, List<Integer>> entry : pCache.entrySet()) {
			QuestInstance quest = QuestDatabase.getValue(entry.getKey());
			if(quest == null) {
				continue;
			}
			for(int tID : entry.getValue()) {
				ITask task = quest.getTasks().getValue(tID);
				if(task != null && type.isAssignableFrom(task.getClass())) {
					list.put((T) task, quest);
				}
			}
		}
		return list;
	}

	public void reset() {
		rawCache.clear();
	}
}