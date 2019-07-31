package betterquesting.api.placeholders.tasks;

import betterquesting.api.misc.IFactory;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskPlaceholder implements IFactory<TaskPlaceholder> {
	public static final FactoryTaskPlaceholder INSTANCE = new FactoryTaskPlaceholder();
	private final ResourceLocation ID = new ResourceLocation("betterquesting:placeholder");

	@Override
	public ResourceLocation getRegistryName() {
		return ID;
	}

	@Override
	public TaskPlaceholder createNew() {
		return new TaskPlaceholder();
	}
}