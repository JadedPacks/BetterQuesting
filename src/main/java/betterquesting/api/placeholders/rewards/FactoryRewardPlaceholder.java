package betterquesting.api.placeholders.rewards;

import betterquesting.api.misc.IFactory;
import net.minecraft.util.ResourceLocation;

public class FactoryRewardPlaceholder implements IFactory<RewardPlaceholder> {
	public static final FactoryRewardPlaceholder INSTANCE = new FactoryRewardPlaceholder();
	private final ResourceLocation ID = new ResourceLocation("betterquesting:placeholder");

	@Override
	public ResourceLocation getRegistryName() {
		return ID;
	}

	@Override
	public RewardPlaceholder createNew() {
		return new RewardPlaceholder();
	}
}