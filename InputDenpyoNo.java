package jp.co.css.TCST;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import java.awt.Color;

import jp.co.css.COMMON.SearchDenpyo;
import jp.co.css.base.AppConfig;
import jp.co.css.webpos.common.log.LogOut;
import jp.co.css.webpos.common.message.MessageBoxValue;
import jp.co.css.webpos.common.message.MessageDispNew;
import jp.co.css.webpos.common.util.Util;

import javax.swing.border.BevelBorder;

import jp.co.css.webpos.common.gui.FKeyAdapter;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.gui.TLabel;
import jp.co.css.webpos.common.gui.TNumericField;
import javax.swing.JTextField;
import jp.co.css.webpos.common.gui.FKey;
import java.awt.GridBagLayout;


/**
 * 　伝票番号入力ﾎﾟｯﾌﾟｱｯﾌﾟ
 */
public class InputDenpyoNo implements MessageBoxValue
{

	public MessageDispNew msg = new MessageDispNew();	//ﾒｯｾｰｼﾞﾎﾞｯｸｽ
	private String strMsgTitle = "伝票入力";			//ﾒｯｾｰｼﾞﾎﾞｯｸｽﾀｲﾄﾙ

	private int ans	= -1;				//どのﾎﾞﾀﾝが押されたかの戻り値

	private JFrame frm = null;				//呼出元画面
    private JDialog dlg = null;
	private JPanel jContentPane = null;
    private TLabel tlblDenpyoNo = null;
	private JLabel lblDenpyoNoWaku = null;
	private TNumericField txtDenpyoNo = null;
	private FKey fKey = null;
	private JPanel jPanelFkey = null;
	private LogOut logOut;
	private int intKensakuFlg = 0;//検索ボタン出力フラグかつ伝票区分
	private AppConfig appConfig;
	private boolean blnTentoSaihako = false;//No.14724 2017/08/25 LYK add start No.16654 blnKatirotiSaihako→blnTentoSaihako

	//ｺﾝｽﾄﾗｸﾀ
	public InputDenpyoNo(JFrame frm, AppConfig appConfig){
		this.frm = frm;
		this.logOut = appConfig.getLogOut();
		getDlg();
		msg = new MessageDispNew(dlg,logOut);
	}
	//伝票検索
	public InputDenpyoNo(JFrame frm, AppConfig appConfig, int intDenpyoKb){
		this.frm = frm;
		this.logOut = appConfig.getLogOut();
		this.intKensakuFlg = intDenpyoKb;
		this.appConfig = appConfig;
		getDlg();
		msg = new MessageDispNew(dlg,logOut);
	}

	/**
	 * ﾒｲﾝ画面表示
	 */
	public int disp() {
		ans = -1; // No.19655 	買取サイト連携データの取消処理 石暁彩 chg 2019/05/08
		//買取再発行の場合、伝票番号をクリアしない No.14724 2017/08/25 LYK chg start
		if (!blnTentoSaihako) {
			txtDenpyoNo.setText("");
		}
		blnTentoSaihako = false;
		//No.14724 2017/08/25 LYK chg end

		dlg.setModal(true);								//ﾓｰﾀﾞﾙに
		dlg.setResizable(false);						//ｻｲｽﾞ変更不可
		fKey.addFButtonListener(new ButtonListener());	//ﾘｽﾅ追加
		fKey.setLogOut(logOut);

		//ログ出力
		logOut.info("画面【" + dlg.getTitle() + "】を開きました。");

		dlg.setVisible(true);							//画面表示

		txtDenpyoNo.requestFocus();						//変更用ﾃｷｽﾄにﾌｫｰｶｽを当てる

		return ans;
	}

	class ButtonListener extends FKeyAdapter
	{
		//検索
		public void f5Click(ActionEvent e) {
			SearchDenpyo search = new SearchDenpyo(dlg, appConfig, intKensakuFlg);
			switch (search.disp()) {
			case MB_CANCEL:
				txtDenpyoNo.requestFocus();
				break;
			case MB_YES:
				txtDenpyoNo.setText(search.getDenpyoNo());
				txtDenpyoNo.requestFocus();
				break;
				default:
					txtDenpyoNo.requestFocus();
					break;
			}
			//フォーカスが当たらないことがあるので念のため 2012/11/15 nomura
			if (txtDenpyoNo.hasFocus() != true) txtDenpyoNo.requestFocus();
		}

		//ｷｬﾝｾﾙ
		public void f9Click(ActionEvent e) {
			ans = -1;
			//ログ出力
			logOut.info("画面【" + dlg.getTitle() + "】を閉じました。");
			dlg.dispose();
		}
		//OK
		public void f12Click(ActionEvent e) {
			if( txtDenpyoNo.getText().equals("") ){
				msg.disp(MB_EXCLAMATION, "伝票番号が入力されていません。", strMsgTitle);
				txtDenpyoNo.requestFocus();
				return;
			}
			//ログ出力
			logOut.info("画面【" + dlg.getTitle() + "】を閉じました。");
			ans = Util.nullToInt(txtDenpyoNo.getText());
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
//			dlg.setSize(new Dimension(304, 186));
//			if (intKensakuFlg == 0) {
//				dlg.setSize(new Dimension(304, 186));
//			} else {
//				dlg.setSize(new Dimension(344, 186));
//			}
			dlg.setSize(new Dimension(344, 186));
			dlg.setTitle("伝票入力");

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
			lblDenpyoNoWaku = new JLabel();
			lblDenpyoNoWaku.setText("");
//			lblDenpyoNoWaku.setBounds(new Rectangle(160, 30, 101, 36));
			lblDenpyoNoWaku.setBounds(new Rectangle(152, 30, 157, 36));
			lblDenpyoNoWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			tlblDenpyoNo = new TLabel();
			tlblDenpyoNo.setText("伝票番号");
//			tlblDenpyoNo.setBounds(new Rectangle(35, 30, 126, 36));
			tlblDenpyoNo.setBounds(new Rectangle(27, 30, 126, 36));

			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			jContentPane.setFont(new Font("Dialog", Font.PLAIN, 12));
			jContentPane.add(getJPanelFkey(), null);
			jContentPane.add(getFKey(), null);
			jContentPane.add(tlblDenpyoNo, null);
			jContentPane.add(lblDenpyoNoWaku, null);
			jContentPane.add(getTxtDenpyoNo(), null);
//			if (intKensakuFlg != 0) {
//				lblDenpyoNoWaku.setBounds(new Rectangle(152, 30, 157, 36));
//				tlblDenpyoNo.setBounds(new Rectangle(27, 30, 126, 36));
//				txtDenpyoNo.setBounds(new Rectangle(157, 35, 147, 26));
//			}
		}
		return jContentPane;
	}

	/**
	 * This method initializes txtDenpyoNo
	 *
	 * @return jp.co.css.webpos.common.gui.TNumericField
	 */
	private TNumericField getTxtDenpyoNo() {
		if (txtDenpyoNo == null) {
			txtDenpyoNo = new TNumericField();
			txtDenpyoNo.setText("12345678");
			txtDenpyoNo.setHorizontalAlignment(JTextField.RIGHT);
			txtDenpyoNo.setMaxLength(8);
			txtDenpyoNo.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 18));
			txtDenpyoNo.setNumericFormat("#");
//			txtDenpyoNo.setBounds(new Rectangle(165, 35, 91, 26));
			txtDenpyoNo.setBounds(new Rectangle(157, 35, 147, 26));
			txtDenpyoNo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtDenpyoNo.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(java.awt.event.KeyEvent e) {
					if( e.getKeyCode() == KeyEvent.VK_ENTER ){
						fKey.butF12.requestFocus();
					}
				}
			});
		}
		return txtDenpyoNo;
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
//			jPanelFkey.setBounds(new Rectangle(35, 85, 226, 46));
			jPanelFkey.setBounds(new Rectangle(25, 85, 286, 46));
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
//			int[] intFormat = new int[] { 9, 12 };
//			fKey = new FKey(intFormat);
//			fKey.setLayout(null);
//			fKey.setBounds(jPanelFkey.getBounds());
//			fKey.setF9Text("F9 ｷｬﾝｾﾙ");
//			fKey.setF12Text("F12 確定");
			if (intKensakuFlg == 0) {
				int[] intFormat = new int[] { 9, 12 };
				fKey = new FKey(intFormat);
				fKey.setLayout(null);
				fKey.setBounds(jPanelFkey.getBounds());
				fKey.setF9Text("F9 ｷｬﾝｾﾙ");
				fKey.setF12Text("F12 確定");
			} else {
				int[] intFormat = new int[] { 5, 9, 12 };
				fKey = new FKey(intFormat);
				fKey.setLayout(null);
				fKey.setBounds(jPanelFkey.getBounds());
				fKey.setF5Text("F5 検索");
				fKey.setF9Text("F9 ｷｬﾝｾﾙ");
				fKey.setF12Text("F12 確定");
			}
		}
		return fKey;
	}

	//No.14724 2017/08/25 LYK add start
	public void setDenpyoNo(int intDenpyoNo){
		txtDenpyoNo.setText(intDenpyoNo);
		blnTentoSaihako = true;
	}
	//No.14724 2017/08/25 LYK add end

}