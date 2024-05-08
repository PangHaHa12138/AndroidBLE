package com.example.hlkb40_demo.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hlkb40_demo.R;


public class AreaAddWindow extends Dialog implements View.OnClickListener {
	private Context context;
	private Button confirmBtn;
	private Button cancelBtn;
	private EditText areaNameEt;
	private TextView titleTv;
	private String period = "";
	private PeriodListener listener;
	private String defaultName = "",title;
	private boolean inputPwd = false;
	public AreaAddWindow(Context context) {
		super(context);
		this.context = context;
	}

	public AreaAddWindow(Context context, int theme, PeriodListener listener) {
		super(context, theme);
		this.context = context;
		this.listener = listener;
	}
	
	public AreaAddWindow(Context context, int theme, String titleName, PeriodListener listener, String defaultName, boolean pwd) {
		super(context, theme);
		this.context = context;
		this.listener = listener;
		this.defaultName = defaultName;
		this.title = titleName;
		this.inputPwd = pwd;
	}

	
	/****
	 * 
	 * @author mqw
	 *
	 */
	public interface PeriodListener {
		public void refreshListener(String string);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.window_area_add);
		confirmBtn = (Button) findViewById(R.id.confirm_btn);
		cancelBtn = (Button) findViewById(R.id.cancel_btn);
		areaNameEt = (EditText) findViewById(R.id.areaName);
		if (inputPwd) {
			areaNameEt.setInputType(InputType.TYPE_CLASS_NUMBER);
			areaNameEt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
		}
		titleTv = (TextView) findViewById(R.id.dialog_title);
		titleTv.setText(title);
		confirmBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		areaNameEt.setText(defaultName);
		areaNameEt.setSelection(defaultName.length());

		setCancelable(false);

		checkType();

		areaNameEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			   @SuppressLint("NewApi")
			@Override
			   public void onFocusChange(View v, boolean hasFocus) {
			       if (hasFocus) {
			            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			       }
			   }
			});
		
		
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if (id == R.id.cancel_btn){
			dismiss();
		} else if (id == R.id.confirm_btn){
			period = areaNameEt.getText().toString();
			if(period.equals(""))
			{
				Toast.makeText(context,"值不能为空！",Toast.LENGTH_SHORT).show();
			}else{
				listener.refreshListener(period);
				dismiss();
			}
		}
	}


	public void setTitleAndValue(String strTitle,String strValue) {
		this.title = strTitle;
		this.defaultName = strValue;
		if (titleTv == null || areaNameEt == null) {
			return;
		}
		titleTv.setText(title);
		areaNameEt.setText(defaultName);
	}

	/**
	 * 输入类型：文本text,整形数字：int,带小数点：float
	 * @param strEditType
	 */
	public void setEditType(String strEditType) {
		this.strEditType = strEditType;
		checkType();
	}

	private String strEditType = "text";
	private void checkType() {
		if (areaNameEt == null) {
			return;
		}
		// 字符串
		if (strEditType.equalsIgnoreCase("text")) {
			areaNameEt.setInputType(InputType.TYPE_CLASS_TEXT );
		}
		// 整形数字
		else if (strEditType.equalsIgnoreCase("int")) {
			areaNameEt.setInputType(InputType.TYPE_CLASS_NUMBER );
		}
		// 小数点
		else if (strEditType.equalsIgnoreCase("float")) {
			areaNameEt.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
		}

	}

}