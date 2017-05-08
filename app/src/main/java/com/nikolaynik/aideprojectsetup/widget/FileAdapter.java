package com.nikolaynik.aideprojectsetup.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.nikolaynik.aideprojectsetup.R;
import java.io.File;
import java.util.Comparator;

public class FileAdapter extends ArrayAdapter<File> implements Comparator<File> {

	public FileAdapter(Context context) {
		super(context, R.layout.item_file);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if(view == null) view = LayoutInflater.from(getContext()).inflate(R.layout.item_file, null);

		File file = getItem(position);
		((TextView)view.findViewById(android.R.id.title)).setText(file.getName());
		
		return view;
	}

	@Override
	public int compare(File p1, File p2) {
		if(p1.isFile() && p2.isDirectory()) return 1;
		if(p1.isDirectory() && p2.isFile()) return -1;
		return p1.getName().toLowerCase().compareTo(p2.getName().toLowerCase());
	}

	public void sort() {
		sort(this);
	}
}
