package com.nikolaynik.aideprojectsetup.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.nikolaynik.aideprojectsetup.R;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class ButtonItem extends Item implements OnClickListener {

	private Button button;
	private LuaValue clicked;
	private String text = "";
	
	public ButtonItem(Context context, String id) {
		super(context, id);
	}

	public ButtonItem(Context context, String id, String title) {
		super(context, id, title);
	}

	public ButtonItem(Context context, String id, String title, String text) {
		super(context, id, title);
		this.setText(text);
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
		this.button.setText(text);
	}
	
	public void setClicked(LuaValue clicked) {
		this.clicked = clicked;
	}

	@Override
	public LuaValue createLua() {
		LuaValue lua = super.createLua();
		
		lua.set("getText", new ZeroArgFunction() {

			@Override
			public LuaValue call() {
				return valueOf(getText());
			}
		});
		
		lua.set("setText", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				setText(p1.tojstring());
				return NIL;
			}
		});
		
		lua.set("setClicked", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				setClicked(p1);
				return NIL;
			}
		});
		
		return lua;
	}
	
	@Override
	public View createView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.item_button, null);
		button = (Button)view.findViewById(android.R.id.content);
		button.setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View p1) {
		if(clicked != null && !clicked.isnil()) clicked.call();
	}
}
