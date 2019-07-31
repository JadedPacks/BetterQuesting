package betterquesting.api.events;

import betterquesting.api.jdoc.IJsonDoc;
import cpw.mods.fml.common.eventhandler.Event;

public class JsonDocEvent extends Event {
	private final IJsonDoc inJdoc;
	private final IJsonDoc outJdoc;

	public JsonDocEvent(IJsonDoc jdoc) {
		inJdoc = jdoc;
		outJdoc = jdoc;
	}

	public IJsonDoc getJdocResult() {
		return outJdoc == null ? inJdoc : outJdoc;
	}
}