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


import android.os.Handler;
import android.os.Process;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A cache of recently used Handlers that will automatically create the required Looper via
 * {@link com.bottlerocketstudios.groundcontrol.looper.LooperController}
 */
public class HandlerCache {

    private final Map<String, Handler> mHandlerMap;
    private final int mDefaultOsThreadPriority;


    /**
     * Create a new HandlerCache using the Process.THREAD_PRIORITY_BACKGROUND
     */
    public HandlerCache() {
        this(Process.THREAD_PRIORITY_BACKGROUND);
    }

    /**
     * Create a new HandlerCache that will use supplied OS thread priority as default.
     */
    public HandlerCache(int defaultOsThreadPriority) {
        mHandlerMap = Collections.synchronizedMap(new HashMap<String, Handler>());
        mDefaultOsThreadPriority = defaultOsThreadPriority;
    }

    /**
     * Get or create a handler with a looper using the associated looperId creating a looping Looper if necessary. Uses
     * default OS thread priority provided to constructor.
     */
    public Handler getHandler(String looperId) {
        return getHandler(looperId, mDefaultOsThreadPriority);
    }

    /**
     * Get or create a handler with a looper using the associated looperId creating a looping Looper if necessary. If
     * creating a new looper, will use the supplied osThreadPriority.
     */
    public synchronized Handler getHandler(String looperId, int osThreadPriority) {
        Handler handler = mHandlerMap.get(looperId);
        if (handler == null) {
            handler = new Handler(LooperController.getLooper(looperId, osThreadPriority));
            mHandlerMap.put(looperId, handler);
        }
        return handler;
    }

    /**
     * Shutdown the looper associated with the specified handler id.
     */
    public synchronized void stopHandler(String looperId) {
        mHandlerMap.remove(looperId);
        LooperController.stopLooper(looperId);
    }

}
