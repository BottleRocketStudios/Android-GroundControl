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
 *
 */

package com.bottlerocketstudios.groundcontrol.model;

/**
 * Convenient Pair-like object with more explicit field and accessor names.
 * @param <R> Type of result.
 * @param <S> Type of status.
 */
public class ResultContainer<R, S> {
    private final R mResult;
    private final S mStatus;

    public ResultContainer(R result, S status) {
        mResult = result;
        mStatus = status;
    }

    public R getResult() {
        return mResult;
    }

    public S getStatus() {
        return mStatus;
    }
}
