package com.simibubi.create.content.contraptions.particle;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllParticleTypes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public class CubeParticleData implements ParticleEffect, CustomParticleData<CubeParticleData> {
	public static final Codec<CubeParticleData> CODEC = RecordCodecBuilder.create(i ->
		i.group(
			Codec.FLOAT.fieldOf("r").forGetter(p -> p.r),
			Codec.FLOAT.fieldOf("g").forGetter(p -> p.g),
			Codec.FLOAT.fieldOf("b").forGetter(p -> p.b),
			Codec.FLOAT.fieldOf("scale").forGetter(p -> p.scale),
			Codec.INT.fieldOf("avgAge").forGetter(p -> p.avgAge),
			Codec.BOOL.fieldOf("hot").forGetter(p -> p.hot))
			.apply(i, CubeParticleData::new));

	public static final Factory<CubeParticleData> DESERIALIZER = new Factory<CubeParticleData>() {
		@Override
		public CubeParticleData read(ParticleType<CubeParticleData> type, StringReader reader) throws CommandSyntaxException {
			reader.expect(' ');
			float r = reader.readFloat();
			reader.expect(' ');
			float g = reader.readFloat();
			reader.expect(' ');
			float b = reader.readFloat();
			reader.expect(' ');
			float scale = reader.readFloat();
			reader.expect(' ');
			int avgAge = reader.readInt();
			reader.expect(' ');
			boolean hot = reader.readBoolean();
			return new CubeParticleData(r, g, b, scale, avgAge, hot);
		}

		@Override
		public CubeParticleData read(ParticleType<CubeParticleData> type, PacketByteBuf buffer) {
			return new CubeParticleData(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readInt(), buffer.readBoolean());
		}
	};

	final float r;
	final float g;
	final float b;
	final float scale;
	final int avgAge;
	final boolean hot;

	public CubeParticleData(float r, float g, float b, float scale, int avgAge, boolean hot) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.scale = scale;
		this.avgAge = avgAge;
		this.hot = hot;
	}

	public CubeParticleData() {
		this(0, 0, 0, 0, 0, false);
	}

	@Override
	public Factory<CubeParticleData> getDeserializer() {
		return DESERIALIZER;
	}

	@Override
	public Codec<CubeParticleData> getCodec(ParticleType<CubeParticleData> type) {
		return CODEC;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public ParticleFactory<CubeParticleData> getFactory() {
		return new CubeParticle.Factory();
	}

	@Override
	public ParticleType<?> getType() {
		return AllParticleTypes.CUBE.get();
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeFloat(r);
		buffer.writeFloat(g);
		buffer.writeFloat(b);
		buffer.writeFloat(scale);
		buffer.writeInt(avgAge);
		buffer.writeBoolean(hot);
	}

	@Override
	public String asString() {
		return String.format(Locale.ROOT, "%s %f %f %f %f %d %s", AllParticleTypes.CUBE.parameter(), r, g, b, scale, avgAge, hot);
	}
}
