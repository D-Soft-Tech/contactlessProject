package com.woleapp.netpos.contactless.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.woleapp.netpos.contactless.model.FBNBranch

class BranchAdapter(
    categoryList: ArrayList<FBNBranch>,
    context: Context,
    layoutId: Int,
) :
    ArrayAdapter<FBNBranch>(context, layoutId, categoryList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                android.R.layout.simple_expandable_list_item_1,
                parent,
                false,
            ) as TextView

        view.text = super.getItem(position)?.branch_name

        return view
    }
}
