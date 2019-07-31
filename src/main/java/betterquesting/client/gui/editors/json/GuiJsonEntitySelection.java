package betterquesting.client.gui.editors.json;

import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiBigTextField;
import betterquesting.api.client.gui.controls.GuiButtonStorage;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingButtons;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.RenderUtils;
import betterquesting.core.BetterQuesting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Modifier;
import java.util.*;

@SideOnly(Side.CLIENT)
public class GuiJsonEntitySelection extends GuiScreenThemed {
	private Entity entity;
	private final ICallback<Entity> callback;
	private final List<String> entityNames = new ArrayList<>();
	private GuiBigTextField searchField;
	private GuiScrollingButtons btnList;
	private String searchTxt = "";
	private Iterator<String> searching = null;

	public GuiJsonEntitySelection(GuiScreen parent, ICallback<Entity> callback, Entity entity) {
		super(parent, "betterquesting.title.select_entity");
		this.entity = entity;
		this.callback = callback;
		if(this.entity == null) {
			this.entity = new EntityPig(Minecraft.getMinecraft().theWorld);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		entityNames.addAll((Collection<String>) EntityList.stringToClassMapping.keySet());
		Collections.sort(entityNames);
		this.searchField = new GuiBigTextField(mc.fontRendererObj, guiLeft + sizeX / 2 + 1, guiTop + 33, sizeX / 2 - 18, 14);
		this.searchField.setWatermark(I18n.format("betterquesting.gui.search"));
		this.searchField.setMaxStringLength(Integer.MAX_VALUE);
		btnList = new GuiScrollingButtons(mc, guiLeft + sizeX / 2, guiTop + 48, sizeX / 2 - 16, sizeY - 80);
		this.embedded.add(btnList);
		this.searching = entityNames.iterator();
		this.updateSearch();
	}

	@Override
	public void mouseClicked(int mx, int my, int click) {
		super.mouseClicked(mx, my, click);
		this.searchField.mouseClicked(mx, my, click);
		GuiButtonThemed btn = btnList.getButtonUnderMouse(mx, my);
		if(btn != null && btn.mousePressed(mc, mx, my) && click == 0) {
			btn.playPressSound(mc.getSoundHandler());
			actionPerformed(btn);
		}
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if(button.id == 0 && callback != null) {
			callback.setValue(entity);
		} else if(button.id == 1) {
			Entity tmpE = EntityList.createEntityByName(((GuiButtonStorage<String>) button).getStored(), this.mc.theWorld);
			if(tmpE != null) {
				try {
					tmpE.readFromNBT(new NBTTagCompound());
					tmpE.isDead = false;
					entity = tmpE;
				} catch(Exception e) {
					BetterQuesting.logger.error("Failed to init selected entity", e);
				}
			}
		}
		super.actionPerformed(button);
	}

	@Override
	public void drawScreen(int mx, int my, float partialTick) {
		this.updateSearch();
		super.drawScreen(mx, my, partialTick);
		this.searchField.drawTextBox(mx, my, partialTick);
		if(entity != null) {
			GL11.glPushMatrix();
			GL11.glColor4f(1F, 1F, 1F, 1F);
			float angle = ((float) Minecraft.getSystemTime() % 30000F) / 30000F * 360F;
			float scale = 64F;
			if(entity.height * scale > this.sizeY / 2F) {
				scale = (this.sizeY / 2F) / entity.height;
			}
			if(entity.width * scale > this.sizeX / 4F) {
				scale = (this.sizeX / 4F) / entity.width;
			}
			try {
				RenderUtils.RenderEntity(this.guiLeft + this.sizeX / 4, this.guiTop + this.sizeY / 2 + MathHelper.ceiling_float_int(entity.height / 2F * scale), (int) scale, angle, 0F, entity);
			} catch(Exception ignored) {}
			GL11.glPopMatrix();
		}
	}

	private void updateSearch() {
		if(searching == null) {
			return;
		} else if(!searching.hasNext()) {
			searching = null;
			return;
		}
		int btnWidth = btnList.getListWidth();
		while(searching.hasNext()) {
			String key = searching.next();
			boolean abs = Modifier.isAbstract(((Class<?>) EntityList.stringToClassMapping.get(key)).getModifiers());
			if(!abs && key.toLowerCase().contains(searchTxt)) {
				GuiButtonStorage<String> btn = new GuiButtonStorage<>(1, 0, 0, btnWidth, 20, key);
				btn.setStored(key);
				btnList.addButtonRow(btn);
			}
		}
	}

	@Override
	public void keyTyped(char c, int keyCode) {
		super.keyTyped(c, keyCode);
		searchField.textboxKeyTyped(c, keyCode);
		if(!searchField.getText().equalsIgnoreCase(searchTxt)) {
			btnList.getEntryList().clear();
			searchTxt = searchField.getText().toLowerCase();
			searching = entityNames.iterator();
		}
	}
}