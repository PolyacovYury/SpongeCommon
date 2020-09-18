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

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;

public class SpongeCookingRecipeRegistration<T extends SmeltingRecipe> extends SpongeRecipeRegistration<T> {

    // Vanilla Recipe
    private Ingredient ingredient;
    private Item result;
    private float experience;
    private int cookingTime;

    // Sponge Recipe
    private ItemStack spongeResult;

    @Override
    protected void serialize0(JsonObject json) {
        // Vanilla Recipe
        json.add("ingredient", this.ingredient.serialize());
        json.addProperty("result", Registry.ITEM.getKey(this.result).toString());
        json.addProperty("experience", this.experience);
        json.addProperty("cookingtime", this.cookingTime);

        // Sponge Recipe
        if (spongeResult != null) {
            json.add("spongeresult", SpongeRecipeRegistration.serializeItemStack(spongeResult));
        }
    }

}
