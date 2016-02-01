/*
 * Copyright (c) 2016. Bottle Rocket LLC
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bottlerocketstudios.groundcontrol;

/**
 * Build  a hash from various components of an object.
 */
public class HashBuilder {

    private int mHash;

    public HashBuilder() {
        this(17);
    }

    public HashBuilder(int initialValue) {
        if (initialValue == 0) {
            throw new IllegalArgumentException("Initial value cannot be 0");
        }
        mHash = initialValue;
    }

    public HashBuilder addHashLong(long value) {
        return addHashInt((int) (value ^ (value  >>> 32)));
    }

    public HashBuilder addHashFloat(float value) {
        return addHashInt(Float.floatToIntBits(value));
    }

    public HashBuilder addHashDouble(double value) {
        return addHashLong(Double.doubleToLongBits(value));
    }

    public HashBuilder addHashBoolean(boolean value) {
        return addHashInt(value ? 1 : 0);
    }

    public HashBuilder addHashObject(Object object) {
        return addHashInt(object.hashCode());
    }

    public HashBuilder addHashInt(int value) {
        mHash = mHash * 31 + value;
        return this;
    }

    public int build() {
        return mHash;
    }
}
