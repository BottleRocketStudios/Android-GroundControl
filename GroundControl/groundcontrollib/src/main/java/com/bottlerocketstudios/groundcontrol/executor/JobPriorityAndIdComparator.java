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

package com.bottlerocketstudios.groundcontrol.executor;

import java.util.Comparator;

public class JobPriorityAndIdComparator implements Comparator<Job> {
    @Override
    public int compare(Job lhs, Job rhs) {
        if (lhs.getPriority().equals(rhs.getPriority())) {
            return longCompare(lhs.getId(), rhs.getId());
        }
        return lhs.getPriority().compareTo(rhs.getPriority());
    }

    /**
     * Required for API < 19 where Long.compare does not exist for comparing two long values without
     * boxing/unboxing the values. Code taken from AOSP 5.1.0_rc1 Long.compare method.
     */
    private int longCompare(long lhs, long rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }
}
