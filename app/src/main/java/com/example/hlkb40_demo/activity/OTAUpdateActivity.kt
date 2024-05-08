package com.example.hlkb40_demo.activity

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothGattDescriptor
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.example.hlkb40_demo.*
import com.example.hlkb40_demo.utilInfo.GetOTAAddrTask
import com.example.hlkb40_demo.utilInfo.SendOTAFileTask
import com.example.hlkb40_demo.utilInfo.Utils

import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*

class OTAUpdateActivity : BaseActivity() {

    private lateinit var precenttv: TextView
    private lateinit var mDialog : Dialog
    private var getOTAAddrTask: GetOTAAddrTask? = null
    private var sendOTAFileTask: SendOTAFileTask? = null
    private var isGetOTAAddr = false
    private var isSendOTAFile = false
    private var selectFile: File? = null
    private var everyPackageSize = 0
    private var fileLength = 0L
    private lateinit var woperation : WriterOperation
    private lateinit var selectServer : UUIDInfo
    private lateinit var selectWrite : UUIDInfo
    private lateinit var selectRead : UUIDInfo
    var isConnected = true

    private var imgBack:ImageView? = null
    private var tvFilePath:TextView? = null
    private var tvName:TextView? = null
    private var tvLog:TextView? = null
    private var rlTitle:RelativeLayout? = null
    private var btnUpdate:Button? = null
    private var scrollView:ScrollView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ota_update)
        imgBack = findViewById(R.id.imgBack)
        tvFilePath = findViewById(R.id.tvFilePath)
        tvName = findViewById(R.id.tvName)
        tvLog = findViewById(R.id.tvLog)
        rlTitle = findViewById(R.id.rlTitle)
        btnUpdate = findViewById(R.id.btnUpdate)
        scrollView = findViewById(R.id.scrollView)


        imgBack?.setOnClickListener { onBackPressed() }

        initUI()
        verifyStoragePermissions()
        bindReceiver()

        bindServerSubNofify()


    }

    private fun bindReceiver() {
        var intentFilter = IntentFilter()
        intentFilter.addAction(BC_ReadData)
        intentFilter.addAction(BC_WriteData)
        intentFilter.addAction(BC_RecvData)
        intentFilter.addAction(BC_ChangeMTU)
        intentFilter.addAction(BC_ConnectStatus)
        // 注册广播
        registerReceiver(mBroadcastReceiver, intentFilter)
    }



    private fun initUI() {

        val mAdapterManager = AdapterManager(this)
        BluetoothApplication.getInstance().adapterManager = mAdapterManager
        verifyStoragePermissions()
        woperation = WriterOperation()

        val layoutinflater = LayoutInflater.from(this)
        val view = layoutinflater.inflate(R.layout.loading_process_dialog_anim, null)
        precenttv = view.findViewById<View>(R.id.precenttv) as TextView
        mDialog = Dialog(this, R.style.dialog)
        mDialog.setCancelable(false)
        mDialog.setContentView(view)

        imgBack?.setOnClickListener { finish() }
        tvFilePath?.setOnClickListener{
            var strFilePath = tvFilePath?.text.toString()
            if (strFilePath.equals("请选择 bin 文件")) strFilePath = ""
            val intent = Intent(mContext, SelectFileActivity::class.java)
            intent.putExtra("filepatch", strFilePath)
            startActivityForResult(intent, SelectFileActivity.RESULT_CODE)
        }
        btnUpdate?.setOnClickListener{
            val strFilePath = tvFilePath?.text.toString()
            startUpdateBT(strFilePath)
        }
    }

    private fun bindServerSubNofify() {

        for (iPosition in 0 until TRXActivity.getInstance().serverList.size) {
            val serverUUID = TRXActivity.getInstance().serverList[iPosition]
            if (serverUUID.uuidString.equals(strOTA_Server,true)) {
                selectServer = serverUUID
                val readArray = TRXActivity.getInstance().readCharaMap[selectServer.uuidString]
                for (iR in 0 until readArray!!.size) {
                    if (readArray[iR].uuidString.equals(strOTA_Read,true)) {
                        selectRead = readArray[iR]
                        break
                    }
                }
                val writeArray = TRXActivity.getInstance().writeCharaMap[selectServer.uuidString]
                for (iW in 0 until writeArray!!.size) {
                    if (writeArray[iW].uuidString.equals(strOTA_Write,true)) {
                        selectWrite = writeArray[iW]
                        break
                    }
                }
                break
            }
        }

        if (!this::selectServer.isInitialized || selectServer == null ||
                !this::selectRead.isInitialized || selectRead == null ||
                !this::selectWrite.isInitialized || selectWrite == null) {
            showToast("未找到指定升级通道！")
            btnUpdate?.isEnabled = false
            return
        }


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

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)

    fun verifyStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "存储权限已打开", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "需要打开存储权限才可以OTA", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SelectFileActivity.RESULT_CODE) {
            // 请求为 "选择文件"
            try {
                // 取得选择的文件名
                val sendFileName = data?.getStringExtra(SelectFileActivity.SEND_FILE_NAME)
                if (sendFileName != null && !sendFileName.equals(""))
                    tvFilePath?.text = sendFileName
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "onActivityResult: " + "回调异常！")
                tvFilePath?.text = "文件异常：${e.message}"
            }
        }
    }



    val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val strAction = intent?.action
            // 主动读通道的回调
            if (strAction.equals(BC_ReadData)) {
                val uuid = intent?.getSerializableExtra("UUID") as UUID
                val iStatus = intent.getIntExtra("status", -1)
                val dataValue = intent.getByteArrayExtra("data")
                if (uuid.toString().equals(selectRead?.uuidString, true)) {

                }

            }
            // 写通道的回调
            else if (strAction.equals(BC_WriteData)) {
                val uuid = intent?.getSerializableExtra("UUID") as UUID
                val iStatus = intent?.getIntExtra("status", -1)
                if (uuid.toString().equals(selectWrite?.uuidString, true)) {

                }

            }
            // 由订阅的通道数据回调
            else if (strAction.equals(BC_RecvData)) {
                val uuid = intent?.getSerializableExtra("UUID") as UUID
                val dataValue = intent.getByteArrayExtra("data")
                if (uuid.toString().equals(selectRead?.uuidString, true)) {
                    if (dataValue != null) {
                        if (dataValue.isEmpty()) return
                    }
                    // 正在获取位置信息
                    if (isGetOTAAddr) {
                        getOTAAddrTask?.recvValue = dataValue
                        getOTAAddrTask?.isResultCallBack = true
                        return
                    }
                    // 正在发文件
                    if (isSendOTAFile) {
                        sendOTAFileTask?.recvValue = dataValue
                        sendOTAFileTask?.isResultCallBack = true
                        return
                    }
                }

            }
            // MTU改变的回调
            else if (strAction.equals(BC_ChangeMTU)) {
                val strMsg = intent?.getStringExtra("strMsg")
                val iMTU = intent?.getIntExtra("iMTU",0)!!
                updateLog(strMsg!!)
                Log.e("onChangeMTUListener", "MTU设置结果：$strMsg")
                // 正在升级
                if (mDialog.isShowing) {
                    everyPackageSize = iMTU - 3 - 9
                    // 启动关于地址的线程(activity,发送帮助类，写事件，文件长度)
                    isGetOTAAddr = true
                    getOTAAddrTask = GetOTAAddrTask()
                    getOTAAddrTask?.execute(handler, woperation, selectWrite, fileLength)
                }

            }
            // 连接状态变化的回调
            else if (strAction.equals(BC_ConnectStatus)) {
                val isConnectState = intent?.getBooleanExtra("isConnectState",false)!!
                val strConnectState = intent?.getStringExtra("strConnectState")!!
                isConnected = isConnectState
                updateLog(strConnectState)
                // 连接已断开
                if (!isConnectState) {
                    // 因为固件已升级，并重启设备，则自动返回列表界面
                    if (loadingDialog.isShowing) {
                        DeviceScanActivity.getInstance().mBLE.setCharacteristicNotification(selectRead.bluetoothGattCharacteristic, false)
                        DeviceScanActivity.getInstance().mBLE.disconnect()
                        unregisterReceiver(this)
                        val intent0 = Intent(mContext, DeviceScanActivity::class.java)
                        intent0.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent0)
                        finish()
                    }
                }
            }
        }

    }

    /** 开始升级
     *  1,设置MTU
     *  2，查询存储地址
     *  3，擦除升级文件大小相应的空间
     *  4，发送文件
     *  5，发送完毕，并发重启
     *  6，自动断开连接
     * */
    private fun startUpdateBT(strFilePath: String) {
        if (strFilePath.equals("请选择 bin 文件")) return
        // 校验文件
        if (!checkFileOK(strFilePath.trim())) return
        selectFile = File(strFilePath.trim())
        fileLength = selectFile!!.length()
        updateLog("大小：${Utils.formatFileSize(fileLength)}")
        tvLog?.text = ""
        updateLog("!!!!!!!!!!开始升级!!!!!!!!!!\n文件：$strFilePath")
        mDialog.show()
        Log.e("doSendFileByBluetooth", "设置MTU:512")
        DeviceScanActivity.getInstance().mBLE.requestMtu(512)
    }

    private fun checkFileOK(strUrl : String) : Boolean {
        val checkFile = File(strUrl)
        val checkFileMaxLength = checkFile.length()
        val infile = FileInputStream(checkFile)
        var Buffer = ByteArray(0x5c)
        infile.read(Buffer, 0, Buffer.size)
        if (Buffer[0x58].toInt() != 0x51 || Buffer[0x59].toInt() != 0x52 || Buffer[0x5a].toInt() != 0x52 || Buffer[0x5b].toInt() != 0x51) {
            Toast.makeText(mContext, "请选择正确的文件", Toast.LENGTH_LONG).show()
            infile.close()
            return false
        }
        else {
            infile.close()
            return true
        }
    }

    /** 开始发送文件 */
    private fun startSendFile(iStartAddr: Int) {
        isGetOTAAddr = false
        isSendOTAFile = true
        sendOTAFileTask = SendOTAFileTask()
        sendOTAFileTask?.execute(handler, woperation, selectWrite, selectFile,iStartAddr,everyPackageSize)
    }

    val iUpdateLog = 111
    val iUpdateStop = 222
    val iRecvLog = 333
    val iSendFile = 444
    val handler = object : Handler() {
        override fun dispatchMessage(msg: Message) {
            super.dispatchMessage(msg)
            when(msg.what) {
                iUpdateLog -> {
                    val writePrecent = (msg.obj).toString()
                    precenttv.text = writePrecent
                    updateLog(writePrecent)
                }
                iUpdateStop -> {
                    mDialog.cancel()
                    isGetOTAAddr = false
                    isSendOTAFile = false
                    getOTAAddrTask?.cancel(true)
                    sendOTAFileTask?.cancel(true)
                    val strMsg = (msg.obj).toString()
                    updateLog(strMsg)
                    loadingDialog.showAndMsg("正在重启...")
                }
                iRecvLog -> {
                    val strMsg = (msg.obj).toString()
                    updateLog(strMsg)
                }
                iSendFile -> {
                    val iStartAddr = msg.obj.toString().toInt()
                    startSendFile(iStartAddr)
                }
            }

        }

    }


    private fun updateLog(strValue: String) {
        val date = Date()
        val dateFormat = SimpleDateFormat("HH:mm:ss:SSS")
        tvLog?.text =  tvLog?.text.toString().plus("\n${dateFormat.format(date)}::::$strValue")
        scrollView?.fullScroll(ScrollView.FOCUS_DOWN)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        unregisterReceiver(mBroadcastReceiver)

    }




}