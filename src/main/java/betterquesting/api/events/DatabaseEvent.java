package betterquesting.api.events;

import cpw.mods.fml.common.eventhandler.Event;

public abstract class DatabaseEvent extends Event {
	public static class Update extends DatabaseEvent {}

	public static class Load extends DatabaseEvent {}

	public static class Save extends DatabaseEvent {}
}