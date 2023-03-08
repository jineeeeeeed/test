package jp.co.css.TTAX;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import jp.co.css.base.AppConfig;
import jp.co.css.base.BaseCtrl;
import jp.co.css.bean.DbInfo;
import jp.co.css.bean.DbInfoValue;
import jp.co.css.dao.BaseDAO;
import jp.co.css.dao.KuniMstDAO;
import jp.co.css.system.MainMenu;
import jp.co.css.talos_l.util.Constants;
import jp.co.css.webpos.common.db.H2;
import jp.co.css.webpos.common.except.TException;
import jp.co.css.webpos.common.gui.FKeyAdapter;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.message.MessageBoxValue;
import jp.co.css.webpos.common.message.MessageDispNew;
import jp.co.css.webpos.common.util.SendTabKeys;
import jp.co.css.webpos.common.util.Util;
/**---------------------------------------------
 * 処理名称	：  国別言語設定画面
 * 作成日	：　2019/01/22
 * 作成者	：　陳小帥
---------------------------------------------**/
public class Ttax0400Ctrl extends BaseCtrl implements MessageBoxValue {

	private final String strMsgTitle = "国別言語設定画面";	//ﾒｯｾｰｼﾞ用ﾀｲﾄﾙ
	private String strErrTitle = "";					//ｴﾗｰﾒｯｾｰｼﾞ用処理ﾀｲﾄﾙ
	private Ttax0400 frm = null;						//画面
	private String activeId = "Ttax0400Ctrl";

	//ﾌｫｰｶｽの移動
	List<Component> compList;							//ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを集めたﾃｰﾌﾞﾙ
	SendTabKeys sendTab = new SendTabKeys(); 	 		//ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを取得するｸﾗｽ

	private H2 comH2 = null;	//共通

	public Ttax0400Ctrl(AppConfig ap,String functionId, Object obj){

		//初期設定
		super(ap,functionId,obj);
		//起動済みの場合、起動済み画面を呼び出す。
		if (Util.ActiveDisp(appConfig.getDispObjects(), activeId)) {
			return;
		}
		if (frm == null) {
			frm = new Ttax0400(ap);
		}
		messageBox = new MessageDispNew(frm,appConfig.getLogOut());
		if(comH2==null){
			comH2=new H2();
			try{
				comH2.h2ClientStart(appConfig.getDatabaseXmlBean());
			}catch(TException e){
				messageBox.disp(e, MB_CRITICAL, "H2 ｴﾗｰ" + "\n"+ e.getMessage() + "\n", strMsgTitle);
				this.dispClose();
				return;
			}
		}
		frm.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispClose();
			}
			public void windowOpened(WindowEvent e){
				frm.txtSearch.requestFocus();
			}
		});
		//ログ処理
		logOut.info("画面【" + frm.getTitle() + "】を開きました。");

		init();
		//画面サイズ調整
		TGuiUtil.resizeWindow(frm);
		//画面の表示
		frm.setVisible(true);
		appConfig.addDisp(activeId, frm);
	}

	private void init() {
		//Fkeyの初期化
		frm.fButton.setFAllEnabled(false);
		frm.fButton.setF1Text("");
		frm.fButton.setF2Text("");
		frm.fButton.setF3Text("");
		frm.fButton.setF4Text("");
		frm.fButton.setF5Text("");
		frm.fButton.setF6Text("");
		frm.fButton.setF7Text("");
		frm.fButton.setF8Text("");
		frm.fButton.setF9Text("F9 終了");
		frm.fButton.setF10Text("");
		frm.fButton.setF11Text("");
		frm.fButton.setF12Text("F12 確定");
		frm.fButton.addFButtonListener(new ButtonListener());
		frm.fButton.setLogOut(logOut);

		frm.setResizable(false);
		//ﾃｰﾌﾞﾙ/ﾏｳｽﾘｽﾅ・ｷｰﾘｽﾅ追加
		frm.txtSearch.addKeyListener(new HeaderKeyListener());

		//ｶｰｿﾙの制御　※Enableの制御が終わってから設定して!!
		compList = sendTab.setCompList(frm.getContentPane().getComponents());
		frm.setFocusTraversalPolicy(sendTab.setCustomFocus());
		//ﾃｷｽﾄの初期化
		frm.txtSearch.setText("");
		frm.txtSearch.setEnabled(true);
		setCboLanguage();
		showData();
		setFkeyEnabled();
	}

	//ﾍｯﾀﾞｰ ﾃｷｽﾄ・ｺﾝﾎﾞ/ｷｰﾘｽﾅ
	class HeaderKeyListener extends KeyAdapter{
		public void keyPressed(java.awt.event.KeyEvent e) {
			//ｴﾝﾀｰｷｰ
			if(e.getKeyCode() == KeyEvent.VK_ENTER ){
				// 会員ｺｰﾄﾞが入力されているか
				if (frm.txtSearch.getText().equals("")) {
					frm.teMain.requestFocus();
					frm.teMain.addRowSelectionInterval(0, 0);
				}
				for(int i=0; i<frm.teMain.getRowCount(); i++) {
					if(frm.teMain.getValueString(i, frm.colKuniNm).indexOf(frm.txtSearch.getText()) != -1) {
						frm.teMain.editCellSet(i, frm.colLang);
						frm.teMain.addRowSelectionInterval(i, i);
						frm.teMain.addColumnSelectionInterval(frm.colLang, frm.colLang);
						Rectangle rect = frm.teMain.getCellRect(i, 0, true);
						frm.teMain.scrollRectToVisible(rect);
						break;
					}
				}
			}
		}
	}

	/**
	 * ﾌｧﾝｸｼｮﾝｷｰの制御
	 * @author Administrator
	 */
	class ButtonListener extends FKeyAdapter {
		//F9 終了
		public void f9Click(ActionEvent e) {
			if( messageBox.disp(MB_QUESTION, MB_YESNO, "編集中の情報は失われます。よろしいですか？", strMsgTitle) == MB_NO ){
				return;
			}
			dispClose();
		}

		//F12確定
		public void f12Click(ActionEvent e) {
			//再度確認Msg
			if (messageBox.disp(MB_QUESTION, MB_YESNO, "更新します。よろしいですか？", strMsgTitle) == MB_NO) {
				return ;
			}
			if(updateMst()) {
				return;
			}
			dispClose();
		}
	}

	/**
	 * ﾌｧﾝｸｼｮﾝｷｰの設定
	 */
	private void setFkeyEnabled(){
		frm.fButton.setF9Enabled(true);
		frm.fButton.setF12Enabled(true);		// F12確定
	}

	/**
	 * 画面を閉じる
	 */
	private void dispClose(){
		if(comH2!=null){
			try{
				comH2.h2ClientStop();
			}catch(TException e){}
		}
		//ログ処理
		logOut.info("画面【" + frm.getTitle() + "】を閉じました。");
		frm.dispose();
		appConfig.removeDisp(activeId);
		MainMenu mainMenu = (MainMenu)Ttax0400Ctrl.this.obj;
		mainMenu.setVisible(true);
	}

	private void showData() {
		try {
			strErrTitle = "国マスタの取得";
			KuniMstDAO daoKuniMst = new KuniMstDAO(appConfig);
			DbInfo dbInfoKuni = daoKuniMst.selectKuniNm(comH2);
			frm.teMain.setRowCount(0);
			for(int i=0; i<dbInfoKuni.getMaxRowCount(); i++) {
				dbInfoKuni.setCurRow(i);
				frm.teMain.addRow();
				String strLangKb = Constants.ARRAY_LANGUAGE_KB[dbInfoKuni.getIntItem("LANGKB")][1];
				frm.teMain.setValueAt(dbInfoKuni.getStringItem("KUNINM"), i, frm.colKuniNm);//国名
				frm.teMain.setValueAt(strLangKb, i, frm.colLang);							//言語
				frm.teMain.setValueAt(dbInfoKuni.getStringItem("KUNICD"), i, frm.colKuniCd);//国コード
				frm.teMain.setValueAt(dbInfoKuni.getStringItem("LANGKB"), i, frm.colLangCdOld);//言語コードOld
				frm.teMain.setValueAt(dbInfoKuni.getStringItem("LANGKB"), i, frm.colLangCdNow);//言語コードNow
			}
		} catch (TException e) {
			messageBox.disp(e, MB_CRITICAL, strErrTitle + "でエラーが発生しました。" + "\n" + e.getMessage(),strMsgTitle);
			return;
		}
	}

	/** 更新処理 */
	private boolean updateMst() {
		BaseDAO basedao = new BaseDAO(appConfig);
		H2 h2 = new H2();
		try {
			h2.h2ClientStart(appConfig.getDatabaseXmlBean());
			h2.setAutoCommit(false);
			h2.startTran();

			//トランザクション開始
			int tranno = 0;
			int renban = 0;

			strErrTitle = "[国マスタINFO作成]";
			DbInfo dbInfoKuni = basedao.getDbInfo(h2, "M_KUNI");
			for(int i=0; i<frm.teMain.getRowCount(); i++){
				if(frm.teMain.getValueInt(i, frm.colLangCdNow) == frm.teMain.getValueInt(i, frm.colLangCdOld)) {
					continue;
				}
				DbInfoValue tgv = new DbInfoValue(dbInfoKuni.getFieldCount());
				dbInfoKuni.setRow(tgv);
				dbInfoKuni.setItem(tgv, "KUNICD", frm.teMain.getValueAt(i, frm.colKuniCd));
				dbInfoKuni.setItem(tgv, "LANGKB", frm.teMain.getValueAt(i, frm.colLangCdNow));
				dbInfoKuni.setItem(tgv, "KOSINFLG", 2); // 変更
				dbInfoKuni.setItem(tgv, "SAISHUKOSINTANTOCD", appConfig.getTantoushaCd());
				dbInfoKuni.setItem(tgv, "SAISHUKOSINDATETIME", Util.getCurrentDateSql());
			}
			strErrTitle = "[国マスタの更新]";
			renban = basedao.insertDbInfo(dbInfoKuni, h2, 0, tranno, renban);

			renban = basedao.insertTrnEnd(h2, tranno, renban);

			// ｺﾐｯﾄ処理
			h2.commitTran();

			return false;

		} catch (Exception e) {
			try {
				// ﾛｰﾙﾊﾞｯｸ
				h2.rollBackTran();
			} catch (TException te) {
				messageBox.disp(te, MB_CRITICAL, "コミット処理でエラーが発生しました。" + "\n"
						+ te.toString() + e.getMessage(), strErrTitle);
				return true;
			}
			messageBox.disp(e, MB_CRITICAL, "更新処理でエラーが発生しました。(Exception)" + "\n"
					+ e.toString() + e.getMessage(), strErrTitle);
			return true;
		}finally{
			try{
				h2.h2ClientStop();
			}catch(TException e){}
		}
	}
	//言語コンボボックス設定
	private void setCboLanguage(){
		String strLanguage[][] =Constants.ARRAY_LANGUAGE_KB;
		frm.cboLang.removeAllItems();
		for(int i= 0;i<strLanguage.length;i++) {
			frm.cboLang.addTextValueItem(strLanguage[i][0], strLanguage[i][1]);
		}
	}
}