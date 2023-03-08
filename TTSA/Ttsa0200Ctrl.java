package jp.co.css.TTSA;

import java.awt.Component;
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
import jp.co.css.dao.TalosTrn;
import jp.co.css.dao.TenpoKihonMstDAO;
import jp.co.css.dao.TimeShareSetDAO;
import jp.co.css.system.MainMenu;
import jp.co.css.webpos.common.db.H2;
import jp.co.css.webpos.common.except.TException;
import jp.co.css.webpos.common.gui.FKeyAdapter;
import jp.co.css.webpos.common.message.MessageBoxValue;
import jp.co.css.webpos.common.message.MessageDispNew;
import jp.co.css.webpos.common.util.SendTabKeys;
import jp.co.css.webpos.common.util.Util;

public class Ttsa0200Ctrl extends BaseCtrl implements MessageBoxValue {

	final String msgTitle = "通販設定"; // ﾒｯｾｰｼﾞ用ﾀｲﾄﾙ
	private String strErrTitle = "";					// エラータイトル
	public Ttsa0200 frm = null;						// 画面
	private String activeId = "Ttsa0200Ctrl";

	//ﾌｫｰｶｽの移動
	List<Component> compList;						// ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを集めたﾃｰﾌﾞﾙ
	SendTabKeys sendTab = new SendTabKeys(); 	 	// ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを取得するｸﾗｽ
	
	DbInfo timeInfo;
	
	private String alltenpolist = "";
	
	public H2 comH2=null;	//共通

	public Ttsa0200Ctrl(AppConfig ap, String functionId, Object obj) {
		// 初期設定
		super(ap, functionId, obj);
		//起動済みの場合、起動済み画面を呼び出す。
		if (Util.ActiveDisp(appConfig.getDispObjects(), activeId)) {
			return;
		}
		if (frm == null) {
			frm = new Ttsa0200(ap);
		}
		frm.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// ログ処理
				logOut.info("画面【" + frm.getTitle() + "】を閉じました。");
				frm.dispose();
			}
		});

		if(comH2==null){
			comH2=new H2();
			try{
				comH2.h2ClientStart(appConfig.getDatabaseXmlBean());
			}catch(TException e){
				messageBox.disp(e, MB_CRITICAL, "H2 ｴﾗｰ"
						+ e.getMessage() + "\n", msgTitle);
				// ログ処理
				logOut.info("画面【" + frm.getTitle() + "】を閉じました。");
				if(comH2!=null){
					try{
						comH2.h2ClientStop();
					}catch(TException e1){}
				}
				frm.dispose();
				return;

			}
		}
		messageBox = new MessageDispNew(frm, appConfig.getLogOut());
		init();
		// ログ処理
		logOut.info("画面【" + frm.getTitle() + "】を開きました。");
		frm.setVisible(true);
		appConfig.addDisp(activeId, frm);
	}

	/** 初期設定 */
	public void init() {

		// ﾍｯﾀﾞｰ部ﾘｽﾅ追加
		frm.scodeTenpo.addKeyListener(new TKeyAdapter());
		frm.txtFtpPortNo.addKeyListener(new TKeyAdapter());
		frm.txtFtpHostAddress.addKeyListener(new TKeyAdapter());
		frm.txtFtpUserName.addKeyListener(new TKeyAdapter());
		frm.txtFtpPassword.addKeyListener(new TKeyAdapter());
		frm.txtFtpFolder.addKeyListener(new TKeyAdapter());

		// コントロールボタン初期化
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
		frm.fButton.setF9Enabled(true);
		frm.fButton.setF10Text("");
		frm.fButton.setF11Text("");
		frm.fButton.setF12Text("F12 確定");
		frm.fButton.setF12Enabled(true);
		frm.fButton.addFButtonListener(new ButtonListener());
		frm.fButton.setLogOut(logOut);
		
		setTenpoInfo();	
		iniText();
		getDataInfo();
		frm.scodeTenpo.requestFocus();
		compList = sendTab.setCompList(frm.jMainPanel.getComponents());
		frm.setFocusTraversalPolicy(sendTab.setCustomFocus());
	}

	/**
	 * This method iniText() ﾃｷｽﾄﾎﾞｯｸｽの初期化 intFlg = 0 すべて初期化
	 *
	 * @return void
	 */
	private void iniText() {
		frm.txtFtpHostAddress.setText("");			// FTPホストアドレス
		frm.txtFtpPortNo.setText("");				// FTPポート番号
		frm.txtFtpUserName.setText("");			// FTPユーザ名
		frm.txtFtpPassword.setText("");			// FTPパスワード
		frm.txtFtpFolder.setText("");				// FTPフォルダ
		frm.scodeTenpo.clearValues();
	}

	/** コントロールボタン処理 */
	class ButtonListener extends FKeyAdapter {

		// F9 本画面を終了して、メニュー画面を戻ります。
		public void f9Click(ActionEvent e) {
			// ログ処理
			logOut.info("画面【" + frm.getTitle() + "】を閉じました。");
			if(comH2!=null){
				try{
					comH2.h2ClientStop();
				}catch(TException e1){}
			}
			appConfig.removeDisp(activeId);
			frm.dispose();
			MainMenu mainMenu = (MainMenu) obj;
			mainMenu.setVisible(true);
		}

		// F12 実行処理
		public void f12Click(ActionEvent e) {
			try {
				// 入力ﾃﾞｰﾀのﾁｪｯｸ
				if (chkInputData() == true) {
					// エラーがあれば、戻る。
					return;
				}
				if (messageBox.disp(MB_QUESTION, MB_YESNO, "変更します。よろしいですか？", msgTitle) == MB_YES) {
				} else {
					return;
				}

				// データの更新処理
				if (updateMst() == true) {
					// エラーがあれば、戻る。
					return;
				}

				// ログ処理
				logOut.info("画面【" + frm.getTitle() + "】を閉じました。");
				if(comH2!=null){
					try{
						comH2.h2ClientStop();
					}catch(TException e1){}
				}
				appConfig.removeDisp(activeId);
				frm.dispose();
				MainMenu mainMenu = (MainMenu) obj;
				mainMenu.setVisible(true);
			} catch (Exception te) {
				messageBox.disp(te, MB_CRITICAL, "処理でエラーが発生しました。(Exception)" + "\n"
						+ te.toString() + te.getMessage(), msgTitle);
			}
		}
	}

	/** 入力チェック */
	private boolean chkInputData() {
		if (frm.scodeTenpo.getSelectCodes().equals("") && alltenpolist.equals("")) {
			messageBox.disp(MB_INFORMATION, "店舗設定を行って下さい。" , msgTitle);
			frm.scodeTenpo.requestFocus();
			return true;
		}

		// FTPホストアドレス
		if (Util.isNullOrEmpty(frm.txtFtpHostAddress.getText())) {
			messageBox.disp(MB_INFORMATION, "FTPホストアドレスを入力してください。" , msgTitle);
			frm.txtFtpHostAddress.requestFocus();
			return true;
		}
		// FTPポート番号
		if (Util.isNullOrEmpty(frm.txtFtpPortNo.getText())) {
			messageBox.disp(MB_INFORMATION, "FTPポート番号を入力してください。" , msgTitle);
			frm.txtFtpPortNo.requestFocus();
			return true;
		}
		// FTPユーザ名
		if (Util.isNullOrEmpty(frm.txtFtpUserName.getText())) {
			messageBox.disp(MB_INFORMATION, "FTPユーザ名を入力してください。" , msgTitle);
			frm.txtFtpUserName.requestFocus();
			return true;
		}
		// FTPパスワード
		if (Util.isNullOrEmpty(frm.txtFtpPassword.getText())) {
			messageBox.disp(MB_INFORMATION, "FTPパスワードを入力してください。" , msgTitle);
			frm.txtFtpPassword.requestFocus();
			return true;
		}
		// FTPフォルダ
		if (Util.isNullOrEmpty(frm.txtFtpFolder.getText())) {
			messageBox.disp(MB_INFORMATION, "FTPフォルダを入力してください。" , msgTitle);
			frm.txtFtpFolder.requestFocus();
			return true;
		}
		return false;
	}

	/** エンターキーを押している時にカーソルします。 */
	class TKeyAdapter extends KeyAdapter {

		public void keyPressed(java.awt.event.KeyEvent e) {

			switch (e.getKeyCode()) {
			case KeyEvent.VK_ENTER:
				// ﾌｫｰｶｽ次へ
				sendTab.SendTabKeys(e);
				break;
			}
		}
	}

	private boolean getDataInfo() {
		try {
			// 初期化
			DbInfo info = null;
			// TIMESHARE設定マスタ情報の取得
			strErrTitle = "TIMESHARE設定マスタ取得";
			TimeShareSetDAO timeShareSetDAO = new TimeShareSetDAO(appConfig);
			info = timeShareSetDAO.select(9999);
			if (info != null && info.getMaxRowCount() > 0) {
				if (!info.getStringItem("CHUMOTORIKOMITENPO").equals("")) {
					frm.scodeTenpo.setSelectCodes(info.getStringItem("CHUMOTORIKOMITENPO"));
				}
				frm.txtFtpHostAddress.setText(info.getStringItem("FTPTIP"));			// FTPホストアドレス
				frm.txtFtpPortNo.setText(info.getStringItem("FTPPORT"));			// FTPポート番号
				frm.txtFtpUserName.setText(info.getStringItem("FTPUSER"));			// FTPユーザ名
				frm.txtFtpPassword.setText(info.getStringItem("FTPPASSWORD"));		// FTPパスワード
				frm.txtFtpFolder.setText(info.getStringItem("FTPFOLDERPATH"));		// FTPフォルダ
			}
			return false;
		} catch (TException te) {
			messageBox.disp(te, MB_CRITICAL, "TIMESHARE設定マスタの読み込みでエラーが発生しました。"
					+ te.toString() + "\n" + te.getMessage(), strErrTitle);
			return true;
		}
	}

	/** 更新処理 */
	private boolean updateMst() {

		BaseDAO basedao = new BaseDAO(appConfig);
		H2 h2 = new H2();

		try {
			
			// 各ﾏｽﾀの更新*********************************************
			strErrTitle = "[TIMESHARE設定マスタINFO作成]";
			TimeShareSetDAO timeShareSetDAO = new TimeShareSetDAO(appConfig);
			DbInfo timeInfo = timeShareSetDAO.get();
			timeInfo.setRow(new DbInfoValue(timeInfo.getFieldCount()));
			timeInfo.setCurRow(0);
			timeInfo.setCurItem("TENPOCD", 9999);										// 店舗コード
			timeInfo.setCurItem("FTPTIP", frm.txtFtpHostAddress.getText());				// FTPホストアドレス
			timeInfo.setCurItem("FTPPORT", frm.txtFtpPortNo.getText());					// FTPポート番号
			timeInfo.setCurItem("FTPUSER", frm.txtFtpUserName.getText());				// FTPユーザ名
			timeInfo.setCurItem("FTPPASSWORD", frm.txtFtpPassword.getText());				// FTPパスワード
			timeInfo.setCurItem("FTPFOLDERPATH", frm.txtFtpFolder.getText());			// FTPフォルダ
			if (!frm.scodeTenpo.getSelectCodes().equals("")) {
				timeInfo.setCurItem("CHUMOTORIKOMITENPO", frm.scodeTenpo.getSelectCodes());
			} else {
				timeInfo.setCurItem("CHUMOTORIKOMITENPO", alltenpolist);
			}
			timeInfo.setCurItem("KOSINFLG", 1); 										// 更新フラグ
			timeInfo.setCurItem("SAISHUKOSINTANTOCD", appConfig.getTantoushaCd());		// 最終更新担当者コード
			timeInfo.setCurItem("SAISHUKOSINDATETIME", Util.getCurrentDateSql());		// 最終更新日付
			timeInfo.setCurItem("HOSTSOSINFLG", 1);		// ホスト送信フラグ
			// 更新処理
			h2.h2ClientStart(appConfig.getDatabaseXmlBean());
			h2.setAutoCommit(false);
			h2.startTran(); // start transaction

			//トランザクション開始
			int tranno = 0;
			int renban = 0;
			if (appConfig.getRentalServerFlag() == 1 || appConfig.getHostFlag() == 0) {
				tranno = TalosTrn.getTrnNo(appConfig);
			}

			// ﾏｽﾀの更新*********************************************
			strErrTitle = "[TIMESHARE設定マスタの更新]";
			if (timeInfo != null && timeInfo.getMaxRowCount() > 0) {
				//renban = basedao.insertDbInfo(timeInfo, h2, 1, tranno, renban);
				renban = basedao.insertDbInfo(timeInfo, h2, 1, tranno, renban, 1, 1);
			}
			if (appConfig.getRentalServerFlag() == 1|| appConfig.getHostFlag() == 0) {
				renban = basedao.insertTrnEnd(h2, tranno, renban);
			}
			
			strErrTitle = "[リアルタイム更新処理]";
			basedao.execRealTimeSql();

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
	
	/**
	 * 店舗ﾃﾞｰﾀを取得する
	 * @return
	 */
	private void setTenpoInfo(){
		try {
			TenpoKihonMstDAO dao = new TenpoKihonMstDAO(appConfig);
			DbInfo info = null;
//				if (appConfig.getIhoujinkanrenkeiFlg() == 1) {
//					info =  dao.selectSearchCode(comH2);
//					if(info.getMaxRowCount()==0){
//						messageBox.disp(MB_CRITICAL, "対象の店舗がありません。", msgTitle);
//						return;
//					}
//				} else if(appConfig.getTantoOwnerCd().equals("")) {
//					messageBox.disp(MB_CRITICAL, "オーナーコード、グループIDを設定してください。", msgTitle);
//					return;
//				}else{
//					info =  dao.selectGroupTenpo(appConfig.getTantoOwnerCd(),appConfig.getTantoGroupId());
//					if(info.getMaxRowCount()==0){
//						messageBox.disp(MB_CRITICAL, "対象の店舗がありません。（オーナー・グループコードを確認してください）", msgTitle);
//						return;
//					}
//				}

			info =  dao.selectAllTenpoSearchCode();

			for(int i=0; i<info.getMaxRowCount(); i++){
				info.setCurRow(i);

				if(i==0) {
					alltenpolist+=info.getStringItem("CD");
				}else {
					alltenpolist+="," + info.getStringItem("CD");
				}
			}

			frm.scodeTenpo.setDbInfo(info);

		} catch (TException ex) {
			messageBox.disp(ex, MB_CRITICAL, "店舗基本マスタデータの読み込みでエラーが発生しました。" + ex.toString() + "\n" +
					ex.getMessage(), msgTitle);
		}
	}
}