package jp.co.css.TTAX;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;

import jp.co.css.base.AppConfig;
import jp.co.css.base.BaseCtrl;
import jp.co.css.bean.DbInfo;
import jp.co.css.webpos.common.except.TException;
import jp.co.css.webpos.common.gui.EditTableModel;
import jp.co.css.webpos.common.gui.FKey;
import jp.co.css.webpos.common.gui.FKeyAdapter;
import jp.co.css.webpos.common.gui.ListModel;
import jp.co.css.webpos.common.gui.RowChangedListener;
import jp.co.css.webpos.common.gui.RowHeader;
import jp.co.css.webpos.common.gui.TEditTable;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.gui.TScrollPane;
import jp.co.css.webpos.common.message.MessageBoxValue;

/**
 * 担当者一覧表示ﾎﾟｯﾌﾟｱｯﾌﾟ
 */
public class ShowRetry extends BaseCtrl implements MessageBoxValue  {
	private JDialog dlg = null; // @jve:decl-index=0:visual-constraint="10,10"
	private JPanel jContentPane = null;

	private int ans = 0; // どのﾎﾞﾀﾝが押されたかの戻り値
	String strErrTitle = "送信エラー一覧";
	private FKey fKey = null;
	private JPanel jPanelFkey = null;

	// ﾃｰﾌﾞﾙ設定
	public TEditTable teMain = null;
	private Object objectMain[][] = new Object[][] { { null, null, null, null, null },
			{ null, null, null, null, null } };
	
	// 列名称
	private int colCount = 0;
//	public final int colChk = colCount++;
	public final int colDenpyoNo = colCount++; // 伝票番号
	public final int colName = colCount++; // 氏名
	public final int colPassport = colCount++; // 旅券番号
	public final int colTotal = colCount++; // 合計金額
	public final int colNum = colCount++; // 合計数量
	
//	public final int colDenpyoGyo = colCount++;
	public final int colDenpyoKb = colCount++;
	public final int colDenpyoNen = colCount++;
	public final int colSendNo = colCount++;
	public final int colTenpocd = colCount++;
	
	public final int intMaxCol = colCount++; // 最大列 + 1 して
	// ﾍｯﾀﾞｰ名
	private String headerMain[] = new String[intMaxCol];
	// ｶﾗﾑﾌｫｰﾏｯﾄ
	private String cellFormatMain[] = new String[intMaxCol];
	// ｶﾗﾑﾃﾞｰﾀﾀｲﾌﾟ
	private int cellDataTypeMain[] = new int[intMaxCol];
	// ｶﾗﾑ表示調整
	private int cellAlignmentMain[] = new int[intMaxCol];
	private TScrollPane jScrollPane = null;

//	private List<SendNoBean> listSendBean = new ArrayList<SendNoBean>();
	private SendNoBean sendBean = null;
	DbInfo dbInfoNoSend = null;
	AppConfig appConfig;

	// ｺﾝｽﾄﾗｸﾀ
	public ShowRetry(AppConfig appConfig) {
		this.appConfig = appConfig;
		getDlg();
	}

	/**
	 * ﾒｲﾝ画面表示
	 */
	public int disp(DbInfo dbInfoNoSend) throws TException {

		this.dbInfoNoSend = dbInfoNoSend;
		// ﾌｧﾝｸｼｮﾝｷｰﾘｽﾅ
		fKey.addFButtonListener(new ButtonListener());

		// ﾃｰﾌﾞﾙｷｰﾘｽﾅ
//		teMain.addKeyListener(new TableKeyListener());
//		teMain.addMouseListener(new TableMouseListener());

		// ﾃｰﾌﾞﾙの初期化
		teMain.setRowCount(0);
		// ﾃｰﾌﾞﾙの表示
		teMain.addRows(dbInfoNoSend.getMaxRowCount());
		for (int i = 0; i < dbInfoNoSend.getMaxRowCount(); i++) {
			dbInfoNoSend.setCurRow(i);
//			teMain.setValueAt("true", i, colChk);
			teMain.setValueAt(dbInfoNoSend.getStringItem("DENPYONO"), i, colDenpyoNo);
			teMain.setValueAt(dbInfoNoSend.getStringItem("NAME"), i, colName);
			teMain.setValueAt(dbInfoNoSend.getStringItem("PASSPORTNO"), i, colPassport);
			teMain.setValueAt(dbInfoNoSend.getLongItem("TOTAL"), i, colTotal);
			teMain.setValueAt(dbInfoNoSend.getLongItem("NUMBER"), i, colNum);
			
//			teMain.setValueAt(dbInfoNoSend.getStringItem("DENPYOGYO"), i, colDenpyoGyo);
			teMain.setValueAt(dbInfoNoSend.getStringItem("DENPYOKB"), i, colDenpyoKb);
			teMain.setValueAt(dbInfoNoSend.getStringItem("DENPYONEN"), i, colDenpyoNen);
			teMain.setValueAt(dbInfoNoSend.getStringItem("SENDNO"), i, colSendNo);
			teMain.setValueAt(dbInfoNoSend.getStringItem("TENPOCD"), i, colTenpocd);
		}

		// ﾃｰﾌﾞﾙの設定
		RowChangedListener rowChangedListener = new rowChangedListener();
		teMain.setRowChangedListener(rowChangedListener);
		teMain.setRowSelectionAllowed(true);
		teMain.setColumnSelectionAllowed(false);
		teMain.addRowSelectionInterval(0, 0);
//		teMain.addMouseListener(new ChkMouseListener(teMain));

		// ﾀﾞｲｱﾛｸﾞの設定
		dlg.addWindowListener(new WindowAdapter() {
			// 閉じるボタンが押された時の処理
			public void windowClosing(WindowEvent e) {
				dlg.dispose();
			}

			// ｳｨﾝﾄﾞｳが開かれたらﾃｰﾌﾞﾙにﾌｫｰｶｽを
			public void windowActivated(WindowEvent e) {
				if (teMain != null)
					teMain.requestFocus();
			}
		});
		dlg.setModal(true); // ﾓｰﾀﾞﾙに
		dlg.setResizable(false); // ｻｲｽﾞ変更不可
		dlg.setVisible(true); // 画面表示

		return ans;
	}

	// ﾃｰﾌﾞﾙ/行変更ﾘｽﾅ
	class rowChangedListener implements RowChangedListener {
		public boolean RowChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting() == false) {
			}
			return true;
		}
	}

	class ButtonListener extends FKeyAdapter {
		// ｷｬﾝｾﾙ
		public void f9Click(ActionEvent e) {
			sendBean = null;
			ans = MB_CANCEL;
			dlg.dispose();
		}

		// OK
		public void f12Click(ActionEvent e) {
			int seletedRow = teMain.getSelectedRow();
			if(seletedRow < 0) {
				messageBox.disp(MB_INFORMATION, "再送信伝票を選択してください。", strErrTitle);
			}
			sendBean = new SendNoBean();
//			sendBean.setDenpyoGyo(teMain.getValueInt(seletedRow, colDenpyoGyo));
			sendBean.setDenpyoKb(teMain.getValueInt(seletedRow, colDenpyoKb));
			sendBean.setDenpyoNen(teMain.getValueInt(seletedRow, colDenpyoNen));
			sendBean.setDenpyoNo(teMain.getValueInt(seletedRow, colDenpyoNo));
			sendBean.setSendNo(teMain.getValueString(seletedRow, colSendNo));
			sendBean.setTenpocd(teMain.getValueInt(seletedRow, colTenpocd));

			ans = MB_YES;
			dlg.dispose();
		}
	}

	/**
	 * ﾃｰﾌﾞﾙで選択された販売先ｺｰﾄﾞを返す
	 * 
	 * @return
	 */
	public SendNoBean getSendNoBean() {
		return sendBean;
	}

	/**
	 * This method initializes dlg
	 *
	 * @return javax.swing.JDialog
	 */
	private JDialog getDlg() {
		if (dlg == null) {
			dlg = new JDialog();
			dlg.setContentPane(getJContentPane());
			dlg.setSize(new Dimension(728, 603));
			dlg.setTitle("送信エラー一覧");

			// 画面サイズ調整
			TGuiUtil.resizeWindow(dlg);

			// 画面の中央に表示
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
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			jContentPane.setFont(new Font("Dialog", Font.PLAIN, 12));
			jContentPane.add(getJPanelFkey(), null);
			jContentPane.add(getFKey(), null);
			jContentPane.add(getJScrollPane(), null);
		}
		return jContentPane;
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
			jPanelFkey.setBounds(new Rectangle(20, 510, 676, 46));
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
		int[] intFormat;
		if (fKey == null) {
			intFormat = new int[] { 9, 12 };
			fKey = new FKey(intFormat);
			fKey.setLayout(null);
			fKey.setBounds(jPanelFkey.getBounds());
			fKey.setF9Text("F9 ｷｬﾝｾﾙ");
			fKey.setF12Text("F12 再送信");
		}
		return fKey;
	}

	/**
	 * This method initializes teMain
	 *
	 * @return jp.co.css.webpos.common.gui.TEditTable
	 */
	private TEditTable getTeMain() {
		if (teMain == null) {

			// 列の表示方法設定
			cellAlignmentMain[colDenpyoNo] = TEditTable.RIGHT;
			cellAlignmentMain[colName] = TEditTable.LEFT;
			cellAlignmentMain[colPassport] = TEditTable.RIGHT;
			cellAlignmentMain[colTotal] = TEditTable.RIGHT;
			cellAlignmentMain[colNum] = TEditTable.RIGHT;

			headerMain[colDenpyoNo] = "伝票番号";
			headerMain[colName] = "氏名";
			headerMain[colPassport] = "旅券番号";
			headerMain[colTotal] = "合計金額";
			headerMain[colNum] = "合計数量";

			teMain = new TEditTable(objectMain, headerMain, cellAlignmentMain, cellDataTypeMain, cellFormatMain);

			teMain.setBackground(new Color(240, 250, 250));
			teMain.setShowHorizontalLines(false);
			teMain.setShowVerticalLines(true);
			teMain.setGridColor(new Color(238, 238, 238));
			teMain.setFont(new Font("MS UI Gothic", 0, 18));
			teMain.headers.setBackground(new Color(220, 220, 220));
			teMain.headers.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			teMain.setModel(new EditTableModel(teMain), true);
			teMain.getTableHeader().setResizingAllowed(true);

			// 横ｽｸﾛｰﾙ非表示
			jScrollPane.setHorizontalScrollBarPolicy(TScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jScrollPane.setVerticalScrollBarPolicy(TScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			// 列幅設定 2015-04-13 麻
			// No.21580 2020/06/19 CXS start
			int[] intWidthMax = new int[intMaxCol];
			int[] intWidthMin = new int[intMaxCol];
			for (int i = 0; i < intMaxCol; i++) {
				intWidthMax[i] = 600;
				intWidthMin[i] = 10;
			}
			TGuiUtil.setColumnWidth(teMain, new int[] {100, 160, 138, 140, 90 }, intWidthMax,
					intWidthMin);
		}
		return teMain;
	}

	/**
	 * This method initializes jScrollPane
	 *
	 * @return javax.swing.TScrollPane
	 */
	private TScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new TScrollPane();
			jScrollPane.setBounds(new Rectangle(20, 20, 676, 471));
			jScrollPane.setViewportView(getTeMain());
			jScrollPane.setRowHeaderView(new RowHeader(teMain, new ListModel(teMain)));
			// 横のｽｸﾛｰﾙﾊﾞｰを非表示にする
			jScrollPane.setHorizontalScrollBarPolicy(TScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}
	
//	class ChkMouseListener extends MouseAdapter {
//
//		private TEditTable table;
//
//		public ChkMouseListener(TEditTable table) {
//			this.table = table;
//		}
//
//		public void mousePressed(MouseEvent e) {
//			teMain.stopCellEditing();
//			int intCol = this.table.columnAtPoint(e.getPoint());
//			int intRow = this.table.rowAtPoint(e.getPoint());
//			if(intRow < 0) return ;
//			if (intCol == colChk) {
//				// ﾁｪｯｸﾎﾞｯｸｽにﾁｪｯｸを付けるorはずす
//				if (teMain.getValueString(intRow, colChk).equals("true")) {
//					teMain.setValueAt(false, intRow, colChk);
//				} else {
//					teMain.setValueAt(true, intRow, colChk);
//				}
//			}
//		}
//	}

}