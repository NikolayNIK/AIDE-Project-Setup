package com.nikolaynik.aideprojectsetup.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.nikolaynik.aideprojectsetup.R;
import java.io.File;
import java.io.IOException;

public class TemplatesActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {

	public static final int SOURCE_INTERNAL = 0;
	public static final int SOURCE_EXTERNAL = 1;
	public static final int SOURCE_EXTERNAL_NO_PERMISSION = 11;
	public static final int SOURCE_REMOTE = 2;

	public static final int REQUEST_INITIAL = 0;
	public static final int REQUEST_ITEM_CLICK = 1;
	public static final int REQUEST_EXT_LOAD = 2;
	
	private TextView empty;
	private ListView list;
	private ArrayAdapter<String> adapter;
	private int source;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_templates);
		((BottomNavigationView)findViewById(R.id.navigation_templates)).setOnNavigationItemSelectedListener(this);
		empty = (TextView)findViewById(android.R.id.empty);
		list = (ListView)findViewById(android.R.id.list);
		list.setAdapter(adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1));
		list.setOnItemClickListener(this);
		checkPermission(REQUEST_INITIAL);
		
		if(savedInstanceState != null) source = savedInstanceState.getInt("source", SOURCE_INTERNAL);
	}

	@Override
	protected void onResume() {
		switch(source) {
			case SOURCE_INTERNAL:
				loadInternal();
				break;
			case SOURCE_EXTERNAL:
				loadExternal();
				break;
			case SOURCE_REMOTE:
				loadRemote();
				break;
		}
		
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.item_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
			default:
				return false;
		}
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, String[] permissions, int[] grantResults) {
		switch(requestCode) {
			case REQUEST_INITIAL:
				if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
					Snackbar.make(findViewById(android.R.id.content), R.string.snack_repeat_permission, Snackbar.LENGTH_LONG).setAction(R.string.snack_permission_action, new OnClickListener() {

						@Override
						public void onClick(View p1) {
							requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
						}
					}).show();
				}
				
				break;
			case REQUEST_EXT_LOAD:
				if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					loadExternal();
				}
				
				break;
		}
		
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	
	@Override
	public boolean onNavigationItemSelected(MenuItem p1) {
		switch(p1.getItemId()) {
			case R.id.item_internal:
				loadInternal();
				return true;
			case R.id.item_external:
				loadExternal();
				return true;
			case R.id.item_remote:
				loadRemote();
				return true;
			default:
				return false;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
		if(checkPermission(REQUEST_ITEM_CLICK)) {
			Intent intent = new Intent(this, MainActivity.class);
			switch(source) {
				case SOURCE_INTERNAL:
					intent.putExtra("internal", true);
					intent.setData(Uri.parse(adapter.getItem(p3) + ".zip"));
					break;
				case SOURCE_EXTERNAL:
					intent.setData(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "AppTemplates/" + adapter.getItem(p3) + ".zip")));
					break;
			}
			startActivity(intent);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("source", source);
		super.onSaveInstanceState(outState);
	}
	
	private boolean checkPermission(final int request) {
		if(Build.VERSION.SDK_INT > 22 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			Snackbar.make(findViewById(android.R.id.content), R.string.snack_permission, Snackbar.LENGTH_LONG).setAction(R.string.snack_permission_action, new OnClickListener() {

				@Override
				public void onClick(View p1) {
					requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, request);
				}
			}).show();
			return false;
		}

		return true;
	}
	
	private void loadInternal() {
		adapter.clear();
		try {
			String[] list = getAssets().list("");
			if(list != null)
				for(String string: list)
					if(string.contains("."))
						if(string.substring(string.lastIndexOf(".")).equals(".zip"))
							adapter.add(string.substring(0, string.lastIndexOf(".")));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		empty.setVisibility(View.INVISIBLE);
		source = SOURCE_INTERNAL;
	}
	
	private void loadExternal() {
		adapter.clear();
		if(checkPermission(REQUEST_EXT_LOAD)) {
			String[] list = new File(Environment.getExternalStorageDirectory(), "AppTemplates").list();
			if(list != null)
				for(String string: list)
					if(string.contains("."))
						if(string.substring(string.lastIndexOf(".")).equals(".zip"))
							adapter.add(string.substring(0, string.lastIndexOf(".")));

			source = SOURCE_EXTERNAL;
		} else {
			source = SOURCE_EXTERNAL_NO_PERMISSION;
		}
		
		if(adapter.isEmpty()) {
			empty.setText(R.string.text_external_hint);
			empty.setVisibility(View.VISIBLE);
		} else {
			empty.setVisibility(View.INVISIBLE);
		}
	}
	
	private void loadRemote() {
		adapter.clear();
		source = SOURCE_REMOTE;
		empty.setText(R.string.text_remote_hint);
		empty.setVisibility(View.VISIBLE);
	}
}
