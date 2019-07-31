package betterquesting.api.utils;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.tasks.ITask;
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
		UUID uuid = QuestingAPI.getQuestingUUID(player);
		HashMap<Integer, List<Integer>> pCache = new HashMap<>();
		IQuestDatabase questDB = QuestingAPI.getAPI(ApiReference.QUEST_DB);
		List<Integer> idList = questDB.getAllKeys();
		for(int qID : idList) {
			IQuest quest = questDB.getValue(qID);
			if(quest == null || (!quest.isUnlocked(uuid) && !quest.getProperties().getProperty(NativeProps.LOCKED_PROGRESS))) {
				continue;
			} else if((!quest.canSubmit(player) || quest.getProperties().getProperty(NativeProps.REPEAT_TIME).intValue() < 0) || (!quest.getProperties().getProperty(NativeProps.AUTO_CLAIM) && !quest.hasClaimed(uuid))) {
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

	public List<IQuest> getActiveQuests(UUID uuid) {
		List<IQuest> list = new ArrayList<>();
		HashMap<Integer, List<Integer>> pCache = rawCache.get(uuid);
		pCache = pCache != null ? pCache : new HashMap<>();
		for(int id : pCache.keySet()) {
			IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(id);
			if(quest != null) {
				list.add(quest);
			}
		}
		return list;
	}

	public Map<ITask, IQuest> getActiveTasks(UUID uuid) {
		return getActiveTasks(uuid, ITask.class);
	}

	public <T extends ITask> Map<T, IQuest> getActiveTasks(UUID uuid, Class<T> type) {
		Map<T, IQuest> list = new HashMap<>();
		HashMap<Integer, List<Integer>> pCache = rawCache.get(uuid);
		pCache = pCache != null ? pCache : new HashMap<>();
		for(Entry<Integer, List<Integer>> entry : pCache.entrySet()) {
			IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(entry.getKey());
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