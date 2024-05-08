package com.example.hlkb40_demo.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import com.example.hlkb40_demo.R
import com.example.hlkb40_demo.UUIDInfo
import com.example.hlkb40_demo.adapter.MySpinnerAdapter
import java.util.*

class AreaSelectServerWindow : Dialog, View.OnClickListener {
    private var mContext: Context? = null
    private var confirmBtn: Button? = null
    private var cancelBtn: Button? = null
    private var listener: PeriodListener? = null
    private lateinit var spinnerServer: Spinner
    private lateinit var spinnerWrite: Spinner
    private lateinit var spinnerRead: Spinner
    private var selectServer : UUIDInfo? = null
    private var selectWrite : UUIDInfo? = null
    private var selectRead : UUIDInfo? = null
    var readArray = ArrayList<UUIDInfo>()
    var writeArray = ArrayList<UUIDInfo>()

    private var serverList = ArrayList<UUIDInfo>()
    private var readCharaMap = HashMap<String, ArrayList<UUIDInfo>>()
    private var writeCharaMap = HashMap<String, ArrayList<UUIDInfo>>()

    constructor(context: Context) : super(context) {
        this.mContext = context
    }

    constructor(context: Context, theme: Int, listener: PeriodListener) : super(context, theme) {
        this.mContext = context
        this.listener = listener
    }

    fun setServerList(serverList: ArrayList<UUIDInfo>) {
        this.serverList = serverList
    }

    fun setReadCharaMap(readCharaMap: HashMap<String, ArrayList<UUIDInfo>>) {
        this.readCharaMap = readCharaMap
    }

    fun setWriteCharaMap(writeCharaMap: HashMap<String, ArrayList<UUIDInfo>>) {
        this.writeCharaMap = writeCharaMap
    }

    fun setSelectServer(serverInfo: UUIDInfo?, readInfo: UUIDInfo?, writeInfo: UUIDInfo?) {
        selectServer = serverInfo
        selectRead = readInfo
        selectWrite = writeInfo

    }

    /****
     *
     * @author mqw
     */
    interface PeriodListener {
        fun refreshListener(selectServer: UUIDInfo, selectRead: UUIDInfo, selectWrite: UUIDInfo)
        fun clearListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.window_area_select_server)
        setCancelable(false)
        spinnerServer = findViewById<View>(R.id.spinnerServer) as Spinner
        spinnerWrite = findViewById<View>(R.id.spinnerWrite) as Spinner
        spinnerRead = findViewById<View>(R.id.spinnerRead) as Spinner
        cancelBtn = findViewById<View>(R.id.btnCancle) as Button
        confirmBtn = findViewById<View>(R.id.confirm_btn) as Button
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        cancelBtn!!.setOnClickListener(this)
        confirmBtn!!.setOnClickListener(this)


        val myAdapter = MySpinnerAdapter(serverList, context, true)
        spinnerServer.adapter = myAdapter
        for ( iSelect in 0 until serverList.size) {
            if (serverList[iSelect].uuidString.equals(selectServer?.uuidString,true)) {
                spinnerServer.setSelection(iSelect)
                break
            }
        }
        var wArray = writeCharaMap[selectServer?.uuidString]
        var rArray = readCharaMap[selectServer?.uuidString]
        if (wArray == null) wArray = ArrayList<UUIDInfo>()
        if (rArray == null) rArray = ArrayList<UUIDInfo>()
        writeArray = wArray
        readArray = rArray
        val myWriteAdapter = MySpinnerAdapter(writeArray, mContext, false)
        spinnerWrite.adapter = myWriteAdapter
        var myReadAdapter = MySpinnerAdapter(readArray, mContext, false)
        spinnerRead.adapter = myReadAdapter

        for ( iSelect in 0 until writeArray.size) {
            if (writeArray[iSelect].uuidString.equals(selectWrite?.uuidString,true)) {
                spinnerWrite.setSelection(iSelect)
                break
            }
        }

        for ( iSelect in 0 until readArray.size) {
            if (readArray[iSelect].uuidString.equals(selectRead?.uuidString,true)) {
                spinnerRead.setSelection(iSelect)
                break
            }
        }

        spinnerServer.onItemSelectedListener = onItemSelectedListener
        spinnerWrite.onItemSelectedListener = onItemWriteListener
        spinnerRead.onItemSelectedListener = onItemReadListener

    }

    /** 选项改变监听事件 */
    private val onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (serverList.size == 0) return
            selectServer = serverList[position]
            val strServerUUID = serverList[position].uuidString
            var wArray = writeCharaMap[strServerUUID]
            var rArray = readCharaMap[strServerUUID]
            if (wArray == null) wArray = ArrayList<UUIDInfo>()
            if (rArray == null) rArray = ArrayList<UUIDInfo>()
            writeArray = wArray
            readArray = rArray
            var myWriteAdapter = MySpinnerAdapter(writeArray, mContext, false)
            spinnerWrite.adapter = myWriteAdapter
            var myReadAdapter = MySpinnerAdapter(readArray, mContext, false)
            spinnerRead.adapter = myReadAdapter
        }
        override fun onNothingSelected(parent: AdapterView<*>) {
            Log.e("TAG_MainActivity", parent.toString())
        }
    }

    /** 选项改变监听事件 */
    private val onItemWriteListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (writeArray.size == 0) return
            selectWrite = writeArray[position]
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {
            Log.e("TAG_MainActivity", parent.toString())
        }
    }
    /** 选项改变监听事件 */
    private val onItemReadListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (readArray.size == 0) return
            selectRead = readArray[position]
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {
            Log.e("TAG_MainActivity", parent.toString())
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.btnCancle -> {
                listener!!.clearListener()
                dismiss()
                val view = window!!.peekDecorView()
                if (view != null) {
                    val inputmanger = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputmanger.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
            R.id.confirm_btn -> {
                listener!!.refreshListener(selectServer!!,selectRead!!,selectWrite!!)
                dismiss()
            }
            else -> {
            }
        }
    }
}