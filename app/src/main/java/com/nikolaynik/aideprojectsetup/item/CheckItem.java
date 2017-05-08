package com.nikolaynik.aideprojectsetup.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.nikolaynik.aideprojectsetup.R;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.OneArgFunction;

public class CheckItem extends Item implements CheckBox.OnCheckedChangeListener {

	private CheckBox checkBox;
	private boolean isChecked;

	public CheckItem(Context context, String id) {
		super(context, id);
	}

	public CheckItem(Context context, String id, String title) {
		super(context, id, title);
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		checkBox.setChecked(isChecked);
	}

	@Override
	public View createView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.item_check, null);
		checkBox = (CheckBox)view.findViewById(android.R.id.content);
		checkBox.setOnCheckedChangeListener(this);
		return view;
	}

	@Override
	public LuaValue createLua() {
		LuaValue lua = super.createLua();
		
		lua.set("isChecked", new ZeroArgFunction() {

			@Override
			public LuaValue call() {
				return valueOf(isChecked());
			}
		});
		
		lua.set("setChecked", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				setChecked(p1.toboolean());
				return NIL;
			}
		});
		
		return lua;
	}

	@Override
	public void onCheckedChanged(CompoundButton p1, boolean p2) {
		isChecked = p2;
	}
}
