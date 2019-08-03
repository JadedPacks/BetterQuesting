package betterquesting.items;

import betterquesting.core.BetterQuesting;
import betterquesting.questing.party.PartyInstance;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

public class ItemExtraLife extends Item {
	IIcon iconQuarter, iconHalf;

	public ItemExtraLife() {
		this.setTextureName("betterquesting:heart");
		this.setUnlocalizedName("betterquesting.extra_life");
		this.setCreativeTab(BetterQuesting.tabQuesting);
		this.setHasSubtypes(true);
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(stack.getMetadata() != 0) {
			return stack;
		} else if(QuestSettings.hardcore) {
			if(!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
			int lives;
			PartyInstance party = PartyManager.getUserParty(NameCache.getQuestingUUID(player));
			if(party == null || !party.sharedLives) {
				lives = LifeDatabase.getLives(NameCache.getQuestingUUID(player));
			} else {
				lives = LifeDatabase.getLives(party);
			}
			if(lives >= QuestSettings.livesMax) {
				if(!world.isRemote) {
					player.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED.toString()).appendSibling(new ChatComponentTranslation("betterquesting.gui.full_lives")));
				}
				return stack;
			}
			world.playSoundAtEntity(player, "random.levelup", 1F, 1F);
			if(!world.isRemote) {
				if(party == null || !party.sharedLives) {
					LifeDatabase.setLives(NameCache.getQuestingUUID(player), lives + 1);
				} else {
					LifeDatabase.setLives(party, lives + 1);
				}
				player.addChatComponentMessage(new ChatComponentTranslation("betterquesting.gui.remaining_lives", EnumChatFormatting.YELLOW.toString() + (lives + 1)));
			}
		} else if(!world.isRemote) {
			player.addChatComponentMessage(new ChatComponentTranslation("betterquesting.msg.heart_disabled"));
		}
		return stack;
	}

	public String getUnlocalizedName(ItemStack stack) {
		switch(stack.getMetadata() % 3) {
			case 2:
				return this.getUnlocalizedName() + ".quarter";
			case 1:
				return this.getUnlocalizedName() + ".half";
			default:
				return this.getUnlocalizedName() + ".full";
		}
	}

	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 1));
		list.add(new ItemStack(item, 1, 2));
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int dmg) {
		switch(dmg % 3) {
			case 2:
				return iconQuarter;
			case 1:
				return iconHalf;
			default:
				return itemIcon;
		}
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		iconQuarter = register.registerIcon(this.getIconString() + "_quarter");
		iconHalf = register.registerIcon(this.getIconString() + "_half");
		itemIcon = register.registerIcon(this.getIconString() + "_full");
	}
}