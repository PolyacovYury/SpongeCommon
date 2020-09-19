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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.recipe.Recipe;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.io.IOException;

public abstract class SpongeRecipeRegistration<T extends Recipe> implements RecipeRegistration<T>, IFinishedRecipe {

    private final ResourceLocation key;
    private final IRecipeSerializer<?> serializer;
    private final ResourceLocation advancementId;
    private final Advancement.Builder advancementBuilder = Advancement.Builder.builder();
    private final String group;

    public static <S extends IRecipeSerializer<T>, T extends IRecipe<?>> S register(String spongeName, S recipeSerializer) {
        return (S)(Registry.<IRecipeSerializer<?>>register(Registry.RECIPE_SERIALIZER, new ResourceLocation("sponge", spongeName), recipeSerializer));
    }
    public static <S extends IRecipeSerializer<T>, T extends IRecipe<?>> S register(ResourceLocation resourceLocation, S recipeSerializer) {
        return (S)(Registry.<IRecipeSerializer<?>>register(Registry.RECIPE_SERIALIZER, resourceLocation, recipeSerializer));
    }

    public SpongeRecipeRegistration(ResourceLocation key, IRecipeSerializer<?> serializer, Item resultItem, String group) {
        this.key = key;
        this.serializer = serializer;
        this.advancementId = new ResourceLocation(key.getNamespace(), "recipes/" + resultItem.getGroup().getPath() + "/" + key.getPath());
        this.advancementBuilder.withCriterion("has_the_recipe", new RecipeUnlockedTrigger.Instance(key));
        // TODO advancements
        this.group = group;
    }

    @Override
    public ResourceLocation getID() {
        return this.key;
    }

    @Override
    public ResourceKey getKey() {
        return (ResourceKey) (Object) this.key;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return this.serializer;
    }

    @Override
    public JsonObject getRecipeJson() {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("type", Registry.RECIPE_SERIALIZER.getKey(this.getSerializer()).toString());
        this.serialize(jsonobject);
        return jsonobject;
    }

    @Override
    public void serialize(JsonObject json) {
        if (!this.group.isEmpty()) {
            json.addProperty("group", this.group);
        }
        this.serialize0(json);
    }

    protected abstract void serialize0(JsonObject json);

    @Override
    public JsonObject getAdvancementJson() {
        return advancementBuilder.serialize();
    }

    @Override
    public ResourceLocation getAdvancementID() {
        return this.advancementId;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        try {
            return DataFormats.JSON.get().read(this.getRecipeJson().toString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected static JsonElement serializeItemStack(ItemStack spongeResult) {
        final DataContainer dataContainer = ItemStackUtil.fromNative(spongeResult).toContainer();
        try {
            return JSONUtils.fromJson(DataFormats.JSON.get().write(dataContainer));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected static ItemStack deserializeItemStack(JsonObject result) {
        try {
            final DataContainer dataContainer = DataFormats.JSON.get().read(result.toString());
            return ItemStackUtil.toNative(org.spongepowered.api.item.inventory.ItemStack.builder().fromContainer(dataContainer).build());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
