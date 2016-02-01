/*
 * Copyright (c) 2016 Bottle Rocket LLC.
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

package com.bottlerocketstudios.groundcontrolsample.core.construction;

import java.util.HashMap;
import java.util.Map;

class SampleAppServiceLocator {

    private Map<Class<?>, Object> mLocatorMap;

    private SampleAppServiceLocator() {
        mLocatorMap = new HashMap<>();
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        public static final SampleAppServiceLocator instance = new SampleAppServiceLocator();
    }

    /**
     * Get the instance or create it. (inherently thread safe Bill Pugh pattern)
     */
    private static SampleAppServiceLocator getInstance() {
        return SingletonHolder.instance;
    }

    static <T> void put(Class<T> type, T instance) {
        if (type == null) {
            throw new NullPointerException("Type is null");
        }
        getInstance().mLocatorMap.put(type, instance);
    }

    static <T> T get(Class<T> type) {
        return type.cast(getInstance().mLocatorMap.get(type));
    }

}
