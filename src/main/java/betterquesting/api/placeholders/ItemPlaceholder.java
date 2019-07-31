package betterquesting.api.placeholders;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.List;

public class ItemPlaceholder extends Item {
	public static final Item placeholder = new ItemPlaceholder();

	public ItemPlaceholder() {
		this.setTextureName("betterquesting:placeholder");
		this.setUnlocalizedName("betterquesting.placeholder");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		if(!stack.hasTagCompound()) {
			list.add("ERROR: Original information missing!");
			return;
		}
		list.add("Original ID: " + stack.getTagCompound().getString("orig_id") + "/" + stack.getTagCompound().getInteger("orig_meta"));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {
		if(!stack.hasTagCompound() || !(entity instanceof EntityPlayer) || world.getTotalWorldTime() % 20 != 0) {
			return;
		}
		EntityPlayer player = (EntityPlayer) entity;
		NBTTagCompound tags = stack.getTagCompound();
		Item i = (Item) Item.itemRegistry.getObject(tags.getString("orig_id"));
		int m = stack.getMaxStackSize() > 0 ? stack.getMetadata() : tags.getInteger("orig_meta");
		NBTTagCompound t = tags.hasKey("orig_tag") ? tags.getCompoundTag("orig_tag") : null;
		if(i != null) {
			ItemStack converted = new ItemStack(i, stack.stackSize, m);
			converted.stackTagCompound = t;
			player.inventory.setInventorySlotContents(slot, converted);
		}
	}
}