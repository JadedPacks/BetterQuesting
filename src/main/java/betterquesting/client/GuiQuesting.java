package betterquesting.client;

import java.awt.Color;
import org.lwjgl.opengl.GL11;
import betterquesting.client.buttons.GuiButtonQuesting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class GuiQuesting extends GuiScreen
{
	public static final String numRegex = "[^\\.0123456789-]"; // I keep screwing this up so now it's reusable
	public static final ResourceLocation guiTexture = new ResourceLocation("betterquesting", "textures/gui/editor_gui.png");
	
	protected GuiScreen parent;
	protected String title = "Better Questing";
	
	protected int guiLeft = 0;
	protected int guiTop = 0;
	protected int sizeX = 0;
	protected int sizeY = 0;
	
	public GuiQuesting(GuiScreen parent, String title)
	{
		this.mc = Minecraft.getMinecraft();
		this.fontRendererObj = this.mc.fontRenderer;
		this.parent = parent;
		this.title = title;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		int border = 8; // The minimum distance between the UI and the window edge
		this.sizeX = this.width - border * 2;
		this.sizeY = this.height - border * 2;
		
		this.sizeX = this.sizeX - (this.sizeX%16);
		this.sizeY = this.sizeY - (this.sizeY%16);
		
		this.guiLeft = (this.width - this.sizeX)/2;
		this.guiTop = (this.height - this.sizeY)/2;
		
		this.buttonList.clear();
        this.buttonList.add(new GuiButtonQuesting(0, this.width / 2 - 100, this.guiTop + this.sizeY - 16, I18n.format("gui.done", new Object[0])));
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		if(button.id == 0)
		{
			this.mc.displayGuiScreen(parent);
		}
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		this.mc.renderEngine.bindTexture(guiTexture);
		
		for(int i = 0; i < this.sizeX; i += 16)
		{
			for(int j = 0; j < this.sizeY; j += 16)
			{
				int tx = 16;
				int ty = 16;
				
				if(i == 0)
				{
					tx -= 16;
				} else if(i == this.sizeX - 16)
				{
					tx += 16;
				}
				
				if(j == 0)
				{
					ty -= 16;
				} else if(j == this.sizeY - 16)
				{
					ty += 16;
				}
				
				this.drawTexturedModalRect(i + this.guiLeft, j + this.guiTop, tx, ty, 16, 16);
			}
		}
		
		this.fontRendererObj.drawString(EnumChatFormatting.BOLD + title, this.guiLeft + (sizeX/2) - this.fontRendererObj.getStringWidth(title)/2, this.guiTop + 16, Color.BLACK.getRGB(), false);
		
		super.drawScreen(mx, my, partialTick);
		
		this.mc.renderEngine.bindTexture(guiTexture);
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}
    
    public boolean isWithin(int mx, int my, int startX, int startY, int sizeX, int sizeY)
    {
    	return mx - this.guiLeft >= startX && my - this.guiTop >= startY && mx - this.guiLeft < startX + sizeX && my - this.guiTop < startY + sizeY;
    }
	
    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
	@Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}