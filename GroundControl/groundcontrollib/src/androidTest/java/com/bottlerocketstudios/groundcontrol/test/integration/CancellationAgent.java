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

package com.bottlerocketstudios.groundcontrol.test.integration;

import android.os.SystemClock;
import android.util.Log;

import com.bottlerocketstudios.groundcontrol.agent.AbstractAgent;

import java.util.concurrent.TimeUnit;

public class CancellationAgent extends AbstractAgent<Boolean, Void> {
    private static final String TAG = CancellationAgent.class.getSimpleName();

    private static final long CANCEL_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(2);
    private static final long MAX_TIME_BEFORE_FAILURE_TO_BE_CANCELLED = TimeUnit.SECONDS.toMillis(2) + CANCEL_TIMEOUT_MS;
    public static final long COMPLETION_WAIT_TIME_MS = MAX_TIME_BEFORE_FAILURE_TO_BE_CANCELLED + TimeUnit.SECONDS.toMillis(1);

    @Override
    public long getCancelTimeoutMs() {
        return CANCEL_TIMEOUT_MS;
    }

    @Override
    public String getUniqueIdentifier() {
        return CancellationAgent.class.getCanonicalName();
    }

    @Override
    public void cancel() {
        getAgentListener().onCompletion(getUniqueIdentifier(), true);
    }

    @Override
    public void onProgressUpdateRequested() {}

    @Override
    public void run() {
        long startTime = SystemClock.uptimeMillis();
        while (SystemClock.uptimeMillis() - startTime < MAX_TIME_BEFORE_FAILURE_TO_BE_CANCELLED) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Log.i(TAG, "Interrupted as expected");
            }
        }
        getAgentListener().onCompletion(getUniqueIdentifier(), false);
    }
}
