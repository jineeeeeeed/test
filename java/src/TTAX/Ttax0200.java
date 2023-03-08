package jp.co.css.TTAX;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import jp.co.css.base.AppConfig;
import jp.co.css.base.TFrame;
import jp.co.css.webpos.common.gui.TButton;
import jp.co.css.webpos.common.gui.TCheckBox;
import jp.co.css.webpos.common.gui.TComboBox;
import jp.co.css.webpos.common.gui.TLabel;
import jp.co.css.webpos.common.gui.TLabel2;
import jp.co.css.webpos.common.gui.TTextField;
/*******************************************************************************
 * 処理名称 ：   免税設定画面     <br>
 * 作成日 　　： 	2019/01/23	  <br>
 * 作成者 　　：  	張 佳明		　<br>
 ******************************************************************************/

public class Ttax0200 extends TFrame{

	private static final long serialVersionUID = 1L;

	private JPanel jPanel = null;

	//販売者氏名
	public TTextField txtHanbaishaNm = null;
	//販売場所在地
	public TTextField txtHanbaishaJyusho = null;
	//所轄税務署
	public TTextField txtZeimushoNm = null;
	//納税地
	public TTextField txtTaxableLand = null;
	//在留資格
//	public TTextField txtStatusOfResidence = null;
	public TComboBox cmbStatusOfResidence = null;//No.22747 2020/09/01 wk chg
	//記載事項言語
	public TComboBox cmbLanguage = null;
	//注釈印字
	public TCheckBox chkAnnotationPrinting = null;
	//誓約書印刷フラグ
	public TCheckBox chkSeiyakushoInjiKb = null;
	//記録表印刷フラグ
	public TCheckBox chkRecordListInjiKb = null;
	//梱包リスト印刷フラグ
	public TCheckBox chkPackingListInjiKb = null;
	//販売場識別符号//No.22747 2020/08/04 趙 志強 add
	public TTextField txtHanbaiSikibetu = null;
	//クライアント証明書有効期限 No.22747 2021/02/09 cf add
	public TTextField txtClientCrtExpiry= null;
	//クライアント証明書 No.22747 2021/02/09 cf add
	public TTextField txtClientCrtPath = null;
	public TButton btnClientCrtPath = null;
	public TLabel2 lblClientCrt = null;


	public Ttax0200(AppConfig appConfig) {
		super(appConfig);
		init();
	}

	private void init() {
		jMainPanel.add(getJPanel(), null);
		this.setTitle("免税設定画面　TTAX0200");
		lblTitle.setText("　免　税　設　定　画　面");
	}

	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setBounds(new Rectangle(16, 75, 984, 558));
			jPanel.add(getJPnlHabaishaInfo(), null);
			jPanel.add(getJPnlZeimushoInfo(), null);
			jPanel.add(getJPnlDefaultSet(), null);
			jPanel.add(getHanbaiSikibetu(), null);//No.22747 2020/08/04 趙 志強 add
		}
		return jPanel;
	}

	private JPanel getJPnlHabaishaInfo() {
		JPanel jPnlHanbaishaInfo = new JPanel();
		jPnlHanbaishaInfo.setLayout(null);
		jPnlHanbaishaInfo.setBounds(new Rectangle(10, 5, 460, 107));
		jPnlHanbaishaInfo.setBorder(BorderFactory.createTitledBorder(null, "≪販売者情報≫", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));

		TLabel tlblTitle = new TLabel();
		tlblTitle.setBounds(new Rectangle(10, 25, 130, 31));
		tlblTitle.setText("販売者氏名");
		JLabel lblWaku = new JLabel();
		lblWaku.setBounds(new Rectangle(139, 25, 300, 31));
		lblWaku.setText("");
		lblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

		txtHanbaishaNm = new TTextField();
		txtHanbaishaNm.setBounds(new Rectangle(143, 28, 291, 25));
		txtHanbaishaNm.setText("");
		txtHanbaishaNm.setIMType(TTextField.IM_HIRAGANA);
		txtHanbaishaNm.setHorizontalAlignment(JTextField.LEFT);
		txtHanbaishaNm.setMaxLength(50);
		txtHanbaishaNm.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		jPnlHanbaishaInfo.add(tlblTitle, null);
		jPnlHanbaishaInfo.add(lblWaku, null);
		jPnlHanbaishaInfo.add(txtHanbaishaNm, null);

		tlblTitle = new TLabel();
		tlblTitle.setBounds(new Rectangle(10, 60, 130, 31));
		tlblTitle.setText("販売場所在地");
		lblWaku = new JLabel();
		lblWaku.setBounds(new Rectangle(139, 60, 300
				, 31));
		lblWaku.setText("");
		lblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

		txtHanbaishaJyusho = new TTextField();
		txtHanbaishaJyusho.setBounds(new Rectangle(143, 63, 291, 25));
		txtHanbaishaJyusho.setText("");
		txtHanbaishaJyusho.setIMType(TTextField.IM_HIRAGANA);
		txtHanbaishaJyusho.setHorizontalAlignment(JTextField.LEFT);
		txtHanbaishaJyusho.setMaxLength(80);
		txtHanbaishaJyusho.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		jPnlHanbaishaInfo.add(tlblTitle, null);
		jPnlHanbaishaInfo.add(lblWaku, null);
		jPnlHanbaishaInfo.add(txtHanbaishaJyusho, null);

		return jPnlHanbaishaInfo;
	}

	private JPanel getJPnlZeimushoInfo() {
		JPanel jPnlZeimushoInfo = new JPanel();
		jPnlZeimushoInfo.setLayout(null);
		jPnlZeimushoInfo.setBounds(new Rectangle(500, 5, 460, 107));
		jPnlZeimushoInfo.setBorder(BorderFactory.createTitledBorder(null, "≪税務署情報≫", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));

		TLabel tlblTitle = new TLabel();
		tlblTitle.setBounds(new Rectangle(10, 25, 130, 31));
		tlblTitle.setText("所轄税務署");
		JLabel lblWaku = new JLabel();
		lblWaku.setBounds(new Rectangle(139, 25, 300, 31));
		lblWaku.setText("");
		lblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

		txtZeimushoNm = new TTextField();
		txtZeimushoNm.setBounds(new Rectangle(143, 28, 291, 25));
		txtZeimushoNm.setText("");
		txtZeimushoNm.setIMType(TTextField.IM_HIRAGANA);
		txtZeimushoNm.setHorizontalAlignment(JTextField.LEFT);
		txtZeimushoNm.setMaxLength(50);
		txtZeimushoNm.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		jPnlZeimushoInfo.add(tlblTitle, null);
		jPnlZeimushoInfo.add(lblWaku, null);
		jPnlZeimushoInfo.add(txtZeimushoNm, null);

		tlblTitle = new TLabel();
		tlblTitle.setBounds(new Rectangle(10, 60, 130, 31));
		tlblTitle.setText("納税地");
		lblWaku = new JLabel();
		lblWaku.setBounds(new Rectangle(139, 60, 300, 31));
		lblWaku.setText("");
		lblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

		txtTaxableLand = new TTextField();
		txtTaxableLand.setBounds(new Rectangle(143, 63, 291, 25));
		txtTaxableLand.setText("");
		txtTaxableLand.setIMType(TTextField.IM_HIRAGANA);
		txtTaxableLand.setHorizontalAlignment(JTextField.LEFT);
		txtTaxableLand.setMaxLength(80);
		txtTaxableLand.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		jPnlZeimushoInfo.add(tlblTitle, null);
		jPnlZeimushoInfo.add(lblWaku, null);
		jPnlZeimushoInfo.add(txtTaxableLand, null);

		return jPnlZeimushoInfo;
	}

	private JPanel getJPnlDefaultSet() {
		JPanel jPnlDefaultSet = new JPanel();
		jPnlDefaultSet.setLayout(null);
		jPnlDefaultSet.setBounds(new Rectangle(10, 120, 950, 177));
		jPnlDefaultSet.setBorder(BorderFactory.createTitledBorder(null, "≪デフォルト設定≫", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));

		TLabel tlblTitle = new TLabel();
		tlblTitle.setBounds(new Rectangle(10, 25, 130, 31));
		tlblTitle.setText("在留資格");
		JLabel lblWaku = new JLabel();
		lblWaku.setBounds(new Rectangle(139, 25, 169, 31));
		lblWaku.setText("");
		lblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

		//No.22747 2020/09/01 wk chg start
//		txtStatusOfResidence = new TTextField();
//		txtStatusOfResidence.setBounds(new Rectangle(143, 28, 160, 25));
//		txtStatusOfResidence.setText("");
//		txtStatusOfResidence.setIMType(TTextField.IM_HIRAGANA);
//		txtStatusOfResidence.setHorizontalAlignment(JTextField.LEFT);
//		txtStatusOfResidence.setMaxLength(20);
//		txtStatusOfResidence.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		cmbStatusOfResidence = new TComboBox();
		cmbStatusOfResidence.setBounds(new Rectangle(143, 28, 160, 25));
		//No.22747 2020/09/01 wk chg end

		jPnlDefaultSet.add(tlblTitle, null);
		jPnlDefaultSet.add(lblWaku, null);
//		jPnlDefaultSet.add(txtStatusOfResidence, null);
		jPnlDefaultSet.add(cmbStatusOfResidence, null);//No.22747 2020/09/01 wk chg

		tlblTitle = new TLabel();
		tlblTitle.setBounds(new Rectangle(10, 60, 130, 31));
		tlblTitle.setText("記載事項言語");
		lblWaku = new JLabel();
		lblWaku.setBounds(new Rectangle(139, 60, 169, 31));
		lblWaku.setText("");
		lblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

		cmbLanguage = new TComboBox();
		cmbLanguage.setBounds(new Rectangle(143, 63, 160, 25));

		jPnlDefaultSet.add(tlblTitle, null);
		jPnlDefaultSet.add(lblWaku, null);
		jPnlDefaultSet.add(cmbLanguage, null);

		tlblTitle = new TLabel();
		tlblTitle.setBounds(new Rectangle(10, 95, 130, 31));
		tlblTitle.setText("注釈印字");
		lblWaku = new JLabel();
		lblWaku.setBounds(new Rectangle(139, 95, 169, 31));
		lblWaku.setText("");
		lblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

		chkAnnotationPrinting = new TCheckBox();
		chkAnnotationPrinting.setBounds(new Rectangle(143, 98, 160, 25));
		chkAnnotationPrinting.setText("英語の注釈を印字する");

		jPnlDefaultSet.add(tlblTitle, null);
		jPnlDefaultSet.add(lblWaku, null);
		jPnlDefaultSet.add(chkAnnotationPrinting, null);

		tlblTitle = new TLabel();
		tlblTitle.setBounds(new Rectangle(10, 130, 130, 31));
		tlblTitle.setText("印刷免税書類");
		lblWaku = new JLabel();
		lblWaku.setBounds(new Rectangle(139, 130, 450, 31));
		lblWaku.setText("");
		lblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

		chkSeiyakushoInjiKb = new TCheckBox();
		chkSeiyakushoInjiKb.setBounds(new Rectangle(143, 133, 120, 25));
		chkSeiyakushoInjiKb.setText("購入者誓約書");

		chkRecordListInjiKb = new TCheckBox();
		chkRecordListInjiKb.setBounds(new Rectangle(269, 133, 120, 25));
		chkRecordListInjiKb.setText("購入記録票");

		chkPackingListInjiKb = new TCheckBox();
		chkPackingListInjiKb.setBounds(new Rectangle(394, 133, 190, 25));
		chkPackingListInjiKb.setText("梱包物品リスト（消耗品のみ）");

		jPnlDefaultSet.add(tlblTitle, null);
		jPnlDefaultSet.add(lblWaku, null);
		jPnlDefaultSet.add(chkSeiyakushoInjiKb, null);
		jPnlDefaultSet.add(chkRecordListInjiKb, null);
		jPnlDefaultSet.add(chkPackingListInjiKb, null);

		return jPnlDefaultSet;
	}
	//No.22747 2020/08/04 趙 志強 add start
	private JPanel getHanbaiSikibetu(){
		JPanel jPnlHanbaiSikibetu = new JPanel();
		jPnlHanbaiSikibetu.setLayout(null);
		jPnlHanbaiSikibetu.setBounds(new Rectangle(10, 320, 950, 130));
		jPnlHanbaiSikibetu.setBorder(BorderFactory.createTitledBorder(null, "≪免税データ送信設定≫", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));
		int y = 25;
		TLabel tlblTitle = new TLabel();
		tlblTitle.setBounds(new Rectangle(10, y, 130, 31));
		tlblTitle.setText("販売場識別符号");
		JLabel lblWaku = new JLabel();
		lblWaku.setBounds(new Rectangle(139, y, 300, 31));
		lblWaku.setText("");
		lblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

		txtHanbaiSikibetu = new TTextField();
		txtHanbaiSikibetu.setBounds(new Rectangle(143, y+3, 291, 25));
		txtHanbaiSikibetu.setText("");
		txtHanbaiSikibetu.setIMType(TTextField.IM_NONE);
		txtHanbaiSikibetu.setHorizontalAlignment(JTextField.LEFT);
		txtHanbaiSikibetu.setMaxLength(21);
		txtHanbaiSikibetu.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		jPnlHanbaiSikibetu.add(tlblTitle, null);
		jPnlHanbaiSikibetu.add(lblWaku, null);
		jPnlHanbaiSikibetu.add(txtHanbaiSikibetu, null);

		y+= 35;
		TLabel tlblClientCrt = new TLabel();
		tlblClientCrt.setBounds(new Rectangle(10, y, 130, 31));
		tlblClientCrt.setText("クライアント証明書");
		JLabel lblClientCrtWaku = new JLabel();
		lblClientCrtWaku.setText("");
		lblClientCrtWaku.setBounds(new Rectangle(139, y, 300, 31));
		lblClientCrtWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
		txtClientCrtPath = new TTextField();
		txtClientCrtPath.setHorizontalAlignment(JTextField.LEFT);
		//txtChumonApiCrtPath.setMaxLength(256);
		txtClientCrtPath.setEnabled(false);
		txtClientCrtPath.setBounds(new Rectangle(143, y+3, 232, 25));
		txtClientCrtPath.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		txtClientCrtPath.setEnabled(false);
		txtClientCrtPath.setFont(new Font("MS UI Gothic", Font.BOLD, 12));
		btnClientCrtPath = new TButton();
		btnClientCrtPath.setBounds(new Rectangle(377, y+3, 60, 25));
		btnClientCrtPath.setText("選択");
		lblClientCrt = new TLabel2();
		lblClientCrt.setText("");
		lblClientCrt.setBounds(new Rectangle(443, y, 86, 31));
		lblClientCrt.setFont(new Font("MS UI Gothic", Font.BOLD, 18));
		lblClientCrt.setForeground(new java.awt.Color(255, 255, 255));
		
		jPnlHanbaiSikibetu.add(tlblClientCrt, null);
		jPnlHanbaiSikibetu.add(lblClientCrtWaku, null);
		jPnlHanbaiSikibetu.add(txtClientCrtPath, null);
		jPnlHanbaiSikibetu.add(btnClientCrtPath, null);
		jPnlHanbaiSikibetu.add(lblClientCrt, null);

		y+= 35;
		TLabel tlblClientCrtDate = new TLabel();
		tlblClientCrtDate.setBounds(new Rectangle(10, y, 130, 31));
		tlblClientCrtDate.setText("<HTML>クライアント証明書<BR>有効期限日</HTML>");
		JLabel lblClientCrtDateWaku = new JLabel();
		lblClientCrtDateWaku.setText("");
		lblClientCrtDateWaku.setBounds(new Rectangle(134, y, 131+60, 31));
		lblClientCrtDateWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
		txtClientCrtExpiry = new TTextField();
		txtClientCrtExpiry.setBounds(new Rectangle(142, y+3, 120+60, 25));
		txtClientCrtExpiry.setEnabled(false);

		jPnlHanbaiSikibetu.add(tlblClientCrtDate, null);
		jPnlHanbaiSikibetu.add(lblClientCrtDateWaku, null);
		jPnlHanbaiSikibetu.add(txtClientCrtExpiry, null);

		return jPnlHanbaiSikibetu;
	}
	//No.22747 2020/08/04 趙 志強 add end
}
