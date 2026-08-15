package net.oktawia.gtopt.mixin;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.network.chat.Component;
import net.oktawia.gtopt.recipe.IRecipeVersionHolder;
import net.oktawia.gtopt.recipe.RecipeCapabilityVersions;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(value = RecipeLogic.class, remap = false)
public abstract class RecipeLogicSearchCacheMixin {

    @Shadow
    @Final
    public IRecipeLogicMachine machine;

    @Shadow
    public List<GTRecipe> lastFailedMatches;

    @Shadow
    protected GTRecipe lastRecipe;

    @Shadow
    @Final
    protected Map<GTRecipe, Component> failureReasonMap;

    @Shadow
    protected boolean recipeDirty;

    @Shadow
    public abstract void findAndHandleRecipe();

    @Unique
    private int gtopt$lastEmptySearchInputVersion = -1;

    @Unique
    private int gtopt$lastEmptySearchOutputVersion = -1;

    @Unique
    private int gtopt$lastEmptySearchRecipeType = -1;

    @Redirect(
              method = "serverTick",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/machine/trait/RecipeLogic;findAndHandleRecipe()V",
                       ordinal = 1))
    private void gtopt$skipRepeatedEmptySearch(RecipeLogic self) {
        if (!(machine instanceof IRecipeVersionHolder holder)) {
            findAndHandleRecipe();
            return;
        }
        RecipeCapabilityVersions versions = holder.gtopt$getRecipeVersions();
        int inputVersion = versions.inputVersion;
        int outputVersion = versions.outputVersion;
        int recipeType = machine.getActiveRecipeType();
        if (!recipeDirty && lastFailedMatches == null &&
                gtopt$lastEmptySearchInputVersion == inputVersion &&
                gtopt$lastEmptySearchOutputVersion == outputVersion &&
                gtopt$lastEmptySearchRecipeType == recipeType) {
            return;
        }
        findAndHandleRecipe();
        if (lastRecipe == null && lastFailedMatches == null && failureReasonMap.isEmpty()) {
            gtopt$lastEmptySearchInputVersion = inputVersion;
            gtopt$lastEmptySearchOutputVersion = outputVersion;
            gtopt$lastEmptySearchRecipeType = recipeType;
        }
    }

    @Inject(
            method = "resetRecipeLogic",
            at = @At("HEAD"))
    private void gtopt$resetSearchCache(CallbackInfo ci) {
        gtopt$lastEmptySearchInputVersion = -1;
        gtopt$lastEmptySearchOutputVersion = -1;
        gtopt$lastEmptySearchRecipeType = -1;
    }
}
