package jp.co.css.TTSA;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import jp.co.css.base.AppConfig;
import jp.co.css.base.TFrame;
import jp.co.css.webpos.common.gui.EditTableModel;
import jp.co.css.webpos.common.gui.ListModel;
import jp.co.css.webpos.common.gui.RowHeader;
import jp.co.css.webpos.common.gui.TEditTable;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.gui.TLabel2;
import jp.co.css.webpos.common.gui.TLabel6;
import jp.co.css.webpos.common.gui.TScrollPane;
/**---------------------------------------------
 * 処理名称	： 注文取込画面
 * 作成日		：　2010/04/13
 * 作成者		：　kaku
---------------------------------------------**/
public class Ttsa0300 extends TFrame {

	private static final long serialVersionUID = 1L;

	//ﾃｰﾌﾞﾙのｽｸﾛｰﾙﾍﾟｲﾝ
	public TScrollPane jScrollPane = null;

	/**
	 * teMain:メインテーブル
	 */
	public TEditTable teMain = null;
	private Object objectMain[][] = new Object[][] {
	{ null, null, null, null, null }, { null, null, null, null, null } };

	private int colCount = 0;

	public final int colChumonSite		= colCount++;	//注文サイト
	public final int colChumonId		= colCount++;	//注文ID
	public final int colKakuteiDate		= colCount++;	//注文確定日
	public final int colTitle			= colCount++;	//商品ﾀｲﾄﾙ
	public final int colRankNm	 	 	= colCount++;	//価格管理ﾗﾝｸ名称
	public final int colJanCd 			= colCount++;	//JANｺｰﾄﾞ
	public final int colSku				= colCount++;	//SKU
	public final int colSu				= colCount++;	//数量
	public final int colHanbaiTanka		= colCount++;	//価格
	public final int colHanbaiKin		= colCount++;	//販売金額
	public final int colSoryo			= colCount++;	//配送料
	public final int colTesuryo			= colCount++;	//手数料
	public final int colNebiki			= colCount++;	//値引き
	public final int colUsePoint		= colCount++;	//ポイント利用
	public final int colGokeiKin		= colCount++;	//合計金額

	public final int colShiharaiHoho	= colCount++;	//決済方法
	public final int colHaisohouhou		= colCount++;	//配送方法
	public final int colChumonShohinId	= colCount++;	//注文商品ID

	public final int colBuyerNm			= colCount++;	//購入者氏名
	public final int colBuyerKana		= colCount++;	//購入者かな
	public final int colBuyerYubinno	= colCount++;	//購入者郵便番号
	public final int colBuyerTodoufuken	= colCount++;	//購入者都道府県
	public final int colBuyerSichoson	= colCount++;	//購入者市区町村
	public final int colBuyerBanchi		= colCount++;	//購入者番地
	public final int colBuyerManshon	= colCount++;	//購入者ビル・マンション名
	public final int colBuyerMailAddress= colCount++;	//購入者のEメール
	public final int colBuyerTel		= colCount++;	//購入者電話番号
	public final int colUketoriJin		= colCount++;	//送付先氏名
	public final int colUketoriJinKana	= colCount++;	//送付先かな
	public final int colTodokesakiYubinno= colCount++;	//送付先郵便番号
	public final int colTodokesakiTodoufuken= colCount++;//送付先都道府県
	public final int colTodokesakiSichoson	= colCount++;//送付先市区町村
	public final int colTodokesaki1		= colCount++;	//送付先番地
	public final int colTodokesaki2		= colCount++;	//送付先ビル・マンション名
	public final int colTodokesakiTel	= colCount++;	//送付先電話番号

	//商品情報
	public final int colTanpinCd		= colCount++;	//単品ｺｰﾄﾞ
	public final int colTeika			= colCount++;	//定価
	public final int colShohinCd	 	= colCount++;	// 商品ｺｰﾄﾞ
	public final int colShohinNm 		= colCount++;	// 商品名称
	public final int colShohinKanaNm 	= colCount++;	// 商品名称カナ
	public final int colShohinNm2 		= colCount++;	// 商品名称2
	public final int colShohinKanaNm2	= colCount++;	// 商品名称2カナ
	public final int colKikakuNo		= colCount++;	// 規格番号
	public final int colMakerCd 		= colCount++;	// ﾒｰｶｰｺｰﾄﾞ
	public final int colMakerNm 		= colCount++;	// ﾒｰｶｰ名称
	public final int colShobunCd 		= colCount++;	// 小分類ｺｰﾄﾞ
	public final int colShobunNm 		= colCount++;	// 小分類名称
	public final int colShobunRyakuNm	= colCount++;	// 小分類略称
	public final int colHatubaiDate 	= colCount++;	// 発売日
	public final int colLabelCd 		= colCount++;	// ﾚｰﾍﾞﾙｺｰﾄﾞ
	public final int colLabelNm 		= colCount++;	// ﾚｰﾍﾞﾙ名称
	public final int colDaibunCd 		= colCount++;	// 大分類ｺｰﾄﾞ
	public final int colDaibunNm 		= colCount++;	// 大分類名称
	public final int colDaibunRyakuNm	= colCount++;	// 大分類略称
	public final int colChubunCd 		= colCount++;	// 中分類ｺｰﾄﾞ
	public final int colChubunNm 		= colCount++;	// 中分類名称
	public final int colChubunRyakuNm	= colCount++;	// 中分類略称
	public final int colRankCd	 	 	= colCount++;	// 価格管理ﾗﾝｸ
	public final int colSinchuKb		= colCount++;	// 新中区分

	public final int intMaxCol 			= colCount++;	//列を追加したらここも更新して（最終列＋1）

	//カラム幅
	public final int cellLen[] = new int[intMaxCol];
	//ﾍｯﾀﾞｰ名　	※表示しないので空の配列でOK
	private String headerMain[] = new String[intMaxCol];
	//ｶﾗﾑﾌｫｰﾏｯﾄ　	※表示のみなので空の配列でOK
	private String cellFormatMain[] = new String[intMaxCol];
	//ｶﾗﾑﾃﾞｰﾀﾀｲﾌﾟ　※表示のみなので空の配列でOK
	private int cellDataTypeMain[] = new int[intMaxCol];
	//ｶﾗﾑ表示調整　※表示列のみ後で設定設定
	private int cellAlignmentMain[] = new int[intMaxCol];
	//ﾃｰﾌﾞﾙﾓﾃﾞﾙ
	public EditTableModel tblModel = null;

	private JPanel pnlFooter = null;
	//下方ﾗﾍﾞﾙ(商品詳細)

	//件数
	public JPanel pnlCount = null;
	public JLabel lblNormalCount = null;
	public JLabel lblErrorCount = null;

	public TLabel6 lblTeika = null;
	public TLabel6 lblZeiKb = null;
	public TLabel6 lblDaibunrui = null;
	public TLabel6 lblChubunrui = null;
	public TLabel6 lblShobunrui = null;
	public TLabel6 lblHatubai = null;
	public TLabel6 lblMaker = null;
	public TLabel6 lblLabel = null;
	public TLabel6 lblShohinNmKana = null;
	public TLabel6 lblShohinNm2 = null;
	public TLabel6 lblSoryo = null;
	public TLabel6 lblTesuRyo = null;

	public TLabel6 lblShohinNm = null;

	public TLabel6 lblTodokeSaki1 = null;
	public TLabel6 lblTodokeSaki2 = null;
	public TLabel6 lblTodokeSaki3 = null;
	public TLabel6 lblSichoson = null;
	public TLabel6 lblTodoufuken = null;
	public TLabel6 lblMailAddress = null;
	public TLabel6 lblYubinNo = null;

	/**
	 * This is the default constructor
	 */
	public Ttsa0300(AppConfig appConfig) {
		super(appConfig);

		jMainPanel.add(getJScrollPane(), null);
		jMainPanel.add(getPnlCount(), null);
		jMainPanel.add(getPnlFooter(),null);
		this.setTitle("注文データ取込画面　TTSA0300");
		lblTitle.setText("注　文　デ　ー　タ　取　込　画　面");

		init();
	}

	/**
	 * This method initializes this
	 * @return void
	 */
	public void init() {
		int intWidth[] = new int[]{
				105,	//注文サイト
				185,	//注文ID
				130,	//注文日
				280,	//商品名称
				57,		//ランク
				95,		//JAN
				90,		//SKU
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
			teMain = new TEditTable(objectMain, headerMain, cellAlignmentMain,
					cellDataTypeMain, cellFormatMain);
			teMain.setHeaderHeight(30);
			teMain.setBackground(new Color(240, 250, 250));
			teMain.setShowHorizontalLines(false);
			teMain.setShowVerticalLines(false);
			teMain.setGridColor(new Color(238, 238, 238));
			teMain.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 13));
			teMain.headers.setBackground(new Color(220, 220, 220));
			teMain.headers.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			tblModel = new EditTableModel(teMain);
			teMain.setModel(tblModel, true);
			teMain.setCellHeight(25);
			teMain.setRowHeight(45);
		}
		return teMain;
	}

	/**
	 * This method initializes jScrollPane1
	 *
	 * @return javax.swing.TScrollPane
	 */
	private TScrollPane getJScrollPane() {
		if (jScrollPane == null) {
	        jScrollPane = new TScrollPane(getTeMain());
	        jScrollPane.setBounds(new Rectangle(15, 60, 990, 410));
	        jScrollPane.setViewportView(getTeMain());
			jScrollPane.setRowHeaderView(new RowHeader(teMain,new ListModel(teMain)));
			//垂直のｽｸﾛｰﾙﾊﾞｰを表示にする
			jScrollPane.setVerticalScrollBarPolicy(TScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		}
		return jScrollPane;
	}


	private void SetTableAlignment(){
		//列の表示方法設定

		cellAlignmentMain[colChumonSite]= TEditTable.LEFT;
		cellAlignmentMain[colChumonId]		= TEditTable.LEFT;
		cellAlignmentMain[colKakuteiDate]	= TEditTable.CENTER;
		cellAlignmentMain[colTitle ]		= TEditTable.LEFT;
		cellAlignmentMain[colRankNm]		= TEditTable.CENTER;
		cellAlignmentMain[colJanCd ]		= TEditTable.RIGHT;
		cellAlignmentMain[colSku]			= TEditTable.RIGHT;

		headerMain[colChumonSite]			= "注文サイト";
		headerMain[colChumonId]				= "注文ID";
		headerMain[colKakuteiDate]			= "注文日";
		headerMain[colTitle ]				= "商品タイトル";
		headerMain[colRankNm]				= "ランク";
		headerMain[colJanCd ]				= "JANコード";
		headerMain[colSku]					= "SKU";

	}

	/**
	 * This method initializes pnlFooter
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPnlFooter() {
		if (pnlFooter == null) {

			pnlFooter = new JPanel();
			pnlFooter.setLayout(null);
			pnlFooter.setBounds(new Rectangle(5, 518, 1020, 156));

			TLabel2 lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(15, 0, 101, 26));
			lblTitle.setText("商品名称");
			lblShohinNm = new TLabel6();
			lblShohinNm.setBounds(new Rectangle(115, 0, 400, 26));
			lblShohinNm.setText("1234567890123");
			lblShohinNm.setHorizontalAlignment(SwingConstants.LEFT);

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblShohinNm,null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(520, 0, 111, 26));
			lblTitle.setText("商品名称２");
			lblShohinNm2 = new TLabel6();
			lblShohinNm2.setBounds(new Rectangle(630, 0, 370, 26));
			lblShohinNm2.setText("あいうえおかきくけこさしすせそ");
			lblShohinNm2.setHorizontalAlignment(SwingConstants.LEFT);

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblShohinNm2,null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(15, 30, 101, 26));
			lblTitle.setText("商品名カナ");
			lblShohinNmKana = new TLabel6();
			lblShohinNmKana.setBounds(new Rectangle(115, 30, 106, 26));
			lblShohinNmKana.setText("あいうえおかきくけこさしすせそ");
			lblShohinNmKana.setHorizontalAlignment(SwingConstants.LEFT);

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblShohinNmKana,null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(520, 60, 111, 26));
			lblTitle.setText("お届け先１");
			lblTodokeSaki1 = new TLabel6();
			lblTodokeSaki1.setBounds(new Rectangle(630, 60, 370, 26));
			lblTodokeSaki1.setText("1234567890123");
			lblTodokeSaki1.setHorizontalAlignment(SwingConstants.LEFT);

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblTodokeSaki1,null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(15, 120, 101, 26));
			lblTitle.setText("大分類");
			lblDaibunrui = new TLabel6();
			lblDaibunrui.setBounds(new Rectangle(115, 120, 106, 26));
			lblDaibunrui.setHorizontalAlignment(SwingConstants.LEFT);
			lblDaibunrui.setText("12：あいう");

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblDaibunrui,null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(520, 90, 111, 26));
			lblTitle.setText("お届け先２");
			lblTodokeSaki2 = new TLabel6();
			lblTodokeSaki2.setBounds(new Rectangle(630, 90, 370, 26));
			lblTodokeSaki2.setHorizontalAlignment(SwingConstants.LEFT);
			lblTodokeSaki2.setText("1234567890123");

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblTodokeSaki2,null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(225, 30, 101, 26));
			lblTitle.setText("中分類");
			lblChubunrui = new TLabel6();
			lblChubunrui.setBounds(new Rectangle(325, 30, 190, 26));
			lblChubunrui.setHorizontalAlignment(SwingConstants.LEFT);
			lblChubunrui.setText("1234：あいう");

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblChubunrui,null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(15, 60, 101, 26));
			lblTitle.setText("発売日");
			lblHatubai = new TLabel6();
			lblHatubai.setBounds(new Rectangle(115, 60, 106, 26));
			lblHatubai.setText("0000/00/00");
			lblHatubai.setHorizontalAlignment(SwingConstants.LEFT);

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblHatubai,null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(520, 30, 111, 26));
			lblTitle.setText("都道府県");
			lblTodoufuken = new TLabel6();
			lblTodoufuken.setBounds(new Rectangle(630, 30, 106, 26));
			lblTodoufuken.setText("あいうえおかきくけこさしすせそ");
			lblTodoufuken.setHorizontalAlignment(SwingConstants.LEFT);

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblTodoufuken,null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(740, 30, 111, 26));
			lblTitle.setText("市町村");
			lblSichoson = new TLabel6();
			lblSichoson.setBounds(new Rectangle(850, 30, 150, 26));
			lblSichoson.setText("あいうえおかきくけこさしすせそ");
			lblSichoson.setHorizontalAlignment(SwingConstants.LEFT);

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblSichoson,null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(520, 120, 111, 26));
			lblTitle.setText("お届け先３");
			lblTodokeSaki3 = new TLabel6();
			lblTodokeSaki3.setBounds(new Rectangle(630, 120, 370, 26));
			lblTodokeSaki3.setHorizontalAlignment(SwingConstants.LEFT);
			lblTodokeSaki3.setText("あいうえお");

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblTodokeSaki3,null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(225, 60, 101, 26));
			lblTitle.setText("小分類");
			lblShobunrui = new TLabel6();
			lblShobunrui.setBounds(new Rectangle(325, 60, 190, 26));
			lblShobunrui.setHorizontalAlignment(SwingConstants.LEFT);
			lblShobunrui.setText("123456：あいう");

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblShobunrui,null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(15, 90, 101, 26));
			lblTitle.setText("定価");
			lblTeika = new TLabel6();
			lblTeika.setBounds(new Rectangle(115, 90, 106, 26));
			lblTeika.setText("12,345,678");
			lblTeika.setHorizontalAlignment(SwingConstants.RIGHT);

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblTeika,null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(225, 90, 101, 26));
			lblTitle.setText("メーカー");
			lblMaker = new TLabel6();
			lblMaker.setBounds(new Rectangle(325, 90, 190, 26));
			lblMaker.setText("123456789：あいう");
			lblMaker.setHorizontalAlignment(SwingConstants.LEFT);

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblMaker,null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(225, 120, 101, 26));
			lblTitle.setText("レーベル");
			lblLabel = new TLabel6();
			lblLabel.setBounds(new Rectangle(325, 120, 190, 26));
			lblLabel.setText("");
			lblLabel.setHorizontalAlignment(SwingConstants.LEFT);

			pnlFooter.add(lblTitle,null);
			pnlFooter.add(lblLabel,null);

		}
		return pnlFooter;
	}

	/**
	 * This method initializes pnlCount
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPnlCount() {

		if (pnlCount == null){

			pnlCount = new JPanel();
			pnlCount.setLayout(null);
			pnlCount.setBounds(new Rectangle(18, 480, 990, 35));


			TLabel2 lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(595, 5, 106, 26));
			lblTitle.setText("正常件数");
			lblNormalCount = new JLabel();
			lblNormalCount.setBounds(new Rectangle(700, 5, 86, 26));
			lblNormalCount.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			lblNormalCount.setOpaque(true);
			lblNormalCount.setText("12,345,678");
			lblNormalCount.setFont(new Font("Dialog", Font.PLAIN, 16));
			lblNormalCount.setHorizontalAlignment(SwingConstants.RIGHT);
			lblNormalCount.setBackground(Color.white);

			pnlCount.add(lblTitle, null);
			pnlCount.add(lblNormalCount, null);

			lblTitle = new TLabel2();
			lblTitle.setBounds(new Rectangle(795, 5, 106, 26));
			lblTitle.setText("エラー件数");
			lblErrorCount = new JLabel();
			lblErrorCount.setBounds(new Rectangle(900, 5, 86, 26));
			lblErrorCount.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			lblErrorCount.setOpaque(true);
			lblErrorCount.setText("12,345,678");
			lblErrorCount.setFont(new Font("Dialog", Font.PLAIN, 16));
			lblErrorCount.setHorizontalAlignment(SwingConstants.RIGHT);
			lblErrorCount.setBackground(Color.white);

			pnlCount.add(lblTitle, null);
			pnlCount.add(lblErrorCount, null);

		}
		return pnlCount;
	}

}
