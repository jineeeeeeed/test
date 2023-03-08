package jp.co.css.TTSA;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jp.co.css.COMMON.ClsGetPrice;
import jp.co.css.COMMON.CodeJudge;
import jp.co.css.COMMON.ExportCSV;
import jp.co.css.COMMON.KakuchoMenu;
import jp.co.css.COMMON.KakuchoMenuInfo;
import jp.co.css.COMMON.PriceInfo;
import jp.co.css.COMMON.SearchShohin;
import jp.co.css.base.AppConfig;
import jp.co.css.base.BaseCtrl;
import jp.co.css.bean.DbInfo;
import jp.co.css.bean.DbInfoValue;
import jp.co.css.communication.ActionService;
import jp.co.css.dao.BaseDAO;
import jp.co.css.dao.SabunMstDAO;
import jp.co.css.dao.TalosTrn;
import jp.co.css.dao.TenpoHanyoMstDAO;
import jp.co.css.dao.TenpoKihonMstDAO;
import jp.co.css.dao.TenpoZaikoDAO;
import jp.co.css.dao.TimeShareSetDAO;
import jp.co.css.dao.TimeShareShuupinListDAO;
import jp.co.css.talos_l.bean.TimeShareShuupinJokenBean;
import jp.co.css.talos_l.util.Constants;
import jp.co.css.webpos.common.db.H2;
import jp.co.css.webpos.common.except.TException;
import jp.co.css.webpos.common.gui.FKeyAdapter;
import jp.co.css.webpos.common.gui.TEditTable;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.gui.TextEnterListener;
import jp.co.css.webpos.common.gui.WaitBox;
import jp.co.css.webpos.common.message.MessageBoxValue;
import jp.co.css.webpos.common.util.Util;

/**---------------------------------------------
 * 処理名称	： TIME_SHARE出品登録画面
 * 作成日	： 2012/08/08
 * 作成者	： ZWH
---------------------------------------------**/
public class Ttsa0100Ctrl extends BaseCtrl implements MessageBoxValue {

	private final String strMsgTitle = "通販出品登録画面";		// ﾒｯｾｰｼﾞ用ﾀｲﾄﾙ
	private String strErrTitle = "";					//ｴﾗｰﾒｯｾｰｼﾞ用処理ﾀｲﾄﾙ
	private Ttsa0100 frm = null;						//画面
	private String activeId = "Ttsa0100Ctrl";
	WaitBox waitBox;

	private static String strFileName = "通販出品登録.csv";
	private static String strPath = "C:\\";

	private CodeJudge codeJudge = null; 					//ｺｰﾄﾞ判断fnc
	private SearchShohin searchShohin = null; 				//商品検索画面
	DbInfo sm = null;
	DbInfo tp = null;

	//ﾎﾟｯﾌﾟｱｯﾌﾟ関係
	private KakuchoMenu kakucho = null; 			//拡張ﾒﾆｭｰ画面

	//現在の入力位置
	private final int intInit 	= 0; 				//初期
	private final int intSearch	= 1; 				//検索
	private final int intJidoShoriGo = 2;			//自動処理後
	private int intInputMode = intInit; 			//ﾃﾞﾌｫﾙﾄは初期

	private final int intKobetuSearch = 0;					//個別検索
	private final int intJokenSearch  = 1;					//条件検索

	//店舗基本情報DAO
	private TenpoKihonMstDAO daoTenpoKihon = null;
	private DbInfo dbInfoTk = null;
	//店舗在庫データDAO
	private TenpoZaikoDAO daoTenpoZaiko = null;
	private DbInfo dbInfoZaiko = null;
	//TIME_SHARE出品リストDAO
	private TimeShareShuupinListDAO daoShuppinList = null;
	private DbInfo dbInfoSp = null;
	private TimeShareSetDAO daoTimeShareSet = null;

	// 価格などの取得用ｸﾗｽ
	private ClsGetPrice clsGetPrice = null;
	private PriceInfo priceInfo = null;
	private ListSelectionListener listSelectionListener = null;
	private TimeShareShuupinJokenBean beanJoken = null;

	// 注文取込店舗
	private String strChumonTenpo = "";

	public H2 comH2=null;	//共通
	private int intProcessRows = 0;

	private Map<Integer,String> mapShuppinJotai = new HashMap<Integer, String>();
	private Map<String, Integer> mapTanpin = new HashMap<String, Integer>();

	public Ttsa0100Ctrl( AppConfig appConfig, String functionId, Object obj) throws Exception{

		//初期設定
		super(appConfig,functionId,obj);

		if (Util.ActiveDisp(appConfig.getDispObjects(), activeId)) {
			return;
		}

		if (frm == null) {
			frm = new Ttsa0100(appConfig);
		}

		//No.13937 GS 2017/05/26
//		frm.addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent e) {
//				dispClose();
//			}
//		});

		if(comH2==null){
			comH2=new H2();
			try{
				comH2.h2ClientStart(appConfig.getDatabaseXmlBean());
			}catch(TException e){
				messageBox.disp(e, MB_CRITICAL, "H2 ｴﾗｰ"
						+ e.getMessage() + "\n", strMsgTitle);
				dispClose();
				return;

			}
		}

		// 価格などの取得用ｸﾗｽの初期化
		clsGetPrice = new ClsGetPrice(comH2,appConfig);
		daoShuppinList = new TimeShareShuupinListDAO(appConfig);
		daoTimeShareSet = new TimeShareSetDAO(appConfig);
		try{
			//店舗基本情報の取得
			strErrTitle = "店舗基本マスタ";
			daoTenpoKihon = new TenpoKihonMstDAO(appConfig);
			dbInfoTk = daoTenpoKihon.select(comH2);

			if( dbInfoTk.getMaxRowCount() == 0 ){
				throw new TException(-1, "店舗基本マスタにデータがありません。");
			}

			//ftpチェック
			strErrTitle = "通販設定マスタ";
			DbInfo info = daoTimeShareSet.select(getTenpoCd());
			if ( info.getMaxRowCount() == 0 ){
				messageBox.disp(MB_EXCLAMATION, "TIME_SHAREFTPサーバ情報が設定していません。", strMsgTitle);
				dispClose();
				return;
			}
			strChumonTenpo = info.getStringItem("CHUMOTORIKOMITENPO");

		}catch(TException ex){
			messageBox.disp(ex, MB_CRITICAL, strErrTitle + "の取得でエラーが発生しました。" + "\n"
					+ ex.getMessage() + "\n", strMsgTitle);
			dispClose();
			return;
		}

		// Fkey初期化
		frm.fButton.setFAllEnabled(false);
		frm.fButton.setF1Text("");
		frm.fButton.setF2Enabled(true);
		frm.fButton.setF2Text("F2 条件指定");
		frm.fButton.setF3Text("");
		frm.fButton.setF4Text("F4 拡張");
		frm.fButton.setF5Text("F5 商品検索");
		frm.fButton.setF6Text("");
		frm.fButton.setF7Text("F7 出品不可");
		frm.fButton.setF8Text("");
		frm.fButton.setF9Text("F9 終了");
		frm.fButton.setF9Enabled(true);
		frm.fButton.setF10Text("F10 CSV出力");
		frm.fButton.setF11Text("");
		frm.fButton.setF12Text("F12 実行");
		frm.fButton.setF12Enabled(true);

		//ﾌｧﾝｸｼｮﾝｷｰ/ﾎﾞﾀﾝﾘｽﾅ追加
		frm.fButton.addFButtonListener(new ButtonListener());
		//ﾌｧﾝｸｼｮﾝｷｰ/ﾛｸﾞ処理追加
		frm.fButton.setLogOut(logOut);

		frm.txtKey.addKeyListener(new HeaderKeyListener());
		frm.txtKey.addFocusListener(new HeaderFocusListener());

		listSelectionListener = new TableRowModelListener(frm.teMain);
		frm.teMain.addKeyListener(new TableKeyListener());
		frm.teMain.setTextEnterListener(new TTextEnterListener());
		frm.teMain.addFocusListener(new TableFocusListener());

		//ﾃｰﾌﾞﾙの初期化
		clearTable(0);
		//商品情報のｸﾘｱ
		clearShohinInfo();
		//ﾌｧﾝｸｼｮﾝｷｰの設定
		setFkeyEnabled();

		//出品状態汎用ﾃﾞｰﾀの取得
		getShuppinJotaiData();

		Date now = Util.getCurrentDate();
		if ( appConfig.getEigyoDate() != null ){
			now = appConfig.getEigyoDate();
		}
		beanJoken = new TimeShareShuupinJokenBean();
		beanJoken.setKaitoriFlg(true);
		beanJoken.setTaishoDateFrom(now);
		beanJoken.setTaishoDateTo(now);

		logOut.info("画面【" + frm.getTitle() + "】を開きました。");
		// 画面サイズ調整
		TGuiUtil.resizeWindow(frm);
		//画面の表示
		frm.setVisible(true);
		appConfig.addDisp(activeId, frm);
	}

	class TTextEnterListener implements TextEnterListener {

		public boolean enter(int row, int col, String value1){
			String value = value1;

			if ( Util.isNullOrEmpty(value)){
				return true;
			}

			//出品価格
			if ( col == frm.colHanbaiKakaku ){
				value = value.replace(",", "");
				if ( !Util.isNumber(value) ){
					return false;
				}
				int intPrice = Integer.parseInt(value);
				if (  intPrice < 1 ){
					return false;
				}
				//自動価格入力された販売価格を改変したということは、価格自動変更を利用しないとみなし改定除外する
				if ( Long.parseLong(value) == (frm.teMain.getValueLong(row, frm.colHanbaiKakakuBefore)) == false ){
					frm.teMain.setValueAt(true, row, frm.colKakakuKoteiFlg);
				}

			}

			//出品予定数
			if ( col == frm.colshuppinYoteiSu ){
				value = value.replace(",", "");
				if ( !Util.isNumber(value) ){
					return false;
				}
				int intYoteiSu = Integer.parseInt(value) + frm.teMain.getValueInt(row, frm.colGenzaiShuppinSu);
				if (  intYoteiSu < 0 || intYoteiSu > frm.teMain.getValueInt(row, frm.colZaikoSu)){
					return false;
				}
				//商品情報の表示
				showShohinInfo();
			}

			return true;
		}
	}

	// ﾃｰﾌﾞﾙ/ｷｰﾘｽﾅ
	class TableKeyListener extends KeyAdapter {
		// ｾﾙの移動前に呼ばれる
		public void keyPressed(java.awt.event.KeyEvent e) {
			// [↑]ｷｰでﾃｷｽﾄに移動。ﾃｷｽﾄがEnable=trueなら
			if (e.getKeyCode() == KeyEvent.VK_UP
					&& frm.txtKey.isEnabled() == true) {
				if (frm.teMain.getSelectedRow() == 0) {
					clearShohinInfo();
					frm.txtKey.requestFocus();
					//blnHeader = true;
				}
			}
		}
	}

	// ﾃｰﾌﾞﾙ/ﾌｫｰｶｽﾘｽﾅ
	class TableFocusListener extends FocusAdapter {
		public void focusGained(java.awt.event.FocusEvent e) {
			// ﾌｧﾝｸｼｮﾝｷｰの制御
			if (frm.teMain.getRowCount() <= 0) {
				return;
			}
			intInputMode = intSearch;
			setFkeyEnabled();
			//商品情報の表示
			showShohinInfo();
		}

		public void focusLost(java.awt.event.FocusEvent e) {

		}
	}

	//ﾒｲﾝﾃｰﾌﾞﾙ：行変更ﾘｽﾅ
	class TableRowModelListener  implements ListSelectionListener  {
		private TEditTable table;

		public TableRowModelListener(TEditTable table) {
		     this.table = table;
		}

		public void valueChanged(ListSelectionEvent e) {
		     boolean isAdjusting = table.getSelectionModel().getValueIsAdjusting();
		     if( !isAdjusting && table.isEnabled() ){
		    	 int intRow = frm.teMain.getSelectedRow();
					if ( intRow < 0 ) return ;

		    	//商品情報の表示
				showShohinInfo();
		     }else{
		    	 int row = frm.teMain.getSelectedRow();
				int column = frm.teMain.getSelectedColumn();
				if ( row >= 0 && column != frm.colHanbaiKakaku && column != frm.colshuppinYoteiSu){
					frm.teMain.addRowSelectionInterval(row,row);
					frm.teMain.moveToEditCell(row, frm.colHanbaiKakaku);
					showShohinInfo();
				}
		     }
		}
	}

	class HeaderFocusListener extends FocusAdapter{
		public void focusGained(FocusEvent e) {
			intInputMode = intInit;
			setFkeyEnabled();
		}

		public void focusLost(FocusEvent e) {

		}
	}

	/** ﾍｯﾀﾞｰ用ﾃｷｽﾄのｷｰﾘｽﾅ */
	class HeaderKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				//空、または書込み不可なら処理を抜ける
				if( frm.txtKey.getText().equals("") || frm.txtKey.isEditable() == false ){
					return;
				}

				kobetsuSearch(frm.txtKey.getText());
			}else if ( e.getKeyCode() == KeyEvent.VK_DOWN ){
				if ( frm.teMain.getRowCount() > 0 ){
					frm.teMain.requestFocus();
					showShohinInfo();
				}
			}
		}
	}

	private void kobetsuSearch(String strCd){
		try{
			//商品の検索
			int intRc = -1;
			intRc = SearchCode(frm.txtKey.getText());
			if( intRc < 0 ){
				return;
			}
			List<String> listShohinValue = new ArrayList<String>();
			listShohinValue.add(sm.getStringItem("SHOHINCD"));
			String tanpinCd = "";
			switch( intRc ){
			case 0://単品あり
				tanpinCd = tp.getStringItem("TANPINCD");
				break;
			case 1://商品あり

				break;
			}
			TimeShareShuupinJokenBean bean = new TimeShareShuupinJokenBean();
			bean.clear();
			bean.setTenpoCd(appConfig.getTenpoCd());
			bean.setShohinKey(0);//商品検索
			bean.setListShohinValue(listShohinValue);
			bean.setTanpinCd(tanpinCd);

			searchAndDisplay(intKobetuSearch, bean);

		}catch(Exception ex){
			messageBox.disp(ex, MB_CRITICAL, "商品情報の検索でエラーが発生しました。"+ "\n" +ex.getMessage() , strMsgTitle);
			frm.txtKey.requestFocus();
			return;
		}
	}

	/**
	 * 入力されたｺｰﾄﾞを判断。
	 */
	private int SearchCode(String strCd ){
		try {
			sm = null;
			tp = null;

			int intRc = 0;

			//ｺｰﾄﾞ判断fnc
			codeJudge = new CodeJudge(frm,appConfig);

			// 単品まで検索
			codeJudge.SetSearchFlg(codeJudge.srcTanpin);
			codeJudge.SetZaikoMode(codeJudge.intZaikoAll);

			// ｺｰﾄﾞ判断fncﾒｲﾝ
			intRc = codeJudge.CodeJudgeMain(comH2,strCd, null, null);

			switch (intRc) {
			case 0: // ﾃﾞｰﾀ無し
				messageBox.disp(MB_INFORMATION, "指定の商品は存在しません。", strMsgTitle);
				return -1;
			case 1: // 商品有り
				messageBox.disp(MB_INFORMATION, "在庫がありません。", strMsgTitle);
				return -1;
			case 2: // 商品・単品有り
				// 商品ﾏｽﾀのDbInfoの保存
				sm = codeJudge.GetShohinInfo();
				// 単品もあれば単品ﾏｽﾀのDbInfo保存
				tp = codeJudge.GetTanpinInfo();
				break;
			default:// ｴﾗｰ・ｷｬﾝｾﾙ
				return -1;
			}

			return 0;

		} catch (Exception e) {
			messageBox.disp(e, MB_CRITICAL, "コード判断処理でエラーが発生しました。[SearchCode]"
					+ "\n" , strMsgTitle);
			return -2;
		}
	}

	/**
	 * ｻｰﾊﾞから現在庫数、引当数、出品数、出品価格の取得
	 * @param sku　SKU
	 * @param tanpincd　単品ｺｰﾄﾞ
	 * @return
	 */
	private DbInfo getTenpoZaikoFromServer(String sku, String tanpincd){
		try{
			DbInfo info = daoShuppinList.selectZaiko(comH2, appConfig.getTenpoCd(), tanpincd, sku, 1);
			return info;
		}catch(TException ex){
			messageBox.disp(ex, MB_CRITICAL, "サーバから出品価格の取得でエラーが発生しました。\n" + ex.toString() + "\n" +
					ex.getMessage(),strMsgTitle);
			return null;
		}
	}

	private void torikomi(){
		waitBox = new WaitBox(frm);
		waitBox.setLblValueVisible(false);
		waitBox.setProgressBarVisible(false);
		waitBox.setTitle("処理中");
		waitBox.start();
		Thread thread = new Thread() {
			public void run(){
				if (appConfig.getHostFlag()==0 && appConfig.getAutoComm() == 1) {

					String tablename = "M_TIMESHARESHUPPINLIST";
					// データ受信処理
					ActionService acs = new ActionService(appConfig);
					// データを受信する
					// 引数：店舗コード、MYSQLテーブル名、H2DBテーブル名
					// 戻り値： -1：異常、0以上；レコード件数
					int res = acs.recieveData(String
							.valueOf(appConfig.getTenpoCd()), tablename, tablename);
					if (res < 0) {
						// 異常時(エラー情報はgetMessageで取得）
						messageBox.disp(MB_CRITICAL, "致命的なエラーが発生しました。"
								+ acs.getErrMessage(), "データ受信");
					}
				}
				waitBox.close();
			}
		};
		thread.start();
	}

	private void changeFocus(){
		if ( intInputMode != intInit && frm.teMain.getRowCount() > 0 ){
			frm.teMain.requestFocus();
		}else{
			frm.txtKey.requestFocus();
		}
	}

	/**
	 * ﾌｧﾝｸｼｮﾝｷｰの制御
	 *
	 * @author Administrator
	 */
	class ButtonListener extends FKeyAdapter
	{
		//F2 条件指定
		public void f2Click(ActionEvent e){

			torikomi();
			SearchShuupinJoken searchJoken = new SearchShuupinJoken(comH2,frm, appConfig);
			int ans = searchJoken.disp(beanJoken, SearchShuupinJoken.FRAMEID_TAMZ0100);

			if ( ans != MB_OK ) return;

			beanJoken = searchJoken.getJokenBean();
			beanJoken.setTenpoCd(appConfig.getTenpoCd());

			searchAndDisplay(intJokenSearch, beanJoken);

		}

		//F4 拡張
		public void f4Click(ActionEvent e){
			// メニューインフォ
			List<KakuchoMenuInfo> list = new ArrayList<KakuchoMenuInfo>();
			KakuchoMenuInfo menuInfo = new KakuchoMenuInfo();

			menuInfo = new KakuchoMenuInfo();
			menuInfo.setCaption("出品不可一括");
			menuInfo.setEnabeld(intInputMode == intSearch);
			menuInfo.setFuncNo(IKakuchoFuncNo.RETURN_SELECT_SHUPPINHUKA);
			list.add(menuInfo);

			menuInfo = new KakuchoMenuInfo();
			menuInfo.setCaption("出品可一括");
			menuInfo.setEnabeld(intInputMode == intSearch);
			menuInfo.setFuncNo(IKakuchoFuncNo.RETURN_SELECT_SHUPPINKA);
			list.add(menuInfo);

			menuInfo = new KakuchoMenuInfo();
			menuInfo.setCaption("POS売価一括変更");
			menuInfo.setEnabeld(intInputMode == intSearch);
			menuInfo.setFuncNo(IKakuchoFuncNo.RETURN_SELECT_POSBAIKAHENKOU);
			list.add(menuInfo);

			kakucho = new KakuchoMenu(frm, appConfig, 404);
			int ans = kakucho.disp(list);

			switch (ans) {
			case IKakuchoFuncNo.RETURN_SELECT_SHUPPINHUKA:
				ShuppinHukaAll();
				break;
			case IKakuchoFuncNo.RETURN_SELECT_SHUPPINKA:
				shuppinKaAll();
				break;
			case IKakuchoFuncNo.RETURN_SELECT_POSBAIKAHENKOU:
				POSprice();
				break;
			}
			changeFocus();
		}

		/**
		 * POS販売価格の取得
		 */
		private void POSprice(){

			try{
				for(int row=0; row<frm.teMain.getRowCount(); row++){
					priceInfo = null;
					priceInfo = clsGetPrice.GetPrice(comH2, frm.teMain.getValueString(row, frm.colShohinCd),frm.teMain.getValueInt(row, frm.colRankCd), frm.teMain.getValueString(row, frm.colTanpinCd));
					if ( priceInfo != null ){
						if (priceInfo.getHanbaiShow() == 0) {
							frm.teMain.setValueAt("", row, frm.colHanbaiKakaku);
						} else {
							frm.teMain.setValueAt(Util.getCurFormat(priceInfo.getHanbaiShow()), row, frm.colHanbaiKakaku);
						}
					}
				}
			}catch(Exception ex){
				messageBox.disp(ex, MB_CRITICAL, "POS販売価格の取得でエラーが発生しました。\n" + ex.toString() + "\n" +
						ex.getMessage(),strMsgTitle);
			}
		}

		public void f5Click(ActionEvent e){
			//商品ﾏｽﾀ情報
			try{
				searchShohin = new SearchShohin(comH2,appConfig);
			}catch( TException te ){
				messageBox.disp(te, MB_CRITICAL, "商品検索エラー:" + te.getMessage(), strMsgTitle);
				frm.txtKey.requestFocus();
				return;
			}
			int ans = searchShohin.disp();
			switch( ans ){
			case MB_YES:
				//商品ｺｰﾄﾞの表示
				frm.txtKey.setText(searchShohin.GetShohinCd());
				kobetsuSearch(frm.txtKey.getText());
				break;
			}
			if(searchShohin!=null){
				searchShohin.dispose();
				searchShohin=null;
			}

			changeFocus();
		}

		//F7
		public void f7Click(ActionEvent e){
			frm.teMain.stopCellEditing();
			int row = frm.teMain.getSelectedRow();
			if ( row < 0 ) return ;

			//出品可の場合
			if ( frm.teMain.getValueInt(row, frm.colShuppinKahi) == Constants.AMAZON_SHUPPINKAHI_KA ){
				//出品予定数は0で設定する
				frm.teMain.setValueAt(0, row, frm.colshuppinYoteiSu);
				//出品可否は出品可でｾｯﾄする
				frm.teMain.setValueAt(Constants.AMAZON_SHUPPINKAHI_FUKA, row, frm.colShuppinKahi);
			}else{//出品不可の場合
				//出品可否は出品可でｾｯﾄする
				frm.teMain.setValueAt(Constants.AMAZON_SHUPPINKAHI_KA, row, frm.colShuppinKahi);
				calcShuppinYoteiSu(row);
			}
			setF7KeyEnabled(row);
			concatColumnValue(row);
			changeFocus();
		}

		// F9 終了
		public void f9Click(ActionEvent e) {
			if ( frm.teMain.getRowCount() <= 0 ){
				dispClose();
			}else{
				mapTanpin.clear();
				frm.txtKey.setText("");
				//ﾃｰﾌﾞﾙ内容をｸﾘｱ
				clearTable(0);
				//商品情報の初期化
				clearShohinInfo();
				intInputMode = intInit;
				setFkeyEnabled();
			}
			changeFocus();
//			switch( intInputMode ){
//			case intInit:
//				dispClose();
//				break;
//			case intSearch:
//			case intJidoShoriGo:
//				//ﾃｰﾌﾞﾙ内容をｸﾘｱ
//				clearTable(0);
//				//商品情報の初期化
//				clearShohinInfo();
//				intInputMode = intInit;
//				setFkeyEnabled();
//				break;
//			}
		}

		//F10 CSV出力
		public void f10Click(ActionEvent e){
			List<List<String>> list = new ArrayList<List<String>>();
			List<String> listTitle = new ArrayList<String>();
			listTitle.add("SKU");
			listTitle.add("出品状態");
			listTitle.add("商品コード");
			listTitle.add("ランク");
			listTitle.add("単品コード");
			listTitle.add("タイトル");
			listTitle.add("大分類");
			listTitle.add("出品価格");
			listTitle.add("原価");
			listTitle.add("出品数");
			listTitle.add("出品済数");
			listTitle.add("在庫数");
			list.add(listTitle);

			for(int i=0; i<frm.teMain.getRowCount(); i++) {
				List<String> subList = new ArrayList<String>();

				subList.add(frm.teMain.getValueString( i, frm.colTanpinCd));
				subList.add(getShuppinJotaiNm(frm.teMain.getValueInt(i, frm.colShuppinJotai)));
				subList.add(frm.teMain.getValueString( i, frm.colShohinCd));
				subList.add(frm.teMain.getValueString( i, frm.colRankNm));
				subList.add(frm.teMain.getValueString( i, frm.colTanpinCd));
				subList.add(frm.teMain.getValueString( i, frm.colTitle));
				subList.add(frm.teMain.getValueString( i, frm.colDaibunrui));
				subList.add(frm.teMain.getValueString( i, frm.colHanbaiKakaku));
				subList.add(frm.teMain.getValueString( i, frm.colGenka));
				subList.add(frm.teMain.getValueString( i, frm.colshuppinYoteiSu));
				subList.add(frm.teMain.getValueString( i, frm.colGenzaiShuppinSu));
				subList.add(frm.teMain.getValueString( i, frm.colZaikoSu));

				list.add(subList);
			}

			ExportCSV exportCsv= new ExportCSV(appConfig,frm);
			exportCsv.show(strPath, strFileName, list);
			changeFocus();
		}

		// F12 更新
		public void f12Click(ActionEvent e) {
			frm.teMain.stopCellEditing();
			//更新ﾃﾞｰﾀのﾁｪｯｸ
			if ( chkInputData() ){
				return ;
			}
			try{
				Thread thread = new Thread() {
					public void run() {
						waitBox = new WaitBox(frm);
						waitBox.setLblValueVisible(false);
						waitBox.setProgressBarVisible(false);
						waitBox.setTitle("出品中");
						waitBox.start();

						synchronized (this) {
							try{
								//ﾃﾞｰﾀ更新
								if ( !updateMst() ){
									f9Click(null);
								}
							}catch(Exception ex){
								messageBox.disp(ex, MB_CRITICAL, "出品データの更新でエラーが起きました。" + "\n" + ex.getMessage(), strMsgTitle);
							}finally{
								waitBox.close();
							}
						}
					}
				};
				thread.start();
			}catch(Exception ex){
				messageBox.disp(ex, MB_CRITICAL, "出品データの更新でエラーが起きました。" + "\n" + ex.getMessage(), strMsgTitle);
			}
		}
	}

	/**
	 * F7ｷｰ表示/非表示の制御
	 * @param row
	 */
	private void setF7KeyEnabled(int row){
		if ( intInputMode == intJidoShoriGo || isShuppinKinsi(row)){
			frm.fButton.butF7.setEnabled(false);
			return ;
		}

		//出品可の行を選択した場合、「F7 出品不可」を表示する
		if ( frm.teMain.getValueInt(row, frm.colShuppinKahi) == Constants.AMAZON_SHUPPINKAHI_KA ){
			frm.fButton.setF7Text("F7 出品不可");
			frm.fButton.setF7Enabled(true);
		}
		//出品不可の行を選択した場合
		else{
			frm.fButton.setF7Text("F7 出品可");
			frm.fButton.setF7Enabled(true);
		}
	}

	private void ShuppinHukaAll(){
		frm.teMain.stopCellEditing();

		if(frm.teMain.getRowCount() < 1){
			return;
		}

		for(int row = 0; row < frm.teMain.getRowCount(); row++){
			if ( isShuppinKinsi(row)) {
				continue;
			}
			//出品可の場合
			if ( frm.teMain.getValueInt(row, frm.colShuppinKahi) == Constants.AMAZON_SHUPPINKAHI_KA){
				//出品予定数は0で設定する
				frm.teMain.setValueAt(0, row, frm.colshuppinYoteiSu);
				//出品可否は出品可でｾｯﾄする
				frm.teMain.setValueAt(Constants.AMAZON_SHUPPINKAHI_FUKA, row, frm.colShuppinKahi);
			}
			setF7KeyEnabled(row);
			concatColumnValue(row);
		}
	}

	private void shuppinKaAll(){
		frm.teMain.stopCellEditing();

		if(frm.teMain.getRowCount() < 1){
			return;
		}

		for(int row = 0; row < frm.teMain.getRowCount(); row++){
			if ( isShuppinKinsi(row)) {
				continue;
			}
			//出品可否は出品可でｾｯﾄする
			frm.teMain.setValueAt(Constants.AMAZON_SHUPPINKAHI_KA, row, frm.colShuppinKahi);
			calcShuppinYoteiSu(row);

			setF7KeyEnabled(row);
			concatColumnValue(row);
		}
	}

	/**
	 * 指定行はエラーが入力エラーがあるかをﾁｪｯｸする
	 * @param row
	 * @return
	 */
	private boolean chkInputData(int row, List<String> listError){

		if ( Util.isNullOrEmpty(frm.teMain.getValueString(row, frm.colHanbaiKakaku)) ){
			if ( listError != null ){
				listError.add((row+1) + "行目の出品価格を入力されていません。");
			}
			return true;
		}
		if ( frm.teMain.getValueInt(row, frm.colHanbaiKakaku) < 1 ){
			if ( listError != null ){
				listError.add((row+1) + "行目の出品価格は1以上を入力されていません。");
			}
			return true;
		}
		if ( frm.teMain.getValueInt(row, frm.colshuppinYoteiSu) + frm.teMain.getValueInt(row, frm.colGenzaiShuppinSu) >
			frm.teMain.getValueInt(row, frm.colZaikoSu) ){
			if ( listError != null ){
				listError.add((row+1) + "行目の出品数は在庫数を超えています。");
			}
			return true;
		}
		if ( frm.teMain.getValueInt(row, frm.colshuppinYoteiSu) + frm.teMain.getValueInt(row, frm.colGenzaiShuppinSu)  < 0 ){
			if ( listError != null ){
				listError.add((row+1) + "行目の出品数は0以上を入力されていません。");
			}
			return true;
		}

		return false;
	}

	/**
	 * 指定行を出品禁止するかどうかをﾁｪｯｸする
	 * @param row 行番号
	 * @return true:禁止する false:禁止しない
	 */
	private boolean isShuppinKinsi(int row){
		if ( frm.teMain.getValueInt(row, frm.colShuppinKinsiFlg) == 1 ){
			return true;
		}
		return false;
	}

	/**
	 * 入力チェック
	 * @return
	 */
	private boolean chkInputData() {

		List<String> listError = new ArrayList<String>();
		List<String> listWarning = new ArrayList<String>();
		//int intErrorRow = 0;
		for(int i=0; i<frm.teMain.getRowCount(); i++){
			//出品不可の場合、下記のﾁｪｯｸは不要とする
			if ( frm.teMain.getValueInt(i, frm.colShuppinKahi) == Constants.AMAZON_SHUPPINKAHI_FUKA ){
				continue;
			}
			if ( Util.isNullOrEmpty(frm.teMain.getValueString(i, frm.colTitle)) ){
				//frm.teMain.moveToEditCell(i, frm.colHanbaiKakaku);
				listError.add((i+1) + "行目のタイトルを入力してください。");
			}
			// 出品価格のチェックをやめる。ZWH 2012/10/15
//			if ( Util.isNullOrEmpty(frm.teMain.getValueString(i, frm.colHanbaiKakaku)) ){
//				//frm.teMain.moveToEditCell(i, frm.colHanbaiKakaku);
//				listError.add((i+1) + "行目の出品価格を入力してください。");
//			}
//			if ( frm.teMain.getValueInt(i, frm.colHanbaiKakaku) < 1 ){
//				listError.add((i+1) + "行目の出品価格は1以上を入力してください。");
//			}
			if ( frm.teMain.getValueInt(i, frm.colshuppinYoteiSu) + frm.teMain.getValueInt(i, frm.colGenzaiShuppinSu) >
				frm.teMain.getValueInt(i, frm.colZaikoSu) ){
				listError.add((i+1) + "行目の出品数は在庫数を超えています。");
			}
			if ( frm.teMain.getValueInt(i, frm.colshuppinYoteiSu) + frm.teMain.getValueInt(i, frm.colGenzaiShuppinSu)  < 0 ){
				listError.add((i+1) + "行目の出品数は0以上を入力してください。");
			}
			if ( frm.teMain.getValueInt(i, frm.colshuppinYoteiSu) > 1000 ){
				listWarning.add((i+1) + "行目の出品予定数を1000を超えています。");
			}
		}
		String errorMessage = "";
		for(int i=0; i<listError.size(); i++){
			errorMessage += Util.isNullOrEmpty(errorMessage) ? listError.get(i): "\n"+ listError.get(i);
		}
		//ｴﾗｰあり
		if ( !Util.isNullOrEmpty(errorMessage) ){
			messageBox.disp(MB_EXCLAMATION, errorMessage, strMsgTitle);
			return true;
		}
		String warningMessage = "";
		for(int i=0; i<listWarning.size(); i++){
			warningMessage += Util.isNullOrEmpty(warningMessage) ? listWarning.get(i): "\n"+ listWarning.get(i);
		}
		if ( !Util.isNullOrEmpty(warningMessage) ){
			if( messageBox.disp(MB_QUESTION, MB_YESNO, warningMessage + "\n" + "登録します。よろしいですか？", strMsgTitle) == MB_NO ){
				return true;
			}
		}

		return false;
	}
	/**
	 * 出品ﾃﾞｰﾀを検索し、画面上に表示する
	 */
	private void searchAndDisplay(final int searchMode, final TimeShareShuupinJokenBean beanJoken){

		try{
			waitBox = new WaitBox(frm);
			waitBox.setLblValueVisible(false);
			waitBox.setProgressBarVisible(false);
			waitBox.setTitle("処理中");
			waitBox.start();

			Thread thread = new Thread() {
				public void run() {
					synchronized (this) {
						try{
							frm.teMain.setEnabled(false);
							frm.teMain.getSelectionModel().removeListSelectionListener(listSelectionListener);
							if ( searchMode == intJokenSearch ){//条件検索
								mapTanpin.clear();
								//ﾃｰﾌﾞﾙ内容をｸﾘｱ
								clearTable(0);
							}

							DbInfo info = null;
							if ( searchMode == intJokenSearch ){//個別検索の場合は件数をチェックしない
								//件数取得
								info = daoShuppinList.select(comH2, beanJoken, 0, 1);
								//ﾃﾞｰﾀ無し
								int intSu = info.getIntItem("SU");
								if ( intSu ==0 ){
									waitBox.close();
									messageBox.disp(MB_INFORMATION, "該当データがありません。", strMsgTitle);
									return ;
								}
								//件数は３０００件を超える、確認ﾒｯｾｰｼﾞを出す
								if ( beanJoken.getShuppinMode() == TimeShareShuupinJokenBean.SHUPPIN_MODE_SHUDOU &&
										intSu > 3000 ) {
									if ( messageBox.disp(MB_INFORMATION, MB_YESNO,"対象件数("+intSu+")は3000件以上です。\n表示してもよろしいですか？。", strMsgTitle) == MB_NO){
										waitBox.close();
										return;
									}
								}
							}
							//ﾃﾞｰﾀの取得
							info = daoShuppinList.select(comH2, beanJoken, 0, 0);

							waitBox.setLblValueVisible(true);
							waitBox.setProgressBarVisible(true);
							waitBox.setCancelButVisible(true);
							waitBox.setMax(info.getMaxRowCount());
							//ﾃｰﾌﾞﾙにﾃﾞｰﾀをｾｯﾄ
							addToTable(searchMode, info, beanJoken);

							//現在の入力位置:検索
//							if ( frm.teMain.getRowCount() > 0 ){
//								intInputMode = intSearch;
//								setFkeyEnabled();
//							}
							if ( searchMode == intJokenSearch ){//条件検索
								//一番上にﾌｫｰｶｽをあてる
								if (frm.teMain.getRowCount() > 0){
									frm.teMain.addRowSelectionInterval(0,0);
									frm.teMain.moveToEditCell(0, frm.colHanbaiKakaku);
									showShohinInfo();
								}

								frm.teMain.requestFocus();
							}else if ( frm.txtKey.isEditable() ){//個別検索
								frm.txtKey.requestFocus();
								showShohinInfo();
							}
							//waitBox.close();
//							frm.teMain.setEnabled(true);
//							frm.teMain.requestFocus();

							//自動出品
							if ( beanJoken.getShuppinMode() == TimeShareShuupinJokenBean.SHUPPIN_MODE_JIDOU ){
								waitBox.setTitle("出品中");
								//ﾃﾞｰﾀ更新
								if ( !updateMst() ){
									//ｴﾗｰがある行の情報を表示
									List<String> listError = new ArrayList<String>();
									listError.add(frm.teMain.getRowCount()+"行のうち、"+intProcessRows+"行を正常に出品されました。");
									listError.add("");
									for(int row=0; row<frm.teMain.getRowCount(); row++){
										chkInputData(row, listError);
									}
									String errorMessage = "";
									for(int i=0; i<listError.size(); i++){
										errorMessage += Util.isNullOrEmpty(errorMessage) ? listError.get(i): "\n"+ listError.get(i);
									}
									messageBox.disp(MB_INFORMATION, errorMessage, strMsgTitle);
									intInputMode = intJidoShoriGo;
									setFkeyEnabled();
								}
							}
							waitBox.close();
						}catch(Exception ex){
							waitBox.close();
							frm.jScrollPane.updateUI();
							messageBox.disp(ex, MB_CRITICAL, "出品データの検索でエラーが起きました。" + "\n" + ex.getMessage(), strMsgTitle);
						//No.18000 2018/10/10 CF add start
						}catch(OutOfMemoryError te){
							System.gc();
							waitBox.close();
							messageBox.disp(te, MB_CRITICAL, "対象データが大量なためメモリー不足になりました。\r\n条件を設定して対象データ量を少なくしてください。(Exception)" + "\n"
									+ te.toString() + te.getMessage(), strMsgTitle);
						//No.18000 2018/10/10 CF add end
						}finally{
							frm.teMain.setEnabled(true);
							frm.teMain.getSelectionModel().addListSelectionListener(listSelectionListener);
						}
					}
				}
			};
			thread.start();
		}catch(Exception ex){
			messageBox.disp(ex, MB_CRITICAL, "出品データの検索でエラーが起きました。" + "\n" + ex.getMessage(), strMsgTitle);
		}
	}

	private int getTenpoCd(){
		return 9999;
	}

	/*
	 * @return
	 */
	private boolean updateMst( ){
		H2 h2 = new H2();
		try{
			h2.h2ClientStart(appConfig.getDatabaseXmlBean());
			h2.setAutoCommit(false);
			h2.startTran();

			BaseDAO basedao = new BaseDAO(appConfig);

			int tranno=0;
			int renban=0;
			//トランザクション開始
			tranno=TalosTrn.getTrnNo(appConfig);
			renban=0;

			strErrTitle = "[TIME_SHARE出品ﾃﾞｰﾀINFOの作成]";
			dbInfoSp = daoShuppinList.get(h2);

			//店舗在庫データDAO
			strErrTitle = "[店舗在庫データINFOの作成]";
			daoTenpoZaiko = new TenpoZaikoDAO(appConfig);
			dbInfoZaiko = daoTenpoZaiko.get(h2);

			strErrTitle = "[店舗基本マスタ取得]";
			TenpoKihonMstDAO tenpoKihonMstDAO = new TenpoKihonMstDAO(appConfig);
			DbInfo tkall = tenpoKihonMstDAO.select(strChumonTenpo, 1);
			// 差分データ
			SabunMstDAO sabundao = new SabunMstDAO(appConfig);
			DbInfo sabunInfo = sabundao.get();

			String strSku = "";
			intProcessRows = 0;
			waitBox.setLblValueVisible(true);
			waitBox.setProgressBarVisible(true);
			waitBox.setMax(frm.teMain.getRowCount());
			for(int i=0; i<frm.teMain.getRowCount(); i++){
				waitBox.setValue(i+1);
				//自動出品かつ入力エラーありの場合、ｽｷｯﾌﾟする
				if ( beanJoken.getShuppinMode() == TimeShareShuupinJokenBean.SHUPPIN_MODE_JIDOU &&
						chkInputData(i, null) ){
					continue;
				}

				strErrTitle = "[TIME_SHARE出品ﾃﾞｰﾀの作成]";
				//SKU
				strSku = frm.teMain.getValueString( i, frm.colTanpinCd);

				int index = -1;
				for(int j=0; j<dbInfoSp.getMaxRowCount(); j++){
					if ( strSku.equals(dbInfoSp.getStringItem("SKU", j)) ){
						index = j;
						break;
					}
				}
				if ( index == -1 ){
					DbInfo info = daoShuppinList.select(h2, getTenpoCd(), strSku);
					if ( info.getMaxRowCount() > 0 ){
						dbInfoSp.setRow(info.getRow());
						index = dbInfoSp.getMaxRowCount()-1;
					}
				}

				if ( index > -1 ){//変更
					dbInfoSp.setCurRow(index);
					dbInfoSp.setCurItem("KOSINFLG", 2);														//更新フラグ:更新
				}else{//新規
					DbInfoValue tkv = new DbInfoValue(dbInfoSp.getFieldCount());
					dbInfoSp.setRow(tkv);
					dbInfoSp.setCurRow(dbInfoSp.getMaxRowCount()-1);

					dbInfoSp.setCurItem("TENPOCD", getTenpoCd());											//店舗コード
					dbInfoSp.setCurItem("SKU", strSku);														//SKU
					dbInfoSp.setCurItem("SHUPPINDATE", Util.getCurrentDateYMDSql());						//出品日付
					dbInfoSp.setCurItem("KOSINFLG", 1);														//更新フラグ:新規
				}

				int shuppinKahi = frm.teMain.getValueInt(i, frm.colShuppinKahi);
				int shuppinJotai = frm.teMain.getValueInt(i, frm.colShuppinJotai);
				if ( shuppinKahi == Constants.AMAZON_SHUPPINKAHI_FUKA ){
					if ( shuppinJotai == Constants.AMAZON_JOTAIKB_MITOROKU ||
							shuppinJotai == Constants.AMAZON_JOTAIKB_MISHUPPIN ){
						dbInfoSp.setCurItem("SHUPPINZUMIFLG", Constants.AMAZON_JOTAIKB_MISHUPPIN);			//出品済みフラグ:未出品
					}else{
						dbInfoSp.setCurItem("SHUPPINZUMIFLG", Constants.AMAZON_JOTAIKB_TORIKESIZUMI);		//出品済みフラグ:取消済
					}
				}else{
					dbInfoSp.setCurItem("SHUPPINZUMIFLG", Constants.AMAZON_JOTAIKB_SHUPPINZUMI);			//出品済みフラグ:出品済
				}
				dbInfoSp.setCurItem("SHUPPINKAFLG", frm.teMain.getValueInt(i, frm.colShuppinKahi));			//出品可ﾌﾗｸﾞ
				dbInfoSp.setCurItem("SHOHINNM", frm.teMain.getValueString(i, frm.colTitle));				//ﾀｲﾄﾙ
				dbInfoSp.setCurItem("SHOHINCD", frm.teMain.getValueString(i, frm.colShohinCd));				//商品ｺｰﾄﾞ
				dbInfoSp.setCurItem("KAKAKUKANRIRANK", frm.teMain.getValueString(i, frm.colRankCd));		//価格管理ﾗﾝｸ

				dbInfoSp.setCurItem("TANPINCD", frm.teMain.getValueString(i, frm.colTanpinCd));				//単品ｺｰﾄﾞ

				dbInfoSp.setCurItem("DAIBUNRUICD", frm.teMain.getValueString(i, frm.colDaibunCd));			//大分類ｺｰﾄﾞ
				dbInfoSp.setCurItem("DAIBUNRUINM", frm.teMain.getValueString(i, frm.colDaibunNm));			//大分類名称
				dbInfoSp.setCurItem("CHUBUNRUICD", frm.teMain.getValueString(i, frm.colChubunCd));			//中分類ｺｰﾄﾞ
				dbInfoSp.setCurItem("CHUBUNRUINM", frm.teMain.getValueString(i, frm.colChubunNm));			//中分類名称
				dbInfoSp.setCurItem("SHOBUNRUICD", frm.teMain.getValueString(i, frm.colShobunCd));			//小分類ｺｰﾄﾞ
				dbInfoSp.setCurItem("SHOBUNRUINM", frm.teMain.getValueString(i, frm.colShobunNm));			//小分類名称
				dbInfoSp.setCurItem("JANCD", frm.teMain.getValueString(i, frm.colJanCd));					//JANコード
				dbInfoSp.setCurItem("HATUBAIDATE", frm.teMain.getValueString(i, frm.colHatubaiDate));		//発売日
				dbInfoSp.setCurItem("KIKAKUNO", frm.teMain.getValueString(i, frm.colKikakuNo));				//品番
				dbInfoSp.setCurItem("TEIKA", frm.teMain.getValueString(i, frm.colTeika).replace(",", ""));	//定価
				dbInfoSp.setCurItem("MAKERCD", frm.teMain.getValueString(i, frm.colMakerCd));				//メーカーｺｰﾄﾞ
				if (!Util.isNullOrEmpty(frm.teMain.getValueString(i, frm.colMakerNm))) {
					dbInfoSp.setCurItem("MAKERNM", Util.left(frm.teMain.getValueString(i, frm.colMakerNm), 32));//メーカー名称
				} else {
					dbInfoSp.setCurItem("MAKERNM", frm.teMain.getValueString(i, frm.colMakerNm));			//メーカー名称
				}
				dbInfoSp.setCurItem("SEIZOUNO", frm.teMain.getValueString(i, frm.colSeizouNo));				//製造番号

				dbInfoSp.setCurItem("GENKA", frm.teMain.getValueInt(i, frm.colGenka));						//原価
				if ( !Util.isNullOrEmpty(frm.teMain.getValueString(i, frm.colHanbaiKakaku)) ){
					dbInfoSp.setCurItem("SHUPINKAKAKU", frm.teMain.getValueInt(i, frm.colHanbaiKakaku));	//販売価格
				}else{
					dbInfoSp.setCurItem("SHUPINKAKAKU", "0");												//販売価格
				}
				dbInfoSp.setCurItem("SOSINZUMIFLG", 0);														//送信済フラグ:未送信

				dbInfoSp.setCurItem("SAISHUKOSINTANTOCD", appConfig.getTantoushaCd());						//最終更新担当者コード
				dbInfoSp.setCurItem("SAISHUKOSINDATETIME", Util.getCurrentDateSql());						//最終更新日付
				dbInfoSp.setCurItem("HOSTSOSINFLG", 1);														//HOST送信フラグ

				//本部出品ならば差分データを作成
				if (tkall.getMaxRowCount() != 0) {
					for (int j=0;j<tkall.getMaxRowCount();j++) {
						tkall.setCurRow(j);
						DbInfoValue sabun = new DbInfoValue(sabundao.getFieldCount());
						if(tkall.getIntItem("TENPOCD") == appConfig.getTenpoCd()){
							continue;
						}
						sabunInfo.setRow(sabun);
						sabunInfo.setCurRow(sabunInfo.getMaxRowCount()-1);
						sabunInfo.setCurItem("TENPOCD", tkall.getIntItem("TENPOCD"));			// 店舗コード
						sabunInfo.setCurItem("MSTNAME", "M_TIMESHARESHUPPINLIST");				// マスタ名称
						sabunInfo.setCurItem("WHEREDATA", "TENPOCD = 9999 " + "AND SKU = '" + strSku +"'");
						sabunInfo.setCurItem("SAKUSEIDATETIME", Util.getCurrentDateSql());	// 作成日時
						sabunInfo.setCurItem("HOSTSOSINFLG", "1"); 							// HOST送信フラグ
					}
				}

				//店舗在庫ﾃﾞｰﾀの更新
				strErrTitle = "[店舗在庫データの検索]";
				DbInfo info = daoTenpoZaiko.selectHost(h2, appConfig.getTenpoCd(), frm.teMain.getValueString(i, frm.colTanpinCd));
				if ( info.getMaxRowCount() > 0 ){
					dbInfoZaiko.setRow(info.getRow());
					dbInfoZaiko.setCurRow(dbInfoZaiko.getMaxRowCount()-1);
					//出品数
					//出品数
					int shuppinSu = 0;
					if ( dbInfoSp.getIntItem("SHUPPINKAFLG") == Constants.AMAZON_SHUPPINKAHI_KA ){
						shuppinSu = frm.teMain.getValueInt(i, frm.colGenzaiShuppinSu)+ frm.teMain.getValueInt(i, frm.colshuppinYoteiSu);

						int hikiateZaiko = dbInfoZaiko.getIntItem("HIKIATEZAIKO") > 0 ? dbInfoZaiko.getIntItem("HIKIATEZAIKO"):0;
						if ( dbInfoZaiko.getIntItem("GENZAIKOSU") - hikiateZaiko < shuppinSu){
							shuppinSu = dbInfoZaiko.getIntItem("GENZAIKOSU") - hikiateZaiko;
						}
						if ( shuppinSu < 0 ) shuppinSu = 0;
					}
					dbInfoZaiko.setCurItem("TIMESHARESHUPPINSU", shuppinSu);								//通販出品数
					dbInfoZaiko.setCurItem("KOSINFLG", 2);												//更新フラグ:更新
					dbInfoZaiko.setCurItem("SAISHUKOSINTANTOCD", appConfig.getTantoushaCd());				//最終更新担当者コード
					dbInfoZaiko.setCurItem("SAISHUKOSINDATETIME", Util.getCurrentDateSql());				//最終更新日付

					//ｻｰﾊﾞ側の出品数の更新
					strErrTitle = "[TIME_SHARE出品数の更新]";
					String strSQL = daoTenpoZaiko.getUpdateTimeShareShuppinSuSql(h2, appConfig.getTenpoCd(),
							frm.teMain.getValueString(i, frm.colTanpinCd),
							frm.teMain.getValueString(i, frm.colShohinCd),
							frm.teMain.getValueInt(i, frm.colRankCd),
							shuppinSu);
					// リアルタイムからD_TRANに変更するように ZWH 2016/06/29
//					renban = basedao.addRealTimeSql(h2, tranno, renban, strSQL);
					renban = basedao.addRealTimeSql(h2, tranno, renban, strSQL, Constants.REALTIME_TRAN);
				}

				intProcessRows++;
			}

			//本部出品ならば差分を送信
			//2017/03/24 YANGCHAO 案件NO:13567 start
//			renban = basedao.insertDbInfo(sabunInfo, h2, 1, tranno, renban, 1, 1);
			renban = basedao.insertDbInfo(sabunInfo, h2, 1, tranno, renban, 1, 0);
			//2017/03/24 YANGCHAO 案件NO:13567 end

			strErrTitle = "[TIME_SHARE出品ﾃﾞｰﾀのUPDATE]";
			renban = basedao.insertDbInfo(dbInfoSp, h2, 1, tranno, renban);

			strErrTitle = "[店舗在庫ﾃﾞｰﾀのUPDATE]";
			basedao.insert(dbInfoZaiko, h2);

			renban = basedao.insertTrnEnd(h2, tranno, renban);

			strErrTitle = "[リアルタイム更新処理]";
			basedao.execRealTimeSql();

			h2.commitTran();

			return false;
		}catch(Exception e){
			//ﾛｰﾙﾊﾞｯｸ
			try{
				h2.rollBackTran();

			}catch( Exception ex ){
				messageBox.disp(ex, MB_CRITICAL, strErrTitle+"ロールバックでエラーが起きました。" + "\n" + e.getMessage(), strMsgTitle);
			}
			messageBox.disp(e, MB_CRITICAL, strErrTitle+"処理でエラーが起きました。" + "\n" + e.getMessage(), strMsgTitle);
			return true;
		}finally{
			try{
				h2.h2ClientStop();
			}catch(TException te){}
		}
	}

	/**
	 * ﾃｰﾌﾞﾙの初期化
	 */
	private void clearTable(int intRowCnt){
		//frm.teMain.getSelectionModel().removeListSelectionListener(listSelectionListener);
		//ﾒｲﾝﾃｰﾌﾞﾙ
		//行ｸﾘｱ
		frm.teMain.setRowCount(0);

		//0以上なら行追加
		if ( intRowCnt > 0 ){
			frm.teMain.addRows(intRowCnt);
		}
		frm.jScrollPane.updateUI();

	}

	private void showSelectedRow(final int row){
		if ( row < frm.teMain.getRowCount() ){
			try{
				SwingUtilities.invokeAndWait(new Runnable(){
					public void run() {
						frm.teMain.addRowSelectionInterval(row,row);
						frm.teMain.moveToEditCell(row, frm.colHanbaiKakaku);
						Rectangle rect = frm.teMain.getCellRect(row, 0, true);
						frm.teMain.scrollRectToVisible(rect);
						frm.jScrollPane.updateUI();
						showShohinInfo();
					}
				});
			}catch(Exception ex){}
		}
	}

	private void addToTable(int searchMode, DbInfo info, TimeShareShuupinJokenBean beanJoken) throws Exception{
//		clearTable(0);
		int intCurrentRow = -1;
		for(int row=0; row<info.getMaxRowCount(); row++){
			waitBox.setValue(row+1);

			if ( waitBox.getCancelFlg() ){
				break ;
			}
			info.setCurRow(row);

			//既に存在するかをチェックする
			String key = info.getStringItem("SHOHINCD")+";"+info.getStringItem("TANPINCD");
			if ( searchMode == intKobetuSearch && mapTanpin.containsKey(key) ){
				showSelectedRow(mapTanpin.get(key));
				continue;
			}

			//ｻｰﾊﾞから店舗在庫ﾃﾞｰﾀの取得
			String sku = info.getStringItem("TANPINCD");
			DbInfo dbInfoZaiko = getTenpoZaikoFromServer(sku, info.getStringItem("TANPINCD"));
			//ｻｰﾊﾞ側で在庫情報が存在しない場合、画面に表示しない
			if ( dbInfoZaiko == null || dbInfoZaiko.getMaxRowCount() == 0 ){
				continue;
			}

			//在庫数
			long zaikosu = 0;
			zaikosu = dbInfoZaiko.getIntItem("GENZAIKOSU");

			long hikiatesu = dbInfoZaiko.getIntItem("HIKIATEZAIKO");
			if ( hikiatesu < 0 ) hikiatesu = 0;
			zaikosu -= hikiatesu;
			if ( zaikosu < 0 ) zaikosu = 0;
			//現在出品数
			long genzaiShuppinsu = dbInfoZaiko.getIntItem("TIMESHARESHUPPINSU");
			//出品予定数
			long shuppinYoteisu  = zaikosu - genzaiShuppinsu;

			//未出品のみ表示
			if ( beanJoken.isMiShuppinNomiFlg() && genzaiShuppinsu >= zaikosu ){
				continue;
			}

			//未出品且つ在庫数=0の場合
			if (Util.isNullOrEmpty(info.getStringItem("SKU")) && zaikosu == 0) {
				continue;
			}
			//出品数条件(FROM)
			if (beanJoken.getShuppinSuFrom() != null){
				if(genzaiShuppinsu < beanJoken.getShuppinSuFrom()){
					continue;
				}
			}
			//出品数条件(TO)
			if (beanJoken.getShuppinSuTo() != null){
				if(genzaiShuppinsu > beanJoken.getShuppinSuTo()){
					continue;
				}
			}

			frm.teMain.addRow();
			intCurrentRow = frm.teMain.getRowCount()-1;

			frm.teMain.setValueAt(info.getIntItem("SHUPPINKINSIFLG"), intCurrentRow, frm.colShuppinKinsiFlg);	//出品禁止ﾌﾗｸﾞ
			frm.teMain.setValueAt(info.getIntItem("SHUPPINJOTAI"), intCurrentRow, frm.colShuppinJotai);			//出品状態
			frm.teMain.setValueAt(info.getStringItem("SHOHINCD"), intCurrentRow, frm.colShohinCd);				//商品ｺｰﾄﾞ
			frm.teMain.setValueAt(info.getStringItem("TANPINCD"), intCurrentRow, frm.colTanpinCd);				//単品ｺｰﾄﾞ
			frm.teMain.setValueAt(info.getStringItem("KAKAKUKANRIRANK"), intCurrentRow, frm.colRankCd);			//価格管理ﾗﾝｸ
			frm.teMain.setValueAt(info.getStringItem("KAKAKUKANRIRANKNM"), intCurrentRow, frm.colRankNm);		//ランク名称
			frm.teMain.setValueAt(info.getIntItem("SHUPPINKAFLG"), intCurrentRow, frm.colShuppinKahi);			//出品可否
			frm.teMain.setValueAt(info.getStringItem("SEIZOUNO"), intCurrentRow, frm.colSeizouNo);				//製造番号

			//未出品の場合
			if ( Util.isNullOrEmpty(info.getStringItem("SKU")) ){
				//初期化
				if (Util.isNullOrEmpty(info.getStringItem("TP_SHOHINNM"))) {
					frm.teMain.setValueAt(trunc(info.getStringItem("SHOHINNM"),30), intCurrentRow, frm.colTitle);	//ﾀｲﾄﾙ
				} else {
					frm.teMain.setValueAt(trunc(info.getStringItem("TP_SHOHINNM"),30), intCurrentRow, frm.colTitle);//単品詳細の商品名称
				}
			}
			//出品済の場合
			else{
				frm.teMain.setValueAt(info.getStringItem("TITLE"), intCurrentRow, frm.colTitle);					//ﾀｲﾄﾙ
			}

			frm.teMain.setValueAt(info.getStringItem("DAIBUNRUIRYAKUNM"), intCurrentRow, frm.colDaibunrui);		//商品大分類
			frm.teMain.setValueAt(info.getStringItem("JANCD"), intCurrentRow, frm.colJanCd);					//JANｺｰﾄﾞ
			frm.teMain.setValueAt(info.getStringItem("DAIBUNRUICD"), intCurrentRow, frm.colDaibunCd);			//大分類ｺｰﾄﾞ
			frm.teMain.setValueAt(info.getStringItem("DAIBUNRUINM"), intCurrentRow, frm.colDaibunNm);			//大分類名称
			frm.teMain.setValueAt(info.getStringItem("DAIBUNRUIRYAKUNM"), intCurrentRow, frm.colDaibunRyakuNm);	//大分類略称
			frm.teMain.setValueAt(info.getStringItem("CHUBUNRUICD"), intCurrentRow, frm.colChubunCd);			//中分類ｺｰﾄﾞ
			frm.teMain.setValueAt(info.getStringItem("CHUBUNRUINM"), intCurrentRow, frm.colChubunNm);			//中分類名称
			frm.teMain.setValueAt(info.getStringItem("CHUBUNRUIRYAKUNM"), intCurrentRow, frm.colChubunRyakuNm);	//中分類略称
			frm.teMain.setValueAt(info.getStringItem("SHOBUNRUICD"), intCurrentRow, frm.colShobunCd);			//小分類ｺｰﾄﾞ
			frm.teMain.setValueAt(info.getStringItem("SHOBUNRUINM"), intCurrentRow, frm.colShobunNm);			//小分類名称
			frm.teMain.setValueAt(info.getStringItem("SHOBUNRUIRYAKUNM"), intCurrentRow, frm.colShobunRyakuNm);	//小分類略称
			frm.teMain.setValueAt(info.getStringItem("MAKERCD"), intCurrentRow, frm.colMakerCd);				//ﾒｰｶｰｺｰﾄﾞ
			frm.teMain.setValueAt(info.getStringItem("MAKERNM"), intCurrentRow, frm.colMakerNm);				//ﾒｰｶｰ名称
			frm.teMain.setValueAt(info.getStringItem("LABELCD"), intCurrentRow, frm.colLabelCd);				//ﾚｰﾍﾞﾙｺｰﾄﾞ
			frm.teMain.setValueAt(info.getStringItem("LABELNM"), intCurrentRow, frm.colLabelNm);				//ﾚｰﾍﾞﾙ名称
			frm.teMain.setValueAt(info.getStringItem("SHOHINKANANM"), intCurrentRow, frm.colShohinKanaNm);		//商品名称カナ
			frm.teMain.setValueAt(info.getStringItem("SHOHINNM2"), intCurrentRow, frm.colShohinNm2);			//商品名称2
			frm.teMain.setValueAt(info.getStringItem("SHOHINKANANM2"), intCurrentRow, frm.colShohinKanaNm2);	//商品名称2カナ
			frm.teMain.setValueAt(info.getStringItem("JANCD"), intCurrentRow, frm.colJanCd);					//JANｺｰﾄﾞ
			frm.teMain.setValueAt(info.getStringItem("KIKAKUNO"), intCurrentRow, frm.colKikakuNo);				//規格番号
			frm.teMain.setValueAt(info.getStringItem("HATUBAIDATE"), intCurrentRow, frm.colHatubaiDate);		//発売日
			frm.teMain.setValueAt(Util.getCurFormat(info.getStringItem("TEIKA")), intCurrentRow, frm.colTeika);	//発売日

			frm.teMain.setValueAt(Util.getCurFormat(zaikosu), intCurrentRow, frm.colZaikoSu);					//在庫数
			frm.teMain.setValueAt(Util.getCurFormat(shuppinYoteisu), intCurrentRow, frm.colshuppinYoteiSu);		//出品予定数
			frm.teMain.setValueAt(Util.getCurFormat(genzaiShuppinsu), intCurrentRow, frm.colGenzaiShuppinSu);	//現在出品数

			priceInfo = null;
			long lngGenka = info.getLongItem("GENKA");
			if ( lngGenka == 0 ) {
				priceInfo = clsGetPrice.GetPrice(comH2, frm.teMain.getValueString(intCurrentRow, frm.colShohinCd),frm.teMain.getValueInt(intCurrentRow, frm.colRankCd),
						frm.teMain.getValueString(intCurrentRow, frm.colTanpinCd));
				if ( priceInfo != null ){
					lngGenka = priceInfo.getCpgenka();
				}
			}
			//原価の表示
			if ( lngGenka == 0 ){
				if(info.getLongItem("GENZAIKOSU")!=0){
					lngGenka = info.getLongItem("GENZAIKOKIN")/info.getLongItem("GENZAIKOSU");
				}
			}
			frm.teMain.setValueAt(Util.getCurFormat(lngGenka), intCurrentRow, frm.colGenka);

			if ( !Util.isNullOrEmpty(dbInfoZaiko.getStringItem("SHUPINKAKAKU")) ){
				long shuppinprice = dbInfoZaiko.getLongItem("SHUPINKAKAKU");
				frm.teMain.setValueAt(Util.getCurFormat(shuppinprice), intCurrentRow, frm.colHanbaiKakaku);			//出品価格
				frm.teMain.setValueAt(Util.getCurFormat(shuppinprice), intCurrentRow, frm.colHanbaiKakakuBefore);	//元販売価格
			}else{
				if ( priceInfo != null && priceInfo.getHanbaiShow() != 0  ){
					frm.teMain.setValueAt(Util.getCurFormat( priceInfo.getHanbaiShow()), intCurrentRow, frm.colHanbaiKakaku);//出品価格
				}
			}

			calcShuppinYoteiSu(intCurrentRow);
			concatColumnValue(intCurrentRow);
			mapTanpin.put(key, intCurrentRow);

			if ( searchMode == intKobetuSearch ){
				showSelectedRow(intCurrentRow);
			}
		}

		//一番上にﾌｫｰｶｽをあてる
//		if (frm.teMain.getRowCount() > 0){
//
//			frm.teMain.addRowSelectionInterval(0,0);
//			frm.teMain.moveToEditCell(0, frm.colHanbaiKakaku);
//			showShohinInfo();
//		}
		frm.jScrollPane.updateUI();
//		frm.teMain.getSelectionModel().addListSelectionListener(listSelectionListener);
	}

	/**
	 * 出品状態の取得
	 */
	private void getShuppinJotaiData(){
		try{
			mapShuppinJotai.clear();
			TenpoHanyoMstDAO hanyodao = new TenpoHanyoMstDAO(appConfig);
			DbInfo info = hanyodao.select(comH2, "AMAZON出品状態");
			for(int i=0; i<info.getMaxRowCount(); i++){
				info.setCurRow(i);
				mapShuppinJotai.put(info.getIntItem("CD"), info.getStringItem("NM1"));
			}
		}catch(TException ex){
			messageBox.disp(ex, MB_CRITICAL, "コンディションの設定でエラーが発生しました。" + ex.toString() + "\n" +
					ex.getMessage(),strMsgTitle);
		}
	}

	/**
	 * ｺｰﾄﾞより出品状態名称を取得する
	 * @param shuppinJotai　出品状態ｺｰﾄﾞ
	 * @return
	 */
	private String getShuppinJotaiNm(int shuppinJotai){
		if ( mapShuppinJotai.containsKey(shuppinJotai) ){
			return mapShuppinJotai.get(shuppinJotai);
		}
		return "";
	}

	/**
	 * コードより出品可否名称を取得する
	 * @param shuppinKahi 出品可否ｺｰﾄﾞ
	 * @return
	 */
	private String getShuppinKahiNm(int shuppinKahi){
		if ( shuppinKahi == Constants.AMAZON_SHUPPINKAHI_KA ){
			return "可";
		}
		return "不可";
	}

	private String trunc(String value, int maxLength){
		if ( Util.isNullOrEmpty(value) ){
			return "";
		}
		if ( value.length() > maxLength){
			return value.substring(0, maxLength);
		}
		return value;

	}

	/**
	 * 出品予定数の計算
	 * @param row 行番号
	 */
	public void calcShuppinYoteiSu(int row){
		//出品不可の場合、出品予定数に0でｾｯﾄ
		if ( frm.teMain.getValueInt(row, frm.colShuppinKahi) == Constants.AMAZON_SHUPPINKAHI_FUKA ){
			frm.teMain.setValueAt(0, row, frm.colshuppinYoteiSu);									//出品予定数
		}else{
			frm.teMain.setValueAt(Util.getCurFormat(frm.teMain.getValueInt(row, frm.colZaikoSu)
					-frm.teMain.getValueInt(row, frm.colGenzaiShuppinSu)), row, frm.colshuppinYoteiSu);//出品予定数
		}
	}

	public void concatColumnValue(int row){
		String strShuppinJotaiNm = getShuppinJotaiNm(frm.teMain.getValueInt(row, frm.colShuppinJotai));
		if ( frm.teMain.getValueInt(row, frm.colShuppinKinsiFlg) == 1 ){
			strShuppinJotaiNm = "出品禁止";
		}
		//出品状態/出品可否
		frm.teMain.setValueAt(concat(strShuppinJotaiNm,
				getShuppinKahiNm(frm.teMain.getValueInt(row, frm.colShuppinKahi))),
				row, frm.colGroup1);
		//商品ｺｰﾄﾞ/ﾗﾝｸ
		frm.teMain.setValueAt(concat(frm.teMain.getValueString(row, frm.colShohinCd),
				frm.teMain.getValueString(row, frm.colRankNm)),
				row, frm.colGroup2);
	}

	private String concat(String value1,String value2){
		return "<HTML>"+ value1+" <BR>"+ value2 +" </HTML>";
	}

	/**
	 * 商品情報をｸﾘｱする
	 */
	private void clearShohinInfo(){
		frm.lblShohinNm.setText("");
		frm.lblShohinCd.setText("");
		frm.lblJanCd.setText("");
		frm.lblHinban.setText("");
		frm.lblTeika.setText("");
		frm.lblShohinNm2.setText("");
		frm.lblHatubai.setText("");
		frm.lblMaker.setText("");
		frm.lblLabel.setText("");
		frm.lblDaibunrui.setText("");
		frm.lblChubunrui.setText("");
		frm.lblShobunrui.setText("");
		frm.lblTanpinCd.setText("");
	}

	/**
	 * 商品情報をﾗﾍﾞﾙに表示
	 */
	private void showShohinInfo( ){
		clearShohinInfo();
		int row = frm.teMain.getSelectedRow();
		if ( row < 0 || frm.teMain.getRowCount() == 0) return ;

		setF7KeyEnabled(row);

		frm.lblShohinNm.setText(frm.teMain.getValueString(row, frm.colShohinNm2));
		frm.lblShohinCd.setText(frm.teMain.getValueString(row, frm.colShohinCd));
		frm.lblJanCd.setText(frm.teMain.getValueString(row, frm.colJanCd));
		frm.lblHatubai.setText(frm.teMain.getValueString(row, frm.colHatubaiDate));
		frm.lblTeika.setText(frm.teMain.getValueString(row, frm.colTeika));
		frm.lblShohinNm2.setText(frm.teMain.getValueString(row, frm.colSubTitle));
		frm.lblMaker.setText(frm.teMain.getValueString(row, frm.colMakerNm));
		frm.lblDaibunrui.setText(frm.teMain.getValueString(row, frm.colDaibunCd)
				+ ":" + frm.teMain.getValueString(row, frm.colDaibunNm));
		frm.lblChubunrui.setText(frm.teMain.getValueString(row, frm.colChubunCd)
				+ ":" + frm.teMain.getValueString(row, frm.colChubunNm));
		frm.lblShobunrui.setText(frm.teMain.getValueString(row, frm.colShobunCd)
				+ ":" + frm.teMain.getValueString(row, frm.colShobunNm));
		frm.lblHinban.setText(frm.teMain.getValueString(row, frm.colKikakuNo));
		frm.lblLabel.setText(frm.teMain.getValueString(row, frm.colLabelNm));
		frm.lblTanpinCd.setText(frm.teMain.getValueString(row, frm.colTanpinCd));
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
		logOut.info("画面【" + frm.getTitle() + "】を閉じました。");
		frm.dispose();
		appConfig.removeDisp(activeId);
		messageBox = null;
	}

	/**
	 * ﾌｧﾝｸｼｮﾝｷｰの設定
	 */
	private void setFkeyEnabled( ){

		//ﾍｯﾀﾞｰにﾌｫｰｶｽがある場合
		switch( intInputMode ){
		case intInit:
			frm.fButton.butF1.setEnabled(false); 		//F1
			frm.fButton.butF2.setEnabled(frm.teMain.getRowCount()==0); 		//F2 条件指定
			frm.fButton.butF3.setEnabled(false);		//F3
			frm.fButton.butF4.setEnabled(true); 		//F4 拡張
			frm.fButton.butF5.setEnabled(true); 		//F5　商品検索
			frm.fButton.butF6.setEnabled(false); 		//F6
			frm.fButton.butF7.setEnabled(false); 		//F7
			frm.fButton.butF8.setEnabled(false); 		//F8
			frm.fButton.butF9.setText("F9 終了");
			frm.fButton.butF9.setEnabled(true); 		//F9 終了
			frm.fButton.butF10.setEnabled(false); 		//F10 CSV出力
			frm.fButton.butF11.setEnabled(false); 		//F11
			frm.fButton.butF12.setEnabled(false); 		//F12 確定
			break;
		case intSearch:
			frm.fButton.butF1.setEnabled(false); 		//F1
			frm.fButton.butF2.setEnabled(false); 		//F2 条件指定
			frm.fButton.butF3.setEnabled(false);		//F3
			frm.fButton.butF4.setEnabled(true); 		//F4 拡張
			frm.fButton.butF5.setEnabled(false); 		//F5
			frm.fButton.butF6.setEnabled(false); 		//F6
			frm.fButton.butF8.setEnabled(false);		//F8
			frm.fButton.butF9.setText("F9 戻る");
			frm.fButton.butF9.setEnabled(true); 		//F9 戻る
			frm.fButton.butF10.setEnabled(true); 		//F10 CSV出力
			frm.fButton.butF11.setEnabled(false); 		//F11
			frm.fButton.butF12.setEnabled(true); 		//F12 確定
			break;
		case intJidoShoriGo:
			frm.fButton.butF1.setEnabled(false); 		//F1
			frm.fButton.butF2.setEnabled(false); 		//F2 条件指定
			frm.fButton.butF3.setEnabled(false);		//F3
			frm.fButton.butF4.setEnabled(false); 		//F4 拡張
			frm.fButton.butF5.setEnabled(false); 		//F5
			frm.fButton.butF6.setEnabled(false); 		//F6
			frm.fButton.butF7.setEnabled(false); 		//F7
			frm.fButton.butF8.setEnabled(false);		//F8
			frm.fButton.butF9.setText("F9 戻る");
			frm.fButton.butF9.setEnabled(true); 		//F9 戻る
			frm.fButton.butF10.setEnabled(false); 		//F10 CSV出力
			frm.fButton.butF11.setEnabled(false); 		//F11
			frm.fButton.butF12.setEnabled(false); 		//F12 確定
			break;
		}
		if ( frm.teMain.getRowCount() <= 0 ){
			frm.fButton.butF9.setText("F9 終了");
			frm.fButton.butF12.setEnabled(false); 		//F12 確定
		}else{
			frm.fButton.butF9.setText("F9 戻る");
			frm.fButton.butF12.setEnabled(true); 		//F12 確定
		}
	}
}