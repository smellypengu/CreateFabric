package com.simibubi.create.foundation.command;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;

import com.simibubi.create.content.contraptions.goggles.GoggleConfigScreen;
import com.simibubi.create.foundation.command.ConfigureConfigPacket.Actions;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class ConfigureConfigPacket extends SimplePacketBase {

	private final String option;
	private final String value;

	public ConfigureConfigPacket(String option, String value) {
		this.option = option;
		this.value = value;
	}

	public ConfigureConfigPacket(PacketByteBuf buffer) {
		this.option = buffer.readString(32767);
		this.value = buffer.readString(32767);
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeString(option);
		buffer.writeString(value);
	}

	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get()
			.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
				try {
					Actions.valueOf(option)
						.performAction(value);
				} catch (IllegalArgumentException e) {
					LogManager.getLogger()
						.warn("Received ConfigureConfigPacket with invalid Option: " + option);
				}
			}));

		ctx.get()
			.setPacketHandled(true);
	}

	enum Actions {
		rainbowDebug(() -> Actions::rainbowDebug),
		overlayScreen(() -> Actions::overlayScreen),
		fixLighting(() -> Actions::experimentalLighting),
		overlayReset(() -> Actions::overlayReset),
		experimentalRendering(() -> Actions::experimentalRendering),

		;

		private final Supplier<Consumer<String>> consumer;

		Actions(Supplier<Consumer<String>> action) {
			this.consumer = action;
		}

		void performAction(String value) {
			consumer.get()
				.accept(value);
		}

		@Environment(EnvType.CLIENT)
		private static void rainbowDebug(String value) {
			AllConfigs.CLIENT.rainbowDebug.set(Boolean.parseBoolean(value));
		}

		@Environment(EnvType.CLIENT)
		private static void experimentalRendering(String value) {
			if (!"".equals(value)) {
				AllConfigs.CLIENT.experimentalRendering.set(Boolean.parseBoolean(value));
			}
			FastRenderDispatcher.refresh();
		}
		
		@Environment(EnvType.CLIENT)
		private static void overlayReset(String value) {
			AllConfigs.CLIENT.overlayOffsetX.set(0);
			AllConfigs.CLIENT.overlayOffsetY.set(0);
		}
		
		@Environment(EnvType.CLIENT)
		private static void overlayScreen(String value) {
			ScreenOpener.open(new GoggleConfigScreen());
		}

		@Environment(EnvType.CLIENT)
		private static void experimentalLighting(String value) {
			ForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.set(true);
			MinecraftClient.getInstance().worldRenderer.reload();
		}
	}
}
