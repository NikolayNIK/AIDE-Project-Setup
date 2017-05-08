package com.nikolaynik.aideprojectsetup.app;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import com.nikolaynik.aideprojectsetup.R;
import com.nikolaynik.aideprojectsetup.item.ButtonItem;
import com.nikolaynik.aideprojectsetup.item.DestinationItem;
import com.nikolaynik.aideprojectsetup.item.EditTextItem;
import com.nikolaynik.aideprojectsetup.item.Item;
import com.nikolaynik.aideprojectsetup.item.MultiChooseItem;
import com.nikolaynik.aideprojectsetup.item.SingleChooseItem;
import com.nikolaynik.aideprojectsetup.item.TextItem;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;
import com.nikolaynik.aideprojectsetup.item.CheckItem;

public class MainActivity extends AppCompatActivity {

	public static final String EXTRA_TEMPLATE = "template";
	
	private ViewGroup tableContainer;
	private TableLayout table;
	private FloatingActionButton fab;
	private File externalFile, destination;
	private Globals globals;
	private List<Item> items;
	private Bundle savedInstanceState;
	private DestinationItem destinationItem;
	private SharedPreferences prefs;
	private String internalFile;
	private boolean isFabEnabled, isEmptyRecomended = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		tableContainer = (ViewGroup)findViewById(R.id.content);
		fab = (FloatingActionButton)findViewById(R.id.fab);
		
		if(savedInstanceState != null && savedInstanceState.getBoolean("fab", false)) enableFab();
		this.savedInstanceState = savedInstanceState;
		
		if(getIntent().getBooleanExtra("internal", false)) {
			internalFile = getIntent().getData().getPath();
			setTitle(internalFile.substring(0, internalFile.lastIndexOf(".")));
		} else {
			externalFile = new File(getIntent().getData().getPath());
			setTitle(externalFile.getName().substring(0, externalFile.getName().lastIndexOf(".")));
		}
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean(getString(R.string.pref_unite_destination_key), true)) destination = new File(prefs.getString(getString(R.string.pref_destination), ""));
		
		new CreateTask().execute();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("fab", isFabEnabled);
		if(items != null)
			for(Item item: items)
				if(item.isSavable())
					item.toBundle(outState);
		
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		if(destinationItem != null) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(getString(R.string.pref_destination), destinationItem.getText());
			editor.apply();
		}
		
		super.onPause();
	}

	public void onClickFab(View view) {
		destination = new File(destinationItem.getText());
		if(isEmptyRecomended) {
			if(destination.exists()) {
				String[] list = destination.list();
				if(destination.isFile() || (list != null && list.length > 0)) {
					AlertDialog.Builder adb = new AlertDialog.Builder(this);
					adb.setTitle(R.string.dialog_destination_title);
					adb.setMessage(R.string.dialog_destination_message);
					adb.setNeutralButton(android.R.string.cancel, null);
					adb.setPositiveButton(R.string.dialog_destination_empty, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface p1, int p2) {
							new GenerateTask().execute(true);
						}
					});

					adb.setNegativeButton(R.string.dialog_destination_ignore, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface p1, int p2) {
							new GenerateTask().execute(false);
						}
					});

					adb.show();
					return;
				}
			}
		}
		
		new GenerateTask().execute(false);
	}
	
	public void enableFab() {
		fab.setEnabled(isFabEnabled = true);
	}
	
	public void disableFab() {
		fab.setEnabled(isFabEnabled = false);
	}
	
	private Item findItemByID(String id) {
		for(Item item: items)
			if(id.equals(item.getID()))
				return item;
		
		return null;
	}
	
	private void createGlobals() {
		globals = JsePlatform.standardGlobals();
		
		globals.set("finish", new ZeroArgFunction() {

			@Override
			public LuaValue call() {
				finish();
				return NIL;
			}
		});
		
		globals.set("runOnUiThread", new OneArgFunction() {
			
			@Override
			public LuaValue call(final LuaValue p1) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						p1.call();
					}
				});
				return NIL;
			}
		});
		
		globals.set("runOnNewThread", new OneArgFunction() {

			@Override
			public LuaValue call(final LuaValue p1) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						p1.call();
					}
				}).start();
				return NIL;
			}
		});
		
		globals.set("setEmptyRecomended", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				isEmptyRecomended = p1.toboolean();
				return NIL;
			}
		});
		
		globals.set("enableFab", new ZeroArgFunction() {

			@Override
			public LuaValue call() {
				enableFab();
				return NIL;
			}
		});
		
		globals.set("disableFab", new ZeroArgFunction() {

			@Override
			public LuaValue call() {
				disableFab();
				return NIL;
			}
		});
		
		globals.set("setTitle", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				setTitle(p1.tojstring());
				return NIL;
			}
		});
		
		globals.set("findByID", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				Item item = findItemByID(p1.tojstring());
				return item == null ? NIL : item.getLua();
			}
		});
		
		globals.set("newText", new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue p1, LuaValue p2) {
				Item item = new TextItem(MainActivity.this, p1.tojstring());
				if(!p2.isnil()) item.setTitle(p2.tojstring());
				if(savedInstanceState != null) item.fromBundle(savedInstanceState);
				items.add(item);
				table.addView(item.getView());
				return item.getLua();
			}
		});

		globals.set("newCheck", new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue p1, LuaValue p2) {
				Item item = new CheckItem(MainActivity.this, p1.tojstring());
				if(!p2.isnil()) item.setTitle(p2.tojstring());
				if(savedInstanceState != null) item.fromBundle(savedInstanceState);
				items.add(item);
				table.addView(item.getView());
				return item.getLua();
			}
		});

		globals.set("newEditText", new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue p1, LuaValue p2) {
				Item item = new EditTextItem(MainActivity.this, p1.tojstring());
				if(!p2.isnil()) item.setTitle(p2.tojstring());
				if(savedInstanceState != null) item.fromBundle(savedInstanceState);
				items.add(item);
				table.addView(item.getView());
				return item.getLua();
			}
		});

		globals.set("newDestination", new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue p1, LuaValue p2) {
				DestinationItem item = destinationItem = new DestinationItem(MainActivity.this, p1.tojstring());
				if(!p2.isnil()) item.setTitle(p2.tojstring());
				if(destination != null) item.setText(destination.getAbsolutePath());
				if(savedInstanceState != null) item.fromBundle(savedInstanceState);
				items.add(item);
				table.addView(item.getView());
				return item.getLua();
			}
		});

		globals.set("newButton", new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue p1, LuaValue p2) {
				Item item = new ButtonItem(MainActivity.this, p1.tojstring());
				if(!p2.isnil()) item.setTitle(p2.tojstring());
				if(savedInstanceState != null) item.fromBundle(savedInstanceState);
				items.add(item);
				table.addView(item.getView());
				return item.getLua();
			}
		});

		globals.set("newMultiChoose", new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue p1, LuaValue p2) {
				Item item = new MultiChooseItem(MainActivity.this, p1.tojstring());
				if(!p2.isnil()) item.setTitle(p2.tojstring());
				if(savedInstanceState != null) item.fromBundle(savedInstanceState);
				items.add(item);
				table.addView(item.getView());
				return item.getLua();
			}
		});

		globals.set("newSingleChoose", new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue p1, LuaValue p2) {
				Item item = new SingleChooseItem(MainActivity.this, p1.tojstring());
				if(!p2.isnil()) item.setTitle(p2.tojstring());
				if(savedInstanceState != null) item.fromBundle(savedInstanceState);
				items.add(item);
				table.addView(item.getView());
				return item.getLua();
			}
		});
		
		globals.set("splitCard", new ZeroArgFunction() {

			@Override
			public LuaValue call() {
				addCard();
				table = new TableLayout(MainActivity.this);
				return NIL;
			}
		});
		
		globals.set("snackbar", new ThreeArgFunction() {

			@Override
			public LuaValue call(LuaValue p1, LuaValue p2, final LuaValue p3) {
				Snackbar bar = Snackbar.make(fab, p1.tojstring(), p2.toint());
				if(!p3.isnil()) bar.setAction(p3.get(1).tojstring(), new OnClickListener() {

					@Override
					public void onClick(View p1) {
						p3.get(2).call();
					}
				});
				bar.show();
				return NIL;
			}
		});
		
		globals.set("sleep", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				try {
					Thread.sleep(p1.tolong());
				} catch (InterruptedException e) {}
				return NIL;
			}
		});
		
		globals.set("contains", new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue p1, LuaValue p2) {
				return p1.tojstring().contains(p2.tojstring()) ? TRUE : FALSE;
			}
		});
		
		globals.set("replace", new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue p1, LuaValue p2) {
				String string = p1.tojstring();
				for(Pair<String, String> pair: toPairArray(p2))
					string = string.replace(pair.first, pair.second);
				
				return valueOf(string);
			}
		});
		
		globals.set("canWrite", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				return valueOf(new File(p1.tojstring()).canWrite());
			}
		});

		globals.set("canRead", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				return valueOf(new File(p1.tojstring()).canRead());
			}
		});
		
		globals.set("scan", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				try {
					final Scanner scanner = new Scanner(openInput(p1.tojstring()));
					final LuaValue value = new LuaTable();

					value.set("next", new OneArgFunction() {

						@Override
						public LuaValue call(LuaValue p1) {
							return valueOf(p1.isnil() ? scanner.next() : scanner.next(p1.tojstring()));
						}
					});

					value.set("nextLine", new ZeroArgFunction() {

						@Override
						public LuaValue call() {
							return valueOf(scanner.nextLine());
						}
					});

					value.set("close", new ZeroArgFunction() {

						@Override
						public LuaValue call() {
							scanner.close();
							return NIL;
						}
					});

					return value;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		globals.set("write", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				try {
					final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(openOutput(p1.tojstring())));
					final LuaValue value = new LuaTable();
					
					value.set("write", new OneArgFunction() {

						@Override
						public LuaValue call(LuaValue p1) {
							try {
								writer.write(p1.tojstring());
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							
							return NIL;
						}
					});
					
					value.set("writeLine", new OneArgFunction() {

						@Override
						public LuaValue call(LuaValue p1) {
							try {
								if(prefs.getBoolean(getString(R.string.pref_use_spaces_key), false)) {
									String line = p1.tojstring();
									int spacesCount = Integer.valueOf(prefs.getString(getString(R.string.pref_spaces_count_key), "4"));
									StringBuilder sb = new StringBuilder();
									for(int i = 0; i < spacesCount; i++) sb.append(' ');
									String spaces = sb.toString();
									int i = 0;
									while(i < line.length() && line.charAt(i) == '\t') {
										line = line.replaceFirst("\t", spaces);
										i += spacesCount;
									}
									writer.write(line);
								} else {
									writer.write(p1.tojstring());
								}
								
								writer.write('\n');
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							
							return NIL;
						}
					});
					
					value.set("close", new ZeroArgFunction() {

						@Override
						public LuaValue call() {
							try {
								writer.close();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							
							return NIL;
						}
					});
					
					return value;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		globals.set("transfer", new ThreeArgFunction() {

			@Override
			public LuaValue call(LuaValue p1, LuaValue p2, LuaValue p3) {
				try {
					transfer(p1.tojstring(), p2.tojstring(), toPairArray(p3));
					return NIL;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		globals.set("transferBinary", new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue p1, LuaValue p2) {
				try {
					transferBinary(p1.tojstring(), p2.tojstring());
					return NIL;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	private void addCard() {
		ViewGroup card = (ViewGroup)getLayoutInflater().inflate(R.layout.item_card, null);
		card.addView(table);
		tableContainer.addView(card);
	}
	
	private Pair[] toPairArray(LuaValue value) {
		if(value.isnil()) return null;
		
		Pair[] pairs = new Pair[value.length()];
		for(int i = 0; i < pairs.length; i++) {
			LuaValue thing = value.get(i + 1);
			pairs[i] = new Pair<String, String>(thing.get(1).tojstring(), thing.get(2).tojstring());
		}
		
		return pairs;
	}
	
	private void transfer(String in, String out, Pair<String, String>[] replacements) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(openInput(in)));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(openOutput(out)));

		try {
			String line;
			if(prefs.getBoolean(getString(R.string.pref_use_spaces_key), false)) {
				int spacesCount = Integer.valueOf(prefs.getString(getString(R.string.pref_spaces_count_key), "4"));
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < spacesCount; i++) sb.append(' ');
				String spaces = sb.toString();
				
				while((line = reader.readLine()) != null) {
					if(line.length() > 0) {
						if(replacements != null)
							for(Pair<String, String> item: replacements)
								line = line.replace(item.first, item.second);

						int i = 0;
						while(i < line.length() && line.charAt(i) == '\t') {
							line = line.replaceFirst("\t", spaces);
							i += spacesCount;
						}

						writer.write(line);
					}
					
					writer.write('\n');
				}
			} else {
				while((line = reader.readLine()) != null) {
					if(replacements != null)
						for(Pair<String, String> item: replacements)
							line = line.replace(item.first, item.second);

					writer.write(line);
					writer.write('\n');
				}
			}
		} catch(IOException e) {
			writer.close();
			reader.close();
			throw e;
		}

		writer.close();
		reader.close();
	}
	
	private void transferBinary(String in, String out) throws IOException {
		InputStream input = openInput(in);
		OutputStream output = openOutput(out);

		try {
			int length;
			byte[] buffer = new byte[1024];
			while((length = input.read(buffer)) > 0) output.write(buffer, 0, length);
		} catch(IOException e) {
			output.close();
			input.close();
			throw e;

		}

		output.close();
		input.close();
	}
	
	private InputStream openInput(String filename) throws IOException {
		if(filename.length() > 1 && filename.charAt(1) == '/') {
			String filepath = filename.substring(2);
			switch(filename.charAt(0)) {
				case '@':
					ZipInputStream zis = new ZipInputStream(externalFile == null ? getAssets().open(internalFile) : new FileInputStream(externalFile));
					ZipEntry entry;
					while((entry = zis.getNextEntry()) != null) {
						if(!entry.isDirectory() && entry.getName().equals(filepath)) {
							return zis;
						}
					}

					zis.close();
					
					break;
				case '#':
					return new FileInputStream(new File(destination, filepath));
			}
		}
		
		throw new FileNotFoundException(filename);
	}

	private OutputStream openOutput(String filename) throws IOException {
		if(filename.length() > 1 && filename.charAt(1) == '/') {
			String filepath = filename.substring(2);
			switch(filename.charAt(0)) {
				case '#':
					File file = new File(destination, filepath);
					file.getParentFile().mkdirs();
					file.createNewFile();
					return new FileOutputStream(file);
			}
		}

		throw new FileNotFoundException(filename);
	}
	
	private void empty(File file) {
		for(File child: file.listFiles())
			remove(child);
	}
	
	private void remove(File file) {
		if(file.isFile()) {
			file.delete();
		} else {
			empty(file);
			file.delete();
		}
	}
	
	private class CreateTask extends AsyncTask {
		
		@Override
		protected Object doInBackground(Object[] p1) {
			try {
				items = new ArrayList<>();
				createGlobals();
				
				Reader reader = new InputStreamReader(openInput("@/create.lua"));
				LuaValue value = globals.load(reader, "script-create");
				reader.close();
				return value;
			} catch (Throwable e) {
				return e;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			if(result instanceof LuaValue) {
				table = new TableLayout(MainActivity.this);
				String value = null;
				
				try {
					LuaValue val = ((LuaValue)result).call();
					if(!val.isnil()) value = val.tojstring();
				} catch(Throwable t) {
					t.printStackTrace();
					value = t.toString();
				}
				
				addCard();
				if(value != null) Snackbar.make(fab, value, Snackbar.LENGTH_SHORT).show();
			} else {
				Snackbar.make(fab, result.toString(), Snackbar.LENGTH_INDEFINITE).show();
			}
			
			super.onPostExecute(result);
		}
	}
	
	private class GenerateTask extends AsyncTask {

		AlertDialog dialog;
		TextView text;

		@Override
		protected void onPreExecute() {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
			
			View view = getLayoutInflater().inflate(R.layout.dialog_progress, null);
			text = (TextView)findViewById(android.R.id.text1);
			
			AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
			adb.setTitle(R.string.dialog_generating_title);
			adb.setView(view);
			adb.setCancelable(false);
			
			dialog = adb.create();
			dialog.show();
			
			super.onPreExecute();
		}
	
		@Override
		protected Object doInBackground(Object[] p1) {
			try {
				if((boolean)p1[0]) {
					if(destination.isDirectory()) {
						empty(destination);
					} else {
						destination.delete();
						destination.mkdir();
					}
				} else if(destination.isFile()) {
					return "Destination is a file";
				}
				
				Reader reader = new InputStreamReader(openInput("@/generate.lua"));
				LuaValue value = globals.load(reader, "script-generate");
				reader.close();
				
				globals.set("setProgressText", new OneArgFunction() {

					@Override
					public LuaValue call(LuaValue p1) {
						final String text = p1.tojstring();
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								GenerateTask.this.text.setText(text);
							}
						});
						
						return NIL;
					}
				});
				
				LuaValue result = value.call();

				dialog.dismiss();
				return result.isnil() ? null : result.tojstring();
			} catch (Throwable e) {
				e.printStackTrace();
				dialog.dismiss();
				Throwable e0 = e.getCause();
				while(e0 != null) {
					e0 = e0.getCause();
					e = e.getCause();
				}
				
				return e.toString();
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			if(result != null) Snackbar.make(fab, result.toString(), Snackbar.LENGTH_INDEFINITE).show();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
			super.onPostExecute(result);
		}
	}
}
