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

package com.bottlerocketstudios.groundcontrol.inactivity;

import android.os.Handler;
import android.os.SystemClock;

import com.bottlerocketstudios.groundcontrol.looper.HandlerCache;

import java.util.UUID;

/**
 * Standard implementation of the InactivityCleanupRunnable.
 */
public class StandardInactivityCleanupRunnable implements InactivityCleanupRunnable {

    private final long mInactivityIdleMs;
    private final long mNormalCleanupIntervalMs;
    private final long mHighSpeedCleanupIntervalMs;
    private final String mLooperGuid = UUID.randomUUID().toString();
    private final HandlerCache mHandlerCache;

    private InactivityCleanupListener mInactivityCleanupListener;
    private boolean mStopped;
    private long mLastActivityTimestamp;
    private long mCleanupIntervalMs;

    public StandardInactivityCleanupRunnable(long inactivityIdleMs, long normalCleanupIntervalMs) {
        this(inactivityIdleMs, normalCleanupIntervalMs, normalCleanupIntervalMs / 2L);
    }

    public StandardInactivityCleanupRunnable(long inactivityIdleMs, long normalCleanupIntervalMs, long highSpeedCleanupIntervalMs) {
        mInactivityIdleMs = inactivityIdleMs;
        mNormalCleanupIntervalMs = normalCleanupIntervalMs;
        mHighSpeedCleanupIntervalMs = highSpeedCleanupIntervalMs;
        mStopped = true;

        mCleanupIntervalMs = mNormalCleanupIntervalMs;
        mHandlerCache = new HandlerCache();
    }

    private void start() {
        mStopped = false;
        postSelf();
    }

    private void postSelf() {
        getHandler().removeCallbacks(this);
        getHandler().postDelayed(this, mCleanupIntervalMs);
    }

    @Override
    public void setListener(InactivityCleanupListener inactivityCleanupListener) {
        mInactivityCleanupListener = inactivityCleanupListener;
    }

    @Override
    public void stop() {
        mStopped = true;
        getHandler().removeCallbacks(this);
        mHandlerCache.stopHandler(getLooperGuid());
    }

    @Override
    public void run() {
        if (!mStopped) {
            mInactivityCleanupListener.performCleanup();
            checkInactivityShutdown();
            postSelf();
        }
    }

    @Override
    public void restartTimer() {
        mLastActivityTimestamp = getTime();
        if (mStopped) start();
    }

    @Override
    public void enterHighSpeedMode() {
        mCleanupIntervalMs = mHighSpeedCleanupIntervalMs;
    }

    @Override
    public void exitHighSpeedMode() {
        mCleanupIntervalMs = mNormalCleanupIntervalMs;
    }

    @Override
    public boolean isHighSpeedMode() {
        return mCleanupIntervalMs == mHighSpeedCleanupIntervalMs;
    }

    private void checkInactivityShutdown() {
        if (!mInactivityCleanupListener.isBusy()
                && ((getTime() - mLastActivityTimestamp) > mInactivityIdleMs)) {
            stop();
            mInactivityCleanupListener.enterIdleState();
        }
    }

    private Handler getHandler() {
        return  mHandlerCache.getHandler(getLooperGuid());
    }

    private String getLooperGuid() {
        return mLooperGuid;
    }

    private long getTime() {
        return SystemClock.uptimeMillis();
    }
}
