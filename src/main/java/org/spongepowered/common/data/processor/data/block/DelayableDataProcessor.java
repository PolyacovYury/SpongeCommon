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
package org.spongepowered.common.data.processor.data.block;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDelayableData;
import org.spongepowered.api.data.manipulator.mutable.block.DelayableData;
import org.spongepowered.api.data.value.BoundedValue.Mutable;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDelayableData;
import org.spongepowered.common.data.processor.common.AbstractBlockOnlyDataProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class DelayableDataProcessor extends
        AbstractBlockOnlyDataProcessor<Integer, Mutable<Integer>, DelayableData, ImmutableDelayableData> {

    public DelayableDataProcessor() {
        super(Keys.DELAY);
    }

    @Override
    protected Integer getDefaultValue() {
        return 1;
    }

    @Override
    protected DelayableData createManipulator() {
        return new SpongeDelayableData();
    }

    @Override
    protected Mutable<Integer> constructValue(Integer actualValue) {
        return SpongeValueFactory.boundedBuilder(this.key)
                .defaultValue(getDefaultValue())
                .minimum(1)
                .maximum(4)
                .actualValue(actualValue)
                .build();
    }

}
