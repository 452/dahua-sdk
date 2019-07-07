package main.java.com.netsdk.demo.module;

import main.java.com.netsdk.lib.NetSDKLib.*;


/**
 * 设备搜索接口实现
 * 主要功能有 ： 设备组播和广播搜索、设备IP单播搜索
 */
public class DeviceSearchModule {

	/**
	 * 设备组播和广播搜索
	 */
	public static LLong multiBroadcastDeviceSearch(fSearchDevicesCB cbSearchDevices) {
		return LoginModule.netsdk.CLIENT_StartSearchDevices(cbSearchDevices, null, null);
	}
	
	/**
	 * 停止设备组播和广播搜索
	 */
	public static void stopDeviceSearch(LLong m_DeviceSearchHandle) {
		if(m_DeviceSearchHandle.longValue() == 0) {
			return;
		}
		
		LoginModule.netsdk.CLIENT_StopSearchDevices(m_DeviceSearchHandle);
		m_DeviceSearchHandle.setValue(0);
	}
	
	/**
	 * 设备IP单播搜索
	 * @param startIP 起始IP
	 * @param nIpNum IP个数，最大 256
	 */
	public static boolean unicastDeviceSearch(String startIP, int nIpNum, fSearchDevicesCB cbSearchDevices) {
		String[] szIPStr = startIP.split("\\.");
		
		DEVICE_IP_SEARCH_INFO deviceSearchInfo = new DEVICE_IP_SEARCH_INFO();
		deviceSearchInfo.nIpNum = nIpNum;
		for(int i = 0; i < deviceSearchInfo.nIpNum; i++) {
			System.arraycopy(getIp(szIPStr, i).getBytes(), 0, deviceSearchInfo.szIPArr[i].szIP, 0, getIp(szIPStr, i).getBytes().length);
		}
		if(LoginModule.netsdk.CLIENT_SearchDevicesByIPs(deviceSearchInfo, cbSearchDevices, null, null, 6000)) {
			System.out.println("SearchDevicesByIPs Succeed!");
			return true;
		}
		return false;
	}
	
	public static String getIp(String[] ip, int num) {
		String szIp = "";
		if(Integer.parseInt(ip[3]) >= 255) {
			szIp = ip[0] + "." + ip[1] + "." + String.valueOf(Integer.parseInt(ip[2]) + 1) + "." + String.valueOf(Integer.parseInt(ip[3]) + num - 255);
		} else {
			szIp = ip[0] + "." + ip[1] + "." + ip[2] + "." + String.valueOf(Integer.parseInt(ip[3]) + num);
		}
		
		return szIp;
	}
}
