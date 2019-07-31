package betterquesting.client.toolbox;

import betterquesting.api.client.toolbox.IToolRegistry;
import betterquesting.api.client.toolbox.IToolboxTab;

import java.util.ArrayList;
import java.util.List;

public class ToolboxRegistry implements IToolRegistry {
	public static final ToolboxRegistry INSTANCE = new ToolboxRegistry();
	private final ArrayList<IToolboxTab> toolTabs = new ArrayList<>();

	@Override
	public void registerToolbox(IToolboxTab toolbox) {
		if(toolbox == null) {
			throw new NullPointerException("Tried to register null toolbox");
		} else if(toolTabs.contains(toolbox)) {
			throw new IllegalArgumentException("Cannot register duplicate toolbox: " + toolbox.getClass());
		}
		toolTabs.add(toolbox);
	}

	@Override
	public List<IToolboxTab> getAllTools() {
		return toolTabs;
	}
}