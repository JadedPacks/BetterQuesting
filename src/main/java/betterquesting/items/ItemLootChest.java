package betterquesting.items;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.BigItemStack;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.rewards.LootGroup;
import betterquesting.questing.rewards.LootRegistry;
import betterquesting.storage.QuestSettings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ItemLootChest extends Item {
	public ItemLootChest() {
		this.setMaxStackSize(1);
		this.setUnlocalizedName("betterquesting.loot_chest");
		this.setTextureName("betterquesting:loot_chest");
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(!world.isRemote) {
			LootGroup group;
			if(stack.getMetadata() == 101) {
				group = LootRegistry.getWeightedGroup(itemRand.nextFloat(), itemRand);
			} else {
				group = LootRegistry.getWeightedGroup(MathHelper.clamp_int(stack.getMetadata(), 0, 100) / 100F, itemRand);
			}
			ArrayList<BigItemStack> loot;
			String title = "Dungeon Loot";
			if(group == null) {
				loot = LootRegistry.getStandardLoot(itemRand);
			} else {
				title = group.name;
				loot = group.getRandomReward(itemRand);
				if(loot == null || loot.size() <= 0) {
					BetterQuesting.logger.warn("Unable to get random loot entry from group " + group.name + "! Reason: Contains 0 loot entries");
					title = "Dungeon Loot";
					loot = LootRegistry.getStandardLoot(itemRand);
				}
			}
			for(BigItemStack s1 : loot) {
				for(ItemStack s2 : s1.getCombinedStacks()) {
					if(!player.inventory.addItemStackToInventory(s2)) {
						player.dropPlayerItemWithRandomChoice(s2, false);
					}
				}
				player.inventory.markDirty();
				player.inventoryContainer.detectAndSendChanges();
			}
			if(player instanceof EntityPlayerMP) {
				NBTTagCompound tags = new NBTTagCompound();
				tags.setInteger("ID", 0);
				tags.setString("title", title);
				NBTTagList list = new NBTTagList();
				for(BigItemStack stac : loot) {
					list.appendTag(stac.writeToNBT(new NBTTagCompound()));
				}
				tags.setTag("rewards", list);
				PacketSender.sendToPlayer(new QuestingPacket(PacketTypeNative.LOOT_CLAIM.GetLocation(), tags), (EntityPlayerMP) player);
			}
		}
		if(!player.capabilities.isCreativeMode) {
			stack.stackSize--;
		}
		return stack;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 25));
		list.add(new ItemStack(item, 1, 50));
		list.add(new ItemStack(item, 1, 75));
		list.add(new ItemStack(item, 1, 100));
		list.add(new ItemStack(item, 1, 101));
		list.add(new ItemStack(item, 1, 102));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		if(stack.getMetadata() > 101) {
			list.add(StatCollector.translateToLocal("betterquesting.btn.edit"));
		} else if(QuestSettings.edit) {
			if(stack.getMetadata() == 101) {
				list.add(StatCollector.translateToLocalFormatted("betterquesting.tooltip.loot_chest", "???"));
			} else {
				list.add(StatCollector.translateToLocalFormatted("betterquesting.tooltip.loot_chest", MathHelper.clamp_int(stack.getMetadata(), 0, 100) + "%"));
			}
		}
	}
}