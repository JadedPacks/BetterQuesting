package betterquesting.client;

import betterquesting.api.utils.RenderUtils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public class QuestNotification {
	public static void ScheduleNotice(String mainTxt, String subTxt, ItemStack icon, String sound) {
		notices.add(new QuestNotice(mainTxt, subTxt, icon, sound));
	}

	static final ArrayList<QuestNotice> notices = new ArrayList<>();

	@SubscribeEvent
	public void onDrawScreen(RenderGameOverlayEvent.Post event) {
		if(event.type != RenderGameOverlayEvent.ElementType.HELMET || notices.size() <= 0) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		int width = resolution.getScaledWidth();
		int height = resolution.getScaledHeight();
		QuestNotice notice = notices.get(0);
		if(!notice.init) {
			if(mc.isGamePaused() || mc.currentScreen != null) {
				return;
			}
			notice.init = true;
			notice.startTime = Minecraft.getSystemTime();
			mc.getSoundHandler().playSound(PositionedSoundRecord.createPositionedSoundRecord(new ResourceLocation(notice.sound), 1.0F));
		}
		if(notice.getTime() >= 6F) {
			notices.remove(0);
			return;
		}
		GL11.glPushMatrix();
		float scale = width > 600 ? 1.5F : 1F;
		GL11.glScalef(scale, scale, scale);
		width = MathHelper.ceiling_float_int(width / scale);
		height = MathHelper.ceiling_float_int(height / scale);
		float alpha = notice.getTime() <= 4F ? Math.min(1F, notice.getTime()) : Math.max(0F, 5F - notice.getTime());
		alpha = MathHelper.clamp_float(alpha, 0.02F, 1F);
		int color = new Color(1F, 1F, 1F, alpha).getRGB();
		GL11.glColor4f(1F, 1F, 1F, alpha);
		if(notice.icon != null) {
			RenderUtils.RenderItemStack(mc, notice.icon, width / 2 - 8, height / 4 - 20, "", new Color(1F, 1F, 1F, alpha));
		}
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		String tmp = EnumChatFormatting.UNDERLINE + "" + EnumChatFormatting.BOLD + StatCollector.translateToLocal(notice.mainTxt);
		int txtW = mc.fontRendererObj.getStringWidth(tmp);
		mc.fontRendererObj.drawString(tmp, width / 2 - txtW / 2, height / 4, color, false);
		tmp = StatCollector.translateToLocal(notice.subTxt);
		txtW = mc.fontRendererObj.getStringWidth(tmp);
		mc.fontRendererObj.drawString(tmp, width / 2 - txtW / 2, height / 4 + 12, color, false);
		GL11.glPopMatrix();
	}

	public static class QuestNotice {
		long startTime;
		public boolean init = false;
		public final String mainTxt;
		public final String subTxt;
		public final String sound;
		public final ItemStack icon;

		public QuestNotice(String mainTxt, String subTxt, ItemStack icon, String sound) {
			this.startTime = Minecraft.getSystemTime();
			this.mainTxt = mainTxt;
			this.subTxt = subTxt;
			this.icon = icon;
			this.sound = sound;
		}

		public float getTime() {
			return (Minecraft.getSystemTime() - startTime) / 1000F;
		}
	}
}