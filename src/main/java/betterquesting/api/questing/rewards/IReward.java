package betterquesting.api.questing.rewards;

import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.questing.QuestInstance;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public interface IReward {
	String getUnlocalisedName();
	ResourceLocation getFactoryID();
	boolean canClaim(EntityPlayer player, QuestInstance quest);
	void claimReward(EntityPlayer player, QuestInstance quest);
	@SideOnly(Side.CLIENT)
	IGuiEmbedded getRewardGui(int x, int y, int w, int h, QuestInstance quest);
	@Nullable
	@SideOnly(Side.CLIENT)
	GuiScreen getRewardEditor(GuiScreen parent, QuestInstance quest);
	// IJsonSaveLoad
	JsonObject writeToJson(JsonObject json);
	void readFromJson(JsonObject json);
}