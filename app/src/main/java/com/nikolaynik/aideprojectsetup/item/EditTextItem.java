package com.nikolaynik.aideprojectsetup.item;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.nikolaynik.aideprojectsetup.R;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import android.os.Bundle;

public class EditTextItem extends Item implements TextWatcher {

	private String hint = "", text = "";
	private EditText editText;
	private LuaValue textChanged;
	
	public EditTextItem(Context context, String id) {
		super(context, id);
	}

	public EditTextItem(Context context, String id, String title) {
		super(context, id, title);
	}

	public EditTextItem(Context context, String id, String title, String text) {
		super(context, id, title);
		this.setText(text);
	}

	public EditTextItem(Context context, String id, String title, String text, String hint) {
		super(context, id, title);
		this.setText(text);
		this.setHint(hint);
	}

	public EditTextItem(Context context, String title, String text, String hint, LuaValue textChanged) {
		super(context, title);
		this.setText(text);
		this.setHint(hint);
		this.textChanged = textChanged;
	}
	
	public String getText() {
		return text;
	}
	
	public String getHint() {
		return hint;
	}
	
	public void setText(String text) {
		editText.setText(text);
	}
	
	public void setHint(String hint) {
		this.hint = hint;
		this.editText.setHint(hint);
	}
	
	public void setTextChanged(LuaValue textChanged) {
		this.textChanged = textChanged;
	}
	
	protected void setEditText(EditText editText) {
		this.editText = editText;
		this.editText.addTextChangedListener(this);
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
		
		lua.set("getHint", new ZeroArgFunction() {

			@Override
			public LuaValue call() {
				return valueOf(hint);
			}
		});
		
		lua.set("setText", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				setText(p1.tojstring());
				return NIL;
			}
		});
		
		lua.set("setHint", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				setHint(p1.tojstring());
				return NIL;
			}
		});
		
		lua.set("setTextChanged", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue p1) {
				setTextChanged(p1);
				return NIL;
			}
		});
		
		return lua;
	}
	
	@Override
	public View createView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.item_edittext, null);
		setEditText((EditText)view.findViewById(android.R.id.content));
		return view;
	}
	
	@Override
	public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
		
	}

	@Override
	public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {
		
	}

	@Override
	public void afterTextChanged(Editable p1) {
		text = p1.toString();
		if(textChanged != null && !textChanged.isnil()) textChanged.call();
	}

	@Override
	public void toBundle(Bundle bundle) {
		bundle.putString(getID(), text);
	}

	@Override
	public void fromBundle(Bundle bundle) {
		setText(bundle.getString(getID()));
	}
}
