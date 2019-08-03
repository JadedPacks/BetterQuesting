package betterquesting.questing.rewards;

import betterquesting.api.misc.IFactory;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.core.BetterQuesting;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class RewardRegistry {
	public static final RewardRegistry INSTANCE = new RewardRegistry();
	public final HashMap<ResourceLocation, IFactory<? extends IReward>> rewardRegistry = new HashMap<>();

	public void registerReward(IFactory<? extends IReward> factory) {
		if(factory == null) {
			throw new NullPointerException("Tried to register null reward");
		} else if(factory.getRegistryName() == null) {
			throw new IllegalArgumentException("Tried to register a reward with a null name: " + factory.getClass());
		}
		if(rewardRegistry.containsKey(factory.getRegistryName()) || rewardRegistry.containsValue(factory)) {
			throw new IllegalArgumentException("Cannot register dupliate reward type: " + factory.getRegistryName());
		}
		rewardRegistry.put(factory.getRegistryName(), factory);
	}

	public IReward createReward(ResourceLocation registryName) {
		IFactory<? extends IReward> factory = rewardRegistry.get(registryName);
		if(factory == null) {
			BetterQuesting.logger.error("Tried to load missing reward type '" + registryName + "'!");
			return null;
		}
		return factory.createNew();
	}
}