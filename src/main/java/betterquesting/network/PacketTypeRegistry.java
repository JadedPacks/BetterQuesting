package betterquesting.network;

import betterquesting.api.network.IPacketHandler;
import betterquesting.network.handlers.*;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class PacketTypeRegistry {
	private static final HashMap<ResourceLocation, IPacketHandler> pktHandlers = new HashMap<>();

	private PacketTypeRegistry() {
		registerHandler(new PktHandlerQuestDB());
		registerHandler(new PktHandlerQuestSync());
		registerHandler(new PktHandlerQuestEdit());
		registerHandler(new PktHandlerLineDB());
		registerHandler(new PktHandlerLineEdit());
		registerHandler(new PktHandlerLineSync());
		registerHandler(new PktHandlerPartyDB());
		registerHandler(new PktHandlerPartyAction());
		registerHandler(new PktHandlerPartySync());
		registerHandler(new PktHandlerDetect());
		registerHandler(new PktHandlerClaim());
		registerHandler(new PktHandlerLives());
		registerHandler(new PktHandlerNotification());
		registerHandler(new PktHandlerNameCache());
		registerHandler(new PktHandlerSettings());
	}

	private void registerHandler(IPacketHandler handler) {
		if(handler == null) {
			throw new NullPointerException("Tried to register null packet handler");
		} else if(handler.getRegistryName() == null) {
			throw new IllegalArgumentException("Tried to register a packet handler with a null name: " + handler.getClass());
		} else if(pktHandlers.containsKey(handler.getRegistryName()) || pktHandlers.containsValue(handler)) {
			throw new IllegalArgumentException("Cannot register dupliate packet handler: " + handler.getRegistryName());
		}
		pktHandlers.put(handler.getRegistryName(), handler);
	}

	public static IPacketHandler getPacketHandler(ResourceLocation name) {
		return pktHandlers.get(name);
	}
}