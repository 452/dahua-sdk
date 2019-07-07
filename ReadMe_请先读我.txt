
【总述】
1. Java库路径： src\main\java\com\netsdk\lib
			    NetSDKLib.java  Java库
				ToolKits.java   封装小工具文件
				Utils.java      判断运行平台，指定加载库的路径文件
				
				SDK所有回调都是子线程，不要在回调里刷新UI以及做一些耗时操作。建议回调函数对象写成全局静态，防止被系统回收。
				
2. 动态库路径：
				libs\win32    win32的动态库
				libs\win64    win64的动态库
				
				run_win32.bat  win32运行脚本，运行的jar包，对应jdk 1.6，此脚本指定了jdk
				run_win64.bat  win64运行脚本，运行的jar包，对应jdk 1.8，此脚本指定了jdk
				
				WEB开发，实时预览，找设备的同事要插件，sdk不提供，sdk提供的拉流，用于客户端开发。	
				
3. 中英文配置文件：res\
			   
4. demo路径： 
	考勤机：src\main\java\com\netsdk\demo\frame\Attendance
	
	主动注册：src\main\java\com\netsdk\demo\frame\AutoRegister
	
	人脸识别：src\main\java\com\netsdk\demo\frame\FaceRecognition

	人脸闸机：src\main\java\com\netsdk\demo\frame\Gate

	报警监听：src\main\java\com\netsdk\demo\frame\AlarmListen.java

	抓图：src\main\java\com\netsdk\demo\frame\CapturePicture.java

	设备控制：src\main\java\com\netsdk\demo\frame\DeviceControl.java

	设备搜索和初始化：src\main\java\com\netsdk\demo\frame\DeviceSearchAndInit.java

	下载录像：src\main\java\com\netsdk\demo\frame\DownLoadRecord.java

	云台：src\main\java\com\netsdk\demo\frame\PTZControl.java

	实时预览：src\main\java\com\netsdk\demo\frame\RealPlay.java

	对讲：src\main\java\com\netsdk\demo\frame\Talk.java

	智能交通：src\main\java\com\netsdk\demo\frame\TrafficEvent.java

	热成像：src\main\java\com\netsdk\demo\frame\ThermalCamera
			   

	以上是功能的界面实现，接口实现路径：src\main\java\com\netsdk\demo\module\
			
5. 运行jar包：target\ 
	
6. 开发工具Ecplise

7. 错误码对应文件：src\main\java\com\netsdk\common\ErrorCode.java
					 
8. package.bat  pom.xml 打包脚本

9. jna.jar 的版本3.4.0


///////////////////////////////////////////////////////////////////
【设备搜索和初始化】
1. 功能概要：
   设备搜索和设备初始化功能。
   
2. Demo中涉及到的NetSDK接口如下：
   1）设备组播和广播搜索
	  开始搜索接口：CLIENT_StartSearchDevices
	  停止搜索接口：CLIENT_StopSearchDevices
   2）设备IP单播搜索
      开始搜索接口：CLIENT_SearchDevicesByIPs   
	  停止搜索接口：CLIENT_StopSearchDevices
	  
3. 注意事项：
   CLIENT_SearchDevicesByIPs 接口每次最多搜索256个
   
4. 相关接口：
   CLIENT_Init      初始化NetSDK
   CLIENT_Cleanup   释放NetSDK缓存
   CLIENT_LogOpen   打开日志
   CLIENT_LogClose  关闭日志
   fSearchDevicesCB 设备搜索回调
   

///////////////////////////////////////////////////////////////////   
【主动注册】
1. 功能概要：
   主动注册主要用于批量登录设备。
   此Demo里集合了 实时预览，对讲、抓图功能。
   
2. Demo中涉及到的NetSDK接口如下：
   1）主动注册功能
	  开启服务接口：CLIENT_ListenServer
	  停止服务接口：CLIENT_StopListenServer
   2）主动注册登录/登出功能
      登录接口：CLIENT_LoginEx2   
	  登出接口：CLIENT_Logout
   3）实时预览功能
	  开始预览接口：CLIENT_RealPlayEx
	  停止预览接口：CLIENT_StopRealPlayEx
   4）抓图功能
      设置抓图回调接口：CLIENT_SetSnapRevCallBack
      远程抓图接口：CLIENT_SnapPictureEx
   5）对讲功能
      设置模式接口：CLIENT_SetDeviceMode
	  开始对讲接口：CLIENT_StartTalkEx
	  停止对讲接口：CLIENT_StopTalkEx
	  PC端录音接口：CLIENT_RecordStart
	  结束PC端录音接口：CLIENT_RecordStop
	  将收到的本地PC端检测到的声卡数据发送给设备端接口：CLIENT_TalkSendData
	  将收到的设备端发送过来的语音数据传给SDK解码播放接口：CLIENT_AudioDecEx
   6）配置设备主动注册信息
      [1]IP登录/登出设备功能
	     登录接口：CLIENT_LoginEx2   
	     登出接口：CLIENT_Logout
	  [2]注册信息配置功能
	     获取设备的主动注册信息：CLIENT_GetNewDevConfig  CLIENT_ParseData   对应命令：NetSDKLib.CFG_CMD_DVRIP
		 设置设备的主动注册信息：CLIENT_PacketData  CLIENT_SetNewDevConfig  对应命令：NetSDKLib.CFG_CMD_DVRIP
	  
3. 注意事项：
   1）在通过开启服务收到设备上报的设备信息后，需要登录设备。   
      此处的主动注册登录设备跟通用的IP登录设备的区别是：入参需要填设备ID、登录方式tcpSpecCap不同
   2）配置设备的主动注册信息可以通过本Demo设置，也可以通过Web设置。
   
4. 相关接口：
   CLIENT_Init      		初始化NetSDK，并且设置断线回调fDisConnect，当设备断线后，回调里会收到信息
   CLIENT_Cleanup   		释放NetSDK缓存
   CLIENT_LogOpen   		打开日志
   CLIENT_LogClose  		关闭日志
   CLIENT_SetAutoReconnect  设置重连回调fHaveReConnect，当设备断线后，会向设备发送心跳包，自动连接设备，当重连成功后，回调里会收到信息
   CLIENT_SetNetworkParam   设置登录网络环境
   fServiceCallBack         启动服务的回调，用于接收设备注册上来的设备信息
   CLIENT_LoginEx2		    登录   
   CLIENT_Logout		    登出
   
   
///////////////////////////////////////////////////////////////////
【人脸识别】
1. 功能概要：
   人脸设备功能，主要用于IVSS、IPC-FR、IPC-FD(只支持人脸检测事件)
   包含功能：1）人脸库的增、删、改查
             2）人员的增、删、改、查
			 3）按人脸库布控、撤控
			 4）人脸识别和人脸检测事件
			 5）实时预览
   
2. Demo中涉及到的NetSDK接口如下：
   1）人脸库功能
	  查询人脸库接口：CLIENT_FindGroupInfo
	  添加人脸库接口：CLIENT_OperateFaceRecognitionGroup  对应枚举  EM_OPERATE_FACERECONGNITION_GROUP_TYPE.NET_FACERECONGNITION_GROUP_ADD
	  修改人脸库接口：CLIENT_OperateFaceRecognitionGroup  对应枚举  EM_OPERATE_FACERECONGNITION_GROUP_TYPE.NET_FACERECONGNITION_GROUP_MODIFY
	  删除人脸库接口：CLIENT_OperateFaceRecognitionGroup  对应枚举  EM_OPERATE_FACERECONGNITION_GROUP_TYPE.NET_FACERECONGNITION_GROUP_DELETE
   2）人员功能
      查询人员信息接口：CLIENT_StartFindFaceRecognition  CLIENT_DoFindFaceRecognition  CLIENT_StopFindFaceRecognition
	  添加人员信息接口：CLIENT_OperateFaceRecognitionDB  对应枚举 EM_OPERATE_FACERECONGNITIONDB_TYPE.NET_FACERECONGNITIONDB_ADD
	  修改人员信息接口：CLIENT_OperateFaceRecognitionDB  对应枚举 EM_OPERATE_FACERECONGNITIONDB_TYPE.NET_FACERECONGNITIONDB_MODIFY
      删除人员信息接口：CLIENT_OperateFaceRecognitionDB  对应枚举 EM_OPERATE_FACERECONGNITIONDB_TYPE.NET_FACERECONGNITIONDB_DELETE
	  
	  查询到的人员图片是一个地址，可以用下载接口下载：CLIENT_DownloadRemoteFile
   3）布控和撤控功能
      按人脸库布控接口：CLIENT_FaceRecognitionPutDisposition
      按人脸库撤控接口：CLIENT_FaceRecognitionDelDisposition
   4）人脸识别和人脸检测事件功能
      智能订阅接口：CLIENT_RealLoadPictureEx
	  停止订阅接口：CLIENT_StopLoadPic
	  
	  人脸识别事件：NetSDKLib.EVENT_IVS_FACERECOGNITION
	  人脸检测事件：NetSDKLib.EVENT_IVS_FACEDETECT
   5）实时预览功能
      显示规则框接口：CLIENT_RenderPrivateData
	  开始预览接口：CLIENT_RealPlayEx
	  停止预览接口：CLIENT_StopRealPlayEx
	  
3. 注意事项：
   1）人脸图片只支持JPG格式
      IPC支持的图片最大为128KB
	  IVSS支持的图片最大为256KB
   2）IPC-FD不支持人脸识别事件，只支持人脸检测事件
   3）IPC最大支持的人脸库个数为5
      IVSS最大支持的人脸库个数为20
   4）人员信息的查询
      [1]先调用CLIENT_StartFindFaceRecognition获取查询句柄
	  [2]在[1]的基础上，根据偏移量循环查询， 每次查询的最大个数为20
	  [3]调用CLIENT_StopFindFaceRecognition关闭查询
   
4. 相关接口：
   CLIENT_Init      		初始化NetSDK，并且设置断线回调fDisConnect，当设备断线后，回调里会收到信息
   CLIENT_Cleanup   		释放NetSDK缓存
   CLIENT_LogOpen   		打开日志
   CLIENT_LogClose  		关闭日志
   CLIENT_SetAutoReconnect  设置重连回调fHaveReConnect，当设备断线后，会向设备发送心跳包，自动连接设备，当重连成功后，回调里会收到信息
   CLIENT_SetNetworkParam   设置登录网络环境
   fAnalyzerDataCallBack    订阅回调，接收人脸识别和人脸检测事件
   CLIENT_LoginEx2		    登录   
   CLIENT_Logout		    登出


///////////////////////////////////////////////////////////////////
【闸机】
1. 功能概要：
   主要功能有：门禁事件
			   卡的增、删、改、查、清空
			   人脸的增、删、改、清空(人脸查询到的是MD5，不是图片地址和图片信息，所以人脸查询的实现没意义，不提供)
   
2. Demo中涉及到的NetSDK接口如下：
   1）门禁事件
	  开始订阅接口：CLIENT_RealLoadPictureEx
	  取消订阅接口：CLIENT_StopLoadPic
	  
	  门禁事件：NetSDKLib.EVENT_IVS_ACCESS_CTL
   2）卡和人脸操作
      添加卡信息：CLIENT_ControlDevice   接口里的对应命令：CtrlType.CTRLTYPE_CTRL_RECORDSET_INSERT   入参里的记录集类型：EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD
	  修改卡信息：CLIENT_ControlDevice   接口里的对应命令：CtrlType.CTRLTYPE_CTRL_RECORDSET_UPDATE   入参里的记录集类型：EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD
	  删除卡信息：CLIENT_ControlDevice   接口里的对应命令：CtrlType.CTRLTYPE_CTRL_RECORDSET_REMOVE   入参里的记录集类型：EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD
	  清空卡信息：CLIENT_ControlDevice   接口里的对应命令：CtrlType.CTRLTYPE_CTRL_RECORDSET_CLEAR   入参里的记录集类型：EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD
	  查询卡信息：[1]调用CLIENT_FindRecord获取查询句柄  [2]调用CLIENT_FindNextRecord循环查询   [3]查询结束，调用CLIENT_FindRecordClose关闭查询
	  
	  添加人脸：CLIENT_FaceInfoOpreate 接口里的对应命令：EM_FACEINFO_OPREATE_TYPE.EM_FACEINFO_OPREATE_ADD
	  修改人脸：CLIENT_FaceInfoOpreate 接口里的对应命令：EM_FACEINFO_OPREATE_TYPE.EM_FACEINFO_OPREATE_UPDATE
	  删除人脸：CLIENT_FaceInfoOpreate 接口里的对应命令：EM_FACEINFO_OPREATE_TYPE.EM_FACEINFO_OPREATE_REMOVE
	  清空人脸：CLIENT_FaceInfoOpreate 接口里的对应命令：EM_FACEINFO_OPREATE_TYPE.EM_FACEINFO_OPREATE_CLEAR
	  
3. 注意事项：
   查询卡信息是根据每次查询的个数循环查询(不像人脸识别里，是根据偏移量)
   
4. 相关接口：
   CLIENT_Init      	  初始化NetSDK
   CLIENT_Cleanup   	  释放NetSDK缓存
   CLIENT_LogOpen   	  打开日志
   CLIENT_LogClose  	  关闭日志
   fAnalyzerDataCallBack  订阅回调，接收门禁事件
   CLIENT_LoginEx2		  登录   
   CLIENT_Logout		  登出


///////////////////////////////////////////////////////////////////
【双通道预览】
1. 功能概要：
   主要功能有：双通道实时预览
   
2. Demo中涉及到的NetSDK接口如下：
   1）实时预览功能
      开始预览接口：CLIENT_RealPlayEx
      停止预览接口：CLIENT_StopRealPlayEx
	  
3. 注意事项：
   无
   
4. 相关接口：
   CLIENT_Init      	  初始化NetSDK
   CLIENT_Cleanup   	  释放NetSDK缓存
   CLIENT_LogOpen   	  打开日志
   CLIENT_LogClose  	  关闭日志
   CLIENT_LoginEx2		  登录   
   CLIENT_Logout		  登出


///////////////////////////////////////////////////////////////////
【云台控制】
1. 功能概要：
   主要功能有：实时预览、远程抓图、八个方向控制、变倍、变焦、光圈
   
2. Demo中涉及到的NetSDK接口如下：
   1）实时预览功能
      开始预览接口：CLIENT_RealPlayEx
      停止预览接口：CLIENT_StopRealPlayEx
   2）远程抓图
      设置抓图回调接口：CLIENT_SetSnapRevCallBack
	  下发抓图命令接口：CLIENT_SnapPictureEx
   3）八个方向控制、变倍、变焦、光圈
      CLIENT_DHPTZControlEx  接口一样，对应的命令不一样，具体参考Demo，Demo里写的很详细
	  
3. 注意事项：
   无
   
4. 相关接口：
   CLIENT_Init      	  初始化NetSDK
   CLIENT_Cleanup   	  释放NetSDK缓存
   CLIENT_LogOpen   	  打开日志
   CLIENT_LogClose  	  关闭日志
   fSnapRev               抓图回调，用于接收图片信息
   CLIENT_LoginEx2		  登录   
   CLIENT_Logout		  登出


///////////////////////////////////////////////////////////////////
【下载录像】
1. 功能概要：
   主要功能有：按时间下载录像、按文件下载录像、设置下载录像的码流类型、查询录像		   
   
2. Demo中涉及到的NetSDK接口如下：
   1）设置下载录像的码流类型
      接口：CLIENT_SetDeviceMode  对应命令：EM_USEDEV_MODE.NET_RECORD_STREAM_TYPE
   2）按时间下载
      开始下载接口：CLIENT_DownloadByTimeEx
      停止下载接口：CLIENT_StopDownload
   3）按文件下载
      查询录像接口：CLIENT_QueryRecordFile
	  开始下载接口：CLIENT_DownloadByTimeEx
      停止下载接口：CLIENT_StopDownload
	  
3. 注意事项：
   1）下载录像格式为dav
   2）按文件下载，是先查询录像文件，再调用按时间下载接口下载录像
   
4. 相关接口：
   CLIENT_Init      	     初始化NetSDK
   CLIENT_Cleanup   	     释放NetSDK缓存
   CLIENT_LogOpen   	     打开日志
   CLIENT_LogClose  	     关闭日志
   fTimeDownLoadPosCallBack  下载回调，用于接收下载录像的进度
   CLIENT_LoginEx2		     登录   
   CLIENT_Logout		     登出 


///////////////////////////////////////////////////////////////////
【智能交通】
1. 功能概要：
   主要功能有：实时预览、手动抓拍、出入口开闸、智能订阅交通事件
   
2. Demo中涉及到的NetSDK接口如下：
   1）实时预览功能
      开始预览接口：CLIENT_RealPlayEx
      停止预览接口：CLIENT_StopRealPlayEx
   2）手动抓拍
      CLIENT_ControlDeviceEx  对应命令：CtrlType.CTRLTYPE_MANUAL_SNAP
   3）出入口开闸
      CLIENT_ControlDeviceEx  对应命令：CtrlType.CTRLTYPE_CTRL_OPEN_STROBE
   4）订阅交通事件
	  开始订阅接口：CLIENT_RealLoadPictureEx
	  停止订阅接口：CLIENT_StopLoadPic
	  
3. 注意事项：
   调用手动抓拍接口，会触发手动抓拍事件
   
4. 相关接口：
   CLIENT_Init      	  初始化NetSDK
   CLIENT_Cleanup   	  释放NetSDK缓存
   CLIENT_LogOpen   	  打开日志
   CLIENT_LogClose  	  关闭日志
   fAnalyzerDataCallBack  订阅回调，用于接收智能交通的各种事件
   CLIENT_LoginEx2		  登录   
   CLIENT_Logout		  登出


///////////////////////////////////////////////////////////////////   
【语音对讲功能】
1.  功能概要：
	主要功能有：直连对讲、转发模式对讲

2.  Demo中涉及到的NetSDK接口如下：
	1） 设置设备对讲模式：CLIENT_SetDeviceMode
			对讲前需要设置若干对讲方式：
				设置语音对讲编码格式对应命令：NetSDKLib.EM_USEDEV_MODE.NET_TALK_ENCODE_TYPE
				设置语音对讲喊话参数对应命令：NetSDKLib.EM_USEDEV_MODE.NET_TALK_SPEAK_PARAM
				设置对讲是否为转发模式对应命令：NetSDKLib.EM_USEDEV_MODE.NET_TALK_TRANSFER_MODE
				转发模式时设置转发通道对应命令：NetSDKLib.EM_USEDEV_MODE.NET_TALK_TALK_CHANNEL
	2） 向设备发送用户的音频数据：CLIENT_TalkSendData
	3） 对设备发来的音频数据解码：CLIENT_AudioDecEx	
	4） 向设备发起语音对讲请求：CLIENT_StartTalkEx
			对讲回调实现pfAudioDataCallBack接口
				回调中byAudioFlag为0调CLIENT_TalkSendData，为1调CLIENT_AudioDecEx处理音频数据
    5） 开始PC端录音：CLIENT_RecordStart
    6） 结束PC端录音：CLIENT_RecordStop
    7） 停止语音对讲：CLIENT_StopTalkEx

3.  注意事项:
	SDK所有回调都是子线程，JNA是弱引用，建议回调函数对象写成全局静态，防止被系统回收

4.  相关接口：
	CLIENT_Init      		初始化NetSDK，并且设置断线回调fDisConnect，当设备断线后，回调里会收到信息
	CLIENT_SetNetworkParam  设置登录网络环境
	CLIENT_Cleanup   		释放NetSDK缓存
	CLIENT_LogOpen   		打开日志
	CLIENT_LogClose  		关闭日志
	CLIENT_LoginEx2			登录   
	CLIENT_Logout			登出


///////////////////////////////////////////////////////////////////   
【设备控制功能】
1.  功能概要：
	主要功能有：设置重启、获取设备当前时间、设置设备时间

2.  Demo中涉及到的NetSDK接口如下：
    1） 设备控制：CLIENT_ControlDevice
			控制类型（参数emType）为CtrlType.CTRLTYPE_CTRL_REBOOT时下发重启设备命令
    2） 设置设备当前时间：CLIENT_SetupDeviceTime
    3） 查询设备当前时间：CLIENT_QueryDeviceTime
		
3.  注意事项:
	无

4.  相关接口：
	CLIENT_Init      		初始化NetSDK，并且设置断线回调fDisConnect，当设备断线后，回调里会收到信息
	CLIENT_SetNetworkParam  设置登录网络环境
	CLIENT_Cleanup   		释放NetSDK缓存
	CLIENT_LogOpen   		打开日志
	CLIENT_LogClose  		关闭日志
	CLIENT_LoginEx2			登录   
	CLIENT_Logout			登出


///////////////////////////////////////////////////////////////////  
【抓图功能】
1.  功能概要：
	主要功能有：实时预览、抓图（包含本地抓图、远程抓图和定时抓图）

2.  Demo中涉及到的NetSDK接口如下：
	1） 实时预览功能
			开始预览接口：CLIENT_RealPlayEx
			停止预览接口：CLIENT_StopRealPlayEx
    2） 抓图功能
			本地抓图：CLIENT_CapturePictureEx
			设置抓图回调：CLIENT_SetSnapRevCallBack
				回调实现fSnapRev接口
			远程抓图、定时抓图、停止定时抓图：CLIENT_SnapPictureEx
				抓图模式：-1:表示停止抓图, 0：表示请求一帧（远程抓图）, 1：表示定时发送请求

3.  注意事项:
	本地抓图需要先实时预览
	SDK所有回调都是子线程，JNA是弱引用，建议回调函数对象写成全局静态，防止被系统回收

4.  相关接口：
	CLIENT_Init      		初始化NetSDK，并且设置断线回调fDisConnect，当设备断线后，回调里会收到信息
	CLIENT_SetNetworkParam  设置登录网络环境
	CLIENT_SetAutoReconnect 设置重连回调fHaveReConnect，当设备断线后，会向设备发送心跳包，自动连接设备，当重连成功后，回调里会收到信息
	CLIENT_Cleanup   		释放NetSDK缓存
	CLIENT_LogOpen   		打开日志
	CLIENT_LogClose  		关闭日志
	CLIENT_LoginEx2			登录   
	CLIENT_Logout			登出


///////////////////////////////////////////////////////////////////
【报警监听功能】
1.  功能概要：
	主要功能有：外部报警、移动侦测、视频遮挡、视频丢失、硬盘满、硬盘坏的报警上报
2.  Demo中涉及到的NetSDK接口如下：
	1） 设置报警回调：CLIENT_SetDVRMessCallBack
			回调实现fMessCallBack接口
    2） 向设备订阅报警：CLIENT_StartListenEx
	3） 停止订阅报警：CLIENT_StopListen	

3.  注意事项:
	SDK所有回调都是子线程，JNA是弱引用，建议回调函数对象写成全局静态，防止被系统回收

4.  相关接口：
	CLIENT_Init      		初始化NetSDK，并且设置断线回调fDisConnect，当设备断线后，回调里会收到信息
	CLIENT_SetNetworkParam  设置登录网络环境
	CLIENT_SetAutoReconnect 设置重连回调fHaveReConnect，当设备断线后，会向设备发送心跳包，自动连接设备，当重连成功后，回调里会收到信息
	CLIENT_Cleanup   		释放NetSDK缓存
	CLIENT_LogOpen   		打开日志
	CLIENT_LogClose  		关闭日志
	CLIENT_LoginEx2			登录   
	CLIENT_Logout			登出


///////////////////////////////////////////////////////////////////
【考勤机功能】
1.  功能概要：
	主要功能有：门禁事件信息展示
				人员记录集的新增/删除/更新/获取/查询
				通过用户ID对指纹记录集的新增/删除/获取
				通过指纹ID指纹记录集获取/删除
				指纹采集功能

2.  Demo中涉及到的NetSDK接口如下：
    1） 实时上传智能分析数据：CLIENT_RealLoadPictureEx
			回调实现fAnalyzerDataCallBack接口
    2） 停止上传智能分析数据：CLIENT_StopLoadPic
	3） 考勤新增加用户：CLIENT_Attendance_AddUser
	4） 考勤获取单个用户信息：CLIENT_Attendance_GetUser	
	5） 考勤修改用户信息：CLIENT_Attendance_ModifyUser	
	6） 考勤删除用户：CLIENT_Attendance_DelUser	
	7） 检索考勤用户信息：CLIENT_Attendance_FindUser	
	8） 通过用户ID插入指纹数据：CLIENT_Attendance_InsertFingerByUserID
	9） 通过用户ID查找该用户下的所有指纹数据：CLIENT_Attendance_GetFingerByUserID
	10）删除单个用户下所有指纹数据：CLIENT_Attendance_RemoveFingerByUserID
	11）通过指纹ID获取指纹数据：CLIENT_Attendance_GetFingerRecord
	12）通过指纹ID删除指纹数据：CLIENT_Attendance_RemoveFingerRecord
	13）设备控制：CLIENT_ControlDeviceEx
			控制类型（参数emType）为CtrlType.CTRLTYPE_CTRL_CAPTURE_FINGER_PRINT时下发指纹采集命令

3.  注意事项:
	SDK所有回调都是子线程，JNA是弱引用，建议回调函数对象写成全局静态，防止被系统回收

4.  相关接口：
	CLIENT_Init      		初始化NetSDK，并且设置断线回调fDisConnect，当设备断线后，回调里会收到信息
	CLIENT_SetNetworkParam  设置登录网络环境
	CLIENT_Cleanup   		释放NetSDK缓存
	CLIENT_LogOpen   		打开日志
	CLIENT_LogClose  		关闭日志
	CLIENT_LoginEx2			登录   
	CLIENT_Logout			登出


///////////////////////////////////////////////////////////////////	
【热成像功能】
1.  功能概要：
	主要功能有：测温点参数查询、测温项参数查询、热成像温度查询、温度分布数据获取和保存

2. Demo中涉及到的NetSDK接口如下：
	1)  查询测温点的参数值: CLIENT_QueryDevInfo 
			对应枚举：NetSDKLib.EM_QUERY_DEV_INFO.RADIOMETRY_POINT_TEMPER
	2)  查询测温项的参数值: CLIENT_QueryDevInfo
			对应枚举：NetSDKLib.EM_QUERY_DEV_INFO.RADIOMETRY_TEMPER
	3)  查询热成像温度（枚举值为NetSDKLib.EM_FIND.RADIOMETRY）:
			开始查询：CLIENT_StartFind
			继续查询：CLIENT_DoFind
			结束查询：CLIENT_StopFind
	4)  温度分布数据获取
			订阅：CLIENT_RadiometryAttach
			通知开始获取热图数据：CLIENT_RadiometryFetch
			热图数据解压与转换：CLIENT_RadiometryDataParse
			取消订阅：CLIENT_RadiometryDetach
	
3.  注意事项:
	SDK所有回调都是子线程，JNA是弱引用，建议回调函数对象写成全局静态，防止被系统回收

4.  相关接口：
	CLIENT_Init      		初始化NetSDK，并且设置断线回调fDisConnect，当设备断线后，回调里会收到信息
	CLIENT_SetNetworkParam  设置登录网络环境
	CLIENT_SetAutoReconnect 设置重连回调fHaveReConnect，当设备断线后，会向设备发送心跳包，自动连接设备，当重连成功后，回调里会收到信息
	CLIENT_Cleanup   		释放NetSDK缓存
	CLIENT_LogOpen   		打开日志
	CLIENT_LogClose  		关闭日志
	CLIENT_LoginEx2			登录   
	CLIENT_Logout			登出

	

	