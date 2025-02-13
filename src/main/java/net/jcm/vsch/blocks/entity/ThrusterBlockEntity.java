package net.jcm.vsch.blocks.entity;

import net.jcm.vsch.blocks.entity.template.AbstractThrusterFluidBlockEntity;
import net.jcm.vsch.config.VSCHConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ThrusterBlockEntity extends AbstractThrusterFluidBlockEntity {

	public ThrusterBlockEntity(BlockPos pos, BlockState state) {
		super("thruster", VSCHBlockEntities.THRUSTER_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	public float getMaxThrottle() {
		return VSCHConfig.THRUSTER_STRENGTH.get().intValue();
	}

	@Override
	public int consumeFuel(float power){
		return Math.max((int)(super.consumeFuel(power) * VSCHConfig.THRUSTER_FUEL_CONSUME.get().floatValue()), 1);
	}
}
