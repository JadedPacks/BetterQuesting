package betterquesting.api.client.gui.misc;

import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.misc.ICallback;
import com.google.gson.JsonElement;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;

@SideOnly(Side.CLIENT)
public interface IGuiHelper {
	<T extends JsonElement> void openJsonEditor(GuiScreen parent, ICallback<T> callback, T json, IJsonDoc jdoc);
	void openTextEditor(GuiScreen parent, ICallback<String> editor, String text);
}