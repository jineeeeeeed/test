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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;

import jp.co.css.base.AppConfig;
import jp.co.css.base.BaseCtrl;
import jp.co.css.bean.DbInfo;
import jp.co.css.dao.KuniMstDAO;
import jp.co.css.dao.TaxMessageMstDAO;
import jp.co.css.dao.TaxSetMstDAO;
import jp.co.css.dao.TorihikiDataDAO;
import jp.co.css.system.MainMenu;
import jp.co.css.talos_l.util.Constants;
import jp.co.css.webpos.common.db.H2;
import jp.co.css.webpos.common.except.TException;
import jp.co.css.webpos.common.gui.FKeyAdapter;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.message.MessageBoxValue;
import jp.co.css.webpos.common.pos.PrinterWriter;
import jp.co.css.webpos.common.util.SendTabKeys;
import jp.co.css.webpos.common.util.Util;

/*******************************************************************************
 * 処理名称 ：   免税書類発行画面      <br>
 * 作成日 　　： 	2019/01/23	  <br>
 * 作成者 　　：  	石暁彩			  <br>
 ******************************************************************************/
public class Ttax0100Ctrl extends BaseCtrl implements MessageBoxValue {

	private final String strMsgTitle = "免税書類発行画面";  	//ﾒｯｾｰｼﾞ用ﾀｲﾄﾙ
	private Ttax0100 frm = null;						//画面
	private String activeId = "Ttax0100Ctrl";
	private PrinterWriter writer;
	private PrintMenzeiShoruiBean bean = null;
	//ﾌｫｰｶｽの移動
	List<Component> compList; 							//ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを集めたﾃｰﾌﾞﾙ
	private SendTabKeys sendTab = new SendTabKeys(); 	//ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを取得するｸﾗｽ

	private boolean blnHeader = true;;
	//免税設定
	private DbInfo dbInfoTaxSet = null;
	//取引ﾃﾞｰﾀ
	private DbInfo dbInfoTd = null;

	//H2 共通
	private H2 comH2=null;

	private String path = appConfig.getSystemPath() + "/taxImage/";

	public Ttax0100Ctrl(AppConfig appConfig,String functionId, Object obj){
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
			frm = new Ttax0100(appConfig);
		}

		frm.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispClose();
			}
		});
		//初期設定
		init();
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
		}catch (TException e) {
			messageBox.disp(e, MB_CRITICAL, "免税設定マスタの取得でエラーが発生しました。" + e.getMessage(), strMsgTitle);
		}

		frm.txtDenpyoNo.addKeyListener(new HeaderKeyListener());
		frm.teMainNormal.addFocusListener(new TableFocusListener());
		frm.teMainShomohin.addFocusListener(new TableFocusListener());
		frm.txtPassport.addKeyListener(new PassportKeyListener());
		frm.btnToRight.setAction(new ActionClass());
		frm.btnToLeft.setAction(new ActionClass());

		frm.txtRyokenNo.addKeyListener(new TKeyAdapter());
		frm.txtRyokenType.addKeyListener(new TKeyAdapter());
		frm.txtKokuseiki.addKeyListener(new TKeyAdapter());
		frm.txtKonyuShaNm.addKeyListener(new TKeyAdapter());
		frm.dtBirthday.addKeyListener(new TKeyAdapter());
		frm.dtJorikuDate.addKeyListener(new TKeyAdapter());
		frm.txtZairyuShikaku.addKeyListener(new TKeyAdapter());
		frm.cboLanguage.addKeyListener(new TKeyAdapter());
		frm.chkChushaku.addKeyListener(new TKeyAdapter());
		frm.chkSeiyakuSho.addKeyListener(new TKeyAdapter());
		frm.chkKirokuHyo.addKeyListener(new TKeyAdapter());
		frm.chkKonpobuppinList.addKeyListener(new TKeyAdapter());

		TGuiUtil.addButtonFactionKey(frm.btnToRight, "RIGHT");
		TGuiUtil.addButtonFactionKey(frm.btnToLeft, "LEFT");
		frm.btnToRight.setText("→");
		frm.btnToLeft.setText("←");

		setCboLanguage();
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
		frm.fButton.setF1Text("");
		frm.fButton.setF2Text("");
		frm.fButton.setF3Text("");
		frm.fButton.setF4Text("");
		frm.fButton.setF5Text("");
		frm.fButton.setF6Text("");
		frm.fButton.setF7Text("");
		frm.fButton.setF8Text("");
		if(blnHeader) {
			frm.fButton.setF9Text("F9 終了");
		}else {
			frm.fButton.setF9Text("F9 戻る");
		}
		frm.fButton.setF10Text("");
		frm.fButton.setF11Text("");
		frm.fButton.setF12Text("F12 確定");
	}
	private void setFKeyEnabled() {
		frm.fButton.setFAllEnabled(false);
		frm.fButton.setF9Enabled(true);
		frm.fButton.setF12Enabled(!blnHeader);
		//ﾌｧﾝｸｼｮﾝｷｰ/ﾎﾞﾀﾝﾘｽﾅ追加
		frm.fButton.addFButtonListener(new ButtonListener());
		//ﾌｧﾝｸｼｮﾝｷｰ/ﾛｸﾞ処理追加
		frm.fButton.setLogOut(logOut);
	}
	private void setTextEnabled() {
		frm.txtDenpyoNo.setEnabled(blnHeader);
		frm.txtPassport.setEnabled(!blnHeader);
		frm.txtRyokenNo.setEnabled(!blnHeader);
		frm.txtRyokenType.setEnabled(!blnHeader);
		frm.txtKokuseiki.setEnabled(!blnHeader);
		frm.txtKonyuShaNm.setEnabled(!blnHeader);
		frm.dtBirthday.setEnabled(!blnHeader);
		frm.dtJorikuDate.setEnabled(!blnHeader);
		frm.txtZairyuShikaku.setEnabled(!blnHeader);
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
		}
		frm.txtPassport.setText("");
		frm.txtRyokenType.setText("");
		frm.txtRyokenNo.setText("");
		frm.txtKokuseiki.setText("");
		frm.txtKonyuShaNm.setText("");
		frm.dtBirthday.setText("");
		frm.dtJorikuDate.setText("");
		frm.cboLanguage.setSelectedIndex(0);
	}
	private  void setTaxSetValue() {
		try {
			if(dbInfoTaxSet.getMaxRowCount() > 0) {
				//在留資格
				frm.txtZairyuShikaku.setText(dbInfoTaxSet.getStringItem("STATUSOFRESIDENCE"));
				//記載事項言語
				frm.cboLanguage.setSelectedIndex(dbInfoTaxSet.getIntItem("LANGKB"));
				//注釈印字
				frm.chkChushaku.setSelected(dbInfoTaxSet.getIntItem("ANNOTATIONKB")==1?true:false);
				//印刷免税書類
				frm.chkKirokuHyo.setSelected(dbInfoTaxSet.getIntItem("RECORDFLG")==1?true:false);
				frm.chkSeiyakuSho.setSelected(dbInfoTaxSet.getIntItem("OATHFLG")==1?true:false);
				frm.chkKonpobuppinList.setSelected(dbInfoTaxSet.getIntItem("PACKINGLISTFLG")==1?true:false);
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
		//F9 終了
		public void f9Click(ActionEvent e) {
			if(!blnHeader){
				if( messageBox.disp(MB_INFORMATION, MB_YESNO,
						"現在処理中のデータは破棄されます。" + "\n" + "よろしいですか？", strMsgTitle) == MB_NO ){
					return;
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
		//F12 確定
		public void f12Click(ActionEvent e) {
			if(chkInputData()) {
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
			blnHeader = true;
			setFKeyEnabled();
			initText(1);
			setFKeyText();
			setTextEnabled();
			setTaxSetValue();
			clearTable();
			frm.txtDenpyoNo.requestFocus();
		}
	}
	//免税書類印刷
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

			lngMenzeigaku += frm.teMainShomohin.getValueInt(i, frm.colShohizeiNormal);

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
			bean.setStrRyokenType(frm.txtRyokenType.getText());
			//番号
			bean.setStrRyokenNo(frm.txtRyokenNo.getText());
			//国籍
			bean.setStrKokuseiki(frm.txtKokuseiki.getText());
			//上陸年月日
			bean.setStrJoriku(frm.dtJorikuDate.getText());
			//在留資格
			bean.setStrZairyuShikaku(frm.txtZairyuShikaku.getText());
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
					return;
				}
				blnHeader = false;
				setFKeyEnabled();
				setFKeyText();
				setTextEnabled();
				frm.txtPassport.requestFocus();
			}
		}
	}
	class TableFocusListener extends FocusAdapter {
		public void focusGained(java.awt.event.FocusEvent e) {
			// ﾌｧﾝｸｼｮﾝｷｰの制御
			if (frm.teMainNormal.getRowCount() <= 0) {
				frm.btnToRight.setEnabled(false);
			}else {
				frm.btnToRight.setEnabled(true);
			}
			if(frm.teMainShomohin.getRowCount() <=0) {
				frm.btnToLeft.setEnabled(false);
			}else {
				frm.btnToLeft.setEnabled(true);
			}
		}
		public void focusLost(java.awt.event.FocusEvent e) {
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
				frm.teMainShomohin.addRow();
				int intR = frm.teMainShomohin.getRowCount()-1;
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colTanpinCdNormal),intR , frm.colTanpinCdShomohin);//単品ｺｰﾄﾞ
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colRankNmNormal),  intR, frm.colRankNmShomohin);	 //ランク名
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colTanpinNmNormal), intR, frm.colTanpinNmShomohin);//タイトル
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colSuNormal), intR, frm.colSuShomohin);			 //数量
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colKinNormal), intR, frm.colKinShomohin);			 //金額
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colShohizeiNormal), intR, frm.colShohizeiShomohin);//消費税
				frm.teMainShomohin.setValueAt(frm.teMainNormal.getValueAt(intRow, frm.colHanbaiTankaNormal), intR, frm.colHanbaiTankaShomohin);//単価

				frm.teMainShomohin.addRowSelectionInterval(0,intR);
				TGuiUtil.setScrollBarPosition(frm.teMainShomohin, false, false);

				frm.teMainNormal.removeRow(intRow);

				if(frm.teMainNormal.getRowCount() >0 ) {
					if(intRow != frm.teMainNormal.getRowCount() || intRow == 0) {
						frm.teMainNormal.addRowSelectionInterval(intRow, intRow);
					} else {
						frm.teMainNormal.addRowSelectionInterval(intRow-1, intRow-1);
					}
				}
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
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colTanpinCdShomohin),intR , frm.colTanpinCdNormal);	//単品ｺｰﾄﾞ
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colRankNmShomohin),  intR, frm.colRankNmNormal);		//ランク名
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colTanpinNmShomohin), intR, frm.colTanpinNmNormal);	//タイトル
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colSuShomohin), intR, frm.colSuNormal);				//数量
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colKinShomohin), intR, frm.colKinNormal);				//金額
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colShohizeiShomohin), intR, frm.colShohizeiNormal);	//消費税
				frm.teMainNormal.setValueAt(frm.teMainShomohin.getValueAt(intRow, frm.colHanbaiTankaShomohin), intR, frm.colHanbaiTankaNormal);//単価
				frm.teMainNormal.addRowSelectionInterval(0,intR);
				TGuiUtil.setScrollBarPosition(frm.teMainNormal, false, false);

				frm.teMainShomohin.removeRow(intRow);

				if(frm.teMainShomohin.getRowCount() >0 ) {
					if(intRow != frm.teMainShomohin.getRowCount() || intRow == 0) {
						frm.teMainShomohin.addRowSelectionInterval(intRow, intRow);
					} else {
						frm.teMainShomohin.addRowSelectionInterval(intRow-1, intRow-1);
					}
				}

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
					frm.txtRyokenType.requestFocus();
					return;
				}
				blnHeader = false;
				setTextEnabled();
				//POCHNLAN<<JIANHUA<<<<<<<<<<<<<<<<<<<<<<<<<<<1424203969CHN3809134M011217819201100<<<<<<08
				String strPassport = frm.txtPassport.getText();
				if(strPassport.trim().length() != 88) {
					messageBox.disp(MB_EXCLAMATION, "パスポート以外の読み込みは対応していません。"+ "\n"+"直接入力してください。" , strMsgTitle);
					frm.txtRyokenType.requestFocus();
					return;
				}
				String strPassportGyo1 = strPassport.trim().substring(0, 44);
				String strPassportGyo2 = strPassport.trim().substring(44, 88);

				if(strPassportGyo1.substring(0, 1).equals("P")) {
					frm.txtRyokenType.setText("パスポート");
				}else {
					messageBox.disp(MB_EXCLAMATION, "パスポート以外の読み込みは対応していません。"+ "\n"+"直接入力してください。" , strMsgTitle);
					initText(0);
					frm.txtPassport.setText(strPassport);
					frm.txtRyokenType.requestFocus();
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
						frm.txtKokuseiki.setText(dbInfoKuni.getStringItem("KUNINM"));//国籍
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
//					if(Util.getYear(Util.getCurrentDate())-(Util.chgNumericInt(strBirthday.substring(0,2)+1900)) >100) {	
					if(Util.getYear(Util.getCurrentDate())-(Util.chgNumericInt(strBirthday.substring(0,2))+1900) >100) {	//2019/2/4 No.21531 zhangsiyuan add 
						strBirthday = "20"+strBirthday;
					}else {
						strBirthday = "19"+strBirthday;
					}
				} else {
					strBirthday = "";
				}
				frm.dtBirthday.setText(strBirthday);//生年月日
				frm.dtJorikuDate.setText("");//上陸年月日
				frm.txtRyokenType.requestFocus();
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
					frm.txtRyokenType.requestFocus();
				}
				break;
			}
		}
	}

	private boolean searchDenpyoNo(String strDenpyoNo) {
		TorihikiDataDAO torihikiDao = new TorihikiDataDAO(appConfig);
		try {
			dbInfoTd = torihikiDao.selectMenzeiDenpyo(comH2, frm.txtDenpyoNo.getTextInt());
			if(dbInfoTd.getMaxRowCount() == 0) {
				messageBox.disp(MB_INFORMATION, "指定の伝票は存在しません。\n伝票番号：" + frm.txtDenpyoNo.getText(), strMsgTitle);
				return true;
			}
			//免税フラグ
			if(dbInfoTd.getIntItem("MENZEIFLG") == 0) {
				messageBox.disp(MB_INFORMATION, "この伝票は免税伝票ではありません。", strMsgTitle);
				return true;
			}
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
			addTable();
			calcTotal();
		} catch (TException e) {
			messageBox.disp(e, MB_CRITICAL, "取引ﾃﾞｰﾀの取得でエラーが発生しました。"+ e.getMessage(), strMsgTitle);
		}
		return false;
	}
	private void addTable() {
		try {
			for (int i = 0; i < dbInfoTd.getMaxRowCount(); i++) {
				// ﾃｰﾌﾞﾙ1行追加
				frm.teMainNormal.addRow();
				dbInfoTd.setCurRow(i);
				//単品ｺｰﾄﾞ
				frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("TANPINCD"), i, frm.colTanpinCdNormal);
				//ランク名
				frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("KAKAKUKANRIRANKNM"), i, frm.colRankNmNormal);
				//タイトル
				frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("SHOHINNM"), i, frm.colTanpinNmNormal);
				//数量
				frm.teMainNormal.setValueAt(Util.formatNumber(dbInfoTd.getIntItem("SU")), i, frm.colSuNormal);
				//金額
				frm.teMainNormal.setValueAt(Util.formatNumber(dbInfoTd.getIntItem("SOTOZEITAISHOKIN")), i, frm.colKinNormal);
				//消費税
				frm.teMainNormal.setValueAt(Util.formatNumber(dbInfoTd.getIntItem("SOTOZEISHOHIZEIGAKU")), i, frm.colShohizeiNormal);
				//販売単価
				frm.teMainNormal.setValueAt(dbInfoTd.getStringItem("HANBAINYURYOKUTANKA"), i, frm.colHanbaiTankaNormal);

				frm.teMainNormal.setRowSelectionInterval(0, 0);
			}
			frm.jScrollPaneNormal.updateUI();
		}catch (Exception e) {
			messageBox.disp(e, MB_CRITICAL, "取引伝票データの表示中でエラーが発生しました。" + "\n" + e.getMessage(), strMsgTitle);
		}
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

	private void setCboLanguage() {
		//0:英語 1:中国語（簡体字） 2:中国語（繁体字） 3:日本語 4:タイ語 5:韓国語
		String strLanguage[][] =Constants.ARRAY_LANGUAGE_KB;
		for(int i= 0;i<strLanguage.length;i++) {
			frm.cboLanguage.addTextValueItem(strLanguage[i][0], strLanguage[i][1]);
		}
	}

	private boolean chkInputData() {
		if(Util.isNullOrEmpty(Util.trim(frm.txtRyokenType.getText()))) {
			messageBox.disp(MB_EXCLAMATION, "入力されていない項目があります。"+ "\n"+"入力してください。" , strMsgTitle);
			frm.txtRyokenType.requestFocus();
			return true;
		}
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
		if(Util.isNullOrEmpty(Util.trim(frm.txtZairyuShikaku.getText()))) {
			messageBox.disp(MB_EXCLAMATION, "入力されていない項目があります。"+ "\n"+"入力してください。" , strMsgTitle);
			frm.txtZairyuShikaku.requestFocus();
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
}
