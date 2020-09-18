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
package org.spongepowered.common.item.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.recipe.Recipe;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SpongeShapedCraftingRecipeRegistration<T extends ShapedCraftingRecipe> extends SpongeRecipeRegistration<T> {

    // Vanilla Recipe
    private Item result;
    private int count;
    private List<String> pattern;
    private Map<Character, Ingredient> key;

    // Sponge Recipe
    private ItemStack spongeResult;


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
            json.add("spongeresult", this.serializeItemStack(spongeResult));
        }
    }

}
