package com.simibubi.create.content.contraptions.fluids.actors;

import static com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.PASS;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.fluids.FluidFX;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.ModList;

public class SpoutTileEntity extends SmartTileEntity {
	private static final boolean IS_TIC_LOADED = ModList.get()
		.isLoaded("tconstruct");
	private static final Class<?> CASTING_FLUID_HANDLER_CLASS;
	static {
		Class<?> testClass;
		try {
			testClass = Class.forName("slimeknights.tconstruct.library.smeltery.CastingFluidHandler");
		} catch (ClassNotFoundException e) {
			testClass = null;
		}
		CASTING_FLUID_HANDLER_CLASS = testClass;
	}

	public static final int FILLING_TIME = 20;

	protected BeltProcessingBehaviour beltProcessing;
	protected int processingTicks;
	protected boolean sendSplash;
	private boolean shouldAnimate = true;

	SmartFluidTankBehaviour tank;

	public SpoutTileEntity(BlockEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		processingTicks = -1;
	}

	protected Box cachedBoundingBox;
	@Override
	@Environment(EnvType.CLIENT)
	public Box getRenderBoundingBox() {
		if (cachedBoundingBox == null) {
			cachedBoundingBox = super.getRenderBoundingBox().stretch(0, -2, 0);
		}
		return cachedBoundingBox;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		tank = SmartFluidTankBehaviour.single(this, 1000);
		behaviours.add(tank);

		beltProcessing = new BeltProcessingBehaviour(this).whenItemEnters(this::onItemReceived)
			.whileItemHeld(this::whenItemHeld);
		behaviours.add(beltProcessing);

	}

	protected ProcessingResult onItemReceived(TransportedItemStack transported,
		TransportedItemStackHandlerBehaviour handler) {
		if (!FillingBySpout.canItemBeFilled(world, transported.stack))
			return PASS;
		if (tank.isEmpty())
			return HOLD;
		if (FillingBySpout.getRequiredAmountForItem(world, transported.stack, getCurrentFluidInTank()) == -1)
			return PASS;
		return HOLD;
	}

	protected ProcessingResult whenItemHeld(TransportedItemStack transported,
		TransportedItemStackHandlerBehaviour handler) {
		shouldAnimate = true;
		if (processingTicks != -1 && processingTicks != 5)
			return HOLD;
		if (!FillingBySpout.canItemBeFilled(world, transported.stack))
			return PASS;
		if (tank.isEmpty())
			return HOLD;
		FluidStack fluid = getCurrentFluidInTank();
		int requiredAmountForItem = FillingBySpout.getRequiredAmountForItem(world, transported.stack, fluid.copy());
		if (requiredAmountForItem == -1)
			return PASS;
		if (requiredAmountForItem > fluid.getAmount())
			return HOLD;

		if (processingTicks == -1) {
			processingTicks = FILLING_TIME;
			notifyUpdate();
			return HOLD;
		}

		// Process finished
		ItemStack out = FillingBySpout.fillItem(world, requiredAmountForItem, transported.stack, fluid);
		if (!out.isEmpty()) {
			List<TransportedItemStack> outList = new ArrayList<>();
			TransportedItemStack held = null;
			TransportedItemStack result = transported.copy();
			result.stack = out;
			if (!transported.stack.isEmpty())
				held = transported.copy();
			outList.add(result);
			handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(outList, held));
		}

		AllTriggers.triggerForNearbyPlayers(AllTriggers.SPOUT, world, pos, 5);
		if (out.getItem() instanceof PotionItem && !PotionUtil.getPotionEffects(out).isEmpty())
			AllTriggers.triggerForNearbyPlayers(AllTriggers.SPOUT_POTION, world, pos, 5);
		
		tank.getPrimaryHandler()
			.setFluid(fluid);
		sendSplash = true;
		notifyUpdate();
		return HOLD;
	}

	private void processTicCastBlock() {
		if (!IS_TIC_LOADED || CASTING_FLUID_HANDLER_CLASS == null)
			return;
		if (world == null)
			return;
		IFluidHandler localTank = this.tank.getCapability()
			.orElse(null);
		if (localTank == null)
			return;
		FluidStack fluid = getCurrentFluidInTank();
		if (fluid.getAmount() == 0)
			return;
		BlockEntity te = world.getBlockEntity(pos.down(2));
		if (te == null)
			return;
		IFluidHandler handler = getFluidHandler(pos.down(2), Direction.UP);
		if (!CASTING_FLUID_HANDLER_CLASS.isInstance(handler))
			return;
		if (handler.getTanks() != 1)
			return;
		if (!handler.isFluidValid(0, this.getCurrentFluidInTank()))
			return;
		FluidStack containedFluid = handler.getFluidInTank(0);
		if (!(containedFluid.isEmpty() || containedFluid.isFluidEqual(fluid)))
			return;
		if (processingTicks == -1) {
			processingTicks = FILLING_TIME;
			notifyUpdate();
			return;
		}
		FluidStack drained = localTank.drain(144, IFluidHandler.FluidAction.SIMULATE);
		if (!drained.isEmpty()) {
			int filled = handler.fill(drained, IFluidHandler.FluidAction.SIMULATE);
			shouldAnimate = filled > 0;
			sendSplash = shouldAnimate;
			if (processingTicks == 5) {
				if (filled > 0) {
					drained = localTank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
					if (!drained.isEmpty()) {
						FluidStack fillStack = drained.copy();
						fillStack.setAmount(Math.min(drained.getAmount(), 6));
						drained.shrink(filled);
						fillStack.setAmount(filled);
						handler.fill(fillStack, IFluidHandler.FluidAction.EXECUTE);
					}
				}
				tank.getPrimaryHandler()
					.setFluid(fluid);
				this.notifyUpdate();
			}
		}
	}

	private FluidStack getCurrentFluidInTank() {
		return tank.getPrimaryHandler()
			.getFluid();
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);

		compound.putInt("ProcessingTicks", processingTicks);
		if (sendSplash && clientPacket) {
			compound.putBoolean("Splash", true);
			sendSplash = false;
		}
	}

	@Override
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		processingTicks = compound.getInt("ProcessingTicks");
		if (!clientPacket)
			return;
		if (compound.contains("Splash"))
			spawnSplash(tank.getPrimaryTank()
				.getRenderedFluid());
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side != Direction.DOWN)
			return tank.getCapability()
				.cast();
		return super.getCapability(cap, side);
	}

	public void tick() {
		super.tick();
		processTicCastBlock();
		if (processingTicks >= 0)
			processingTicks--;
		if (processingTicks >= 8 && world.isClient && shouldAnimate)
			spawnProcessingParticles(tank.getPrimaryTank()
				.getRenderedFluid());
	}

	protected void spawnProcessingParticles(FluidStack fluid) {
		Vec3d vec = VecHelper.getCenterOf(pos);
		vec = vec.subtract(0, 8 / 16f, 0);
		ParticleEffect particle = FluidFX.getFluidParticle(fluid);
		world.addImportantParticle(particle, vec.x, vec.y, vec.z, 0, -.1f, 0);
	}

	protected static int SPLASH_PARTICLE_COUNT = 20;

	protected void spawnSplash(FluidStack fluid) {
		Vec3d vec = VecHelper.getCenterOf(pos);
		vec = vec.subtract(0, 2 - 5 / 16f, 0);
		ParticleEffect particle = FluidFX.getFluidParticle(fluid);
		for (int i = 0; i < SPLASH_PARTICLE_COUNT; i++) {
			Vec3d m = VecHelper.offsetRandomly(Vec3d.ZERO, world.random, 0.125f);
			m = new Vec3d(m.x, Math.abs(m.y), m.z);
			world.addImportantParticle(particle, vec.x, vec.y, vec.z, m.x, m.y, m.z);
		}
	}

	@Nullable
	private IFluidHandler getFluidHandler(BlockPos pos, Direction direction) {
		if (this.world == null) {
			return null;
		} else {
			BlockEntity te = this.world.getBlockEntity(pos);
			return te != null ? te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction)
				.orElse(null) : null;
		}
	}

	public int getCorrectedProcessingTicks() {
		if (shouldAnimate)
			return processingTicks;
		return -1;
	}

}
