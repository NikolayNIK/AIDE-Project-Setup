package com.nikolaynik.aideprojectsetup.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.nikolaynik.aideprojectsetup.R;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class TextItem extends Item {

	private String text;
	private TextView textView;
	
	public TextItem(Context context, String id) {
		super(context, id);
	}
	
	public TextItem(Context context, String id, String title) {
		super(context, id, title);
	}

	public TextItem(Context context, String id, String title, String text) {
		super(context, id, title);
		this.setText(text);
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.textView.setText(text);
		this.text = text;
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
		
		return lua;
	}
	
	@Override
	public View createView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.item_text, null);
		textView = (TextView)view.findViewById(android.R.id.content);
		return view;
	}
}
