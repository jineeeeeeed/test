package jp.co.css.TTSA;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import jp.co.css.base.AppConfig;
import jp.co.css.base.TFrameNew;
import jp.co.css.talos_l.util.Constants;
import jp.co.css.webpos.common.gui.ColumnGroup;
import jp.co.css.webpos.common.gui.EditTableModel;
import jp.co.css.webpos.common.gui.GroupableTableHeader;
import jp.co.css.webpos.common.gui.ListModel;
import jp.co.css.webpos.common.gui.RowHeader;
import jp.co.css.webpos.common.gui.TComboBox;
import jp.co.css.webpos.common.gui.TEditTable;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.gui.TLabel;
import jp.co.css.webpos.common.gui.TLabel2;
import jp.co.css.webpos.common.gui.TLabel6;
import jp.co.css.webpos.common.gui.TScrollPane;
import jp.co.css.webpos.common.gui.TTextField;
/**---------------------------------------------
 * 処理名称	： TIME_SHARE出品登録画面
 * 作成日	： 2012/08/08
 * 作成者	： ZWH
---------------------------------------------**/
public class Ttsa0100 extends TFrameNew {

	private static final long serialVersionUID = 1L;

	public JPanel jPanelKey = null;
	public JLabel lblKeyWaku = null;
	public TLabel tlblKey = null;
	public TTextField txtKey = null;

	//ﾃｰﾌﾞﾙのｽｸﾛｰﾙﾍﾟｲﾝ
	public TScrollPane jScrollPane = null;

	/**
	 * teMain:メインテーブル
	 */
	public TEditTable teMain = null;
	private Object objectMain[][] = new Object[][] {
	{ null, null, null, null, null }, { null, null, null, null, null } };

	private int colCount = 0;
	public final int colGroup1			= colCount++;	// 出品状態/出品可否
	public final int colGroup2			= colCount++;	// 商品ｺｰﾄﾞ/ﾗﾝｸ
	public final int colTanpinCd		= colCount++;	// 単品コード
	public final int colTitle			= colCount++;	// 商品ﾀｲﾄﾙ
	public final int colDaibunrui		= colCount++;	// 大分類
	public final int colHanbaiKakaku	= colCount++;	// 販売価格
	public final int colGenka			= colCount++;	// 原価
	public final int colshuppinYoteiSu	= colCount++;	// 出品予定数
	public final int colGenzaiShuppinSu	= colCount++;	// 出品済数
	public final int colZaikoSu	 	 	= colCount++;	// 在庫数(現在在庫数-引当数)

	//商品情報
	public final int colTeika			= colCount++;	// 定価
	public final int colSubTitle		= colCount++;	// 商品ｻﾌﾞﾀｲﾄﾙ
	public final int colSubTitleKana	= colCount++;	// 商品ｻﾌﾞﾀｲﾄﾙカナ
	public final int colShohinCd	 	= colCount++;	// 商品ｺｰﾄﾞ
	public final int colShohinKanaNm 	= colCount++;	// 商品名称カナ
	public final int colShohinNm2 		= colCount++;	// 商品名称2
	public final int colShohinKanaNm2	= colCount++;	// 商品名称2カナ
	public final int colJanCd 			= colCount++;	// JANｺｰﾄﾞ
	public final int colKikakuNo		= colCount++;	// 規格番号
	public final int colMakerCd 		= colCount++;	// ﾒｰｶｰｺｰﾄﾞ
	public final int colMakerNm 		= colCount++;	// ﾒｰｶｰ名称
	public final int colMakerKanaNm		= colCount++;	// ﾒｰｶｰ名称カナ
	public final int colHatubaiDate 	= colCount++;	// 発売日
	public final int colLabelCd 		= colCount++;	// ﾚｰﾍﾞﾙｺｰﾄﾞ
	public final int colLabelNm 		= colCount++;	// ﾚｰﾍﾞﾙ名称
	public final int colLabelKanaNm 	= colCount++;	// ﾚｰﾍﾞﾙ名称カナ
	public final int colDaibunCd 		= colCount++;	// 大分類ｺｰﾄﾞ
	public final int colDaibunNm 		= colCount++;	// 大分類名称
	public final int colDaibunRyakuNm	= colCount++;	// 大分類略称
	public final int colChubunCd 		= colCount++;	// 中分類ｺｰﾄﾞ
	public final int colChubunNm 		= colCount++;	// 中分類名称
	public final int colChubunRyakuNm	= colCount++;	// 中分類略称
	public final int colShobunCd 		= colCount++;	// 小分類ｺｰﾄﾞ
	public final int colShobunNm 		= colCount++;	// 小分類名称
	public final int colShobunRyakuNm	= colCount++;	// 小分類略称
	public final int colRankNm	 	 	= colCount++;	// 価格管理ﾗﾝｸ名称
	public final int colRankCd	 	 	= colCount++;	// 価格管理ﾗﾝｸ
	public final int colShuppinJotai	= colCount++;	// 出品状態

	public final int colShuppinKahi		= colCount++;	// 出品可否
	public final int colTorikomiZumiFlg	= colCount++;	// 取込済みﾌﾗｸﾞ

	public final int colHanbaiKakakuBefore = colCount++;// 元販売価格
	public final int colKakakuKoteiFlg	= colCount++;	// 価格固定

	//在庫情報
	public final int colHikiateSu 	 	= colCount++;	// 引当数
	public final int colSku				= colCount++;	// SKU

	public final int colShuppinKinsiFlg = colCount++;	// 出品禁止ﾌﾗｸﾞ

	public final int colSeizouNo		= colCount++;	// 製造番号

	public final int intMaxCol 			= colCount++;	//列を追加したらここも更新して（最終列＋1）

	//カラム幅
	public final int cellLen[] = new int[intMaxCol];
	//カラムIMタイプ
	private int cellIMTypeMain[] = new int[intMaxCol];
	//ﾍｯﾀﾞｰ名　	※表示しないので空の配列でOK
	private String headerMain[] = new String[intMaxCol];
	//ｶﾗﾑﾌｫｰﾏｯﾄ　	※表示のみなので空の配列でOK
	private String cellFormatMain[] = new String[intMaxCol];
	//ｶﾗﾑﾃﾞｰﾀﾀｲﾌﾟ　※表示のみなので空の配列でOK
	private int cellDataTypeMain[] = new int[intMaxCol];
	//ｶﾗﾑ表示調整　※表示列のみ後で設定設定
	private int cellAlignmentMain[] = new int[intMaxCol];
	int intEditCol[] = new int[]{colHanbaiKakaku,colshuppinYoteiSu};
	//ﾃｰﾌﾞﾙﾓﾃﾞﾙ
	public EditTableModel tblModel = null;

	public TComboBox cboCondition = null;
	private JPanel pnlFooter = null;
	//下方ﾗﾍﾞﾙ(商品詳細)

	public TLabel2 tlblShohinCd = null;
	public TLabel2 tlblJanCd = null;
	public TLabel2 tlblMaker = null;
	public TLabel2 tlblLabel = null;
	public TLabel2 tlblChubunrui = null;
	public TLabel2 tlblShobunrui = null;
	public TLabel2 tlblHatubai = null;
	public TLabel2 tlblTeika = null;
	public TLabel2 tlblHinban = null;
	public TLabel2 tlblZeiKb = null;
	public TLabel2 tlblDaibunrui = null;
	public TLabel2 tlblShohinNm2 = null;
	public TLabel6 lblShohinCd = null;
	public TLabel6 lblJanCd = null;
	public TLabel6 lblTeika = null;
	public TLabel6 lblHinban = null;
	public TLabel6 lblZeiKb = null;
	public TLabel6 lblDaibunrui = null;
	public TLabel6 lblChubunrui = null;
	public TLabel6 lblShobunrui = null;
	public TLabel6 lblHatubai = null;
	public TLabel6 lblMaker = null;
	public TLabel6 lblLabel = null;
	public TLabel6 lblShohinNm2 = null;
	public TLabel2 tlblBiko = null;
	public TLabel6 lblBiko = null;
	public TLabel6 lblShohinNm = null;
	public TLabel2 tlblShohinNm = null;
	public TLabel6 lblTanpinCd = null;
	public TLabel2 tlblTanpinCd = null;

	/**
	 * This is the default constructor
	 */
	public Ttsa0100(AppConfig appConfig) {
		super(appConfig);
		jMainPanel.add(getjPanelKey(), null);
		jMainPanel.add(getJScrollPane1(), null);
		jMainPanel.add(getPnlFooter(),null);
		this.setTitle("通販出品登録画面　TTSA0100");
		lblTitle.setText("　通 販 出 品 登 録 画 面");

		init();
	}

	/**
	 * This method initializes this
	 * @return void
	 */
	public void init() {

		int intWidth[] = new int[]{
				66,		//出品状態
				96,		//商品ｺｰﾄﾞ/ﾗﾝｸ
				96,		//単品コード
				241+52,	//タイトル
				76,		//大分類
				92,		//販売価格
				80,		//原価
				46,		//出品予定数
				51,		//出品済数
				46,		//在庫数
				};

		//ﾒｲﾝﾃｰﾌﾞﾙの列ｻｲｽﾞの設定 ※表示部分のみ
		TGuiUtil.setColumnWidth( teMain, intWidth );
	}

	/**
	 * This method initializes teMain
	 *
	 * @return jp.co.css.webpos.common.gui.TEditTable
	 */
	private TEditTable getTeMain() {
		if (teMain == null) {
			SetTableAlignment();
			teMain = new TEditTable(objectMain, headerMain, cellLen, cellAlignmentMain,
					cellIMTypeMain, cellDataTypeMain, cellFormatMain){
				private static final long serialVersionUID = 1L;
				public Component prepareEditor(TableCellEditor editor, int row, int column) {
					Component comp = super.prepareEditor(editor, row, column);
					if ( comp != null ){
						if ( comp instanceof JTextField ){
							((JTextField)comp).setEditable(true);
							if ( column == colHanbaiKakaku && teMain.getValueString(row, colKakakuKoteiFlg).equals("true")){
								((JTextField)comp).setEditable(false);
							}
						}
					}

					return comp;
				}
			};
			teMain.setEditingColumns(intEditCol);
			teMain.setHeaderHeight(20);
			teMain.setBackground(new Color(240, 250, 250));
			teMain.setShowHorizontalLines(false);
			teMain.setShowVerticalLines(false);
			teMain.setGridColor(new Color(238, 238, 238));
			teMain.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 14));
			teMain.headers.setBackground(new Color(220, 220, 220));
			teMain.headers.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			tblModel = new EditTableModel(teMain);
			teMain.setModel(tblModel, true);
			teMain.setDefaultRenderer(String.class, new EditTableCellRenderer(teMain));
			teMain.setCellHeight(30);
			teMain.setRowHeight(30);

			int group3Height = 20;
			TableColumnModel cm = teMain.getColumnModel();

			ColumnGroup grpCol1 = new ColumnGroup("出品状態");
			grpCol1.setHeight(group3Height);
			grpCol1.add(cm.getColumn(colGroup1));

			ColumnGroup grpCol2 = new ColumnGroup("商品コード");
			grpCol2.setHeight(group3Height);
			grpCol2.add(cm.getColumn(colGroup2));

			GroupableTableHeader header = (GroupableTableHeader) teMain.getTableHeader();
			header.addColumnGroup(grpCol1);
			header.addColumnGroup(grpCol2);
			header.updateUI();

		}
		return teMain;
	}

	/**
	 * This method initializes jScrollPane1
	 *
	 * @return javax.swing.TScrollPane
	 */
	private TScrollPane getJScrollPane1() {
		if (jScrollPane == null) {
	        jScrollPane = new TScrollPane(getTeMain());
	        jScrollPane.setBounds(new Rectangle(15, 100, 990, 438));
	        jScrollPane.setViewportView(getTeMain());
			jScrollPane.setRowHeaderView(new RowHeader(teMain,new ListModel(teMain)));
			//垂直のｽｸﾛｰﾙﾊﾞｰを表示にする
			jScrollPane.setVerticalScrollBarPolicy(TScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		}
		return jScrollPane;
	}


	private void SetTableAlignment(){

		//列の表示方法設定
		cellAlignmentMain[colGroup1]				= TEditTable.CENTER;
		cellAlignmentMain[colGroup2]				= TEditTable.RIGHT;
		cellAlignmentMain[colTanpinCd]				= TEditTable.RIGHT;
		cellAlignmentMain[colTitle]					= TEditTable.LEFT;
		cellAlignmentMain[colDaibunrui]				= TEditTable.LEFT;
		cellAlignmentMain[colKakakuKoteiFlg]		= TEditTable.CENTER;
		cellAlignmentMain[colHanbaiKakaku]			= TEditTable.RIGHT;
		cellAlignmentMain[colshuppinYoteiSu]		= TEditTable.RIGHT;
		cellAlignmentMain[colGenka]					= TEditTable.RIGHT;
		cellAlignmentMain[colGenzaiShuppinSu]		= TEditTable.RIGHT;
		cellAlignmentMain[colZaikoSu]				= TEditTable.RIGHT;

		headerMain[colGroup1]						= "出品可否";
		headerMain[colGroup2]						= "ランク";
		headerMain[colTanpinCd]						= "単品コード";
		headerMain[colTitle]						= "タイトル";
		headerMain[colDaibunrui]					= "大分類";
		headerMain[colKakakuKoteiFlg] 				= "<HTML>改定<BR>除外</HTML>";
		headerMain[colHanbaiKakaku] 				= "出品価格";
		headerMain[colGenka]						= "原価";
		headerMain[colshuppinYoteiSu]				= "出品数";
		headerMain[colGenzaiShuppinSu]				= "出品済数";
		headerMain[colZaikoSu]						= "在庫数";

		cellIMTypeMain[colHanbaiKakaku]				= TEditTable.IM_OFF;
		cellIMTypeMain[colshuppinYoteiSu]			= TEditTable.IM_OFF;

		cellLen[colHanbaiKakaku]					= 8;
		cellLen[colshuppinYoteiSu]					= 6;//8桁→6桁変更

		cellDataTypeMain[colKakakuKoteiFlg]			= TEditTable.TYPE_BOOLEAN;
		cellDataTypeMain[colHanbaiKakaku]			= TEditTable.TYPE_NUMERIC;
		cellDataTypeMain[colshuppinYoteiSu]			= TEditTable.TYPE_NUMERIC;

		cellFormatMain[colHanbaiKakaku]				= "###,##0";
		cellFormatMain[colshuppinYoteiSu]			= "###,##0";
	}

	/**
	 * This method initializes pnlFooter
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPnlFooter() {
		if (pnlFooter == null) {

			tlblShohinNm = new TLabel2();
			tlblShohinNm.setBounds(new Rectangle(15, 0, 101, 26));
			tlblShohinNm.setText("商品名称2");
			lblShohinNm = new TLabel6();
			lblShohinNm.setBounds(new Rectangle(115, 0, 400, 26));
			lblShohinNm.setText("あいうえおかきくけこ");
			lblShohinNm.setHorizontalAlignment(SwingConstants.LEFT);

			tlblShohinNm2 = new TLabel2();
			tlblShohinNm2.setBounds(new Rectangle(520, 0, 111, 26));
			tlblShohinNm2.setText("サブタイトル");
			lblShohinNm2 = new TLabel6();
			lblShohinNm2.setBounds(new Rectangle(630, 0, 166, 26));
			lblShohinNm2.setText("あいうえおかきくけこさしすせそ");
			lblShohinNm2.setHorizontalAlignment(SwingConstants.LEFT);

			tlblShohinCd = new TLabel2();
			tlblShohinCd.setBounds(new Rectangle(15, 30, 101, 26));
			tlblShohinCd.setText("商品コード");
			lblShohinCd = new TLabel6();
			lblShohinCd.setBounds(new Rectangle(115, 30, 106, 26));
			lblShohinCd.setText("1234567890123");
			lblShohinCd.setHorizontalAlignment(SwingConstants.RIGHT);

			tlblDaibunrui = new TLabel2();
			tlblDaibunrui.setBounds(new Rectangle(520, 30, 111, 26));
			tlblDaibunrui.setText("大分類");
			lblDaibunrui = new TLabel6();
			lblDaibunrui.setBounds(new Rectangle(630, 30, 166, 26));
			lblDaibunrui.setHorizontalAlignment(SwingConstants.LEFT);
			lblDaibunrui.setText("12：あいう");

			tlblHinban = new TLabel2();
			tlblHinban.setBounds(new Rectangle(225, 30, 101, 26));
			tlblHinban.setText("品番");
			lblHinban = new TLabel6();
			lblHinban.setBounds(new Rectangle(325, 30, 190, 26));
			lblHinban.setHorizontalAlignment(SwingConstants.LEFT);
			lblHinban.setText("あいうえお");

			//---------------------------------------------------
			tlblJanCd = new TLabel2();
			tlblJanCd.setBounds(new Rectangle(15, 60, 101, 26));
			tlblJanCd.setText("JANコード");
			lblJanCd = new TLabel6();
			lblJanCd.setBounds(new Rectangle(115, 60, 106, 26));
			lblJanCd.setHorizontalAlignment(SwingConstants.LEFT);
			lblJanCd.setText("1234567890123");

			tlblChubunrui = new TLabel2();
			tlblChubunrui.setBounds(new Rectangle(520, 60, 111, 26));
			tlblChubunrui.setText("中分類");
			lblChubunrui = new TLabel6();
			lblChubunrui.setBounds(new Rectangle(630, 60, 166, 26));
			lblChubunrui.setHorizontalAlignment(SwingConstants.LEFT);
			lblChubunrui.setText("1234：あいう");

			tlblHatubai = new TLabel2();
			tlblHatubai.setBounds(new Rectangle(15, 90, 101, 26));
			tlblHatubai.setText("発売日");
			lblHatubai = new TLabel6();
			lblHatubai.setBounds(new Rectangle(115, 90, 106, 26));
			lblHatubai.setText("0000/00/00");
			lblHatubai.setHorizontalAlignment(SwingConstants.LEFT);

			tlblShobunrui = new TLabel2();
			tlblShobunrui.setBounds(new Rectangle(520, 90, 111, 26));
			tlblShobunrui.setText("小分類");
			lblShobunrui = new TLabel6();
			lblShobunrui.setBounds(new Rectangle(630, 90, 166, 26));
			lblShobunrui.setHorizontalAlignment(SwingConstants.LEFT);
			lblShobunrui.setText("123456：あいう");

			tlblMaker = new TLabel2();
			tlblMaker.setBounds(new Rectangle(225, 60, 101, 26));
			tlblMaker.setText("メーカー");
			lblMaker = new TLabel6();
			lblMaker.setBounds(new Rectangle(325, 60, 190, 26));
			lblMaker.setText("123456789：あいう");
			lblMaker.setHorizontalAlignment(SwingConstants.LEFT);

			tlblLabel = new TLabel2();
			tlblLabel.setBounds(new Rectangle(225, 90, 101, 26));
			tlblLabel.setText("レーベル");
			lblLabel = new TLabel6();
			lblLabel.setBounds(new Rectangle(325, 90, 190, 26));
			lblLabel.setText("");
			lblLabel.setHorizontalAlignment(SwingConstants.LEFT);

			tlblTanpinCd = new TLabel2();
			tlblTanpinCd.setBounds(new Rectangle(810, 0, 81, 26));
			tlblTanpinCd.setText("単品コード");
			lblTanpinCd = new TLabel6();
			lblTanpinCd.setBounds(new Rectangle(890, 0, 108, 26));
			lblTanpinCd.setText("1234567890123");
			lblTanpinCd.setHorizontalAlignment(SwingConstants.RIGHT);

			tlblTeika = new TLabel2();
			tlblTeika.setBounds(new Rectangle(810, 30, 81, 26));
			tlblTeika.setText("定価(税抜)");
			lblTeika = new TLabel6();
			lblTeika.setBounds(new Rectangle(890, 30, 108, 26));
			lblTeika.setText("12,345,678");
			lblTeika.setHorizontalAlignment(SwingConstants.RIGHT);

			pnlFooter = new JPanel();
			pnlFooter.setLayout(null);
			pnlFooter.setBounds(new Rectangle(5, 548, 1020, 126));

			pnlFooter.add(lblShohinNm,null);
			pnlFooter.add(tlblShohinNm,null);
			pnlFooter.add(tlblShohinCd,null);
			pnlFooter.add(lblShohinCd,null);
			pnlFooter.add(tlblDaibunrui,null);
			pnlFooter.add(lblDaibunrui,null);
			pnlFooter.add(tlblHinban,null);
			pnlFooter.add(lblHinban,null);
			pnlFooter.add(tlblJanCd,null);
			pnlFooter.add(lblJanCd,null);
			pnlFooter.add(tlblShohinNm2,null);
			pnlFooter.add(lblShohinNm2,null);
			pnlFooter.add(tlblChubunrui,null);
			pnlFooter.add(lblChubunrui,null);
			pnlFooter.add(tlblHatubai,null);
			pnlFooter.add(lblHatubai,null);
			pnlFooter.add(tlblShobunrui,null);
			pnlFooter.add(lblShobunrui,null);
			pnlFooter.add(tlblTeika,null);
			pnlFooter.add(lblTeika,null);
			pnlFooter.add(tlblMaker,null);
			pnlFooter.add(lblMaker,null);
			pnlFooter.add(tlblLabel,null);
			pnlFooter.add(lblLabel,null);
			pnlFooter.add(tlblTanpinCd,null);
			pnlFooter.add(lblTanpinCd,null);
		}
		return pnlFooter;
	}

	public class EditTableCellRenderer extends TEditTable.EditTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public EditTableCellRenderer(TEditTable et) {

			et.super();
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			//出品不可の場合、行文字は赤へ変更する
			if (teMain.getValueInt(row, colShuppinKahi) == Constants.AMAZON_SHUPPINKAHI_FUKA){
				setForeground(Color.RED);
			}
			//出品可の場合、行文字は黒へ変更する
			else{
				setForeground(Color.black);
			}

			return comp;
		}

	}

	/**
	 * This method initializes jPanelBunruiey
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getjPanelKey() {
		if (jPanelKey == null) {
			int intX = 15;
			int intY = 20;

			tlblKey = new TLabel();
			tlblKey.setText("コード");
			tlblKey.setBounds(new Rectangle(intX, intY, 121, 31));
			lblKeyWaku = new JLabel();
			lblKeyWaku.setText("");
			lblKeyWaku.setBounds(new Rectangle(intX+120, intY, 161, 31));
			lblKeyWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtKey = new TTextField();
			txtKey.setText("");
			txtKey.setBounds(new Rectangle(intX+123, intY+3, 155, 25));
			txtKey.setMaxLength(18);
			txtKey.setHorizontalAlignment(JTextField.RIGHT);
			txtKey.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 18));
			txtKey.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtKey.setIMType(TTextField.IM_OFF);

			jPanelKey = new JPanel();
			jPanelKey.setLayout(null);
			jPanelKey.setBounds(new Rectangle(5, 40, 550, 56));
			//jPanelKey.setBorder(BorderFactory.createTitledBorder(null, "KEY項目", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));
			jPanelKey.add(lblKeyWaku, null);
			jPanelKey.add(tlblKey, null);
			jPanelKey.add(txtKey, null);
		}
		return jPanelKey;
	}
}
