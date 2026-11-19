package net.oktawia.gtopt.mixin.ae2;

import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;

import net.minecraft.world.item.ItemStack;
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

@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine$ExportOnlyAEStockingItemSlot",
       remap = false)
public abstract class StockingItemSlotMixin implements IStockingSlotOwner {

    @Shadow
    @Final
    MEStockingBusPartMachine this$0;

    @Inject(method = "extractItem", at = @At("RETURN"))
    private void gtopt$markDirtyAfterExtract(int slot, int amount, boolean simulate,
                                             CallbackInfoReturnable<ItemStack> cir) {
        GenericStack config = ((IConfigurableSlot) this).getConfig();
        if (!simulate && config != null && !cir.getReturnValue().isEmpty()) {
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
