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
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SingleItemRecipe;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.single.StoneCutterRecipe;

import java.util.function.Predicate;

public class SpongeSingleItemRecipeRegistration<T extends StoneCutterRecipe> extends SpongeRecipeRegistration<T> {

    // Vanilla Recipe
    private final Ingredient ingredient;
    private final Item result;
    private final int count;

    // Sponge Recipe
    private final ItemStack spongeResult;
    private final Predicate<ItemStackSnapshot> spongeIngredient;

    public static IRecipeSerializer<?> SPONGE_STONECUTTING = SpongeRecipeRegistration.register("stonecutting", new Serializer<>(StonecuttingRecipe::new));

    public SpongeSingleItemRecipeRegistration(ResourceLocation key, String group, Ingredient ingredient, ItemStack spongeResult) {
        super(key, SpongeSingleItemRecipeRegistration.serializer(spongeResult), spongeResult.getItem(), group);
        this.ingredient = ingredient;
        this.spongeIngredient = null;
        this.result = spongeResult.getItem();
        this.count = spongeResult.getCount();
        this.spongeResult = spongeResult;
    }

    public SpongeSingleItemRecipeRegistration(ResourceLocation key, String group, Predicate<ItemStackSnapshot> spongeIngredient, ItemStack spongeResult) {
        super(key, SpongeSingleItemRecipeRegistration.serializer(spongeResult), spongeResult.getItem(), group);
        this.ingredient = null;
        this.spongeIngredient = spongeIngredient;
        this.result = spongeResult.getItem();
        this.count = spongeResult.getCount();
        this.spongeResult = spongeResult;
        throw new UnsupportedOperationException("Not implemented yet"); // TODO custom serializer
    }

    private static IRecipeSerializer<?> serializer(ItemStack spongeResult) {
        return spongeResult.hasTag() ? SpongeSingleItemRecipeRegistration.SPONGE_STONECUTTING : IRecipeSerializer.STONECUTTING;
    }

    @Override
    protected void serialize0(JsonObject json) {
        // Vanilla Recipe
        json.add("ingredient", this.ingredient.serialize());
        json.addProperty("result", Registry.ITEM.getKey(this.result).toString());
        json.addProperty("count", this.count);

        // Sponge Recipe
        if (spongeResult != null) {
            json.add("spongeresult", SpongeRecipeRegistration.serializeItemStack(spongeResult));
        }
    }

    public static class Serializer<R extends SingleItemRecipe> implements IRecipeSerializer<R> {
        final Serializer.IRecipeFactory<R> factory;

        protected Serializer(Serializer.IRecipeFactory<R> factory) {
            this.factory = factory;
        }

        public R read(ResourceLocation recipeId, JsonObject json) {
            String s = JSONUtils.getString(json, "group", "");
            Ingredient ingredient;
            if (JSONUtils.isJsonArray(json, "ingredient")) {
                ingredient = Ingredient.deserialize(JSONUtils.getJsonArray(json, "ingredient"));
            } else {
                ingredient = Ingredient.deserialize(JSONUtils.getJsonObject(json, "ingredient"));
            }

            String s1 = JSONUtils.getString(json, "result");
            int i = JSONUtils.getInt(json, "count");
            ItemStack itemstack = new ItemStack(Registry.ITEM.getOrDefault(new ResourceLocation(s1)), i);
            ItemStack spongeStack = SpongeRecipeRegistration.deserializeItemStack(json.getAsJsonObject("spongeresult"));
            return this.factory.create(recipeId, s, ingredient, spongeStack == null ? itemstack : spongeStack);
        }

        public R read(ResourceLocation recipeId, PacketBuffer buffer) {
            String s = buffer.readString(32767);
            Ingredient ingredient = Ingredient.read(buffer);
            ItemStack itemstack = buffer.readItemStack();
            return this.factory.create(recipeId, s, ingredient, itemstack);
        }

        public void write(PacketBuffer buffer, R recipe) {
            buffer.writeString(recipe.getGroup());
            recipe.getIngredients().get(0).write(buffer);
            buffer.writeItemStack(recipe.getRecipeOutput());
        }

        interface IRecipeFactory<T extends SingleItemRecipe> {
            T create(ResourceLocation p_create_1_, String p_create_2_, Ingredient p_create_3_, ItemStack p_create_4_);
        }
    }

}
