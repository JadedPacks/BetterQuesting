package betterquesting.questing.tasks;

import betterquesting.api.misc.IFactory;
import betterquesting.api.placeholders.tasks.FactoryTaskPlaceholder;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.questing.tasks.ITaskRegistry;
import betterquesting.core.BetterQuesting;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskRegistry implements ITaskRegistry {
	public static final TaskRegistry INSTANCE = new TaskRegistry();
	private final HashMap<ResourceLocation, IFactory<? extends ITask>> taskRegistry = new HashMap<>();

	@Override
	public IFactory<? extends ITask> getFactory(ResourceLocation registryName) {
		return taskRegistry.get(registryName);
	}

	@Override
	public List<IFactory<? extends ITask>> getAll() {
		return new ArrayList<>(taskRegistry.values());
	}

	public ITask createTask(ResourceLocation registryName) {
		try {
			IFactory<? extends ITask> factory;
			if(FactoryTaskPlaceholder.INSTANCE.getRegistryName().equals(registryName)) {
				factory = FactoryTaskPlaceholder.INSTANCE;
			} else {
				factory = getFactory(registryName);
			}
			if(factory == null) {
				BetterQuesting.logger.error("Tried to load missing task type '" + registryName + "'! Are you missing an expansion pack?");
				return null;
			}
			return factory.createNew();
		} catch(Exception e) {
			BetterQuesting.logger.error("Unable to instatiate task: " + registryName, e);
			return null;
		}
	}
}