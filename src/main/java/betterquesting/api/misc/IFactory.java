package betterquesting.api.misc;

import net.minecraft.util.ResourceLocation;

public interface IFactory<T> {
	ResourceLocation getRegistryName();
	T createNew();
}