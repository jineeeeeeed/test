package jp.co.css.TTAX;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.border.Border;

import jp.co.css.TCST.ChangeValue;
import jp.co.css.base.AppConfig;
import jp.co.css.base.BaseCtrl;
import jp.co.css.bean.DbInfo;
import jp.co.css.bean.DbInfoValue;
import jp.co.css.communication.ActionService;
import jp.co.css.dao.BaseDAO;
import jp.co.css.dao.KuniMstDAO;
import jp.co.css.dao.TalosTrn;
import jp.co.css.dao.TaxDataDAO;
import jp.co.css.dao.TaxMessageMstDAO;
import jp.co.css.dao.TaxSetMstDAO;
import jp.co.css.dao.TorihikiDataDAO;
import jp.co.css.system.MainMenu;
import jp.co.css.talos_l.util.Constants;
import jp.co.css.talos_l.util.NexUtil;
import jp.co.css.webpos.common.db.H2;
import jp.co.css.webpos.common.except.TException;
import jp.co.css.webpos.common.gui.FKeyAdapter;
import jp.co.css.webpos.common.gui.TEditTable;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.kakasi.KanaChangeUtil;
import jp.co.css.webpos.common.message.MessageBoxValue;
import jp.co.css.webpos.common.pos.PrinterWriter;
import jp.co.css.webpos.common.util.SendTabKeys;
import jp.co.css.webpos.common.util.Util;

/*******************************************************************************
 * 処理名称 ：   免税申請画面      <br>
 * 作成日 　　： 	2020/08/06	  <br>
 * 作成者 　　：  	wk			  <br>
 ******************************************************************************/
public class Ttax0500Ctrl extends BaseCtrl implements MessageBoxValue {

	private final String strMsgTitle = "免税申請画面";  	//ﾒｯｾｰｼﾞ用ﾀｲﾄﾙ
	private Ttax0500 frm = null;						//画面
	private String activeId = "Ttax0500Ctrl";
	private PrinterWriter writer;
	private PrintMenzeiShoruiBean bean = null;
	private final int TYPE_NORMAL = 0;					//現在の操作エリアー一般商品
	private final int TYPE_SHOMOHIN = 1;				//現在の操作エリアー消耗品
	//ﾌｫｰｶｽの移動
	List<Component> compList; 							//ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを集めたﾃｰﾌﾞﾙ
	private SendTabKeys sendTab = new SendTabKeys(); 	//ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを取得するｸﾗｽ
	private int currentType = TYPE_NORMAL;				//現在ﾌｫｰｶｽは一般商品か消耗品か
	private Border border = null;
	private boolean blnHeader = true;;
	//免税設定
	private DbInfo dbInfoTaxSet = null;
	//取引ﾃﾞｰﾀ
	private DbInfo dbInfoTd = null;

	//H2 共通
	private H2 comH2=null;

	private String path = appConfig.getSystemPath() + "/taxImage/";

	private int intShuzeiCount = 0;
	private int intLqNumberTotal = 0;
	private int intLqKinTotal = 0;
	private Map<String, String> mapLqCode = null;

	public Ttax0500Ctrl(AppConfig appConfig,String functionId, Object obj){
		super(appConfig,functionId,obj);
		//起動済みの場合、起動済み画面を呼び出す。
		if (Util.ActiveDisp(appConfig.getDispObjects(), activeId)) {
			return;
		}
		if(comH2==null){
			comH2=new H2();
			try{
				comH2.h2ClientStart(appConfig.getDatabaseXmlBean());
			}catch(TException e){
				messageBox.disp(e, MB_CRITICAL, "H2ｴﾗｰ:" + e.getMessage(), strMsgTitle);
				dispClose();
				return;
			}
		}
		if (frm == null) {
			frm = new Ttax0500(appConfig);
		}

		frm.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispClose();
			}
		});
		//初期設定
		init();

		//未送信、送信エラーチェック
		TaxDataDAO daoTaxData = new TaxDataDAO(appConfig);
		try {
			DbInfo dbInfoNoSend = daoTaxData.selectNoSend(comH2, appConfig.getTenpoCd());
			if(dbInfoNoSend != null && dbInfoNoSend.getMaxRowCount() > 0) {
					messageBox.disp(MB_INFORMATION,
							"未送信のデータがあります。\nF5リトライで再送信してください。", strMsgTitle);
			}
		} catch (TException e1) {
			messageBox.disp(e1, MB_CRITICAL, "免税送信データの取得でエラーが発生しました。" + '\n' + e1.toString() + e1.getMessage(), strMsgTitle);
		}
	}

	/**
	 *  初期設定
	 * @param ap
	 * @param functionId
	 * @param obj
	 */
	private void init() {
		try {
			TaxSetMstDAO daoTaxSetMst= new TaxSetMstDAO(appConfig);
			dbInfoTaxSet = daoTaxSetMst.select(comH2, appConfig.getTenpoCd());
			if(dbInfoTaxSet.getMaxRowCount() ==0) {
				messageBox.disp(MB_EXCLAMATION, "免税設定が登録されていません。先に免税設定を登録してください。" , strMsgTitle);
				frm.dispose();
				return;
			}

			if(!Util.isSmallerThenByYMD(Util.getCurrentDate(), Util.convertToYYMMDDDate("2021/10/01"))){
				if(dbInfoTaxSet != null && dbInfoTaxSet.getMaxRowCount() > 0){
					if(Util.isNullOrEmpty(dbInfoTaxSet.getStringItem("HANBAISIKIBETU"))){
						messageBox.disp(MB_EXCLAMATION, "販売場識別符号が登録されていません。\n免税設定を確認してください。" , strMsgTitle);
					}
				}
			}
		}catch (TException e) {
			messageBox.disp(e, MB_CRITICAL, "免税設定マスタの取得でエラーが発生しました。" + e.getMessage(), strMsgTitle);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		frm.lblInsert.setVisible(true);
		frm.lblCancel.setVisible(false);

		frm.txtDenpyoNo.addKeyListener(new HeaderKeyListener());
		frm.teMainNormal.addFocusListener(new TableFocusListener());
		frm.teMainShomohin.addFocusListener(new TableFocusListener());
		frm.txtPassport.addKeyListener(new PassportKeyListener());
		frm.btnToRight.setAction(new ActionClass());
		frm.btnToLeft.setAction(new ActionClass());

		frm.txtRyokenNo.addKeyListener(new TKeyAdapter());
		frm.cboRyokenType.addKeyListener(new TKeyAdapter());
		frm.txtKokuseiki.addKeyListener(new TKeyAdapter());
		frm.txtKonyuShaNm.addKeyListener(new TKeyAdapter());
		frm.dtBirthday.addKeyListener(new TKeyAdapter());
		frm.dtJorikuDate.addKeyListener(new TKeyAdapter());
		frm.cboZairyuShikaku.addKeyListener(new TKeyAdapter());
		frm.txtHaisoGyoshaNm.addKeyListener(new TKeyAdapter());
		frm.cboLanguage.addKeyListener(new TKeyAdapter());
		frm.chkChushaku.addKeyListener(new TKeyAdapter());
		frm.chkSeiyakuSho.addKeyListener(new TKeyAdapter());
		frm.chkKirokuHyo.addKeyListener(new TKeyAdapter());
		frm.chkKonpobuppinList.addKeyListener(new TKeyAdapter());

		TGuiUtil.addButtonFactionKey(frm.btnToRight, "RIGHT");
		TGuiUtil.addButtonFactionKey(frm.btnToLeft, "LEFT");
		frm.btnToRight.setText("→");
		frm.btnToLeft.setText("←");

		setCboRyoken();
		setCboLanguage();
		setCboZairyuShikaku();
		setFKeyText();
		setFKeyEnabled();
		setTextEnabled();
		initText(1);
		setTaxSetValue();
		setTaxImage();

		//ログ処理
		logOut.info("画面【" + frm.getTitle() + "】を開きました。");
//		画面サイズ調整
		TGuiUtil.resizeWindow(frm);
		frm.setVisible(true);
		appConfig.addDisp(activeId, frm);
	}
	private void setFKeyText() {
		frm.fButton.setF1Text("F1 登録");
		frm.fButton.setF2Text("F2 削除");
		frm.fButton.setF3Text("");
		frm.fButton.setF4Text("");
		frm.fButton.setF5Text("F5 リトライ");
		frm.fButton.setF6Text("");
		frm.fButton.setF7Text("F7 品名編集");//No.25969 2021/11/30 cf chg
		frm.fButton.setF8Text("F8 酒税適用");
		if(blnHeader) {
			frm.fButton.setF9Text("F9 終了");
		}else {
			frm.fButton.setF9Text("F9 戻る");
		}
		frm.fButton.setF10Text("");
		frm.fButton.setF11Text("F11 印刷");
		frm.fButton.setF12Text("F12 確定");
	}
	private void setFKeyEnabled() {
		frm.fButton.setFAllEnabled(false);
		frm.fButton.setF9Enabled(true);
		//ヘッダー
		if(blnHeader) {
			if(frm.mode == 0){//登録
				frm.fButton.butF1.setEnabled(false);
				frm.fButton.butF2.setEnabled(true);
			}else if(frm.mode == 1){//削除
				frm.fButton.butF1.setEnabled(true);
				frm.fButton.butF2.setEnabled(false);
			}
			frm.fButton.setF5Enabled(blnHeader);
			frm.fButton.setF8Enabled(!blnHeader);
			frm.fButton.setF11Enabled(!blnHeader);
			frm.fButton.setF12Enabled(!blnHeader);
		}else{
			frm.fButton.butF1.setEnabled(false);
			frm.fButton.butF2.setEnabled(false);
			frm.fButton.setF5Enabled(false);
			if(frm.mode == 0){
				frm.fButton.setF8Enabled(!blnHeader);
				frm.fButton.setF11Enabled(!blnHeader);
			}else if(frm.mode == 1){
				frm.fButton.setF8Enabled(blnHeader);
				frm.fButton.setF11Enabled(blnHeader);
			}else if(frm.mode == 2){//No.28208 2022/12/5 wk add
				frm.fButton.setF11Enabled(!blnHeader);
			}
			frm.fButton.setF12Enabled(!blnHeader);
		}
		frm.fButton.setF7Enabled(!blnHeader);//No.25969 2021/11/30 cf add
		//ﾌｧﾝｸｼｮﾝｷｰ/ﾎﾞﾀﾝﾘｽﾅ追加
		frm.fButton.addFButtonListener(new ButtonListener());
		//ﾌｧﾝｸｼｮﾝｷｰ/ﾛｸﾞ処理追加
		frm.fButton.setLogOut(logOut);
	}
	private void setTextEnabled() {
		frm.txtDenpyoNo.setEnabled(blnHeader);
		frm.txtPassport.setEnabled(!blnHeader);
		frm.txtRyokenNo.setEnabled(!blnHeader);
		frm.cboRyokenType.setEnabled(!blnHeader);
		frm.txtKokuseiki.setEnabled(!blnHeader);
		frm.txtKonyuShaNm.setEnabled(!blnHeader);
		frm.dtBirthday.setEnabled(!blnHeader);
		frm.dtJorikuDate.setEnabled(!blnHeader);
		frm.cboZairyuShikaku.setEnabled(!blnHeader);
		frm.txtHaisoGyoshaNm.setEnabled(!blnHeader);
		frm.cboLanguage.setEnabled(!blnHeader);
		frm.chkChushaku.setEnabled(!blnHeader);
		frm.chkSeiyakuSho.setEnabled(!blnHeader);
		frm.chkKirokuHyo.setEnabled(!blnHeader);
		frm.chkKonpobuppinList.setEnabled(!blnHeader);
		frm.btnToLeft.setEnabled(!blnHeader);
		frm.btnToRight.setEnabled(!blnHeader);
		// ｶｰｿﾙの制御 ※Enableの制御が終わってから設定して!!
		compList = sendTab.setCompList(frm.getContentPane().getComponents());
		frm.setFocusTraversalPolicy(sendTab.setCustomFocus());
	}

	private void initText(int intKb) {
		if(intKb == 1) {
			frm.txtDenpyoNo.setText("");
			frm.txtKensuNormal.setText("");
			frm.txtTotalKinNormal.setText("");
			frm.txtKensuShomohin.setText("");
			frm.txtTotalKinShomohin.setText("");
			clearTable();
			intShuzeiCount = 0;
			intLqNumberTotal = 0;
			intLqKinTotal = 0;
		}
		frm.txtPassport.setText("");
		frm.cboRyokenType.setSelectedIndex(0);
		frm.txtRyokenNo.setText("");
		frm.txtKokuseiki.setText("");
		frm.txtKonyuShaNm.setText("");
		frm.dtBirthday.setText("");
		frm.dtJorikuDate.setText("");
		frm.txtHaisoGyoshaNm.setText("");
		frm.cboZairyuShikaku.setSelectedIndex(0);
		frm.cboLanguage.setSelectedIndex(0);
	}
	private  void setTaxSetValue() {
		try {
			if(dbInfoTaxSet.getMaxRowCount() > 0) {
				//在留資格
				frm.cboZairyuShikaku.setSelectedItemValue(dbInfoTaxSet.getStringItem("STATUSOFRESIDENCE"));
				//記載事項言語
				frm.cboLanguage.setSelectedIndex(dbInfoTaxSet.getIntItem("LANGKB"));
				//注釈印字
				frm.chkChushaku.setSelected(dbInfoTaxSet.getIntItem("ANNOTATIONKB")==1?true:false);
				//印刷免税書類
				frm.chkKirokuHyo.setSelected(dbInfoTaxSet.getIntItem("RECORDFLG")==1?true:false);
				frm.chkSeiyakuSho.setSelected(dbInfoTaxSet.getIntItem("OATHFLG")==1?true:false);
				frm.chkKonpobuppinList.setSelected(dbInfoTaxSet.getIntItem("PACKINGLISTFLG")==1?true:false);

				frm.txtSellerNm.setText(dbInfoTaxSet.getStringItem("SELLERNAME"));
				frm.txtSalesLocation.setText(dbInfoTaxSet.getStringItem("SALESLOCATION"));
				frm.txtTaxOffice.setText(dbInfoTaxSet.getStringItem("TAXOFFICE"));
				frm.txtTaxpayPlace.setText(dbInfoTaxSet.getStringItem("TAXPAYPLACE"));
				frm.txtHanbaiSymbol.setText(dbInfoTaxSet.getStringItem("HANBAISIKIBETU"));
			}
		}catch (TException e) {
			messageBox.disp(e, MB_CRITICAL, "免税設定マスタの取得でエラーが発生しました。"+e.getMessage(), strMsgTitle);
		}
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
		MainMenu mainMenu = (MainMenu)this.obj;
		mainMenu.setVisible(true);
	}

	class ButtonListener extends FKeyAdapter {
		//F1登録
		public void f1Click(ActionEvent e) {
			frm.mode=0;
			blnHeader = true;
			setFKeyEnabled();
			initText(1);
			setFKeyText();
			setTextEnabled();
			setTaxSetValue();
			clearTable();
			frm.txtDenpyoNo.requestFocus();

			frm.lblInsert.setVisible(true);
			frm.lblCancel.setVisible(false);
		}

		//F2削除
		public void f2Click(ActionEvent e) {
			frm.mode=1;
			blnHeader = true;
			setFKeyEnabled();
			initText(1);
			setFKeyText();
			setTextEnabled();
			setTaxSetValue();
			clearTable();
			frm.txtDenpyoNo.requestFocus();

			frm.lblInsert.setVisible(false);
			frm.lblCancel.setVisible(true);
		}

		//F5 リトライ
		public void f5Click(ActionEvent e) {
			TaxDataDAO daoTaxData = new TaxDataDAO(appConfig);
			try {
				DbInfo dbInfoNoSend = daoTaxData.selectNoSend(comH2, appConfig.getTenpoCd());
				if(dbInfoNoSend == null || dbInfoNoSend.getMaxRowCount() == 0) {
					messageBox.disp(MB_INFORMATION,
							"未送信データがありません。", strMsgTitle);
					return;
				}
				ShowRetry retryPanel = new ShowRetry(appConfig);
				int ans = retryPanel.disp(dbInfoNoSend);
				if(ans != MB_YES) {
					return;
				}
				//No.24990 2021/06/04 wk add start
				if (appConfig.getDebugMode() == 1) {
					return;
				}
				//No.24990 2021/06/04 wk add end
				SendNoBean sendbean = retryPanel.getSendNoBean();
				if(sendbean == null) {
					return;
				}
				for(int i=0; i<dbInfoNoSend.getMaxRowCount(); i++) {
					dbInfoNoSend.setCurRow(i);
					if(sendbean.getDenpyoNo() != dbInfoNoSend.getIntItem("DENPYONO")
							|| sendbean.getTenpocd() != dbInfoNoSend.getIntItem("TENPOCD")
							|| !dbInfoNoSend.getStringItem("SENDNO").equals(sendbean.getSendNo())
							|| sendbean.getDenpyoKb() != dbInfoNoSend.getIntItem("DENPYOKB")
							|| sendbean.getDenpyoNen() != dbInfoNoSend.getIntItem("DENPYONEN")
							) {
						continue;
					}
					String errmsg = "";
					switch(dbInfoNoSend.getIntItem("SENDKB")){
					    case 1 ://登録
					    	errmsg = sendTaxData(sendbean.getDenpyoNo()
					    			, sendbean.getDenpyoNen(), sendbean.getDenpyoKb(), 0);
					       break;
					    case 2 ://取消
					    	errmsg = sendTaxData(sendbean.getDenpyoNo()
					    			, sendbean.getDenpyoNen(), sendbean.getDenpyoKb(), 1);
					       break;
					    default :
					    	return;
					}
					// データの更新処理
					if (updateSosinCode(errmsg, sendbean.getTenpocd(), sendbean.getDenpyoNo()
							, sendbean.getDenpyoNen(), sendbean.getDenpyoKb()) == true) {
						return;
					}
					if(Util.isNullOrEmpty(errmsg)){
						messageBox.disp( MB_INFORMATION, "送信しました。", strMsgTitle);
						blnHeader = true;
						setFKeyEnabled();
						initText(1);
						setFKeyText();
						setTextEnabled();
						setTaxSetValue();
						frm.txtDenpyoNo.requestFocus();
					}

				}

			} catch (TException e1) {
				messageBox.disp(e1, MB_CRITICAL, "免税送信データの取得でエラーが発生しました。" + '\n' + e1.toString() + e1.getMessage(), strMsgTitle);
			}
		}
		//No.25969 2021/11/30 cf add start
		public void f7Click(ActionEvent e){
			int intRow;
			if(currentType == TYPE_NORMAL){
				intRow = frm.teMainNormal.getSelectedRow();
				if ( intRow < 0 ) return ;
				changeText(intRow, frm.teMainNormal, frm.colTanpinNmNormal, "品名");
			}else{
				intRow = frm.teMainShomohin.getSelectedRow();
				if ( intRow < 0 ) return ;
				changeText(intRow, frm.teMainShomohin, frm.colTanpinNmShomohin, "品名");
			}
		}
		private void changeText(int row, TEditTable te, int col, String titel){
			ChangeValue chgValue = new ChangeValue(frm,appConfig);
			chgValue.setMenzeiChk(true);///No.28505 2023/01/06 QIYFIEI add
			long ans = chgValue.disp(titel, 100, te.getValueString(row, col));
			if( ans == MB_OK ){
				te.setValueAt(chgValue.getAfterValue(), row, col);
			}
		}
		//No.25969 2021/11/30 cf add end

		//F8 酒税適用
		public void f8Click(ActionEvent e) {
			AlcoholTaxBean bean = new AlcoholTaxBean();
			int intRow;
			String strOldShuzei = "";
			if(currentType == TYPE_NORMAL){
				intRow = frm.teMainNormal.getSelectedRow();
				strOldShuzei = frm.teMainNormal.getValueString(intRow, frm.collqIndividualNormal);
				if(strOldShuzei.equals("あり")){
					bean.setStrLqIndividual(frm.teMainNormal.getValueString(intRow, frm.collqIndividualNormal));
					bean.setIntLqCode(frm.teMainNormal.getValueInt(intRow, frm.collqCodeNormal));
					bean.setIntLqTaxRate(frm.teMainNormal.getValueInt(intRow, frm.collqTaxRateNormal));
					bean.setIntLqCapacity(frm.teMainNormal.getValueInt(intRow, frm.collqCapacityNormal));
					bean.setIntLqNumber(frm.teMainNormal.getValueInt(intRow, frm.collqNumberNormal));
				}
			}else{
				intRow = frm.teMainShomohin.getSelectedRow();
				strOldShuzei = frm.teMainShomohin.getValueString(intRow, frm.collqIndividualShomohin);
				if(strOldShuzei.equals("あり")){
					bean.setStrLqIndividual(frm.teMainShomohin.getValueString(intRow, frm.collqIndividualShomohin));
					bean.setIntLqCode(frm.teMainShomohin.getValueInt(intRow, frm.collqCodeShomohin));
					bean.setIntLqTaxRate(frm.teMainShomohin.getValueInt(intRow, frm.collqTaxRateShomohin));
					bean.setIntLqCapacity(frm.teMainShomohin.getValueInt(intRow, frm.collqCapacityShomohin));
					bean.setIntLqNumber(frm.teMainShomohin.getValueInt(intRow, frm.collqNumberShomohin));
				}
			}

			try {
				ChangeAlcoholTax changeTax;
				changeTax = new ChangeAlcoholTax(frm, appConfig, bean);
				int intAns = changeTax.disp();
				if (intAns == MB_YES) {
					bean = changeTax.getBean();
					if(bean != null){
						if(currentType == TYPE_NORMAL){
							frm.teMainNormal.setValueAt(bean.getStrLqIndividual(), intRow, frm.collqIndividualNormal);
							frm.teMainNormal.setValueAt(bean.getIntLqCode(), intRow, frm.collqCodeNormal);
							frm.teMainNormal.setValueAt(bean.getStrLqCodeNm(), intRow, frm.collqCodeNmNormal);
							frm.teMainNormal.setValueAt(bean.getIntLqTaxRate(), intRow, frm.collqTaxRateNormal);
							frm.teMainNormal.setValueAt(bean.getIntLqCapacity(), intRow, frm.collqCapacityNormal);
							frm.teMainNormal.setValueAt(bean.getIntLqNumber(), intRow, frm.collqNumberNormal);
						}else{
							frm.teMainShomohin.setValueAt(bean.getStrLqIndividual(), intRow, frm.collqIndividualShomohin);
							frm.teMainShomohin.setValueAt(bean.getIntLqCode(), intRow, frm.collqCodeShomohin);
							frm.teMainShomohin.setValueAt(bean.getStrLqCodeNm(), intRow, frm.collqCodeNmShomohin);
							frm.teMainShomohin.setValueAt(bean.getIntLqTaxRate(), intRow, frm.collqTaxRateShomohin);
							frm.teMainShomohin.setValueAt(bean.getIntLqCapacity(), intRow, frm.collqCapacityShomohin);
							frm.teMainShomohin.setValueAt(bean.getIntLqNumber(), intRow, frm.collqNumberShomohin);
						}
					}
				}
			} catch (TException ex) {
				messageBox.disp(ex, MB_CRITICAL, "酒税適用処理でエラーが発生しました。" + '\n' + ex.toString() + ex.getMessage(), strMsgTitle);
			}
		}

		//F9 終了
		public void f9Click(ActionEvent e) {
			if(!blnHeader){
				if( messageBox.disp(MB_INFORMATION, MB_YESNO,
						"現在処理中のデータは破棄されます。" + "\n" + "よろしいですか？", strMsgTitle) == MB_NO ){
					return;
				}
				if(frm.mode == 2) {
					frm.mode = 0;
				}
				blnHeader = true;
				setFKeyEnabled();
				initText(1);
				setFKeyText();
				setTextEnabled();
				setTaxSetValue();
				clearTable();
				frm.txtDenpyoNo.requestFocus();
			}else {
				dispClose();
			}
		}

		//F11 印刷
		public void f11Click(ActionEvent e) {
			if(chkPrintData()) {
				return;
			}
			if (messageBox.disp(MB_QUESTION, MB_YESNO, "発行します。よろしいですか？",strMsgTitle) == MB_NO) {
				return;
			}
			setPrintDataBean();
			writer = appConfig.getWriter();
			if(frm.chkSeiyakuSho.isSelected()) {//購入者誓約書
				printReceipt(1);
			}
			if(frm.chkKirokuHyo.isSelected()) {//購入記録票
				printReceipt(0);
			}
			if(frm.chkKonpobuppinList.isSelected() && frm.teMainShomohin.getRowCount()>0) {//梱包物品リスト
				printReceipt(2);
			}
		}

		//F12 確定
		public void f12Click(ActionEvent e) {
			int intDenpyoNen = 0;
			int intDenpyoKb = 0;
			intLqNumberTotal = 0;
			intLqKinTotal = 0;
			intShuzeiCount = 0;
			for (int i = 0; i < frm.teMainNormal.getRowCount(); i++) {
				intDenpyoNen = frm.teMainNormal.getValueInt(i, frm.colDenpyoYearNormal);
				intDenpyoKb = frm.teMainNormal.getValueInt(i, frm.colDenpyoKbNormal);
				if(frm.teMainNormal.getValueString(i, frm.collqIndividualNormal).equals("あり")){
					intLqNumberTotal += frm.teMainNormal.getValueInt(i, frm.collqNumberNormal);
					intLqKinTotal += frm.teMainNormal.getValueInt(i, frm.colKinNormal);
					intShuzeiCount++;
				}
				//No.28084 2022/11/21 wk add start
				if(chkTitle(frm.teMainNormal.getValueString(i, frm.colTanpinNmNormal), i+1, 0)) {
					return;
				}
				//No.28084 2022/11/21 wk add end
			}
			for (int i = 0; i < frm.teMainShomohin.getRowCount(); i++) {
				intDenpyoNen = frm.teMainShomohin.getValueInt(i, frm.colDenpyoYearShomohin);
				intDenpyoKb = frm.teMainShomohin.getValueInt(i, frm.colDenpyoKbShomohin);
				if(frm.teMainShomohin.getValueString(i, frm.collqIndividualShomohin).equals("あり")){
					intLqNumberTotal += frm.teMainShomohin.getValueInt(i, frm.collqNumberShomohin);
					intLqKinTotal += frm.teMainShomohin.getValueInt(i, frm.colKinShomohin);
					intShuzeiCount++;
				}
				//No.28084 2022/11/21 wk add start
				if(chkTitle(frm.teMainShomohin.getValueString(i, frm.colTanpinNmShomohin), i+1, 1)) {
					return;
				}
				//No.28084 2022/11/21 wk add end
			}
			int intDenpyoNo = frm.txtDenpyoNo.getTextInt();
			if((frm.mode ==0 || frm.mode ==2) && chkInputData()) {
				return;
			}

			//No.24990 2021/06/04 wk add start
			if (appConfig.getDebugMode() == 1) {
				return;
			}
			//No.24990 2021/06/04 wk add end

			String errmsg = "";
			if(frm.mode == 1) {
				if(messageBox.disp(MB_QUESTION, MB_YESNO, "削除します。よろしいですか？", strMsgTitle) == MB_NO) {
					return;
				}
				errmsg = sendTaxData(intDenpyoNo, intDenpyoNen, intDenpyoKb, 1);
				// データの更新処理
				if (updateMst() == true) {
					return;// エラーがあれば、戻る。
				}
			}else if(frm.mode == 2) {
				if(messageBox.disp(MB_QUESTION, MB_YESNO, "訂正します。よろしいですか？", strMsgTitle) == MB_NO) {
					return;
				}
//				frm.mode = 1;
				errmsg = sendTaxData(intDenpyoNo, intDenpyoNen, intDenpyoKb, 1);
				if(!Util.isNullOrEmpty(errmsg)) {
					return;// エラーがあれば、戻る。
				}
				if (updateMst() == true) {
					return;// エラーがあれば、戻る。
				}

				frm.mode = 0;
				// データの更新処理
				if (updateMst() == true) {
					return;// エラーがあれば、戻る。
				}
				errmsg = sendTaxData(intDenpyoNo, intDenpyoNen, intDenpyoKb, 0);
			}else {
				if(messageBox.disp(MB_QUESTION, MB_YESNO, "新規登録します。よろしいですか？", strMsgTitle) == MB_NO) {
					return;
				}
				// データの更新処理
				if (updateMst() == true) {
					return;// エラーがあれば、戻る。
				}
				errmsg = sendTaxData(intDenpyoNo, intDenpyoNen, intDenpyoKb, 0);
			}

			// データの更新処理
			if (updateSosinCode(errmsg) == true) {
				return;
			}

			if(Util.isNullOrEmpty(errmsg)){
				messageBox.disp( MB_INFORMATION, "送信しました。", strMsgTitle);
				blnHeader = true;
				setFKeyEnabled();
				initText(1);
				setFKeyText();
				setTextEnabled();
				setTaxSetValue();
				frm.txtDenpyoNo.requestFocus();
			}
		}
	}

	private String sendTaxData(int dpNo, int intDenpyoNen, int intDenpyoKb, int intDel) {
		// サーバーと通信クラス初期化
		ActionService acs = new ActionService(appConfig);

		String strPostData = "denpyono=" + dpNo + "&tenpocd=" + appConfig.getTenpoCd()
							+ "&denpyonen=" + intDenpyoNen + "&denpyokb=" + intDenpyoKb + "&kaishacd=" + appConfig.getKaishaCd()
							+ "&del="+intDel;
		try {
			int res = acs.action(appConfig.getWebserviceURL(), "menzeisosinapi/MenzeiSosin.php", strPostData, 0);
			if (res < 0) {
				String errmsg = acs.getErrMessage();
				if(Util.isNumber(errmsg) && frm.mode != 2) {
					errmsg = "送信エラーが発生しました。"+"\n"
							+"時間をおいて、F5リトライから再送信を行ってください。"+"\n"
							+"それでも送信できない場合は、サポートセンターまでお問い合わせください。"+"\n"
							+"その際には、表示されているエラーコードをお伝えください。"+"\n\n"
							+"エラーコード:"+errmsg;
				}else {
					errmsg = "送信エラーが発生しました。"+"\n"
							+"時間をおいて、再実行を行ってください。"+"\n"
							+"それでも送信できない場合は、サポートセンターまでお問い合わせください。"+"\n"
							+"その際には、表示されているエラーコードをお伝えください。"+"\n\n"
							+"エラーコード:"+errmsg;
				}
				messageBox.disp( MB_CRITICAL, errmsg, strMsgTitle);
//			}else{
//				messageBox.disp( MB_INFORMATION, "送信しました。", strMsgTitle);
			}
		} catch (TException ex) {
			String message = "免税送信処理でエラーが起きました。" + "\n" + ex.getMessage();
			messageBox.disp(ex, MB_CRITICAL, message, strMsgTitle);
		}

		return acs.getErrMessage();
	}

	//免税申請印刷
	private void printReceipt(int intPrintKb) {
		setPrintData(writer,intPrintKb);
		writer.print("def/ReceiptPrintFormatMenzei.xml","normal");
	}
	//印刷データ設定
	@SuppressWarnings("rawtypes")
	private void setPrintData(PrinterWriter writer, int intPrintKb) {
		ArrayList<Hashtable> list = new ArrayList<Hashtable>();
		writer.assign("printkb", String.valueOf(intPrintKb));
		if(frm.chkChushaku.isSelected()) {
			writer.assign("englishflg", "1");
		}else {
			writer.assign("englishflg", "0");
		}
		if(!Util.isNullOrEmpty(bean.getStrMsg1())) {
			writer.assign("message1flg", "1");
//			writer.assign("message1", bean.getStrMsg1());
			String strFileName = "taximage_" + frm.cboLanguage.getSelectedItemValue() + "_1.jpg";
			writer.assign("messagefile1", path + strFileName);
		}else {
			writer.assign("message1flg", "0");
		}
		if(!Util.isNullOrEmpty(bean.getStrMsg2())) {
			writer.assign("message2flg", "1");
//			writer.assign("message2", bean.getStrMsg2());
			String strFileName = "taximage_" + frm.cboLanguage.getSelectedItemValue() + "_2.jpg";
			writer.assign("messagefile2", path + strFileName);
		}else {
			writer.assign("message2flg", "0");
		}
		if(!Util.isNullOrEmpty(bean.getStrMsg3())) {
			writer.assign("message3flg", "1");
//			writer.assign("message3", bean.getStrMsg3());
			String strFileName = "taximage_" + frm.cboLanguage.getSelectedItemValue() + "_3.jpg";
			writer.assign("messagefile3", path + strFileName);
		}else {
			writer.assign("message3flg", "0");
		}
		if(!Util.isNullOrEmpty(bean.getStrMsg4())) {
			writer.assign("message4flg", "1");
//			writer.assign("message4", bean.getStrMsg4());
			String strFileName = "taximage_" + frm.cboLanguage.getSelectedItemValue() + "_4.jpg";
			writer.assign("messagefile4", path + strFileName);
		}else {
			writer.assign("message4flg", "0");
		}
		writer.assign("ryoken", bean.getStrRyokenType());						//[旅券の種類]
		writer.assign("bangou", bean.getStrRyokenNo());							//[番号]
		writer.assign("kokuseki", bean.getStrKokuseiki());						//[国籍]
		writer.assign("zairyuushikaku", bean.getStrZairyuShikaku());			//[在留資格]
		writer.assign("jourikuymd", bean.getStrJoriku());						//[上陸年月日]
		writer.assign("space1", getSpace(bean.getStrKonyuShaNm()));				//スペース
		writer.assign("kounyuushashime", addSpace(bean.getStrKonyuShaNm()));	//[購入者氏名]
		writer.assign("seiymd", bean.getStrBirthday());							//[生年月日]
		writer.assign("space2", getSpace(bean.getStrTaxOffice()));				//スペース
		writer.assign("shokatsuzeimusho", addSpace(bean.getStrTaxOffice()));	//[所轄税務署]
		writer.assign("space3", getSpace(bean.getStrTaxPayPlace()));			//スペース
		writer.assign("nouzeichi", addSpace(bean.getStrTaxPayPlace()));			//[納税地]
		writer.assign("space4", getSpace(bean.getStrSellerNm()));				//スペース
		writer.assign("hanbaishanm", addSpace(bean.getStrSellerNm()));			//[販売者名称]
		writer.assign("space5", getSpace(bean.getStrSalesLocation()));			//スペース
		writer.assign("hanbaibashozaichi", addSpace(bean.getStrSalesLocation()));//[販売場所在地]
		writer.assign("kounyuuymd", bean.getStrSellDate());						//[購入年月日]

		//一般物品
		long lngMenzeigaku = 0l;
		for(int i=0; i<frm.teMainNormal.getRowCount(); i++) {
			Hashtable<Object, Object> rowData = new Hashtable<Object, Object>();

			rowData.put("hinmei", frm.teMainNormal.getValueAt(i, frm.colTanpinNmNormal));
			//金額
			long lngKin = frm.teMainNormal.getValueLong(i, frm.colKinNormal);
			//数量
			int intSu = frm.teMainNormal.getValueInt(i, frm.colSuNormal);
			rowData.put("tanka","\\"+Util.getCurFormatDbl(Util.getCurFormat(Util.DoubleCalc(lngKin,"/",intSu, 2,Util.ROUND_FLOOR)),false));
			rowData.put("suuryou", Util.formatNumber(intSu));
			rowData.put("hanbaikakaku","\\"+ Util.formatNumber(lngKin));

			list.add(rowData);

			lngMenzeigaku += frm.teMainNormal.getValueInt(i, frm.colShohizeiNormal);
		}
		writer.assign("goukei1","\\"+ Util.formatNumber(frm.txtTotalKinNormal.getTextLong()));		//合計
		writer.assign("menzeigaku1", "\\"+Util.formatNumber(lngMenzeigaku));						//免税額
		if(list.size()>0 && intPrintKb != 2) {
			writer.assign("printflg1", "1");
		}else {
			writer.assign("printflg1", "0");
		}
		writer.assign("shohinlist1", list);
		//消耗品
		lngMenzeigaku = 0l;
		list = new ArrayList<Hashtable>();
		for(int i=0;i<frm.teMainShomohin.getRowCount();i++) {
			Hashtable<Object, Object> rowData = new Hashtable<Object, Object>();

			rowData.put("hinmei", frm.teMainShomohin.getValueAt(i, frm.colTanpinNmShomohin));
			//金額
			long lngKin = frm.teMainShomohin.getValueLong(i, frm.colKinShomohin);
			//数量
			int intSu = frm.teMainShomohin.getValueInt(i, frm.colSuShomohin);
			rowData.put("tanka","\\"+Util.getCurFormatDbl(Util.getCurFormat(Util.DoubleCalc(lngKin,"/",intSu, 2,Util.ROUND_FLOOR)),false));
			rowData.put("suuryou", Util.formatNumber(intSu));
			rowData.put("hanbaikakaku","\\"+ Util.formatNumber(lngKin));

			list.add(rowData);

			lngMenzeigaku += frm.teMainShomohin.getValueInt(i, frm.colShohizeiShomohin);

		}
		writer.assign("goukei", "\\"+Util.formatNumber(frm.txtTotalKinShomohin.getTextLong()));	//合計
		writer.assign("menzeigaku", "\\"+Util.formatNumber(lngMenzeigaku));						//免税額
		if(list.size()>0) {
			writer.assign("printflg", "1");
		}else {
			writer.assign("printflg", "0");
		}
		writer.assign("shohinlist", list);
	}

	private void setPrintDataBean() {
		try {
			bean = new PrintMenzeiShoruiBean();
			//生年月日
			bean.setStrBirthday(frm.dtBirthday.getText());
			//旅券の種類
			bean.setStrRyokenType(frm.cboRyokenType.getSelectedItemComment());
			//番号
			bean.setStrRyokenNo(frm.txtRyokenNo.getText());
			//国籍
			bean.setStrKokuseiki(frm.txtKokuseiki.getText());
			//上陸年月日
			bean.setStrJoriku(frm.dtJorikuDate.getText());
			//在留資格
//			bean.setStrZairyuShikaku(frm.cboZairyuShikaku.getSelectedItemComment());
			bean.setStrZairyuShikaku(frm.cboZairyuShikaku.getSelectedItemString());//No.25713 2021/10/14 wk chg
			//購入者氏名
			bean.setStrKonyuShaNm(frm.txtKonyuShaNm.getText());

			bean.setStrSellDate(dbInfoTd.getStringItem("DENPYODATE").replaceAll("-", "/"));
			if(dbInfoTaxSet.getMaxRowCount()>0) {
				//所轄税務署
				bean.setStrTaxOffice(dbInfoTaxSet.getStringItem("TAXOFFICE"));
				//納税地
				bean.setStrTaxPayPlace(dbInfoTaxSet.getStringItem("TAXPAYPLACE"));
				//販売者名称
				bean.setStrSellerNm(dbInfoTaxSet.getStringItem("SELLERNAME"));
				//販売場所在地
				bean.setStrSalesLocation(dbInfoTaxSet.getStringItem("SALESLOCATION"));
			}

			TaxMessageMstDAO daoTaxMessage = new TaxMessageMstDAO(appConfig);
			//POCHNLAN<<JIANHUA<<<<<<<<<<<<<<<<<<<<<<<<<<<1424203969CHN3809134M011217819201100<<<<<<08
			DbInfo dbInfoTaxMsg = daoTaxMessage.select(comH2, appConfig.getTenpoCd(), frm.cboLanguage.getSelectedIndex());
			if(dbInfoTaxMsg.getMaxRowCount() >0) {
				bean.setStrMsg1(dbInfoTaxMsg.getStringItem("MESSAGE1"));
				bean.setStrMsg2(dbInfoTaxMsg.getStringItem("MESSAGE2"));
				bean.setStrMsg3(dbInfoTaxMsg.getStringItem("MESSAGE3"));
				bean.setStrMsg4(dbInfoTaxMsg.getStringItem("MESSAGE4"));
			}
		}catch (TException e) {
			messageBox.disp(e, MB_CRITICAL, "印字データの取得でエラーが発生しました。"+e.getMessage(), strMsgTitle);
		}
	}

	class HeaderKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				String strDenpyoNo = frm.txtDenpyoNo.getText();
				if(Util.isNullOrEmpty(strDenpyoNo)) {
					return;
				}
				if(searchDenpyoNo(strDenpyoNo)) {
					blnHeader = true;
					setFKeyEnabled();
					setFKeyText();
					setTextEnabled();
					frm.txtDenpyoNo.requestFocus();
					return;
				}
				blnHeader = false;
				setFKeyEnabled();
				setFKeyText();
				if(frm.mode == 1){
					blnHeader = true;
				}
				setTextEnabled();
				frm.txtDenpyoNo.setEnabled(false);
				if(frm.mode == 0 || frm.mode == 2){
					frm.txtPassport.requestFocus();
				}
			}
		}
	}
	class TableFocusListener extends FocusAdapter {
		public void focusGained(java.awt.event.FocusEvent e) {
			// ﾌｧﾝｸｼｮﾝｷｰの制御
			if (frm.teMainNormal.getRowCount() <= 0) {
				frm.btnToRight.setEnabled(false);
			}else {
				if(frm.mode == 0){
					frm.btnToLeft.setEnabled(true);
				}
			}
			if(frm.teMainShomohin.getRowCount() <=0) {
				frm.btnToLeft.setEnabled(false);
			}else {
				if(frm.mode == 0){
					frm.btnToLeft.setEnabled(true);
				}
			}
			if (e.getSource() == frm.teMainNormal ){
				frm.teMainShomohin.clearSelection();
				border = frm.jScrollPaneNormal.getBorder();
				frm.jScrollPaneNormal.setBorder(javax.swing.BorderFactory.createLineBorder(Color.yellow,2));
				currentType = TYPE_NORMAL;
			}else if (e.getSource() == frm.teMainShomohin){
				frm.teMainNormal.clearSelection();
				border = frm.jScrollPaneShomohin.getBorder();
				frm.jScrollPaneShomohin.setBorder(javax.swing.BorderFactory.createLineBorder(Color.yellow,2));
				currentType = TYPE_SHOMOHIN;
			}

			// ﾌｧﾝｸｼｮﾝｷｰの制御
			blnHeader = false;
		}
		public void focusLost(java.awt.event.FocusEvent e) {
			if (e.getSource() == frm.teMainNormal ){
				frm.jScrollPaneNormal.setBorder(border);
			}else if (e.getSource() == frm.teMainShomohin){
				frm.jScrollPaneShomohin.setBorder(border);
			}
		}
	}
	class ActionClass extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e){
			if(e.getSource().equals(frm.btnToRight)) {
				int intRow = frm.teMainNormal.getSelectedRow();
				if(intRow < 0) {
					return;
				}
//				if(frm.teMainNormal.getValueString(intRow, frm.collqIndividualNormal).equals("あり")){
//					messageBox.disp( MB_EXCLAMATION, "酒税適用物品が移動できません。", strMsgTitle);
//					return;
//				}
				frm.teMainShomohin.addRow();
				int intR = frm.teMainShomohin.getRowCount()-1;
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colTanpinCdNormal),intR , frm.colTanpinCdShomohin);		//単品ｺｰﾄﾞ
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colRankNmNormal),  intR, frm.colRankNmShomohin);			//ランク名
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colTanpinNmNormal), intR, frm.colTanpinNmShomohin);		//タイトル
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colSuNormal), intR, frm.colSuShomohin);			 		//数量
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colKinNormal), intR, frm.colKinShomohin);			 		//金額
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.collqIndividualNormal), intR, frm.collqIndividualShomohin);//酒税適用有無（物品）
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.collqCodeNmNormal), intR, frm.collqCodeNmShomohin);	 	//（酒税）品目分類名称
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.collqTaxRateNormal), intR, frm.collqTaxRateShomohin);		//（酒税）税率
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.collqCapacityNormal), intR, frm.collqCapacityShomohin);	//（酒税）容器容量
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.collqNumberNormal), intR, frm.collqNumberShomohin);		//（酒税）本数
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colShohizeiNormal), intR, frm.colShohizeiShomohin);		//消費税
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colHanbaiTankaNormal), intR, frm.colHanbaiTankaShomohin);	//単価
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colTenpoCdNormal), intR, frm.colTenpoCdShomohin);	 		//店舗コード
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colDenpyoYearNormal), intR, frm.colDenpyoYearShomohin);	//伝票年
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colDenpyoKbNormal), intR, frm.colDenpyoKbShomohin);		//伝票区分
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colDenpyoNoNormal), intR, frm.colDenpyoNoShomohin);		//伝票番号
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colDenpyoGyoNormal), intR, frm.colDenpyoGyoShomohin);		//伝票行位置
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colDenpyoDateNormal), intR, frm.colDenpyoDateShomohin);	//伝票日付
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colJanCdNormal), intR, frm.colJanCdShomohin);				//JANコード
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colShohizeiRateNormal), intR, frm.colShohizeiRateShomohin);//消費税率
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.collqCodeNormal), intR, frm.collqCodeShomohin);	 		//（酒税）品目分類

				TGuiUtil.setScrollBarPosition(frm.teMainShomohin, false, false);

				frm.teMainNormal.removeRow(intRow);

				if(frm.teMainNormal.getRowCount() >0 ) {
					if(intRow != frm.teMainNormal.getRowCount() || intRow == 0) {
						frm.teMainNormal.addRowSelectionInterval(intRow, intRow);
					} else {
						frm.teMainNormal.addRowSelectionInterval(intRow-1, intRow-1);
					}
				}
				frm.teMainNormal.clearSelection();
				currentType = TYPE_SHOMOHIN;

				frm.btnToLeft.setEnabled(true);
				if(frm.teMainNormal.getRowCount() <=0) {
					frm.btnToRight.setEnabled(false);
				}
			}
			if(e.getSource().equals(frm.btnToLeft)) {
				int intRow = frm.teMainShomohin.getSelectedRow();
				if(intRow < 0) {
					return;
				}
				frm.teMainNormal.addRow();
				int intR = frm.teMainNormal.getRowCount()-1;
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colTanpinCdShomohin),intR , frm.colTanpinCdNormal);		//単品ｺｰﾄﾞ
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colRankNmShomohin),  intR, frm.colRankNmNormal);			//ランク名
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colTanpinNmShomohin), intR, frm.colTanpinNmNormal);		//タイトル
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colSuShomohin), intR, frm.colSuNormal);					//数量
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colKinShomohin), intR, frm.colKinNormal);					//金額
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colShohizeiShomohin), intR, frm.colShohizeiNormal);		//消費税
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.collqIndividualShomohin), intR, frm.collqIndividualNormal);//酒税適用有無（物品）
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.collqCodeNmShomohin), intR, frm.collqCodeNmNormal);	 	//（酒税）品目分類名称
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.collqTaxRateShomohin), intR, frm.collqTaxRateNormal);		//（酒税）税率
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.collqCapacityShomohin), intR, frm.collqCapacityNormal);	//（酒税）容器容量
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.collqNumberShomohin), intR, frm.collqNumberNormal);		//（酒税）本数
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colHanbaiTankaShomohin), intR, frm.colHanbaiTankaNormal); //単価
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colTenpoCdShomohin), intR, frm.colTenpoCdNormal);	 		//店舗コード
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colDenpyoYearShomohin), intR, frm.colDenpyoYearNormal);	//伝票年
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colDenpyoKbShomohin), intR, frm.colDenpyoKbNormal);		//伝票区分
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colDenpyoNoShomohin), intR, frm.colDenpyoNoNormal);		//伝票番号
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colDenpyoGyoShomohin), intR, frm.colDenpyoGyoNormal);		//伝票行位置
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colDenpyoDateShomohin), intR, frm.colDenpyoDateNormal);	//伝票日付
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colJanCdShomohin), intR, frm.colJanCdNormal);				//JANコード
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colShohizeiRateShomohin), intR, frm.colShohizeiRateNormal);//消費税率
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.collqCodeShomohin), intR, frm.collqCodeNormal);	 		//（酒税）品目分類

				TGuiUtil.setScrollBarPosition(frm.teMainNormal, false, false);

				frm.teMainShomohin.removeRow(intRow);

				if(frm.teMainShomohin.getRowCount() >0 ) {
					if(intRow != frm.teMainShomohin.getRowCount() || intRow == 0) {
						frm.teMainShomohin.addRowSelectionInterval(intRow, intRow);
					} else {
						frm.teMainShomohin.addRowSelectionInterval(intRow-1, intRow-1);
					}
				}
				frm.teMainShomohin.clearSelection();
				currentType = TYPE_NORMAL;

				frm.btnToRight.setEnabled(true);
				if(frm.teMainShomohin.getRowCount() <=0) {
					frm.btnToLeft.setEnabled(false);
				}
			}
			calcTotal();
			frm.jScrollPaneNormal.updateUI();
			frm.jScrollPaneShomohin.updateUI();
		}
	}
	class PassportKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				if(Util.isNullOrEmpty(frm.txtPassport.getText())) {
					frm.cboRyokenType.requestFocus();
					return;
				}
				//POCHNLAN<<JIANHUA<<<<<<<<<<<<<<<<<<<<<<<<<<<1424203969CHN3809134M011217819201100<<<<<<08
				String strPassport = frm.txtPassport.getText();
				if(strPassport.trim().length() != 88) {
					messageBox.disp(MB_EXCLAMATION, "パスポート以外の読み込みは対応していません。"+ "\n"+"直接入力してください。" , strMsgTitle);
					frm.cboRyokenType.requestFocus();
					return;
				}
				String strPassportGyo1 = strPassport.trim().substring(0, 44);
				String strPassportGyo2 = strPassport.trim().substring(44, 88);

				if(strPassportGyo1.substring(0, 1).equals("P")) {
//					frm.cboRyokenType.setSelectedIndex(1);
					frm.cboRyokenType.setSelectedItemValue("1");
				}else {
					messageBox.disp(MB_EXCLAMATION, "パスポート以外の読み込みは対応していません。"+ "\n"+"直接入力してください。" , strMsgTitle);
					initText(0);
					frm.txtPassport.setText(strPassport);
					frm.cboRyokenType.requestFocus();
					return;
				}
				//No.25588 石暁彩 2021/09/28 chg
//				frm.txtRyokenNo.setText(strPassportGyo2.substring(0,9));//番号
				frm.txtRyokenNo.setText(strPassportGyo2.substring(0,9).replaceAll("<", ""));//番号
				//国籍
				KuniMstDAO daoKuniMst = new KuniMstDAO(appConfig);
				try {
					DbInfo dbInfoKuni= daoKuniMst.selectByKuniCd(comH2, strPassportGyo2.substring(10,13));//国籍
					if(dbInfoKuni.getMaxRowCount()>0) {
						frm.txtKokuseiki.setText(dbInfoKuni.getStringItem("KUNICD"));//国籍
						//0:英語 1:中国語（簡体字） 2:中国語（繁体字） 3:日本語 4:タイ語 5:韓国語
						frm.cboLanguage.setSelectedIndex(dbInfoKuni.getIntItem("LANGKB"));//記載事項言語
					}
				}catch (TException e1) {
					messageBox.disp(e1, MB_CRITICAL, "国ﾃﾞｰﾀの取得でエラーが発生しました。"+e1.getMessage(), strMsgTitle);
				}
				//購入者氏名
				frm.txtKonyuShaNm.setText(Util.trim(strPassportGyo1.substring(5,44).replaceAll("[<]+", " ")));
				//生年月日
				String strBirthday = strPassportGyo2.substring(13,19);
				if(Util.isNumber(strBirthday)) {
					if(Util.getYear(Util.getCurrentDate())-(Util.chgNumericInt(strBirthday.substring(0,2))+1900) >100) {
						strBirthday = "20"+strBirthday;
					}else {
						strBirthday = "19"+strBirthday;
					}
				} else {
					strBirthday = "";
				}
				frm.dtBirthday.setText(strBirthday);//生年月日
				frm.dtJorikuDate.setText("");//上陸年月日
				frm.cboRyokenType.requestFocus();
			}
		}
	}
	class TKeyAdapter extends KeyAdapter {
		public void keyPressed(java.awt.event.KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_ENTER:
				sendTab.SendTabKeys(e);
				frm.teMainNormal.setFocusable(false);
				if(e.getSource().equals(frm.chkKonpobuppinList)) {
					frm.cboRyokenType.requestFocus();
				}
				break;
			}
		}
	}

	private boolean searchDenpyoNo(String strDenpyoNo) {
		TorihikiDataDAO torihikiDao = new TorihikiDataDAO(appConfig);
		try {
			dbInfoTd = torihikiDao.selectMenzeiDenpyoShosai(comH2, frm.txtDenpyoNo.getTextInt(), frm.mode);
			if(dbInfoTd.getMaxRowCount() == 0) {
				messageBox.disp(MB_INFORMATION, "指定の伝票は存在しません。\n伝票番号：" + frm.txtDenpyoNo.getText(), strMsgTitle);
				return true;
			}
			//免税フラグ
			if(dbInfoTd.getIntItem("MENZEIFLG") == 0) {
				messageBox.disp(MB_INFORMATION, "この伝票は免税伝票ではありません。", strMsgTitle);
				return true;
			}
			if(frm.mode != 1) {
				// 赤伝票
				if (dbInfoTd.getIntItem("AKADENKB") == 1) {
					messageBox.disp(MB_EXCLAMATION,	"赤伝の伝票の為、免税書類は発行できません。", strMsgTitle);
					return true;
				}
				// 取消済み伝票
				if (dbInfoTd.getIntItem("TORIKESIZUMIFLG") == 1) {
					messageBox.disp(MB_EXCLAMATION,	"取消済みの伝票の為、免税書類は発行できません。", strMsgTitle);
					return true;
				}
				// 返品伝票
				if (dbInfoTd.getIntItem("HENPINKB") == 1) {
					messageBox.disp(MB_EXCLAMATION,	"返品の伝票の為、免税書類は発行できません。", strMsgTitle);
					return true;
				}
			}

			addTable();
			calcTotal();
		} catch (TException e) {
			messageBox.disp(e, MB_CRITICAL, "取引ﾃﾞｰﾀの取得でエラーが発生しました。"+ e.getMessage(), strMsgTitle);
		}
		return false;
	}

	private void addTable() {
		try {
			int intRow = 0;
			for (int i = 0; i < dbInfoTd.getMaxRowCount(); i++) {
				dbInfoTd.setCurRow(i);
				// ﾃｰﾌﾞﾙ1行追加
				if(dbInfoTd.getIntItem("GOODSTYPE") == 0 || dbInfoTd.getIntItem("GOODSTYPE") == 1){
					frm.teMainNormal.addRow();
					intRow = frm.teMainNormal.getRowCount() - 1;
					//単品ｺｰﾄﾞ
					frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("TANPINCD"), intRow, frm.colTanpinCdNormal);
					//ランク名
					frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("KAKAKUKANRIRANKNM"), intRow, frm.colRankNmNormal);
					//タイトル
//					frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("SHOHINNM"), intRow, frm.colTanpinNmNormal);
					frm.teMainNormal.setValueAt(KanaChangeUtil.zenHanKatakanaConverter(dbInfoTd.getStringItem("SHOHINNM"), Constants.listHanKatakanaJyogai), intRow, frm.colTanpinNmNormal);//No.28505 2023/01/05 QIYIFEI chg　半角を全角に変換、"｢","｣"と半角数字は変換しない
					//数量
					frm.teMainNormal.setValueAt((frm.mode == 1 ? "-" : "") + Util.formatNumber(dbInfoTd.getIntItem("SU")), intRow, frm.colSuNormal);
					//金額
					frm.teMainNormal.setValueAt((frm.mode == 1 ? "-" : "") + Util.formatNumber(dbInfoTd.getIntItem("SOTOZEITAISHOKIN")), intRow, frm.colKinNormal);
					//酒税適用有無（物品）
					frm.teMainNormal.setValueAt(dbInfoTd.getIntItem("LQINDIVIDUAL") == 0 ? "なし" : "あり", intRow, frm.collqIndividualNormal);
					//（酒税）品目分類
					frm.teMainNormal.setValueAt(dbInfoTd.getIntItem("LQINDIVIDUAL") > 0 ? getLqCodeNm(dbInfoTd.getStringItem("LQCODE")) : "", intRow, frm.collqCodeNmNormal);
					//（酒税）税率
					frm.teMainNormal.setValueAt(dbInfoTd.getIntItem("LQINDIVIDUAL") > 0 ? dbInfoTd.getIntItem("LQTAXRATE") : "", intRow, frm.collqTaxRateNormal);
					//（酒税）容器容量
					frm.teMainNormal.setValueAt(dbInfoTd.getIntItem("LQINDIVIDUAL") > 0 ? Util.formatNumber(dbInfoTd.getIntItem("LQCAPACITY")) : "", intRow, frm.collqCapacityNormal);
					//（酒税）本数
					frm.teMainNormal.setValueAt(dbInfoTd.getIntItem("LQINDIVIDUAL") > 0 ? Util.formatNumber(dbInfoTd.getIntItem("LQNUMBER")) : "", intRow, frm.collqNumberNormal);
					//消費税
					frm.teMainNormal.setValueAt(Util.formatNumber(dbInfoTd.getIntItem("SOTOZEISHOHIZEIGAKU")), intRow, frm.colShohizeiNormal);
					//販売単価
					frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("HANBAINYURYOKUTANKA"), intRow, frm.colHanbaiTankaNormal);
					//店舗コード
					frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("TENPOCD"), intRow, frm.colTenpoCdNormal);
					//伝票年
					frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("DENPYONEN"), intRow, frm.colDenpyoYearNormal);
					//伝票区分
					frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("DENPYOKB"), intRow, frm.colDenpyoKbNormal);
					//伝票番号
					frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("DENPYONO"), intRow, frm.colDenpyoNoNormal);
					//伝票行位置
					frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("DENPYOGYO"), intRow, frm.colDenpyoGyoNormal);
					//伝票日付
					frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("DENPYODATE"), intRow, frm.colDenpyoDateNormal);
					//JANコード
					frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("JANCD"), intRow, frm.colJanCdNormal);
					//消費税率
					frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("SHOHIZEIRITU"), intRow, frm.colShohizeiRateNormal);
					//（酒税）品目分類
					frm.teMainNormal.setValueAt(dbInfoTd.getIntItem("LQINDIVIDUAL") > 0 ? dbInfoTd.getIntItem("LQCODE") : "", intRow, frm.collqCodeNormal);

					frm.teMainNormal.setRowSelectionInterval(0, 0);
				}else{
					frm.teMainShomohin.addRow();
					intRow = frm.teMainShomohin.getRowCount() - 1;
					//単品ｺｰﾄﾞ
					frm.teMainShomohin.setValueAt(dbInfoTd.getStringItem("TANPINCD"), intRow, frm.colTanpinCdShomohin);
					//ランク名
					frm.teMainShomohin.setValueAt(dbInfoTd.getStringItem("KAKAKUKANRIRANKNM"), intRow, frm.colRankNmShomohin);
					//タイトル
//					frm.teMainShomohin.setValueAt(dbInfoTd.getStringItem("SHOHINNM"), intRow, frm.colTanpinNmShomohin);
					frm.teMainShomohin.setValueAt(KanaChangeUtil.zenHanKatakanaConverter(dbInfoTd.getStringItem("SHOHINNM"), Constants.listHanKatakanaJyogai), intRow, frm.colTanpinNmShomohin);//No.28505 2023/01/05 QIYIFEI chg　半角を全角に変換、"｢","｣"と半角数字は変換しない
					//数量
					frm.teMainShomohin.setValueAt((frm.mode == 1 ? "-" : "") + Util.formatNumber(dbInfoTd.getIntItem("SU")), intRow, frm.colSuShomohin);
					//金額
					frm.teMainShomohin.setValueAt((frm.mode == 1 ? "-" : "") + Util.formatNumber(dbInfoTd.getIntItem("SOTOZEITAISHOKIN")), intRow, frm.colKinShomohin);
					//酒税適用有無（物品）
					frm.teMainShomohin.setValueAt(dbInfoTd.getIntItem("LQINDIVIDUAL") == 0 ? "なし" : "あり", intRow, frm.collqIndividualShomohin);
					//（酒税）品目分類
					frm.teMainShomohin.setValueAt(dbInfoTd.getIntItem("LQINDIVIDUAL") > 0 ? getLqCodeNm(dbInfoTd.getStringItem("LQCODE")) : "", intRow, frm.collqCodeNmShomohin);
					//（酒税）税率
					frm.teMainShomohin.setValueAt(dbInfoTd.getIntItem("LQINDIVIDUAL") > 0 ? dbInfoTd.getIntItem("LQTAXRATE") : "", intRow, frm.collqTaxRateShomohin);
					//（酒税）容器容量
					frm.teMainShomohin.setValueAt(dbInfoTd.getIntItem("LQINDIVIDUAL") > 0 ? Util.formatNumber(dbInfoTd.getIntItem("LQCAPACITY")) : "", intRow, frm.collqCapacityShomohin);
					//（酒税）本数
					frm.teMainShomohin.setValueAt(dbInfoTd.getIntItem("LQINDIVIDUAL") > 0 ? Util.formatNumber(dbInfoTd.getIntItem("LQNUMBER")) : "", intRow, frm.collqNumberShomohin);
					//消費税
					frm.teMainShomohin.setValueAt(Util.formatNumber(dbInfoTd.getIntItem("SOTOZEISHOHIZEIGAKU")), intRow, frm.colShohizeiShomohin);
					//販売単価
					frm.teMainShomohin.setValueAt(dbInfoTd.getStringItem("HANBAINYURYOKUTANKA"), intRow, frm.colHanbaiTankaShomohin);
					//店舗コード
					frm.teMainShomohin.setValueAt(dbInfoTd.getStringItem("TENPOCD"), intRow, frm.colTenpoCdShomohin);
					//伝票年
					frm.teMainShomohin.setValueAt(dbInfoTd.getStringItem("DENPYONEN"), intRow, frm.colDenpyoYearShomohin);
					//伝票区分
					frm.teMainShomohin.setValueAt(dbInfoTd.getStringItem("DENPYOKB"), intRow, frm.colDenpyoKbShomohin);
					//伝票番号
					frm.teMainShomohin.setValueAt(dbInfoTd.getStringItem("DENPYONO"), intRow, frm.colDenpyoNoShomohin);
					//伝票行位置
					frm.teMainShomohin.setValueAt(dbInfoTd.getStringItem("DENPYOGYO"), intRow, frm.colDenpyoGyoShomohin);
					//伝票日付
					frm.teMainShomohin.setValueAt(dbInfoTd.getStringItem("DENPYODATE"), intRow, frm.colDenpyoDateShomohin);
					//JANコード
					frm.teMainShomohin.setValueAt(dbInfoTd.getStringItem("JANCD"), intRow, frm.colJanCdShomohin);
					//消費税率
					frm.teMainShomohin.setValueAt(dbInfoTd.getStringItem("SHOHIZEIRITU"), intRow, frm.colShohizeiRateShomohin);
					//（酒税）品目分類
					frm.teMainShomohin.setValueAt(dbInfoTd.getIntItem("LQINDIVIDUAL") > 0 ? dbInfoTd.getIntItem("LQCODE") : "", intRow, frm.collqCodeShomohin);

					frm.teMainShomohin.setRowSelectionInterval(0, 0);
				}
			}
			if(dbInfoTd.getIntItem("NUMBER") > 0) {
//				frm.txtPassport.setText(dbInfoTd.getStringItem("PASSPORTNO"));
				if(frm.cboRyokenType.getItemCount() > 0) {
					frm.cboRyokenType.setSelectedItemValue(dbInfoTd.getStringItem("DOCTYPE"));
				}
				//No.28576 2023/01/20 wk chg start
				if(dbInfoTd.getIntItem("DOCTYPE") > 2){
					frm.txtRyokenNo.setText(dbInfoTd.getStringItem("LANDINGPERMITNO"));
				}else {
					frm.txtRyokenNo.setText(dbInfoTd.getStringItem("PASSPORTNO"));
				}
				//No.28576 2023/01/20 wk chg end
				frm.txtKokuseiki.setText(dbInfoTd.getStringItem("NATION"));
				frm.txtKonyuShaNm.setText(dbInfoTd.getStringItem("NAME"));
				frm.dtBirthday.setText(dbInfoTd.getStringItem("BIRTH"));
				frm.dtJorikuDate.setText(dbInfoTd.getStringItem("LANDDATE"));
				frm.txtHaisoGyoshaNm.setText(dbInfoTd.getStringItem("TRANSNAME"));
				if(frm.cboZairyuShikaku.getItemCount() > 0) {
					frm.cboZairyuShikaku.setSelectedItemValue(dbInfoTd.getStringItem("STATUS"));
				}
//				frm.cboLanguage.setSelectedIndex(dbInfoTd.getIntItem("DOCTYPE"));
				if(frm.mode == 0 && "201".equals(dbInfoTd.getStringItem("SENDSTATUS"))) {
					frm.mode = 2;
				}
			}
			frm.jScrollPaneNormal.updateUI();
			frm.jScrollPaneShomohin.updateUI();
		}catch (Exception e) {
			messageBox.disp(e, MB_CRITICAL, "取引伝票データの表示中でエラーが発生しました。" + "\n" + e.getMessage(), strMsgTitle);
		}
	}

	private String getLqCodeNm(String strCd) {
		if(mapLqCode == null || mapLqCode.isEmpty()) {
			TaxCodeUtil codeutil = new TaxCodeUtil();
			mapLqCode = codeutil.getLiquorMap();
		}
		if(mapLqCode.containsKey(strCd)) {
			return  mapLqCode.get(strCd);
		}
		return "";
	}

	//合計金額数量の計算
	private void calcTotal() {
		long lngTotalSu = 0;
		long  lngTotalKin = 0;
		for(int i=0;i<frm.teMainNormal.getRowCount();i++) {
			lngTotalSu += frm.teMainNormal.getValueInt(i, frm.colSuNormal);
			lngTotalKin += frm.teMainNormal.getValueInt(i, frm.colKinNormal);
		}
		frm.txtKensuNormal.setText(Util.formatNumber(lngTotalSu));//一般商品:数量合計
		frm.txtTotalKinNormal.setText(Util.formatNumber(lngTotalKin));//一般商品:金額合計

		lngTotalSu = 0;
		lngTotalKin = 0;
		for(int i=0;i<frm.teMainShomohin.getRowCount();i++) {
			lngTotalSu += frm.teMainShomohin.getValueInt(i, frm.colSuShomohin);
			lngTotalKin += frm.teMainShomohin.getValueInt(i, frm.colKinShomohin);
		}
		frm.txtKensuShomohin.setText(Util.formatNumber(lngTotalSu));//消耗品:数量合計
		frm.txtTotalKinShomohin.setText(Util.formatNumber(lngTotalKin));//消耗品:金額合計
	}

	private void setCboRyoken() {
		frm.cboRyokenType.removeAllItems();
		frm.cboRyokenType.addTextValueItem("1", "1:旅券");
		frm.cboRyokenType.addTextValueItem("2", "2:船舶観光上陸許可書（裏面印刷）");
		frm.cboRyokenType.addTextValueItem("3", "3:船舶観光上陸許可書");
		frm.cboRyokenType.addTextValueItem("4", "4:乗員上陸許可書");
		frm.cboRyokenType.addTextValueItem("5", "5:緊急上陸許可書");
		frm.cboRyokenType.addTextValueItem("6", "6:遭難による上陸許可書");
	}

	private void setCboLanguage() {
		//0:英語 1:中国語（簡体字） 2:中国語（繁体字） 3:日本語 4:タイ語 5:韓国語
		String strLanguage[][] =Constants.ARRAY_LANGUAGE_KB;
		for(int i= 0;i<strLanguage.length;i++) {
			frm.cboLanguage.addTextValueItem(strLanguage[i][0], strLanguage[i][1]);
		}
	}

	private void setCboZairyuShikaku() {
		if(frm.cboZairyuShikaku.getItemCount() > 0) {
			frm.cboZairyuShikaku.removeAllItems();
		}
		TaxCodeUtil codeUtil = new TaxCodeUtil();
		Map<String, String> codemap = codeUtil.getStatMap();
		for (Map.Entry<String, String> entry : codemap.entrySet()) {
			frm.cboZairyuShikaku.addTextValueItem(entry.getKey(), entry.getValue());
		}
	}

	private boolean chkPrintData() {
		if(Util.isNullOrEmpty(Util.trim(frm.txtRyokenNo.getText()))) {
			messageBox.disp(MB_EXCLAMATION, "入力されていない項目があります。"+ "\n"+"入力してください。" , strMsgTitle);
			frm.txtRyokenNo.requestFocus();
			return true;
		}
		if(Util.isNullOrEmpty(Util.trim(frm.txtKokuseiki.getText()))) {
			messageBox.disp(MB_EXCLAMATION, "入力されていない項目があります。"+ "\n"+"入力してください。" , strMsgTitle);
			frm.txtKokuseiki.requestFocus();
			return true;
		}
		if(Util.isNullOrEmpty(Util.trim(frm.txtKonyuShaNm.getText()))) {
			messageBox.disp(MB_EXCLAMATION, "入力されていない項目があります。"+ "\n"+"入力してください。" , strMsgTitle);
			frm.txtKonyuShaNm.requestFocus();
			return true;
		}
		if(Util.isNullOrEmpty(frm.dtBirthday.getText())) {
			messageBox.disp(MB_EXCLAMATION, "入力されていない項目があります。"+ "\n"+"入力してください。" , strMsgTitle);
			frm.dtBirthday.requestFocus();
			return true;
		}
		if(Util.isNullOrEmpty(frm.dtJorikuDate.getText())) {
			messageBox.disp(MB_EXCLAMATION, "入力されていない項目があります。"+ "\n"+"入力してください。" , strMsgTitle);
			frm.dtJorikuDate.requestFocus();
			return true;
		}
		if(!frm.chkSeiyakuSho.isSelected() && !frm.chkKirokuHyo.isSelected() && !frm.chkKonpobuppinList.isSelected()) {
			messageBox.disp(MB_EXCLAMATION, "印刷免税書類が選択されていません。"+ "\n"+"選択してください。" , strMsgTitle);
			frm.chkSeiyakuSho.requestFocus();
			return true;
		}
		if(!frm.chkSeiyakuSho.isSelected() && !frm.chkKirokuHyo.isSelected() && frm.chkKonpobuppinList.isSelected()
				&& frm.teMainShomohin.getRowCount() <=0) {
			messageBox.disp(MB_EXCLAMATION, "梱包物品リストに記載する消耗品商品がありません。" , strMsgTitle);
			frm.chkKonpobuppinList.requestFocus();
			return true;
		}
		return false;
	}

	private boolean chkInputData() {
		try{
			//氏名
			if(Util.isNullOrEmpty(Util.trim(frm.txtKonyuShaNm.getText()))) {
				messageBox.disp(MB_EXCLAMATION, "「氏名」を設定してください。" , strMsgTitle);
				frm.txtKonyuShaNm.requestFocus();
				return true;
			}else{
				if(frm.txtKonyuShaNm.getText().length() > 39){
					messageBox.disp(MB_EXCLAMATION, "「氏名」を39桁以内で入力してください。" , strMsgTitle);
					frm.txtKonyuShaNm.requestFocus();
					return true;
				}
				if(!frm.txtKonyuShaNm.getText().matches("^[[A-Z]\\s]+$")){
					messageBox.disp(MB_EXCLAMATION, "「氏名」が正しくありません。" , strMsgTitle);
					frm.txtKonyuShaNm.requestFocus();
					return true;
				}
			}
			//国籍
			if(Util.isNullOrEmpty(Util.trim(frm.txtKokuseiki.getText()))) {
				messageBox.disp(MB_EXCLAMATION, "「国籍」を設定してください。" , strMsgTitle);
				frm.txtKokuseiki.requestFocus();
				return true;
			}else{
				if(frm.txtKokuseiki.getText().length() != 3){
					messageBox.disp(MB_EXCLAMATION, "「国籍」を3桁で入力してください。" , strMsgTitle);
					frm.txtKokuseiki.requestFocus();
					return true;
				}
				if(!frm.txtKokuseiki.getText().matches("^[A-Z]*")){
					messageBox.disp(MB_EXCLAMATION, "「国籍」が正しくありません。" , strMsgTitle);
					frm.txtKokuseiki.requestFocus();
					return true;
				}
			}
			//生年月日
			if(Util.isNullOrEmpty(frm.dtBirthday.getText())) {
				messageBox.disp(MB_EXCLAMATION, "「生年月日」を設定してください。" , strMsgTitle);
				frm.dtBirthday.requestFocus();
				return true;
			}
			//No.28387 2022/12/5 wk add start
			else if(Util.isSmallerThenByNoEqualYMD(Util.getCurrentDate(), frm.dtBirthday.getTextDate())){
				messageBox.disp(MB_EXCLAMATION, "「生年月日」に未来の日付が入力されています。"+ "\n" + "確認してください。" , strMsgTitle);
				frm.dtBirthday.requestFocus();
				return true;
			}
			//No.28387 2022/12/5 wk add end
			//上陸年月日
			if(Util.isNullOrEmpty(frm.dtJorikuDate.getText())) {
				messageBox.disp(MB_EXCLAMATION, "「上陸年月日」を設定してください。" , strMsgTitle);
				frm.dtJorikuDate.requestFocus();
				return true;
			}
			//No.28387 2022/12/5 wk add start
			else if(Util.isSmallerThenByNoEqualYMD(Util.getCurrentDate(), frm.dtJorikuDate.getTextDate())){
				messageBox.disp(MB_EXCLAMATION, "「上陸年月日」に未来の日付が入力されています。"+ "\n" + "確認してください。" , strMsgTitle);
				frm.dtJorikuDate.requestFocus();
				return true;
			}
			//No.28387 2022/12/5 wk add end
			if(Util.isNullOrEmpty(Util.trim(frm.txtRyokenNo.getText()))) {
				messageBox.disp(MB_EXCLAMATION, "「旅券等の番号」を入力してください。" , strMsgTitle);
				frm.txtRyokenNo.requestFocus();
				return true;
			}else {
				if("1".equals(frm.cboRyokenType.getSelectedItemValue().toString())
						|| "2".equals(frm.cboRyokenType.getSelectedItemValue().toString())){
					if(frm.txtRyokenNo.getText().length() < 7 || frm.txtRyokenNo.getText().length() > 11){
						messageBox.disp(MB_EXCLAMATION, "「旅券等の番号」を7桁以上、11桁以内で設定してください。" , strMsgTitle);
						frm.txtRyokenNo.requestFocus();
						return true;
					}
					if(!frm.txtRyokenNo.getText().matches("^[A-Z0-9]*$")){
						messageBox.disp(MB_EXCLAMATION, "「旅券等の番号」が正しくありません。" , strMsgTitle);
						frm.txtRyokenNo.requestFocus();
						return true;
					}
				}else {
					if(frm.txtRyokenNo.getText().length() < 1 || frm.txtRyokenNo.getText().length() > 15){
						messageBox.disp(MB_EXCLAMATION, "「旅券等の番号」を1桁以上、15桁以内で設定してください。" , strMsgTitle);
						frm.txtRyokenNo.requestFocus();
						return true;
					}
					if(!frm.txtRyokenNo.getText().matches("^[a-zA-Z0-9]*$")){
						messageBox.disp(MB_EXCLAMATION, "「旅券等の番号」が正しくありません。" , strMsgTitle);
						frm.txtRyokenNo.requestFocus();
						return true;
					}
				}
			}
			//販売場識別符号
			if(Util.isNullOrEmpty(Util.trim(frm.txtHanbaiSymbol.getText()))) {
				messageBox.disp(MB_EXCLAMATION, "「販売場識別符号」を設定してください。" , strMsgTitle);
				frm.txtHanbaiSymbol.requestFocus();
				return true;
			}else{
				if(frm.txtHanbaiSymbol.getText().length() != 21){
					messageBox.disp(MB_EXCLAMATION, "「販売場識別符号」を21桁で入力してください。" , strMsgTitle);
					frm.txtHanbaiSymbol.requestFocus();
					return true;
				}
				if(!frm.txtHanbaiSymbol.getText().matches("^[0-9]*$")){
					messageBox.disp(MB_EXCLAMATION, "「販売場識別符号」が正しくありません。" , strMsgTitle);
					frm.txtHanbaiSymbol.requestFocus();
					return true;
				}
			}
			//販売場名称
			if(Util.isNullOrEmpty(Util.trim(frm.txtSellerNm.getText()))) {
				messageBox.disp(MB_EXCLAMATION, "「販売場名称」を設定してください。" , strMsgTitle);
				frm.txtSellerNm.requestFocus();
				return true;
			}else{
				if(frm.txtSellerNm.getText().length() > 70){
					messageBox.disp(MB_EXCLAMATION, "「販売場名称」を70桁以内で設定してください。" , strMsgTitle);
					frm.txtSellerNm.requestFocus();
					return true;
				}
				if(frm.txtSellerNm.getText().getBytes().length > 210){
					messageBox.disp(MB_EXCLAMATION, "「販売場名称」を210 bytes以内で設定してください。" , strMsgTitle);
					frm.txtSellerNm.requestFocus();
					return true;
				}
			}
			//販売場所在地
			if(Util.isNullOrEmpty(Util.trim(frm.txtSalesLocation.getText()))) {
				messageBox.disp(MB_EXCLAMATION, "「販売場所在地」を設定してください。" , strMsgTitle);
				frm.txtSalesLocation.requestFocus();
				return true;
			}else{
				if(frm.txtSalesLocation.getText().length() > 70){
					messageBox.disp(MB_EXCLAMATION, "「販売場所在地」を70桁以内で設定してください。" , strMsgTitle);
					frm.txtSalesLocation.requestFocus();
					return true;
				}
				if(frm.txtSalesLocation.getText().getBytes().length > 210){
					messageBox.disp(MB_EXCLAMATION, "「販売場所在地」を210 bytes以内で設定してください。" , strMsgTitle);
					frm.txtSalesLocation.requestFocus();
					return true;
				}
			}
			//事業者氏名名称
			if(Util.isNullOrEmpty(Util.trim(frm.txtTaxOffice.getText()))) {
				messageBox.disp(MB_EXCLAMATION, "「事業者氏名名称」を設定してください。" , strMsgTitle);
				frm.txtTaxOffice.requestFocus();
				return true;
			}else{
				if(frm.txtTaxOffice.getText().length() > 50){
					messageBox.disp(MB_EXCLAMATION, "「事業者氏名名称」を50桁以内で設定してください。" , strMsgTitle);
					frm.txtTaxOffice.requestFocus();
					return true;
				}
				if(frm.txtTaxOffice.getText().getBytes().length > 150){
					messageBox.disp(MB_EXCLAMATION, "「事業者氏名名称」を150 bytes以内で設定してください。" , strMsgTitle);
					frm.txtTaxOffice.requestFocus();
					return true;
				}
			}
			//納税地
			if(Util.isNullOrEmpty(Util.trim(frm.txtTaxpayPlace.getText()))) {
				messageBox.disp(MB_EXCLAMATION, "「納税地」を設定してください。" , strMsgTitle);
				frm.txtTaxpayPlace.requestFocus();
				return true;
			}else{
				if(frm.txtTaxpayPlace.getText().length() > 50){
					messageBox.disp(MB_EXCLAMATION, "「納税地」を50桁以内で設定してください。" , strMsgTitle);
					frm.txtTaxpayPlace.requestFocus();
					return true;
				}
				if(frm.txtTaxpayPlace.getText().getBytes().length > 150){
					messageBox.disp(MB_EXCLAMATION, "「納税地」を150 bytes以内で設定してください。" , strMsgTitle);
					frm.txtTaxpayPlace.requestFocus();
					return true;
				}
			}
			//海外配送業者名
			if(!Util.isNullOrEmpty(frm.txtHaisoGyoshaNm.getText())){
				if(frm.txtHaisoGyoshaNm.getText().length() > 30){
					messageBox.disp(MB_EXCLAMATION, "「海外配送業者名」を30文字以内で設定してください。" , strMsgTitle);
					frm.txtHaisoGyoshaNm.requestFocus();
					return true;
				}
				if(frm.txtHaisoGyoshaNm.getText().getBytes().length > 90){
					messageBox.disp(MB_EXCLAMATION, "「海外配送業者名」を90 bytes以内で設定してください。" , strMsgTitle);
					frm.txtHaisoGyoshaNm.requestFocus();
					return true;
				}
			}
			//印刷
			//No.25024 2021/06/07 wk chg start
//			if(!frm.chkSeiyakuSho.isSelected() && !frm.chkKirokuHyo.isSelected() && !frm.chkKonpobuppinList.isSelected()) {
//				messageBox.disp(MB_EXCLAMATION, "印刷免税申請が選択されていません。"+ "\n"+"選択してください。" , strMsgTitle);
//				frm.chkSeiyakuSho.requestFocus();
//				return true;
//			}
			//No.25024 2021/06/07 wk chg end
			if(!frm.chkSeiyakuSho.isSelected() && !frm.chkKirokuHyo.isSelected() && frm.chkKonpobuppinList.isSelected()
					&& frm.teMainShomohin.getRowCount() <=0) {
				messageBox.disp(MB_EXCLAMATION, "梱包物品リストに記載する消耗品商品がありません。" , strMsgTitle);
				frm.chkKonpobuppinList.requestFocus();
				return true;
			}
			//一般物品合計額
			if(Util.isNullOrEmpty(frm.txtTotalKinNormal.getText())) {
				messageBox.disp(MB_EXCLAMATION, "「一般物品合計額」が未設定です。" , strMsgTitle);
				return true;
			}else{
				if(frm.txtTotalKinNormal.getTextInt() < -999999999 || frm.txtTotalKinNormal.getTextInt() > 999999999){
					messageBox.disp(MB_EXCLAMATION, "「一般物品合計額」が範囲を超えています。" , strMsgTitle);
					return true;
				}
//				if(frm.teMainNormal.getRowCount() > 0 && frm.txtTotalKinNormal.getTextInt() < 5000){
//					messageBox.disp(MB_EXCLAMATION, "「一般物品合計額」が5000以内になります。" , strMsgTitle);
//					return true;
//				}
			}
			//消耗品合計額
			if(Util.isNullOrEmpty(frm.txtTotalKinShomohin.getText())) {
				messageBox.disp(MB_EXCLAMATION, "「消耗品合計額」が未設定です。" , strMsgTitle);
				return true;
			}else{
				if(frm.txtTotalKinShomohin.getTextInt() < -999999999 || frm.txtTotalKinShomohin.getTextInt() > 999999999){
					messageBox.disp(MB_EXCLAMATION, "「消耗品合計額」の範囲が正しくありません。" , strMsgTitle);
					return true;
				}
//				if(frm.teMainShomohin.getRowCount() > 0 && frm.txtTotalKinShomohin.getTextInt() < 5000){
//					messageBox.disp(MB_EXCLAMATION, "「消耗品合計額」が5000以内になります。" , strMsgTitle);
//					return true;
//				}
//				if(frm.txtTotalKinShomohin.getTextInt() > 500000){
//					messageBox.disp(MB_EXCLAMATION, "「消耗品合計額」が500,000以上になります。" , strMsgTitle);
//					return true;
//				}
			}
			if((frm.txtTotalKinNormal.getTextInt() + frm.txtTotalKinShomohin.getTextInt()) < 5000) {
				messageBox.disp(MB_EXCLAMATION, "「一般物品と消耗品の合計額」が5000以内になります。" , strMsgTitle);
				return true;
			}
			//一般物品テーブル
			for (int i = 0; i < frm.teMainNormal.getRowCount(); i++) {
				if(Util.isNullOrEmpty(frm.teMainNormal.getValueString(i, frm.colTanpinNmNormal))){
					messageBox.disp(MB_EXCLAMATION, "一般物品" + (i+1) + "行目の「タイトル」が未設定です。" , strMsgTitle);
					return true;
				}else{
					if(frm.teMainNormal.getValueString(i, frm.colTanpinNmNormal).length() > 50){
						messageBox.disp(MB_EXCLAMATION, "一般物品" + (i+1) + "行目の「タイトル」を50桁以内で設定してください。" , strMsgTitle);
						return true;
					}
					if(frm.teMainNormal.getValueString(i, frm.colTanpinNmNormal).getBytes().length > 150){
						messageBox.disp(MB_EXCLAMATION, "一般物品" + (i+1) + "行目の「タイトル」を150 bytes以内で設定してください。" , strMsgTitle);
						return true;
					}
//					if(frm.teMainNormal.getValueString(i, frm.colTanpinNmNormal).getBytes("shift-jis").length
//							< (2 * frm.teMainNormal.getValueString(i, frm.colTanpinNmNormal).length())){
//						messageBox.disp(MB_EXCLAMATION, "一般物品" + (i+1) + "行目の「タイトル」を文字のみで設定してください。" , strMsgTitle);
//						return true;
//					}
				}
				if(!Util.isNullOrEmpty(frm.teMainNormal.getValueString(i, frm.colJanCdNormal))){
					if(frm.teMainNormal.getValueString(i, frm.colJanCdNormal).length() > 13){
						messageBox.disp(MB_EXCLAMATION, "「一般物品」" + (i+1) + "行目の「JANコード」を13桁以内で設定してください。" , strMsgTitle);
						return true;
					}
					if(!frm.teMainNormal.getValueString(i, frm.colJanCdNormal).matches("^[0-9]*$")){
						messageBox.disp(MB_EXCLAMATION, "「一般物品」" + (i+1) + "行目の「JANコード」が正しくありません。" , strMsgTitle);
						return true;
					}
				}
				if(Util.isNullOrEmpty(frm.teMainNormal.getValueString(i, frm.colSuNormal))){
					messageBox.disp(MB_EXCLAMATION, "「一般物品」" + (i+1) + "行目の「数量」が未設定です。" , strMsgTitle);
					return true;
				}else{
					if(frm.teMainNormal.getValueInt(i, frm.colSuNormal) < -9999 || frm.teMainNormal.getValueInt(i, frm.colSuNormal) > 9999){
						messageBox.disp(MB_EXCLAMATION, "「一般物品」" + (i+1) + "行目の「数量」が範囲を超えています。" , strMsgTitle);
						return true;
					}
					if(!frm.teMainNormal.getValueString(i, frm.colSuNormal).matches("^([-]?0|[-]?[1-9][0-9]*)?$")){
						messageBox.disp(MB_EXCLAMATION, "「一般物品」" + (i+1) + "行目の「数量」が正しくありません。" , strMsgTitle);
						return true;
					}
				}
				if(Util.isNullOrEmpty(frm.teMainNormal.getValueString(i, frm.colKinNormal))){
					messageBox.disp(MB_EXCLAMATION, "「一般物品」" + (i+1) + "行目の「金額」が未設定です。" , strMsgTitle);
					return true;
				}else{
					if(frm.teMainNormal.getValueInt(i, frm.colKinNormal) < -999999999 || frm.teMainNormal.getValueInt(i, frm.colKinNormal) > 999999999){
						messageBox.disp(MB_EXCLAMATION, "「一般物品」" + (i+1) + "行目の「金額」が範囲を超えています。" , strMsgTitle);
						return true;
					}
					if(!frm.teMainNormal.getValueString(i, frm.colKinNormal).replace(",", "").matches("^([-]?0|[-]?[1-9][0-9]*)?$")){
						messageBox.disp(MB_EXCLAMATION, "「一般物品」" + (i+1) + "行目の「金額」が正しくありません。" , strMsgTitle);
						return true;
					}
				}
			}
			//消耗品テーブル
			for (int i = 0; i < frm.teMainShomohin.getRowCount(); i++) {
				if(Util.isNullOrEmpty(frm.teMainShomohin.getValueString(i, frm.colTanpinNmShomohin))){
					messageBox.disp(MB_EXCLAMATION, "「消耗品」" + (i+1) + "行目の「タイトル」が未設定です。" , strMsgTitle);
					return true;
				}else{
					if(frm.teMainShomohin.getValueString(i, frm.colTanpinNmShomohin).length() > 50){
						messageBox.disp(MB_EXCLAMATION, "「消耗品」" + (i+1) + "行目の「タイトル」を50桁以内で設定してください。" , strMsgTitle);
						return true;
					}
					if(frm.teMainShomohin.getValueString(i, frm.colTanpinNmShomohin).getBytes().length > 150){
						messageBox.disp(MB_EXCLAMATION, "「消耗品」" + (i+1) + "行目の「タイトル」を150 bytes以内で設定してください。" , strMsgTitle);
						return true;
					}
//					if(frm.teMainShomohin.getValueString(i, frm.colTanpinNmShomohin).getBytes("shift-jis").length
//							< (2 * frm.teMainShomohin.getValueString(i, frm.colTanpinNmShomohin).length())){
//						messageBox.disp(MB_EXCLAMATION, "「消耗品」" + (i+1) + "行目の「タイトル」を文字のみで設定してください。" , strMsgTitle);
//						return true;
//					}
				}
				if(!Util.isNullOrEmpty(frm.teMainShomohin.getValueString(i, frm.colJanCdShomohin))){
					if(frm.teMainShomohin.getValueString(i, frm.colJanCdShomohin).length() > 13){
						messageBox.disp(MB_EXCLAMATION, "「消耗品」" + (i+1) + "行目の「JANコード」を13桁以内で設定してください。" , strMsgTitle);
						return true;
					}
					if(!frm.teMainShomohin.getValueString(i, frm.colJanCdShomohin).matches("^[0-9]*")){
						messageBox.disp(MB_EXCLAMATION, "「消耗品」" + (i+1) + "行目の「JANコード」が正しくありません。" , strMsgTitle);
						return true;
					}
				}
				if(Util.isNullOrEmpty(frm.teMainShomohin.getValueString(i, frm.colSuShomohin))){
					messageBox.disp(MB_EXCLAMATION, "「消耗品」" + (i+1) + "行目の「数量」が未設定です。" , strMsgTitle);
					return true;
				}else{
					if(frm.teMainShomohin.getValueInt(i, frm.colSuShomohin) < -9999 || frm.teMainShomohin.getValueInt(i, frm.colSuShomohin) > 9999){
						messageBox.disp(MB_EXCLAMATION, "「消耗品」" + (i+1) + "行目の「数量」が範囲を超えています。" , strMsgTitle);
						return true;
					}
					if(!frm.teMainShomohin.getValueString(i, frm.colSuShomohin).matches("^([-]?0|[-]?[1-9][0-9]*)?$")){
						messageBox.disp(MB_EXCLAMATION, "「消耗品」" + (i+1) + "行目の「数量」が正しくありません。" , strMsgTitle);
						return true;
					}
				}
				if(Util.isNullOrEmpty(frm.teMainShomohin.getValueString(i, frm.colKinShomohin))){
					messageBox.disp(MB_EXCLAMATION, "「消耗品」" + (i+1) + "行目の「金額」が未設定です。" , strMsgTitle);
					return true;
				}else{
					if(frm.teMainShomohin.getValueInt(i, frm.colKinShomohin) < -999999999 || frm.teMainShomohin.getValueInt(i, frm.colKinShomohin) > 999999999){
						messageBox.disp(MB_EXCLAMATION, "「消耗品」" + (i+1) + "行目の「金額」が範囲を超えています。" , strMsgTitle);
						return true;
					}
					if(!frm.teMainShomohin.getValueString(i, frm.colKinShomohin).replace(",", "").matches("^([-]?0|[-]?[1-9][0-9]*)?$")){
						messageBox.disp(MB_EXCLAMATION, "「消耗品」" + (i+1) + "行目の「金額」が正しくありません。" , strMsgTitle);
						return true;
					}
				}
			}

			if(intShuzeiCount > 0){
				if(intLqNumberTotal == 0 || intLqKinTotal == 0){
					messageBox.disp(MB_EXCLAMATION, "「酒税免税対象販売合計額」または「酒税免税対象酒類総本数」が０になります。" , strMsgTitle);
					return true;
				}else{
					if(intLqKinTotal > 999999999 || intLqKinTotal < -999999999){
						messageBox.disp(MB_EXCLAMATION, "「酒税免税対象販売合計額」が範囲を超えています。" , strMsgTitle);
						return true;
					}
					if(intLqNumberTotal > 999999 || intLqNumberTotal < -999999){
						messageBox.disp(MB_EXCLAMATION, "「酒税免税対象酒類総本数」が範囲を超えています。" , strMsgTitle);
						return true;
					}
				}
			}else{
				if(intLqNumberTotal > 0 || intLqKinTotal > 0){
					messageBox.disp(MB_EXCLAMATION, "「酒税適用」が設定しないと、\n" + "「酒税免税対象販売合計額」または「酒税免税対象酒類総本数」が０以上になりました。" , strMsgTitle);
					return true;
				}
			}

			return false;
		}catch (Exception e) {
			messageBox.disp(e, MB_CRITICAL, "データチェック処理でエラーが発生しました。" + "\n"
					+ e.toString() + e.getMessage(), strMsgTitle);
			return false;
		}
	}

	private void clearTable() {
		frm.teMainNormal.setRowCount(0);
		frm.teMainShomohin.setRowCount(0);
		frm.jScrollPaneNormal.updateUI();
		frm.jScrollPaneShomohin.updateUI();
	}

	private void setTaxImage() {
		try {
			TaxMessageMstDAO daoTaxMessage = new TaxMessageMstDAO(appConfig);
			DbInfo dbInfoTaxMsg = daoTaxMessage.select(comH2, appConfig.getTenpoCd());
			for (int i=0;i<dbInfoTaxMsg.getMaxRowCount();i++) {
				dbInfoTaxMsg.setCurRow(i);
				for (int j=1;j<=4;j++) {
					if (!Util.isNullOrEmpty(dbInfoTaxMsg.getStringItem("MESSAGE" + j))) {
						String strFileName = "taximage_" + dbInfoTaxMsg.getIntItem("LANGKB") + "_" + j + ".jpg";
						saveImageFile(dbInfoTaxMsg.getStringItem("MESSAGE" + j), strFileName);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveImageFile(String strValue, String strFileName) {
		try {
			int startX = 5;
			int x = startX;
			int width = 290 * 2;
			int changeRow = 1;
			BufferedImage image = new BufferedImage(width, 100, BufferedImage.TYPE_BYTE_GRAY);
			Graphics2D g2 = image.createGraphics();
			int defaultFontSize = 26;
			Font defaultFont = new Font("Dialog", Font.BOLD, defaultFontSize);
			g2.setFont(defaultFont);
			int intStrWidth = g2.getFontMetrics().stringWidth(strValue);
			if (intStrWidth == 0) {
				return;
			}
			int startY = g2.getFontMetrics().getHeight() / 2 + 4;
			int y = startY;
			// 一行の長さを超える時、
			List<String> values = new ArrayList<String>();
			if (x + intStrWidth > width - startX) {
				String value = "";
				int tmpX = x;
				for (int n=0;n<strValue.length();n++) {
					value += strValue.subSequence(n, n+1);
					//改行コードがある場合は改行をする
					String[] valueArr = value.split("\n",-1);
					if(valueArr.length > 1){
						values.add(valueArr[0]);
						value = "";
						tmpX = startX;
					} else if (tmpX + g2.getFontMetrics().stringWidth(value) >= width - startX) {
						values.add(value);
						value = "";
						tmpX = startX;
					} else if (n + 1 < strValue.length()
							&& tmpX + g2.getFontMetrics().stringWidth(value + strValue.subSequence(n + 1, n + 2)) > width - startX) {
						values.add(value);
						value = "";
						tmpX = startX;
					} else if (n+1 == strValue.length()) {
						values.add(value);
					}
				}
			} else {
				String value = "";
				value = strValue;
				String[] valueArr = value.split("\n",-1);
				if(valueArr.length > 1){
					for (int n=0;n<valueArr.length;n++) {
						values.add(valueArr[n]);
					}
				}else{
					values.add(value);
				}
			}

			int height = g2.getFontMetrics().getHeight() * values.size() + changeRow * (values.size() - 1) - 8;

			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
			g2 = image.createGraphics();
			g2.setFont(defaultFont);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, width, height);
			g2.setColor(Color.BLACK);
			for (int m=0;m<values.size();m++) {
				if (m > 0) {
					y += g2.getFontMetrics().getHeight() + changeRow;
				}
				x = startX;
				intStrWidth = g2.getFontMetrics().stringWidth(values.get(m));
				g2.drawString(values.get(m), x, y);
			}

			Util.createPath(path);
			ImageIO.write(image, "JPEG", new File(path + strFileName));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getSpace(String strValue) {
		String strSpace = "";
		//文字列の長さ
		int intlength = Util.getStringB(strValue);
		//レシートの一行は半角48文字
		if(intlength >= 48) {
			return strSpace;
		}
		int intLoop = 23;
		if(intlength > 25) {
			intLoop = 48 - intlength;
		}
		for(int i = 0; i < intLoop; i++) {
			strSpace += " ";
		}
		return strSpace;
	}

	private String addSpace(String strValue) {
		//文字列の長さ
		int intlength = Util.getStringB(strValue);
		if(intlength >= 25) {
			return strValue;
		}
		String strSpace = "";
		for(int i = 0; i < 25-intlength; i++) {
			strSpace += " ";
		}
		strSpace += strValue;
		return strSpace;
	}

	private String getSendNo() {
		String strDateTime = Util.getCurrentDateString();
		TaxDataDAO daoTax = new TaxDataDAO(appConfig);
		int tenpocd = 0;
		if(frm.teMainNormal.getRowCount() > 0) {
			tenpocd = frm.teMainNormal.getValueInt(0, frm.colTenpoCdNormal);
		}else if(frm.teMainShomohin.getRowCount() > 0){
			tenpocd = frm.teMainShomohin.getValueInt(0, frm.colTenpoCdShomohin);
		}else {
			return strDateTime + "001";
		}
		try {
			DbInfo dbinfoTax = daoTax.selectSendNo(comH2, tenpocd, strDateTime);
			if(dbinfoTax != null && dbinfoTax.getMaxRowCount() > 0 && !Util.isNullOrEmpty(dbinfoTax.getStringItem("SENDNO"))) {
				return String.valueOf(dbinfoTax.getLongItem("SENDNO")+1);
			}
		} catch (TException e) {
			return strDateTime + "001";
		}
		return strDateTime + "001";
	}

	private boolean updateMst() {
		BaseDAO basedao = new BaseDAO(appConfig);
		H2 h2 = new H2();
		String strErrTitle = "";
		DbInfo infoTax = null;
		try {
			// 更新処理
			h2.h2ClientStart(appConfig.getDatabaseXmlBean());
			h2.setAutoCommit(false);
			h2.startTran();

			//トランザクション開始
			int tranno = TalosTrn.getTrnNo(appConfig);
			int renban = 0;

			strErrTitle = "[免税設定マスタINFO作成]";
			TaxDataDAO daoTax = new TaxDataDAO(appConfig);
			infoTax = daoTax.get(h2);
//			int intSosinNo = 0;
//			String strDateTime = Util.getCurrentDateString();
			String strSendNo = getSendNo();
//			//一般物品と消耗品とを合算して免税適用を判定した場合に消耗品とみなされる一般物品については、「2」=消耗品を設定する。
//			boolean isAllShomohin = !Util.isNullOrEmpty(frm.txtTotalKinShomohin.getText())
//											&& frm.txtTotalKinShomohin.getTextLong() > 0
//											&& frm.txtTotalKinShomohin.getTextLong() < 5000;

			for (int i = 0; i < frm.teMainNormal.getRowCount(); i++) {
				DbInfo info = daoTax.select(h2, frm.teMainNormal.getValueInt(i, frm.colTenpoCdNormal)
						, frm.teMainNormal.getValueInt(i, frm.colDenpyoYearNormal), frm.teMainNormal.getValueInt(i, frm.colDenpyoKbNormal)
						, frm.teMainNormal.getValueInt(i, frm.colDenpyoNoNormal), frm.teMainNormal.getValueInt(i, frm.colDenpyoGyoNormal));
				if(info != null && info.getMaxRowCount() > 0){
					infoTax.setRow(info.getRow());
					infoTax.setCurRow(infoTax.getMaxRowCount()-1);
					if(frm.mode==0) {
						infoTax.setCurItem("KOSINFLG", "2");	//変更
					}else{
						infoTax.setCurItem("SENDKB", "2");

						infoTax.setCurItem("GENERALTOTAL", frm.txtTotalKinNormal.getText().replace(",", ""));
						infoTax.setCurItem("CONSUMTOTAL", frm.txtTotalKinShomohin.getText().replace(",", ""));
						infoTax.setCurItem("NUMBER", frm.teMainNormal.getValueString(i, frm.colSuNormal));
						infoTax.setCurItem("PRICE", frm.teMainNormal.getValueString(i, frm.colKinNormal).replace(",", ""));
						infoTax.setCurItem("LQTOTAL", intShuzeiCount > 0 ? intLqKinTotal : "");
						infoTax.setCurItem("LQTOTALNUM", intShuzeiCount > 0 ? intLqNumberTotal : "");

						infoTax.setCurItem("KOSINFLG", "3");	//削除
						infoTax.setCurItem("DATASAKUJODATETIME", Util.getCurrentDateSql());
						continue;
					}
				}else{
					if(frm.mode==0) {
						DbInfoValue tgv = new DbInfoValue(infoTax.getFieldCount());
						infoTax.setRow(tgv);
						infoTax.setCurRow(infoTax.getMaxRowCount() - 1);
						infoTax.setCurItem("SENDKB", "1");
						infoTax.setCurItem("KOSINFLG", "1");	//新規
					}else{
						continue;
					}
				}

				infoTax.setCurItem("SENDSTATUS", "");
				//ヘッダ情報
				if(info == null || info.getMaxRowCount() == 0){
					infoTax.setCurItem("TENPOCD", frm.teMainNormal.getValueString(i, frm.colTenpoCdNormal));
					infoTax.setCurItem("DENPYONEN", frm.teMainNormal.getValueString(i, frm.colDenpyoYearNormal));
					infoTax.setCurItem("DENPYOKB", frm.teMainNormal.getValueString(i, frm.colDenpyoKbNormal));
					infoTax.setCurItem("DENPYONO", frm.teMainNormal.getValueString(i, frm.colDenpyoNoNormal));
					infoTax.setCurItem("DENPYOGYO", frm.teMainNormal.getValueString(i, frm.colDenpyoGyoNormal));
//					infoTax.setCurItem("SENDNO", strDateTime + String.format("%03d", intSosinNo++));
					infoTax.setCurItem("SENDNO", strSendNo);
				}
				infoTax.setCurItem("SENDERID", frm.txtHanbaiSymbol.getText());
				infoTax.setCurItem("SENDERIDTYPE", "1");
				infoTax.setCurItem("PROCEDURESID", "A");
				infoTax.setCurItem("VERSION", "1");
				//旅券等情報
				infoTax.setCurItem("NAME", frm.txtKonyuShaNm.getText());
				infoTax.setCurItem("NATION", frm.txtKokuseiki.getText());
				infoTax.setCurItem("BIRTH", frm.dtBirthday.getText().replace("/", ""));
				infoTax.setCurItem("STATUS", frm.cboZairyuShikaku.getSelectedItemValue());
				infoTax.setCurItem("LANDDATE", frm.dtJorikuDate.getText().replace("/", ""));
				infoTax.setCurItem("DOCTYPE", frm.cboRyokenType.getSelectedItemValue());
				//No.28576 2023/01/13 wk add start
				if("1".equals(frm.cboRyokenType.getSelectedItemValue().toString())
						|| "2".equals(frm.cboRyokenType.getSelectedItemValue().toString())){
					infoTax.setCurItem("PASSPORTNO", frm.txtRyokenNo.getText());//旅券番号
				}else {
					infoTax.setCurItem("LANDINGPERMITNO", frm.txtRyokenNo.getText());//許可書番号
				}
				//No.28576 2023/01/13 wk add end
				//販売場情報
				infoTax.setCurItem("SHOPID", frm.txtHanbaiSymbol.getText());
				infoTax.setCurItem("SHOPNAME", frm.txtSellerNm.getText());
				infoTax.setCurItem("SHOPPLACE", frm.txtSalesLocation.getText());
				infoTax.setCurItem("BIZNAME", frm.txtTaxOffice.getText());
				infoTax.setCurItem("BIZPLACE", frm.txtTaxpayPlace.getText());
				infoTax.setCurItem("SELLDATE", frm.teMainNormal.getValueString(i, frm.colDenpyoDateNormal).replace("-", ""));
				infoTax.setCurItem("TRANSORNOT", Util.isNullOrEmpty(frm.txtHaisoGyoshaNm.getText()) ? 0 : 1);
				infoTax.setCurItem("TRANSNAME", frm.txtHaisoGyoshaNm.getText());
				infoTax.setCurItem("GENERALTOTAL", frm.txtTotalKinNormal.getText().replace(",", ""));
				infoTax.setCurItem("CONSUMTOTAL", frm.txtTotalKinShomohin.getText().replace(",", ""));
				infoTax.setCurItem("LQEXEMPTORNOT", intShuzeiCount > 0 ? 1 : 0);
				infoTax.setCurItem("LQTOTAL", intShuzeiCount > 0 ? intLqKinTotal : "");
				infoTax.setCurItem("LQTOTALNUM", intShuzeiCount > 0 ? intLqNumberTotal : "");
				//物品情報
				infoTax.setCurItem("SERIAL", frm.teMainNormal.getValueString(i, frm.colDenpyoGyoNormal));
				infoTax.setCurItem("GOODSTYPE", 1);
				infoTax.setCurItem("GOODSNAME", frm.teMainNormal.getValueString(i, frm.colTanpinNmNormal));
				infoTax.setCurItem("JANCODE", frm.teMainNormal.getValueString(i, frm.colJanCdNormal));
				infoTax.setCurItem("REDUCED", frm.teMainNormal.getValueInt(i, frm.colShohizeiRateNormal) == 8 ? 1 : 0);
				infoTax.setCurItem("NUMBER", frm.teMainNormal.getValueString(i, frm.colSuNormal));
				infoTax.setCurItem("PRICE", frm.teMainNormal.getValueString(i, frm.colKinNormal).replace(",", ""));
				infoTax.setCurItem("LQINDIVIDUAL", frm.teMainNormal.getValueString(i, frm.collqIndividualNormal).equals("なし") ? 0 : 1);
				infoTax.setCurItem("LQCODE", frm.teMainNormal.getValueString(i, frm.collqCodeNormal));
				infoTax.setCurItem("LQTAXRATE", frm.teMainNormal.getValueString(i, frm.collqTaxRateNormal));
				infoTax.setCurItem("LQCAPACITY", frm.teMainNormal.getValueString(i, frm.collqCapacityNormal));
				infoTax.setCurItem("LQNUMBER", frm.teMainNormal.getValueString(i, frm.collqNumberNormal).replace(",", ""));

				infoTax.setCurItem("SAISHUKOSINTANTOCD", String.valueOf(appConfig.getTantoushaCd()));
				infoTax.setCurItem("SAISHUKOSINDATETIME", Util.getCurrentDateSql());
				infoTax.setCurItem("HOSTSOSINFLG", 1);
			}

			for (int i = 0; i < frm.teMainShomohin.getRowCount(); i++) {
				DbInfo info = daoTax.select(h2, frm.teMainShomohin.getValueInt(i, frm.colTenpoCdShomohin)
						, frm.teMainShomohin.getValueInt(i, frm.colDenpyoYearShomohin), frm.teMainShomohin.getValueInt(i, frm.colDenpyoKbShomohin)
						, frm.teMainShomohin.getValueInt(i, frm.colDenpyoNoShomohin), frm.teMainShomohin.getValueInt(i, frm.colDenpyoGyoShomohin));
				if(info != null && info.getMaxRowCount() > 0){
					infoTax.setRow(info.getRow());
					infoTax.setCurRow(infoTax.getMaxRowCount()-1);
					if(frm.mode==0) {
						infoTax.setCurItem("KOSINFLG", "2");	//変更
					}else{
						infoTax.setCurItem("SENDKB", "2");

						infoTax.setCurItem("GENERALTOTAL", frm.txtTotalKinNormal.getText().replace(",", ""));
						infoTax.setCurItem("CONSUMTOTAL", frm.txtTotalKinShomohin.getText().replace(",", ""));
						infoTax.setCurItem("NUMBER", frm.teMainShomohin.getValueString(i, frm.colSuShomohin));
						infoTax.setCurItem("PRICE", frm.teMainShomohin.getValueString(i, frm.colKinShomohin).replace(",", ""));
						infoTax.setCurItem("LQTOTAL", intShuzeiCount > 0 ? intLqKinTotal : "");
						infoTax.setCurItem("LQTOTALNUM", intShuzeiCount > 0 ? intLqNumberTotal : "");

						infoTax.setCurItem("KOSINFLG", "3");	//削除
						infoTax.setCurItem("DATASAKUJODATETIME", Util.getCurrentDateSql());
						continue;
					}
				}else{
					if(frm.mode==0) {
						DbInfoValue tgv = new DbInfoValue(infoTax.getFieldCount());
						infoTax.setRow(tgv);
						infoTax.setCurRow(infoTax.getMaxRowCount() - 1);
						infoTax.setCurItem("SENDKB", "1");
						infoTax.setCurItem("KOSINFLG", "1");	//新規
					}else{
						continue;
					}
				}

				infoTax.setCurItem("SENDSTATUS", "");
				//ヘッダ情報
				if(info == null || info.getMaxRowCount() == 0){
					infoTax.setCurItem("TENPOCD", frm.teMainShomohin.getValueString(i, frm.colTenpoCdShomohin));
					infoTax.setCurItem("DENPYONEN", frm.teMainShomohin.getValueString(i, frm.colDenpyoYearShomohin));
					infoTax.setCurItem("DENPYOKB", frm.teMainShomohin.getValueString(i, frm.colDenpyoKbShomohin));
					infoTax.setCurItem("DENPYONO", frm.teMainShomohin.getValueString(i, frm.colDenpyoNoShomohin));
					infoTax.setCurItem("DENPYOGYO", frm.teMainShomohin.getValueString(i, frm.colDenpyoGyoShomohin));
//					infoTax.setCurItem("SENDNO", strDateTime + String.format("%03d", intSosinNo++));
					infoTax.setCurItem("SENDNO", strSendNo);
				}
				infoTax.setCurItem("SENDERID", frm.txtHanbaiSymbol.getText());
				infoTax.setCurItem("SENDERIDTYPE", "1");
				infoTax.setCurItem("PROCEDURESID", "A");
				infoTax.setCurItem("VERSION", "1");
				//旅券等情報
				infoTax.setCurItem("NAME", frm.txtKonyuShaNm.getText());
				infoTax.setCurItem("NATION", frm.txtKokuseiki.getText());
				infoTax.setCurItem("BIRTH", frm.dtBirthday.getText().replace("/", ""));
				infoTax.setCurItem("STATUS", frm.cboZairyuShikaku.getSelectedItemValue());
				infoTax.setCurItem("LANDDATE", frm.dtJorikuDate.getText().replace("/", ""));
				infoTax.setCurItem("DOCTYPE", frm.cboRyokenType.getSelectedItemValue());
				//No.28576 2023/01/13 wk add start
				if("1".equals(frm.cboRyokenType.getSelectedItemValue().toString())
						|| "2".equals(frm.cboRyokenType.getSelectedItemValue().toString())){
					infoTax.setCurItem("PASSPORTNO", frm.txtRyokenNo.getText());//旅券番号
				}else {
					infoTax.setCurItem("LANDINGPERMITNO", frm.txtRyokenNo.getText());//許可書番号
				}
				//No.28576 2023/01/13 wk add end
				//販売場情報
				infoTax.setCurItem("SHOPID", frm.txtHanbaiSymbol.getText());
				infoTax.setCurItem("SHOPNAME", frm.txtSellerNm.getText());
				infoTax.setCurItem("SHOPPLACE", frm.txtSalesLocation.getText());
				infoTax.setCurItem("BIZNAME", frm.txtTaxOffice.getText());
				infoTax.setCurItem("BIZPLACE", frm.txtTaxpayPlace.getText());
				infoTax.setCurItem("SELLDATE", frm.teMainShomohin.getValueString(i, frm.colDenpyoDateShomohin).replace("-", ""));
				infoTax.setCurItem("TRANSORNOT", Util.isNullOrEmpty(frm.txtHaisoGyoshaNm.getText()) ? 0 : 1);
				infoTax.setCurItem("TRANSNAME", frm.txtHaisoGyoshaNm.getText());
				infoTax.setCurItem("GENERALTOTAL", frm.txtTotalKinNormal.getText().replace(",", ""));
				infoTax.setCurItem("CONSUMTOTAL", frm.txtTotalKinShomohin.getText().replace(",", ""));
				infoTax.setCurItem("LQEXEMPTORNOT", intShuzeiCount > 0 ? 1 : 0);
				infoTax.setCurItem("LQTOTAL", intShuzeiCount > 0 ? intLqKinTotal : 0);
				infoTax.setCurItem("LQTOTALNUM", intShuzeiCount > 0 ? intLqNumberTotal : "");
				//物品情報
				infoTax.setCurItem("SERIAL", frm.teMainShomohin.getValueString(i, frm.colDenpyoGyoShomohin));
				infoTax.setCurItem("GOODSTYPE", 2);
				infoTax.setCurItem("GOODSNAME", frm.teMainShomohin.getValueString(i, frm.colTanpinNmShomohin));
				infoTax.setCurItem("JANCODE", frm.teMainShomohin.getValueString(i, frm.colJanCdShomohin));
				infoTax.setCurItem("REDUCED", frm.teMainShomohin.getValueInt(i, frm.colShohizeiRateShomohin) == 8 ? 1 : 0);
				infoTax.setCurItem("NUMBER", frm.teMainShomohin.getValueString(i, frm.colSuShomohin));
				infoTax.setCurItem("PRICE", frm.teMainShomohin.getValueString(i, frm.colKinShomohin).replace(",", ""));
				infoTax.setCurItem("LQINDIVIDUAL", frm.teMainShomohin.getValueString(i, frm.collqIndividualShomohin).equals("なし") ? 0 : 1);
				infoTax.setCurItem("LQCODE", frm.teMainShomohin.getValueString(i, frm.collqCodeShomohin));
				infoTax.setCurItem("LQTAXRATE", frm.teMainShomohin.getValueString(i, frm.collqTaxRateShomohin));
				infoTax.setCurItem("LQCAPACITY", frm.teMainShomohin.getValueString(i, frm.collqCapacityShomohin));
				infoTax.setCurItem("LQNUMBER", frm.teMainShomohin.getValueString(i, frm.collqNumberShomohin).replace(",", ""));

				infoTax.setCurItem("SAISHUKOSINTANTOCD", String.valueOf(appConfig.getTantoushaCd()));
				infoTax.setCurItem("SAISHUKOSINDATETIME", Util.getCurrentDateSql());
				infoTax.setCurItem("HOSTSOSINFLG", 1);
			}
			strErrTitle = "[免税設定マスタの更新]";
			renban = basedao.insertDbInfo(infoTax, h2, 1, tranno, renban, 0, 1);

			renban = basedao.insertTrnEnd(h2, tranno, renban);

			strErrTitle = "[リアルタイム更新処理]";
			basedao.execRealTimeSql();

			h2.commitTran();
			return false;
		}catch(Exception e){
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

	private boolean updateSosinCode(String errCode) {
		BaseDAO basedao = new BaseDAO(appConfig);
		H2 h2 = new H2();
		String strErrTitle = "";
		try {
			if(!Util.isNullOrEmpty(errCode)) {
				if(errCode.startsWith("PHP")) {
					errCode = "PHP ERROR";
				}else if(errCode.length() > 20) {
					errCode = errCode.substring(0,20);
				}
			}
			// 更新処理
			h2.h2ClientStart(appConfig.getDatabaseXmlBean());
			h2.setAutoCommit(false);
			h2.startTran();

			//トランザクション開始
			int tranno = TalosTrn.getTrnNo(appConfig);
			int renban = 0;

			strErrTitle = "[免税設定マスタINFO作成]";
			TaxDataDAO daoTax = new TaxDataDAO(appConfig);
			DbInfo info = null;
			if(frm.teMainNormal.getRowCount() > 0) {
				info = daoTax.select(h2, frm.teMainNormal.getValueInt(0, frm.colTenpoCdNormal)
						, frm.teMainNormal.getValueInt(0, frm.colDenpyoYearNormal), frm.teMainNormal.getValueInt(0, frm.colDenpyoKbNormal)
						, frm.teMainNormal.getValueInt(0, frm.colDenpyoNoNormal));
				if(info != null && info.getMaxRowCount() > 0){
					for(int i = 0; i < info.getMaxRowCount(); i++){
						info.setCurRow(i);
						if(Util.isNullOrEmpty(errCode)){
							info.setCurItem("SENDSTATUS", "201");
						}else{
							info.setCurItem("SENDSTATUS", "");
							info.setCurItem("ERRORCODE", errCode);
						}
					}
				}
			}
			if((info == null || info.getMaxRowCount() == 0)
					&& frm.teMainShomohin.getRowCount() > 0){
				info = daoTax.select(h2, frm.teMainShomohin.getValueInt(0, frm.colTenpoCdShomohin)
						, frm.teMainShomohin.getValueInt(0, frm.colDenpyoYearShomohin), frm.teMainShomohin.getValueInt(0, frm.colDenpyoKbShomohin)
						, frm.teMainShomohin.getValueInt(0, frm.colDenpyoNoShomohin));
				if(info != null && info.getMaxRowCount() > 0){
					for(int i = 0; i < info.getMaxRowCount(); i++){
						info.setCurRow(i);
						if(Util.isNullOrEmpty(errCode)){
							info.setCurItem("SENDSTATUS", "201");
						}else{
							info.setCurItem("SENDSTATUS", "");
							info.setCurItem("ERRORCODE", errCode);
						}
					}
				}
			}

			strErrTitle = "[免税設定マスタの更新]";
			if(info != null && info.getMaxRowCount() > 0) {
				renban = basedao.insertDbInfo(info, h2, 1, tranno, renban, 0, 1);
			}

			renban = basedao.insertTrnEnd(h2, tranno, renban);

			strErrTitle = "[リアルタイム更新処理]";
			basedao.execRealTimeSql();

			h2.commitTran();
			return false;
		}catch(Exception e){
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

	private boolean updateSosinCode(String errCode, int tenpocd, int denpyoNo, int denpyoYear, int denpyoKb) {
		BaseDAO basedao = new BaseDAO(appConfig);
		H2 h2 = new H2();
		String strErrTitle = "";
		try {
			if(!Util.isNullOrEmpty(errCode)) {
				if(errCode.startsWith("PHP")) {
					errCode = "PHP ERROR";
				}else if(errCode.length() > 20) {
					errCode = errCode.substring(0,20);
				}
			}
			// 更新処理
			h2.h2ClientStart(appConfig.getDatabaseXmlBean());
			h2.setAutoCommit(false);
			h2.startTran();

			//トランザクション開始
			int tranno = TalosTrn.getTrnNo(appConfig);
			int renban = 0;

			strErrTitle = "[免税設定マスタINFO作成]";
			TaxDataDAO daoTax = new TaxDataDAO(appConfig);
			DbInfo info = daoTax.select(h2, tenpocd
					, denpyoYear, denpyoKb
					, denpyoNo);
			if(info != null && info.getMaxRowCount() > 0){
				for(int i = 0; i < info.getMaxRowCount(); i++){
					info.setCurRow(i);
					if(Util.isNullOrEmpty(errCode)){
						info.setCurItem("SENDSTATUS", "201");
					}else{
						info.setCurItem("SENDSTATUS", "");
						info.setCurItem("ERRORCODE", errCode);
					}
				}
			}

			strErrTitle = "[免税設定マスタの更新]";
			renban = basedao.insertDbInfo(info, h2, 1, tranno, renban, 0, 1);

			renban = basedao.insertTrnEnd(h2, tranno, renban);

			strErrTitle = "[リアルタイム更新処理]";
			basedao.execRealTimeSql();

			h2.commitTran();
			return false;
		}catch(Exception e){
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

	//No.28084 2022/11/21 wk add start
	private boolean chkTitle(String value, int row, int intKb) {
        for (int i=0; i < value.length(); i++) {
    		if (!Util.checkETaxCode(value.charAt(i))) {
    			int intDakuten = i+1 < value.length() ? NexUtil.chkDakuten(String.valueOf(value.charAt(i+1))) : -1;
    			messageBox.disp(MB_EXCLAMATION, (intKb == 0 ? "一般物品" : "消耗品") + row + "行目　タイトルに使用不可の文字列が含まれています。\n"
    					+ "使用不可文字：" + value.charAt(i) + (intDakuten >= 0 ? value.charAt(i+1) : "")
    					+ "\n[F7 品名編集]からタイトルを編集して、登録をお願いします。", strMsgTitle);
    			return true;
            }
        }
		return false;
	}
	//No.28084 2022/11/21 wk add end

}
