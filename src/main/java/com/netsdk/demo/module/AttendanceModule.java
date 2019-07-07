package main.java.com.netsdk.demo.module;

import java.io.UnsupportedEncodingException;

import com.sun.jna.Memory;

import main.java.com.netsdk.lib.NetSDKLib;
import main.java.com.netsdk.lib.ToolKits;
import main.java.com.netsdk.lib.NetSDKLib.*;

/**
 * \if ENGLISH_LANG
 * Attendance Interface
 * contains:smart subscribe、CRUD of user&&fingerprint and collection fingerprint 
 * \else
 * 考勤机接口实现
 * 包含: 智能订阅、考勤用户及指纹的增删改查、指纹采集
 * \endif
 */
public class AttendanceModule {
	
	public static final int TIME_OUT = 3000;
	public static final int nMaxFingerPrintSize = 2048;
	public static LLong m_hAttachHandle = new LLong(0);
	
	 /**
	 * 智能订阅
	 * @param callback   智能订阅回调函数
	 */
    public static boolean realLoadPicture(fAnalyzerDataCallBack callback) {

        int bNeedPicture = 0; // 不需要图片

        m_hAttachHandle =  LoginModule.netsdk.CLIENT_RealLoadPictureEx(LoginModule.m_hLoginHandle, -1,
        		NetSDKLib.EVENT_IVS_ALL, bNeedPicture, callback, null, null);

        if(m_hAttachHandle.longValue() == 0) {
        	System.err.printf("CLIENT_RealLoadPictureEx Failed!" + ToolKits.getErrorCodePrint());
        }
        
        return m_hAttachHandle.longValue() != 0;
    }

    /**
	 * 停止智能订阅
	 */
    public static void stopRealLoadPicture(){
        if (m_hAttachHandle.longValue() == 0) {
            return;
        }
        
        LoginModule.netsdk.CLIENT_StopLoadPic(m_hAttachHandle);
        m_hAttachHandle.setValue(0);
    }
	
	/**
	 * 考勤新增加用户
	 * @param userId   用户ID
	 * @param userName 用户名
	 * @param cardNo   卡号
	 */
	public static boolean addUser(String userId, String userName, String cardNo) {
		
		/*
		 * 入参
		 */
		NET_IN_ATTENDANCE_ADDUSER stuIn = new NET_IN_ATTENDANCE_ADDUSER();
		stringToByteArray(userId, stuIn.stuUserInfo.szUserID);
		stringToByteArray(userName, stuIn.stuUserInfo.szUserName);
		stringToByteArray(cardNo, stuIn.stuUserInfo.szCardNo);
		
		/*
		 * 出参
		 */
		NET_OUT_ATTENDANCE_ADDUSER stuOut = new NET_OUT_ATTENDANCE_ADDUSER();
		
		boolean bRet = LoginModule.netsdk.CLIENT_Attendance_AddUser(LoginModule.m_hLoginHandle, stuIn, stuOut, TIME_OUT);
		if (!bRet) {
			System.err.printf("CLIENT_Attendance_AddUser Failed!" + ToolKits.getErrorCodePrint());
		}
		
		return bRet;
	}
	
	/**
	 * 考勤删除用户
	 * @param userId   用户ID
	 */
	public static boolean deleteUser(String userId) {
		
		removeFingerByUserId(userId); 	// 先去删除指纹
		
		/*
		 * 入参
		 */
		NET_IN_ATTENDANCE_DELUSER stuIn = new NET_IN_ATTENDANCE_DELUSER();
		stringToByteArray(userId, stuIn.szUserID);
		
		/*
		 * 出参
		 */
		NET_OUT_ATTENDANCE_DELUSER stuOut = new NET_OUT_ATTENDANCE_DELUSER();
		
		boolean bRet = LoginModule.netsdk.CLIENT_Attendance_DelUser(LoginModule.m_hLoginHandle, stuIn, stuOut, TIME_OUT);
		if (!bRet) {
			System.err.printf("CLIENT_Attendance_DelUser Failed!" + ToolKits.getErrorCodePrint());
		}
		
		return bRet;
	}
	
	/**
	 * 考勤修改用户
	 * @param userId   用户ID
	 * @param userName 用户名
	 * @param cardNo   卡号
	 */
	public static boolean modifyUser(String userId, String userName, String cardNo) {
		
		/*
		 * 入参
		 */
		NET_IN_ATTENDANCE_ModifyUSER stuIn = new NET_IN_ATTENDANCE_ModifyUSER();
		stringToByteArray(userId, stuIn.stuUserInfo.szUserID);
		stringToByteArray(userName, stuIn.stuUserInfo.szUserName);
		stringToByteArray(cardNo, stuIn.stuUserInfo.szCardNo);
		
		/*
		 * 出参
		 */
		NET_OUT_ATTENDANCE_ModifyUSER stuOut = new NET_OUT_ATTENDANCE_ModifyUSER();
		
		boolean bRet = LoginModule.netsdk.CLIENT_Attendance_ModifyUser(LoginModule.m_hLoginHandle, stuIn, stuOut, TIME_OUT);
		if (!bRet) {
			System.err.printf("CLIENT_Attendance_ModifyUser Failed!" + ToolKits.getErrorCodePrint());
		}
		
		return bRet;
	}
	
	/**
	 * 考勤机 查找用户
	 * @param nOffset   		查询偏移
	 * @param nPagedQueryCount 	查询个数
	 * @return UserData[] 		用户信息组
	 */
	public static UserData[] findUser(int nOffset, int nPagedQueryCount) {
		
		/*
		 * 入参
		 */
		NET_IN_ATTENDANCE_FINDUSER stuIn = new NET_IN_ATTENDANCE_FINDUSER();
		stuIn.nOffset = nOffset;
		stuIn.nPagedQueryCount = nPagedQueryCount;
		
		/*
		 * 出参
		 */
		NET_OUT_ATTENDANCE_FINDUSER stuOut = new NET_OUT_ATTENDANCE_FINDUSER();
		NET_ATTENDANCE_USERINFO[]  userInfo = new NET_ATTENDANCE_USERINFO[nPagedQueryCount];
		for(int i = 0; i < nPagedQueryCount; i++) {
			userInfo[i] = new NET_ATTENDANCE_USERINFO();
		}
		stuOut.nMaxUserCount = nPagedQueryCount;
		stuOut.stuUserInfo = new Memory(userInfo[0].size() * stuOut.nMaxUserCount);
		stuOut.stuUserInfo.clear(userInfo[0].size() * stuOut.nMaxUserCount);
		ToolKits.SetStructArrToPointerData(userInfo, stuOut.stuUserInfo);  // 将数组内存拷贝到Pointer
		stuOut.nMaxPhotoDataLength = 128;
		stuOut.pbyPhotoData = new Memory(stuOut.nMaxPhotoDataLength);    // 申请内存
		
		boolean bRet = LoginModule.netsdk.CLIENT_Attendance_FindUser(LoginModule.m_hLoginHandle, stuIn, stuOut, TIME_OUT);
		if (!bRet) {
			System.err.printf("CLIENT_Attendance_FindUser Failed!" + ToolKits.getErrorCodePrint());
			return null;
		}
		
		ToolKits.GetPointerDataToStructArr(stuOut.stuUserInfo, userInfo); // 将 Pointer 的内容 输出到   数组
		
		UserData[]  userData = new UserData[stuOut.nRetUserCount];
		for(int i = 0; i < stuOut.nRetUserCount; i++) {
			userData[i] = new UserData();
			try {
				userData[i].userId = new String(userInfo[i].szUserID, "GBK").trim();
				userData[i].userName = new String(userInfo[i].szUserName, "GBK").trim();
				userData[i].cardNo = new String(userInfo[i].szCardNo, "GBK").trim();
			}catch(Exception e) {	// 如果转化失败就采用原始数据
				userData[i].userId = new String(userInfo[i].szUserID).trim();
				userData[i].userName = new String(userInfo[i].szUserName).trim();
				userData[i].cardNo = new String(userInfo[i].szCardNo).trim();
			}
			
//			getFingerByUserId(userData[i].userId, userData[i]); // 获取指纹信息
		} 
		
		UserData.nTotalUser = stuOut.nTotalUser;
		
		return userData;
	}
	
	/**
	 * 考勤获取用户信息
	 * @param userId   	用户ID
	 * @return UserData 用户信息
	 */
	public static UserData getUser(String userId) {
		
		/*
		 * 入参
		 */
		NET_IN_ATTENDANCE_GetUSER stuIn = new NET_IN_ATTENDANCE_GetUSER();
		stringToByteArray(userId, stuIn.szUserID);
		
		/*
		 * 出参
		 */
		NET_OUT_ATTENDANCE_GetUSER stuOut = new NET_OUT_ATTENDANCE_GetUSER();
		stuOut.nMaxLength = 128;
		stuOut.pbyPhotoData = new Memory(stuOut.nMaxLength);    // 申请内存
		stuOut.pbyPhotoData.clear(stuOut.nMaxLength);
		
		boolean bRet = LoginModule.netsdk.CLIENT_Attendance_GetUser(LoginModule.m_hLoginHandle, stuIn, stuOut, TIME_OUT);
		if (!bRet) {
			System.err.printf("CLIENT_Attendance_GetUser Failed!" + ToolKits.getErrorCodePrint());
			return null;
		}
		
		UserData userData = new UserData();
		try {
			userData.userId = new String(stuOut.stuUserInfo.szUserID, "GBK").trim();
			userData.userName = new String(stuOut.stuUserInfo.szUserName, "GBK").trim();
			userData.cardNo = new String(stuOut.stuUserInfo.szCardNo, "GBK").trim();
		}catch(Exception e) {	// 如果转化失败就采用原始数据
			userData.userId = new String(stuOut.stuUserInfo.szUserID).trim();
			userData.userName = new String(stuOut.stuUserInfo.szUserName).trim();
			userData.cardNo = new String(stuOut.stuUserInfo.szCardNo).trim();
		}
		
//		getFingerByUserId(userId, userData); // 获取指纹信息
		
		return userData;
	}

	/**
	 * 考勤机  通过用户ID插入指纹数据
	 * @param userId   			用户ID
	 * @param szFingerPrintInfo 指纹信息
	 */
	public static boolean insertFingerByUserId(String userId, byte[] szFingerPrintInfo) {
		
		/*
		 * 入参
		 */
		NET_IN_FINGERPRINT_INSERT_BY_USERID stuIn = new NET_IN_FINGERPRINT_INSERT_BY_USERID();
		stringToByteArray(userId, stuIn.szUserID);
		stuIn.nPacketCount = 1;
		stuIn.nSinglePacketLen = szFingerPrintInfo.length;
		stuIn.szFingerPrintInfo = new Memory(stuIn.nPacketCount * stuIn.nSinglePacketLen);    // 申请内存
		stuIn.szFingerPrintInfo.clear(stuIn.nPacketCount * stuIn.nSinglePacketLen);
		stuIn.szFingerPrintInfo.write(0, szFingerPrintInfo, 0, szFingerPrintInfo.length);
		
		/*
		 * 出参
		 */
		NET_OUT_FINGERPRINT_INSERT_BY_USERID stuOut = new NET_OUT_FINGERPRINT_INSERT_BY_USERID();
		
		boolean bRet = LoginModule.netsdk.CLIENT_Attendance_InsertFingerByUserID(LoginModule.m_hLoginHandle, stuIn, stuOut, TIME_OUT);
		if (!bRet) {
			System.err.printf("CLIENT_Attendance_InsertFingerByUserID Failed!" + ToolKits.getErrorCodePrint());
		}
		
		return bRet;
	}
	
	/**
	 * 考勤机 删除单个用户下所有指纹数据
	 * @param userId   用户ID
	 */
	public static boolean removeFingerByUserId(String userId) {
		
		/*
		 * 入参
		 */
		NET_CTRL_IN_FINGERPRINT_REMOVE_BY_USERID stuIn = new NET_CTRL_IN_FINGERPRINT_REMOVE_BY_USERID();
		stringToByteArray(userId, stuIn.szUserID);
		
		/*
		 * 出参
		 */
		NET_CTRL_OUT_FINGERPRINT_REMOVE_BY_USERID stuOut = new NET_CTRL_OUT_FINGERPRINT_REMOVE_BY_USERID();
		
		boolean bRet = LoginModule.netsdk.CLIENT_Attendance_RemoveFingerByUserID(LoginModule.m_hLoginHandle, stuIn, stuOut, TIME_OUT);
		if (!bRet) {
			System.err.printf("CLIENT_Attendance_RemoveFingerByUserID Failed!" + ToolKits.getErrorCodePrint());
		}
		
		return bRet;
	}
	
	/**
	 * 考勤机 通过指纹ID删除指纹数据
	 * @param nFingerPrintID  指纹ID
	 */
	public static boolean removeFingerRecord(int nFingerPrintID) {
		
		/*
		 * 入参
		 */
		NET_CTRL_IN_FINGERPRINT_REMOVE stuIn = new NET_CTRL_IN_FINGERPRINT_REMOVE();
		stuIn.nFingerPrintID = nFingerPrintID;
		
		/*
		 * 出参
		 */
		NET_CTRL_OUT_FINGERPRINT_REMOVE stuOut = new NET_CTRL_OUT_FINGERPRINT_REMOVE();
		
		boolean bRet = LoginModule.netsdk.CLIENT_Attendance_RemoveFingerRecord(LoginModule.m_hLoginHandle, stuIn, stuOut, TIME_OUT);
		if (!bRet) {
			System.err.printf("CLIENT_Attendance_RemoveFingerRecord Failed!" + ToolKits.getErrorCodePrint());
		}
		
		return bRet;
	}
	
	/**
	 * 考勤机 通过指纹ID获取指纹数据
	 * @param nFingerPrintID  指纹ID
	 * @return userData 用户数据
	 */
	public static UserData getFingerRecord(int nFingerPrintID) {
		
		/*
		 * 入参
		 */
		NET_CTRL_IN_FINGERPRINT_GET stuIn = new NET_CTRL_IN_FINGERPRINT_GET();
		stuIn.nFingerPrintID = nFingerPrintID;
		
		/*
		 * 出参
		 */
		NET_CTRL_OUT_FINGERPRINT_GET stuOut = new NET_CTRL_OUT_FINGERPRINT_GET();
		stuOut.nMaxFingerDataLength = nMaxFingerPrintSize;
		stuOut.szFingerPrintInfo = new Memory(stuOut.nMaxFingerDataLength);    // 申请内存
		stuOut.szFingerPrintInfo.clear(stuOut.nMaxFingerDataLength);

		boolean bRet = LoginModule.netsdk.CLIENT_Attendance_GetFingerRecord(LoginModule.m_hLoginHandle, stuIn, stuOut, TIME_OUT);
		if (!bRet) {
			System.err.printf("CLIENT_Attendance_GetFingerRecord Failed!" + ToolKits.getErrorCodePrint());
			return null;
		}
		
		if (stuOut.nRetLength == 0) {
			System.err.println("GetFingerRecord Failed nRetLength == 0!");
		}
		
		UserData userData = new UserData();
		userData.userId = new String(stuOut.szUserID).trim();
		userData.nFingerPrintIDs = new int[1];
		userData.nFingerPrintIDs[0] = nFingerPrintID; 
		userData.szFingerPrintInfo = new byte[1][stuOut.nRetLength];
		stuOut.szFingerPrintInfo.read(0, userData.szFingerPrintInfo[0], 0, stuOut.nRetLength);
		
		return userData;
	}
	
	/**
	 * 考勤机 通过用户ID查找该用户下的所有指纹数据
	 * @param userId   用户ID
	 * @param userData 用户数据
	 */
	public static boolean getFingerByUserId(String userId, UserData userData) {
		
		/*
		 * 入参
		 */
		NET_IN_FINGERPRINT_GETBYUSER stuIn = new NET_IN_FINGERPRINT_GETBYUSER();
		stringToByteArray(userId, stuIn.szUserID);
		
		/*
		 * 出参
		 */
		NET_OUT_FINGERPRINT_GETBYUSER stuOut = new NET_OUT_FINGERPRINT_GETBYUSER();
		stuOut.nMaxFingerDataLength = NetSDKLib.NET_MAX_FINGER_PRINT * nMaxFingerPrintSize;
		stuOut.pbyFingerData = new Memory(stuOut.nMaxFingerDataLength);    // 申请内存
		stuOut.pbyFingerData.clear(stuOut.nMaxFingerDataLength);
		
		boolean bRet = LoginModule.netsdk.CLIENT_Attendance_GetFingerByUserID(LoginModule.m_hLoginHandle, stuIn, stuOut, TIME_OUT);
		if (!bRet) {
			System.err.printf("CLIENT_Attendance_GetFingerByUserID Failed!" + ToolKits.getErrorCodePrint());
		}else {
			userData.nFingerPrintIDs = new int[stuOut.nRetFingerPrintCount];
			userData.szFingerPrintInfo = new byte[stuOut.nRetFingerPrintCount][stuOut.nSinglePacketLength];
			int offset = 0;
			for (int i = 0; i < stuOut.nRetFingerPrintCount; ++i) {
				userData.nFingerPrintIDs[i] = stuOut.nFingerPrintIDs[i];
				stuOut.pbyFingerData.read(offset, userData.szFingerPrintInfo[i], 0, stuOut.nSinglePacketLength);
				offset += stuOut.nSinglePacketLength;
			}
		}

		return bRet;
	}
	
	/**
	 * 指纹采集
	 * @param nChannelID   门禁序号
	 * @param szReaderID   读卡器ID
	 */
	public static boolean collectionFinger(int nChannelID, String szReaderID) {
		/*
		 * 入参
		 */
		NET_CTRL_CAPTURE_FINGER_PRINT stuCollection = new NET_CTRL_CAPTURE_FINGER_PRINT();
		stuCollection.nChannelID = nChannelID;
		stringToByteArray(szReaderID, stuCollection.szReaderID);
		
		stuCollection.write();
		boolean bRet = LoginModule.netsdk.CLIENT_ControlDeviceEx(LoginModule.m_hLoginHandle, NetSDKLib.CtrlType.CTRLTYPE_CTRL_CAPTURE_FINGER_PRINT, stuCollection.getPointer(), null, 5000);
		if (!bRet) {
			System.err.printf("CLIENT_ControlDeviceEx CAPTURE_FINGER_PRINT Failed!" + ToolKits.getErrorCodePrint());
		}
		return bRet;
	}
	
	/**
	 * 字符串转字符数组
	 * @param src   源字符串
	 * @param dst   目标字符数组
	 */
	public static void stringToByteArray(String src, byte[] dst) {
		
		if (src == null || src.isEmpty()) {
			return;
		}
		
		for(int i = 0; i < dst.length; i++) {
			dst[i] = 0;
		}
		
		byte []szSrc;
		
		try {
			szSrc = src.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			szSrc = src.getBytes();
		} 
		
		if (szSrc != null) {
			int len = szSrc.length >= dst.length ? dst.length-1:szSrc.length;
			System.arraycopy(szSrc, 0, dst, 0, len);
		}
	}
	
	/**
     * 用户信息
     * */
	public static class UserData {
		public static int nTotalUser;		// 用户总数
		
		public String userId;				// 用户ID
		public String userName;				// 用户名
		public String cardNo;				// 卡号
		public int[]    nFingerPrintIDs;	// 指纹ID数组
		public byte[][] szFingerPrintInfo;	// 指纹数据数组
	}
	
	 /**
     * 门禁事件信息
     * */
	public static class AccessEventInfo {
    	public String userId;		// 用户ID
		public String cardNo;		// 卡号
		public String eventTime;	// 事件发生时间
    	public int openDoorMethod;	// 开门方式
    }
	
	/**
     * 操作类型
     * */
	public enum OPERATE_TYPE {
		UNKNOWN,						// 未知
		SEARCH_USER,					// 搜索用户（第一页）
		PRE_SEARCH_USER,				// 搜索用户（上一页）
		NEXT_SEARCH_USER,				// 搜索用户（下一页）
		SEARCH_USER_BY_USERID,			// 通过用户ID搜索用户
		ADD_USER,						// 添加用户
		DELETE_USER,					// 删除用户
		MODIFIY_USER,					// 修改用户
		FINGERPRINT_OPEARTE_BY_USERID,	// 通过用户ID操作指纹
		FINGERPRINT_OPEARTE_BY_ID,		// 通过指纹ID操作指纹
		SEARCH_FINGERPRINT_BY_USERID,	// 通过用户ID搜索指纹
		SEARCH_FINGERPRINT_BY_ID,		// 通过指纹ID搜索指纹
		ADD_FINGERPRINT,				// 添加指纹
		DELETE_FINGERPRINT_BY_USERID,	// 通过用户ID删除指纹
		DELETE_FINGERPRINT_BY_ID		// 通过指纹ID删除指纹
	};
}





