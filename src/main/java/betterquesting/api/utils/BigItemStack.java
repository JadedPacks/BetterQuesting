package betterquesting.api.utils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;

public class BigItemStack {
	public int stackSize;
	public String oreDict = "";
	final ItemStack baseStack;

	public BigItemStack(ItemStack stack) {
		baseStack = stack.copy();
		this.stackSize = baseStack.stackSize;
		baseStack.stackSize = 1;
	}

	public BigItemStack(Block block) {
		this(block, 1);
	}

	public BigItemStack(Block block, int amount) {
		this(block, amount, 0);
	}

	public BigItemStack(Block block, int amount, int damage) {
		this(Item.getItemFromBlock(block), amount, damage);
	}

	public BigItemStack(Item item) {
		this(item, 1);
	}

	public BigItemStack(Item item, int amount) {
		this(item, amount, 0);
	}

	public BigItemStack(Item item, int amount, int damage) {
		baseStack = new ItemStack(item, 1, damage);
		this.stackSize = amount;
	}

	public ItemStack getBaseStack() {
		return baseStack;
	}

	public NBTTagCompound GetTagCompound() {
		return baseStack.getTagCompound();
	}

	public void SetTagCompound(NBTTagCompound tags) {
		baseStack.setTagCompound(tags);
	}

	public boolean HasTagCompound() {
		return baseStack.hasTagCompound();
	}

	@Override
	public boolean equals(Object stack) {
		if(stack instanceof ItemStack) {
			return baseStack.isItemEqual((ItemStack) stack) && ItemStack.areItemStackTagsEqual(baseStack, (ItemStack) stack);
		} else {
			return super.equals(stack);
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound tags) {
		baseStack.writeToNBT(tags);
		tags.setInteger("Count", stackSize);
		tags.setString("OreDict", oreDict);
		return tags;
	}

	public ArrayList<ItemStack> getCombinedStacks() {
		ArrayList<ItemStack> list = new ArrayList<>();
		int tmp1 = Math.max(1, stackSize);
		while(tmp1 > 0) {
			int size = Math.min(tmp1, baseStack.getMaxStackSize());
			ItemStack stack = baseStack.copy();
			stack.stackSize = size;
			list.add(stack);
			tmp1 -= size;
		}
		return list;
	}
}