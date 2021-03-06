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

/**
 * Temporary storage of cached results for quick redelivery to repeat clients. This is intended to
 * behave similar to Loaders and work for caching UI bound data. This is not for long term storage of
 * objects.
 */
public interface AgentResultCache {
    <ResultType> void put(String agentIdentifier, ResultType value, long initialCacheLifetimeMs);
    <ResultType> ResultType get(String agentIdentifier, long requestCacheLifetimeMs);
    void removeCache(String agentIdentifier);
}
