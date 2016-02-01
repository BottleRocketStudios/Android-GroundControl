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

package com.bottlerocketstudios.groundcontrol.looper;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Contains a collection of Loopers in a static, synchronized map. Will instantiate loopers with a
 * newly provided identifier and restart dead Loopers.
 */
public class LooperController {
    private static final String TAG = LooperController.class.getSimpleName();

    public static final String UI_LOOPER_ID = "uiLooperId";

    private static final long MAX_CREATION_TIME_MS = TimeUnit.SECONDS.toMillis(2);

    private static final Map<String, Looper> sLooperMap = Collections.synchronizedMap(new HashMap<String, Looper>());

    /**
     * Return the main Looper associated with the UI
     */
    private static Looper getUiLooper() {
        return Looper.getMainLooper();
    }

    /**
     * Return the looper associated with the provided ID. Use Process.THREAD_PRIORITY_BACKGROUND for non UI Looper.
     * @see com.bottlerocketstudios.groundcontrol.looper.LooperController#UI_LOOPER_ID
     */
    public static Looper getLooper(String looperId) {
        return getLooper(looperId, Process.THREAD_PRIORITY_BACKGROUND);
    }

    /**
     * Return the looper associated with the provided ID and if not present, create one with the provided priority.
     * If UI_LOOPER_ID is provided, no change in priority will occur.
     * @see com.bottlerocketstudios.groundcontrol.looper.LooperController#UI_LOOPER_ID
     */
    public static Looper getLooper(String looperId, int osThreadPriority) {
        if (UI_LOOPER_ID.equals(looperId)) {
            return getUiLooper();
        } else {
            synchronized (sLooperMap) {
                Looper looper = sLooperMap.get(looperId);
                if (looper == null) {
                    looper = createLooper(osThreadPriority);
                    sLooperMap.put(looperId, looper);
                }
                return looper;
            }
        }
    }

    /**
     * Stop the looper associated with the provided ID. If UI_LOOPER_ID is provided, nothing will happen.
     */
    public static void stopLooper(String looperId) {
        Looper looper = sLooperMap.remove(looperId);
        if (looper != null) {
            quitLooper(looper);
        }
    }


    @SuppressLint("NewApi")
    private static void quitLooper(Looper looper) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            looper.quitSafely();
        } else {
            looper.quit();
        }
    }

    private static class Container<T> {
        T mValue;

        public void setValue(T value) {
            mValue = value;
        }

        public T getValue() {
            return mValue;
        }
    }

    private static Looper createLooper(final int osThreadPriority) {
        final Container<Looper> looperContainer = new Container<>();

        if (osThreadPriority < Process.THREAD_PRIORITY_URGENT_AUDIO || osThreadPriority > Process.THREAD_PRIORITY_LOWEST) {
            throw new IllegalArgumentException("Cannot set thread priority to " + osThreadPriority);
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                android.os.Looper.prepare();
                Process.setThreadPriority(osThreadPriority);
                looperContainer.setValue(Looper.myLooper());
                Looper.loop();
            }
        };
        thread.start();

        //Wait until looper is ready.
        long creationStart = SystemClock.uptimeMillis();
        while (looperContainer.getValue() == null && SystemClock.uptimeMillis() - creationStart < MAX_CREATION_TIME_MS) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Log.e(TAG, "Caught java.lang.InterruptedException", e);
            }
        }

        return looperContainer.getValue();
    }
}
