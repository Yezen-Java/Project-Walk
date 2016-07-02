package com.zaffre.infoapp.frontend.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.zaffre.infoapp.R;

import java.util.ArrayList;
import java.util.List;

public class CountryAdapter extends ArrayAdapter<CountryListElement> implements Filterable {

    private List<CountryListElement> countryList;
    private Context context;
    private Filter countryFilter;
    private List<CountryListElement> originalCountryList;

    public CountryAdapter(List<CountryListElement> countryList, Context context) {
        super(context, R.layout.country_row, countryList);
        this.countryList = countryList;
        this.context = context;
        this.originalCountryList = countryList;
    }

    /**
     * Get number of elements in list
     * @return number of elements in list
     */
    public int getCount() {
        return countryList.size();
    }

    /**
     * Get list
     * @return list
     */
    public List<CountryListElement> getList() {return countryList;}

    /**
     * Get item at given index
     * @param position index to get item from
     * @return item at given index
     */
    public CountryListElement getItem(int position) {
        return countryList.get(position);
    }

    /**
     * Get item id at given position
     * @param position index to get item from
     * @return id of item at given index
     */
    public long getItemId(int position) {
        return countryList.get(position).hashCode();
    }

    /**
     * Get view of element
     * @param position index of item
     * @param convertView view
     * @param parent parent view
     * @return element view
     */
    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        CountryHolder holder = new CountryHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.country_row, null);
            holder.countryNameView = (TextView) view.findViewById(R.id.name);
            holder.countryFlagView = (ImageView) view.findViewById(R.id.flag);
            view.setTag(holder);
        } else {
            holder = (CountryHolder) view.getTag();
        }

        //Get country at position, set text, flag and highlight
        CountryListElement country = countryList.get(position);
        holder.countryNameView.setText(country.getName());
        Context context = holder.countryFlagView.getContext();
        int id = context.getResources().getIdentifier(country.getIcon().toLowerCase(), "drawable", context.getPackageName());
        holder.countryFlagView.setImageResource(id);
        if(country.isHighlighted()) {
            view.setBackgroundColor(Color.parseColor("#1a8fcb"));
        } else {
            view.setBackgroundColor(Color.TRANSPARENT);
        }

        return view;
    }

    /**
     * Reset data to original list
     */
    public void resetData() {
        countryList = originalCountryList;
    }

    /**
     * Holder for view
     */
    private static class CountryHolder {
        public TextView countryNameView;
        public ImageView countryFlagView;
    }

    /**
     * Get custom filter
     * @return filter
     */
    @Override
    public Filter getFilter() {
        if (countryFilter == null) {
            countryFilter = new CountryFilter();
        }
        return countryFilter;
    }

    /**
     * Custom filter
     */
    private class CountryFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = originalCountryList;
                results.count = originalCountryList.size();
            } else {
                //Perform filtering
                List<CountryListElement> countryList = new ArrayList<>();
                for (CountryListElement country : CountryAdapter.this.countryList) {
                    if (country.getName().toUpperCase().startsWith(constraint.toString().toUpperCase()))
                        countryList.add(country);
                }
                results.values = countryList;
                results.count = countryList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count == 0) {
                notifyDataSetInvalidated();
            } else {
                countryList = (List<CountryListElement>) results.values;
                notifyDataSetChanged();
            }
        }
    }
}
