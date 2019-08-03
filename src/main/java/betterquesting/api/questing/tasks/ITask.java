package betterquesting.api.questing.tasks;

import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.network.QuestingPacket;
import betterquesting.questing.QuestInstance;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public interface ITask {
	String getUnlocalisedName();
	ResourceLocation getFactoryID();
	void detect(EntityPlayer player, QuestInstance quest);
	boolean isComplete(UUID uuid);
	void setComplete(UUID uuid);
	void resetUser(UUID uuid);
	void resetAll();
	@SideOnly(Side.CLIENT)
	IGuiEmbedded getTaskGui(int x, int y, int w, int h, QuestInstance quest);
	@Nullable
	@SideOnly(Side.CLIENT)
	GuiScreen getTaskEditor(GuiScreen parent, QuestInstance quest);
	// IJsonSaveLoad
	JsonObject writeToJson(JsonObject json);
	void readFromJson(JsonObject json);
	// IDataSync
	QuestingPacket getSyncPacket();
	void readPacket(NBTTagCompound payload);
}