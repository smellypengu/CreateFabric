package com.simibubi.create.content.contraptions.components.structureMovement.sync;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;

public class ClientMotionPacket extends SimplePacketBase {

	private Vec3d motion;
	private boolean onGround;
	private float limbSwing;

	public ClientMotionPacket(Vec3d motion, boolean onGround, float limbSwing) {
		this.motion = motion;
		this.onGround = onGround;
		this.limbSwing = limbSwing;
	}

	public ClientMotionPacket(PacketByteBuf buffer) {
		motion = new Vec3d(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		onGround = buffer.readBoolean();
		limbSwing = buffer.readFloat();
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeFloat((float) motion.x);
		buffer.writeFloat((float) motion.y);
		buffer.writeFloat((float) motion.z);
		buffer.writeBoolean(onGround);
		buffer.writeFloat(limbSwing);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity sender = context.get()
					.getSender();
				if (sender == null)
					return;
				sender.setVelocity(motion);
				sender.setOnGround(onGround);
				if (onGround) {
					sender.handleFallDamage(sender.fallDistance, 1);
					sender.fallDistance = 0;
					sender.networkHandler.floatingTicks = 0;
				}
				AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> sender),
					new LimbSwingUpdatePacket(sender.getEntityId(), sender.getPos(), limbSwing));
			});
		context.get()
			.setPacketHandled(true);
	}

}
