package jp.co.css.TTSA;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import jp.co.css.COMMON.SearchCode;
import jp.co.css.base.AppConfig;
import jp.co.css.base.TFrame;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jp.co.css.webpos.common.message.MessageBoxValue;
import jp.co.css.webpos.common.gui.TLabel;
import jp.co.css.webpos.common.gui.TNumericField;
import jp.co.css.webpos.common.gui.TTextField;

import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

public class Ttsa0200 extends TFrame implements MessageBoxValue {

	private static final long serialVersionUID = 1L;

	//パネル　KEY
	private JPanel jPanelKey = null;	
	private TLabel lblTenpo = null;
	private JLabel TenpoWaku = null;
	public SearchCode scodeTenpo = null;

	// FTP情報
	private JPanel jFtpInfo = null;	
	
	private TLabel tlblFtpPortNo = null;
	private JLabel lblFtpPortNoWaku = null;
	public TNumericField txtFtpPortNo = null;
	
	private TLabel tlblFtpHostAddress = null;
	private JLabel lblFtpHostAddressWaku = null;
	public TTextField txtFtpHostAddress = null;
	
	private TLabel tlblFtpUserName = null;
	private JLabel lblFtpUserNameWaku = null;
	public TTextField txtFtpUserName = null;
	
	private TLabel tlblFtpPassword = null;
	private JLabel lblFtpPasswordWaku = null;
	public TTextField txtFtpPassword = null;
	
	private TLabel tlblFtpFolder = null;
	private JLabel lblFtpFolderWaku = null;
	public TTextField txtFtpFolder = null;
	
	/**
	 * This is the default constructor
	 */
	public Ttsa0200(AppConfig appConfig) {
		super(appConfig);
		setJMainPanel();		
		this.setTitle("通販設定　TTSA0200");
	}

	/**
	 * This method initializes jMainPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private void setJMainPanel() {
		lblTitle.setText("通販設定");
		jMainPanel.add(getjPanelKey(), null);
		jMainPanel.add(getjPanelKihon(), null);
	}

	/**
	 * This method initializes jPanelKey	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getjPanelKey() {
		if (jPanelKey == null) {
			
			TenpoWaku = new JLabel();
			TenpoWaku.setText("");
			TenpoWaku.setBounds(new Rectangle(140, 20, 281, 31));
			TenpoWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			lblTenpo = new TLabel();
			lblTenpo.setBounds(new Rectangle(15, 20, 126, 31));
			lblTenpo.setText("注文取込店舗");
			scodeTenpo = new SearchCode();
			scodeTenpo.setBounds(new Rectangle(143, 23, 275, 25));
			scodeTenpo.setTitle("注文取込店舗選択");
			
			jPanelKey = new JPanel();
			jPanelKey.setLayout(null);
			jPanelKey.setBounds(new Rectangle(25, 75, 440, 66));
			jPanelKey.setBorder(BorderFactory.createTitledBorder(null, "<<基本情報>>", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));
			jPanelKey.add(lblTenpo, null);
			jPanelKey.add(TenpoWaku, null);
			jPanelKey.add(scodeTenpo, null);
		}
		return jPanelKey;
	}
	
	/**
	 * This method initializes jPanelKihon	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getjPanelKihon() {
		if (jFtpInfo == null) {
			
			tlblFtpPortNo = new TLabel();
			tlblFtpPortNo.setText("FTPポート番号");
			tlblFtpPortNo.setBounds(new Rectangle(15, 30, 121, 31));
			lblFtpPortNoWaku = new JLabel();
			lblFtpPortNoWaku.setText("");
			lblFtpPortNoWaku.setBounds(new Rectangle(135, 30, 66, 31));
			lblFtpPortNoWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

			txtFtpPortNo = new TNumericField();
			txtFtpPortNo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtFtpPortNo.setText("21");
			txtFtpPortNo.setHorizontalAlignment(JTextField.RIGHT);
			txtFtpPortNo.setMaxLength(4);
			txtFtpPortNo.setNumericFormat("");
			txtFtpPortNo.setBounds(new Rectangle(138, 33, 60, 25));
			txtFtpPortNo.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 18));
			
			tlblFtpHostAddress = new TLabel();
			tlblFtpHostAddress.setText("FTPホストアドレス");
			tlblFtpHostAddress.setBounds(new Rectangle(15, 65, 121, 31));
			lblFtpHostAddressWaku = new JLabel();
			lblFtpHostAddressWaku.setText("");
			lblFtpHostAddressWaku.setBounds(new Rectangle(135, 65, 280, 31));
			lblFtpHostAddressWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

			txtFtpHostAddress = new TTextField();
			txtFtpHostAddress.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtFtpHostAddress.setText("");
			txtFtpHostAddress.setHorizontalAlignment(JTextField.LEFT);
			txtFtpHostAddress.setMaxLength(200);
			txtFtpHostAddress.setBounds(new Rectangle(138, 68, 274, 25));
			txtFtpHostAddress.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 18));
			
			tlblFtpUserName = new TLabel();
			tlblFtpUserName.setText("FTPユーザ名");
			tlblFtpUserName.setBounds(new Rectangle(15, 100, 121, 31));
			lblFtpUserNameWaku = new JLabel();
			lblFtpUserNameWaku.setText("");
			lblFtpUserNameWaku.setBounds(new Rectangle(135, 100, 180, 31));
			lblFtpUserNameWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			
			txtFtpUserName = new TTextField();
			txtFtpUserName.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtFtpUserName.setText("");
			txtFtpUserName.setHorizontalAlignment(JTextField.LEFT);
			txtFtpUserName.setMaxLength(60);
			txtFtpUserName.setBounds(new Rectangle(138, 103, 174, 25));
			txtFtpUserName.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 18));
	
			tlblFtpPassword = new TLabel();
			tlblFtpPassword.setText("FTPパスワード");
			tlblFtpPassword.setBounds(new Rectangle(15, 135, 121, 31));
			lblFtpPasswordWaku = new JLabel();
			lblFtpPasswordWaku.setText("");
			lblFtpPasswordWaku.setBounds(new Rectangle(135, 135, 180, 31));
			lblFtpPasswordWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			
			txtFtpPassword = new TTextField();
			txtFtpPassword.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtFtpPassword.setText("");
			txtFtpPassword.setHorizontalAlignment(JTextField.LEFT);
			txtFtpPassword.setMaxLength(60);
			txtFtpPassword.setBounds(new Rectangle(138, 138, 174, 25));
			txtFtpPassword.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 18));
	
			tlblFtpFolder = new TLabel();
			tlblFtpFolder.setText("FTPフォルダ");
			tlblFtpFolder.setBounds(new Rectangle(15, 170, 121, 31));
			lblFtpFolderWaku = new JLabel();
			lblFtpFolderWaku.setText("");
			lblFtpFolderWaku.setBounds(new Rectangle(135, 170, 280, 31));
			lblFtpFolderWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			
			txtFtpFolder = new TTextField();
			txtFtpFolder.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtFtpFolder.setText("");
			txtFtpFolder.setHorizontalAlignment(JTextField.LEFT);
			txtFtpFolder.setMaxLength(200);
			txtFtpFolder.setBounds(new Rectangle(138, 173, 274, 25));
			txtFtpFolder.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 18));
			
			jFtpInfo = new JPanel();
			jFtpInfo.setLayout(null);
			jFtpInfo.setBounds(new Rectangle(25, 145, 440, 225));
			jFtpInfo.setBorder(BorderFactory.createTitledBorder(null, "<<FTP情報設定>>", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));
			jFtpInfo.add(tlblFtpPortNo, null);
			jFtpInfo.add(lblFtpPortNoWaku, null);
			jFtpInfo.add(txtFtpPortNo, null);
			
			jFtpInfo.add(tlblFtpHostAddress, null);
			jFtpInfo.add(lblFtpHostAddressWaku, null);
			jFtpInfo.add(txtFtpHostAddress, null);
			
			jFtpInfo.add(tlblFtpUserName, null);
			jFtpInfo.add(lblFtpUserNameWaku, null);
			jFtpInfo.add(txtFtpUserName, null);
			
			jFtpInfo.add(tlblFtpPassword, null);
			jFtpInfo.add(lblFtpPasswordWaku, null);
			jFtpInfo.add(txtFtpPassword, null);
			
			jFtpInfo.add(tlblFtpFolder, null);
			jFtpInfo.add(lblFtpFolderWaku, null);
			jFtpInfo.add(txtFtpFolder, null);
		}
		return jFtpInfo;
	}

}