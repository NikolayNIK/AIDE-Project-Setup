package com.nikolaynik.aideprojectsetup.item;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import com.nikolaynik.aideprojectsetup.R;
import com.nikolaynik.aideprojectsetup.widget.FileAdapter;
import java.io.File;
import java.io.FileFilter;

public class DestinationItem extends EditTextItem implements OnClickListener {
	
	private File directory;
	
	public DestinationItem(Context context, String id) {
		super(context, id);
	}

	public DestinationItem(Context context, String id, String title) {
		super(context, id, title);
	}

	public DestinationItem(Context context, String id, String title, String text) {
		super(context, id, title, text);
	}

	public DestinationItem(Context context, String id, String title, String text, String hint) {
		super(context, id, title, text, hint);
	}
	
	public AlertDialog getBrowseDialog() {
		String text = getText();
		directory = text == null ? Environment.getExternalStorageDirectory() : new File(getText());
		
		View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_browse, null);
		final EditText editPath = (EditText)view.findViewById(R.id.edit_path);
		final FileAdapter adapter = new FileAdapter(getContext());
		final ListView list = (ListView)view.findViewById(android.R.id.list);
		list.setAdapter(adapter);

		final Runnable updateList = new Runnable() {

			@Override
			public void run() {
				adapter.clear();
				adapter.addAll(directory.listFiles(new FileFilter() {

					@Override
					public boolean accept(File p1) {
						return p1.isDirectory();
					}
				}));
				
				adapter.sort();
				editPath.setText(directory.getAbsolutePath());
			}
		};

		view.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View p1) {
				File file = directory;
				directory = new File(editPath.getText().toString());
				try {
					updateList.run();
				} catch(Throwable e) {
					directory = file;
					updateList.run();
				}
			}
		});

		view.findViewById(R.id.button_up).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View p1) {
				File file = directory;
				try {
					directory = directory.getParentFile();
					updateList.run();
					list.setSelection(adapter.getPosition(file));
				} catch(Throwable e) {
					directory = file;
					updateList.run();
				}
			}
		});
		
		view.findViewById(R.id.button_mkdir).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View p1) {
				final EditText editText = new EditText(getContext());
				editText.setHint(R.string.dialog_new_folder_hint);
				
				AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
				adb.setTitle(R.string.dialog_new_folder_title);
				adb.setView(editText);
				adb.setNegativeButton(android.R.string.cancel, null);
				adb.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface p1, int p2) {
						File file = directory;
						try {
							directory = new File(directory, editText.getText().toString());
							directory.mkdir();
							updateList.run();
						} catch(Throwable e) {
							directory = file;
							updateList.run();
						}
					}
				});
				
				AlertDialog dialog = adb.create();
				dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				dialog.show();
			}
		});

		try {
			updateList.run();
		} catch(Throwable e) {
			directory = Environment.getExternalStorageDirectory();
			updateList.run();
		}

		AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
		adb.setTitle("Add");
		adb.setView(view);
		adb.setNegativeButton(android.R.string.cancel, null);
		adb.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface p1, int p2) {
				setText(directory.getAbsolutePath());
			}
		});

		final AlertDialog dialog = adb.create();

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
				File file = adapter.getItem(p3);
				directory = file;
				updateList.run();
			}
		});

		return dialog;
	}

	@Override
	public void onClick(View p1) {
		directory = getText().length() > 0 ? new File(getText()) : Environment.getExternalStorageDirectory();
		getBrowseDialog().show();
	}

	@Override
	public View createView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.item_destination, null);
		view.findViewById(android.R.id.button1).setOnClickListener(this);
		setEditText((EditText)view.findViewById(android.R.id.edit));
		return view;
	}
}
