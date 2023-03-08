package jp.co.css.TTAX;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;

import jp.co.css.base.AppConfig;
import jp.co.css.base.TFrame;
import jp.co.css.webpos.common.gui.EditTableModel;
import jp.co.css.webpos.common.gui.ListModel;
import jp.co.css.webpos.common.gui.RowHeader;
import jp.co.css.webpos.common.gui.TComboBox;
import jp.co.css.webpos.common.gui.TEditTable;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.gui.TLabel;
import jp.co.css.webpos.common.gui.TScrollPane;
import jp.co.css.webpos.common.gui.TTextField;
import jp.co.css.webpos.common.message.MessageBoxValue;
import jp.co.css.webpos.common.util.Util;

/**---------------------------------------------
 * 処理名称		： 国別言語設定画面
 * 作成日		：　2019/01/22
 * 作成者		：　陳小帥
---------------------------------------------**/
public class Ttax0400 extends TFrame implements MessageBoxValue {
	private static final long serialVersionUID = 1L;

	//検索
	public TTextField txtSearch = null;
	//ﾃｰﾌﾞﾙのｽｸﾛｰﾙﾍﾟｲﾝ
	public TScrollPane jScrollPanel = null;
	public TComboBox cboLang = null;

	/**
	 * teMain:メインテーブル
	 */
	public TEditTable teMain = null;
	private Object objectMain[][] = new Object[][] {
	{ null, null, null, null, null }, { null, null, null, null, null } };

	//列名称(表示列)
	private int intMaxCol = 0;
	public final int colKuniNm		= intMaxCol++;	//国名
	public final int colLang 		= intMaxCol++;	//言語
	//列名称(非表示列)
	public final int colKuniCd		= intMaxCol++;	//国コード
	public final int colLangCdNow	= intMaxCol++;	//言語コードNow
	public final int colLangCdOld	= intMaxCol++;	//言語コードOld

	//各列幅
	private int intWidth[] = new int[intMaxCol];
	//ﾍｯﾀﾞｰ名　	※表示しないので空の配列でOK
	private String headerMain[] = new String[intMaxCol];
	//ｶﾗﾑﾌｫｰﾏｯﾄ　	※表示のみなので空の配列でOK
	private String cellFormatMain[] = new String[intMaxCol];
	//ｶﾗﾑﾃﾞｰﾀﾀｲﾌﾟ　※表示のみなので空の配列でOK
	private int cellDataTypeMain[] = new int[intMaxCol];
	//ｶﾗﾑ表示調整　※表示列のみ後で設定設定
	private int cellAlignmentMain[] = new int[intMaxCol];
	//カラム幅
	private int cellLen[] = new int[intMaxCol];
	//カラムIMタイプ
	private int cellIMTypeMain[] = new int[intMaxCol];
	//ﾃｰﾌﾞﾙﾓﾃﾞﾙ
	public EditTableModel tblModel = null;

	private JPanel jPanelHeader = null;
	private JPanel jPanelTable = null;

	/**
	 * This is the default constructor
	 */
	public Ttax0400(AppConfig appConfig) {
		super(appConfig);
		init();
	}

	/**
	 * This method initializes
	 * @return void
	 */
	public void init() {
		this.setTitle("国別言語設定画面　TTAX0400");
		lblTitle.setText("国別言語設定画面");

		jMainPanel.add(getPanelHeader(), null);
		jMainPanel.add(getPanelTable(), null);
	}

	/**
	 * This method initializes jPanelHeader
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelHeader() {
		if (jPanelHeader == null) {
			jPanelHeader = new JPanel();
			jPanelHeader.setLayout(null);
			jPanelHeader.setBounds(new Rectangle(10, 55, 901, 76));
			jPanelHeader.setBorder(BorderFactory.createTitledBorder(null, "KEY項目", TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, new Font("Fialog", Font.BOLD, 14), Color.gray));

			TLabel tlblTitle = new TLabel();
			tlblTitle.setText("検索");
			tlblTitle.setBounds(new Rectangle(15, 25, 96, 31));
			JLabel lblTitleWaku = new JLabel();
			lblTitleWaku.setText("");
			lblTitleWaku.setBounds(new Rectangle(110, 25, 182, 31));
			lblTitleWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtSearch = new TTextField();
			txtSearch.setBounds(new Rectangle(113, 28, 176, 25));
			txtSearch.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtSearch.setText("123456789");
			txtSearch.setHorizontalAlignment(TTextField.LEFT);
			txtSearch.setMaxLength(50);
			txtSearch.setIMType(TTextField.IM_HIRAGANA);
			txtSearch.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 18));

			jPanelHeader.add(tlblTitle, null);
			jPanelHeader.add(lblTitleWaku, null);
			jPanelHeader.add(txtSearch, null);
		}
		return jPanelHeader;
	}

	private JPanel getPanelTable() {
		if(jPanelTable == null) {
			jPanelTable = new JPanel();
			jPanelTable.setLayout(null);
			jPanelTable.setBounds(new Rectangle(10, 136, 1001, 528));
			jPanelTable.setBorder(BorderFactory.createTitledBorder(null, "言語設定",TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION,new Font("Dialog", Font.BOLD, 14), Color.gray));

			jPanelTable.add(getJScrollPanel(), null);
		}
		return jPanelTable;
	}

	/**
	 * This method initializes teMain
	 *
	 * @return jp.co.css.webpos.common.gui.TEditTable
	 */
	private TEditTable getTeMain() {
		if (teMain == null) {
			setTableAlignment();
			//書込み可能な列-入力ﾓｰﾄﾞ
			int[] intEditColScroll 	= new int[]{colLang};

			teMain = new TEditTable(objectMain, headerMain, cellLen, cellAlignmentMain, cellIMTypeMain,
									cellDataTypeMain, cellFormatMain);
			teMain.setEditingColumns(intEditColScroll);
			teMain.setBackground(new Color(240, 250, 250));
			teMain.setShowHorizontalLines(false);
			teMain.setShowVerticalLines(true);
			teMain.setHeaderFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 14));
			teMain.setGridColor(new Color(238, 238, 238));
			teMain.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			teMain.headers.setBackground(new Color(220, 220, 220));
			teMain.headers.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			tblModel = new EditTableModel(teMain);
			teMain.setModel(tblModel, true);
			teMain.setEnabled(true);
			teMain.getTableHeader().setResizingAllowed(true);

			TableColumn column = teMain.getColumnModel().getColumn(colLang);
			cboLang = new TComboBox();
			cboLang.setFont(new Font(Util.getFontName(), Font.PLAIN, 16));
			column.setCellEditor(new CustomerCellEditor(cboLang));

			int[] intWidthMax = new int[intWidth.length];
			int[] intWidthMin = new int[intWidth.length];
			for (int i=0;i<intWidth.length;i++) {
				intWidthMax[i] = 400;
				intWidthMin[i] = 10;
			}

			//ﾒｲﾝﾃｰﾌﾞﾙの列ｻｲｽﾞの設定 ※表示部分のみ
			TGuiUtil.setColumnWidth( teMain, intWidth, intWidthMax, intWidthMin);
		}
		return teMain;
	}

	/**
	 * This method initializes jScrollPane1
	 *
	 * @return javax.swing.TScrollPane
	 */
	private TScrollPane getJScrollPanel() {
		if (jScrollPanel == null) {
			jScrollPanel = new TScrollPane();
			jScrollPanel.setBounds(new Rectangle(10, 25, 508, 481));
			jScrollPanel.setViewportView(getTeMain());
			jScrollPanel.setRowHeaderView(new RowHeader(teMain,new ListModel(teMain)));
//			//横のｽｸﾛｰﾙﾊﾞｰを非表示にする
			jScrollPanel.setVerticalScrollBarPolicy(TScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		}
		return jScrollPanel;
	}

	private void setTableAlignment(){

		//列の表示方法設定
		cellAlignmentMain[colKuniNm] 		= TEditTable.LEFT;
		cellAlignmentMain[colLang] 			= TEditTable.LEFT;

		cellLen[colLang] = 50;

		//ﾍｯﾀﾞｰ名設定
		headerMain[colKuniNm] 	= "国名";
		headerMain[colLang] 	= "言語";

		//列幅設定
		intWidth[colKuniNm]  	= 300;
		intWidth[colLang]	 	= 160;
	}
	/*
	* コンボボックス用のエディター
	*/
	class CustomerCellEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("rawtypes")
		public CustomerCellEditor(final JComboBox comboBox){
			super(comboBox);
		}
		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column) {
			TComboBox comboBox = (TComboBox)this.editorComponent;
			comboBox.setSelectedItemName(String.valueOf(value));
			table.setValueAt(comboBox.getSelectedItemValue(), row, colLangCdNow);
			return comboBox;
		}

	}
}