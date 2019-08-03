package betterquesting.questing.tasks;

import betterquesting.api.misc.IFactory;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.core.BetterQuesting;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class TaskRegistry {
	public static final TaskRegistry INSTANCE = new TaskRegistry();
	public final HashMap<ResourceLocation, IFactory<? extends ITask>> taskRegistry = new HashMap<>();

	public void registerTask(IFactory<? extends ITask> factory) {
		if(factory == null) {
			throw new NullPointerException("Tried to register null reward");
		} else if(factory.getRegistryName() == null) {
			throw new IllegalArgumentException("Tried to register a reward with a null name: " + factory.getClass());
		}
		if(taskRegistry.containsKey(factory.getRegistryName()) || taskRegistry.containsValue(factory)) {
			throw new IllegalArgumentException("Cannot register dupliate reward type: " + factory.getRegistryName());
		}
		taskRegistry.put(factory.getRegistryName(), factory);
	}

	public ITask createTask(ResourceLocation registryName) {
		IFactory<? extends ITask> factory = taskRegistry.get(registryName);
		if(factory == null) {
			BetterQuesting.logger.error("Tried to load missing task type '" + registryName + "'!");
			return null;
		}
		return factory.createNew();
	}
}