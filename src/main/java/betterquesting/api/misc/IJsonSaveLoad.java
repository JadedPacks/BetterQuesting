package betterquesting.api.misc;

import betterquesting.api.enums.EnumSaveType;
import com.google.gson.JsonElement;

public interface IJsonSaveLoad<T extends JsonElement> {
	T writeToJson(T json, EnumSaveType saveType);
	void readFromJson(T json, EnumSaveType saveType);
}