package net.jcm.vsch.blocks.entity.template;

import com.mojang.blaze3d.systems.RenderSystem;
import net.jcm.vsch.compat.CompatMods;
import net.jcm.vsch.config.VSCHConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public abstract class AbstractThrusterFluidBlockEntity extends AbstractThrusterBlockEntity implements IFluidHandler {
    private final FluidTank tank = new FluidTank(1000, fluid -> fluid.getFluid().isSame(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(VSCHConfig.FUEL_TYPE.get()))));
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> this);

    protected AbstractThrusterFluidBlockEntity(String typeStr, BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(typeStr, type, pos, state);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return this.tank.getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.tank.getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return this.tank.isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return this.tank.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return this.tank.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return this.tank.drain(maxDrain, action);
    }


    @Override
    public void tickForce(ServerLevel level, BlockPos pos, BlockState state) {
        int remainingFuel = this.tank.getFluidAmount() - consumeFuel(super.getPower());
        if (remainingFuel>0 && super.getPower()!=0){
            super.onServerTick(level,pos,state);
            this.tank.drain(consumeFuel(super.getPower()), FluidAction.EXECUTE);
            syncToClient();
        }else {
            super.setPower(0);
            super.onServerTick(level,pos,state);
        }
    }
    @Override
    public void tickParticles(Level level, BlockPos pos, BlockState state) {
        int remainingFuel = this.tank.getFluidAmount() - consumeFuel(super.getPower());
        if (remainingFuel>0){
            super.onClientTick(level,pos,state);
        }
    }

    @Override
    public void load(CompoundTag data) {
        super.load(data);
        this.tank.setFluid(new FluidStack(Fluids.WATER,data.getInt("FluidAmount")));
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putInt("FluidAmount", this.tank.getFluidAmount());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag data = super.getUpdateTag();
        this.saveAdditional(data);
        return data;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        load(tag);
    }

    public int consumeFuel(float power){
        return  (int)(power *15);
    }

    public void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

}
