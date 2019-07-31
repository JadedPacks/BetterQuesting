package betterquesting.api.properties;

import betterquesting.api.misc.IJsonSaveLoad;
import com.google.gson.JsonObject;

public interface IPropertyContainer extends IJsonSaveLoad<JsonObject> {
	<T> T getProperty(IPropertyType<T> prop);
	<T> T getProperty(IPropertyType<T> prop, T def);
	boolean hasProperty(IPropertyType<?> prop);
	<T> void setProperty(IPropertyType<T> prop, T value);
}