package com.nikolaynik.aideprojectsetup.item;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import android.os.Bundle;

public abstract class Item {
	
	private final Context context;
	private final View view;
	private final LuaValue lua;
	private final String id;

	private String title;
	private boolean isSavable = true;

	public Item(Context context, String id, String title) {
		this(context, id);
		this.setTitle(title);
	}
	
	public Item(Context context, String id) {
		this.context = context;
		this.id = id;
		this.view = createView();
		this.lua = createLua();
	}
	
	public Context getContext() {
		return context;
	}
	
	public String getID() {
		return id;
	}
	
	public View getView() {
		return view;
	}
	
	public LuaValue getLua() {
		return lua;
	}
	
	public String getTitle() {
		return title;
	}
	
	public boolean isSavable() {
		return isSavable;
	}
	
	public void setSavable(boolean isSavable) {
		this.isSavable = isSavable;
	}
	
	public void setTitle(String title) {
		((TextView)view.findViewById(android.R.id.title)).setText(title);
		this.title = title;
	}
	
	public abstract View createView();
	
	public void fromBundle(Bundle bundle) {
		
	}
	
	public void toBundle(Bundle bundle) {
		
	}
	
	public LuaValue createLua() {
		LuaValue lua = new LuaTable();
		
		lua.set("getID", new ZeroArgFunction() {

			@Override
			public LuaValue call() {
				return valueOf(getID());
			}
		});
		
		lua.set("getTitle", new ZeroArgFunction() {

			@Override
			public LuaValue call() {
				return valueOf(getTitle());
			}
		});
		
		lua.set("setTitle", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				setTitle(p1.tojstring());
				return NIL;
			}
		});
		
		lua.set("setSavable", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				setSavable(p1.toboolean());
				return NIL;
			}
		});
		
		return lua;
	}
}
