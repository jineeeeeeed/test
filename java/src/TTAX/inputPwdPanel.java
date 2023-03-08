package jp.co.css.TTAX;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import jp.co.css.base.AppConfig;
import jp.co.css.webpos.common.gui.FKey;
import jp.co.css.webpos.common.gui.FKeyAdapter;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.gui.TLabel;
import jp.co.css.webpos.common.gui.TTextField;
import jp.co.css.webpos.common.log.LogOut;
import jp.co.css.webpos.common.message.MessageBoxValue;
import jp.co.css.webpos.common.message.MessageDispNew;

/**
 * 　買取査定呼出ﾎﾟｯﾌﾟｱｯﾌﾟ
 */
public class inputPwdPanel implements MessageBoxValue
{

	public MessageDispNew msg = new MessageDispNew();	//ﾒｯｾｰｼﾞﾎﾞｯｸｽ
	private String strMsgTitle = "クライアント証明書";			//ﾒｯｾｰｼﾞﾎﾞｯｸｽﾀｲﾄﾙ

	private int ans	= -1;				//どのﾎﾞﾀﾝが押されたかの戻り値
	AppConfig appConfig;
	private JFrame frm = null;				//呼出元画面
    private JDialog dlg = null;
	private JPanel jContentPane = null;
	private TTextField txtClientCrtPwd = null;
	private FKey fKey = null;
	private JPanel jPanelFkey = null;
	private LogOut logOut;
	private String strReturn;


	//ｺﾝｽﾄﾗｸﾀ
	public inputPwdPanel(AppConfig appConfig){
		this.logOut = appConfig.getLogOut();
		this.appConfig = appConfig;
		getDlg();
		msg = new MessageDispNew(dlg,logOut);
	}

	/**
	 * ﾒｲﾝ画面表示
	 */
	public int disp() {
		txtClientCrtPwd.setText("");

		dlg.setModal(true);								//ﾓｰﾀﾞﾙに
		dlg.setResizable(false);						//ｻｲｽﾞ変更不可
		fKey.addFButtonListener(new ButtonListener());	//ﾘｽﾅ追加
		fKey.setLogOut(logOut);

		//ログ出力
		logOut.info("画面【" + dlg.getTitle() + "】を開きました。");

		dlg.setVisible(true);							//画面表示

		txtClientCrtPwd.requestFocus();						//変更用ﾃｷｽﾄにﾌｫｰｶｽを当てる

		return ans;
	}

	class ButtonListener extends FKeyAdapter{
		//ｷｬﾝｾﾙ
		public void f9Click(ActionEvent e) {
			ans = -1;
			//ログ出力
			logOut.info("画面【" + dlg.getTitle() + "】を閉じました。");
			dlg.dispose();
		}
		//OK
		public void f12Click(ActionEvent e) {
			if( txtClientCrtPwd.getText().equals("") ){
				msg.disp(MB_EXCLAMATION, "パスワードが入力されていません。", strMsgTitle);
				txtClientCrtPwd.requestFocus();
				return;
			}
			strReturn = txtClientCrtPwd.getText();
			//ログ出力
			logOut.info("画面【" + dlg.getTitle() + "】を閉じました。");
			ans = 1;
			dlg.dispose();
		}
	}

	/**
	 * This method initializes dlg
	 *
	 * @return javax.swing.JDialog
	 */
	private JDialog getDlg() {
		if (dlg == null) {
			dlg = new JDialog(frm);
			dlg.setContentPane(getJContentPane());
			dlg.setSize(new Dimension(384, 186));
			dlg.setTitle("クライアント証明書");

			// 画面サイズ調整
			TGuiUtil.resizeWindow(dlg);

			//画面の中央に表示
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (screen.width - dlg.getWidth()) / 2;
			int y = (screen.height - dlg.getHeight()) / 2;
			dlg.setLocation(x, y);
		}
		return dlg;
	}
	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			JLabel lblClientCrtPwd = new JLabel();
			lblClientCrtPwd.setText("");
			lblClientCrtPwd.setBounds(new Rectangle(160, 30, 181, 36));
			lblClientCrtPwd.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			TLabel tlblClientCrtPwd = new TLabel();
			tlblClientCrtPwd.setText("パスワード");
			tlblClientCrtPwd.setBounds(new Rectangle(35, 30, 126, 36));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			jContentPane.setFont(new Font("Dialog", Font.PLAIN, 12));
			jContentPane.add(getJPanelFkey(), null);
			jContentPane.add(getFKey(), null);
			jContentPane.add(lblClientCrtPwd, null);
			jContentPane.add(tlblClientCrtPwd, null);
			jContentPane.add(getTxtField(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes txtDenpyoNo
	 *
	 * @return jp.co.css.webpos.common.gui.TNumericField
	 */
	private TTextField getTxtField() {
		if (txtClientCrtPwd == null) {
			txtClientCrtPwd = new TTextField();
			txtClientCrtPwd.setText("1234567890123");
			txtClientCrtPwd.setHorizontalAlignment(JTextField.LEFT);
			txtClientCrtPwd.setMaxLength(64);
			txtClientCrtPwd.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 18));
			txtClientCrtPwd.setBounds(new Rectangle(165, 35, 171, 26));
			txtClientCrtPwd.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtClientCrtPwd.setIMType(TTextField.IM_OFF);
			txtClientCrtPwd.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(java.awt.event.KeyEvent e) {
					if( e.getKeyCode() == KeyEvent.VK_ENTER ){
						fKey.butF12.requestFocus();
					}
				}
			});
		}
		return txtClientCrtPwd;
	}
	/**
	 * This method initializes jPanelFkey
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelFkey() {
		if (jPanelFkey == null) {
			jPanelFkey = new JPanel();
			jPanelFkey.setLayout(new GridBagLayout());
			jPanelFkey.setBounds(new Rectangle(35, 85, 306, 46));
			jPanelFkey.setVisible(false);
		}
		return jPanelFkey;
	}

	/**
	 * This method initializes fKey2
	 *
	 * @return jp.co.css.webpos.common.gui.FKey2
	 */
	private FKey getFKey() {
		if (fKey == null) {
			int[] intFormat = new int[] { 9, 12 };
			fKey = new FKey(intFormat);
			fKey.setLayout(null);
			fKey.setBounds(jPanelFkey.getBounds());
			fKey.setF9Text("F9 戻る");
			fKey.setF12Text("F12 確定");
		}
		return fKey;
	}

	public String getStrReturn() {
		return strReturn;
	}



}