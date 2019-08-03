package betterquesting.api.utils;

import betterquesting.core.BetterQuesting;
import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class JsonHelper {
	public static JsonArray GetArray(JsonObject json, String id) {
		if(json == null) {
			return new JsonArray();
		}
		if(json.has(id) && json.get(id).isJsonArray()) {
			return json.get(id).getAsJsonArray();
		}
		return new JsonArray();
	}

	public static JsonObject GetObject(JsonObject json, String id) {
		if(json == null) {
			return new JsonObject();
		}
		if(json.has(id) && json.get(id).isJsonObject()) {
			return json.get(id).getAsJsonObject();
		}
		return new JsonObject();
	}

	public static String GetString(JsonObject json, String id, String def) {
		if(json == null) {
			return def;
		}
		if(json.has(id) && json.get(id).isJsonPrimitive() && json.get(id).getAsJsonPrimitive().isString()) {
			return json.get(id).getAsString();
		}
		return def;
	}

	public static int GetInt(JsonObject json, String id, int def) {
		if(json == null) {
			return def;
		}
		if(json.has(id) && json.get(id).isJsonPrimitive()) {
			try {
				return json.get(id).getAsInt();
			} catch(Exception e) {
				return def;
			}
		}
		return def;
	}

	public static long GetLong(JsonObject json, String id, long def) {
		if(json == null) {
			return def;
		}
		if(json.has(id) && json.get(id).isJsonPrimitive()) {
			try {
				return json.get(id).getAsLong();
			} catch(Exception e) {
				return def;
			}
		}
		return def;
	}

	public static boolean GetBoolean(JsonObject json, String id, boolean def) {
		if(json == null) {
			return def;
		}
		if(json.has(id) && json.get(id).isJsonPrimitive()) {
			try {
				return json.get(id).getAsBoolean();
			} catch(Exception e) {
				return def;
			}
		}
		return def;
	}

	public static <E extends Enum<E>> E GetEnum(JsonObject json, String id, E def) {
		if(json == null) {
			return def;
		}
		if(json.has(id) && json.get(id).isJsonPrimitive()) {
			try {
				return Enum.valueOf(def.getDeclaringClass(), json.getAsString());
			} catch(Exception e) {
				return def;
			}
		}
		return def;
	}

	public static ArrayList<JsonElement> GetUnderlyingArray(JsonArray array) {
		ArrayList<JsonElement> list = new ArrayList<>();
		array.iterator().forEachRemaining(list::add);
		return list;
	}

	public static JsonObject ReadFromFile(File file) {
		if(file == null || !file.exists()) {
			return new JsonObject();
		}
		try {
			InputStreamReader fr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
			JsonObject json = new Gson().fromJson(fr, JsonObject.class);
			fr.close();
			return json;
		} catch(Exception e) {
			BetterQuesting.logger.error("An error occured while loading JSON from file:", e);
			int i = 0;
			File bkup = new File(file.getParent(), "malformed_" + file.getName() + i + ".json");
			while(bkup.exists()) {
				i++;
				bkup = new File(file.getParent(), "malformed_" + file.getName() + i + ".json");
			}
			BetterQuesting.logger.error("Creating backup at: " + bkup.getAbsolutePath());
			CopyPaste(file, bkup);
			return new JsonObject();
		}
	}

	public static void WriteToFile(File file, JsonObject jObj) {
		try {
			if(!file.exists()) {
				if(file.getParentFile() != null) {
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			JsonWriter jsonWriter = new JsonWriter(Streams.writerForAppendable(fw));
			jsonWriter.setIndent("\t");
			jsonWriter.setSerializeNulls(false);
			new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(jObj, jsonWriter);
			fw.close();
		} catch(Exception e) {
			BetterQuesting.logger.error("An error occured while saving JSON to file:", e);
		}
	}

	public static void CopyPaste(File fileIn, File fileOut) {
		try(BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn), StandardCharsets.UTF_8));
		    BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut), StandardCharsets.UTF_8))) {
			char[] buffer = new char[256];
			int read;
			while((read = fr.read(buffer)) != -1) {
				fw.write(buffer, 0, read);
			}
		} catch(Exception e1) {
			BetterQuesting.logger.error("Failed copy paste", e1);
		}
	}

	public static boolean isItem(JsonObject json) {
		if(json != null && json.has("id") && json.has("Count") && json.has("Damage") && json.get("id").isJsonPrimitive()) {
			if(!json.get("id").getAsJsonPrimitive().isNumber()) {
				return Item.itemRegistry.containsKey(json.get("id").getAsString());
			} else {
				return Item.itemRegistry.containsId(json.get("id").getAsNumber().intValue());
			}
		}
		return false;
	}

	public static boolean isFluid(JsonObject json) {
		return json != null && json.has("FluidName") && FluidRegistry.getFluid(GetString(json, "FluidName", "")) != null;
	}

	public static boolean isEntity(JsonObject json) {
		NBTTagCompound tags = NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound(), true);
		return tags.hasKey("id") && EntityList.stringToClassMapping.containsKey(tags.getString("id"));
	}

	public static BigItemStack JsonToItemStack(JsonObject json) {
		Item item = (Item) Item.itemRegistry.getObject(JsonHelper.GetString(json, "id", "minecraft:stone"));
		if(item == null) {
			return null;
		}
		int damage = JsonHelper.GetInt(json, "Damage", 0);
		BigItemStack stack = new BigItemStack(item, JsonHelper.GetInt(json, "Count", 1), damage >= 0 ? damage : OreDictionary.WILDCARD_VALUE);
		if(json.has("OreDict")) {
			stack.oreDict = JsonHelper.GetString(json, "OreDict", "");
		}
		if(json.has("tag")) {
			stack.SetTagCompound(NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(json, "tag"), new NBTTagCompound(), true));
		}
		return stack;
	}

	public static JsonObject ItemStackToJson(BigItemStack stack, JsonObject json) {
		if(stack == null) {
			return json;
		}
		json.addProperty("id", Item.itemRegistry.getNameForObject(stack.getBaseStack().getItem()));
		int size = stack.stackSize;
		if(size != 1) {
			json.addProperty("Count", size);
		}
		String ore = stack.oreDict;
		if(!ore.equals("")) {
			json.addProperty("OreDict", ore);
		}
		int meta = stack.getBaseStack().getMetadata();
		if(meta != 0) {
			json.addProperty("Damage", meta);
		}
		if(stack.HasTagCompound()) {
			json.add("tag", NBTConverter.NBTtoJSON_Compound(stack.GetTagCompound(), new JsonObject(), true));
		}
		return json;
	}

	public static FluidStack JsonToFluidStack(JsonObject json) {
		Fluid fluid = FluidRegistry.getFluid(GetString(json, "FluidName", "water"));
		if(fluid == null) {
			return null;
		}
		FluidStack stack = new FluidStack(fluid, GetInt(json, "Amount", 1000));
		if(json.has("Tag")) {
			stack.tag = NBTConverter.JSONtoNBT_Object(GetObject(json, "Tag"), new NBTTagCompound(), true);
		}
		return stack;
	}

	public static JsonObject FluidStackToJson(FluidStack stack, JsonObject json) {
		if(stack == null) {
			return json;
		}
		json.addProperty("FluidName", FluidRegistry.getFluidName(stack));
		json.addProperty("Amount", stack.amount);
		if(stack.tag != null) {
			json.add("Tag", NBTConverter.NBTtoJSON_Compound(stack.tag, new JsonObject(), true));
		}
		return json;
	}

	public static Entity JsonToEntity(JsonObject json, World world) {
		NBTTagCompound tags = NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound(), true);
		if(tags.hasKey("id") && EntityList.stringToClassMapping.containsKey(tags.getString("id"))) {
			return EntityList.createEntityFromNBT(tags, world);
		}
		return null;
	}

	public static JsonObject EntityToJson(Entity entity, JsonObject json) {
		if(entity == null) {
			return json;
		}
		NBTTagCompound tags = new NBTTagCompound();
		entity.writeToNBTOptional(tags);
		String id = EntityList.getEntityString(entity);
		tags.setString("id", id);
		NBTConverter.NBTtoJSON_Compound(tags, json, true);
		return json;
	}
}