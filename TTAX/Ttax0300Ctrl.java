package jp.co.css.TTAX;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.co.css.base.AppConfig;
import jp.co.css.base.BaseCtrl;
import jp.co.css.bean.DbInfo;
import jp.co.css.bean.DbInfoValue;
import jp.co.css.dao.BaseDAO;
import jp.co.css.dao.TalosTrn;
import jp.co.css.dao.TaxMessageMstDAO;
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

/*******************************************************************************
 * 処理名称	：	免税記載事項設定	<br>
 * 作成日 	：	2019/01/22	<br>
 * 作成者	：	鄧　日佳	<br>
 ******************************************************************************/
public class Ttax0300Ctrl extends BaseCtrl implements MessageBoxValue {

	final String strMsgTitle = "免税記載事項設定";
	private String strErrTitle="";
	public Ttax0300 frm=null;
	private String activeId="Ttax0300Ctrl";

	private boolean blnHeader =true;

	// ﾌｫｰｶｽの移動
	List<Component> compList; 						// ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを集めたﾃｰﾌﾞﾙ
	SendTabKeys sendTab = new SendTabKeys(); // ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを取得するｸﾗｽ

	private Map<Container, Component> mapFocus = new HashMap<Container, Component>();

	//H2 共通
	private H2 comH2=null;


	public Ttax0300Ctrl(AppConfig ap, String functionId, Object obj) throws Exception {
		// 初期設定
		super(ap,functionId, obj);
		//起動済みの場合、起動済み画面を呼び出す。
		if (Util.ActiveDisp(appConfig.getDispObjects(), activeId)) {
			return;
		}
		appConfig=ap;
		if (frm == null) {
			frm = new Ttax0300(ap);
		}
		if(comH2==null){
			comH2=new H2();
			try{
				comH2.h2ClientStart(appConfig.getDatabaseXmlBean());
			}catch(TException e){
				messageBox.disp(MB_EXCLAMATION, "H2ｴﾗｰ："+ e.getMessage(), strMsgTitle);
				this.dispClose();
				return;
			}
		}
		//2重起動防止のため追加
		frm.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispClose();
			}
		});
		// 画面の初期化
		logOut.info("画面【" + frm.getTitle() + "】を開きました。");
		init();

		messageBox = new MessageDispNew(frm, appConfig.getLogOut());

		frm.setSize(new Dimension(1024,740));
		frm.setResizable(false);
		// 画面サイズ調整
		TGuiUtil.resizeWindow(frm);
		// 画面の表示
		frm.setVisible(true);
		appConfig.addDisp(activeId, frm);
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void init() {
		// 入力ﾓｰﾄﾞの設定
		blnHeader = true;

		setEnabledFkey();
		setCombo();
		initTextArea();
		setEnabledText();
		frm.cboLangKb.setSelectedIndex(0);
		frm.tabbedPane.setSelectedIndex(0);
		frm.cboLangKb.addKeyListener(new EnterKey());
		frm.txtKirokuhyoTop.addKeyListener(new EnterKey());
		frm.txtKirokuhyoBut.addKeyListener(new EnterKey());
		frm.txtSeiyakushoTop.addKeyListener(new EnterKey());
		frm.txtSeiyakushoBut.addKeyListener(new EnterKey());
		frm.tabbedPane.addChangeListener(new tabChangeListener());

		// ｶｰｿﾙの制御 ※Enableの制御が終わってから設定して!!
		compList = sendTab.setCompList(frm.getContentPane().getComponents());
		frm.setFocusTraversalPolicy(sendTab.setCustomFocus());
	}

	/** ﾌｧﾝｸｼｮﾝｷｰのEnable設定 */
	public void setEnabledFkey() {
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
		frm.fButton.setF12Enabled(!blnHeader);
		if (!blnHeader) {
			frm.fButton.setF9Text("F9 戻る");
			frm.fButton.setF12Enabled(!blnHeader);
		}

		frm.fButton.addFButtonListener(new ButtonListener());
		frm.fButton.setLogOut(logOut);
	}

	public void setEnabledText() {
		frm.tabbedPane.setEnabled(!blnHeader);
		frm.txtKirokuhyoTop.setEnabled(!blnHeader);
		frm.txtKirokuhyoBut.setEnabled(!blnHeader);
		frm.txtSeiyakushoTop.setEnabled(!blnHeader);
		frm.txtSeiyakushoBut.setEnabled(!blnHeader);
	}

	private void setCombo(){
		frm.cboLangKb.removeAllItems();
		String strLanguage[][] =Constants.ARRAY_LANGUAGE_KB;
		for(int i= 0;i<strLanguage.length;i++) {
			frm.cboLangKb.addTextValueItem(strLanguage[i][0], strLanguage[i][1]);
		}
	}
	class EnterKey extends KeyAdapter{
		public void keyPressed(KeyEvent e){
			if (e.getKeyCode() == KeyEvent.VK_ENTER ){
				if(e.getSource().equals(frm.cboLangKb)) {
					frm.cboLangKb.setEnabled(false);
					blnHeader=false;
					setTextAreaValue();
					setEnabledText();
					setEnabledFkey();
					setCompList(frm.tabbedPane);
				}else {
					if ( !e.isControlDown() ){
						sendTab.SendTabKeys(e);
					}
				}
				return;
			}
		}
	}

	class tabChangeListener implements ChangeListener{
		public void stateChanged(ChangeEvent e){
			switch (frm.tabbedPane.getSelectedIndex()) {
			case 0:
				compList = sendTab.setCompList(frm.jPanel1.getComponents());
				frm.txtKirokuhyoTop .requestFocus();
				break;
			case 1:
				compList = sendTab.setCompList(frm.jPanel2.getComponents());
				frm.txtSeiyakushoTop .requestFocus();
				break;
			}
		}
	}
	private void setCompList(Object obj){
		if ( obj == null || !(obj instanceof JTabbedPane)) {
			return ;
		}

		JTabbedPane tb = (JTabbedPane)obj;
		Container container = (Container)tb.getSelectedComponent();

		compList = sendTab.setCompList(container.getComponents());
		if ( mapFocus.containsKey(container) && mapFocus.get(container) != null ){
			mapFocus.get(container).requestFocus();
		}else{
			for(int i=0; i<compList.size(); i++){
				 if ( compList.get(i).isVisible() && compList.get(i).isEnabled() ){
					 compList.get(i).requestFocus();
					 break;
				 }
			}
		}
		frm.setFocusTraversalPolicy(sendTab.setCustomFocus());
	}

	private void dispClose() {
		if(comH2 != null) {
			try {
				comH2.h2ClientStop();
			} catch(TException ex) {
			}
		}
		//ログ処理
		logOut.info("画面【" + frm.getTitle() + "】を閉じました。");
		frm.dispose();
		MainMenu mainMenu = (MainMenu)obj;
		mainMenu.setVisible(true);
		appConfig.removeDisp(activeId);
	}

	class ButtonListener extends FKeyAdapter {
		public void f9Click(ActionEvent e) {
			if (blnHeader) {
				dispClose();
			} else {
				if (messageBox.disp(MB_QUESTION, MB_YESNO, "変更中の内容は破棄されます。よろしいですか？", strMsgTitle) == MB_YES) {
					blnHeader = true;
					frm.cboLangKb.setEnabled(blnHeader);
					initTextArea();
					setEnabledFkey();
					setEnabledText();
					frm.tabbedPane.setSelectedIndex(0);
					frm.cboLangKb.requestFocus();
				}
			}
		}

		public void f12Click(ActionEvent e) {

			if (chkInputData()) {
				return;
			}
			if (messageBox.disp(MB_QUESTION, MB_YESNO, "変更します。よろしいですか？" , strMsgTitle) != MB_YES) {
				return;
			}
			if (updateMst() == true) {
				return;
			}
			blnHeader = true;
			frm.cboLangKb.setEnabled(blnHeader);
			initTextArea();
			setEnabledFkey();
			setEnabledText();
			frm.tabbedPane.setSelectedIndex(0);
			frm.cboLangKb.requestFocus();
		}
	}
	/*
	 * 入力チェック
	 */
	private boolean chkInputData(){
		if(Util.isNullOrEmpty(Util.trim(frm.txtKirokuhyoTop.getText())) && Util.isNullOrEmpty(Util.trim(frm.txtKirokuhyoBut.getText()))) {
			messageBox.disp(MB_INFORMATION, "購入記録票のメッセージが入力されていません。", strMsgTitle);
			frm.tabbedPane.setSelectedIndex(0);
			frm.txtKirokuhyoTop.requestFocus();
			return true;
		}
		if(Util.isNullOrEmpty(Util.trim(frm.txtSeiyakushoTop.getText())) && Util.isNullOrEmpty(Util.trim(frm.txtSeiyakushoBut.getText()))) {
			messageBox.disp(MB_INFORMATION, "購入者誓約書のメッセージが入力されていません。", strMsgTitle);
			frm.tabbedPane.setSelectedIndex(1);
			frm.txtSeiyakushoTop.requestFocus();
			return true;
		}
		return false;
	}

	public void initTextArea() {
		frm.txtKirokuhyoTop.setText("");
		frm.txtKirokuhyoBut.setText("");
		frm.txtSeiyakushoTop.setText("");
		frm.txtSeiyakushoBut.setText("");
	}

	public void setTextAreaValue() {
		strErrTitle="免税記載事項設定の取得";
		TaxMessageMstDAO daoTaxMsg = new TaxMessageMstDAO(appConfig);
		try {
			DbInfo dbInfoTaxMessage = daoTaxMsg.select(comH2,appConfig.getTenpoCd(),frm.cboLangKb.getSelectedIndex());
			if(dbInfoTaxMessage.getMaxRowCount() > 0) {
				frm.txtKirokuhyoTop.setText(dbInfoTaxMessage.getStringItem("MESSAGE1"));			//購入記録票の印字位置　上
				frm.txtKirokuhyoBut.setText(dbInfoTaxMessage.getStringItem("MESSAGE2"));			//購入記録票の印字位置　下
				frm.txtSeiyakushoTop.setText(dbInfoTaxMessage.getStringItem("MESSAGE3"));			//購入者誓約書の印字位置　上
				frm.txtSeiyakushoBut.setText(dbInfoTaxMessage.getStringItem("MESSAGE4"));			//購入者誓約書の印字位置　下
			}
		} catch (TException e) {
			messageBox.disp(e, MB_CRITICAL, "免税記載事項設定の取得でエラーが発生しました。" + "\n" + strErrTitle, strMsgTitle);
		}
	}

	public boolean updateMst() {
		H2 h2 = new H2();
		String strErrTitle = "";

		try {
			h2.h2ClientStart(appConfig.getDatabaseXmlBean());
			h2.setAutoCommit(false); 	// autocommit off( h2の場合 strat // transaction に相当 )
			h2.startTran(); 			// start transaction

			//*** BaseDAOの初期化
			BaseDAO basedao = new BaseDAO(appConfig);
			//*** トランザクション開始
			int tranno = TalosTrn.getTrnNo(appConfig);
			int renban = 0;

			TaxMessageMstDAO daoTaxMsg = new TaxMessageMstDAO(appConfig);
			DbInfo dbInfoTaxMessage = daoTaxMsg.select(h2, appConfig.getTenpoCd(), frm.cboLangKb.getSelectedIndex());
			if(dbInfoTaxMessage == null || dbInfoTaxMessage.getMaxRowCount() == 0) {
				DbInfoValue trv = new DbInfoValue(dbInfoTaxMessage.getFieldCount());
				dbInfoTaxMessage.setRow(trv);
				dbInfoTaxMessage.setCurRow(0);
				dbInfoTaxMessage.setItem(trv,"TENPOCD", appConfig.getTenpoCd());					//店舗コード
				dbInfoTaxMessage.setItem(trv,"LANGKB", frm.cboLangKb.getSelectedIndex());			//言語区分
				dbInfoTaxMessage.setItem(trv,"KOSINFLG", 1);										//更新フラグ
			}else {
				dbInfoTaxMessage.setCurRow(0);
				dbInfoTaxMessage.setCurItem("KOSINFLG", 2);											//更新フラグ
			}
			dbInfoTaxMessage.setCurItem("MESSAGE1", frm.txtKirokuhyoTop.getText());					//メッセージ１
			dbInfoTaxMessage.setCurItem("MESSAGE2", frm.txtKirokuhyoBut.getText());					//メッセージ2
			dbInfoTaxMessage.setCurItem("MESSAGE3", frm.txtSeiyakushoTop.getText());					//メッセージ3
			dbInfoTaxMessage.setCurItem("MESSAGE4", frm.txtSeiyakushoBut.getText());					//メッセージ4
			dbInfoTaxMessage.setCurItem("SAISHUKOSINTANTOCD",appConfig.getTantoushaCd());			//最終更新担当者ｺｰﾄﾞ
			dbInfoTaxMessage.setCurItem("SAISHUKOSINDATETIME",Util.getCurrentDateSql());			//最終更新日付
			dbInfoTaxMessage.setCurItem("HOSTSOSINFLG",1);

			strErrTitle = "[免税記載事項設定のINSERT]";
			renban = basedao.insertDbInfo(dbInfoTaxMessage, h2, 0, tranno, renban);
			renban = basedao.insertTrnEnd(h2, tranno, renban);

			h2.commitTran();
			return false;
		} catch (Exception e) {
			// ﾛｰﾙﾊﾞｯｸ
			try {
				h2.rollBackTran();
			} catch (Exception ex) {
				messageBox.disp(ex, MB_CRITICAL, "ロールバックでエラーが起きました。" + "\n" + e.getMessage(), strMsgTitle);
			}
			messageBox.disp(e, MB_CRITICAL, strErrTitle + "\n" + "更新処理でエラーが起きました。" + "\n" + e.getMessage(), strMsgTitle);
			return true;
		}finally{
			try{
				h2.h2ClientStop();
			}catch(TException e){};
		}
	}
}
