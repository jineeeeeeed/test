package jp.co.css.TTAX;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import jp.co.css.base.AppConfig;
import jp.co.css.base.BaseCtrl;
import jp.co.css.bean.DbInfo;
import jp.co.css.bean.DbInfoValue;
import jp.co.css.dao.BaseDAO;
import jp.co.css.dao.TalosTrn;
import jp.co.css.dao.TaxSetMstDAO;
import jp.co.css.system.MainMenu;
import jp.co.css.talos_l.util.Constants;
import jp.co.css.talos_l.util.NexUtil;
import jp.co.css.webpos.common.db.H2;
import jp.co.css.webpos.common.except.TException;
import jp.co.css.webpos.common.gui.FKeyAdapter;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.gui.TTextField;
import jp.co.css.webpos.common.message.MessageDispNew;
import jp.co.css.webpos.common.util.SendTabKeys;
import jp.co.css.webpos.common.util.Util;
/*******************************************************************************
 * 処理名称 ：   免税設定画面     <br>
 * 作成日 　　： 	2019/01/23	  <br>
 * 作成者 　　：  	張 佳明		　<br>
 ******************************************************************************/

public class Ttax0200Ctrl extends BaseCtrl{

	final String strMsgTitle = "免税設定画面";  		// ﾒｯｾｰｼﾞ用ﾀｲﾄﾙ

	public Ttax0200 frm = null;					// 画面
	private String activeId = "Ttax0200Ctrl";

	private H2 comH2=null;

	List<Component> compList;					// ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを集めたﾃｰﾌﾞﾙ
	SendTabKeys sendTab = new SendTabKeys(); 	// ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを取得するｸﾗｽ
	CertBean certbean = null;//No.22747 2021/02/09 cf add

	public Ttax0200Ctrl(AppConfig ap,String functionId, Object obj) throws Exception{
		//初期設定
		super(ap,functionId,obj);
		if (Util.ActiveDisp(appConfig.getDispObjects(), activeId)) {
			return;
		}
		if (frm == null) {
			frm = new Ttax0200(ap);
		}
		if(comH2==null){
			comH2= new H2();
			try{
				comH2.h2ClientStart(appConfig.getDatabaseXmlBean());
			}catch(TException e){
				messageBox.disp(e, MB_CRITICAL, "H2 ｴﾗｰ"
						+ e.getMessage() + "\n", strMsgTitle);
				return;
			}
		}
		messageBox = new MessageDispNew(frm, appConfig.getLogOut());
		frm.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispClose();
			}
		});
		init();
		logOut.info("画面【" + frm.getTitle() + "】を開きました。");
		TGuiUtil.resizeWindow(frm);
		frm.setVisible(true);
		appConfig.addDisp(activeId, frm);
	}

	/** 初期設定 */
	public void init() throws Exception{
		frm.txtHanbaishaNm.addKeyListener(new TKeyAdapter());
		frm.txtHanbaishaJyusho.addKeyListener(new TKeyAdapter());
		frm.txtZeimushoNm.addKeyListener(new TKeyAdapter());
		frm.txtTaxableLand.addKeyListener(new TKeyAdapter());
//		frm.txtStatusOfResidence.addKeyListener(new TKeyAdapter());
		frm.cmbStatusOfResidence.addKeyListener(new TKeyAdapter());//No.22747 2020/09/01 wk chg
		frm.txtHanbaiSikibetu.addKeyListener(new TKeyAdapter());//No.22747 2020/08/04 趙 志強 add
		frm.cmbLanguage.addKeyListener(new TKeyAdapter());
		frm.chkAnnotationPrinting.addKeyListener(new TKeyAdapter());
		frm.chkSeiyakushoInjiKb.addKeyListener(new TKeyAdapter());
		frm.chkRecordListInjiKb.addKeyListener(new TKeyAdapter());
		frm.chkPackingListInjiKb.addKeyListener(new TKeyAdapter());
		frm.txtClientCrtPath.addKeyListener(new TKeyAdapter());//No.22747 2021/02/09 cf add
		frm.btnClientCrtPath.addActionListener(new FileChooseActionListener());

		// Fkey初期化
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
		frm.fButton.setF12Text("F12 実行");
		frm.fButton.setF12Enabled(true);
		frm.fButton.addFButtonListener(new ButtonListener());
		//ﾌｧﾝｸｼｮﾝｷｰ/ﾛｸﾞ処理追加
		frm.fButton.setLogOut(logOut);

		// 画面内容初期化
		initText();
		setValue();

		// ｶｰｿﾙの制御　※Enableの制御が終わってから設定して!!
		compList = sendTab.setCompList(frm.jMainPanel.getComponents());
		frm.setFocusTraversalPolicy(sendTab.setCustomFocus());
	}

	private void initText() throws Exception{
		//販売者氏名
		frm.txtHanbaishaNm.setText("");
		//販売場所在地
		frm.txtHanbaishaJyusho.setText("");
		//所轄税務署
		frm.txtZeimushoNm.setText("");
		//納税地
		frm.txtTaxableLand.setText("");
		//在留資格
//		frm.txtStatusOfResidence.setText("");
		setCmbStatusOfResidence();//No.22747 2020/09/01 wk chg
		//記載事項言語
		setCmbLanguage();
		//注釈印字
		frm.chkAnnotationPrinting.setSelected(false);
		//誓約書印刷フラグ
		frm.chkSeiyakushoInjiKb.setSelected(false);
		//記録表印刷フラグ
		frm.chkRecordListInjiKb.setSelected(false);
		//梱包リスト印刷フラグ
		frm.chkPackingListInjiKb.setSelected(false);
		//販売場識別符号//No.22747 2020/08/04 趙 志強 add
		frm.txtHanbaiSikibetu.setText("");
		frm.txtClientCrtPath.setText("");
		frm.lblClientCrt.setBackground(Color.red);
		frm.lblClientCrt.setText("未設定");
		certbean = null;
	}

	private void setValue() throws Exception{
		try {
			// データDAO初期化
			TaxSetMstDAO daoTaxSet = new TaxSetMstDAO(appConfig);
			// データを取得
			DbInfo dbInfoTaxSet = daoTaxSet.select(comH2,appConfig.getTenpoCd());
			if (dbInfoTaxSet.getMaxRowCount() == 0) {
				return;
			}
			//販売者氏名
			frm.txtHanbaishaNm.setText(dbInfoTaxSet.getStringItem("SELLERNAME"));
			//販売場所在地
			frm.txtHanbaishaJyusho.setText(dbInfoTaxSet.getStringItem("SALESLOCATION"));
			//所轄税務署
			frm.txtZeimushoNm.setText(dbInfoTaxSet.getStringItem("TAXOFFICE"));
			//納税地
			frm.txtTaxableLand.setText(dbInfoTaxSet.getStringItem("TAXPAYPLACE"));
			//在留資格
//			frm.txtStatusOfResidence.setText(dbInfoTaxSet.getStringItem("STATUSOFRESIDENCE"));
			frm.cmbStatusOfResidence.setSelectedItemValue(dbInfoTaxSet.getStringItem("STATUSOFRESIDENCE"));//No.22747 2020/09/01 wk chg
			//記載事項言語
			frm.cmbLanguage.setSelectedIndex(dbInfoTaxSet.getIntItem("LANGKB"));
			//注釈印字
			if(dbInfoTaxSet.getIntItem("ANNOTATIONKB") == 1) {
				frm.chkAnnotationPrinting.setSelected(true);
			}
			//誓約書印刷フラグ
			if(dbInfoTaxSet.getIntItem("OATHFLG") == 1) {
				frm.chkSeiyakushoInjiKb.setSelected(true);
			}
			//記録表印刷フラグ
			if(dbInfoTaxSet.getIntItem("RECORDFLG") == 1) {
				frm.chkRecordListInjiKb.setSelected(true);
			}
			//梱包リスト印刷フラグ
			if(dbInfoTaxSet.getIntItem("PACKINGLISTFLG") == 1) {
				frm.chkPackingListInjiKb.setSelected(true);
			}
			//販売場識別符号//No.22747 2020/08/04 趙 志強 add
			frm.txtHanbaiSikibetu.setText(dbInfoTaxSet.getStringItem("HANBAISIKIBETU"));
			if ( !Util.isNullOrEmpty(dbInfoTaxSet.getStringItem("CLIENTCERTIFY")) ){
				frm.lblClientCrt.setBackground(new Color(0, 204, 153));
				frm.lblClientCrt.setText("設定済");
			}else{
				frm.lblClientCrt.setBackground(Color.red);
				frm.lblClientCrt.setText("未設定");
			}
			frm.lblClientCrt.setVisible(true);
			if ( !Util.isNullOrEmpty(dbInfoTaxSet.getStringItem("CLIENTCERTEXPIRYDATE"))){
				frm.txtClientCrtExpiry.setText(dbInfoTaxSet.getStringItem("CLIENTCERTEXPIRYDATE"));
			}

		} catch (TException ex) {
			messageBox.disp(ex, MB_CRITICAL, "免税設定マスタの読み込みでエラーが発生しました。" + ex.toString() + "\n" + ex.getMessage(), strMsgTitle);
			return;
		}
	}

	private void setCmbLanguage() {
		frm.cmbLanguage.removeAllItems();
		String strLanguage[][] =Constants.ARRAY_LANGUAGE_KB;
		for(int i= 0;i<strLanguage.length;i++) {
			frm.cmbLanguage.addTextValueItem(strLanguage[i][0], strLanguage[i][1]);
		}
	}

	//No.22747 2020/09/01 wk add start
	private void setCmbStatusOfResidence() {
		if(frm.cmbStatusOfResidence.getItemCount() > 0) {
			frm.cmbStatusOfResidence.removeAllItems();
		}
		TaxCodeUtil codeUtil = new TaxCodeUtil();
		Map<String, String> codemap = codeUtil.getStatMap();
		for (Map.Entry<String, String> entry : codemap.entrySet()) {
			frm.cmbStatusOfResidence.addTextValueItem(entry.getKey(), entry.getValue());
		}
	}
	//No.22747 2020/09/01 wk add end

	private void dispClose( ){
		logOut.info("画面【" + frm.getTitle() + "】を閉じました。");
		frm.dispose();
		appConfig.removeDisp(activeId);
		MainMenu mainMenu = (MainMenu) obj;
		mainMenu.setVisible(true);
	}

	class ButtonListener extends FKeyAdapter {
		public void f9Click(ActionEvent e) {
			if (messageBox.disp(MB_QUESTION, MB_YESNO,
					"変更中の内容は破棄されます。よろしいですか？", strMsgTitle) == MB_YES) {
				dispClose();
			}
		}

		public void f12Click(ActionEvent e) {
			if(chkInputData() == true) {
				return;
			}
			if (messageBox.disp(MB_QUESTION, MB_YESNO, "更新します。よろしいですか？" , strMsgTitle) == MB_YES) {
				// データの更新処理
				if (updateMst() == true) {
					// エラーがあれば、戻る。
					return;
				}
				dispClose();
			}
		}
	}

	private boolean chkInputData() {
		//販売者氏名
		if (Util.isNullOrEmpty(Util.trim(frm.txtHanbaishaNm.getText()))) {
			messageBox.disp(MB_EXCLAMATION, "販売者氏名が入力されていません。入力してください。", strMsgTitle);
			frm.txtHanbaishaNm.requestFocus();
			return true;
		}
		//No.28084 2022/12/1 wk add start
		else {
			int intDakuten = NexUtil.chkDakuten(frm.txtHanbaishaNm.getText());
			if(intDakuten >= 0) {
				int intSa = intDakuten == 0 ? 0 : 1;
				messageBox.disp(MB_EXCLAMATION, "販売者氏名に使用不可の文字列が含まれています。" + "\n"
						+ "使用不可文字：" + frm.txtHanbaishaNm.getText().substring(intDakuten-intSa, intDakuten+1), strMsgTitle);
				frm.txtHanbaishaNm.requestFocus();
				return true;
			}
		}
		//No.28084 2022/12/1 wk add end
		//販売場所在地
		if (Util.isNullOrEmpty(Util.trim(frm.txtHanbaishaJyusho.getText()))) {
			messageBox.disp(MB_EXCLAMATION, "販売場所在地が入力されていません。入力してください。", strMsgTitle);
			frm.txtHanbaishaJyusho.requestFocus();
			return true;
		}else if(frm.txtHanbaishaJyusho.getText().length() > 70){//No.22747 2020/08/04 趙 志強 add
			messageBox.disp(MB_EXCLAMATION, "販売場所在地は70桁以内で入力してください。", strMsgTitle);
			frm.txtHanbaishaJyusho.requestFocus();
			return true;
		}
		//No.28084 2022/12/1 wk add start
		else {
			int intDakuten = NexUtil.chkDakuten(frm.txtHanbaishaJyusho.getText());
			if(intDakuten >= 0) {
				int intSa = intDakuten == 0 ? 0 : 1;
				messageBox.disp(MB_EXCLAMATION, "販売場所在地に使用不可の文字列が含まれています。" + "\n"
						+ "使用不可文字：" + frm.txtHanbaishaJyusho.getText().substring(intDakuten-intSa, intDakuten+1), strMsgTitle);
				frm.txtHanbaishaJyusho.requestFocus();
				return true;
			}
		}
		//No.28084 2022/12/1 wk add end
		//所轄税務署
		if (Util.isNullOrEmpty(Util.trim(frm.txtZeimushoNm.getText()))) {
			messageBox.disp(MB_EXCLAMATION, "所轄税務署が入力されていません。入力してください。", strMsgTitle);
			frm.txtZeimushoNm.requestFocus();
			return true;
		}
		//納税地
		if (Util.isNullOrEmpty(Util.trim(frm.txtTaxableLand.getText()))) {
			messageBox.disp(MB_EXCLAMATION, "納税地が入力されていません。入力してください。", strMsgTitle);
			frm.txtTaxableLand.requestFocus();
			return true;
		}else if(frm.txtTaxableLand.getText().length() > 70){//No.22747 2020/08/04 趙 志強 add
			messageBox.disp(MB_EXCLAMATION, "納税地は70桁以内で入力してください。", strMsgTitle);
			frm.txtTaxableLand.requestFocus();
			return true;
		}
		//販売場識別符号//No.22747 2020/08/04 趙 志強 add start
		if (Util.isNullOrEmpty(Util.trim(frm.txtHanbaiSikibetu.getText()))) {
			try {
				if(Util.isSmallerThenByYMD(Util.convertToDate("2021-10-01"),Util.getCurrentDate())) {
					messageBox.disp(MB_EXCLAMATION, "販売場識別符号が入力されていません。"+"\n"+"令和3年10月から免税手続き情報は電子データ送信が必須となります。", strMsgTitle);
					frm.txtHanbaiSikibetu.requestFocus();
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}else if(frm.txtHanbaiSikibetu.getText().length() != 21) {//No.22747 2021/01/25 cf mod
			messageBox.disp(MB_EXCLAMATION, "販売場識別符号は21桁で入力してください。", strMsgTitle);
			frm.txtHanbaiSikibetu.requestFocus();
			return true;
		}
		//No.22747 2020/08/04 趙 志強 add end
		return false;
	}

	private boolean updateMst() {
		BaseDAO basedao = new BaseDAO(appConfig);
		H2 h2 = new H2();
		try {
			h2.h2ClientStart(appConfig.getDatabaseXmlBean());
			h2.setAutoCommit(false);
			h2.startTran();

			//トランザクション開始
			int tranno=TalosTrn.getTrnNo(appConfig);
			int renban=0;

			TaxSetMstDAO daoTaxSet = new TaxSetMstDAO(appConfig);
			DbInfo dbInfoTaxSet = null;

			dbInfoTaxSet = daoTaxSet.select(h2,appConfig.getTenpoCd());
			if(dbInfoTaxSet.getMaxRowCount()>0) {
				dbInfoTaxSet.setCurRow(0);
				dbInfoTaxSet.setCurItem("KOSINFLG", "2");	//更新
			} else {
				dbInfoTaxSet = daoTaxSet.get(h2);
				DbInfoValue tgv = new DbInfoValue(dbInfoTaxSet.getFieldCount());
				dbInfoTaxSet.setRow(tgv);
				dbInfoTaxSet.setCurItem("KOSINFLG", "1");	//新規
				//店舗コード
				dbInfoTaxSet.setCurItem("TENPOCD", appConfig.getTenpoCd());
			}
			//販売者氏名
			dbInfoTaxSet.setCurItem("SELLERNAME", frm.txtHanbaishaNm.getText());
			//販売場所在地
			dbInfoTaxSet.setCurItem("SALESLOCATION", frm.txtHanbaishaJyusho.getText());
			//所轄税務署
			dbInfoTaxSet.setCurItem("TAXOFFICE", frm.txtZeimushoNm.getText());
			//納税地
			dbInfoTaxSet.setCurItem("TAXPAYPLACE", frm.txtTaxableLand.getText());
			//在留資格
//			dbInfoTaxSet.setCurItem("STATUSOFRESIDENCE", frm.txtStatusOfResidence.getText());
			dbInfoTaxSet.setCurItem("STATUSOFRESIDENCE", frm.cmbStatusOfResidence.getSelectedItemValue());//No.22747 2020/09/01 wk chg
			//記載事項言語
			dbInfoTaxSet.setCurItem("LANGKB", frm.cmbLanguage.getSelectedIndex());
			//注釈印字
			if(frm.chkAnnotationPrinting.isSelected()) {
				dbInfoTaxSet.setCurItem("ANNOTATIONKB", 1);
			}else {
				dbInfoTaxSet.setCurItem("ANNOTATIONKB", 0);
			}
			//誓約書印刷フラグ
			if(frm.chkSeiyakushoInjiKb.isSelected()) {
				dbInfoTaxSet.setCurItem("OATHFLG", 1);
			}else {
				dbInfoTaxSet.setCurItem("OATHFLG", 0);
			}
			//記録表印刷フラグ
			if(frm.chkRecordListInjiKb.isSelected()) {
				dbInfoTaxSet.setCurItem("RECORDFLG", 1);
			}else {
				dbInfoTaxSet.setCurItem("RECORDFLG", 0);
			}
			//梱包リスト印刷フラグ
			if(frm.chkPackingListInjiKb.isSelected()) {
				dbInfoTaxSet.setCurItem("PACKINGLISTFLG", 1);
			}else {
				dbInfoTaxSet.setCurItem("PACKINGLISTFLG", 0);
			}
			//販売場識別符号//No.22747 2020/08/04 趙 志強 add
			dbInfoTaxSet.setCurItem("HANBAISIKIBETU", frm.txtHanbaiSikibetu.getText());
			if(certbean != null
					&& !Util.isNullOrEmpty(certbean.getCert())
					&& !Util.isNullOrEmpty(certbean.getPKey())) {
				String strTemp = "";
				strTemp = "-----BEGIN CERTIFICATE-----"+"\n";
				strTemp += certbean.getCert()+"\n";
				strTemp += "-----END CERTIFICATE-----"+"\n";
				dbInfoTaxSet.setCurItem("CLIENTCERTIFY", strTemp);
				
				strTemp = "-----BEGIN PRIVATE KEY-----"+"\n";
				strTemp += certbean.getPKey()+"\n";
				strTemp += "-----END PRIVATE KEY-----"+"\n";
				dbInfoTaxSet.setCurItem("CLIENTCERTKEY", strTemp);
				dbInfoTaxSet.setCurItem("CLIENTCERTEXPIRYDATE"
						, Util.convertToYYYYMMDDHHMMSS(certbean.getExpiry()));
			}
			dbInfoTaxSet.setCurItem("HOSTSOSINFLG", 1);
			
			//最終更新担当者コード
			dbInfoTaxSet.setCurItem("SAISHUKOSINTANTOCD", appConfig.getTantoushaCd());
			// 最終更新日付
			dbInfoTaxSet.setCurItem("SAISHUKOSINDATETIME", Util.getCurrentDateSql());

			// 更新処理
			renban = basedao.insertDbInfo(dbInfoTaxSet, h2, 1, tranno, renban, 0, 1);
			renban = basedao.insertTrnEnd(h2, tranno, renban);
			basedao.execRealTimeSql();

			// ｺﾐｯﾄ処理
			h2.commitTran();

			return false;
		}catch(Exception e){
			try {
				// ﾛｰﾙﾊﾞｯｸ
				h2.rollBackTran();
			} catch (TException te) {
				messageBox.disp(te, MB_CRITICAL, "コミット処理でエラーが発生しました。" + "\n"
						+ te.toString() + e.getMessage(), strMsgTitle);
				return true;
			}
			messageBox.disp(e, MB_CRITICAL, "更新処理でエラーが発生しました。(Exception)" + "\n"
					+ e.toString() + e.getMessage(), strMsgTitle);
			return true;
		}finally{
			try{
				h2.h2ClientStop();
			}catch(TException e){}
		}
	}

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
	//No.22747 2021/02/09 cf add start
	class FileChooseActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				if (e.getSource().equals(frm.btnClientCrtPath)) {
					chooseFile(frm.txtClientCrtPath);
				}
			} catch (Exception te) {
				messageBox.disp(te, MB_CRITICAL, "ファイル指定でエラーが発生しました。(Exception)"
						+ "\n" + e.toString() + te.getMessage(), strMsgTitle);
			}
		}
	}
	
	private void chooseFile(TTextField txtField){

		JFileChooser filechooser = new JFileChooser(txtField.getText());
		filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		filechooser.setAcceptAllFileFilterUsed(false);
		filechooser.addChoosableFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "p12ファイル(*.p12)";
			}

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				String ext = getExtension(f);
				if (ext != null) {
					if (ext.equalsIgnoreCase("p12")) {
						return true;
					}else {
						return false;
					}
				}
				return false;
			}

			private String getExtension(File f) {
				String ext = null;
				String filename = f.getName();
				int dotIndex = filename.lastIndexOf('.');
				if ((dotIndex > 0) && (dotIndex < filename.length() - 1)) {
					ext = filename.substring(dotIndex + 1).toLowerCase();
				}
				return ext;
			}
		});

		int selected = filechooser.showOpenDialog(frm);
		if (selected == JFileChooser.APPROVE_OPTION) {
			String strPwd = "";
			inputPwdPanel pwdInput = new inputPwdPanel(appConfig);
			if(pwdInput.disp() != MB_YES) {
				txtField.setText("");
				return;
			}
			strPwd = pwdInput.getStrReturn();
			File sel_file = null;
			sel_file = filechooser.getSelectedFile();
			ReadKeyStore readKs = new ReadKeyStore();
   			certbean = readKs.openPkcs12(sel_file.getPath(), strPwd);
   			if(certbean == null
   					|| Util.isNullOrEmpty(certbean.getCert()) 
   					|| Util.isNullOrEmpty(certbean.getCert())) {
   				messageBox.disp(MB_EXCLAMATION, "P12ファイルの解析は失敗しました。"+"\n"+"パスワードを確認してください。", strMsgTitle);
   				txtField.setText("");
   				txtField.requestFocus();
   			}else {
   				txtField.setText(sel_file.getPath());
   				if(!Util.isNullOrEmpty(certbean.getExpiry())){
   					frm.txtClientCrtExpiry.setText(Util.convertToYMDHMString(certbean.getExpiry()));
   				}
   			}
			
		} else {
			txtField.setText("");
		}
	}
	//No.22747 2021/02/09 cf add end

}
