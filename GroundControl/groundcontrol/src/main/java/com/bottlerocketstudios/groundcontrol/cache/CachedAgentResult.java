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

package com.bottlerocketstudios.groundcontrol.cache;

import android.os.SystemClock;

/**
 * An instance of a cached agent response with associated time metadata.
 */
public class CachedAgentResult<ResultType> {
    private ResultType mValue;
    private final long mCacheTimestamp;
    private long mMaximumLifetimeMs;

    public CachedAgentResult(ResultType value, long maximumLifetimeMs) {
        mValue = value;
        mCacheTimestamp = getTime();
        mMaximumLifetimeMs = maximumLifetimeMs;
    }

    public ResultType getValue() {
        return mValue;
    }

    public void setValue(ResultType value) {
        mValue = value;
    }

    private long getTime() {
        return SystemClock.uptimeMillis();
    }

    public void setMaximumLifetimeMs(long maximumLifetimeMs) {
        if (mMaximumLifetimeMs < maximumLifetimeMs) {
            mMaximumLifetimeMs = maximumLifetimeMs;
        }
    }

    public boolean isExpiredForMaxLifetime() {
        return isExpiredForSpecifiedLifetime(mMaximumLifetimeMs);
    }

    public boolean isExpiredForSpecifiedLifetime(long lifetimeMs) {
        return getTime() - mCacheTimestamp > lifetimeMs;
    }

}
