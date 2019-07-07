package main.java.com.netsdk.demo.frame.FaceRecognition;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import java.util.concurrent.ExecutionException;


import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import main.java.com.netsdk.common.DateChooserJButtonEx;
import main.java.com.netsdk.common.PaintPanel;
import main.java.com.netsdk.common.Res;
import main.java.com.netsdk.demo.module.LoginModule;
import main.java.com.netsdk.demo.module.SearchByPictureModule;
import main.java.com.netsdk.lib.NetSDKLib;
import main.java.com.netsdk.lib.ToolKits;
import main.java.com.netsdk.lib.NetSDKLib.*;

public class SearchByPicDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Vector<String> chnList = new Vector<String>(); 
	private Memory memory = null;
	private static volatile int nProgress = 0;  		   // 设备处理进度
	private static volatile int nCount = 0;
	
	public SearchByPicDialog() {
	    setTitle("以图搜图");
	    setLayout(new BorderLayout());
	    setModal(true);  
	    pack();
	    setSize(780, 550);
	    setResizable(false);
	    setLocationRelativeTo(null);
	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);   // 释放窗体
	    	
	    JPanel panel = new JPanel();
	    progressBar = new JProgressBar(0, 100);
	    Dimension dimension = new Dimension();
	    dimension.height = 18;
	    progressBar.setPreferredSize(dimension);
		progressBar.setStringPainted(true);
	    
	    add(panel, BorderLayout.CENTER);
	    add(progressBar, BorderLayout.SOUTH);
	    
	    ////////
	    panel.setLayout(new BorderLayout());
	    SearchPicConditionPanel searchPicConditionPanel = new SearchPicConditionPanel();
	    searchPicInfoTextArea = new JTextArea();
	    
	    Dimension dimension1 = new Dimension();
	    dimension1.width = 220;    
	    searchPicConditionPanel.setPreferredSize(dimension1);
	    
	    panel.add(searchPicConditionPanel, BorderLayout.WEST);
	    panel.add(new JScrollPane(searchPicInfoTextArea), BorderLayout.CENTER);
	    
	    addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent e) {
	    		dispose();
	    	}
	    });
	}
	
	private class SearchPicConditionPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public SearchPicConditionPanel() {
			setLayout(new BorderLayout());
			
			JPanel panelNorth = new JPanel();
			JPanel panelSouth = new JPanel();
			
			add(panelNorth, BorderLayout.NORTH);
			add(panelSouth, BorderLayout.SOUTH);
			
			////////
			searchPicPanel = new PaintPanel();
			JButton selectPicBtn = new JButton(Res.string().getSelectPicture());
			JButton downloadBtn = new JButton("下载查询到的图片");
			
			searchPicPanel.setPreferredSize(new Dimension(210, 270));
			selectPicBtn.setPreferredSize(new Dimension(210, 20));
			downloadBtn.setPreferredSize(new Dimension(210, 20));
		
			panelNorth.setLayout(new FlowLayout());
			panelNorth.setPreferredSize(new Dimension(210, 330));
			panelNorth.add(searchPicPanel);
			panelNorth.add(selectPicBtn);
			panelNorth.add(downloadBtn);
			
			/////
			faceCheckBox = new JCheckBox("人脸库");
			historyCheckBox = new JCheckBox("历史库");
			faceCheckBox.setPreferredSize(new Dimension(100, 20));
			historyCheckBox.setPreferredSize(new Dimension(100, 20));

			startTimeLabel = new JLabel(Res.string().getStartTime(), JLabel.CENTER);
			endTimeLabel = new JLabel(Res.string().getEndTime(), JLabel.CENTER);
			chnLabel = new JLabel(Res.string().getChannel(), JLabel.CENTER);
			JLabel similaryLabel = new JLabel(Res.string().getSimilarity(), JLabel.CENTER);
		
			Dimension dimension1 = new Dimension();
			dimension1.width = 80;
			dimension1.height = 20;
			
			startTimeLabel.setPreferredSize(dimension1);
			endTimeLabel.setPreferredSize(dimension1);
			chnLabel.setPreferredSize(dimension1);
			similaryLabel.setPreferredSize(dimension1);
			
		    startTimeBtn = new DateChooserJButtonEx("2018-11-07");
			endTimeBtn = new DateChooserJButtonEx();
			
			chnComboBox = new JComboBox(); 
			for(int i = 1; i < LoginModule.m_stDeviceInfo.byChanNum + 1; i++) {
				chnList.add(Res.string().getChannel() + " " + String.valueOf(i));
			}
			
			// 登陆成功，将通道添加到控件
			chnComboBox.setModel(new DefaultComboBoxModel(chnList));
			
			similaryTextField = new JTextField("60", JTextField.CENTER);
			
			Dimension dimension2 = new Dimension();
			dimension2.width = 120;
			dimension2.height = 20;
			
			startTimeBtn.setPreferredSize(dimension2);
			endTimeBtn.setPreferredSize(dimension2);
			chnComboBox.setPreferredSize(dimension2);
			similaryTextField.setPreferredSize(dimension2);
			
			searchPicBtn = new JButton(Res.string().getSearch());
			searchPicBtn.setPreferredSize(new Dimension(210, 20));
			
			panelSouth.setLayout(new FlowLayout());
			panelSouth.setPreferredSize(new Dimension(210, 160));
			panelSouth.add(faceCheckBox);
			panelSouth.add(historyCheckBox);
			panelSouth.add(startTimeLabel);
			panelSouth.add(startTimeBtn);
			panelSouth.add(endTimeLabel);
			panelSouth.add(endTimeBtn);
			panelSouth.add(chnLabel);
			panelSouth.add(chnComboBox);
			panelSouth.add(similaryLabel);
			panelSouth.add(similaryTextField);
			panelSouth.add(searchPicBtn);
			
			historyCheckBox.setSelected(true);
			faceCheckBox.setSelected(false);
			
			// 选择图片，获取图片的信息
			selectPicBtn.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					String picPath = "";
					
					// 选择图片，获取图片路径，并在界面显示
					picPath = ToolKits.openPictureFile(searchPicPanel);
							
					if(!picPath.equals("")) {
						memory = ToolKits.readPictureFile(picPath);
					}
			
				}
			});
			
			downloadBtn.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					DownloadPictureDialog dialog = new DownloadPictureDialog();
					dialog.setVisible(true);		
				}
			});
			
			searchPicBtn.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					SwingUtilities.invokeLater(new Runnable() {				
						@Override
						public void run() {
							searchPicBtn.setEnabled(false);
							progressBar.setValue(0);
							searchPicInfoTextArea.setText("");
						}
					});
					
					searchByPicture();
				}
			});
			
			faceCheckBox.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(faceCheckBox.isSelected()) {
						historyCheckBox.setSelected(false);
						chnLabel.setVisible(false);
						chnComboBox.setVisible(false);
						startTimeLabel.setVisible(false);
						endTimeLabel.setVisible(false);
						startTimeBtn.setVisible(false);
						endTimeBtn.setVisible(false);
					} else {
						historyCheckBox.setSelected(true);
						chnLabel.setVisible(true);
						chnComboBox.setVisible(true);
						startTimeLabel.setVisible(true);
						endTimeLabel.setVisible(true);
						startTimeBtn.setVisible(true);
						endTimeBtn.setVisible(true);
					}					
				}
			});
			
			historyCheckBox.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(historyCheckBox.isSelected()) {
						faceCheckBox.setSelected(false);
						chnLabel.setVisible(true);
						chnComboBox.setVisible(true);
						startTimeLabel.setVisible(true);
						endTimeLabel.setVisible(true);
						startTimeBtn.setVisible(true);
						endTimeBtn.setVisible(true);
					} else {
						faceCheckBox.setSelected(true);
						chnLabel.setVisible(false);
						chnComboBox.setVisible(false);
						startTimeLabel.setVisible(false);
						endTimeLabel.setVisible(false);
						startTimeBtn.setVisible(false);
						endTimeBtn.setVisible(false);
					}					
				}
			});
		}
	}
	
	private void searchByPicture() {
		new SwingWorker<Boolean, StringBuffer>() {
			int nTotalCount = 0;      // 查询到的总个数
			
			@Override
			protected Boolean doInBackground() {							
				int beginNum = 0;     // 偏移量
				int nCount = 0;		  // 循环查询了几次	
				int index = 0;	 	  // index + 1 为查询到的总个数    
				int nFindCount = 10;  // 每次查询的个数
				
				StringBuffer message = null;
				
				if(memory == null) {
					JOptionPane.showMessageDialog(null, "请先选择人脸图片", Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				// 获取查询句柄
				nTotalCount = SearchByPictureModule.startFindPerson(memory, 
																   startTimeBtn.getText(), 
																   endTimeBtn.getText(), 
																   historyCheckBox.isSelected(),
																   chnComboBox.getSelectedIndex(), 
																   similaryTextField.getText());
				if(nTotalCount == 0) {   // 查询失败				
					// 查询失败，关闭查询
					SearchByPictureModule.doFindClosePerson(); 			
					return false;
				} else if(nTotalCount == -1) {  // 设备正在处理，通过订阅来查询处理进度
					nProgress = 0;
					nCount = 0;
					SearchByPictureModule.attachFaceFindState(fFaceFindStateCb.getInstance());		
				} else {				
					while(true) {
						CANDIDATE_INFOEX[] caInfoexs = SearchByPictureModule.doFindNextPerson(beginNum, nFindCount);
						if(caInfoexs == null) {
							break;
						}
						
						for(int i = 0; i < caInfoexs.length; i++) {
							index = i + nFindCount * nCount + 1;
							
							// 清空
							message = new StringBuffer();
							
							if(historyCheckBox.isSelected()) {    // 历史库显示
								message.append("[" + index + "]时间 :" + caInfoexs[i].stTime.toStringTimeEx() + "\n");
							
								message.append("[" + index + "]UID :" + new String(caInfoexs[i].stPersonInfo.szUID).trim() + "\n");
								message.append("[" + index + "]性别 :" + Res.string().getSex(caInfoexs[i].stPersonInfo.bySex) + "\n");
								message.append("[" + index + "]年龄 :" + caInfoexs[i].stPersonInfo.byAge + "\n");
								message.append("[" + index + "]种族 :" + Res.string().getRace(caInfoexs[i].stPersonInfo.emRace) + "\n");
								message.append("[" + index + "]眼睛 :" + Res.string().getEyeState(caInfoexs[i].stPersonInfo.emEye) + "\n");
								message.append("[" + index + "]嘴巴 :" + Res.string().getMouthState(caInfoexs[i].stPersonInfo.emMouth) + "\n");
								message.append("[" + index + "]口罩 :" + Res.string().getMaskState(caInfoexs[i].stPersonInfo.emMask) + "\n");
								message.append("[" + index + "]胡子 :" + Res.string().getBeardState(caInfoexs[i].stPersonInfo.emBeard) + "\n");
								message.append("[" + index + "]眼镜 :" + Res.string().getGlasses(caInfoexs[i].stPersonInfo.byGlasses) + "\n");
								message.append("[" + index + "]相似度 :" + caInfoexs[i].bySimilarity + "\n");
								message.append("[" + index + "]图片路径 :" + caInfoexs[i].stPersonInfo.szFacePicInfo[0].pszFilePath.getString(0) + "\n");
			
							} else {                              // 人脸库显示
								message.append("[" + index + "]人脸库ID :" + new String(caInfoexs[i].stPersonInfo.szGroupID).trim() + "\n");
								try {
									message.append("[" + index + "]人脸库名称 :" + new String(caInfoexs[i].stPersonInfo.szGroupName, "GBK").trim() + "\n");					
									message.append("[" + index + "]姓名 :" + new String(caInfoexs[i].stPersonInfo.szPersonName, "GBK").trim() + "\n");
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
								message.append("[" + index + "]UID :" + new String(caInfoexs[i].stPersonInfo.szUID).trim() + "\n");
								message.append("[" + index + "]生日 :" + (caInfoexs[i].stPersonInfo.wYear) + "-" + 
											                		    (0xff & caInfoexs[i].stPersonInfo.byMonth) + "-" +
											                		    (0xff & caInfoexs[i].stPersonInfo.byDay) + "\n");
								message.append("[" + index + "]性别 :" + Res.string().getSex(caInfoexs[i].stPersonInfo.bySex) + "\n");
								message.append("[" + index + "]证件类型 :" + Res.string().getIdType(caInfoexs[i].stPersonInfo.byIDType) + "\n");
								message.append("[" + index + "]证件号 :" + new String(caInfoexs[i].stPersonInfo.szID).trim() + "\n");
								message.append("[" + index + "]相似度 :" + caInfoexs[i].bySimilarity + "\n");
								message.append("[" + index + "]图片路径 :" + caInfoexs[i].stPersonInfo.szFacePicInfo[0].pszFilePath.getString(0) + "\n");
							}
							
							message.append("\n");
							publish(message);
						}
						
				        if(caInfoexs.length < nFindCount) {
				            System.out.printf("No More Record, Find End!\n");
				            break;
				        } else {
				            beginNum += nFindCount;
				            nCount++;
				        }
					}	
					
					// 关闭查询
					SearchByPictureModule.doFindClosePerson(); 
				}

				return true;
			}
			
			@Override
			protected void process(java.util.List<StringBuffer> chunks) {
				for(StringBuffer data : chunks) {
					searchPicInfoTextArea.append(data.toString());
					searchPicInfoTextArea.updateUI();
				}
				
				super.process(chunks);
			}
			
			@Override
			protected void done() {
				if(nTotalCount == 0) {      	 // 查询总个数失败
					searchPicBtn.setEnabled(true);
					progressBar.setValue(100);
					searchPicInfoTextArea.append("未查询到相关信息... \n");
			        searchPicInfoTextArea.updateUI();
				} else if(nTotalCount == -1){    // 设备在处理中
					searchPicInfoTextArea.append("设备正在处理中... \n");
			        searchPicInfoTextArea.updateUI();
				} else {
					try {
						if(get()) {             // 其他情况，查询信息结束
							searchPicBtn.setEnabled(true);
							progressBar.setValue(100);
							searchPicInfoTextArea.append("查询结束... \n");
						    searchPicInfoTextArea.updateUI();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}		
			}
		}.execute();
	}
	
	/**
	 * 订阅人脸回调函数
	 */
	private static class fFaceFindStateCb implements NetSDKLib.fFaceFindState {	
		private fFaceFindStateCb() {}
		
		private static class fFaceFindStateCbHolder {
			private static final fFaceFindStateCb instance = new fFaceFindStateCb();
		}
		
		public static fFaceFindStateCb getInstance() {
			return fFaceFindStateCbHolder.instance;
		}
		
		@Override
		public void invoke(LLong lLoginID, LLong lAttachHandle,
				Pointer pstStates, int nStateNum, Pointer dwUser) {
			if(nStateNum < 1) {
				return;
			}
			NET_CB_FACE_FIND_STATE[] msg = new NET_CB_FACE_FIND_STATE[nStateNum];
			for(int i = 0; i < nStateNum; i++) {
				msg[i] = new NET_CB_FACE_FIND_STATE();
			}
			ToolKits.GetPointerDataToStructArr(pstStates, msg);
			
			for(int i = 0; i < nStateNum; i++) {
				if(SearchByPictureModule.nToken == msg[i].nToken) {			
					nProgress = msg[i].nProgress;	
					nCount = msg[i].nCurrentCount; // 返回的总个数
								
					// 刷新设备处理进度
					// UI线程
		            EventQueue.invokeLater(new Runnable() {			            	
						@Override
						public void run() {
							progressBar.setValue(nProgress);	
							
					        if(nProgress == 100) {    				    // 进度等于100，设备处理完毕，开始查询
					        	// 异步线程处理
					            new SearchPictureWoker(nCount).execute();		            	
					        }
						}
					});	         
				}
			}
		}	
	}
	
	/**
	 * 用于订阅人脸状态后的查询
	 * 以图搜图与查询人员信息的接口是一样的，只是逻辑不一样，doFindNextPerson接口时，都是指定每次查询的个数，最大20，然后根据偏移量循环查询
	 * SwingWorker为异步线程，回调属于子线程，不能做耗时操作和刷新UI
	 */
	private static class SearchPictureWoker extends SwingWorker<Boolean, StringBuffer> {		
		private int nTotalCount;  // 查询到的总个数
		public SearchPictureWoker(int nTotalCount) {
			this.nTotalCount = nTotalCount;
		}
		
		@Override
		protected Boolean doInBackground() {
			int beginNum = 0;     // 偏移量
			int nCount = 0;		  // 循环查询了几次	
			int index = 0;	 	  // index + 1 为查询到的总个数    
			int nFindCount = 10;  // 每次查询的个数
			
			StringBuffer message = null;
			
			// 进度达到100%，关闭订阅
			SearchByPictureModule.detachFaceFindState();
			System.out.println("nTotalCount = " + nTotalCount);
			if(nTotalCount == 0) {
				return false;
			}
							
			while(true) {
				CANDIDATE_INFOEX[] caInfoexs = SearchByPictureModule.doFindNextPerson(beginNum, nFindCount);
				if(caInfoexs == null) {
					break;
				}

				for(int i = 0; i < caInfoexs.length; i++) {
					index = i + nFindCount * nCount + 1;
					
					// 清空
					message = new StringBuffer();
					
					if(historyCheckBox.isSelected()) {    // 历史库显示
						message.append("[" + index + "]时间 :" + caInfoexs[i].stTime.toStringTimeEx() + "\n");
					
						message.append("[" + index + "]UID :" + new String(caInfoexs[i].stPersonInfo.szUID).trim() + "\n");
						message.append("[" + index + "]性别 :" + Res.string().getSex(caInfoexs[i].stPersonInfo.bySex) + "\n");
						message.append("[" + index + "]年龄 :" + caInfoexs[i].stPersonInfo.byAge + "\n");
						message.append("[" + index + "]种族 :" + Res.string().getRace(caInfoexs[i].stPersonInfo.emRace) + "\n");
						message.append("[" + index + "]眼睛 :" + Res.string().getEyeState(caInfoexs[i].stPersonInfo.emEye) + "\n");
						message.append("[" + index + "]嘴巴 :" + Res.string().getMouthState(caInfoexs[i].stPersonInfo.emMouth) + "\n");
						message.append("[" + index + "]口罩 :" + Res.string().getMaskState(caInfoexs[i].stPersonInfo.emMask) + "\n");
						message.append("[" + index + "]胡子 :" + Res.string().getBeardState(caInfoexs[i].stPersonInfo.emBeard) + "\n");
						message.append("[" + index + "]眼镜 :" + Res.string().getGlasses(caInfoexs[i].stPersonInfo.byGlasses) + "\n");
						message.append("[" + index + "]相似度 :" + caInfoexs[i].bySimilarity + "\n");
						message.append("[" + index + "]图片路径 :" + caInfoexs[i].stPersonInfo.szFacePicInfo[0].pszFilePath.getString(0) + "\n");
	
					} else {                             // 人脸库显示
						message.append("[" + index + "]人脸库ID :" + new String(caInfoexs[i].stPersonInfo.szGroupID).trim() + "\n");
						try {
							message.append("[" + index + "]人脸库名称 :" + new String(caInfoexs[i].stPersonInfo.szGroupName, "GBK").trim() + "\n");					
							message.append("[" + index + "]姓名 :" + new String(caInfoexs[i].stPersonInfo.szPersonName, "GBK").trim() + "\n");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						message.append("[" + index + "]UID :" + new String(caInfoexs[i].stPersonInfo.szUID).trim() + "\n");
						message.append("[" + index + "]生日 :" + (caInfoexs[i].stPersonInfo.wYear) + "-" + 
									                		    (0xff & caInfoexs[i].stPersonInfo.byMonth) + "-" +
									                		    (0xff & caInfoexs[i].stPersonInfo.byDay) + "\n");
						message.append("[" + index + "]性别 :" + Res.string().getSex(caInfoexs[i].stPersonInfo.bySex) + "\n");
						message.append("[" + index + "]证件类型 :" + Res.string().getIdType(caInfoexs[i].stPersonInfo.byIDType) + "\n");
						message.append("[" + index + "]证件号 :" + new String(caInfoexs[i].stPersonInfo.szID).trim() + "\n");
						message.append("[" + index + "]相似度 :" + caInfoexs[i].bySimilarity + "\n");
						message.append("[" + index + "]图片路径 :" + caInfoexs[i].stPersonInfo.szFacePicInfo[0].pszFilePath.getString(0) + "\n");
					}
					
					message.append("\n");
					publish(message);
				}
				
		        if(caInfoexs.length < nFindCount) {
		            System.out.printf("No More Record, Find End!\n");
		            break;
		        } else {
		            beginNum += nFindCount;
		            nCount++;
		        }
			}	
			
			// 关闭查询
			SearchByPictureModule.doFindClosePerson(); 

			return true;
		}
		
		@Override
		protected void process(java.util.List<StringBuffer> chunks) {
			for(StringBuffer data : chunks) {
				searchPicInfoTextArea.append(data.toString());
				searchPicInfoTextArea.updateUI();
			}
			
			super.process(chunks);
		}
		
		@Override
		protected void done() {	
			searchPicBtn.setEnabled(true);
			searchPicInfoTextArea.append("查询结束... \n");
		    searchPicInfoTextArea.updateUI();
		}
	}

	
	private static JTextArea searchPicInfoTextArea;
	private static JProgressBar progressBar;
	private static JButton searchPicBtn;
	
	private PaintPanel searchPicPanel;
	private JComboBox chnComboBox;
	private JTextField similaryTextField;
	private DateChooserJButtonEx startTimeBtn;
	private DateChooserJButtonEx endTimeBtn;
	private JLabel chnLabel;
	private JLabel startTimeLabel;
	private JLabel endTimeLabel;
	
	private JCheckBox faceCheckBox;
	private static JCheckBox historyCheckBox;
}
