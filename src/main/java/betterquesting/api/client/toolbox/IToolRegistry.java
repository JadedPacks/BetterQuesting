package betterquesting.api.client.toolbox;

import java.util.List;

public interface IToolRegistry {
	void registerToolbox(IToolboxTab toolbox);
	List<IToolboxTab> getAllTools();
}