package net.oktawia.gtopt.mixin.ae2;

import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.oktawia.gtopt.ae2.IStockingPartOpt;
import net.oktawia.gtopt.ae2.IStockingSlotOwner;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
       targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine$ExportOnlyAEStockingFluidSlot",
       remap = false)
public abstract class StockingFluidSlotMixin implements IStockingSlotOwner {

    @Shadow
    @Final
    MEStockingHatchPartMachine this$0;

    @Inject(method = "drain(ILnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;",
            at = @At("RETURN"))
    private void gtopt$markDirtyAfterDrain(int maxDrain, IFluidHandler.FluidAction action,
                                           CallbackInfoReturnable<FluidStack> cir) {
        GenericStack config = ((IConfigurableSlot) this).getConfig();
        if (action.execute() && config != null && !cir.getReturnValue().isEmpty()) {
            ((IStockingPartOpt) this$0).gtopt$markStockDirty(config.what());
        }
    }

    @Override
    public void gtopt$notifyConfigChanged() {
        ((IStockingPartOpt) this$0).gtopt$markForRefresh();
    }

    @Override
    public boolean gtopt$isConfigAllowed(@Nullable GenericStack stack) {
        IStockingPartOpt part = (IStockingPartOpt) this$0;
        if (part.gtopt$isApplyingAutoPull()) {
            return true;
        }
        if (stack == null || stack.amount() <= 0) {
            return true;
        }
        return !part.getSlotList().hasStackInConfig(stack, true);
    }
}
