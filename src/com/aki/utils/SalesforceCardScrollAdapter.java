package com.aki.utils;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.widget.CardScrollAdapter;

public class SalesforceCardScrollAdapter extends CardScrollAdapter {
	private List<View> mCards;

	public SalesforceCardScrollAdapter(List<View> mCards) {
		this.mCards = mCards;
	}

	@Override
	public int getPosition(Object item) {
		return mCards.indexOf(item);
	}

	@Override
	public int getCount() {
		return mCards.size();
	}

	@Override
	public Object getItem(int position) {
		return mCards.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return mCards.get(position);
	}
}