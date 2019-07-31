package betterquesting.api.questing.tasks;

import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.misc.IJsonSaveLoad;
import betterquesting.api.questing.IQuest;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public interface ITask extends IJsonSaveLoad<JsonObject> {
	String getUnlocalisedName();
	ResourceLocation getFactoryID();
	void detect(EntityPlayer player, IQuest quest);
	boolean isComplete(UUID uuid);
	void setComplete(UUID uuid);
	void resetUser(UUID uuid);
	void resetAll();
	IJsonDoc getDocumentation();
	@SideOnly(Side.CLIENT)
	IGuiEmbedded getTaskGui(int x, int y, int w, int h, IQuest quest);
	@Nullable
	@SideOnly(Side.CLIENT)
	GuiScreen getTaskEditor(GuiScreen parent, IQuest quest);
}