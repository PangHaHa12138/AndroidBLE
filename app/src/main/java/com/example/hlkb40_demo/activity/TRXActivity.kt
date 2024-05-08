package com.example.hlkb40_demo.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.hlkb40_demo.*
import com.example.hlkb40_demo.utilInfo.Utils
import com.example.hlkb40_demo.view.AddMenuWindowDialog
import com.example.hlkb40_demo.view.AreaAddWindow
import com.example.hlkb40_demo.view.AreaSelectServerWindow

import java.text.SimpleDateFormat
import java.util.*

class TRXActivity : BaseActivity(), View.OnClickListener{

    private lateinit var getServerDialog:ProgressDialog
    private var iSendLength = 0
    private var iRecvLength = 0
    private var getServerTimer = Timer()

    private var imgBack:ImageView?=null
    private var tvClear:TextView?=null
    private var switchTimer:Switch?=null
    private var edSpeed:EditText?=null
    private var edMaxNum:EditText?=null
    private var edData:EditText?=null
    private var rbStr:RadioButton?=null
    private var btnSend:Button?=null
    private var btnChangeServer:Button?=null
    private var tvMenu:TextView?=null
    private var tvName:TextView?=null
    private var tvMac:TextView?=null
    private var tvSendLen:TextView?=null
    private var tvRecvLen:TextView?=null
    private var tvServerInfo:TextView?=null
    private var tvReadInfo:TextView?=null
    private var tvWriteInfo:TextView?=null
    private var tvLog:TextView?=null
    private var scrollView:ScrollView?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trx_data)

        imgBack = findViewById(R.id.imgBack)
        tvClear = findViewById(R.id.tvClear)
        switchTimer = findViewById(R.id.switchTimer)
        edSpeed = findViewById(R.id.edSpeed)
        edMaxNum = findViewById(R.id.edMaxNum)
        edData = findViewById(R.id.edData)
        rbStr = findViewById(R.id.rbStr)
        btnSend = findViewById(R.id.btnSend)
        btnChangeServer = findViewById(R.id.btnChangeServer)
        tvMenu = findViewById(R.id.tvMenu)
        tvName = findViewById(R.id.tvName)
        tvMac = findViewById(R.id.tvMac)
        tvSendLen = findViewById(R.id.tvSendLen)
        tvRecvLen = findViewById(R.id.tvRecvLen)
        tvServerInfo = findViewById(R.id.tvServerInfo)
        tvReadInfo = findViewById(R.id.tvReadInfo)
        tvWriteInfo = findViewById(R.id.tvWriteInfo)
        tvLog = findViewById(R.id.tvLog)
        scrollView = findViewById(R.id.scrollView)

        initUI()
        initData()

        trxActivity = this

    }

    companion object {
        private lateinit var trxActivity: TRXActivity
        fun getInstance(): TRXActivity {
            return trxActivity
        }
    }



    fun initUI() {

        getServerDialog = ProgressDialog(this)
        getServerDialog.setOnDismissListener {
            startGetServerTimer(false)
        }
        getServerDialog.setCanceledOnTouchOutside(false)

        imgBack?.setOnClickListener {
            onBackPressed()
        }
        tvClear?.setOnClickListener(this)
        switchTimer?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val strSpeed = edSpeed?.text.toString()
                val strMaxNum = edMaxNum?.text.toString()
                val strSendData = edData?.text.toString()
                if (strSpeed != null && !strSpeed.equals("") && (strSpeed.toInt() != 0) &&
                        strMaxNum != null && !strMaxNum.equals("") &&
                        strSendData != null && !strSendData.equals("")) {
                    val iCount = strMaxNum.toInt()
                    strWillSendData = strSendData
                    SendTimer(true,strSpeed.toLong(),iCount)
                }
                else {
                    switchTimer?.isChecked = false
                }
            }
            else
                SendTimer(false,0,0)
        }
        rbStr?.isChecked = true
        btnSend?.setOnClickListener(this)
        btnChangeServer?.setOnClickListener(this)
        tvMenu?.setOnClickListener{
            strNowCheckCode = BluetoothApplication.getInstance().getValueBySharedPreferences(
                    DeviceScanActivity.getInstance().nowSelectDevice.address)
            checkCodeInfo(strNowCheckCode)
//            deviceSelectMenu()
        }

    }

    private var iMaxNum = 0
    private var iSpeed:Long = 10
    private var timerSend = Timer()
    private var strWillSendData = ""
    private fun SendTimer(isRun: Boolean,lSpeed:Long,iCount:Int) {
        timerSend.cancel()
        if (isRun) {
            iMaxNum = iCount
            if (iCount == 0) iMaxNum = -1
            iSpeed = lSpeed
            timerSend = Timer()
            timerSend.schedule(object : TimerTask(){
                override fun run() {
                    // 无限循环
                    if (iMaxNum == -1) {
                        startSendData(strWillSendData)
                    }
                    // 有次数循环
                    else if (iMaxNum > 0){
                        startSendData(strWillSendData)
                        --iMaxNum
                    }
                    // 停止循环
                    else if (iMaxNum == 0) {
                        timerSend.cancel()
                        runOnUiThread { switchTimer?.isChecked = false }
                    }
                }
            },0,iSpeed)
        }

    }

    /** 初始化数据 */
    @SuppressLint("MissingPermission")
    private fun initData() {
        tvName?.text = DeviceScanActivity.getInstance().nowSelectDevice.name
        tvMac?.text = DeviceScanActivity.getInstance().nowSelectDevice.address

        tvSendLen?.text = "发送:$iSendLength"
        tvRecvLen?.text = "接收:$iRecvLength"



        // 断开或连接 状态发生变化时调用
        DeviceScanActivity.getInstance().mBLE.setOnConnectListener(OnConnectListener)
        // 发现BLE终端的Service时回调
        DeviceScanActivity.getInstance().mBLE.setOnServiceDiscoverListener(mOnServiceDiscover)
        // 读操作的回调
        DeviceScanActivity.getInstance().mBLE.setOnDataAvailableListener(OnDataAvailableListener)
        // 写操作的回调
        DeviceScanActivity.getInstance().mBLE.setOnWriteDataListener(OnWriteDataListener)
        // 接收到硬件返回的数据
        DeviceScanActivity.getInstance().mBLE.setOnRecvDataListener(OnRecvDataListerner)
        DeviceScanActivity.getInstance().mBLE.setOnChangeMTUListener(onChangeMTUListener)

        // 设置MTU
        DeviceScanActivity.getInstance().mBLE.requestMtu(512)
        getServerDialog.setMessage("获取服务信息,请稍后...")
        getServerDialog.show()
        startGetServerTimer(true)

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        // 断开或连接 状态发生变化时调用
//        DeviceScanActivity.getInstance().mBLE.setUnConnectListener()
//        // 发现BLE终端的Service时回调
//        DeviceScanActivity.getInstance().mBLE.setUnServiceDiscoverListener()
//        // 读操作的回调
//        DeviceScanActivity.getInstance().mBLE.setUnDataAvailableListener()
//        // 写操作的回调
//        DeviceScanActivity.getInstance().mBLE.setUnWriteDataListener()
//        // 接收到硬件返回的数据
//        DeviceScanActivity.getInstance().mBLE.setUnRecvDataListener()
//        DeviceScanActivity.getInstance().mBLE.setUnChangeMTUListener()

    }

    private fun deviceSelectMenu() {
        val list = ArrayList<String>()
        list.add("设备信息")
        list.add("参数设置")
        val dialog1 = AddMenuWindowDialog(mContext, R.style.dialog, list, "菜单")
        dialog1.setListener { number, strItem ->
            when (number) {
                0 -> {
                    startActivity(Intent(mContext, DeviceInfoActivity().javaClass))
                }
                1 -> {
                    strNowCheckCode = BluetoothApplication.getInstance().getValueBySharedPreferences(
                            DeviceScanActivity.getInstance().nowSelectDevice.address)
                    checkCodeInfo(strNowCheckCode)
//                    startActivity(Intent(mContext,SetWorkActivity().javaClass))
                }
            }
        }
        dialog1.show()

    }

    var readCodeUUID : UUIDInfo? = null
    var writeCodeUUID : UUIDInfo? = null
    var strNowCheckCode = ""
    // 校验操作码
    private fun checkCodeInfo(strCode : String) {
        // 输入校验码
        if (strCode == "") {

            runOnUiThread {
                val checkCodeDialog = AreaAddWindow(mContext,R.style.dialog,"请输入秘钥",object : AreaAddWindow.PeriodListener{
                    override fun refreshListener(string: String) {
                        strNowCheckCode = string
                        checkCodeInfo(strNowCheckCode)
                    }
                },"",false)
                checkCodeDialog.show()
            }

        }
        else {
            readCodeUUID = null
            writeCodeUUID = null
            val readArray = readCharaMap[strSET_Server]
            if (readArray!!.size > 0) {
                for (readInfo in readArray) {
                    if (readInfo.uuidString.equals(strCHECK_Read)) {
                        readCodeUUID = readInfo
                        break
                    }
                }
            }
            val writeArray = writeCharaMap[strSET_Server]
            if (writeArray!!.size > 0) {
                for (writeInfo in writeArray) {
                    if (writeInfo.uuidString.equals(strCHECK_Write)) {
                        writeCodeUUID = writeInfo
                        break
                    }
                }
            }

            if (writeCodeUUID == null || readCodeUUID == null) {
                showToast("未找到相关特征！")
                return
            }
            var mgattCharacteristic = writeCodeUUID?.bluetoothGattCharacteristic!!
            mgattCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            mgattCharacteristic.value = strCode.toByteArray()
            DeviceScanActivity.getInstance().mBLE.writeCharacteristic(mgattCharacteristic)
        }
    }

    private fun startGetServerTimer(isRun: Boolean) {
        getServerTimer.cancel()
        if (isRun) {
            getServerTimer = Timer()
            getServerTimer.schedule(object : TimerTask() {
                override fun run() {
                    val getResult = DeviceScanActivity.getInstance().mBLE.getServiceByGatt()
                    if (!getResult) {
                        runOnUiThread {
                            getServerDialog.dismiss()
                            Toast.makeText(mContext, "getServiceByGatt:$getResult", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }, 0, 3000)
        }

    }

    // 断开或连接 状态发生变化时调用
    private val OnConnectListener = object : BluetoothLeClass.OnConnectListener {
        override fun onConnected(gatt: BluetoothGatt?, status: Int, newState: Int) {
            sendBroadcast(Intent(BC_ConnectStatus)
                    .putExtra("isConnectState",true)
                    .putExtra("strConnectState","Connected to GATT server."))
            var msgStop = Message()
            msgStop.what = iConnectState
            msgStop.obj = "Connected to GATT server."
            selfHandler.sendMessage(msgStop)
        }
        override fun onDisconnect(gatt: BluetoothGatt?, status: Int, newState: Int) {
            sendBroadcast(Intent(BC_ConnectStatus)
                    .putExtra("isConnectState",false)
                    .putExtra("strConnectState","Disconnected from GATT server."))
            SendTimer(false,0,0)
            var msgStop = Message()
            msgStop.what = iConnectState
            msgStop.obj = "Disconnected from GATT server."
            selfHandler.sendMessage(msgStop)
        }
        override fun onConnectting(gatt: BluetoothGatt?, status: Int, newState: Int) {
            sendBroadcast(Intent(BC_ConnectStatus)
                    .putExtra("isConnectState",false)
                    .putExtra("strConnectState","Connectting to GATT..."))
            var msgStop = Message()
            msgStop.what = iConnectState
            msgStop.obj = "Connectting to GATT..."
            selfHandler.sendMessage(msgStop)
        }
    }

    var serverList = ArrayList<UUIDInfo>()
    var readCharaMap = HashMap<String, ArrayList<UUIDInfo>>()
    var writeCharaMap = HashMap<String, ArrayList<UUIDInfo>>()
    private var selectServer : UUIDInfo? = null
    private var selectWrite : UUIDInfo? = null
    private var selectRead : UUIDInfo? = null

    /**
     * 搜索到BLE终端服务的事件
     */
    private val mOnServiceDiscover = BluetoothLeClass.OnServiceDiscoverListener {
        Log.e("onConnected", "mOnServiceDiscover: ${it.services.size}")

        startGetServerTimer(false)
        val gattlist = it.services
        serverList.clear()
        readCharaMap.clear()
        writeCharaMap.clear()
        for (bluetoothGattService in gattlist) {
            val serverInfo = UUIDInfo(bluetoothGattService.uuid)
            serverInfo.strCharactInfo = "[Server]"
            serverList.add(serverInfo)
            val readArray = ArrayList<UUIDInfo>()
            val writeArray = ArrayList<UUIDInfo>()
            val characteristics = bluetoothGattService.characteristics
            for (characteristic in characteristics) {
                val charaProp = characteristic.properties
                var isRead = false
                var isWrite = false
                // 具备读的特征
                var strReadCharactInfo = ""
                // 具备写的特征
                var strWriteCharactInfo = ""
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                    isRead = true
                    strReadCharactInfo += "[PROPERTY_READ]"
                    Log.e(ContentValues.TAG, "read_chara=" + characteristic.uuid + "----read_service=" + bluetoothGattService.uuid)
                }
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                    isWrite = true
                    strWriteCharactInfo += "[PROPERTY_WRITE]"
                    Log.e(ContentValues.TAG, "write_chara=" + characteristic.uuid + "----write_service=" + bluetoothGattService.uuid)
                }
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
                    isWrite = true
                    strWriteCharactInfo += "[PROPERTY_WRITE_NO_RESPONSE]"
                    Log.e(ContentValues.TAG, "write_chara=" + characteristic.uuid + "----write_service=" + bluetoothGattService.uuid)
                }
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                    isRead = true
                    strReadCharactInfo += "[PROPERTY_NOTIFY]"
                    Log.e(ContentValues.TAG, "notify_chara=" + characteristic.uuid + "----notify_service=" + bluetoothGattService.uuid)
                }
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0) {
                    isRead = true
                    strReadCharactInfo += "[PROPERTY_INDICATE]"
                    Log.e(ContentValues.TAG, "indicate_chara=" + characteristic.uuid + "----indicate_service=" + bluetoothGattService.uuid)
                }
                if (isRead) {
                    val uuidInfo = UUIDInfo(characteristic.uuid)
                    uuidInfo.strCharactInfo = strReadCharactInfo
                    uuidInfo.bluetoothGattCharacteristic = characteristic
                    readArray.add(uuidInfo)
                }
                if (isWrite) {
                    val uuidInfo = UUIDInfo(characteristic.uuid)
                    uuidInfo.strCharactInfo = strWriteCharactInfo
                    uuidInfo.bluetoothGattCharacteristic = characteristic
                    writeArray.add(uuidInfo)
                }
                readCharaMap.put(bluetoothGattService.uuid.toString(), readArray)
                writeCharaMap.put(bluetoothGattService.uuid.toString(), writeArray)
            }
        }

        for (serverInfo in serverList) {
            if (serverInfo.uuidString.equals(strSerial_Server)) {
                selectServer = serverInfo
                break
            }
        }

        val readArray = readCharaMap[selectServer?.uuidString]
        val writeArray = writeCharaMap[selectServer?.uuidString]
        if (readArray != null) {
            for (readInfo in readArray) {
                if (readInfo.uuidString.equals(strSerial_Read)) {
                    selectRead = readInfo
                    break
                }
            }
        }
        if (writeArray != null) {
            for (writeInfo in writeArray) {
                if (writeInfo.uuidString.equals(strSerial_Write)) {
                    selectWrite = writeInfo
                    break
                }
            }
        }

        bindServerSubNofify()
    }

    // 读操作的回调
    private val OnDataAvailableListener = object : BluetoothLeClass.OnDataAvailableListener {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            sendBroadcast(Intent(BC_ReadData).putExtra("UUID",characteristic!!.uuid).putExtra("status",status).putExtra("data",characteristic.value))
            // 正在验证code
            if (readCodeUUID != null && characteristic?.uuid.toString().equals(readCodeUUID?.uuidString)) {
//                val strResultDataHex = Utils.bytesToHexString(characteristic?.value)
                // 验证通过，则直接跳转参数设置页面
                if (characteristic?.value!![0].toInt() == 1) {
                    BluetoothApplication.getInstance().saveValueBySharedPreferences(
                            DeviceScanActivity.getInstance().nowSelectDevice.address,strNowCheckCode)
                    strNowCheckCode = ""
                    readCodeUUID = null
                    writeCodeUUID = null
                    runOnUiThread { startActivity(Intent(mContext, SetWorkActivity().javaClass)) }

                }
                // 重新校验
                else {
                    showToast("验证失败，请重试！")
                    checkCodeInfo("")
                }

            }
            // 读数据的通道并不是当前页面选择的通道，则不理会
            if (!characteristic?.uuid.toString().equals(selectRead?.uuidString,true)) {
                return
            }
            val strStatus = BluetoothLeClass.strResultInfoByStatus(status)
            var msgStart = Message()
            msgStart.what = iConnectState
            msgStart.obj = strStatus
            selfHandler.sendMessage(msgStart)

        }
    }

    // 写操作的回调
    private val OnWriteDataListener = object : BluetoothLeClass.OnWriteDataListener {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun OnCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            Log.e("OnWriteDataListener", "writeStatus:${status == 0}")
            sendBroadcast(Intent(BC_WriteData).putExtra("UUID",characteristic!!.uuid).putExtra("status",status))
            // 正在验证密码
            if (writeCodeUUID != null) {
                if (status != 0) {
                    showToast("验证码写入失败！")
                    return
                }
                val isRead = DeviceScanActivity.getInstance().mBLE.readCharacteristic(readCodeUUID!!.bluetoothGattCharacteristic)
                if (!isRead) {
                    showToast("读取失败！")
                }
            }

            // 写数据的通道并不是当前页面选择的通道，则不理会
            if (!characteristic?.uuid.toString().equals(selectWrite?.uuidString,true)) {
                return
            }
            // 如果没在升级，则打印写的结果
            if (status != 0) {
                val strStatus = BluetoothLeClass.strResultInfoByStatus(status)
                var msgStart = Message()
                msgStart.what = iConnectState
                msgStart.obj = "Write,Reuslt:$strStatus"
                selfHandler.sendMessage(msgStart)
                return
            }
        }
    }

    // 接收到硬件返回的数据
    private val OnRecvDataListerner = object : BluetoothLeClass.OnRecvDataListerner {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun OnCharacteristicRecv(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            if (characteristic!!.value.size == 0) return
            Log.e("OnRecvDataListerner", "RECVDATA,to HEX:${Utils.bytesToHexString(characteristic!!.value)},to STR:${String(characteristic!!.value)}")
            sendBroadcast(Intent(BC_RecvData).putExtra("UUID",characteristic!!.uuid).putExtra("data",characteristic.value))
            // 接收数据的通道并不是当前页面选择的通道，则不理会
            if (!characteristic.uuid.toString().equals(selectRead?.uuidString,true)) {
                return
            }
            val iNowRecvLength = characteristic!!.value.size
            var string = ""
            // 字符
            if (rbStr!!.isChecked)
                string = String(characteristic!!.value)
            // 十六进制
            else
                string = Utils.bytesToHexString(characteristic!!.value)
            var bundle = Bundle()
            bundle.putInt("iLength",iNowRecvLength)
            bundle.putString("strData",string)

            var msgStart = Message()
            msgStart.what = iRevDataLog
            msgStart.obj = bundle
            selfHandler.sendMessage(msgStart)



        }
    }


    /** MTU改变监听 */
    private val onChangeMTUListener = object : BluetoothLeClass.OnChangeMTUListener {
        override fun onChangeMTUListener(isResult: Boolean?, strMsg: String?, iMTU: Int) {
            Log.e("onChangeMTUListener", "MTU设置结果：$strMsg")
            sendBroadcast(Intent(BC_ChangeMTU).putExtra("strMsg",strMsg).putExtra("iMTU",iMTU))
            runOnUiThread {
                updateLog(strMsg!!)
            }
        }
    }

    /** 绑定服务，订阅通知 */
    private fun bindServerSubNofify() {

        runOnUiThread {
            getServerDialog.dismiss()
            tvServerInfo?.text = "S:${selectServer?.uuidString}"

            if (selectWrite != null)
                tvWriteInfo?.text = "W:${selectWrite?.uuidString}"

            if (selectRead == null) {
                tvReadInfo?.text = "R:"
                return@runOnUiThread
            }
            tvReadInfo?.text = "R:${selectRead?.uuidString}"
            // 订阅通知
            val isNotification = DeviceScanActivity.getInstance().mBLE.setCharacteristicNotification(selectRead?.bluetoothGattCharacteristic, true)
            Log.e("TestDataActivity", "isNotification:$isNotification")
            updateLog("subscribe Notification:$isNotification")
            if (isNotification) {
                val descriptors: List<BluetoothGattDescriptor> = selectRead?.bluetoothGattCharacteristic!!.descriptors
                for (descriptor in descriptors) {
                    // 读写开关操作，writeDescriptor 否则可能读取不到数据。
                    val b1 = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    if (b1) {
                        val isB = DeviceScanActivity.getInstance().mBLE.writeDescriptor(descriptor)
                        Log.e(ContentValues.TAG, "startRead: " + "监听收数据")
                        updateLog("writeDescriptor:$isB")
                    }
                }
            }
        }
    }

    val iConnectState = 111
    val iRevDataLog = 222
    val iSendDataLog = 333
    val selfHandler = object : Handler() {
        override fun dispatchMessage(msg: Message) {
            super.dispatchMessage(msg)
            when(msg.what) {
                iConnectState -> {
                    val writePrecent = (msg.obj).toString()
                    updateLog(writePrecent)
                }
                iRevDataLog -> {
                    val dataBundle = msg.obj as Bundle
                    val iLength = dataBundle["iLength"].toString().toInt()
                    val strData = dataBundle["strData"].toString()
                    iRecvLength += iLength
                    tvRecvLen?.text = "接收:$iRecvLength"
                    updateLog("<<<接收:$strData,长度:$iLength")
                }
                iSendDataLog -> {
                    val dataBundle = msg.obj as Bundle
                    val iLength = dataBundle["iLength"].toString().toInt()
                    val strData = dataBundle["strData"].toString()
                    iSendLength += iLength
                    tvSendLen?.text = "发送:$iSendLength"
                    updateLog(">>>发送:$strData,长度:$iLength")
                }
            }

        }

    }

    private fun startSendData(strSend: String) {
        if (selectWrite == null) return
        var mgattCharacteristic = selectWrite?.bluetoothGattCharacteristic!!
        mgattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
        var iNowSendLength = 0
        // 字符发送
        if (rbStr!!.isChecked) {
            mgattCharacteristic.setValue(strSend.toByteArray())
            iNowSendLength= strSend.length
        }
        // 十六进制发送
        else {
            mgattCharacteristic.setValue(Utils.hexStringToBytes(strSend))
            iNowSendLength = (strSend.length/2)
        }

        var bundle = Bundle()
        bundle.putInt("iLength",iNowSendLength)
        bundle.putString("strData",strSend)

        var msgStart = Message()
        msgStart.what = iSendDataLog
        msgStart.obj = bundle
        selfHandler.sendMessage(msgStart)

        DeviceScanActivity.getInstance().mBLE.writeCharacteristic(mgattCharacteristic)

    }

    private fun updateLog(strValue: String) {
        val date = Date()
        val dateFormat = SimpleDateFormat("HH:mm:ss:SSS")
        if (tvLog!!.lineCount > 200) {
            tvLog?.text = "\n${dateFormat.format(date)}$strValue"
        }
        else {
            tvLog?.text = tvLog?.text.toString().plus("\n${dateFormat.format(date)}$strValue")
            scrollView?.fullScroll(ScrollView.FOCUS_DOWN)
        }


    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.tvClear -> {
                tvLog?.text = ""
                iSendLength = 0
                iRecvLength = 0
                tvSendLen?.text = "发送:$iSendLength"
                tvRecvLen?.text = "接收:$iRecvLength"
            }
            R.id.btnSend -> {
                val strSend = edData?.text.toString()
                if (strSend.equals("")) return
                startSendData(strSend)
            }
            R.id.btnChangeServer -> {
                var areaSelectServerWindow = AreaSelectServerWindow(mContext,R.style.dialog,selectServerListener)
                areaSelectServerWindow.setServerList(serverList)
                areaSelectServerWindow.setReadCharaMap(readCharaMap)
                areaSelectServerWindow.setWriteCharaMap(writeCharaMap)
                areaSelectServerWindow.setSelectServer(selectServer,selectRead,selectWrite)
                areaSelectServerWindow.show()
            }

        }
    }

    /** 选择服务的回调 */
    private val selectServerListener = object : AreaSelectServerWindow.PeriodListener{
        override fun refreshListener(selectS: UUIDInfo, selectR: UUIDInfo, selectW: UUIDInfo) {
            selectServer = selectS
            selectRead = selectR
            selectWrite = selectW
            bindServerSubNofify()
        }
        override fun clearListener() {
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        startGetServerTimer(false)
        SendTimer(false,0,0)
        DeviceScanActivity.getInstance().mBLE.disconnect()
    }


}