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
package org.spongepowered.common.item.recipe.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.item.recipe.crafting.ShapelessCraftingRecipe;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;

import java.util.List;

public class SpongeShapelessCraftingRecipeRegistration<T extends ShapelessCraftingRecipe> extends SpongeRecipeRegistration<T> {
    // Vanilla Recipe
    private final Item result;
    private final int count;
    private final List<Ingredient> ingredients;

    // Sponge Recipe
    private final ItemStack spongeResult;

    private static IRecipeSerializer<?> SPONGE_CRAFTING_SHAPELESS = SpongeRecipeRegistration.register("crafting_shapeless", new Serializer());

    public SpongeShapelessCraftingRecipeRegistration(ResourceLocation key, String group, List<Ingredient> ingredients, ItemStack spongeResult) {
        super(key, SpongeShapelessCraftingRecipeRegistration.shapelessSerializer(spongeResult), spongeResult.getItem(), group);
        this.ingredients = ingredients;

        this.count = spongeResult.getCount();
        this.result = spongeResult.getItem();
        this.spongeResult = spongeResult.hasTag() ? spongeResult : null;
    }

    private static IRecipeSerializer<?> shapelessSerializer(ItemStack spongeResult) {
        return spongeResult.hasTag() ? SpongeShapelessCraftingRecipeRegistration.SPONGE_CRAFTING_SHAPELESS : IRecipeSerializer.CRAFTING_SHAPELESS;
    }

    @Override
    protected void serialize0(JsonObject json) {
        // Vanilla Recipe
        JsonArray jsonarray = new JsonArray();

        for(Ingredient ingredient : this.ingredients) {
            jsonarray.add(ingredient.serialize());
        }

        json.add("ingredients", jsonarray);
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("item", Registry.ITEM.getKey(this.result).toString());
        if (this.count > 1) {
            jsonobject.addProperty("count", this.count);
        }

        json.add("result", jsonobject);

        // Sponge Recipe
        if (spongeResult != null) {
            json.add("spongeresult", SpongeRecipeRegistration.serializeItemStack(spongeResult));
        }
    }

    // Custom ShapelessRecipe.Serializer with support for:
    // result full ItemStack instead of ItemType+Count
    // TODO ingredient itemstacks
    public static class Serializer extends ShapelessRecipe.Serializer {
        public ShapelessRecipe read(ResourceLocation recipeId, JsonObject json) {
            String s = JSONUtils.getString(json, "group", "");
            NonNullList<Ingredient> nonnulllist = Serializer.readIngredients(JSONUtils.getJsonArray(json, "ingredients"));
            if (nonnulllist.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else if (nonnulllist.size() > 9) {
                throw new JsonParseException("Too many ingredients for shapeless recipe");
            } else {
                ItemStack itemstack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
                ItemStack spongeStack = SpongeRecipeRegistration.deserializeItemStack(json.getAsJsonObject("spongeresult"));
                return new ShapelessRecipe(recipeId, s, spongeStack == null ? itemstack : spongeStack, nonnulllist);
            }
        }

        private static NonNullList<Ingredient> readIngredients(JsonArray p_199568_0_) {
            NonNullList<Ingredient> nonnulllist = NonNullList.create();

            for(int i = 0; i < p_199568_0_.size(); ++i) {
                Ingredient ingredient = Ingredient.deserialize(p_199568_0_.get(i));
                if (!ingredient.hasNoMatchingItems()) {
                    nonnulllist.add(ingredient);
                }
            }

            return nonnulllist;
        }
    }


}
