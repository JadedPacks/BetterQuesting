package betterquesting.network;

import net.minecraft.util.ResourceLocation;

public enum PacketTypeNative {
	QUEST_DATABASE,
	PARTY_DATABASE,
	LINE_DATABASE,
	LIFE_DATABASE,
	QUEST_SYNC,
	QUEST_EDIT,
	PARTY_SYNC,
	PARTY_EDIT,
	LINE_SYNC,
	LINE_EDIT,
	DETECT,
	CLAIM,
	NAME_CACHE,
	NOTIFICATION,
	SETTINGS,
	// Standard Expansion
	LOOT_SYNC,
	LOOT_CLAIM,
	CHECKBOX,
	CHOICE
	;

	private final ResourceLocation ID;

	PacketTypeNative() {
		this.ID = new ResourceLocation("betterquesting:" + this.toString().toLowerCase());
	}

	public ResourceLocation GetLocation() {
		return ID;
	}
}