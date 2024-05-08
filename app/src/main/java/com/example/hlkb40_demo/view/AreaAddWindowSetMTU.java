package com.example.hlkb40_demo.view;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.hlkb40_demo.R;


public class AreaAddWindowSetMTU extends  Dialog implements View.OnClickListener {
	private Context context;
	private Button confirmBtn;
	private Button cancelBtn;
	private EditText oldPwd;
	private String oldPeriod = "";
	private PeriodListener listener;
	
	public AreaAddWindowSetMTU(Context context) {
		super(context);
		this.context = context;
	}

	public AreaAddWindowSetMTU(Context context, int theme, PeriodListener listener, String m) {
		super(context, theme);
		this.context = context;
		this.listener = listener;
		this.oldPeriod = m;
	}

	
	/****
	 * 
	 * @author mqw
	 *
	 */
	public interface PeriodListener {
		public void refreshListener(String oldPwd);
		public void clearListener();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.window_area_set_pwd);
		
		setCancelable(false);
		cancelBtn = (Button) findViewById(R.id.btnCancle);
		confirmBtn = (Button) findViewById(R.id.confirm_btn);
		oldPwd = (EditText) findViewById(R.id.oldPwd);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

		oldPwd.setText(oldPeriod);
		cancelBtn.setOnClickListener(this);
		confirmBtn.setOnClickListener(this);

		oldPwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			   @Override
			   public void onFocusChange(View v, boolean hasFocus) {
			       if (hasFocus) {
			            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			       }
			   }
			});
		
	}
	

		 
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btnCancle){
			listener.clearListener();
			dismiss();
			if (null != getWindow()) {
				View view = getWindow().peekDecorView();
				if (view != null) {
					InputMethodManager inputmanger = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
			}
		} else if (id == R.id.confirm_btn) {
			String strV = oldPwd.getText().toString();
			if (strV.equalsIgnoreCase("")) {
				return;
			}

			if (Integer.parseInt(strV) < 0 || Integer.parseInt(strV) > 512) {
				return;
			}

			listener.refreshListener(strV);
			dismiss();
		}
	}
}