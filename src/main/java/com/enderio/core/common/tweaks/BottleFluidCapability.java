package com.enderio.core.common.tweaks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.EnderCore;

import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BottleFluidCapability implements IFluidHandlerItem, ICapabilityProvider {

  private static final ResourceLocation KEY = new ResourceLocation(EnderCore.DOMAIN, "bottle");

  private static final int AMOUNT = 333;

  private @Nonnull ItemStack container;

  private BottleFluidCapability(@Nonnull ItemStack container) {
    this.container = container;
  }

  private boolean isFull() {
    return container.getItem() == Items.POTIONITEM && PotionUtils.getPotionFromItem(container) == PotionTypes.WATER;
  }

  private void fill() {
    container = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER);
  }

  private boolean isEmpty() {
    return container.getItem() == Items.GLASS_BOTTLE;
  }

  private void empty() {
    container = new ItemStack(Items.GLASS_BOTTLE);
  }

  @Override
  public IFluidTankProperties[] getTankProperties() {
    return new IFluidTankProperties[] { new IFluidTankProperties() {

      @Override
      @Nullable
      public FluidStack getContents() {
        return isFull() ? new FluidStack(FluidRegistry.WATER, AMOUNT) : null;
      }

      @Override
      public int getCapacity() {
        return AMOUNT;
      }

      @Override
      public boolean canFill() {
        return isEmpty();
      }

      @Override
      public boolean canDrain() {
        return isFull();
      }

      @Override
      public boolean canFillFluidType(FluidStack fluidStack) {
        return fluidStack != null && fluidStack.getFluid() == FluidRegistry.WATER;
      }

      @Override
      public boolean canDrainFluidType(FluidStack fluidStack) {
        return fluidStack != null && fluidStack.getFluid() == FluidRegistry.WATER;
      }
    } };
  }

  @Override
  public int fill(FluidStack resource, boolean doFill) {
    if (container.getCount() != 1 || !isEmpty() || resource == null || resource.getFluid() != FluidRegistry.WATER || resource.amount < AMOUNT) {
      return 0;
    } else {
      if (doFill) {
        fill();
      }
      return AMOUNT;
    }
  }

  @Override
  @Nullable
  public FluidStack drain(FluidStack resource, boolean doDrain) {
    if (container.getCount() != 1 || !isFull() || resource == null || resource.getFluid() != FluidRegistry.WATER || resource.amount < AMOUNT) {
      return null;
    } else {
      if (doDrain) {
        empty();
      }
      return new FluidStack(FluidRegistry.WATER, AMOUNT);
    }
  }

  @Override
  @Nullable
  public FluidStack drain(int maxDrain, boolean doDrain) {
    if (container.getCount() != 1 || !isFull() || maxDrain < AMOUNT) {
      return null;
    } else {
      if (doDrain) {
        empty();
      }
      return new FluidStack(FluidRegistry.WATER, AMOUNT);
    }
  }

  @Override
  @Nonnull
  public ItemStack getContainer() {
    return container;
  }

  @Override
  public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
    return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY && (isEmpty() || isFull());
  }

  @Override
  @Nullable
  public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
    return hasCapability(capability, facing) ? CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(this) : null;
  }

  @SubscribeEvent
  public static void attachCapabilities(@Nonnull AttachCapabilitiesEvent<ItemStack> evt) {
    if (evt.getCapabilities().containsKey(KEY)) {
      return;
    }
    final ItemStack stack = evt.getObject();
    if (stack == null) {
      return;
    }
    if (stack.getItem() == Items.GLASS_BOTTLE || stack.getItem() == Items.POTIONITEM) {
      BottleFluidCapability cap = new BottleFluidCapability(stack);
      evt.addCapability(KEY, cap);
    }
  }

}