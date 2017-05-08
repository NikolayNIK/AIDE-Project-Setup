package com.nikolaynik.aideprojectsetup.item;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.nikolaynik.aideprojectsetup.R;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class MultiChooseItem extends Item {

	private ViewGroup group;
	private CheckBox[] boxes;
	private boolean[] checked, saved;
	
	public MultiChooseItem(Context context, String id) {
		super(context, id);
	}

	public MultiChooseItem(Context context, String id, String title) {
		super(context, id, title);
	}

	public MultiChooseItem(Context context, String id, String title, String[] items) {
		super(context, id, title);
		this.setItems(items);
	}

	public MultiChooseItem(Context context, String id, String title, String[] items, boolean[] checked) {
		super(context, id, title);
		this.setItems(items, checked);
	}

	public boolean[] getChecked() {
		return checked;
	}
	
	public void setItems(String[] items) {
		setItems(items, new boolean[items.length]);
	}
	
	public void setItems(String[] items, boolean[] checked) {
		this.checked = checked;
		
		group.removeAllViews();
		boxes = new CheckBox[items.length];
		for(int i = 0; i < items.length; i++) {
			final int index = i;
			CheckBox check = boxes[i] = new CheckBox(getContext());
			check.setText(items[i]);
			check.setChecked(checked[i]);
			check.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton p1, boolean p2) {
					MultiChooseItem.this.checked[index] = p2;
				}
			});
			
			group.addView(check);
		}
	}
	
	public void setChecked(boolean[] checked) {
		for(int i = 0; i < boxes.length; i++) {
			if(i < checked.length)
				boxes[i].setChecked(checked[i]);
		}
	}

	@Override
	public LuaValue createLua() {
		LuaValue lua = super.createLua();
		
		lua.set("getChecked", new ZeroArgFunction() {

			@Override
			public LuaValue call() {
				LuaValue value = new LuaTable();
				
				for(int i = 0; i < checked.length; i++)
					value.set(i + 1, checked[i] ? TRUE : FALSE);
				
				return value;
			}
		});
		
		lua.set("setItems", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				String[] items = new String[p1.length()];
				boolean[] checked = new boolean[items.length];
				for(int i = 0; i < items.length; i++) {
					LuaValue thing = p1.get(i + 1);
					if(thing.istable()) {
						items[i] = thing.get(1).tojstring();
						checked[i] = thing.get(2).toboolean();
					} else {
						items[i] = thing.tojstring();
						if(saved != null && saved.length > i)
							checked[i] = saved[i];
					}
				}
				
				setItems(items, checked);
				
				return NIL;
			}
		});
		
		lua.set("setChecked", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				for(int i = 0; i < boxes.length; i++)
					boxes[i].setChecked(p1.get(i + 1).toboolean());
				
				return NIL;
			}
		});
		
		return lua;
	}

	@Override
	public View createView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.item_group, null);
		group = (ViewGroup)view.findViewById(android.R.id.content);
		return view;
	}

	@Override
	public void toBundle(Bundle bundle) {
		bundle.putBooleanArray(getID(), getChecked());
	}

	@Override
	public void fromBundle(Bundle bundle) {
		saved = bundle.getBooleanArray(getID());
	}
}
