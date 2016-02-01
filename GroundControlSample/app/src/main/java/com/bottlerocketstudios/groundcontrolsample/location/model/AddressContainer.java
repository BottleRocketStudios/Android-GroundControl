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

package com.bottlerocketstudios.groundcontrolsample.location.model;

import android.location.Address;

import java.util.List;

public class AddressContainer {
    private List<Address> mAddresses;

    public AddressContainer(List<Address> addresses) {
        this.mAddresses = addresses;
    }
    
    public List<Address> getAddresses() {
        return mAddresses;
    }
}
