package betterquesting.api.utils;

import betterquesting.core.BetterQuesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

public class NBTConverter {
	private static JsonElement NBTtoJSON_Base(NBTBase tag, boolean format) {
		if(tag == null) {
			return new JsonObject();
		}
		if(tag.getId() >= 1 && tag.getId() <= 6) {
			return new JsonPrimitive(getNumber(tag));
		} else if(tag instanceof NBTTagString) {
			return new JsonPrimitive(((NBTTagString) tag).getString());
		} else if(tag instanceof NBTTagCompound) {
			return NBTtoJSON_Compound((NBTTagCompound) tag, new JsonObject(), format);
		} else if(tag instanceof NBTTagList) {
			if(format) {
				JsonObject jAry = new JsonObject();
				ArrayList<NBTBase> tagList = getTagList((NBTTagList) tag);
				for(NBTBase nbtBase : tagList) {
					jAry.add("" + nbtBase.getId(), NBTtoJSON_Base(nbtBase, true));
				}
				return jAry;
			} else {
				JsonArray jAry = new JsonArray();
				ArrayList<NBTBase> tagList = getTagList((NBTTagList) tag);
				for(NBTBase nbtBase : tagList) {
					jAry.add(NBTtoJSON_Base(nbtBase, false));
				}

				return jAry;
			}
		} else if(tag instanceof NBTTagByteArray) {
			JsonArray jAry = new JsonArray();
			for(byte b : ((NBTTagByteArray) tag).getByteArray()) {
				jAry.add(new JsonPrimitive(b));
			}
			return jAry;
		} else if(tag instanceof NBTTagIntArray) {
			JsonArray jAry = new JsonArray();
			for(int i : ((NBTTagIntArray) tag).getIntArray()) {
				jAry.add(new JsonPrimitive(i));
			}
			return jAry;
		} else {
			return new JsonObject();
		}
	}

	public static JsonObject NBTtoJSON_Compound(NBTTagCompound parent, JsonObject jObj) {
		return NBTtoJSON_Compound(parent, jObj, false);
	}

	public static JsonObject NBTtoJSON_Compound(NBTTagCompound parent, JsonObject jObj, boolean format) {
		if(parent == null) {
			return jObj;
		}
		for(String key : (Set<String>) parent.getKeySet()) {
			NBTBase tag = parent.getTag(key);
			if(tag == null) {
				continue;
			}
			if(format) {
				jObj.add(key + ":" + tag.getId(), NBTtoJSON_Base(tag, true));
			} else {
				jObj.add(key, NBTtoJSON_Base(tag, false));
			}
		}
		return jObj;
	}

	public static NBTTagCompound JSONtoNBT_Object(JsonObject jObj, NBTTagCompound tags) {
		return JSONtoNBT_Object(jObj, tags, false);
	}

	public static NBTTagCompound JSONtoNBT_Object(JsonObject jObj, NBTTagCompound tags, boolean format) {
		if(jObj == null) {
			return tags;
		}
		for(Entry<String, JsonElement> entry : jObj.entrySet()) {
			String key = entry.getKey();
			if(!format) {
				tags.setTag(key, JSONtoNBT_Element(entry.getValue(), (byte) 0, false));
			} else {
				String[] s = key.split(":");
				byte id = 0;
				try {
					id = Byte.parseByte(s[s.length - 1]);
					key = key.substring(0, key.lastIndexOf(":" + id));
				} catch(Exception e) {
					if(tags.hasKey(key)) {
						BetterQuesting.logger.warn("JSON/NBT formatting conflict on key '" + key + "'. Skipping...");
						continue;
					}
				}
				tags.setTag(key, JSONtoNBT_Element(entry.getValue(), id, true));
			}
		}
		return tags;
	}

	private static NBTBase JSONtoNBT_Element(JsonElement jObj, byte id, boolean format) {
		if(jObj == null) {
			return new NBTTagString();
		}
		byte tagID = id <= 0 ? fallbackTagID(jObj) : id;
		try {
			if(tagID >= 1 && tagID <= 6) {
				return instanceNumber(jObj.getAsNumber(), tagID);
			} else if(tagID == 8) {
				return new NBTTagString(jObj.getAsString());
			} else if(tagID == 10) {
				return JSONtoNBT_Object(jObj.getAsJsonObject(), new NBTTagCompound(), format);
			} else if(tagID == 7) {
				JsonArray jAry = jObj.getAsJsonArray();
				byte[] bAry = new byte[jAry.size()];
				for(int i = 0; i < jAry.size(); i++) {
					bAry[i] = jAry.get(i).getAsByte();
				}
				return new NBTTagByteArray(bAry);
			} else if(tagID == 11) {
				JsonArray jAry = jObj.getAsJsonArray();
				int[] iAry = new int[jAry.size()];
				for(int i = 0; i < jAry.size(); i++) {
					iAry[i] = jAry.get(i).getAsInt();
				}
				return new NBTTagIntArray(iAry);
			} else if(tagID == 9) {
				NBTTagList tList = new NBTTagList();
				if(jObj.isJsonArray()) {
					JsonArray jAry = jObj.getAsJsonArray();
					for(int i = 0; i < jAry.size(); i++) {
						JsonElement jElm = jAry.get(i);
						tList.appendTag(JSONtoNBT_Element(jElm, (byte) 0, format));
					}
				} else if(jObj.isJsonObject()) {
					JsonObject jAry = jObj.getAsJsonObject();
					for(Entry<String, JsonElement> entry : jAry.entrySet()) {
						try {
							tList.appendTag(JSONtoNBT_Element(entry.getValue(), Byte.parseByte(entry.getKey()), format));
						} catch(Exception e) {
							tList.appendTag(JSONtoNBT_Element(entry.getValue(), (byte) 0, format));
						}
					}
				}
				return tList;
			}
		} catch(Exception e) {
			BetterQuesting.logger.error("An error occured while parsing JsonElement to NBTBase (" + tagID + "):", e);
		}
		BetterQuesting.logger.warn("Unknown NBT representation for " + jObj.toString() + " (ID: " + tagID + ")");
		return new NBTTagString();
	}

	public static ArrayList<NBTBase> getTagList(NBTTagList tag) {
		return ObfuscationReflectionHelper.getPrivateValue(NBTTagList.class, tag, new String[] {"tagList", "field_74747_a"});
	}

	public static Number getNumber(NBTBase tag) {
		if(tag instanceof NBTTagByte) {
			return ((NBTTagByte) tag).getByte();
		} else if(tag instanceof NBTTagShort) {
			return ((NBTTagShort) tag).getShort();
		} else if(tag instanceof NBTTagInt) {
			return ((NBTTagInt) tag).getInt();
		} else if(tag instanceof NBTTagFloat) {
			return ((NBTTagFloat) tag).getFloat();
		} else if(tag instanceof NBTTagDouble) {
			return ((NBTTagDouble) tag).getDouble();
		} else if(tag instanceof NBTTagLong) {
			return ((NBTTagLong) tag).getLong();
		} else {
			return 0;
		}
	}

	public static NBTBase instanceNumber(Number num, byte type) {
		switch(type) {
			case 1:
				return new NBTTagByte(num.byteValue());
			case 2:
				return new NBTTagShort(num.shortValue());
			case 3:
				return new NBTTagInt(num.intValue());
			case 4:
				return new NBTTagLong(num.longValue());
			case 5:
				return new NBTTagFloat(num.floatValue());
			default:
				return new NBTTagDouble(num.doubleValue());
		}
	}

	private static byte fallbackTagID(JsonElement jObj) {
		byte tagID = 0;
		if(jObj.isJsonPrimitive()) {
			JsonPrimitive prim = jObj.getAsJsonPrimitive();
			if(prim.isNumber()) {
				if(prim.getAsString().contains(".")) {
					return 6;
				}
				return 4;
			}
			return 8;
		} else if(jObj.isJsonArray()) {
			JsonArray array = jObj.getAsJsonArray();
			for(JsonElement entry : array) {
				if(entry.isJsonPrimitive() && tagID == 0) {
					try {
						for(JsonElement element : array) {
							if(element.getAsLong() != element.getAsByte()) {
								throw new ClassCastException();
							}
						}
						tagID = 7;
					} catch(Exception e1) {
						try {
							for(JsonElement element : array) {
								if(element.getAsLong() != element.getAsInt()) {
									throw new ClassCastException();
								}
							}
							tagID = 11;
						} catch(Exception e2) {
							tagID = 9;
						}
					}
				} else if(!entry.isJsonPrimitive()) {
					return 9;
				}
			}
			return 9;
		} else {
			tagID = 10;
		}
		return tagID;
	}
}
