package main.java.com.netsdk.demo.frame.AutoRegister;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.sun.jna.Pointer;

import main.java.com.netsdk.common.BorderEx;
import main.java.com.netsdk.common.DeviceManagerListener;
import main.java.com.netsdk.common.FunctionList;
import main.java.com.netsdk.common.PaintPanel;
import main.java.com.netsdk.common.Res;
import main.java.com.netsdk.common.SavePath;
import main.java.com.netsdk.demo.module.*;
import main.java.com.netsdk.lib.NetSDKLib;
import main.java.com.netsdk.lib.ToolKits;
import main.java.com.netsdk.lib.NetSDKLib.*;

/**
 * 主动注册Demo：包含主动注册、实时预览、对讲、主动注册网络配置
 */
class AutoRegisterFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	// 设备断线通知回调
	private DisConnect disConnectCallback = new DisConnect(); 
	
	// 主动注册监听回调
	private ServiceCB servicCallback = new ServiceCB();
	
	// 抓图回调
	public CaptureReceiveCB  captureCallback = new CaptureReceiveCB();
    
    private boolean isExist = false;
	
    private TreeCellRender treeCellRender = new TreeCellRender();
	
    // 预览句柄
	private LLong realplayHandle = new LLong(0);
	
	// 预览节点
	private ChannelTreeNode realplayChannelTreeNode = null;
	
	// 对讲节点
	private DeviceTreeNode talkDeviceTreeNode = null;
	
	public AutoRegisterFrame() {
	    setTitle(Res.string().getAutoRegister());
	    setLayout(new BorderLayout());
	    pack();
	    setSize(780, 540);
	    setResizable(false);
	    setLocationRelativeTo(null);
	    
	    // 打开工程，初始化，设置断线回调
	    LoginModule.init(disConnectCallback, null);   
	    
	    // 设置抓图回调
	    AutoRegisterModule.setSnapRevCallBack(captureCallback);
	    
    	try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        	e.printStackTrace();
        } 
    	
    	// 注册列表面板
    	AutoRegisterTreePanel autoRegisterTreePanel = new AutoRegisterTreePanel();
		
		// 实时预览面板
		RealPlayPanel realplayPanel = new RealPlayPanel();   		
		
		// 监听和设备管理面板
		ListenDeviceManagerPanel listenDeviceManagerPanel = new ListenDeviceManagerPanel();

		add(autoRegisterTreePanel, BorderLayout.WEST);
		add(realplayPanel, BorderLayout.CENTER);
		add(listenDeviceManagerPanel, BorderLayout.EAST);
    	
    	addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent e) {
				// 停止对讲
				if(AutoRegisterModule.m_hTalkHandle.longValue() != 0) {
					AutoRegisterModule.stopTalk(AutoRegisterModule.m_hTalkHandle);									
					talkDeviceTreeNode = null;
				}	
				
				// 停止拉流
				if(realplayHandle.longValue() != 0) {
					AutoRegisterModule.stopRealPlay(realplayHandle);
					realplayWindowPanel.repaint();
					
					realplayChannelTreeNode = null;						
				}			
				
				// 登出所有设备
				for(int i = 0; i < rootNode.getChildCount(); i++) {
					DeviceTreeNode deviceTreeNode = (DeviceTreeNode)rootNode.getChildAt(i);
					if(deviceTreeNode.getDeviceInfo().getLoginHandle().longValue() != 0) {
						AutoRegisterModule.logout(deviceTreeNode.getDeviceInfo().getLoginHandle());
					}
				}
	
				// 停止服务
				AutoRegisterModule.stopServer();
				
	    		LoginModule.cleanup();   // 关闭工程，释放资源
	    		dispose();	
	    		
	    		SwingUtilities.invokeLater(new Runnable() {
	    			public void run() {
	    				FunctionList demo = new FunctionList();
	    				demo.setVisible(true);
	    			}
	    		});
	    	}
	    });
	}
	
	/////////////////面板///////////////////
	// 设备断线回调: 通过 CLIENT_Init 设置该回调函数，当设备出现断线时，SDK会调用该函数
	private class DisConnect implements fDisConnect {
		public void invoke(LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
			System.out.printf("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
				
			for(int i = 0; i < rootNode.getChildCount(); i++) {
				DeviceTreeNode deviceTreeNode = (DeviceTreeNode)rootNode.getChildAt(i);			
			
				// 根据设备IP判断断线设备
				if(pchDVRIP.equals(deviceTreeNode.getDeviceInfo().getDeviceIp())
						&& nDVRPort == deviceTreeNode.getDeviceInfo().getDevicePort()) {						
				
					synchronized (this) {
						// 停止断线设备的对讲, 主动注册中，对讲要用同步，不能在另开的线程里停止对讲，否则会出现句柄无效的错误					
						if(deviceTreeNode == talkDeviceTreeNode) {
							AutoRegisterModule.stopTalk(AutoRegisterModule.m_hTalkHandle);					

							talkDeviceTreeNode = null;
						}

						// 停止断线设备的拉流
						for(int j = 0; j < deviceTreeNode.getChildCount(); j++) {
							ChannelTreeNode channelTreeNode = (ChannelTreeNode)deviceTreeNode.getChildAt(j);
							if(channelTreeNode == realplayChannelTreeNode) {
								AutoRegisterModule.stopRealPlay(realplayHandle);
								realplayWindowPanel.repaint();
								
								realplayChannelTreeNode = null;

								break;
							}							
						}		
						
						// 登出
						if(deviceTreeNode.getDeviceInfo().getLoginHandle().longValue() != 0) {
							AutoRegisterModule.logout(deviceTreeNode.getDeviceInfo().getLoginHandle());
						}
						
						SwingUtilities.invokeLater(new DisConnectRunnable(deviceTreeNode));
					}		
					
					break;
				}	
			}
					
		}
	}
	
	private class DisConnectRunnable implements Runnable {
		DeviceTreeNode devicetTreeNode;
		
		public DisConnectRunnable(DeviceTreeNode devicetTreeNode) {
			this.devicetTreeNode = devicetTreeNode;
		}
	
		@Override
		public void run() {			
			devicetTreeNode.setUserObject(devicetTreeNode.getDeviceInfo().getDevcieId());				
			devicetTreeNode.removeAllChildren();  						
						
			tree.updateUI();				
		}	
	}
	
	private class AutoRegisterTreePanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public AutoRegisterTreePanel() {
			setLayout(new BorderLayout());
			Dimension dimension = new Dimension();
			dimension.width = 210;
			setPreferredSize(dimension);
			
			JPanel configPanel = new JPanel();
			JPanel deviceListPanel = new JPanel();
			
			add(configPanel, BorderLayout.NORTH);
			add(deviceListPanel, BorderLayout.CENTER);
			
			////// 配置面板  ////////
		    configPanel.setBorder(BorderFactory.createTitledBorder(Res.string().getOperate()));
			configPanel.setPreferredSize(new Dimension(200, 55));
		    configPanel.setLayout(new FlowLayout());
		    
		    JButton configBtn = new JButton(Res.string().getDeviceConfig());
		    configBtn.setPreferredSize(new Dimension(180, 21));
		    
		    configPanel.add(configBtn);
		    
		    ////// 设备列表面板 ///////
		    deviceListPanel.setBorder(BorderFactory.createTitledBorder(Res.string().getDeviceList()));
		    deviceListPanel.setLayout(new BorderLayout());
		    
		    rootNode = new DefaultMutableTreeNode(); 		    
		    treeModel = new DefaultTreeModel(rootNode);
		    tree = new JTree(treeModel);

		    tree.setBackground(Color.LIGHT_GRAY);
		    tree.setForeground(Color.BLACK);
		    tree.setEditable(false);
		    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);  
		    tree.setRootVisible(false);
			tree.expandPath(new TreePath(rootNode));
			tree.setShowsRootHandles(true);
	    
		    JScrollPane jScrollPane = new JScrollPane(tree);	    
		    deviceListPanel.add(jScrollPane, BorderLayout.CENTER);
		  
		    // 配置设备
		    configBtn.addActionListener(new ActionListener() {			
				@Override
				public void actionPerformed(ActionEvent arg0) {
					DeviceConfigDialog deviceConfigDialog = new DeviceConfigDialog();
					deviceConfigDialog.setVisible(true);		
				}
			});
		    
		    tree.addMouseListener(new MouseListener() {		
				@Override
				public void mouseReleased(MouseEvent e) {

				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					// 右键
					if(SwingUtilities.isRightMouseButton(e)) {
						TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
						tree.setSelectionPath(treePath);
						
						showPopupMenu(e.getComponent(), e.getX(), e.getY());
					}	
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
	
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {

				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					// 双击
					if(e.getClickCount() > 1) {
						if(tree.getLastSelectedPathComponent() instanceof ChannelTreeNode) {
							ChannelTreeNode channelTreeNode = (ChannelTreeNode)tree.getLastSelectedPathComponent();
							
							if(channelTreeNode != realplayChannelTreeNode) {
								startRealplay();
							}	
						}	
					}
				}
			});
		}
	}
	
	/**
	 * 树的右键弹出菜单
	 */
	@SuppressWarnings("static-access")
	private void showPopupMenu(	Component component, int x, int y) {
		JPopupMenu popupMenu = new JPopupMenu();
		
		JMenuItem logoutMenuItem = new JMenuItem(Res.string().getLogout());
		JMenuItem addDeviceMenuItem = new JMenuItem(Res.string().getAddDevice());
		JMenuItem modifyDeviceMenuItem = new JMenuItem(Res.string().getModifyDevice());
		JMenuItem deleteDeviceMenuItem = new JMenuItem(Res.string().getDeleteDevice());
		JMenuItem clearDeviceMenuItem = new JMenuItem(Res.string().getClearDevice());
		JMenuItem importDeviceMenuItem = new JMenuItem(Res.string().getImportDevice());
		JMenuItem exportDeviceMenuItem = new JMenuItem(Res.string().getExportDevice());
		JMenuItem realplayMenuItem = new JMenuItem(Res.string().getStartRealPlay());
		JMenuItem stopRealplayMenuItem = new JMenuItem(Res.string().getStopRealPlay());
		JMenuItem startTalkMenuItem = new JMenuItem(Res.string().getStartTalk());
		JMenuItem stopTalkMenuItem = new JMenuItem(Res.string().getStopTalk());
		JMenuItem captureMenuItem = new JMenuItem(Res.string().getRemoteCapture());
		
		if(tree.getLastSelectedPathComponent() instanceof DeviceTreeNode) {  // 右击设备节点
			DeviceTreeNode deviceTreeNode = (DeviceTreeNode)tree.getLastSelectedPathComponent();	
			
			if(deviceTreeNode.getDeviceInfo().getLoginHandle().longValue() != 0) { // 登陆
				popupMenu.add(logoutMenuItem);
				popupMenu.add(deleteDeviceMenuItem);
				
				if(deviceTreeNode == talkDeviceTreeNode) {  // 对讲
					popupMenu.add(stopTalkMenuItem);
				} else {  // 停止对讲
					popupMenu.add(startTalkMenuItem);
				}			
			} else {  // 未登陆
				popupMenu.add(modifyDeviceMenuItem);
				popupMenu.add(deleteDeviceMenuItem);
			}
		} else if(tree.getLastSelectedPathComponent() instanceof ChannelTreeNode) {  // 右击通道节点
			ChannelTreeNode channelTreeNode = (ChannelTreeNode)tree.getLastSelectedPathComponent();		
			
			if(channelTreeNode == realplayChannelTreeNode) { // 拉流
				popupMenu.add(stopRealplayMenuItem);
			} else {  // 未拉流
				popupMenu.add(realplayMenuItem);
			}
			
			popupMenu.add(captureMenuItem);
		} else { // 右键空白区域
			popupMenu.add(addDeviceMenuItem);
			popupMenu.add(clearDeviceMenuItem);
			popupMenu.add(importDeviceMenuItem);
			popupMenu.add(exportDeviceMenuItem);
			
			logoutBtn.setEnabled(false);
			modifyDeviceBtn.setEnabled(false);
			deleteDeviceBtn.setEnabled(false);
			startRealPlayBtn.setEnabled(false);
			stopRealPlayBtn.setEnabled(false);
			startTalkBtn.setEnabled(false);
			stopTalkBtn.setEnabled(false);
			captureBtn.setEnabled(false);
		}
		
		popupMenu.setDefaultLightWeightPopupEnabled(false);
		popupMenu.show(component, x, y);
		
		// 登出
		logoutMenuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {			
					@Override
					public void run() {
						logout();
					}
				});
			}
		});
		
		// 添加设备
		addDeviceMenuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addDevice();
			}
		});
		
		// 修改设备
		modifyDeviceMenuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				modifyDevice();
			}
		});
		
		// 删除设备
		deleteDeviceMenuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				deleteDevice();
			}
		});
		
		// 清空设备
		clearDeviceMenuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clearDevice();
			}
		});
		
		// 导入设备
		importDeviceMenuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				importDevice();
			}
		});
		
		// 导出设备
		exportDeviceMenuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exportDevice();
			}
		});
		
		// 开始预览
		realplayMenuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {					
					@Override
					public void run() {
						startRealplay();
					}
				});
			}
		});
		
		// 停止预览
		stopRealplayMenuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {			
					@Override
					public void run() {
						stopRealplay();
					}
				});
			}
		});
		
		// 开始对讲
		startTalkMenuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {				
					@Override
					public void run() {
						startTalk();
					}
				});
			}
		});
		
		// 停止对讲
		stopTalkMenuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {		
					@Override
					public void run() {
						stopTalk();
					}
				});
			}
		});
		
		// 远程抓图
		captureMenuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				capture();
			}
		});
	}
	
	/**
	 * 实时预览面板
	 */
	private class RealPlayPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		public RealPlayPanel() {
			BorderEx.set(this, "", 0);
			setLayout(new BorderLayout());
			
			JPanel realplayPanel = new JPanel();
			JPanel picPanel = new JPanel();
			
			add(realplayPanel, BorderLayout.NORTH);
			add(picPanel, BorderLayout.CENTER);
			
			// 实时预览面板
			realplayPanel.setBorder(BorderFactory.createTitledBorder(Res.string().getRealplay()));
			Dimension dimension = new Dimension();
			dimension.height = 250;
			realplayPanel.setPreferredSize(dimension);
			realplayPanel.setLayout(new BorderLayout());
			
			realplayWindowPanel = new Panel();
			realplayWindowPanel.setBackground(Color.GRAY);
			realplayPanel.add(realplayWindowPanel, BorderLayout.CENTER);
			
			// 抓图面板
			picPanel.setBorder(BorderFactory.createTitledBorder(Res.string().getCapturePicture()));
			picPanel.setLayout(new BorderLayout());
			
			capturePanel = new PaintPanel();

			picPanel.add(capturePanel, BorderLayout.CENTER);	
		}
	}
	
	/**
	 * 监听和设备管理面板
	 */
	private class ListenDeviceManagerPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		public ListenDeviceManagerPanel() {
			setLayout(new BorderLayout());
			Dimension dimension = new Dimension();
			dimension.width = 230;
			setPreferredSize(dimension);
			
			JPanel listenPanel = new JPanel();
			JPanel deviceManagerPanel = new JPanel();
			JPanel functionOperatePanel = new JPanel();
			
			add(listenPanel, BorderLayout.NORTH);
			add(deviceManagerPanel, BorderLayout.CENTER);
			add(functionOperatePanel, BorderLayout.SOUTH);
			
			///// 监听面板 /////
			listenPanel.setBorder(BorderFactory.createTitledBorder(Res.string().getAutoRegisterListen()));
			listenPanel.setPreferredSize(new Dimension(200, 110));
			listenPanel.setLayout(new FlowLayout());
			
			JLabel ipLabel = new JLabel(Res.string().getRegisterAddress(), JLabel.CENTER);
			JLabel portLabel = new JLabel(Res.string().getRegisterPort(), JLabel.CENTER);
			
			ipLabel.setPreferredSize(new Dimension(100, 21));
			portLabel.setPreferredSize(new Dimension(100, 21));
			
			ipTextField = new JTextField(getHostAddress());
			portTextField = new JTextField("9500");
			
			ipTextField.setPreferredSize(new Dimension(100, 21));
			portTextField.setPreferredSize(new Dimension(100, 21));
			
			startListenBtn = new JButton(Res.string().getStartListen());
			stopListenBtn = new JButton(Res.string().getStopListen());
			
			startListenBtn.setPreferredSize(new Dimension(105, 21));
			stopListenBtn.setPreferredSize(new Dimension(100, 21));
			stopListenBtn.setEnabled(false);
					
			listenPanel.add(ipLabel);
			listenPanel.add(ipTextField);
			listenPanel.add(portLabel);
			listenPanel.add(portTextField);
			listenPanel.add(startListenBtn);
			listenPanel.add(stopListenBtn);
			
			///// 设备管理  ////////
			deviceManagerPanel.setBorder(BorderFactory.createTitledBorder(Res.string().getDeviceManager()));
			deviceManagerPanel.setLayout(new FlowLayout());
			
			logoutBtn = new JButton(Res.string().getLogout());
			addDeviceBtn = new JButton(Res.string().getAddDevice());
			modifyDeviceBtn = new JButton(Res.string().getModifyDevice());
			deleteDeviceBtn = new JButton(Res.string().getDeleteDevice());
			clearDeviceBtn = new JButton(Res.string().getClearDevice());
			importDeviceBtn = new JButton(Res.string().getImportDevice());
			exportDeviceBtn = new JButton(Res.string().getExportDevice());
			
			logoutBtn.setPreferredSize(new Dimension(210, 21));
			addDeviceBtn.setPreferredSize(new Dimension(210, 21));
			modifyDeviceBtn.setPreferredSize(new Dimension(210, 21));
			deleteDeviceBtn.setPreferredSize(new Dimension(210, 21));
			clearDeviceBtn.setPreferredSize(new Dimension(210, 21));
			importDeviceBtn.setPreferredSize(new Dimension(210, 21));
			exportDeviceBtn.setPreferredSize(new Dimension(210, 21));
			
			logoutBtn.setEnabled(false);
			modifyDeviceBtn.setEnabled(false);
			deleteDeviceBtn.setEnabled(false);
			
			deviceManagerPanel.add(logoutBtn);
			deviceManagerPanel.add(addDeviceBtn);
			deviceManagerPanel.add(modifyDeviceBtn);
			deviceManagerPanel.add(deleteDeviceBtn);
			deviceManagerPanel.add(clearDeviceBtn);
			deviceManagerPanel.add(importDeviceBtn);
			deviceManagerPanel.add(exportDeviceBtn);
			
			///// 基本操作  ////////
			functionOperatePanel.setBorder(BorderFactory.createTitledBorder(Res.string().getFunctionOperate()));
			functionOperatePanel.setLayout(new FlowLayout());
			Dimension dimension1 = new Dimension();
			dimension1.height = 160;
			functionOperatePanel.setPreferredSize(dimension1);
			
			startRealPlayBtn = new JButton(Res.string().getStartRealPlay());
			stopRealPlayBtn = new JButton(Res.string().getStopRealPlay());
			startTalkBtn = new JButton(Res.string().getStartTalk());
			stopTalkBtn = new JButton(Res.string().getStopTalk());
			captureBtn = new JButton(Res.string().getRemoteCapture());
		
			startRealPlayBtn.setPreferredSize(new Dimension(210, 21));
			stopRealPlayBtn.setPreferredSize(new Dimension(210, 21));
			startTalkBtn.setPreferredSize(new Dimension(210, 21));
			stopTalkBtn.setPreferredSize(new Dimension(210, 21));
			captureBtn.setPreferredSize(new Dimension(210, 21));
			
			startRealPlayBtn.setEnabled(false);
			stopRealPlayBtn.setEnabled(false);
			startTalkBtn.setEnabled(false);
			stopTalkBtn.setEnabled(false);
			captureBtn.setEnabled(false);
			
			functionOperatePanel.add(startRealPlayBtn);
			functionOperatePanel.add(stopRealPlayBtn);
			functionOperatePanel.add(startTalkBtn);
			functionOperatePanel.add(stopTalkBtn);
			functionOperatePanel.add(captureBtn);
			
			// 开启监听服务
			startListenBtn.addActionListener(new ActionListener() {		
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(ipTextField.getText().equals("")) {
						JOptionPane.showMessageDialog(null, Res.string().getInput() + Res.string().getRegisterAddress(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					if(portTextField.getText().equals("")) {
						JOptionPane.showMessageDialog(null, Res.string().getInput() + Res.string().getRegisterPort(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					if(AutoRegisterModule.startServer(ipTextField.getText(), 
												      Integer.parseInt(portTextField.getText()), 
												      servicCallback)) {
						startListenBtn.setEnabled(false);
						stopListenBtn.setEnabled(true);
					}
				}
			});
			
			// 停止监听服务
			stopListenBtn.addActionListener(new ActionListener() {		
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(AutoRegisterModule.stopServer()) {
						startListenBtn.setEnabled(true);
						stopListenBtn.setEnabled(false);
					}
				}
			});
			
	    	// 添加设备
			addDeviceBtn.addActionListener(new ActionListener() {			
				@Override
				public void actionPerformed(ActionEvent arg0) {
					addDevice();		
				}
			});
			
		    // 修改设备
			modifyDeviceBtn.addActionListener(new ActionListener() {			
				@Override
				public void actionPerformed(ActionEvent arg0) {
					modifyDevice();
				}
			});	
			
			// 删除单个
			deleteDeviceBtn.addActionListener(new ActionListener() {			
				@Override
				public void actionPerformed(ActionEvent arg0) {	
					deleteDevice();				
				}
			});
			
			// 清空设备
			clearDeviceBtn.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					clearDevice();
				}
			});

			// 导入文件
			importDeviceBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					importDevice();
				}
			});
			
			// 导出文件
			exportDeviceBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					exportDevice();
				}
			});
			
			// 登出
			logoutBtn.addActionListener(new ActionListener() {		
				@Override
				public void actionPerformed(ActionEvent arg0) {
					SwingUtilities.invokeLater(new Runnable() {					
						@Override
						public void run() {
							logout();	
						}
					});
				}
			});
			
			// 开始拉流
			startRealPlayBtn.addActionListener(new ActionListener() {			
				@Override
				public void actionPerformed(ActionEvent arg0) {
					SwingUtilities.invokeLater(new Runnable() {					
						@Override
						public void run() {
							startRealplay();
						}
					});
				}
			});
			
			// 停止拉流
			stopRealPlayBtn.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					SwingUtilities.invokeLater(new Runnable() {					
						@Override
						public void run() {
							stopRealplay();
						}
					});
				}
			});
			
			// 开始对讲
			startTalkBtn.addActionListener(new ActionListener() {		
				@Override
				public void actionPerformed(ActionEvent arg0) {
					SwingUtilities.invokeLater(new Runnable() {					
						@Override
						public void run() {
							startTalk();
						}
					});
				}
			});
			
			// 停止对讲
			stopTalkBtn.addActionListener(new ActionListener() {		
				@Override
				public void actionPerformed(ActionEvent arg0) {
					SwingUtilities.invokeLater(new Runnable() {					
						@Override
						public void run() {
							stopTalk();
						}
					});
				}
			});
			
			// 抓图
			captureBtn.addActionListener(new ActionListener() {		
				@Override
				public void actionPerformed(ActionEvent arg0) {
					capture();
				}
			});
		}
	}
	
	/**
	 * 添加设备
	 */
	private void addDevice() {
		isExist = false;
		
		AddDeviceDialog addDeviceDialog = new AddDeviceDialog();

		addDeviceDialog.addDeviceManagerListener(new DeviceManagerListener() {						
			@Override
			public void onDeviceManager(String deviceId, String username,
										String password) {					
				if(!deviceId.equals("")) {
					// 最大支持导入100个
					if(rootNode.getChildCount() >= 100) {
						JOptionPane.showMessageDialog(null, Res.string().getMaximumSupport(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
						return;
					}
	
					// 判断设备是否存在
					for(int i = 0; i < rootNode.getChildCount(); i++) {
						DeviceTreeNode childNode = (DeviceTreeNode)rootNode.getChildAt(i);
						
						if(deviceId.equals(childNode.getDeviceInfo().getDevcieId())) {
							isExist = true;
							JOptionPane.showMessageDialog(null, Res.string().getAlreadyExisted(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					
					// 不存在，可以添加
					if(!isExist) {
						DEVICE_INFO deviceInfo = new DEVICE_INFO();
						deviceInfo.setDevcieId(deviceId);
						deviceInfo.setUsername(username);
						deviceInfo.setPassword(password);
						
						DeviceTreeNode childNode = new DeviceTreeNode();					
						childNode.setDeviceInfo(deviceInfo);
						childNode.setUserObject(deviceId);
						
						rootNode.add(childNode);
						
						tree.expandPath(new TreePath(rootNode));						
						tree.updateUI();
					    tree.setCellRenderer(treeCellRender);
						
						JOptionPane.showMessageDialog(null, Res.string().getSucceed(), Res.string().getPromptMessage(), JOptionPane.INFORMATION_MESSAGE);
					}
				}				
			}
		});		
		
		addDeviceDialog.setVisible(true);
	}
	
	/**
	 * 修改设备
	 */
	private void modifyDevice() {
		isExist = false;

		// 选中的设备节点
		final DeviceTreeNode deviceTreeNode = (DeviceTreeNode)tree.getLastSelectedPathComponent();
		
		if(deviceTreeNode == null) {
			return;
		}
		
		ModifyDeviceDialog modifyDeviceDialog = new ModifyDeviceDialog(deviceTreeNode.getDeviceInfo().devcieId, deviceTreeNode.getDeviceInfo().username, deviceTreeNode.getDeviceInfo().password);
		
		modifyDeviceDialog.addDeviceManagerListener(new DeviceManagerListener() {						
			@Override
			public void onDeviceManager(String deviceId, String username, String password) {	
				if(deviceTreeNode.getDeviceInfo().getLoginHandle().longValue() != 0) {
					JOptionPane.showMessageDialog(null, Res.string().getDeviceLogined(), Res.string().getErrorMessage(), JOptionPane.INFORMATION_MESSAGE);	
					return;
				}				
				
				if(!deviceId.equals("")) {				
					// 设备ID变化，循环检查，查看是否已存在。    设备ID不变，即修改用户名和密码
					if(!deviceId.equals(deviceTreeNode.getDeviceInfo().devcieId)) {
						// 判断设备是否存在
						for(int i = 0; i < rootNode.getChildCount(); i++) {
							DeviceTreeNode childNode = (DeviceTreeNode)rootNode.getChildAt(i);
							
							if(deviceId.equals(childNode.getDeviceInfo().getDevcieId())) {
								isExist = true;
								JOptionPane.showMessageDialog(null, Res.string().getAlreadyExisted(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
								break;
							}
						}
					}
					
					if(!isExist) {
						// 修改信息
						deviceTreeNode.getDeviceInfo().setDevcieId(deviceId);							
						deviceTreeNode.getDeviceInfo().setUsername(username);
						deviceTreeNode.getDeviceInfo().setPassword(password);			

						deviceTreeNode.setUserObject(deviceId);				
						deviceTreeNode.removeAllChildren();  
						
						tree.updateUI();	
			
						JOptionPane.showMessageDialog(null, Res.string().getSucceed(), Res.string().getPromptMessage(), JOptionPane.INFORMATION_MESSAGE);	
					}
				}
			}
		});
		
		modifyDeviceDialog.setVisible(true);
	}
	
	/**
	 * 删除单个设备
	 */
	private void deleteDevice() {
		// 判断是否选中
		DeviceTreeNode deviceTreeNode = (DeviceTreeNode)tree.getLastSelectedPathComponent();

		if(deviceTreeNode == null) {
			return;
		}
		
		// 停止当前登录设备的对讲
		if(deviceTreeNode == talkDeviceTreeNode) {
			AutoRegisterModule.stopTalk(AutoRegisterModule.m_hTalkHandle);					

			talkDeviceTreeNode = null;
		}
		
		// 停止当前登录设备的拉流
		for(int i = 0; i < deviceTreeNode.getChildCount(); i++) {
			ChannelTreeNode channelTreeNode = (ChannelTreeNode)deviceTreeNode.getChildAt(i);
			if(channelTreeNode == realplayChannelTreeNode) {
				AutoRegisterModule.stopRealPlay(realplayHandle);
				realplayWindowPanel.repaint();

				realplayChannelTreeNode = null;
				
				break;
			}	
		}	
		
		// 登出
		if(deviceTreeNode.getDeviceInfo().getLoginHandle().longValue() != 0) {
			AutoRegisterModule.logout(deviceTreeNode.getDeviceInfo().getLoginHandle());

			logoutBtn.setEnabled(false);
			modifyDeviceBtn.setEnabled(true);
		}
				
		deviceTreeNode.setUserObject(deviceTreeNode.getDeviceInfo().getDevcieId());				
		
		treeModel.removeNodeFromParent(deviceTreeNode);
		tree.updateUI();
		
		modifyDeviceBtn.setEnabled(false);
		deleteDeviceBtn.setEnabled(false);
	}
	
	/**
	 * 清空设备
	 */
	private void clearDevice() {
		// 停止拉流
		if(realplayHandle.longValue() != 0) {
			AutoRegisterModule.stopRealPlay(realplayHandle);
			realplayWindowPanel.repaint();
			
			realplayChannelTreeNode = null;
		}
		
		// 停止对讲
		if(AutoRegisterModule.m_hTalkHandle.longValue() != 0) {
			AutoRegisterModule.stopTalk(AutoRegisterModule.m_hTalkHandle);	

			talkDeviceTreeNode = null;					
		}	
		
		// 登出所有设备
		for(int i = 0; i < rootNode.getChildCount(); i++) {
			DeviceTreeNode deviceTreeNode = (DeviceTreeNode)rootNode.getChildAt(i);
			if(deviceTreeNode.getDeviceInfo().getLoginHandle().longValue() != 0) {
				AutoRegisterModule.logout(deviceTreeNode.getDeviceInfo().getLoginHandle());
			}
		}
		
		capturePanel.setOpaque(true);
		capturePanel.repaint();	

		rootNode.removeAllChildren();
		tree.updateUI();	
		
		logoutBtn.setEnabled(false);
		modifyDeviceBtn.setEnabled(false);
		deleteDeviceBtn.setEnabled(false);
		startRealPlayBtn.setEnabled(false);
		stopRealPlayBtn.setEnabled(false);
		startTalkBtn.setEnabled(false);
		stopTalkBtn.setEnabled(false);
		captureBtn.setEnabled(false);
	}
	
	/**
	 * 导入文件
	 */
	private void importDevice() {
		jfc = new JFileChooser("./");
		jfc.setMultiSelectionEnabled(false);   //不可以拖选多个文件
		jfc.setAcceptAllFileFilterUsed(false); //关掉显示所有
		jfc.setDialogTitle(Res.string().getImportDevice());
	
		//添加过滤器
		jfc.addChoosableFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if(f.getName().toLowerCase().endsWith("csv")
						|| f.isDirectory()) {
					return true;
				}
				return false;
			}
			public String getDescription() {
				return ".csv";
			}
		});
		
        if(jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {	
			String path = jfc.getSelectedFile().getAbsolutePath();
        	
        	if(!path.endsWith("csv")) {
        		path += ".csv";
        	}
			importFile(path, "GB2312");	
        } 
	}
	
	/**
	 * 导出文件
	 */
	private void exportDevice() {
		jfc = new JFileChooser("./");
		jfc.setMultiSelectionEnabled(false);   //不可以拖选多个文件
		jfc.setAcceptAllFileFilterUsed(false); //关掉显示所有
		jfc.setDialogTitle(Res.string().getExportDevice());

		//添加过滤器
		jfc.addChoosableFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if(f.getName().toLowerCase().endsWith("csv")
						|| f.isDirectory()) {
					return true;
				}
				return false;
			}
			public String getDescription() {
				return ".csv";
			}
		});
								
        if(jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {

        	String path = jfc.getSelectedFile().toString();
        	if(!path.endsWith("csv")) {
        		path += ".csv";
        	}
        	System.out.println(path);
        	File file = new File(path);
        	
    		if(file.exists()) {  // 已存在文件
    			int result = JOptionPane.showConfirmDialog(null, Res.string().getWhetherNoToCover(), Res.string().getPromptMessage(), JOptionPane.YES_NO_OPTION);
    			if(result == JOptionPane.YES_OPTION) {  // 覆盖
    				if(!file.renameTo(file)) {  // 被使用
    					JOptionPane.showMessageDialog(null, Res.string().getFileOpened(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
    				} else {   // 未被使用
    					exportFile(file);	
    				}			
    			} 
    		} else {  // 新建文件			    			
    			exportFile(file);		
    		}
        } 			
	}
	
	/**
	 * 登出设备功能
	 */
	private void logout() {
		// 选中的设备节点
		DeviceTreeNode deviceTreeNode = (DeviceTreeNode)tree.getLastSelectedPathComponent();
		
		if(deviceTreeNode == null) {
			return;
		}
		
		// 停止当前登录设备的对讲
		if(deviceTreeNode == talkDeviceTreeNode) {
			AutoRegisterModule.stopTalk(AutoRegisterModule.m_hTalkHandle);					

			talkDeviceTreeNode = null;
		}
		
		// 停止当前登录设备的拉流
		for(int i = 0; i < deviceTreeNode.getChildCount(); i++) {
			ChannelTreeNode channelTreeNode = (ChannelTreeNode)deviceTreeNode.getChildAt(i);
			if(channelTreeNode == realplayChannelTreeNode) {
				AutoRegisterModule.stopRealPlay(realplayHandle);
				realplayWindowPanel.repaint();

				realplayChannelTreeNode = null;
				
				break;
			}	
		}	
		
		// 登出
		if(deviceTreeNode.getDeviceInfo().getLoginHandle().longValue() != 0) {
			AutoRegisterModule.logout(deviceTreeNode.getDeviceInfo().getLoginHandle());

			logoutBtn.setEnabled(false);
			modifyDeviceBtn.setEnabled(true);
		}
				
		deviceTreeNode.setUserObject(deviceTreeNode.getDeviceInfo().getDevcieId());				
		deviceTreeNode.removeAllChildren();  
		
		tree.updateUI();
	}
	
	/**
	 * 开始拉流功能
	 */
	private void startRealplay() {
		// 选中的通道节点
		ChannelTreeNode channelTreeNode = (ChannelTreeNode)tree.getLastSelectedPathComponent();
		
		if(channelTreeNode == null) {
			return;
		}
		
		// 通道节点对应的设备节点
		DeviceTreeNode deviceTreeNode = (DeviceTreeNode)channelTreeNode.getParent();
		
		// 判断所有设备有没有拉流
		if(realplayHandle.longValue() != 0) {
			AutoRegisterModule.stopRealPlay(realplayHandle);					
			realplayWindowPanel.repaint();

			realplayChannelTreeNode = null;
		}	

		// 选中通道拉流
		realplayHandle = AutoRegisterModule.startRealPlay(deviceTreeNode.getDeviceInfo().getLoginHandle(), channelTreeNode.getChn() - 1, 0, realplayWindowPanel);
	
		if(realplayHandle.longValue() != 0) {
			startRealPlayBtn.setEnabled(false);
			stopRealPlayBtn.setEnabled(true);
			
			realplayChannelTreeNode = channelTreeNode;					
		} else {
			JOptionPane.showMessageDialog(null, Res.string().getRealplay() + Res.string().getFailed() + ", " + ToolKits.getErrorCodeShow(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
		}
		
		tree.updateUI();
	}
	
	/**
	 * 停止拉流功能
	 */
	private void stopRealplay() {
		if(realplayHandle.longValue() != 0) {
			AutoRegisterModule.stopRealPlay(realplayHandle);
			realplayWindowPanel.repaint();
			
			realplayChannelTreeNode = null;

			startRealPlayBtn.setEnabled(true);
			stopRealPlayBtn.setEnabled(false);
		}
		tree.updateUI();
	}
	
	/**
	 * 开始对讲
	 */
	private void startTalk() {
		// 选中的设备节点
		DeviceTreeNode deviceTreeNode = (DeviceTreeNode)tree.getLastSelectedPathComponent();
		
		if(deviceTreeNode == null) {
			return;
		}
		
		// 判断所有设备有没有对讲
		if(AutoRegisterModule.m_hTalkHandle.longValue() != 0) {
			AutoRegisterModule.stopTalk(AutoRegisterModule.m_hTalkHandle);					
			
			talkDeviceTreeNode = null;
		}	

		// 选中设备对讲
		if(AutoRegisterModule.startTalk(deviceTreeNode.getDeviceInfo().getLoginHandle())) {		
			startTalkBtn.setEnabled(false);
			stopTalkBtn.setEnabled(true);
			
			talkDeviceTreeNode = deviceTreeNode;				
		} else {
			JOptionPane.showMessageDialog(null, Res.string().getTalk() + Res.string().getFailed() + ", " +  ToolKits.getErrorCodeShow(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
		}
		
		tree.updateUI();
	}
	
	/**
	 * 停止对讲
	 */
	private void stopTalk() {
		// 判断设备的通道有没有对讲
		if(AutoRegisterModule.m_hTalkHandle.longValue() != 0) {
			AutoRegisterModule.stopTalk(AutoRegisterModule.m_hTalkHandle);					

			talkDeviceTreeNode = null;

			startTalkBtn.setEnabled(true);
			stopTalkBtn.setEnabled(false);
		}	

		tree.updateUI();
	}
	
	/**
	 * 抓图功能
	 */
	private void capture() {
		// 选中的通道节点
		ChannelTreeNode channelTreeNode = (ChannelTreeNode)tree.getLastSelectedPathComponent();
		
		if(channelTreeNode == null) {
			return;
		}
		
		// 通道节点对应的设备节点
		DeviceTreeNode deviceTreeNode = (DeviceTreeNode)channelTreeNode.getParent();
		
		AutoRegisterModule.snapPicture(deviceTreeNode.getDeviceInfo().getLoginHandle(), channelTreeNode.getChn() - 1);
	}
	
	/**
	 * 侦听服务器回调函数
	 */
	public class ServiceCB implements fServiceCallBack {
		@Override
		public int invoke(LLong lHandle, final String pIp, final int wPort,
				int lCommand, Pointer pParam, int dwParamLen,
				Pointer dwUserData) {

			// 将 pParam 转化为序列号
			byte[] buffer = new byte[dwParamLen];
			pParam.read(0, buffer, 0, dwParamLen);
			String deviceId = "";
			try {
				deviceId = new String(buffer, "GBK").trim();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			System.out.printf("Register Device Info [Device address %s][port %s][DeviceID %s] \n", pIp, wPort, deviceId);
			switch(lCommand) {
				case EM_LISTEN_TYPE.NET_DVR_DISCONNECT: {  // 验证期间设备断线回调
					for(int i = 0; i < rootNode.getChildCount(); i++) {
						if(deviceId.equals(((DeviceTreeNode)rootNode.getChildAt(i)).getDeviceInfo().getDevcieId())) {							
							DeviceTreeNode deviceTreeNode = (DeviceTreeNode)rootNode.getChildAt(i);				
							
							deviceTreeNode.getDeviceInfo().setDeviceIp("");
							deviceTreeNode.getDeviceInfo().setDevicePort(0);
							
							break;
						}
					}	
					
					break;
				}			
				case EM_LISTEN_TYPE.NET_DVR_SERIAL_RETURN: { // 设备注册携带序列号
					for(int i = 0; i < rootNode.getChildCount(); i++) {
						if(deviceId.equals(((DeviceTreeNode)rootNode.getChildAt(i)).getDeviceInfo().getDevcieId())) {							
							final DeviceTreeNode deviceTreeNode = (DeviceTreeNode)rootNode.getChildAt(i);				
							
							deviceTreeNode.getDeviceInfo().setDeviceIp(pIp);
							deviceTreeNode.getDeviceInfo().setDevicePort(wPort);
							
							try {
								Thread.sleep(20);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							
							new SwingWorker<LLong, String>() {					
								@Override
								protected LLong doInBackground() {
									return login(deviceTreeNode);						
								}
								
								@Override
								protected void done() {
									try {
										if(get() == null) {
											return;
										}
										
										if(get().longValue() != 0) {
//											deviceTreeNode.getDeviceInfo().setDeviceIp(pIp);
//											deviceTreeNode.getDeviceInfo().setDevicePort(wPort);
											deviceTreeNode.getDeviceInfo().setLoginHandle(get());
											
											for(int i = 0; i < AutoRegisterModule.m_stDeviceInfo.byChanNum; i++) {
												ChannelTreeNode chnNode = new ChannelTreeNode();
												chnNode.setChn(i + 1);
												chnNode.setUserObject(Res.string().getChannel() + " " + String.valueOf(i + 1));
												
												deviceTreeNode.add(chnNode);
											}
											
											deviceTreeNode.setUserObject(deviceTreeNode.getDeviceInfo().getDevcieId() + " (" + deviceTreeNode.getDeviceInfo().getDeviceIp() + ")");	
											tree.expandPath(new TreePath(rootNode));
											tree.setCellRenderer(treeCellRender);	
											tree.updateUI();
										}		
									} catch (InterruptedException e) {
										e.printStackTrace();
									} catch (ExecutionException e) {
										e.printStackTrace();
									}					
								}
							}.execute();
							
							break;
						}
					}	
					
					break;
				}
				default:
					break;
			
			}			

			return 0;
		}
	}
	
	/**
	 * 登录设备
	 * @param deviceIp 登录设备IP
	 * @param port 登录设备端口号
	 * @param deviceTreeNode 登录设备设备节点
	 */
	private LLong login(DeviceTreeNode deviceTreeNode) {				
		// 判断设备是否登录
		if(deviceTreeNode.getDeviceInfo().getLoginHandle().longValue() != 0) {
			return null;
		}
		
		LLong loginHandleLong = AutoRegisterModule.login(deviceTreeNode.getDeviceInfo().getDeviceIp(), 
														 deviceTreeNode.getDeviceInfo().getDevicePort(), 
														 deviceTreeNode.getDeviceInfo().getUsername(), 
														 deviceTreeNode.getDeviceInfo().getPassword(), 
														 deviceTreeNode.getDeviceInfo().getDevcieId());
		if(loginHandleLong.longValue() != 0) {
			System.out.printf("Login Success [Device IP %s][port %d][DeviceID %s]\n", deviceTreeNode.getDeviceInfo().getDeviceIp(), 
					deviceTreeNode.getDeviceInfo().getDevicePort(), deviceTreeNode.getDeviceInfo().getDevcieId());
		}  else {
			System.err.printf("Login Failed[Device IP %s] [Port %d][DeviceID %s] %s", deviceTreeNode.getDeviceInfo().getDeviceIp(), 
					deviceTreeNode.getDeviceInfo().getDevicePort(), deviceTreeNode.getDeviceInfo().getDevcieId(), ToolKits.getErrorCodePrint());
		}
		
		return loginHandleLong;
	} 

	/**
	 * 设备节点
	 */
	private class DeviceTreeNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = 1L;
		
		private DEVICE_INFO deviceInfo; // 信息
		
		public DeviceTreeNode() {
			
		}
		
		@Override
		public Object getUserObject() {
			return super.getUserObject();
		}

		@Override
		public void setUserObject(Object arg0) {
			super.setUserObject(arg0);
		}

		public DEVICE_INFO getDeviceInfo() {
			return deviceInfo;
		}
		
		public void setDeviceInfo(DEVICE_INFO deviceInfo) {
			this.deviceInfo = deviceInfo;
		}
	}
	
	/**
	 * 通道节点
	 */
	private class ChannelTreeNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = 1L;

		private int nChn = 0;
		
		public ChannelTreeNode() {
			
		}
		
		public int getChn() {
			return nChn;
		}
		public void setChn(int nChn) {
			this.nChn = nChn;
		}
	}
	
	/**
	 * 设备信息
	 */
	private class DEVICE_INFO {
		private String devcieId = "";
		private String username = "";
		private String password = "";
		private String deviceIp = "";
		private int port = 0;
		private LLong loginHandle = new LLong(0);
		
		public String getDevcieId() {
			return devcieId;
		}
		public void setDevcieId(String devcieId) {
			this.devcieId = devcieId;
		}
		
		public String getDeviceIp() {
			return deviceIp;
		}
		public void setDeviceIp(String deviceIp) {
			this.deviceIp = deviceIp;
		}
		
		public int getDevicePort() {
			return port;
		}
		public void setDevicePort(int port) {
			this.port = port;
		}
		
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		
		public LLong getLoginHandle() {
			return loginHandle;
		}
		public void setLoginHandle(LLong loginHandle) {
			this.loginHandle = loginHandle;
		}	
	}

	/**
	 * 自定义树的渲染器
	 */
	private class TreeCellRender extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, 
				boolean expanded, boolean leaf, int row, boolean hasFocus) {
			
			super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		
			if(value instanceof DeviceTreeNode) {
				setIcon(new ImageIcon("./libs/device.png"));
				
				DeviceTreeNode deviceTreeNode = (DeviceTreeNode)value;
		
				// 设备节点的字体颜色
				if(deviceTreeNode.getDeviceInfo().getLoginHandle().longValue() != 0) {					
					if(deviceTreeNode == talkDeviceTreeNode) { 
					    setForeground(Color.RED);
					} else {
						setForeground(Color.GREEN);
					}
				} else {
					setForeground(Color.BLACK);
				}	
				
				if(selected) {	
					// 如果设备节点被选中，拉流按钮使能
					startRealPlayBtn.setEnabled(false);
					stopRealPlayBtn.setEnabled(false);
					captureBtn.setEnabled(false);
					deleteDeviceBtn.setEnabled(true);
					
					// 设备节点被选中，登录时的使能
					if(deviceTreeNode.getDeviceInfo().getLoginHandle().longValue() != 0) {
						logoutBtn.setEnabled(true);
						modifyDeviceBtn.setEnabled(false);
									
						if(deviceTreeNode == talkDeviceTreeNode) { 
							startTalkBtn.setEnabled(false);
							stopTalkBtn.setEnabled(true);						
						} else {
							startTalkBtn.setEnabled(true);
							stopTalkBtn.setEnabled(false);
						}
					} else {	// 登出时的使能					
						logoutBtn.setEnabled(false);
						modifyDeviceBtn.setEnabled(true);
						startTalkBtn.setEnabled(false);
						stopTalkBtn.setEnabled(false);
					}	
				} 
			} else if(value instanceof ChannelTreeNode) {
				setIcon(new ImageIcon("./libs/camera.png"));
				
				ChannelTreeNode channelTreeNode = (ChannelTreeNode)value;
				
				// 设置通道节点的字体颜色	
				if(channelTreeNode == realplayChannelTreeNode) {
					setForeground(Color.RED);
				} else {
					setForeground(Color.BLACK);					
				}

				if(selected) {		
					// 如果通道节点被选中，登出置灰
					logoutBtn.setEnabled(false);
					captureBtn.setEnabled(true);
					modifyDeviceBtn.setEnabled(false);
					deleteDeviceBtn.setEnabled(false);
					startTalkBtn.setEnabled(false);
					stopTalkBtn.setEnabled(false);	
					
					if(channelTreeNode == realplayChannelTreeNode) {
						startRealPlayBtn.setEnabled(false);
						stopRealPlayBtn.setEnabled(true);
					} else {
						startRealPlayBtn.setEnabled(true);
						stopRealPlayBtn.setEnabled(false);					
					}
				} 
			}

			return this;
		}
	}
	
	/**
	 * 导入csv文件
	 */
	private void importFile(String filePath, String encoding) {    	
		File file = new File(filePath);

		if(!file.exists()) {
			JOptionPane.showMessageDialog(null, Res.string().getFileNotExist(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
			return;
		}
		
  		FileInputStream input = null;
		InputStreamReader reader = null;
		BufferedReader bReader = null;
		
    	try {
    		input = new FileInputStream(file);

    		if(encoding == null) {
    			reader = new InputStreamReader(input);
    		} else {
    			reader = new InputStreamReader(input, encoding);
    		}
    		bReader = new BufferedReader(reader);
    		
    		// 头标题
    		String line = bReader.readLine();
        	String everyLine = "";           	
        	
    		while((line = bReader.readLine()) != null) {
    			everyLine = line;
    			
    			// 最大支持导入100个
    			if(rootNode.getChildCount() >= 100) {
    				return;
    			}
    			
				String[] infos = everyLine.split(",");

				isExist = false;
				if(!infos[0].equals("")) {				
					// 判断是否存在
					for(int i = 0; i < rootNode.getChildCount(); i++) {   					
						DeviceTreeNode childNode = (DeviceTreeNode)rootNode.getChildAt(i);					
						if(infos[0].equals(childNode.getDeviceInfo().getDevcieId())) {
							isExist = true;										
							break;
						}
					}
					
					// 不存在, 可以添加
					if(!isExist) {
	    	   			final DEVICE_INFO deviceInfo = new DEVICE_INFO();
	    				deviceInfo.setDevcieId(infos[0]);
	    				if(infos.length == 1) {
	    					deviceInfo.setUsername("");
	    					deviceInfo.setPassword("");
	    				} else if(infos.length == 2) {
	    					deviceInfo.setUsername(infos[1]);
	    					deviceInfo.setPassword("");
	    				} else if(infos.length == 3){
	    					deviceInfo.setUsername(infos[1]);
	        				deviceInfo.setPassword(infos[2]);
	    				}       			
	    				
						DeviceTreeNode childNode = new DeviceTreeNode();					
	    				childNode.setDeviceInfo(deviceInfo);
	    				childNode.setUserObject(infos[0]);
	    				
	    				rootNode.add(childNode);
	    				
	    				tree.expandPath(new TreePath(rootNode));
	    				tree.updateUI();
					}
				}				
    		} 
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (Exception e) {
			e.printStackTrace();
		} finally {
    		if(null != bReader) {
    			try {
    				bReader.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
            tree.setCellRenderer(treeCellRender);
    		JOptionPane.showMessageDialog(null, Res.string().getImportCompletion(), Res.string().getPromptMessage(), JOptionPane.INFORMATION_MESSAGE);
    	}
	}

	/**
	 * 导出csv文件
	 */
	private void exportFile(File file) {
		FileOutputStream output = null;
		OutputStreamWriter osWriter = null;
		BufferedWriter bWriter = null;
		
		try {		
			output = new FileOutputStream(file);
			osWriter = new OutputStreamWriter(output, "GB2312");	
			bWriter = new BufferedWriter(osWriter);
			
			// 写头标题
			bWriter.write("DeviceId" + "," + "UserName" + "," + "Password");
			bWriter.newLine();
			
			// 写内容
			for(int i = 0; i < rootNode.getChildCount(); i++) {   					
				DeviceTreeNode childNode = (DeviceTreeNode)rootNode.getChildAt(i);	
				String deviceId = childNode.getDeviceInfo().getDevcieId();
				String username = childNode.getDeviceInfo().getUsername();
				String password = childNode.getDeviceInfo().getPassword();
				
				bWriter.write(deviceId + "," + username + "," + password);
				bWriter.newLine();
			}				
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();		
		} finally {
			if(null != bWriter) {
				try {
					bWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			JOptionPane.showMessageDialog(null, Res.string().getExportCompletion(), Res.string().getPromptMessage(), JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * 抓图回调函数
	 */
	public class CaptureReceiveCB implements NetSDKLib.fSnapRev{
		BufferedImage bufferedImage = null;
		public void invoke( LLong lLoginID, Pointer pBuf, int RevLen, int EncodeType, int CmdSerial, Pointer dwUser) {	
			if(pBuf != null && RevLen > 0) {			        
				String strFileName = SavePath.getSavePath().getSaveCapturePath(); 

				System.out.println("strFileName = " + strFileName);

				byte[] buf = pBuf.getByteArray(0, RevLen);
				ByteArrayInputStream byteArrInput = new ByteArrayInputStream(buf);
				try {
					bufferedImage = ImageIO.read(byteArrInput);
					if(bufferedImage == null) {
						return;
					}
					ImageIO.write(bufferedImage, "jpg", new File(strFileName));	
				} catch (IOException e) {
					e.printStackTrace();
				}	
				
				// 界面显示抓图	 
				SwingUtilities.invokeLater(new Runnable() {	
					@Override
					public void run() {			
						capturePanel.setOpaque(false);
						capturePanel.setImage(bufferedImage);
						capturePanel.repaint();				
					}
				});
			}
		}
	}
	
	/**
	 * 获取本地地址
	 * @return
	 */
	private String getHostAddress() {
		String address = "";
		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			address = inetAddress.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		return address;
	}
	
    private DefaultMutableTreeNode rootNode; 	 
    private DefaultTreeModel treeModel;
    private JTree tree;
	
    private Panel realplayWindowPanel;
    private PaintPanel capturePanel;
    
	private JTextField ipTextField;
	private JTextField portTextField;
	private JButton startListenBtn;
	private JButton stopListenBtn;	
	private JButton addDeviceBtn;
	private JButton modifyDeviceBtn;
	private JButton deleteDeviceBtn;
	private JButton clearDeviceBtn;
	private JButton importDeviceBtn;
	private JButton exportDeviceBtn;
	private JButton logoutBtn;

	private JButton startRealPlayBtn;
	private JButton stopRealPlayBtn;
	private JButton startTalkBtn;
	private JButton stopTalkBtn;
	private JButton captureBtn;
	
	private JFileChooser jfc;
}

public class AutoRegister {  
	public static void main(String[] args) {	
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				AutoRegisterFrame demo = new AutoRegisterFrame();
				demo.setVisible(true);
			}
		});		
	}
};
