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

package com.bottlerocketstudios.groundcontrolsample.product.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bottlerocketstudios.groundcontrolsample.R;
import com.bottlerocketstudios.groundcontrolsample.product.model.ProductItem;

public class ProductViewHolder extends RecyclerView.ViewHolder {

    private final TextView mPartId;
    private final TextView mPartColor;
    private final TextView mPartPrice;

    public ProductViewHolder(View itemView) {
        super(itemView);
        mPartId = (TextView) itemView.findViewById(R.id.product_part_id);
        mPartColor = (TextView) itemView.findViewById(R.id.product_color);
        mPartPrice = (TextView) itemView.findViewById(R.id.product_price);
    }

    public void bindView(ProductItem productItem) {
        mPartId.setText(String.valueOf(productItem.getPartId()));
        mPartColor.setText(productItem.getColor());
        mPartPrice.setText(productItem.getPrice());
    }
}
