package main.java.com.netsdk.demo.frame.Attendance;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.sun.jna.Pointer;

import main.java.com.netsdk.common.BorderEx;
import main.java.com.netsdk.common.Res;
import main.java.com.netsdk.demo.frame.Attendance.AttendanceShowPanel.EventInfoShowPanel;
import main.java.com.netsdk.demo.module.AttendanceModule;
import main.java.com.netsdk.demo.module.AttendanceModule.AccessEventInfo;
import main.java.com.netsdk.lib.NetSDKLib;
import main.java.com.netsdk.lib.ToolKits;
import main.java.com.netsdk.lib.NetSDKLib.*;

/**
 * 订阅面板
 */
public class SubscribePanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private java.awt.Component  target = this;	// 目标
	private boolean bSubscribe = false; 		// 订阅标志
	private EventInfoShowPanel eventShowPanel;	// 事件显示界面
	public SubscribePanel(EventInfoShowPanel eventPanel) {
		BorderEx.set(this, Res.string().getSubscribe(), 1);
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 30));
		setPreferredSize(new Dimension(180, 80));

		eventShowPanel = eventPanel;
		callback = new fAnalyzerDataCB();
		
		subscribeBtn = new JButton(Res.string().getSubscribe());
		subscribeBtn.setPreferredSize(new Dimension(150, 20));

		add(subscribeBtn);
		
		subscribeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (bSubscribe) {
					AttendanceModule.stopRealLoadPicture();
					eventShowPanel.clearEvent();
					setSubscribeStatus(false);
				}else {
					if (AttendanceModule.realLoadPicture(callback)) {
						setSubscribeStatus(true);
					}else {
						JOptionPane.showMessageDialog(null, Res.string().getSubscribeFailed() + ", " + ToolKits.getErrorCodeShow(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			
		});
		
		subscribeBtn.setEnabled(false);
	}

	public void setButtonEnable(boolean b) {
		setSubscribeStatus(false);
		subscribeBtn.setEnabled(b);
	}
	
	public void setSubscribeStatus(boolean b) {
		bSubscribe = b;
		if (bSubscribe) {
			subscribeBtn.setText(Res.string().getUnSubscribe());
		}else {
			subscribeBtn.setText(Res.string().getSubscribe());
		}
	}
	
	/**
     * 智能报警事件回调
     **/
    public class fAnalyzerDataCB implements fAnalyzerDataCallBack {
    	public final EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
		@Override
		public int invoke(LLong lAnalyzerHandle, int dwAlarmType,
				Pointer pAlarmInfo, Pointer pBuffer, int dwBufSize,
				Pointer dwUser, int nSequence, Pointer reserved) {
        	if(pAlarmInfo == null) {
        		return 0;
        	}
        	
			switch(dwAlarmType) {
	            case NetSDKLib.EVENT_IVS_ACCESS_CTL:   // 门禁事件
	            	DEV_EVENT_ACCESS_CTL_INFO event = new DEV_EVENT_ACCESS_CTL_INFO();
	            	ToolKits.GetPointerData(pAlarmInfo, event);
	            	AccessEventInfo accessEvent = new AccessEventInfo();
	            	accessEvent.userId = new String(event.szUserID).trim();
	            	accessEvent.cardNo = new String(event.szCardNo).trim();
	            	accessEvent.eventTime = event.UTC.toStringTime();
	            	accessEvent.openDoorMethod = event.emOpenMethod;
	                if (eventQueue != null) {
	                	eventQueue.postEvent(new AccessEvent(target, accessEvent));
	                }
	            	break;
                default:
                	break;
			}
			
			return 0;
		}
	}
    
    /**
     * 门禁事件
     **/
	class AccessEvent extends AWTEvent {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public static final int EVENT_ID = AWTEvent.RESERVED_ID_MAX + 1;
		private AccessEventInfo accessEvent;

		public AccessEvent(Object target, AccessEventInfo accessEvent) {
			super(target, EVENT_ID);
			this.accessEvent = accessEvent;
		}
		
		public AccessEventInfo getAccessEventInfo() {
			return this.accessEvent;
		}
	}
	
	@Override
    protected void processEvent( AWTEvent event) {
        if ( event instanceof AccessEvent) {
        	AccessEventInfo accessEventInfo = ((AccessEvent)event).getAccessEventInfo();
        	eventShowPanel.insertEvent(accessEventInfo);
		} else {
            super.processEvent(event);   
        }
    } 
    
	private JButton subscribeBtn; 				// 订阅按钮
	private fAnalyzerDataCallBack callback;		// 事件回调
}