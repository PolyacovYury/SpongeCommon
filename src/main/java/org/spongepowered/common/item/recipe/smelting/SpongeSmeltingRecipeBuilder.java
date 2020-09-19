/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.item.recipe.smelting;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CookingRecipeSerializer;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.item.recipe.RecipeType;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
import org.spongepowered.common.util.SpongeCatalogBuilder;

public class SpongeSmeltingRecipeBuilder extends SpongeCatalogBuilder<RecipeRegistration<SmeltingRecipe>, SmeltingRecipe.Builder>
        implements SmeltingRecipe.Builder.ResultStep, SmeltingRecipe.Builder.IngredientStep, SmeltingRecipe.Builder.EndStep {

    private IRecipeType type;
    private Ingredient ingredient;
    private Item result;
    @Nullable private Float experience;
    @Nullable private Integer smeltTime;
    @Nullable private String group;

    @Override
    public ResultStep ingredient(org.spongepowered.api.item.recipe.crafting.Ingredient ingredient) {
        this.ingredient = (Ingredient) (Object) ingredient;
        return this;
    }

    @Override
    public SmeltingRecipe.Builder reset() {
        super.reset();
        this.type = null;
        this.ingredient = null;
        this.result = null;
        this.experience = null;
        this.smeltTime = null;
        this.group = null;
        return this;
    }

    @Override
    public EndStep result(ItemType result) {
        this.result = (Item) result;
        return this;
    }

    @Override
    public EndStep experience(double experience) {
        checkState(experience >= 0, "The experience must be non-negative.");
        this.experience = (float) experience;
        return this;
    }

    @Override
    public EndStep smeltTime(int ticks) {
        this.smeltTime = ticks;
        return this;
    }

    @Override
    public IngredientStep type(RecipeType<SmeltingRecipe> type) {
        this.type = (IRecipeType) type;
        return this;
    }

    // TODO vanilla does not use groups for cooking recipes @Override
    public EndStep group(String group) {
        this.group = group;
        return this;
    }

    @Override
    protected RecipeRegistration<SmeltingRecipe> build(ResourceKey key) {
        checkNotNull(this.type);
        checkNotNull(this.ingredient);
        checkNotNull(this.result);
        checkNotNull(key);
        this.key = key;

        if (this.experience == null) {
            this.experience = 0f;
        }

        IRecipeSerializer<?> vanillaSerializer;
        if (this.type == IRecipeType.BLASTING) {
            if (this.smeltTime == null) {
                this.smeltTime = 100;
            }
            vanillaSerializer = IRecipeSerializer.BLASTING;
        } else if (this.type == IRecipeType.CAMPFIRE_COOKING) {
            if (this.smeltTime == null) {
                this.smeltTime = 600;
            }
            vanillaSerializer = CookingRecipeSerializer.CAMPFIRE_COOKING;
        } else if (this.type == IRecipeType.SMOKING) {
            if (this.smeltTime == null) {
                this.smeltTime = 100;
            }
            vanillaSerializer = CookingRecipeSerializer.SMOKING;
        } else if (this.type == IRecipeType.SMELTING) {
            if (this.smeltTime == null) {
                this.smeltTime = 200;
            }
            vanillaSerializer = CookingRecipeSerializer.SMELTING;
        } else {
            throw new IllegalArgumentException("Unknown RecipeType " + this.type);
        }

        final ItemStack spongeResult = new ItemStack(() -> this.result);
        return new SpongeCookingRecipeRegistration<>((ResourceLocation) (Object) this.key, vanillaSerializer, this.group,
                this.ingredient, this.experience, this.smeltTime, spongeResult);
    }

}
