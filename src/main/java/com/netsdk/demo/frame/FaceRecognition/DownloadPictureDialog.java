package main.java.com.netsdk.demo.frame.FaceRecognition;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import main.java.com.netsdk.common.PaintPanel;
import main.java.com.netsdk.common.Res;
import main.java.com.netsdk.demo.module.FaceRecognitionModule;
import main.java.com.netsdk.lib.ToolKits;

public class DownloadPictureDialog extends JDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DownloadPictureDialog() {
	    setTitle("下载图片");
	    setLayout(new BorderLayout());
	    setModal(true);  
	    pack();
	    setSize(380, 500);
	    setResizable(false);
	    setLocationRelativeTo(null);
	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);   // 释放窗体
	    
	    JLabel picPathLabel = new JLabel("图片路径:", JLabel.CENTER);
	    picPathTextField = new JTextField();
	    downloadBth = new JButton("下载图片");
	    
	    picPathTextField.setPreferredSize(new Dimension(180, 20));
	    downloadBth.setPreferredSize(new Dimension(80, 20));
	    
	    paintPanel = new PaintPanel();
	    paintPanel.setPreferredSize(new Dimension(360, 450));
	    
	    setLayout(new FlowLayout());
	    add(picPathLabel);
	    add(picPathTextField);
	    add(downloadBth);
	    add(paintPanel);
	    
	    downloadBth.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {				
					@Override
					public void run() {
						paintPanel.setOpaque(true);
						paintPanel.repaint();
						downloadBth.setEnabled(false);
					}
				});
				
				downloadPicture();
			}
		});
	    
	    addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent e) {
	    		dispose();
	    	}
	    });
	}
	
	private void downloadPicture() {
		new SwingWorker<Boolean, Integer>() {	
			String szFileName = "";   		       // 要下载的图片路径
			String pszFileDst = "";    			   // 保存图片的路径
			
			@Override
			protected Boolean doInBackground() {
				if(picPathTextField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "请输入图片地址", Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				szFileName = picPathTextField.getText();    
				pszFileDst = "./person.jpg";    			  
				
				File file = new File(pszFileDst);
				if(file.exists()) {
					file.delete();
				}
				
				if(!FaceRecognitionModule.downloadPersonPic(szFileName, pszFileDst)) {
					return false;
				}

				return true;
			}	
			
			@Override
			protected void done() {
				if(picPathTextField.getText().isEmpty()) {
					return;
				}
				
				try {
					if(get()) {				
						// 下载成功后，面板上显示下载的图片
						ToolKits.openPictureFile(pszFileDst, paintPanel);	
					} else {
						JOptionPane.showMessageDialog(null, ToolKits.getErrorCodeShow(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
					}
				} catch (HeadlessException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				downloadBth.setEnabled(true);
			}
		}.execute();
	}
	
	private JTextField picPathTextField;
	private PaintPanel paintPanel;
	private JButton downloadBth;
}
