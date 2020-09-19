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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.BlastingRecipe;
import net.minecraft.item.crafting.CampfireCookingRecipe;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SmokingRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;

public class SpongeCookingRecipeRegistration<T extends SmeltingRecipe> extends SpongeRecipeRegistration<T> {

    // Vanilla Recipe
    private final Ingredient ingredient;
    private final Item result;
    private final float experience;
    private final int cookingTime;

    // Sponge Recipe
    private final ItemStack spongeResult;

    public static Serializer<FurnaceRecipe> SPONGE_SMELTING = SpongeRecipeRegistration.register("smelting", new Serializer<>(FurnaceRecipe::new, 200));
    public static Serializer<BlastingRecipe> SPONGE_BLASTING = SpongeRecipeRegistration.register("blasting", new Serializer<>(BlastingRecipe::new, 100));
    public static Serializer<SmokingRecipe> SPONGE_SMOKING = SpongeRecipeRegistration.register("smoking", new Serializer<>(SmokingRecipe::new, 100));
    public static Serializer<CampfireCookingRecipe> SPONGE_CAMPFIRE_COOKING = SpongeRecipeRegistration.register("campfire_cooking", new Serializer<>(CampfireCookingRecipe::new, 100));

    public SpongeCookingRecipeRegistration(ResourceLocation key, IRecipeSerializer<?> serializer,
            String group, Ingredient ingredient, float experience, int cookingTime,
            ItemStack spongeResult) {
        super(key, SpongeCookingRecipeRegistration.cookingSerializer(spongeResult, serializer), spongeResult.getItem(), group);
        this.ingredient = ingredient;
        this.result = spongeResult.getItem();
        this.experience = experience;
        this.cookingTime = cookingTime;
        this.spongeResult = spongeResult.hasTag() ? spongeResult : null;
    }

    private static IRecipeSerializer<?> cookingSerializer(ItemStack spongeResult, IRecipeSerializer<?> serializer) {
        if (spongeResult.hasTag()) {
            if (serializer == IRecipeSerializer.SMELTING) {
                return SpongeCookingRecipeRegistration.SPONGE_SMELTING;
            }
            if (serializer == IRecipeSerializer.BLASTING) {
                return SpongeCookingRecipeRegistration.SPONGE_BLASTING;
            }
            if (serializer == IRecipeSerializer.SMOKING) {
                return SpongeCookingRecipeRegistration.SPONGE_SMOKING;
            }
            if (serializer == IRecipeSerializer.CAMPFIRE_COOKING) {
                return SpongeCookingRecipeRegistration.SPONGE_CAMPFIRE_COOKING;
            }
        }
        return serializer;
    }

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

    // Custom Serializer with support for:
    // result full ItemStack instead of ItemType+Count
    // TODO ingredient itemstacks
    public static class Serializer<R extends AbstractCookingRecipe> implements IRecipeSerializer<R> {

        private final int cookingTime;
        private final IFactory<R> factory;

        public Serializer(IFactory<R> p_i50025_1_, int p_i50025_2_) {
            this.cookingTime = p_i50025_2_;
            this.factory = p_i50025_1_;
        }

        public R read(ResourceLocation recipeId, JsonObject json) {
            String s = JSONUtils.getString(json, "group", "");
            JsonElement jsonelement = JSONUtils.isJsonArray(json, "ingredient") ? JSONUtils.getJsonArray(json, "ingredient") : JSONUtils.getJsonObject(json, "ingredient");
            Ingredient ingredient = Ingredient.deserialize(jsonelement);
            String s1 = JSONUtils.getString(json, "result");
            ResourceLocation resourcelocation = new ResourceLocation(s1);
            ItemStack itemstack = new ItemStack(Registry.ITEM.getValue(resourcelocation).orElseThrow(() -> new IllegalStateException("Item: " + s1 + " does not exist")));
            ItemStack spongeStack = SpongeRecipeRegistration.deserializeItemStack(json.getAsJsonObject("spongeresult"));
            float f = JSONUtils.getFloat(json, "experience", 0.0F);
            int i = JSONUtils.getInt(json, "cookingtime", this.cookingTime);
            return this.factory.create(recipeId, s, ingredient, spongeStack == null ? itemstack : spongeStack, f, i);
        }

        public R read(ResourceLocation recipeId, PacketBuffer buffer) {
            String s = buffer.readString(32767);
            Ingredient ingredient = Ingredient.read(buffer);
            ItemStack itemstack = buffer.readItemStack();
            float f = buffer.readFloat();
            int i = buffer.readVarInt();
            return this.factory.create(recipeId, s, ingredient, itemstack, f, i);
        }

        public void write(PacketBuffer buffer, R recipe) {
            buffer.writeString(recipe.getGroup());
            recipe.getIngredients().get(0).write(buffer);
            buffer.writeItemStack(recipe.getRecipeOutput());
            buffer.writeFloat(recipe.getExperience());
            buffer.writeVarInt(recipe.getCookTime());
        }

        interface IFactory<T extends AbstractCookingRecipe> {
            T create(ResourceLocation p_create_1_, String p_create_2_, Ingredient p_create_3_, ItemStack p_create_4_, float p_create_5_, int p_create_6_);
        }
    }

}
