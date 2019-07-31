package betterquesting.api.properties;

import com.google.gson.JsonElement;
import net.minecraft.util.ResourceLocation;

public interface IPropertyType<T> {
	ResourceLocation getKey();
	T getDefault();
	T readValue(JsonElement json);
	JsonElement writeValue(T value);
}