package betterquesting.api.jdoc;

public interface IJsonDoc {
	String getUnlocalisedTitle();
	String getUnlocalisedName(String key);
	String getUnlocalisedDesc(String key);
	IJsonDoc getChildDoc(String child);
}