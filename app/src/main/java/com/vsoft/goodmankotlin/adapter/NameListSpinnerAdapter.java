package com.vsoft.goodmankotlin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.vsoft.goodmankotlin.R;
import com.vsoft.goodmankotlin.model.ChoiceListOperator;

import java.util.ArrayList;
import java.util.List;

public class NameListSpinnerAdapter extends BaseAdapter implements Filterable {
   private List<ChoiceListOperator> containerVolumeSpinnerList;
    private List<ChoiceListOperator> choiceFilterList;
    private NameListSpinnerAdapter.ChoiceFilter filterChoice;
    private Context mContext;
    private String dataFrom;
    public NameListSpinnerAdapter(Context context, List<ChoiceListOperator> items,String dataFrom) {

        this.mContext = context;
        this.containerVolumeSpinnerList = items;
        this.choiceFilterList = items;
        this.dataFrom =dataFrom;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).
                    inflate(R.layout.spinner_row_item, parent, false);
        }
        TextView label = (TextView) convertView.findViewById(R.id.spinner_text);
        label.setText(containerVolumeSpinnerList.get(position).getName());

        // returns the view for the current row
        return convertView;
    }


    @Override
    public int getCount() {
        return containerVolumeSpinnerList.size();
    }

    @Override
    public ChoiceListOperator getItem(int position) {
        return containerVolumeSpinnerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {

        if (filterChoice == null) {
            filterChoice = new NameListSpinnerAdapter.ChoiceFilter();
        }

        return filterChoice;

    }

    private class ChoiceFilter extends Filter {
        FilterResults results = new FilterResults();
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<ChoiceListOperator> filterList = new ArrayList<ChoiceListOperator>();
            if (charSequence != null && charSequence.length() > 0) {
                for (int i = 0; i < choiceFilterList.size(); i++) {
                    if (choiceFilterList.get(i).getName().toLowerCase().contains(charSequence)) {
                        filterList.add(choiceFilterList.get(i));
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = choiceFilterList.size();
                results.values = choiceFilterList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            containerVolumeSpinnerList = (List<ChoiceListOperator>) filterResults.values;
            notifyDataSetChanged();
        }
    }
}
