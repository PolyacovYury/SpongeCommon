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
package org.spongepowered.common.mixin.core.block;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataManipulator.Immutable;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableConnectedDirectionData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeConnectedDirectionData;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.VineBlock;

@Mixin(VineBlock.class)
public abstract class BlockVineMixin extends BlockMixin {

    @SuppressWarnings("RedundantTypeArguments") // some JDK's can fail to compile without the explicit type generics
    @Override
    public ImmutableList<Immutable<?, ?>> bridge$getManipulators(final net.minecraft.block.BlockState blockState) {
        return ImmutableList.<Immutable<?, ?>>of(impl$getConnectedDirectionData(blockState));
    }

    @Override
    public boolean bridge$supports(final Class<? extends Immutable<?, ?>> immutable) {
        return ImmutableConnectedDirectionData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> bridge$getStateWithData(final net.minecraft.block.BlockState blockState, final Immutable<?, ?> manipulator) {
        if (manipulator instanceof ImmutableConnectedDirectionData) {
            return Optional.of((BlockState) blockState);
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final net.minecraft.block.BlockState blockState, final Key<? extends Value<E>> key, final E value) {
        if (key.equals(Keys.CONNECTED_DIRECTIONS) || key.equals(Keys.CONNECTED_EAST) || key.equals(Keys.CONNECTED_NORTH)
                || key.equals(Keys.CONNECTED_SOUTH) || key.equals(Keys.CONNECTED_WEST)) {
            return Optional.of((BlockState) blockState);
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    private ImmutableConnectedDirectionData impl$getConnectedDirectionData(final net.minecraft.block.BlockState blockState) {
        final Set<Direction> directions = new HashSet<>();
        final Boolean north = blockState.get(VineBlock.NORTH);
        final Boolean east = blockState.get(VineBlock.EAST);
        final Boolean west = blockState.get(VineBlock.WEST);
        final Boolean south = blockState.get(VineBlock.SOUTH);
        final Boolean up = blockState.get(VineBlock.UP);
        if (north) {
            directions.add(Direction.NORTH);
        }
        if (south) {
            directions.add(Direction.SOUTH);
        }
        if (west) {
            directions.add(Direction.WEST);
        }
        if (east) {
            directions.add(Direction.EAST);
        }
        if (up) {
            directions.add(Direction.UP);
        }
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeConnectedDirectionData.class, directions);
    }
}
