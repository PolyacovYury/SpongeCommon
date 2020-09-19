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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.common.accessor.item.crafting.ShapedRecipeAccessor;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;

import java.util.List;
import java.util.Map;

public class SpongeShapedCraftingRecipeRegistration<T extends ShapedCraftingRecipe> extends SpongeRecipeRegistration<T> {

    // Vanilla Recipe
    private final Item result;
    private final int count;
    private final List<String> pattern;
    private final Map<Character, Ingredient> key;

    // Sponge Recipe
    private final ItemStack spongeResult;

    private static IRecipeSerializer<?> SPONGE_CRAFTING_SHAPED = SpongeRecipeRegistration.register("crafting_shapeless", new SpongeShapedCraftingRecipeRegistration.Serializer());

    public SpongeShapedCraftingRecipeRegistration(ResourceLocation key, String group, List<String> pattern, Map<Character, Ingredient> key1, ItemStack spongeResult) {
        super(key, SpongeShapedCraftingRecipeRegistration.shapedSerializer(spongeResult), spongeResult.getItem(), group);
        this.result = spongeResult.getItem();
        this.count = spongeResult.getCount();
        this.pattern = pattern;
        this.key = key1;
        this.spongeResult = spongeResult;
    }

    private static IRecipeSerializer<?> shapedSerializer(ItemStack spongeResult) {
        return spongeResult.hasTag() ? SpongeShapedCraftingRecipeRegistration.SPONGE_CRAFTING_SHAPED : IRecipeSerializer.CRAFTING_SHAPED;
    }

    @Override
    protected void serialize0(JsonObject json) {
        // Vanilla Recipe
        JsonArray jsonarray = new JsonArray();

        for(String s : this.pattern) {
            jsonarray.add(s);
        }

        json.add("pattern", jsonarray);
        JsonObject jsonobject = new JsonObject();

        for(Map.Entry<Character, Ingredient> entry : this.key.entrySet()) {
            jsonobject.add(String.valueOf(entry.getKey()), entry.getValue().serialize());
        }

        json.add("key", jsonobject);
        JsonObject jsonobject1 = new JsonObject();
        jsonobject1.addProperty("item", Registry.ITEM.getKey(this.result).toString());
        if (this.count > 1) {
            jsonobject1.addProperty("count", this.count);
        }

        json.add("result", jsonobject1);

        // Sponge Recipe
        if (spongeResult != null) {
            json.add("spongeresult", SpongeRecipeRegistration.serializeItemStack(spongeResult));
        }
    }

    // Custom ShapelessRecipe.Serializer with support for:
    // result full ItemStack instead of ItemType+Count
    // TODO ingredient itemstacks
    public static class Serializer extends ShapedRecipe.Serializer {

        @Override
        public ShapedRecipe read(ResourceLocation recipeId, JsonObject json) {
            String s = JSONUtils.getString(json, "group", "");
            Map<String, Ingredient> map = ShapedRecipeAccessor.deserializeKey(JSONUtils.getJsonObject(json, "key"));
            String[] astring = ShapedRecipeAccessor.shrink(ShapedRecipeAccessor.patternFromJson(JSONUtils.getJsonArray(json, "pattern")));
            int i = astring[0].length();
            int j = astring.length;
            NonNullList<Ingredient> nonnulllist = ShapedRecipeAccessor.accessor$deserializeIngredients(astring, map, i, j);
            ItemStack itemstack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
            ItemStack spongeStack = SpongeRecipeRegistration.deserializeItemStack(json.getAsJsonObject("spongeresult"));
            return new ShapedRecipe(recipeId, s, i, j, nonnulllist, spongeStack == null ? itemstack : spongeStack);
        }
    }
}
