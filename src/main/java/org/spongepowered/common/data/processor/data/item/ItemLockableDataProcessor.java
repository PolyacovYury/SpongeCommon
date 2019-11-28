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
package org.spongepowered.common.data.processor.data.item;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.LockCode;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableLockableData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.LockableData;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.api.data.value.Value.Mutable;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeLockableData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public final class ItemLockableDataProcessor extends AbstractItemSingleDataProcessor<String, Mutable<String>, LockableData, ImmutableLockableData> {

    public ItemLockableDataProcessor() {
        super(stack -> {
            final Item item = stack.getItem();
            if (!(item instanceof BlockItem)) {
                return false;
            }
            final Block block = ((BlockItem) item).getBlock();
            if (!(block instanceof ITileEntityProvider)) {
                return false;
            }
            final TileEntity tile = ((ITileEntityProvider) block).createNewTileEntity(null, item.getMetadata(stack.getDamage()));
            return tile instanceof LockableTileEntity;
        } , Keys.LOCK_TOKEN);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        if (supports(container)) {
            set((ItemStack) container, "");
            return DataTransactionResult.successNoData();
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean set(final ItemStack stack, final String value) {
        if (value.isEmpty()) {
            if (stack.hasTag() && stack.getTag().contains(Constants.Item.BLOCK_ENTITY_TAG, Constants.NBT.TAG_COMPOUND)) {
                stack.getTag().getCompound(Constants.Item.BLOCK_ENTITY_TAG).remove(Constants.Item.LOCK);
            }
            return true;
        }
        final LockCode code = new LockCode(value);
        code.write(stack.getOrCreateChildTag(Constants.Item.BLOCK_ENTITY_TAG));
        return true;
    }

    @Override
    protected Optional<String> getVal(final ItemStack container) {
        if (container.getTag() == null) {
            return Optional.of("");
        }
        final CompoundNBT tileCompound = container.getTag().getCompound(Constants.Item.BLOCK_ENTITY_TAG);
        final LockCode code = LockCode.read(tileCompound);
        if (code.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(code.getLock());
    }

    @Override
    protected Mutable<String> constructValue(final String actualValue) {
        return new SpongeValue<String>(Keys.LOCK_TOKEN, "", actualValue);
    }

    @Override
    protected Immutable<String> constructImmutableValue(final String value) {
        return new ImmutableSpongeValue<String>(Keys.LOCK_TOKEN, "", value);
    }

    @Override
    protected LockableData createManipulator() {
        return new SpongeLockableData();
    }

}
