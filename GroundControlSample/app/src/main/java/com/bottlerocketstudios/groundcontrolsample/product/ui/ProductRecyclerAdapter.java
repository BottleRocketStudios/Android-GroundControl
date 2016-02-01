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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bottlerocketstudios.groundcontrolsample.R;
import com.bottlerocketstudios.groundcontrolsample.product.model.ProductItem;

import java.util.ArrayList;
import java.util.List;

public class ProductRecyclerAdapter extends RecyclerView.Adapter<ProductViewHolder> {

    private List<ProductItem> mProductList = new ArrayList<>();
    private LayoutInflater mLayoutInflater;

    private LayoutInflater getLayoutInflater(Context context) {
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(context);
        }
        return mLayoutInflater;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = getLayoutInflater(parent.getContext()).inflate(R.layout.product_list_item, parent, false);
        ProductViewHolder productViewHolder = new ProductViewHolder(itemView);
        return productViewHolder;
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        ProductItem productItem = mProductList.get(position);
        holder.bindView(productItem);
    }

    @Override
    public int getItemCount() {
        return mProductList.size();
    }

    public void swapItems(List<ProductItem> productList) {
        mProductList = new ArrayList<>(productList);
        notifyDataSetChanged();
    }
}
