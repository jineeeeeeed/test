package jp.co.css.TTAX;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import jp.co.css.base.AppConfig;
import jp.co.css.base.TFrame;
import jp.co.css.webpos.common.gui.EditTableModel;
import jp.co.css.webpos.common.gui.ListModel;
import jp.co.css.webpos.common.gui.RowHeader;
import jp.co.css.webpos.common.gui.TButton;
import jp.co.css.webpos.common.gui.TCheckBox;
import jp.co.css.webpos.common.gui.TComboBox;
import jp.co.css.webpos.common.gui.TDateChooser;
import jp.co.css.webpos.common.gui.TEditTable;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.gui.TLabel;
import jp.co.css.webpos.common.gui.TLabel5;
import jp.co.css.webpos.common.gui.TNumericField;
import jp.co.css.webpos.common.gui.TScrollPane;
import jp.co.css.webpos.common.gui.TTextField;

/*******************************************************************************
 * 処理名称 ：   免税書類発行画面      <br>
 * 作成日 　　： 	2019/01/23	  <br>
 * 作成者 　　：  	石暁彩			  <br>
 ******************************************************************************/
public class Ttax0100 extends TFrame {
	private static final long serialVersionUID = 1L;

	//伝票番号
	public TNumericField txtDenpyoNo = null;

	//一般商品:数量合計
	public TNumericField txtKensuNormal = null;
	//一般商品:金額合計
	public TNumericField txtTotalKinNormal = null;
	//消耗品:数量合計
	public TNumericField txtKensuShomohin = null;
	//消耗品:金額合計
	public TNumericField txtTotalKinShomohin = null;

	public TButton btnToRight = null;
	public TButton btnToLeft = null;

	//パスポート
	public JPanel jPanelPassport = null;
	//パスポート
	public TTextField txtPassport= null;
	//旅券の種類
	public TTextField txtRyokenType = null;
	//番号
	public TTextField txtRyokenNo = null;
	//国籍
	public TTextField txtKokuseiki = null;
	//購入者氏名
	public TTextField txtKonyuShaNm = null;
	//生年月日
	public TDateChooser dtBirthday = null;
	//上陸年月日
	public TDateChooser dtJorikuDate = null;
	//在留資格
	public TTextField txtZairyuShikaku = null;
	//記載事項言語
	public TComboBox cboLanguage = null;
	//注釈印字
	public TCheckBox  chkChushaku = null;
	//印刷免税書類
	public TCheckBox chkSeiyakuSho = null;
	public TCheckBox chkKirokuHyo = null;
	public TCheckBox chkKonpobuppinList =  null;

	public JPanel jPanelButton = null;

	//一般商品
	public JPanel jPanelNormal = null;
	//消耗品
	public JPanel jPanelShomohin = null;

	//ﾃｰﾌﾞﾙのｽｸﾛｰﾙﾍﾟｲﾝ
	public TScrollPane jScrollPaneNormal = null;
	public TScrollPane jScrollPaneShomohin = null;

	//ﾃｰﾌﾞﾙ
	public TEditTable teMainNormal = null;
	public TEditTable teMainShomohin = null;
	private Object objectMain[][] = new Object[][] {
	{ null, null, null, null, null }, { null, null, null, null, null } };

	//一般品用列名称(表示列)
	public int intMaxColNormal 					= 0;	//列を追加したらここも更新して（最終列＋1）
	public final int colTanpinCdNormal  			= intMaxColNormal++;	//単品ｺｰﾄﾞ
	public final int colRankNmNormal  				= intMaxColNormal++;	//ランク名
	public final int colTanpinNmNormal  			= intMaxColNormal++;	//タイトル
	public final int colSuNormal 					= intMaxColNormal++;	//数量
	public final int colKinNormal 					= intMaxColNormal++;	//金額
	//非表示列
	public final int colShohizeiNormal 			= intMaxColNormal++;	//消費税
	public final int colHanbaiTankaNormal 			= intMaxColNormal++;	//単価

	//ﾍｯﾀﾞｰ名　	※表示しないので空の配列でOK
	private String headerMainNormal[] = new String[intMaxColNormal];
	//ｶﾗﾑﾌｫｰﾏｯﾄ　	※表示のみなので空の配列でOK
	private String cellFormatMainNormal[] = new String[intMaxColNormal];
	//ｶﾗﾑﾃﾞｰﾀﾀｲﾌﾟ　※表示のみなので空の配列でOK
	private int cellDataTypeMainNormal[] = new int[intMaxColNormal];
	//ｶﾗﾑ表示調整　※表示列のみ後で設定設定
	private int cellAlignmentMainNormal[] = new int[intMaxColNormal];
	//ﾃｰﾌﾞﾙﾓﾃﾞﾙ
	public EditTableModel tblModelNormal = null;

	//振替先用列名称(表示列)
	public int intMaxColShomohin 					= 0;	//列を追加したらここも更新して（最終列＋1）
	public final int colTanpinCdShomohin 			= intMaxColShomohin++;	//単品ｺｰﾄﾞ
	public final int colRankNmShomohin 			= intMaxColShomohin++;	//ランク名
	public final int colTanpinNmShomohin  			= intMaxColShomohin++;	//タイトル
	public final int colSuShomohin 				= intMaxColShomohin++;	//数量
	public final int colKinShomohin 				= intMaxColShomohin++;	//金額
	//非表示列
	public final int colShohizeiShomohin 			= intMaxColShomohin++;	//消費税
	public final int colHanbaiTankaShomohin 		= intMaxColShomohin++;	//単価

	//ﾍｯﾀﾞｰ名　	※表示しないので空の配列でOK
	private String headerMainSaki[] = new String[intMaxColShomohin];
	//ｶﾗﾑﾌｫｰﾏｯﾄ　	※表示のみなので空の配列でOK
	private String cellFormatMainSaki[] = new String[intMaxColShomohin];
	//ｶﾗﾑﾃﾞｰﾀﾀｲﾌﾟ　※表示のみなので空の配列でOK
	private int cellDataTypeMainSaki[] = new int[intMaxColShomohin];
	//ｶﾗﾑ表示調整　※表示列のみ後で設定設定
	private int cellAlignmentMainSaki[] = new int[intMaxColShomohin];
	//ﾃｰﾌﾞﾙﾓﾃﾞﾙ
	public EditTableModel tblModelSaki = null;

	public Ttax0100(AppConfig appConfig) {
		super(appConfig);
		init();
	}

	/**
	 * 画面を初期化する
	 */
	private void init(){
		TLabel lblTilte= new TLabel();
		lblTilte.setBounds(new Rectangle(15, 60, 126, 31));
		lblTilte.setText("伝票番号");
		JLabel tlblWaku = new JLabel();
		tlblWaku.setText("");
		tlblWaku.setBounds(new Rectangle(140, 60, 101, 31));
		tlblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
		txtDenpyoNo = new TNumericField();
		txtDenpyoNo.setBounds(new Rectangle(143, 63, 95, 25));
		txtDenpyoNo.setHorizontalAlignment(JTextField.RIGHT);
		txtDenpyoNo.setNumericFormat("#");
		txtDenpyoNo.setMaxLength(8);
		txtDenpyoNo.setMinusInput(false);

		jMainPanel.add(lblTilte, null);
		jMainPanel.add(tlblWaku, null);
		jMainPanel.add(txtDenpyoNo, null);
		jMainPanel.add(getJPanelNormal(), null);
		jMainPanel.add(getJPanelShokohin(), null);
		jMainPanel.add(getJPanelArrow(), null);

		lblTilte = new TLabel();
		lblTilte.setBounds(new Rectangle(15, 435, 131, 31));
		lblTilte.setText("パスポート読込");
		tlblWaku = new JLabel();
		tlblWaku.setText("");
		tlblWaku.setBounds(new Rectangle(145, 435, 301, 31));
		tlblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
		txtPassport = new TTextField();
		txtPassport.setBounds(new Rectangle(148,438,295,25));
		txtPassport.setMaxLength(88);
		txtPassport.setText("");
		txtPassport.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		jMainPanel.add(lblTilte,null);
		jMainPanel.add(tlblWaku,null);
		jMainPanel.add(txtPassport,null);

		jMainPanel.add(getJPanelPasupoto(),null);

		this.setTitle("免税書類発行画面  TTAX0100");
		lblTitle.setText("　免税書類発行画面");
		setWidth();
	}

	private void setWidth() {
		int intWidthNormal[] = new int[intMaxColNormal];
		intWidthNormal[colTanpinCdNormal] 		= 100;//単品コード
		intWidthNormal[colRankNmNormal] 		= 58;//ランク
		intWidthNormal[colTanpinNmNormal] 		= 120;//タイトル
		intWidthNormal[colSuNormal] 			= 55;//数量
		intWidthNormal[colKinNormal] 			= 74;//金額

		//列幅設定
		int intMinWidth[] = new int[intMaxColNormal];
		int intMaxWidth[] = new int[intMaxColNormal];
		for (int i = 0; i < intMaxColNormal; i++) {
			intMinWidth[i] = 10;
			intMaxWidth[i] = 300;
		}
		TGuiUtil.setColumnWidth(teMainNormal, intWidthNormal,intMaxWidth,intMinWidth);

		int intWidthShokohin[] = new int[intMaxColShomohin];
		intWidthShokohin[colTanpinCdShomohin] 		= 100;//単品コード
		intWidthShokohin[colRankNmShomohin] 		= 58;//ランク
		intWidthShokohin[colTanpinNmShomohin] 		= 120;//タイトル
		intWidthShokohin[colSuShomohin] 			= 55;//数量
		intWidthShokohin[colKinShomohin] 			= 74;//金額

		//列幅設定
		int intMinWidthShokohin[] = new int[intMaxColShomohin];
		int intMaxWidthShokohin[] = new int[intMaxColShomohin];
		for (int i = 0; i < intMaxColShomohin; i++) {
			intMinWidthShokohin[i] = 10;
			intMaxWidthShokohin[i] = 300;
		}
		TGuiUtil.setColumnWidth(teMainShomohin, intWidthShokohin,intMaxWidth,intMinWidth);
	}
	private JPanel getJPanelNormal(){
		if ( jPanelNormal == null){

			jPanelNormal = new JPanel();
			jPanelNormal.setLayout(null);
			jPanelNormal.setBounds(new Rectangle(5, 100, 465, 324));
			jPanelNormal.setBorder(BorderFactory.createTitledBorder(null, "一般物品", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));
			jPanelNormal.add(getJScrollPaneNormal(), null);

			TLabel5 lblTitle = new TLabel5();
			lblTitle.setBounds(new Rectangle(160, 281, 66, 31));
			lblTitle.setText("数量合計");
			lblTitle.setFont(new Font("Dialog", Font.PLAIN, 12));
			lblTitle.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			txtKensuNormal = new TNumericField();
			txtKensuNormal.setBounds(new Rectangle(225, 281, 61, 31));
			txtKensuNormal.setText("12,345,678");
			txtKensuNormal.setHorizontalAlignment(JTextField.RIGHT);
			txtKensuNormal.setMaxLength(8);
			txtKensuNormal.setEnabled(false);
			txtKensuNormal.setBackground(Color.white);
			txtKensuNormal.setNumericFormat("##,###,###");
			txtKensuNormal.setFont(new Font("Dialog", Font.PLAIN, 12));
			txtKensuNormal.setBackground(Color.white);
			txtKensuNormal.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

			jPanelNormal.add(lblTitle,null);
			jPanelNormal.add(txtKensuNormal,null);

			lblTitle = new TLabel5();
			lblTitle.setBounds(new Rectangle(285, 281, 66, 31));
			lblTitle.setText("金額合計");
			lblTitle.setFont(new Font("Dialog", Font.PLAIN, 12));
			lblTitle.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			txtTotalKinNormal = new TNumericField();
			txtTotalKinNormal.setBounds(new Rectangle(350, 281, 78, 31));
			txtTotalKinNormal.setText("12,345,678");
			txtTotalKinNormal.setHorizontalAlignment(JTextField.RIGHT);
			txtTotalKinNormal.setMaxLength(8);
			txtTotalKinNormal.setEnabled(false);
			txtTotalKinNormal.setBackground(Color.white);
			txtTotalKinNormal.setNumericFormat("##,###,###");
			txtTotalKinNormal.setFont(new Font("Dialog", Font.PLAIN, 12));
			txtTotalKinNormal.setBackground(Color.white);
			txtTotalKinNormal.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

			jPanelNormal.add(lblTitle,null);
			jPanelNormal.add(txtTotalKinNormal,null);
		}
		return jPanelNormal;
	}
	/**
	 * This method initializes teMainMoto
	 *
	 * @return jp.co.css.webpos.common.gui.TEditTable
	 */
	private TEditTable getTeMainNormal() {
		if (teMainNormal == null) {
			//列の表示方法設定
			cellAlignmentMainNormal[colTanpinCdNormal]		= TEditTable.RIGHT;
			cellAlignmentMainNormal[colRankNmNormal]		= TEditTable.CENTER;
			cellAlignmentMainNormal[colTanpinNmNormal]		= TEditTable.LEFT;
			cellAlignmentMainNormal[colSuNormal]			= TEditTable.RIGHT;
			cellAlignmentMainNormal[colKinNormal]			= TEditTable.RIGHT;
			//END

			headerMainNormal[colTanpinCdNormal]		= "単品コード";
			headerMainNormal[colRankNmNormal]		= "ランク";
			headerMainNormal[colTanpinNmNormal]		= "タイトル";
			headerMainNormal[colSuNormal]			= "数量";
			headerMainNormal[colKinNormal]			= "金額";
			//END

			teMainNormal = new TEditTable(objectMain, headerMainNormal, cellAlignmentMainNormal,
									cellDataTypeMainNormal, cellFormatMainNormal);
			teMainNormal.setBackground(new Color(240, 250, 250));
			teMainNormal.setShowHorizontalLines(false);
			teMainNormal.setShowVerticalLines(true);
			teMainNormal.setGridColor(new Color(238, 238, 238));
			teMainNormal.setCellFont(new Font("Dialog", Font.PLAIN, 12));
			teMainNormal.headers.setBackground(new Color(220, 220, 220));
			teMainNormal.headers.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			teMainNormal.getTableHeader().setResizingAllowed(true);
			tblModelNormal = new EditTableModel(teMainNormal);
			teMainNormal.setModel(new EditTableModel(teMainNormal), true);
		}
		return teMainNormal;
	}
	private TScrollPane getJScrollPaneNormal() {
		if (jScrollPaneNormal == null) {
			jScrollPaneNormal = new TScrollPane();
			jScrollPaneNormal.setBounds(new Rectangle(5, 25, 455, 258));
			jScrollPaneNormal.setViewportView(getTeMainNormal());
			jScrollPaneNormal.setRowHeaderView(new RowHeader(teMainNormal,new ListModel(teMainNormal)));
			//横のｽｸﾛｰﾙﾊﾞｰを非表示にする
			jScrollPaneNormal.setHorizontalScrollBarPolicy(TScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jScrollPaneNormal.setVerticalScrollBarPolicy(TScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		}
		return jScrollPaneNormal;
	}

	private JPanel getJPanelShokohin(){
		if (jPanelShomohin == null){

			jPanelShomohin = new JPanel();
			jPanelShomohin.setLayout(null);
			jPanelShomohin.setBounds(new Rectangle(540, 100, 465, 324));
			jPanelShomohin.setBorder(BorderFactory.createTitledBorder(null, "消耗品", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));
			jPanelShomohin.add(getJScrollPaneShokohin(), null);

			TLabel5 lblTitle = new TLabel5();
			lblTitle.setBounds(new Rectangle(160, 281, 66, 31));
			lblTitle.setText("数量合計");
			lblTitle.setFont(new Font("Dialog", Font.PLAIN, 12));
			lblTitle.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			txtKensuShomohin = new TNumericField();
			txtKensuShomohin.setBounds(new Rectangle(225, 281, 61, 31));
			txtKensuShomohin.setText("12,345,678");
			txtKensuShomohin.setHorizontalAlignment(JTextField.RIGHT);
			txtKensuShomohin.setMaxLength(8);
			txtKensuShomohin.setEnabled(false);
			txtKensuShomohin.setBackground(Color.white);
			txtKensuShomohin.setNumericFormat("##,###,###");
			txtKensuShomohin.setFont(new Font("Dialog", Font.PLAIN, 12));
			txtKensuShomohin.setBackground(Color.white);
			txtKensuShomohin.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

			jPanelShomohin.add(lblTitle,null);
			jPanelShomohin.add(txtKensuShomohin,null);

			lblTitle = new TLabel5();
			lblTitle.setBounds(new Rectangle(285, 281, 66, 31));
			lblTitle.setText("金額合計");
			lblTitle.setFont(new Font("Dialog", Font.PLAIN, 12));
			lblTitle.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			txtTotalKinShomohin = new TNumericField();
			txtTotalKinShomohin.setBounds(new Rectangle(350, 281, 78, 31));
			txtTotalKinShomohin.setText("12,345,678");
			txtTotalKinShomohin.setHorizontalAlignment(JTextField.RIGHT);
			txtTotalKinShomohin.setMaxLength(8);
			txtTotalKinShomohin.setEnabled(false);
			txtTotalKinShomohin.setBackground(Color.white);
			txtTotalKinShomohin.setNumericFormat("##,###,###");
			txtTotalKinShomohin.setFont(new Font("Dialog", Font.PLAIN, 12));
			txtTotalKinShomohin.setBackground(Color.white);
			txtTotalKinShomohin.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

			jPanelShomohin.add(lblTitle,null);
			jPanelShomohin.add(txtTotalKinShomohin,null);

		}
		return jPanelShomohin;
	}

	private TEditTable getTeMainShomohin() {
		if (teMainShomohin == null) {
			//列の表示方法設定

			cellAlignmentMainSaki[colTanpinCdShomohin]		= TEditTable.RIGHT;
			cellAlignmentMainSaki[colRankNmShomohin]		= TEditTable.CENTER;
			cellAlignmentMainSaki[colTanpinNmShomohin]		= TEditTable.LEFT;
			cellAlignmentMainSaki[colSuShomohin]			= TEditTable.RIGHT;
			cellAlignmentMainSaki[colKinShomohin]			= TEditTable.RIGHT;
			//END
			headerMainSaki[colTanpinCdShomohin]				= "単品コード";
			headerMainSaki[colRankNmShomohin]				= "ランク";
			headerMainSaki[colTanpinNmShomohin]				= "タイトル";
			headerMainSaki[colSuShomohin]					= "数量";
			headerMainSaki[colKinShomohin]					= "金額";
			//END
			teMainShomohin = new TEditTable(objectMain, headerMainSaki, cellAlignmentMainSaki,
									cellDataTypeMainSaki, cellFormatMainSaki);
			teMainShomohin.setBackground(new Color(240, 250, 250));
			teMainShomohin.setShowHorizontalLines(false);
			teMainShomohin.setShowVerticalLines(true);
			teMainShomohin.setGridColor(new Color(238, 238, 238));
			teMainShomohin.setCellFont(new Font("Dialog", Font.PLAIN, 12));
			teMainShomohin.headers.setBackground(new Color(220, 220, 220));
			teMainShomohin.headers.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			teMainShomohin.getTableHeader().setResizingAllowed(true);
			tblModelSaki = new EditTableModel(teMainShomohin);
			teMainShomohin.setModel(new EditTableModel(teMainShomohin), true);

		}
		return teMainShomohin;
	}

	/**
	 * This method initializes jScrollPaneShokohin
	 *
	 * @return javax.swing.TScrollPane
	 */
	private TScrollPane getJScrollPaneShokohin() {
		if (jScrollPaneShomohin == null) {
			jScrollPaneShomohin = new TScrollPane();
			jScrollPaneShomohin.setBounds(new Rectangle(5, 25, 455, 258));
			jScrollPaneShomohin.setViewportView(getTeMainShomohin());
			jScrollPaneShomohin.setRowHeaderView(new RowHeader(teMainShomohin,new ListModel(teMainShomohin)));

			//横のｽｸﾛｰﾙﾊﾞｰを非表示にする
			jScrollPaneShomohin.setHorizontalScrollBarPolicy(TScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jScrollPaneShomohin.setVerticalScrollBarPolicy(TScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		}
		return jScrollPaneShomohin;
	}

	private JPanel getJPanelArrow(){
		if ( jPanelButton == null){
			jPanelButton = new JPanel();
			jPanelButton.setLayout(null);
			jPanelButton.setBounds(new Rectangle(468, 100, 85, 200));

			btnToRight = new TButton();
			btnToRight.setText("→");
			btnToRight.setBounds(new Rectangle(5,120,65,31));

			btnToLeft = new TButton();
			btnToLeft.setText("←");
			btnToLeft.setBounds(new Rectangle(5,167,65,31));

			jPanelButton.add(btnToRight,null);
			jPanelButton.add(btnToLeft,null);
		}
		return jPanelButton;
	}
	private JPanel getJPanelPasupoto() {
		if(jPanelPassport == null) {
			jPanelPassport = new JPanel();
			jPanelPassport.setLayout(null);
			jPanelPassport.setBounds(new Rectangle(5,470 , 999, 193));
			jPanelPassport.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));

			//旅券の種類
			int intY = 10;
			TLabel lblTitle = new TLabel();
			lblTitle.setBounds(new Rectangle(10, intY, 131, 31));
			lblTitle.setText("旅券等の種類");
			JLabel tlblWaku = new JLabel();
			tlblWaku.setText("");
			tlblWaku.setBounds(new Rectangle(140, intY, 211, 31));
			tlblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtRyokenType = new TTextField();
			txtRyokenType.setText("");
			txtRyokenType.setMaxLength(50);
			txtRyokenType.setBounds(new Rectangle(143,intY+3,205,25));
			txtRyokenType.setHorizontalAlignment(JTextField.LEFT);
			txtRyokenType.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtRyokenType.setIMType(TTextField.IM_HIRAGANA);

			jPanelPassport.add(lblTitle,null);
			jPanelPassport.add(tlblWaku,null);
			jPanelPassport.add(txtRyokenType,null);
			//番号
			intY +=36;
			lblTitle = new TLabel();
			lblTitle.setBounds(new Rectangle(10, intY, 131, 31));
			lblTitle.setText("番号");
			tlblWaku = new JLabel();
			tlblWaku.setText("");
			tlblWaku.setBounds(new Rectangle(140, intY, 211, 31));
			tlblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtRyokenNo = new TTextField();
			txtRyokenNo.setText("");
			txtRyokenNo.setMaxLength(9);
			txtRyokenNo.setBounds(new Rectangle(143,intY+3,205,25));
			txtRyokenNo.setHorizontalAlignment(JTextField.LEFT);
			txtRyokenNo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

			jPanelPassport.add(lblTitle,null);
			jPanelPassport.add(tlblWaku,null);
			jPanelPassport.add(txtRyokenNo,null);
			//国籍
			intY +=36;
			lblTitle = new TLabel();
			lblTitle.setBounds(new Rectangle(10, intY, 131, 31));
			lblTitle.setText("国籍");
			tlblWaku = new JLabel();
			tlblWaku.setText("");
			tlblWaku.setBounds(new Rectangle(140, intY, 211, 31));
			tlblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtKokuseiki = new TTextField();
			txtKokuseiki.setText("");
			txtKokuseiki.setMaxLength(50);
			txtKokuseiki.setBounds(new Rectangle(143,intY+3,205,25));
			txtKokuseiki.setHorizontalAlignment(JTextField.LEFT);
			txtKokuseiki.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtKokuseiki.setIMType(TTextField.IM_HIRAGANA);

			jPanelPassport.add(lblTitle,null);
			jPanelPassport.add(tlblWaku,null);
			jPanelPassport.add(txtKokuseiki,null);
			//購入者氏名
			intY +=36;
			lblTitle = new TLabel();
			lblTitle.setBounds(new Rectangle(10, intY, 131, 31));
			lblTitle.setText("購入者氏名");
			tlblWaku = new JLabel();
			tlblWaku.setText("");
			tlblWaku.setBounds(new Rectangle(140, intY, 211, 31));
			tlblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtKonyuShaNm = new TTextField();
			txtKonyuShaNm.setText("");
			txtKonyuShaNm.setMaxLength(39);
			txtKonyuShaNm.setBounds(new Rectangle(143,intY+3,205,25));
			txtKonyuShaNm.setHorizontalAlignment(JTextField.LEFT);
			txtKonyuShaNm.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

			jPanelPassport.add(lblTitle,null);
			jPanelPassport.add(tlblWaku,null);
			jPanelPassport.add(txtKonyuShaNm,null);
			//生年月日
			intY +=36;
			lblTitle = new TLabel();
			lblTitle.setBounds(new Rectangle(10, intY, 131, 31));
			lblTitle.setText("生年月日");
			tlblWaku = new JLabel();
			tlblWaku.setText("");
			tlblWaku.setBounds(new Rectangle(140, intY, 134, 31));
			tlblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			dtBirthday = new TDateChooser();
			dtBirthday.setText("");
			dtBirthday.setBounds(new Rectangle(143,intY+3,128,25));

			jPanelPassport.add(lblTitle,null);
			jPanelPassport.add(tlblWaku,null);
			jPanelPassport.add(dtBirthday,null);
			//上陸年月日
			int intX = 401;
			intY = 10;
			lblTitle = new TLabel();
			lblTitle.setBounds(new Rectangle(intX, intY, 131, 31));
			lblTitle.setText("上陸年月日");
			tlblWaku = new JLabel();
			tlblWaku.setText("");
			tlblWaku.setBounds(new Rectangle(intX+130, intY, 134, 31));
			tlblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			dtJorikuDate = new TDateChooser();
			dtJorikuDate.setText("");
			dtJorikuDate.setBounds(new Rectangle(intX+133,intY+3,128,25));

			jPanelPassport.add(lblTitle,null);
			jPanelPassport.add(tlblWaku,null);
			jPanelPassport.add(dtJorikuDate,null);
			//在留資格
			intY +=36;
			lblTitle = new TLabel();
			lblTitle.setBounds(new Rectangle(intX, intY, 131, 31));
			lblTitle.setText("在留資格");
			tlblWaku = new JLabel();
			tlblWaku.setText("");
			tlblWaku.setBounds(new Rectangle(intX+130, intY, 156, 31));
			tlblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtZairyuShikaku = new TTextField();
			txtZairyuShikaku.setText("");
			txtZairyuShikaku.setMaxLength(20);
			txtZairyuShikaku.setBounds(new Rectangle(intX+133,intY+3,150,25));
			txtZairyuShikaku.setHorizontalAlignment(JTextField.LEFT);
			txtZairyuShikaku.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtZairyuShikaku.setIMType(TTextField.IM_HIRAGANA);

			jPanelPassport.add(lblTitle,null);
			jPanelPassport.add(tlblWaku,null);
			jPanelPassport.add(txtZairyuShikaku,null);
			//記載事項言語
			intY +=36;
			lblTitle = new TLabel();
			lblTitle.setBounds(new Rectangle(intX, intY, 131, 31));
			lblTitle.setText("記載事項言語");
			tlblWaku = new JLabel();
			tlblWaku.setText("");
			tlblWaku.setBounds(new Rectangle(intX+130, intY, 156, 31));
			tlblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			cboLanguage = new TComboBox();
			cboLanguage.setBounds(new Rectangle(intX+133,intY+3,150,25));

			jPanelPassport.add(lblTitle,null);
			jPanelPassport.add(tlblWaku,null);
			jPanelPassport.add(cboLanguage,null);
			//注釈印字
			intY +=36;
			lblTitle = new TLabel();
			lblTitle.setBounds(new Rectangle(intX, intY, 131, 31));
			lblTitle.setText("注釈印字");
			tlblWaku = new JLabel();
			tlblWaku.setText("");
			tlblWaku.setBounds(new Rectangle(intX+130, intY, 156, 31));
			tlblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			chkChushaku = new TCheckBox();
			chkChushaku.setBounds(new Rectangle(intX+135, intY+3, 150, 25));
			chkChushaku.setText("英語の注釈を印字する");

			jPanelPassport.add(lblTitle,null);
			jPanelPassport.add(tlblWaku,null);
			jPanelPassport.add(chkChushaku,null);
			//印刷免税書類
			intY +=36;
			lblTitle = new TLabel();
			lblTitle.setBounds(new Rectangle(intX, intY, 131, 31));
			lblTitle.setText("印刷免税書類");
			tlblWaku = new JLabel();
			tlblWaku.setText("");
			tlblWaku.setBounds(new Rectangle(intX+130, intY, 453, 31));
			tlblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			chkSeiyakuSho = new TCheckBox();
			chkSeiyakuSho.setBounds(new Rectangle(intX+135, intY+3, 111, 25));
			chkSeiyakuSho.setText("購入者誓約書");
			chkKirokuHyo = new TCheckBox();
			chkKirokuHyo.setBounds(new Rectangle(intX+135+125, intY+3, 111, 25));
			chkKirokuHyo.setText("購入記録票");
			chkKonpobuppinList = new TCheckBox();
			chkKonpobuppinList.setBounds(new Rectangle(intX+135+125+125, intY+3, 190, 25));
			chkKonpobuppinList.setText("梱包物品リスト（消耗品のみ）");

			jPanelPassport.add(lblTitle,null);
			jPanelPassport.add(tlblWaku,null);
			jPanelPassport.add(chkSeiyakuSho,null);
			jPanelPassport.add(chkKirokuHyo,null);
			jPanelPassport.add(chkKonpobuppinList,null);

		}
		return jPanelPassport;
	}
}
