package betterquesting.client;

import betterquesting.core.BetterQuesting;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTabQuesting extends CreativeTabs {
	public CreativeTabQuesting() {
		super("betterquesting");
	}

	@Override
	public Item getTabIconItem() {
		return BetterQuesting.extraLife;
	}
}