package betterquesting.questing.rewards;

import betterquesting.api.misc.IFactory;
import betterquesting.api.placeholders.rewards.FactoryRewardPlaceholder;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.rewards.IRewardRegistry;
import betterquesting.core.BetterQuesting;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RewardRegistry implements IRewardRegistry {
	public static final RewardRegistry INSTANCE = new RewardRegistry();
	private final HashMap<ResourceLocation, IFactory<? extends IReward>> rewardRegistry = new HashMap<>();

	@Override
	public IFactory<? extends IReward> getFactory(ResourceLocation registryName) {
		return rewardRegistry.get(registryName);
	}

	@Override
	public List<IFactory<? extends IReward>> getAll() {
		return new ArrayList<>(rewardRegistry.values());
	}

	@Override
	public IReward createReward(ResourceLocation registryName) {
		try {
			IFactory<? extends IReward> factory;
			if(FactoryRewardPlaceholder.INSTANCE.getRegistryName().equals(registryName)) {
				factory = FactoryRewardPlaceholder.INSTANCE;
			} else {
				factory = getFactory(registryName);
			}
			if(factory == null) {
				BetterQuesting.logger.error("Tried to load missing reward type '" + registryName + "'! Are you missing an expansion pack?");
				return null;
			}
			return factory.createNew();
		} catch(Exception e) {
			BetterQuesting.logger.error("Unable to instatiate reward: " + registryName, e);
			return null;
		}
	}
}