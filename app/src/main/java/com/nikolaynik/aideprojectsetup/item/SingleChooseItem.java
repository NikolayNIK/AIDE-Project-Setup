package com.nikolaynik.aideprojectsetup.item;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import com.nikolaynik.aideprojectsetup.R;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class SingleChooseItem extends Item {
	
	private RadioButton[] buttons;
	private ViewGroup group;
	private int choosen;
	
	public SingleChooseItem(Context context, String id) {
		super(context, id);
	}

	public SingleChooseItem(Context context, String id, String title) {
		super(context, id, title);
	}

	public SingleChooseItem(Context context, String id, String title, String[] items) {
		super(context, id, title);
		this.setItems(items);
	}

	public SingleChooseItem(Context context, String id, String title, String[] items, int choosen) {
		super(context, id, title);
		this.setItems(items, choosen);
	}
	
	public int getChoosen() {
		return choosen;
	}
	
	public void setItems(String[] items) {
		setItems(items, 0);
	}
	
	public void setItems(String[] items, int choosen) {
		this.choosen = choosen;
		this.buttons = new RadioButton[items.length];
		group.removeAllViews();
		for(int i = 0; i < items.length; i++) {
			final int index = i;
			RadioButton button = buttons[i] = new RadioButton(getContext());
			button.setText(items[i]);
			if(i == choosen) button.setChecked(true);
			button.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton p1, boolean p2) {
					if(p2) {
						buttons[SingleChooseItem.this.choosen].setChecked(false);
						SingleChooseItem.this.choosen = index;
					}
				}
			});
			
			group.addView(button);
		}
	}
	
	public void setChoosen(int choosen) {
		buttons[choosen].setChecked(true);
	}

	@Override
	public View createView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.item_group, null);
		group = (ViewGroup)view.findViewById(android.R.id.content);
		return view;
	}

	@Override
	public LuaValue createLua() {
		LuaValue lua = super.createLua();
		
		lua.set("getChoosen", new ZeroArgFunction() {

			@Override
			public LuaValue call() {
				return valueOf(getChoosen());
			}
		});
		
		lua.set("setItems", new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue p1, LuaValue p2) {
				String[] items = new String[p1.length()];
				for(int i = 0; i < items.length; i++)
					items[i] = p1.get(i + 1).tojstring();
				
				if(p2.isnil()) setItems(items);
				else setItems(items, p2.toint());
				
				return NIL;
			}
		});
		
		lua.set("setChoosen", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				setChoosen(p1.toint());
				return NIL;
			}
		});
		
		return lua;
	}

	@Override
	public void fromBundle(Bundle bundle) {
		setChoosen(bundle.getInt(getID()));
	}

	@Override
	public void toBundle(Bundle bundle) {
		bundle.putInt(getID(), getChoosen());
	}
}
