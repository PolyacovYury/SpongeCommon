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
package org.spongepowered.common.data.processor.value.entity;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.BoundedValue.Mutable;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.mixin.core.entity.item.EntityFallingBlockAccessor;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class FallHurtAmountValueProcessor extends AbstractSpongeValueProcessor<EntityFallingBlockAccessor, Double, Mutable<Double>> {

    public FallHurtAmountValueProcessor() {
        super(EntityFallingBlockAccessor.class, Keys.FALL_DAMAGE_PER_BLOCK);
    }

    @Override
    protected Mutable<Double> constructValue(final Double value) {
        return SpongeValueFactory.boundedBuilder(Keys.FALL_DAMAGE_PER_BLOCK)
                .actualValue(value)
                .defaultValue(Constants.Entity.FallingBlock.DEFAULT_FALL_DAMAGE_PER_BLOCK)
                .minimum(0d)
                .maximum(Double.MAX_VALUE)
                .build();
    }

    @Override
    protected boolean set(final EntityFallingBlockAccessor container, final Double value) {
        container.accessor$setFallHurtAmount(value.floatValue());
        return true;
    }

    @Override
    protected Optional<Double> getVal(final EntityFallingBlockAccessor container) {
        return Optional.of((double)container.accessor$getFallHurtAmount());
    }

    @Override
    protected Immutable<Double> constructImmutableValue(final Double value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
