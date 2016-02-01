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

import com.bottlerocketstudios.groundcontrol.agent.AbstractAgent;

/**
 * Information container only. Will throw exceptions if an attempt is made to run it.
 */
class HollowAgent<ResultType, ProgressType> extends AbstractAgent<ResultType, ProgressType> {

    private final String mUniqueIdentifier;

    public HollowAgent(String uniqueIdentifier) {
        mUniqueIdentifier = uniqueIdentifier;
    }

    @Override
    public String getUniqueIdentifier() {
        return mUniqueIdentifier;
    }

    @Override
    public void cancel() {}

    @Override
    public void onProgressUpdateRequested() {}

    @Override
    public void run() {
        throw new RuntimeException("HollowAgent is not intended to be executed.");
    }
}
