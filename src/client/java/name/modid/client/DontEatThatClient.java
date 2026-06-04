package name.modid.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import name.modid.nutrition.NutritionNetworking;
import name.modid.nutrition.NutritionState;

public class DontEatThatClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		NutritionHudRenderer.register();
		NutritionMenu.init();

		ClientPlayNetworking.registerGlobalReceiver(NutritionNetworking.SYNC_ID,
				(client, handler, buf, responseSender) -> {
					NutritionState state = NutritionState.readFromBuf(buf);
					client.execute(() -> NutritionClientState.update(state));
				});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
				sender.sendPacket(NutritionNetworking.REQUEST_SYNC_ID, PacketByteBufs.empty()));
	}
}