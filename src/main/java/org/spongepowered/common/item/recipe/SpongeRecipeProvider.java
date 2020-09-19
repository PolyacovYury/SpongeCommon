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
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.common.SpongeGame;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SpongeRecipeProvider {

    private static final int PACK_VERSION_1_15 = 6;

    public static void registerRecipes(Registry<RecipeRegistration> recipes) {
        for (RecipeRegistration<?> recipe : recipes) {
            final IFinishedRecipe mcRecipe = (IFinishedRecipe) recipe;
            SpongeRecipeProvider.save(mcRecipe);
        }
    }

    private static void save(IFinishedRecipe recipe) {
        Path path = Paths.get("world"); // TODO check path

        SpongeRecipeProvider.saveToFile(recipe.getRecipeJson(),
                path.resolve("datapacks/plugin-recipes/data/" + recipe.getID().getNamespace() + "/recipes/" + recipe.getID().getPath() + ".json"));
        JsonObject jsonobject = recipe.getAdvancementJson();
        if (jsonobject != null) {
            SpongeRecipeProvider.saveToFile(jsonobject,
                    path.resolve("datapacks/plugin-recipes/data/" + recipe.getID().getNamespace() + "/advancements/" + recipe.getAdvancementID().getPath() + ".json"));
        }

        final Path packMeta = path.resolve("datapacks/plugin-recipes").resolve("pack.mcmeta");
        final JsonObject packDataRoot = new JsonObject();
        final JsonObject packData = new JsonObject();
        packDataRoot.add("pack", packData);
        packData.addProperty("pack_format", SpongeRecipeProvider.PACK_VERSION_1_15);
        packData.addProperty("description", "Sponge Plugin provided Recipes");
        SpongeRecipeProvider.saveToFile(packDataRoot, packMeta);
    }

    private static void saveToFile(JsonObject json, Path pathIn) {
        try {
            Files.createDirectories(pathIn.getParent());
            try (BufferedWriter bufferedwriter = Files.newBufferedWriter(pathIn)) {
                bufferedwriter.write(json.toString());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }


}
