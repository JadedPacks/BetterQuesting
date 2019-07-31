package betterquesting.misc;

import betterquesting.core.BetterQuesting;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class QuestResourcesFolder implements IResourcePack {
	static final File rootFolder = new File("config/betterquesting/resources/");

	@Override
	public InputStream getInputStream(ResourceLocation location) throws IOException {
		if(!resourceExists(location)) {
			return null;
		}
		return new FileInputStream(new File(rootFolder.getPath() + "/" + location.getResourceDomain(), location.getResourcePath()));
	}

	@Override
	public boolean resourceExists(ResourceLocation location) {
		File res = new File(rootFolder.getPath() + "/" + location.getResourceDomain(), location.getResourcePath());
		return res.exists();
	}

	@Override
	public Set<String> getResourceDomains() {
		if(!rootFolder.exists()) {
			rootFolder.mkdirs();
		}
		String[] content = rootFolder.list();
		HashSet<String> folders = new HashSet<>();
		if(content == null) {
			return folders;
		}
		for(String s : content) {
			File f = new File(rootFolder, s);
			if(f.exists() && f.isDirectory()) {
				if(!f.getName().equals(f.getName().toLowerCase())) {
					logNameNotLowercase(f.getName(), f.toString());
				} else {
					folders.add(f.getName());
				}
			}
		}
		return folders;
	}

	@Override
	public IMetadataSection getPackMetadata(IMetadataSerializer p_135058_1_, String p_135058_2_) {
		return null;
	}

	@Override
	public BufferedImage getPackImage() {
		return null;
	}

	@Override
	public String getPackName() {
		return "BetterQuesting_folders";
	}

	protected void logNameNotLowercase(String name, String file) {
		BetterQuesting.logger.warn("ResourcePack: ignored non-lowercase namespace: {} in {}", name, file);
	}
}