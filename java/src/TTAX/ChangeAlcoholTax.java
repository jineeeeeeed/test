package jp.co.css.TTAX;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import jp.co.css.base.AppConfig;
import jp.co.css.bean.DbInfo;
import jp.co.css.dao.TaxHanyoMstDAO;
import jp.co.css.webpos.common.except.TException;
import jp.co.css.webpos.common.gui.FKey;
import jp.co.css.webpos.common.gui.FKeyAdapter;
import jp.co.css.webpos.common.gui.TComboBox;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.gui.TLabel;
import jp.co.css.webpos.common.gui.TNumericField;
import jp.co.css.webpos.common.log.LogOut;
import jp.co.css.webpos.common.message.MessageBoxValue;
import jp.co.css.webpos.common.message.MessageDispNew;
import jp.co.css.webpos.common.util.SendTabKeys;
import jp.co.css.webpos.common.util.Util;

/**
 * 　酒税設定ﾎﾟｯﾌﾟｱｯﾌﾟ
 */
public class ChangeAlcoholTax implements MessageBoxValue {
	private int ans	= 0;								//どのﾎﾞﾀﾝが押されたかの戻り値

	private AppConfig appConfig;						//基本情報
	private MessageDispNew messageBox = new MessageDispNew();//ﾒｯｾｰｼﾞﾎﾞｯｸｽ  //  @jve:decl-index=0:
	private final String strMsgTitle = "酒税設定";  	//ﾒｯｾｰｼﾞ用ﾀｲﾄﾙ
	private Object obj = null;

	//ﾌｫｰｶｽの移動
	List<Component> compList;							//ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを集めたﾃｰﾌﾞﾙ
	SendTabKeys sendTab = new SendTabKeys(); 	 		//ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを取得するｸﾗｽ

    //各ｺﾝﾝﾎﾟｰﾈﾝﾄ
    private JDialog dlg = null;
	private JPanel jContentPane = null;
	private JPanel jPanel = null;
	private FKey fKey = null;
	private JPanel jPanelFkey = null;

	//酒税適用有無（物品）
	private TLabel lblLqIndividual = null;
	private JLabel lblLqIndividualWaku = null;
	private TComboBox cboLqIndividual = null;
	//（酒税）品目分類
	private TLabel lblLqCode = null;
	private JLabel lblLqCodeWaku = null;
	private TComboBox cboLqCode = null;
	//（酒税）税率
	private TLabel lblLqTaxRate = null;
	private JLabel lblLqTaxRateWaku = null;
	private TNumericField txtLqTaxRate = null;
	//（酒税）容器容量
	private TLabel lblLqCapacity = null;
	private JLabel lblLqCapacityWaku = null;
	private TNumericField txtLqCapacity = null;
	//（酒税）本数
	private TLabel lblLqNumber = null;
	private JLabel lblLqNumberWaku = null;
	private TNumericField txtLqNumber = null;

	private LogOut logOut;
	public AlcoholTaxBean bean = null;

	//ｺﾝｽﾄﾗｸﾀ
	public ChangeAlcoholTax(Object obj, AppConfig appConfig, AlcoholTaxBean bean) throws TException{
		this.appConfig = appConfig;
		logOut = appConfig.getLogOut();
		this.obj = obj;
		this.bean = bean;
		getDlg();
		init();
		setTextValue();
		messageBox = new MessageDispNew(dlg, appConfig.getLogOut());
	}

	/**
	 * ﾒｲﾝ画面表示
	 */
	public int disp() {
		logOut.info("画面【" + dlg.getTitle() + "】を開きました。");
		dlg.setVisible(true);								//画面表示

		return ans;
	}

	class ButtonListener extends FKeyAdapter{

		//ｷｬﾝｾﾙ
		public void f9Click(ActionEvent e) {
			ans = MB_CANCEL;
			logOut.info("画面【" + dlg.getTitle() + "】を閉じました。");
			dlg.dispose();
		}

		//OK
		public void f12Click(ActionEvent e) {
			if(chkInputData()){
				return;
			}
			ans = MB_YES;
			setBean();
			logOut.info("画面【" + dlg.getTitle() + "】を閉じました。");
			//呼出元画面に戻る
			dlg.dispose();
		}
	}

	private void init(){
		cboLqIndividual.addKeyListener(new TKeyAdapter());
		cboLqCode.addKeyListener(new TKeyAdapter());
		txtLqTaxRate.addKeyListener(new TKeyAdapter());
		txtLqCapacity.addKeyListener(new TKeyAdapter());
		txtLqNumber.addKeyListener(new TKeyAdapter());

		cboLqIndividual.removeAllItems();
		cboLqIndividual.addTextValueItem("0", "0:なし");
		cboLqIndividual.addTextValueItem("1", "1:あり");
		cboLqIndividual.setSelectedIndex(0);
		cboLqIndividual.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if(cboLqIndividual.getSelectedIndex() == 0){
						cboLqCode.setEnabled(false);
						txtLqTaxRate.setEnabled(false);
						txtLqCapacity.setEnabled(false);
						txtLqNumber.setEnabled(false);
						iniText();
					}else{
						cboLqCode.setEnabled(true);
						txtLqTaxRate.setEnabled(true);
						txtLqCapacity.setEnabled(true);
						txtLqNumber.setEnabled(true);
					}
				}
			}
		});

		setcboLqCode();
		iniText();

		// ｶｰｿﾙの制御　※Enableの制御が終わってから設定して!!
		compList = sendTab.setCompList(jContentPane.getComponents());
		dlg.setFocusTraversalPolicy(sendTab.setCustomFocus());

		dlg.setModal(true);								//ﾓｰﾀﾞﾙに
		dlg.setResizable(false);						//ｻｲｽﾞ変更不可
		fKey.addFButtonListener(new ButtonListener());	//ﾘｽﾅ追加
		fKey.setLogOut(logOut);
	}

	private void iniText(){
		cboLqIndividual.setSelectedIndex(0);
		cboLqCode.setSelectedIndex(0);
		txtLqTaxRate.setText("");
		txtLqCapacity.setText("");
		txtLqNumber.setText("");
	}

	private void setTextValue() {
		if(bean.getStrLqIndividual().equals("あり")){
			cboLqIndividual.setSelectedItemValue("1");
			cboLqCode.setSelectedItemValue(String.valueOf(bean.getIntLqCode()));
			txtLqTaxRate.setText(bean.getIntLqTaxRate());
			txtLqCapacity.setText(bean.getIntLqCapacity());
			txtLqNumber.setText(bean.getIntLqNumber());
		}else{
			cboLqIndividual.setSelectedItemValue("0");
			cboLqCode.setEnabled(false);
			txtLqTaxRate.setEnabled(false);
			txtLqCapacity.setEnabled(false);
			txtLqNumber.setEnabled(false);
		}
	}

	private void setcboLqCode() {
		if(cboLqCode.getItemCount() > 0) {
			cboLqCode.removeAllItems();
		}
		TaxCodeUtil codeUtil = new TaxCodeUtil();
		Map<String, String> codemap = codeUtil.getLiquorMap();
		cboLqCode.addTextValueItem("-1", "");
		for (Map.Entry<String, String> entry : codemap.entrySet()) {
			cboLqCode.addTextValueItem(entry.getKey(), entry.getValue());
		}
	}

	private boolean chkInputData() {
		if(cboLqIndividual.getSelectedItemNum() == 1){
			if(cboLqCode.getSelectedIndex() == 0){
				messageBox.disp(MB_EXCLAMATION, "「品目分類」を設定してください。",strMsgTitle);
				return true;
			}
			if(Util.isNullOrEmpty(txtLqTaxRate.getText())){
				messageBox.disp(MB_EXCLAMATION, "「税率」を設定してください。",strMsgTitle);
				return true;
			}
			if(Util.isNullOrEmpty(txtLqCapacity.getText())){
				messageBox.disp(MB_EXCLAMATION, "「容器容量」を設定してください。",strMsgTitle);
				return true;
			}
			if(Util.isNullOrEmpty(txtLqNumber.getText())){
				messageBox.disp(MB_EXCLAMATION, "「本数」を設定してください。",strMsgTitle);
				return true;
			}
		}
		return false;
	}

	private void setBean() {
		bean.setStrLqIndividual(cboLqIndividual.getSelectedItemComment());
		bean.setIntLqCode(Integer.valueOf(cboLqCode.getSelectedItemValue().toString()));
		bean.setStrLqCodeNm(cboLqCode.getSelectedItemString());
		bean.setIntLqTaxRate(txtLqTaxRate.getTextInt());
		bean.setIntLqCapacity(txtLqCapacity.getTextInt());
		bean.setIntLqNumber(txtLqNumber.getTextInt());
	}

	public AlcoholTaxBean getBean() {
		return bean;
	}

	public void setBean(AlcoholTaxBean bean) {
		this.bean = bean;
	}

	class TKeyAdapter extends KeyAdapter {

		public void keyPressed(java.awt.event.KeyEvent e) {

			switch( e.getKeyCode() ){
			case KeyEvent.VK_ENTER:
				// ﾌｫｰｶｽ次へ
				sendTab.SendTabKeys(e);
				break;
			}
		}
	}

	/**
	 * This method initializes dlg
	 *
	 * @return javax.swing.JDialog
	 */
	private JDialog getDlg() throws TException{
		if (dlg == null) {
			if (obj == null) {
				dlg = new JDialog();
			} else if (obj instanceof JFrame) {
				dlg = new JDialog((JFrame)obj);
			} else if (obj instanceof JDialog) {
				dlg = new JDialog((JDialog)obj);
			}

			dlg.setContentPane(getJContentPane());
			dlg.setSize(new Dimension(397+70+40, 349));
			dlg.setTitle("酒税設定");

			// 画面サイズ調整
			TGuiUtil.resizeWindow(dlg);

			//画面の中央に表示
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (screen.width - dlg.getWidth()) / 2;
			int y = (screen.height - dlg.getHeight()) / 2;
			dlg.setLocation(x, y);
			dlg.addWindowListener(new WindowAdapter(){
				public void windowOpened(WindowEvent we){
					cboLqIndividual.requestFocus();
				}
			});
		}
		return dlg;
	}
	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() throws TException{
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			jContentPane.setFont(new Font("Dialog", Font.PLAIN, 12));
			jContentPane.add(getJPanel(), null);
			jContentPane.add(getJPanelFkey(), null);
			jContentPane.add(getFKey(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() throws TException{
		if (jPanel == null) {
			lblLqIndividualWaku = new JLabel();
			lblLqIndividualWaku.setText("");
			lblLqIndividualWaku.setBounds(new Rectangle(150-10, 30, 157+160, 31));
			lblLqIndividualWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			lblLqIndividual = new TLabel();
			lblLqIndividual.setText("酒税適用有無");
			lblLqIndividual.setBounds(new Rectangle(25-10, 30, 126, 31));

			cboLqIndividual = new TComboBox();
			cboLqIndividual.setBounds(new Rectangle(153-10, 33, 151+160, 25));
			cboLqIndividual.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

			lblLqCodeWaku = new JLabel();
			lblLqCodeWaku.setBounds(new Rectangle(150-10, 70, 157+160, 31));
			lblLqCodeWaku.setText("");
			lblLqCodeWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			lblLqCode = new TLabel();
			lblLqCode.setBounds(new Rectangle(25-10, 70, 126, 31));
			lblLqCode.setText("品目分類");

			cboLqCode = new TComboBox();
			cboLqCode.setBounds(new Rectangle(153-10, 73, 151+160, 25));
			cboLqCode.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

			lblLqTaxRateWaku = new JLabel();
			lblLqTaxRateWaku.setBounds(new Rectangle(150-10, 110, 157+160, 31));
			lblLqTaxRateWaku.setText("");
			lblLqTaxRateWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			lblLqTaxRate = new TLabel();
			lblLqTaxRate.setBounds(new Rectangle(25-10, 110, 126, 31));
			lblLqTaxRate.setText("税率");

			txtLqTaxRate = new TNumericField();
			txtLqTaxRate.setBounds(new Rectangle(153-10, 113, 151+160, 25));
			txtLqTaxRate.setText("");
			txtLqTaxRate.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtLqTaxRate.setMaxLength(7);
			txtLqTaxRate.setMinusInput(false);

			lblLqCapacityWaku = new JLabel();
			lblLqCapacityWaku.setBounds(new Rectangle(150-10, 150, 157+160, 31));
			lblLqCapacityWaku.setText("");
			lblLqCapacityWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			lblLqCapacity = new TLabel();
			lblLqCapacity.setBounds(new Rectangle(25-10, 150, 126, 31));
			lblLqCapacity.setText("容器容量");

			txtLqCapacity = new TNumericField();
			txtLqCapacity.setBounds(new Rectangle(153-10, 153, 151+160, 25));
			txtLqCapacity.setText("");
			txtLqCapacity.setMaxLength(7);
			txtLqCapacity.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtLqTaxRate.setMinusInput(false);

			lblLqNumberWaku = new JLabel();
			lblLqNumberWaku.setBounds(new Rectangle(150-10, 190, 157+160, 31));
			lblLqNumberWaku.setText("");
			lblLqNumberWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			lblLqNumber = new TLabel();
			lblLqNumber.setBounds(new Rectangle(25-10, 190, 126, 31));
			lblLqNumber.setText("本数");

			txtLqNumber = new TNumericField();
			txtLqNumber.setBounds(new Rectangle(153-10, 193, 151+160, 25));
			txtLqNumber.setText("");
			txtLqNumber.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtLqNumber.setMaxLength(4);
			txtLqNumber.setMinusInput(false);

			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setBounds(new Rectangle(25-10, 5, 332+100+40, 235));
			jPanel.setBorder(BorderFactory.createTitledBorder(null, "酒税設定", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));
			jPanel.add(lblLqIndividual, null);
			jPanel.add(lblLqIndividualWaku, null);
			jPanel.add(cboLqIndividual, null);
			jPanel.add(lblLqCode, null);
			jPanel.add(lblLqCodeWaku, null);
			jPanel.add(cboLqCode, null);
			jPanel.add(lblLqTaxRate, null);
			jPanel.add(lblLqTaxRateWaku, null);
			jPanel.add(txtLqTaxRate, null);
			jPanel.add(lblLqCapacity, null);
			jPanel.add(lblLqCapacityWaku, null);
			jPanel.add(txtLqCapacity, null);
			jPanel.add(lblLqNumber, null);
			jPanel.add(lblLqNumberWaku, null);
			jPanel.add(txtLqNumber, null);

		}
		return jPanel;
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
			jPanelFkey.setBounds(new Rectangle(53, 248, 286+70, 46));

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
			int[] intFormat;
			intFormat = new int[] { 9, 12 };
			fKey = new FKey(intFormat);
			fKey.setLayout(null);
			fKey.setBounds(jPanelFkey.getBounds());
			fKey.setF9Text("F9　キャンセル");
			fKey.setF12Text("F12　確定");
		}
		return fKey;
	}
}