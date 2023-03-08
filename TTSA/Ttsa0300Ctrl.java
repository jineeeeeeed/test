package jp.co.css.TTSA;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import jp.co.css.base.AppConfig;
import jp.co.css.base.BaseCtrl;
import jp.co.css.bean.DbInfo;
import jp.co.css.bean.DbInfoValue;
import jp.co.css.dao.BaseDAO;
import jp.co.css.dao.NetShukkaSijiDAO;
import jp.co.css.dao.TalosTrn;
import jp.co.css.dao.TanaMstDAO;
import jp.co.css.dao.TanpinMstDAO;
import jp.co.css.dao.TenpoControlMstDAO;
import jp.co.css.dao.TenpoKakuchoMstDAO;
import jp.co.css.dao.TenpoZaikoDAO;
import jp.co.css.dao.TimeShareChumonKanriReportDAO;
import jp.co.css.dao.TimeShareShuupinListDAO;
import jp.co.css.talos_l.bean.NetPickingBean;
import jp.co.css.talos_l.print.amazon.PrintPickingListTanpinDetail;
import jp.co.css.talos_l.print.amazon.PrintPickingListTanpinMain;
import jp.co.css.talos_l.util.Constants;
import jp.co.css.talos_l.util.NexUtil;
import jp.co.css.webpos.common.db.H2;
import jp.co.css.webpos.common.except.TException;
import jp.co.css.webpos.common.file.GenericFileFilter;
import jp.co.css.webpos.common.file.TCsvCreate;
import jp.co.css.webpos.common.file.TCsvReader;
import jp.co.css.webpos.common.gui.FKeyAdapter;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.gui.WaitBox;
import jp.co.css.webpos.common.message.MessageBoxValue;
import jp.co.css.webpos.common.message.MessageDispNew;
import jp.co.css.webpos.common.print.PDFViewCtrl;
import jp.co.css.webpos.common.util.Util;

/**---------------------------------------------
 * 処理名称	： 注文データ取込画面
 * 作成日	： 2012/08/20
 * 作成者	： ZWH
---------------------------------------------**/
public class Ttsa0300Ctrl extends BaseCtrl implements MessageBoxValue {
	public static final String ENCODE = "MS932";
	private final String strMsgTitle = "注文データ取込画面";// ﾒｯｾｰｼﾞ用ﾀｲﾄﾙ
	private String strErrTitle = "";					//ｴﾗｰﾒｯｾｰｼﾞ用処理ﾀｲﾄﾙ
	private Ttsa0300 frm = null;						//画面
	private String activeId = "Ttsa0300Ctrl";
	WaitBox waitBox;

	//現在の入力位置
	private final int intInit 	= 0; 					//初期
	private final int intShukka		= 1; 				//出荷
	private int intInputMode = intInit; 				//ﾃﾞﾌｫﾙﾄは初期

	//単品マスタDAO
	private TanpinMstDAO daoTanpin = null;
	private DbInfo dbInfoTp = null;

	private boolean isRunUpdate = false;						//更新処理が走っている間はTrue

	private List<Long> listTanpinKanriNo = new ArrayList<Long>();
	private List<Long> listMultiKanriNo = new ArrayList<Long>();
	public H2 comH2 = null; //共通

	// TIMESHARE出品データ情報
	private TimeShareShuupinListDAO daoTimeShareShuupinList = null;
	private DbInfo dbInfoShupin = null;

	private TimeShareChumonKanriReportDAO daoTimeShareChumonKanriReport = null;

	List<List<String>> listErrCreate = new ArrayList<List<String>>();

	private String strCsvFileName = "";

	private long lngShukkaSijiNoFrom;

	private long lngShukkaSijiNoTo;

	public Ttsa0300Ctrl( AppConfig ap, String functionId, Object obj) throws Exception{

		//初期設定
		super(ap,functionId,obj);
		//起動済みの場合、起動済み画面を呼び出す。2011/4/4 nomura
		if (Util.ActiveDisp(appConfig.getDispObjects(), activeId)) {
			return;
		}
		appConfig = ap;
		if (frm == null) {
			frm = new Ttsa0300(ap);
		}
		frm.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispClose();
			}
		});
		if(comH2==null){
			comH2=new H2();
			try{
				comH2.h2ClientStart(appConfig.getDatabaseXmlBean());
			}catch(TException e){
				messageBox.disp(MB_EXCLAMATION, "H2 ｴﾗｰ" + e.getMessage(), strMsgTitle);
				this.dispClose();
				return ;

			}
		}

		messageBox = new MessageDispNew(frm, appConfig.getLogOut());

		// Fkey初期化
		frm.fButton.setFAllEnabled(false);
		frm.fButton.setF1Enabled(true);
		frm.fButton.setF1Text("F1 CSV取込");
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

		//ﾌｧﾝｸｼｮﾝｷｰ/ﾎﾞﾀﾝﾘｽﾅ追加
		frm.fButton.addFButtonListener(new ButtonListener());
		//ﾌｧﾝｸｼｮﾝｷｰ/ﾛｸﾞ処理追加
		frm.fButton.setLogOut(logOut);

		frm.teMain.addMouseListener(new TableMouseListener());
		frm.teMain.addKeyListener(new TableKeyListener());

		// 列定義の初期化
		initColumns();

		initText();
		//ﾃｰﾌﾞﾙの初期化
		clearTable(0);
		//商品情報のｸﾘｱ
		clearShohinInfo();
		//ﾌｧﾝｸｼｮﾝｷｰの設定
		setFkeyEnabled();

		logOut.info("画面【" + frm.getTitle() + "】を開きました。");
		// 画面サイズ調整
		TGuiUtil.resizeWindow(frm);
		//画面の表示
		frm.setVisible(true);
		appConfig.addDisp(activeId, frm);

	}

	//ﾒｲﾝﾃｰﾌﾞﾙ:ﾏｳｽﾘｽﾅ
	class TableMouseListener extends MouseAdapter {
		public void mouseClicked(java.awt.event.MouseEvent e ){

			//ﾃｰﾌﾞﾙに表示されているﾃﾞｰﾀをﾃｷｽﾄ(入力項目)及びﾗﾍﾞﾙに表示
			showShohinInfo();
		}
	}

	//ﾒｲﾝﾃｰﾌﾞﾙ:ｷｰﾘｽﾅ
	class TableKeyListener extends KeyAdapter {
		//ｾﾙの移動前に呼ばれる
		public void keyPressed(java.awt.event.KeyEvent e ){

		}

		//ｾﾙの移動後呼ばれる
		public void keyReleased(java.awt.event.KeyEvent e ){
			//ﾃｰﾌﾞﾙに表示されているﾃﾞｰﾀをﾃｷｽﾄ(入力項目)及びﾗﾍﾞﾙに表示
			showShohinInfo();
		}
	}

	private void initText(){

		frm.lblErrorCount.setText("");
		frm.lblNormalCount.setText("");

	}

	/**
	 * ﾌｧﾝｸｼｮﾝｷｰの制御
	 *
	 * @author Administrator
	 */
	class ButtonListener extends FKeyAdapter
	{
		//F1 データ取込
		public void f1Click(ActionEvent e) {
			csvRead();
		}

		// F9 終了
		public void f9Click(ActionEvent e) {
			switch( intInputMode ){
			case intInit:
				dispClose();
				break;
			case intShukka:
				//ﾃｰﾌﾞﾙ内容をｸﾘｱ
				clearTable(0);
				//商品情報の初期化
				clearShohinInfo();
				initText();
				intInputMode = intInit;
				setFkeyEnabled();
				break;
			}
		}

		// F12 登録
		public void f12Click(ActionEvent e) {
			listTanpinKanriNo.clear();
			listMultiKanriNo.clear();

			try {
				//取込前に再度取込
				if (!isRunUpdate) {
					//更新
					if ( updateMst() ){
						isRunUpdate=false;
						return ;
					}

					NetShukkaSijiDAO daoNetShukkaSiji = new NetShukkaSijiDAO(appConfig);
					DbInfo dbInfoSiji = daoNetShukkaSiji.selectShukka(comH2, lngShukkaSijiNoFrom, lngShukkaSijiNoTo);

					//ピッキングリスト
					printPListTanpin(dbInfoSiji);
				}
				isRunUpdate=false;
				f9Click(e);
			} catch (Exception te) {
				messageBox.disp(te, MB_CRITICAL, te.toString() + "\n"
						+ te.getMessage(), "f12Click");
			}
		}
	}

	/**
	 * ピッキングリスト出力
	 */
	private void printPListTanpin(DbInfo dbInfoSiji){

		try{
			int nexPickingListShohinNmType = 0;				//通販ピッキングリスト商品名称出力方式
			int nexPickingListShohinNmMojiSu1 = 0;			//通販ピッキングリスト商品名称出力文字数１
			int nexPickingListShohinNmMojiSu2 = 0;			//通販ピッキングリスト商品名称出力文字数２
			NetPickingBean bean = new NetPickingBean();// bean初期化 No6850 2014/11/14 寺澤追加
			TenpoKakuchoMstDAO tkkDao = new TenpoKakuchoMstDAO(appConfig);
			DbInfo tkkInfo = tkkDao.select(comH2, appConfig.getTenpoCd());
			if ( tkkInfo.getMaxRowCount() > 0 ){
				nexPickingListShohinNmType = tkkInfo.getIntItem("NEXPICKINGLISTSHOHINNMTYPE");
				nexPickingListShohinNmMojiSu1 = tkkInfo.getIntItem("NEXPICKINGLISTSHOHINNMMOJISU1");
				nexPickingListShohinNmMojiSu2 = tkkInfo.getIntItem("NEXPICKINGLISTSHOHINNMMOJISU2");
			}


			TanaMstDAO tanaDao = new TanaMstDAO(appConfig);
			List<PrintPickingListTanpinDetail> list = new ArrayList<PrintPickingListTanpinDetail>();

			String shukkakanrinoFrom = "";
			String shukkakanrinoTo = "";

			PrintPickingListTanpinDetail detail = new PrintPickingListTanpinDetail();
			for(int i=0; i<dbInfoSiji.getMaxRowCount(); i++){
				dbInfoSiji.setCurRow(i);
				if ( dbInfoSiji.getIntItem("SHOHINZOKUSEI") != 0 ){//通常商品ではない場合、対象外
					continue;
				}
				if(detail.getChubunruiCd().equals(dbInfoSiji.getStringItem("CHUBUNRUICD"))&&
						detail.getMakerCd().equals(dbInfoSiji.getStringItem("MAKERCD"))&&
						detail.getKikakuNo().equals(dbInfoSiji.getStringItem("KIKAKUNO"))&&
						detail.getShohinCd()==(dbInfoSiji.getStringItem("SHOHINCD"))&&
						detail.getShuppinKakaku()==(dbInfoSiji.getLongItem("HANBAITANKA"))&&
						detail.getTanpinCd().equals(dbInfoSiji.getStringItem("TANPINCD"))){
					//重複するデータがある場合数量を加算
					detail.setSu(detail.getSu() + dbInfoSiji.getLongItem("SHUKKASIJISU"));

				}else{
					if (i > 0) {
						//データを保存し次の行へ(初回は除く)
						list.add(detail);
						detail = new PrintPickingListTanpinDetail();
					}

					if(!Util.isNullOrEmpty(dbInfoSiji.getStringItem("TENPOTANABANCD"))){
						DbInfo Tinfo = tanaDao.select(comH2, dbInfoSiji.getIntItem("TENPOTANABANCD"), appConfig.getTenpoCd());
						if(Tinfo.getMaxRowCount()>0){
							if(!Util.isNullOrEmpty(Tinfo.getStringItem("TANANAME"))){
								detail.setTanaNo(Tinfo.getStringItem("TANANAME"));
							}else{
								detail.setTanaNo(dbInfoSiji.getStringItem("TENPOTANABANCD"));
							}
						}else{
							detail.setTanaNo(dbInfoSiji.getStringItem("TENPOTANABANCD"));
						}
					}else{
						detail.setTanaNo("-");
					}
					//出荷管理番号
					detail.setShukkaKanriNo(dbInfoSiji.getStringItem("SHUKKAKANRINO"));
					//メーカー名称
					detail.setMakerCd(dbInfoSiji.getStringItem("MAKERCD"));
					detail.setMakerNm(dbInfoSiji.getStringItem("MAKERRYAKUNM"));
					//商品コード
					detail.setShohinCd(dbInfoSiji.getStringItem("SHOHINCD"));
					//商品名称
					//detail.setShohinNm(dbInfoSiji.getStringItem("SHOHINNM"));
					detail.setShohinNm(NexUtil.getNexPickingListOutputShohinNm(dbInfoSiji.getStringItem("TITLE"),
							nexPickingListShohinNmType, nexPickingListShohinNmMojiSu1, nexPickingListShohinNmMojiSu2));
					//商品名称２
					detail.setShohinNm2(dbInfoSiji.getStringItem("SHOHINNM2"));
					//JANコード
					detail.setJanCd(dbInfoSiji.getStringItem("JANCD"));
					//規格番号
					detail.setKikakuNo(dbInfoSiji.getStringItem("KIKAKUNO"));
					//中分類
					detail.setChubunruiCd(dbInfoSiji.getStringItem("CHUBUNRUICD"));
					detail.setChubunruiNm(dbInfoSiji.getStringItem("CHUBUNRUIRYAKUNM"));
					//ランク
					detail.setStrRankCd(dbInfoSiji.getStringItem("KAKAKUKANRIRANK"));//No.22346 2020/06/16 趙 志強 add
					detail.setRankNm(dbInfoSiji.getStringItem("KAKAKUKANRIRANKNM"));
					//数量
					detail.setSu(dbInfoSiji.getLongItem("SHUKKASIJISU"));
					//在庫数 2015/07/30 MA
					detail.setZaikoSu(dbInfoSiji.getLongItem("GENZAIKOSU"));
					//出品価格
					detail.setShuppinKakaku(dbInfoSiji.getLongItem("HANBAITANKA"));
					//単品コード
					detail.setTanpinCd(dbInfoSiji.getStringItem("TANPINCD"));

				}
			}
			//出荷管理番号範囲を取得
			shukkakanrinoFrom = dbInfoSiji.getStringItem("SHUKKAKANRINO", 0);
			shukkakanrinoTo = dbInfoSiji.getStringItem("SHUKKAKANRINO",dbInfoSiji.getMaxRowCount()-1);
			bean.setStrKanriNoFrom(shukkakanrinoFrom);//No6850 2014/11/14 寺澤追加
			bean.setStrKanriNoTo(shukkakanrinoTo);//No6850 2014/11/14 寺澤追加

			//最終行追加
			list.add(detail);

			//ﾋﾟｯｷﾝｸﾞﾘｽﾄ(単品)PDFファイル作成
			//PrintPickingListTanpinMain report = new PrintPickingListTanpinMain(list, shukkakanrinoFrom, shukkakanrinoTo,"");

			//No6850 2014/11/14 寺澤追加
			PrintPickingListTanpinMain report = new PrintPickingListTanpinMain(list, bean,"");
			report.write();

			//ﾋﾟｯｷﾝｸﾞﾘｽﾄプレビュー画面表示
			PDFViewCtrl view = new PDFViewCtrl(report.getFileName(), report.getSize(),frm);
			view.show();
		}catch(Exception ex){
			messageBox.disp(ex, MB_CRITICAL, "ピッキングリスト印刷処理でエラーが起きました。" + "\n" + ex.getMessage(), strMsgTitle);
		}
	}

	/*
	 * 出荷指示番号をサーバから取得する
	 * 汎用採番メソッド（9999対応）
	 */
	public long getNewShukkaSijiNo(H2 h2, int intCnt){

		long lngNewNumber;
		try {
			// 新規コード取得
			if (appConfig.getStandAloneFlg() == 0 ) {
				TenpoControlMstDAO tenpoControlDao = new TenpoControlMstDAO(appConfig);
				lngNewNumber = tenpoControlDao.getNumberHost("9999", "AMAZONSHUKKASIJIDENPYONO", intCnt);
			}else{
				TenpoControlMstDAO tenpoControlDao = new TenpoControlMstDAO(appConfig);
				lngNewNumber = tenpoControlDao.getNumber(h2, "AMAZONSHUKKASIJIDENPYONO", intCnt);
			}
			return lngNewNumber;
		} catch (TException te) {
			messageBox.disp(te, MB_CRITICAL, "出荷指示番号の採番処理でエラーが発生しました。(Exception)" + "\n"
					+ te.toString() + te.getMessage(), strMsgTitle);
			return -1;
		}
	}

	/**
	 * 更新処理
	 * @return
	 */
	private boolean updateMst( ){

		H2 h2 = new H2(); // H2

		try {

			h2.h2ClientStart(appConfig.getDatabaseXmlBean());
			h2.setAutoCommit(false); // autocommit off( h2の場合 strat
										// transaction に相当 )
			h2.startTran(); // start transaction
			// トランザクション開始
			int tranno = TalosTrn.getTrnNo(appConfig);
			int renban = 0;

			// 伝票番号取得
			strErrTitle = "[伝票番号取得]";

			//出荷指示番号の取得
			long lngShukkaSijiNo = getNewShukkaSijiNo(h2, frm.teMain.getRowCount());
			if ( lngShukkaSijiNo < 0 ){
				return true;
			}

			lngShukkaSijiNoFrom = lngShukkaSijiNo;

			// BaseDAOの初期化
			BaseDAO daoBase = new BaseDAO(appConfig);
			TenpoControlMstDAO tcdao = new TenpoControlMstDAO(appConfig);

			strErrTitle = "[店舗在庫データINFOの作成]";
			TenpoZaikoDAO tenpoZaikoDao = new TenpoZaikoDAO(appConfig);
			DbInfo dbInfoZaiko = tenpoZaikoDao.get(h2);

			DbInfo dbInfoChumon = daoBase.getDbInfo(h2, "D_TIMESHARECHUMONKANRIREPORT");
			DbInfo dbInfoSiji = daoBase.getDbInfo(h2, "D_NETSHUKKASIJI");

			//注文管理ﾚﾎﾟｰﾄの作成
			for(int row=0; row<frm.teMain.getRowCount(); row++){
				strErrTitle = "[通販注文管理レポートINFOの作成]";
				// ﾌｨｰﾙﾄﾞ数分DbInfoValueを作成
				DbInfoValue trv = new DbInfoValue(dbInfoChumon.getFieldCount());

				dbInfoChumon.setItem(trv, "TENPOCD", appConfig.getTenpoCd());													// 店舗コード
				dbInfoChumon.setItem(trv, "CHUMONSITE", frm.teMain.getValueString(row, frm.colChumonSite));						// サイト名
				dbInfoChumon.setItem(trv, "CHUMONID", frm.teMain.getValueString(row, frm.colChumonId));							// 注文ID
				dbInfoChumon.setItem(trv, "CHUMONSHOHINID", frm.teMain.getValueString(row, frm.colTanpinCd));					// 注文商品ID
				dbInfoChumon.setItem(trv, "KAKUTEIDATE", frm.teMain.getValueTimeStamp(row, frm.colKakuteiDate));				// 注文確定日
				dbInfoChumon.setItem(trv, "BUYERNM", frm.teMain.getValueString(row, frm.colBuyerNm));							// 購入者氏名
				dbInfoChumon.setItem(trv, "BUYERKANA", frm.teMain.getValueString(row, frm.colBuyerKana));						// 購入者かな
				dbInfoChumon.setItem(trv, "BUYERYUBINNO", frm.teMain.getValueString(row, frm.colBuyerYubinno));					// 購入者郵便番号
				dbInfoChumon.setItem(trv, "BUYERTODOUFUKEN", frm.teMain.getValueString(row, frm.colBuyerTodoufuken));			// 購入者都道府県
				dbInfoChumon.setItem(trv, "BUYERSICHOSON", frm.teMain.getValueString(row, frm.colBuyerSichoson));				// 購入者市区町村
				dbInfoChumon.setItem(trv, "BUYERBANCHI", frm.teMain.getValueString(row, frm.colBuyerBanchi));					// 購入者番地
				dbInfoChumon.setItem(trv, "BUYERMANSHON", frm.teMain.getValueString(row, frm.colBuyerManshon));					// 購入者ビル・マンション名
				dbInfoChumon.setItem(trv, "BUYERMAILADDRESS", frm.teMain.getValueString(row, frm.colBuyerMailAddress));			// 購入者Eメール
				dbInfoChumon.setItem(trv, "BUYERTEL", frm.teMain.getValueString(row, frm.colBuyerTel));							// 購入者電話番号
				dbInfoChumon.setItem(trv, "UKETORIJIN", frm.teMain.getValueString(row, frm.colUketoriJin));						// 送付先氏名
				dbInfoChumon.setItem(trv, "UKETORIJINKANA", frm.teMain.getValueString(row, frm.colUketoriJinKana));				// 送付先かな
				dbInfoChumon.setItem(trv, "TODOKESAKIYUBINNO", frm.teMain.getValueString(row, frm.colTodokesakiYubinno));		// 送付先郵便番号
				dbInfoChumon.setItem(trv, "TODOKESAKITODOUFUKEN", frm.teMain.getValueString(row, frm.colTodokesakiTodoufuken));	// 送付先都道府県
				dbInfoChumon.setItem(trv, "TODOKESAKISICHOSON", frm.teMain.getValueString(row, frm.colTodokesakiSichoson));		// 送付先市区町村
				dbInfoChumon.setItem(trv, "TODOKESAKI1", frm.teMain.getValueString(row, frm.colTodokesaki1));					// 送付先番地
				dbInfoChumon.setItem(trv, "TODOKESAKI2", frm.teMain.getValueString(row, frm.colTodokesaki2));					// 送付先ビル・マンション名
				dbInfoChumon.setItem(trv, "TODOKESAKITEL", frm.teMain.getValueString(row, frm.colTodokesakiTel));				// 送付先電話番号
				dbInfoChumon.setItem(trv, "TITLE", frm.teMain.getValueString(row, frm.colTitle));								// 商品名
				dbInfoChumon.setItem(trv, "SKU", frm.teMain.getValueString(row, frm.colSku));									// SKU
				dbInfoChumon.setItem(trv, "HANBAISU", frm.teMain.getValueInt(row, frm.colSu));									// 数量
				dbInfoChumon.setItem(trv, "HANBAITANKA", frm.teMain.getValueInt(row, frm.colHanbaiTanka));						// 価格
				dbInfoChumon.setItem(trv, "HANBAIKIN", frm.teMain.getValueInt(row, frm.colHanbaiKin));							// 販売金額
				dbInfoChumon.setItem(trv, "SORYO", frm.teMain.getValueInt(row, frm.colSoryo));									// 送料
				dbInfoChumon.setItem(trv, "TESURYO", frm.teMain.getValueInt(row, frm.colTesuryo));								// 手数料
				dbInfoChumon.setItem(trv, "NEBIKI", frm.teMain.getValueInt(row, frm.colNebiki));								// 値引き
				dbInfoChumon.setItem(trv, "USEPOINT", frm.teMain.getValueInt(row, frm.colUsePoint));							// ポイント利用
				dbInfoChumon.setItem(trv, "GOKEIKIN", frm.teMain.getValueInt(row, frm.colGokeiKin));							// 合計金額
				dbInfoChumon.setItem(trv, "SHIHARAIHOHONM", frm.teMain.getValueString(row, frm.colShiharaiHoho));				// 決済方法
				dbInfoChumon.setItem(trv, "HAISOUHOUHOU", frm.teMain.getValueString(row, frm.colHaisohouhou));					// 配送方法
				dbInfoChumon.setItem(trv, "SHORIZUMIFLG", 0);																	// 処理済フラグ
				dbInfoChumon.setItem(trv, "SHOHINCD", frm.teMain.getValueString(row, frm.colShohinCd));							// 商品コード
				dbInfoChumon.setItem(trv, "KAKAKUKANRIRANK", frm.teMain.getValueString(row, frm.colRankCd));					// 価格管理ランク
				dbInfoChumon.setItem(trv, "STATUS", "");																		// ｽﾃｰﾀｽ
				dbInfoChumon.setItem(trv, "KOSINFLG", 1);																		// 更新フラグ
				dbInfoChumon.setItem(trv, "SAISHUKOSINTANTOCD", appConfig.getTantoushaCd());									// 最終更新担当者コード
				dbInfoChumon.setItem(trv, "SAISHUKOSINDATETIME", Util.getCurrentDateSql());										// 最終更新日付
				dbInfoChumon.setItem(trv, "HOSTSOSINFLG", 0);

				// valueｾｯﾄ
				dbInfoChumon.setRow(trv);
			}

			//通販出荷指示ﾃﾞｰﾀの作成
			for(int row=0; row<dbInfoChumon.getMaxRowCount(); row++){
				dbInfoChumon.setCurRow(row);
				String strTanpinCd = frm.teMain.getValueString(row, frm.colTanpinCd);
				String strShohinCd = frm.teMain.getValueString(row, frm.colShohinCd);
				int intRankCd	   = frm.teMain.getValueInt(row, frm.colRankCd);
				strErrTitle = "[通販出荷指示データINFOの作成]";
				// ﾌｨｰﾙﾄﾞ数分DbInfoValueを作成
				DbInfoValue trv = new DbInfoValue(dbInfoSiji.getFieldCount());

				dbInfoSiji.setItem(trv, "TENPOCD", dbInfoChumon.getStringItem("TENPOCD"));					//店舗コード
				dbInfoSiji.setItem(trv, "SHUKKASIJINO", lngShukkaSijiNo++);									//出荷指示No
				dbInfoSiji.setItem(trv, "SHUKKASIJIGYO", 1);												//出荷指示行No
				dbInfoSiji.setItem(trv, "SHUKKASIJISU", dbInfoChumon.getStringItem("HANBAISU"));			//出荷指示数
				dbInfoSiji.setItem(trv, "SHUKKASIJIDATE", "");												//出荷指示日
				dbInfoSiji.setItem(trv, "SHUKKAJOTAIKB", Constants.AMAZON_SHUKKAJOTAIKB_KARISHUKKA);		//出荷状態：仮出荷指示
				dbInfoSiji.setItem(trv, "TANPINCD", strTanpinCd);											//単品コード
				dbInfoSiji.setItem(trv, "SHOHINCD", strShohinCd);											//商品コード
				dbInfoSiji.setItem(trv, "KAKAKUKANRIRANK", intRankCd);										//価格管理ランク
				dbInfoSiji.setItem(trv, "SHOHINJOTAI", "");													//商品状態
				dbInfoSiji.setItem(trv, "CHUMONID", dbInfoChumon.getStringItem("CHUMONID"));				//注文ID
				dbInfoSiji.setItem(trv, "CHUMONSHOHINID", dbInfoChumon.getStringItem("CHUMONSHOHINID"));	//注文商品ID
				dbInfoSiji.setItem(trv, "TITLE", dbInfoChumon.getStringItem("TITLE"));						//商品名
				dbInfoSiji.setItem(trv, "SHUPPINID", "");													//出品ID
				dbInfoSiji.setItem(trv, "SKU", dbInfoChumon.getStringItem("SKU"));							//SKU
				dbInfoSiji.setItem(trv, "HANBAITANKA", dbInfoChumon.getLongItem("HANBAITANKA"));			//価格
				dbInfoSiji.setItem(trv, "SORYO", dbInfoChumon.getLongItem("SORYO"));						//配送料
				dbInfoSiji.setItem(trv, "NEBIKI", dbInfoChumon.getLongItem("NEBIKI"));						//値引き
				dbInfoSiji.setItem(trv, "POINTNEBIKI", dbInfoChumon.getLongItem("USEPOINT"));				//ポイント値引
				dbInfoSiji.setItem(trv, "TESURYO", dbInfoChumon.getLongItem("TESURYO"));					//手数料
				dbInfoSiji.setItem(trv, "HAISOUHOUHOUNM", dbInfoChumon.getStringItem("HAISOUHOUHOU"));		//配送方法
				dbInfoSiji.setItem(trv, "KAKUTEIDATE", dbInfoChumon.getStringItem("KAKUTEIDATE"));			//注文確定日
				dbInfoSiji.setItem(trv, "BUYERMAILADDRESS", dbInfoChumon.getStringItem("BUYERMAILADDRESS"));//購入者のEメール
				dbInfoSiji.setItem(trv, "BUYERNM", dbInfoChumon.getStringItem("BUYERNM"));					//購入者の名前
				dbInfoSiji.setItem(trv, "RENRAKUSAKI", dbInfoChumon.getStringItem("BUYERTEL"));				//購入者の電話番号
				dbInfoSiji.setItem(trv, "KEITAITEL", "");													//購入者の携帯番号
				dbInfoSiji.setItem(trv, "TODOKESAKITEL", dbInfoChumon.getStringItem("TODOKESAKITEL"));		//お届け先の携帯番号
				dbInfoSiji.setItem(trv, "TODOKESAKIKEITAITEL", "");											//お届け先の携帯番号
				dbInfoSiji.setItem(trv, "UKETORIJIN", dbInfoChumon.getStringItem("UKETORIJIN"));			//受取人
				dbInfoSiji.setItem(trv, "TODOKESAKI1", dbInfoChumon.getStringItem("TODOKESAKI1"));			//お届け先１
				dbInfoSiji.setItem(trv, "TODOKESAKI2", dbInfoChumon.getStringItem("TODOKESAKI2"));			//お届け先２
				dbInfoSiji.setItem(trv, "TODOKESAKI3", "");													//お届け先３
				dbInfoSiji.setItem(trv, "SICHOSON", dbInfoChumon.getStringItem("TODOKESAKISICHOSON"));		//市町村
				dbInfoSiji.setItem(trv, "TODOUFUKEN", dbInfoChumon.getStringItem("TODOKESAKITODOUFUKEN"));	//都道府県
				dbInfoSiji.setItem(trv, "YUBINNO", dbInfoChumon.getStringItem("TODOKESAKIYUBINNO"));		//郵便番号
				dbInfoSiji.setItem(trv, "COUNTRY", "JP");													//国
				dbInfoSiji.setItem(trv, "CHUMONSITEFLG", Constants.CHUMONSITEFLG_TIME_SHARE);				//注文サイトフラグ:TIME SHARE
				dbInfoSiji.setItem(trv, "CHUMONSITE", dbInfoChumon.getStringItem("CHUMONSITE"));			//注文サイト
				dbInfoSiji.setItem(trv, "OTODOKEJIKAN", "");												//お届け時間
				dbInfoSiji.setItem(trv, "YOUBOU", "");														//要望等
				dbInfoSiji.setItem(trv, "COMMENT", "");														//コメント
				dbInfoSiji.setItem(trv, "OTODOKEJIKANCD", "");												//お届け時間コード
				dbInfoSiji.setItem(trv, "HAITATUKIBOBI", "");												//配達希望日
				dbInfoSiji.setItem(trv, "SHIHARAIHOHONM", dbInfoChumon.getStringItem("SHIHARAIHOHONM"));	//支払い方法名称
				dbInfoSiji.setItem(trv, "KOSINFLG", 1);														//更新フラグ
				dbInfoSiji.setItem(trv, "SAISHUKOSINTANTOCD", appConfig.getTantoushaCd());					//最終更新担当者コード
				dbInfoSiji.setItem(trv, "SAISHUKOSINDATETIME", Util.getCurrentDateSql());					//最終更新日付
				dbInfoSiji.setItem(trv, "HOSTSOSINFLG", 0);

				// valueｾｯﾄ
				dbInfoSiji.setRow(trv);

				strErrTitle = "[店舗在庫データの検索]";
				DbInfo info = tenpoZaikoDao.selectHost(h2, appConfig.getTenpoCd(), frm.teMain.getValueString(row, frm.colTanpinCd));
				if ( info.getMaxRowCount() > 0 ){
					dbInfoZaiko.setRow(info.getRow());
					dbInfoZaiko.setCurRow(dbInfoZaiko.getMaxRowCount()-1);

					int intHikiate = dbInfoChumon.getIntItem("HANBAISU");
					dbInfoZaiko.setCurItem("HIKIATEZAIKO", dbInfoZaiko.getIntItem("HIKIATEZAIKO") + intHikiate);//引当数
					dbInfoZaiko.setCurItem("KOSINFLG", 2);													//更新フラグ
					dbInfoZaiko.setCurItem("SAISHUKOSINTANTOCD", appConfig.getTantoushaCd());					//最終更新担当者コード
					dbInfoZaiko.setCurItem("SAISHUKOSINDATETIME", Util.getCurrentDateSql());					//最終更新日付
					dbInfoZaiko.setCurItem("HOSTSOSINFLG", 1);												//HOST送信フラグ
					dbInfoZaiko.setCurItem("WEBTOROKUZUMIFLG", 0);											//WEB登録済みフラグ
					// リアルタイム更新SQL文を取得する。
					List<String> strSQL = tenpoZaikoDao.getUpdateSql(h2, appConfig.getTenpoCd(), strTanpinCd, strShohinCd, intRankCd, intHikiate);
					// リアルタイムからD_TRANに変更するように ZWH 2016/06/29
//					renban = daoBase.addRealTimeSql(h2, tranno, renban, strSQL);
					renban = daoBase.addRealTimeSql(h2, tranno, renban, strSQL, Constants.REALTIME_TRAN);
				}
			}

			lngShukkaSijiNoTo = lngShukkaSijiNo-1;

			List<ChumonId> list = new ArrayList<ChumonId>();

			//出荷管理番号の作成(注文単位で纏める)
			for(int row=0; row < dbInfoSiji.getMaxRowCount(); row++){
				dbInfoSiji.setCurRow(row);
				String strChumonId = dbInfoSiji.getStringItem("CHUMONID");
				String strChumonSite = dbInfoSiji.getStringItem("CHUMONSITE");
				long lngShukkaKanriNo = -1;
				for (int m=0;m<list.size();m++) {
					if (strChumonSite.equals(list.get(m).getChumonSite()) && strChumonId.equals(list.get(m).getChumonId())) {
						lngShukkaKanriNo = list.get(m).getKanriNo();
						break;
					}
				}
				if (lngShukkaKanriNo == -1) {
					lngShukkaKanriNo = tcdao.getNumber("SHUKKAKANRINO");//***出荷管理番号
					ChumonId chumonId = new ChumonId();
					chumonId.setChumonId(strChumonId);
					chumonId.setChumonSite(strChumonSite);
					chumonId.setKanriNo(lngShukkaKanriNo);
					list.add(chumonId);
				}

				dbInfoSiji.setCurItem("SHUKKASIJIDATE", Util.getCurrentDateSql());								//出荷指示日
				dbInfoSiji.setCurItem("SHUKKAJOTAIKB", Constants.AMAZON_SHUKKAJOTAIKB_SHUKKASIJI);				//出荷状態    1:指示済
				dbInfoSiji.setCurItem("SHUKKAKANRINO", lngShukkaKanriNo);										//出荷管理番号
				dbInfoSiji.setCurItem("TANMATUNO", appConfig.getTanmatuNo());									//端末Ｎｏ
				dbInfoSiji.setCurItem("KOGUTISU", dbInfoSiji.getIntItem("SHUKKASIJISU"));						//個口数
				dbInfoSiji.setCurItem("KOSINFLG", 2);															//更新フラグ:更新
				dbInfoSiji.setCurItem("SAISHUKOSINTANTOCD", appConfig.getTantoushaCd());						//最終更新担当者コード
				dbInfoSiji.setCurItem("SAISHUKOSINDATETIME", Util.getCurrentDateSql());							//最終更新日付
				dbInfoSiji.setCurItem("HOSTSOSINFLG", 0);														//HOST送信フラグ
			}

			strErrTitle = "[通販注文管理レポートのINSERT]";
			renban = daoBase.insertDbInfo(dbInfoChumon, h2, 1, tranno, renban);

			strErrTitle = "[通販出荷指示データのINSERT]";
			renban = daoBase.insertDbInfo(dbInfoSiji, h2, 1, tranno, renban);

			strErrTitle = "[店舗在庫ﾃﾞｰﾀのUPDATE]";
			daoBase.insert(dbInfoZaiko, h2);

			renban = daoBase.insertTrnEnd(h2, tranno, renban);

			strErrTitle = "[リアルタイム更新処理]";
			daoBase.execRealTimeSql();

			// ｺﾐｯﾄ
			h2.commitTran();

			return false;

		} catch (Exception e) {
			// ﾛｰﾙﾊﾞｯｸ
			try {
				h2.rollBackTran();
			} catch (Exception ex) {
				messageBox.disp(ex, MB_CRITICAL, "ロールバックでエラーが起きました。" + "\n"
						+ e.getMessage(), strMsgTitle);
			}
			messageBox.disp(e, MB_CRITICAL, strErrTitle + "\n" + "更新処理でエラーが起きました。"
					+ "\n" + e.getMessage(), strMsgTitle);
			return true;
		}finally{
			try{
				h2.h2ClientStop();
			}catch(TException e){};
		}
	}

	/**
	 * ﾃｰﾌﾞﾙの初期化
	 */
	private void clearTable(int intRowCnt){
		//ﾒｲﾝﾃｰﾌﾞﾙ
		//行ｸﾘｱ
		frm.teMain.setRowCount(0);

		//0以上なら行追加
		if ( intRowCnt > 0 ){
			frm.teMain.addRows(intRowCnt);
		}
		frm.jScrollPane.updateUI();
	}

	private void addToTable(DbInfo info, List<String> list) throws TException{
		frm.teMain.addRow();
		info.setCurRow(0);

		int row = frm.teMain.getRowCount() - 1;

		//出荷指示情報
		frm.teMain.setValueAt(list.get(0), row, frm.colChumonSite);    								//サイト名
		frm.teMain.setValueAt(list.get(1), row, frm.colChumonId);    								//注文ID
		frm.teMain.setValueAt(list.get(21), row, frm.colChumonShohinId);							//注文商品ID
		frm.teMain.setValueAt(list.get(2), row, frm.colKakuteiDate);								//注文確定日

		frm.teMain.setValueAt(list.get(3), row, frm.colBuyerNm);									//購入者氏名
		frm.teMain.setValueAt(list.get(4), row, frm.colBuyerKana);									//購入者かな
		frm.teMain.setValueAt(list.get(5), row, frm.colBuyerYubinno);								//購入者郵便番号
		frm.teMain.setValueAt(list.get(6), row, frm.colBuyerTodoufuken);							//購入者都道府県
		frm.teMain.setValueAt(list.get(7), row, frm.colBuyerSichoson);								//購入者市区町村
		frm.teMain.setValueAt(list.get(8), row, frm.colBuyerBanchi);								//購入者番地
		frm.teMain.setValueAt(list.get(9), row, frm.colBuyerManshon);								//購入者ビル・マンション名
		frm.teMain.setValueAt(list.get(10), row, frm.colBuyerMailAddress);							//購入者Eメール
		frm.teMain.setValueAt(list.get(11), row, frm.colBuyerTel);									//購入者電話番号

		if (Util.isNullOrEmpty(list.get(12))) {
			frm.teMain.setValueAt(list.get(3), row, frm.colUketoriJin);								//送付先氏名
			frm.teMain.setValueAt(list.get(4), row, frm.colUketoriJinKana);							//送付先かな
		} else {
			frm.teMain.setValueAt(list.get(12), row, frm.colUketoriJin);							//送付先氏名
			frm.teMain.setValueAt(list.get(13), row, frm.colUketoriJinKana);						//送付先かな
		}
		if (Util.isNullOrEmpty(list.get(13))) {
			frm.teMain.setValueAt(list.get(5), row, frm.colTodokesakiYubinno);						//送付先郵便番号
			frm.teMain.setValueAt(list.get(6), row, frm.colTodokesakiTodoufuken);					//送付先都道府県
			frm.teMain.setValueAt(list.get(7), row, frm.colTodokesakiSichoson);						//送付先市区町村
			frm.teMain.setValueAt(list.get(8), row, frm.colTodokesaki1);							//送付先番地
			frm.teMain.setValueAt(list.get(9), row, frm.colTodokesaki2);							//送付先ビル・マンション名
			frm.teMain.setValueAt(list.get(11), row, frm.colTodokesakiTel);							//送付先電話番号
		} else {
			frm.teMain.setValueAt(list.get(14), row, frm.colTodokesakiYubinno);						//送付先郵便番号
			frm.teMain.setValueAt(list.get(15), row, frm.colTodokesakiTodoufuken);					//送付先都道府県
			frm.teMain.setValueAt(list.get(16), row, frm.colTodokesakiSichoson);					//送付先市区町村
			frm.teMain.setValueAt(list.get(17), row, frm.colTodokesaki1);							//送付先番地
			frm.teMain.setValueAt(list.get(18), row, frm.colTodokesaki2);							//送付先ビル・マンション名
			frm.teMain.setValueAt(list.get(19), row, frm.colTodokesakiTel);							//送付先電話番号
		}
		frm.teMain.setValueAt(list.get(20), row, frm.colTitle);										//商品名
		frm.teMain.setValueAt(list.get(21), row, frm.colSku);										//SKU

		frm.teMain.setValueAt(list.get(22), row, frm.colSu);										//数量
		frm.teMain.setValueAt(list.get(23), row, frm.colHanbaiTanka);								//価格
		frm.teMain.setValueAt(list.get(24), row, frm.colHanbaiKin);									//販売金額
		frm.teMain.setValueAt(list.get(25), row, frm.colSoryo);										//送料
		frm.teMain.setValueAt(list.get(26), row, frm.colTesuryo);									//手数料
		frm.teMain.setValueAt(list.get(27), row, frm.colNebiki);									//値引き
		frm.teMain.setValueAt(list.get(28), row, frm.colUsePoint);									//ポイント利用
		frm.teMain.setValueAt(list.get(29), row, frm.colGokeiKin);									//合計金額
		frm.teMain.setValueAt(list.get(30), row, frm.colShiharaiHoho);								//決済方法
		frm.teMain.setValueAt(list.get(31), row, frm.colHaisohouhou);								//配送方法

		//商品情報
		frm.teMain.setValueAt(info.getStringItem("TANPINCD"), row, frm.colTanpinCd);				//単品ｺｰﾄﾞ
		frm.teMain.setValueAt(info.getStringItem("SHOHINCD"), row, frm.colShohinCd);				//商品ｺｰﾄﾞ
		frm.teMain.setValueAt(info.getStringItem("SHOHINNM"), row, frm.colShohinNm);				//商品名称
		frm.teMain.setValueAt(info.getStringItem("SHOHINKANANM"), row, frm.colShohinKanaNm);		//商品名称カナ
		frm.teMain.setValueAt(info.getStringItem("SHOHINNM2"), row, frm.colShohinNm2);				//商品名称2
		frm.teMain.setValueAt(info.getStringItem("SHOHINKANANM2"), row, frm.colShohinKanaNm2);		//商品名称2カナ
		frm.teMain.setValueAt(info.getStringItem("JANCD"), row, frm.colJanCd);						//JANｺｰﾄﾞ
		frm.teMain.setValueAt(info.getStringItem("KIKAKUNO"), row, frm.colKikakuNo);				//規格番号
		frm.teMain.setValueAt(info.getStringItem("HATUBAIDATE"), row, frm.colHatubaiDate);			//発売日
		if(!Util.isNullOrEmpty(info.getStringItem("TEIKA"))){
			frm.teMain.setValueAt(Util.getNumFormat(info.getStringItem("TEIKA")), row, frm.colTeika);//定価
		}
		frm.teMain.setValueAt(info.getStringItem("MAKERCD"), row, frm.colMakerCd);					//ﾒｰｶｰｺｰﾄﾞ
		frm.teMain.setValueAt(info.getStringItem("MAKERNM"), row, frm.colMakerNm);					//ﾒｰｶｰ名称
		frm.teMain.setValueAt(info.getStringItem("LABELCD"), row, frm.colLabelCd);					//ﾚｰﾍﾞﾙｺｰﾄﾞ
		frm.teMain.setValueAt(info.getStringItem("LABELNM"), row, frm.colLabelNm);					//ﾚｰﾍﾞﾙ名称
		frm.teMain.setValueAt(info.getStringItem("DAIBUNRUICD"), row, frm.colDaibunCd);				//大分類ｺｰﾄﾞ
		frm.teMain.setValueAt(info.getStringItem("DAIBUNRUINM"), row, frm.colDaibunNm);				//大分類名称
		frm.teMain.setValueAt(info.getStringItem("DAIBUNRUIRYAKUNM"), row, frm.colDaibunRyakuNm);	//大分類略称
		frm.teMain.setValueAt(info.getStringItem("CHUBUNRUICD"), row, frm.colChubunCd);				//中分類ｺｰﾄﾞ
		frm.teMain.setValueAt(info.getStringItem("CHUBUNRUINM"), row, frm.colChubunNm);				//中分類名称
		frm.teMain.setValueAt(info.getStringItem("CHUBUNRUIRYAKUNM"), row, frm.colChubunRyakuNm);	//中分類略称
		frm.teMain.setValueAt(info.getStringItem("SHOBUNRUICD"), row, frm.colShobunCd);				//小分類ｺｰﾄﾞ
		frm.teMain.setValueAt(info.getStringItem("SHOBUNRUINM"), row, frm.colShobunNm);				//小分類名称
		frm.teMain.setValueAt(info.getStringItem("SHOBUNRUIRYAKUNM"), row, frm.colShobunRyakuNm);	//小分類略称
		frm.teMain.setValueAt(info.getStringItem("KAKAKUKANRIRANK"), row, frm.colRankCd);			//価格管理ﾗﾝｸ
		frm.teMain.setValueAt(info.getStringItem("KAKAKUKANRIRANKNM"), row, frm.colRankNm);			//価格管理ﾗﾝｸ名称
	}

	/**
	 * 商品情報をｸﾘｱする
	 */
	private void clearShohinInfo(){
		frm.lblShohinNm.setText("");
		frm.lblTodokeSaki1.setText("");
		frm.lblTodokeSaki2.setText("");
		frm.lblTodokeSaki3.setText("");
		frm.lblTeika.setText("");
		frm.lblShohinNmKana.setText("");
		frm.lblShohinNm2.setText("");
		frm.lblHatubai.setText("");
		frm.lblMaker.setText("");
		frm.lblLabel.setText("");
		frm.lblDaibunrui.setText("");
		frm.lblChubunrui.setText("");
		frm.lblShobunrui.setText("");
		frm.lblTodoufuken.setText("");
		frm.lblSichoson.setText("");
	}

	/**
	 * 商品情報をﾗﾍﾞﾙに表示
	 */
	private void showShohinInfo( ){
		clearShohinInfo();
		int row = frm.teMain.getSelectedRow();
		if ( row < 0 || frm.teMain.getRowCount() == 0) return ;

		frm.lblShohinNm.setText(frm.teMain.getValueString(row, frm.colShohinNm));

		frm.lblHatubai.setText(frm.teMain.getValueString(row, frm.colHatubaiDate));
		frm.lblTeika.setText(frm.teMain.getValueString(row, frm.colTeika));
		frm.lblShohinNmKana.setText(frm.teMain.getValueString(row, frm.colShohinKanaNm));
		frm.lblShohinNm2.setText(frm.teMain.getValueString(row, frm.colShohinNm2));
		frm.lblMaker.setText(frm.teMain.getValueString(row, frm.colMakerCd)
				+ ":" + frm.teMain.getValueString(row, frm.colMakerNm));
		frm.lblDaibunrui.setText(frm.teMain.getValueString(row, frm.colDaibunCd)
				+ ":" + frm.teMain.getValueString(row, frm.colDaibunNm));
		frm.lblChubunrui.setText(frm.teMain.getValueString(row, frm.colChubunCd)
				+ ":" + frm.teMain.getValueString(row, frm.colChubunNm));
		frm.lblShobunrui.setText(frm.teMain.getValueString(row, frm.colShobunCd)
				+ ":" + frm.teMain.getValueString(row, frm.colShobunNm));
		frm.lblLabel.setText(frm.teMain.getValueString(row, frm.colLabelCd)
				+ ":" + frm.teMain.getValueString(row, frm.colLabelNm));
		frm.lblTodoufuken.setText(frm.teMain.getValueString(row, frm.colTodokesakiTodoufuken));
		frm.lblSichoson.setText(frm.teMain.getValueString(row, frm.colTodokesakiSichoson));
		frm.lblTodokeSaki1.setText(frm.teMain.getValueString(row, frm.colTodokesaki1));
		frm.lblTodokeSaki2.setText(frm.teMain.getValueString(row, frm.colTodokesaki2));
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
		appConfig.removeDisp(activeId);//2011/4/4 nomura
		messageBox = null;
	}

	/**
	 * ﾌｧﾝｸｼｮﾝｷｰの設定
	 */
	private void setFkeyEnabled( ){

		//ﾍｯﾀﾞｰにﾌｫｰｶｽがある場合
		switch( intInputMode ){
		case intInit:
			frm.fButton.butF1.setEnabled(true); 		//F1 ﾃﾞｰﾀ取込
			frm.fButton.butF2.setEnabled(false); 		//F2
			frm.fButton.butF3.setEnabled(false);		//F3
			frm.fButton.butF4.setEnabled(false); 		//F4
			frm.fButton.butF5.setEnabled(false); 		//F5
			frm.fButton.butF6.setEnabled(false); 		//F6
			frm.fButton.butF7.setEnabled(false); 		//F7
			frm.fButton.butF8.setEnabled(false); 		//F8
			frm.fButton.butF9.setText("F9 終了");
			frm.fButton.butF9.setEnabled(true); 		//F9 終了
			frm.fButton.butF10.setEnabled(false); 		//F10
			frm.fButton.butF11.setEnabled(false); 		//F11
			frm.fButton.butF12.setEnabled(false); 		//F12 確定

			break;

		case intShukka:
			frm.fButton.butF1.setEnabled(false); 		//F1 ﾃﾞｰﾀ取込
			frm.fButton.butF2.setEnabled(false); 		//F2
			frm.fButton.butF3.setEnabled(false);		//F3
			frm.fButton.butF4.setEnabled(false); 		//F4
			frm.fButton.butF5.setEnabled(false); 		//F5
			frm.fButton.butF6.setEnabled(false); 		//F6
			frm.fButton.butF7.setEnabled(false); 		//F7
			frm.fButton.butF8.setEnabled(false);		//F8
			frm.fButton.butF9.setText("F9 戻る");
			frm.fButton.butF9.setEnabled(true); 		//F9 戻る
			frm.fButton.butF10.setEnabled(false); 		//F10
			frm.fButton.butF11.setEnabled(false); 		//F11
			frm.fButton.butF12.setEnabled(true); 		//F12 確定
			break;
		}
	}

	/**
	 * CSV取込
	 */
	private void csvRead() {

		String strPath = "";
		String strFileName = "";
		JFileChooser parseDir = new JFileChooser();
		parseDir.setFileSelectionMode(JFileChooser.FILES_ONLY);
		parseDir.setCurrentDirectory(new File(""));
		String csvExts[] = { "csv" };
		GenericFileFilter filter = new GenericFileFilter(csvExts, "csvファイル(*.csv)");
		parseDir.addChoosableFileFilter(filter);
		parseDir.setDialogTitle("CSVﾌｧｲﾙを選択してください");

		int intRetVal = parseDir.showOpenDialog(frm);
		File file = null;
		if (intRetVal == JFileChooser.APPROVE_OPTION) {
			file = parseDir.getSelectedFile();
			strPath = file.getParent() + File.separator;
			strFileName = file.getName();
			strCsvFileName = strPath + strFileName;
		} else {
			strCsvFileName = "";
			return;
		}

		// CSVファイルではない場合
		if (strFileName.length() < 4
				|| !Util.rightBX(strFileName, 4).toLowerCase().equals(".csv")) {
			messageBox.disp(MB_EXCLAMATION, "CSVファイルを選択してください。", strMsgTitle);
			return;
		}
		// ﾌｧｲﾙの存在ﾁｪｯｸ
		if (file != null && !file.exists()) {
			messageBox.disp(MB_EXCLAMATION, strFileName + "が存在しません。", strMsgTitle);
			return;
		}

		try {

			// WaitBox初期化
			waitBox = new WaitBox(frm);
			waitBox.setLblValueVisible(true);
			waitBox.setProgressBarVisible(true);
			waitBox.setTitle("データの表示中");
			waitBox.start();
			Thread thread = new Thread() {
				public void run() {
					synchronized (this) {
						try {

							listErrCreate = new ArrayList<List<String>>();

							frm.teMain.setEnabled(false);

							// 行数の取得
							int count = 0;
							TCsvReader csvReader0 = null;
							try {
								InputStreamReader reader = new InputStreamReader(new FileInputStream(strCsvFileName), Util.EN_CODE_DEFAULT);
								csvReader0 = new TCsvReader(reader);
								count = csvReader0.getLineCount();
							} catch (IOException e) {
								throw e;
							} finally {
								if (csvReader0 != null) csvReader0.close();
							}

							waitBox.setMax(count);
							TCsvReader csvReader = null;
							int intLoop = 0;
							int intErrCnt = 0;
							int intOkCnt = 0;
							try {
								InputStreamReader reader = new InputStreamReader(
										new FileInputStream(strCsvFileName), Util.EN_CODE_DEFAULT);
								csvReader = new TCsvReader(reader);
								// ﾍｰﾀﾞ行を取り外す
								List<String> list = csvReader.readLineToColumnList();

								while ((list = csvReader.readLineToColumnList()) != null) {
									intLoop++;
									waitBox.setValue(intLoop);
									// ){//空白行の場合、ｽｷｯﾌﾟ
									if (list.size() == 1) {// 空白行の場合、ｽｷｯﾌﾟ
										if (Util.isNullOrEmpty(list.get(0))) {
											continue;
										}
									}
									// 両端の空白を削除する
									for (int i = 0; i < list.size(); i++) {
										if (!Util.isNullOrEmpty(list.get(i))) {
											list.set(i, list.get(i).toString().trim());
										}
									}
									if (chkCsvData(comH2, intLoop, list) != 0) {
										intErrCnt++;
									} else {
										addToTable(dbInfoTp, list);
										intOkCnt++;
									}
								}
							} catch (IOException e) {
								throw e;
							} finally {
								if (csvReader != null)
									csvReader.close();
							}

							frm.lblErrorCount.setText(Util.formatNumber(intErrCnt));
							frm.lblNormalCount.setText(Util.formatNumber(intOkCnt));

							//一番上にﾌｫｰｶｽをあてる
							if (frm.teMain.getRowCount() > 0){
								frm.teMain.addRowSelectionInterval(0,0);
								showShohinInfo();

								intInputMode = intShukka;
								setFkeyEnabled();

							}

							frm.jScrollPane.updateUI();

							frm.teMain.setEnabled(true);
							frm.teMain.requestFocus();


							waitBox.close();

							// ｴﾗｰﾃﾞｰﾀあり
							if (intErrCnt > 0) {
								try{
									String strPath = "C:\\Css\\webpos\\CSVエラーリスト\\";
									String strFileName = "注文取込CSVエラーリスト"+Util.getCurrentDateString()+".txt";
									TCsvCreate tCsvCreate = new TCsvCreate();
									tCsvCreate.createTextFileOfCSV(strPath, strFileName, listErrCreate, false);
									messageBox.disp(MB_INFORMATION, "取込エラーが発生しましたので、"
													+ "\nwebposフォルダにCSVエラーリストが出力されました。", strMsgTitle);

								} catch (TException ex) {
									messageBox.disp(ex, MB_CRITICAL, "注文データ取込ｴﾗｰﾃﾞｰﾀの保存でエラーが発生しました。"
											+ "\n" + ex.getMessage(), strMsgTitle);
								}
							}
						} catch (Exception te) {
							waitBox.close();
							messageBox.disp(te, MB_CRITICAL,
											"CSVファイルの取込でエラーが発生しました。"
													+ "\n"
													+ te.getMessage(),
													strMsgTitle);
						}
					}
				}
			};
			thread.start();

		} catch (Exception ex) {
			messageBox.disp(ex, MB_CRITICAL, "CSVファイルの取込でエラーが発生しました。"
					+ "\n" + ex.toString() + ex.getMessage(), strMsgTitle);
		}
	}

	/**
	 * 取り込んだ行ﾃﾞｰﾀのﾁｪｯｸを行う
	 *
	 * @param gyoNo
	 * @param list
	 * @return
	 */
	private int chkCsvData(H2 comH2, int gyoNo, List<String> list) throws TException {
		int intRet = -1;
		List<ColumnBean> columns = columnList;
		dbInfoShupin = null;

		// 項目数ﾁｪｯｸ
		if (list.size() < columns.size()) {
			// 項目を空で補填
			int intCount = columns.size() - list.size();
			for (int i = 0; i < intCount; i++) {
				list.add("");
			}
		} else if (list.size() > columns.size()) {
			// 項目数不一致ｴﾗｰ
			addErrorMessage(MessageFormat.format(CSV_ERR_MESSEGE_COL,
					new Object[] { gyoNo, list.size() }));
			return intRet;
		}

		ColumnBean column = null;
		String value = "";

		// SKU
		String strSku = list.get(21);
		// 注文サイト
		String strChumonSite = list.get(0);
		// 注文ID
		String strChumonNo = list.get(1);

		for (int i = 0; i < columns.size(); i++) {
			column = columns.get(i);

			// ﾁｪｯｸなし
			if (!column.isChecked)
				continue;

			value = (String) list.get(column.getIndex());
			if (value != null)
				value = value.trim();

			// 必須項目-空白ﾁｪｯｸ
			if (column.isRequired == 1 && Util.isNullOrEmpty(value)) {
				addErrorMessage(MessageFormat.format(CSV_ERR_MESSEGE_NUL
						, new Object[] { gyoNo, column.getName(), value
						, strChumonSite, strChumonNo, strSku}));
				return intRet;
			}

			if (Util.isNullOrEmpty(value))
				continue;

			// 桁数ﾁｪｯｸ
			if (value.length() > column.getMaxLength()) {
				addErrorMessage(MessageFormat.format(CSV_ERR_MESSEGE_KETA
						, new Object[] { gyoNo, column.getName(), value
						, strChumonSite, strChumonNo, strSku}));
				return intRet;
			}

			// ﾃﾞｰﾀﾀｲﾌﾟﾁｪｯｸ
			switch (column.getType()) {
			case TYPE_INTEGER:
			case TYPE_DOUBLE:
				// 数値ﾁｪｯｸ
				if (!Util.isNumber(value)) {
					addErrorMessage(MessageFormat.format(CSV_ERR_MESSEGE_NUM
							, new Object[] { gyoNo, column.getName(), value
							, strChumonSite, strChumonNo, strSku}));
					return intRet;
				}
				break;
			case TYPE_STRING:
				// 文字ﾁｪｯｸ
				if (!isValidString(value)) {
					addErrorMessage(MessageFormat.format(CSV_ERR_MESSEGE_STRING
							, new Object[] { gyoNo, column.getName(), value
							, strChumonSite, strChumonNo, strSku}));
					return intRet;
				}
				break;
			case TYPE_DATE_TIME:
				// 文字ﾁｪｯｸ
				if (!Util.isDateTime(value, "yyyy/MM/dd HH:mm:ss")) {
					addErrorMessage(MessageFormat.format(CSV_ERR_MESSEGE_DATE_TIME
							, new Object[] { gyoNo, column.getName(), value
							, strChumonSite, strChumonNo, strSku}));
					return intRet;
				}
				break;
			}

		}

		if (daoTimeShareShuupinList == null) {
			daoTimeShareShuupinList = new TimeShareShuupinListDAO(appConfig);
		}
		dbInfoShupin = daoTimeShareShuupinList.select(comH2, 9999, strSku);
		if (dbInfoShupin == null || dbInfoShupin.getMaxRowCount() == 0) {
			addErrorMessage(MessageFormat.format(CSV_ERR_MESSEGE_SHUPIN
					, new Object[] { gyoNo
					, strChumonSite, strChumonNo, strSku}));
			return intRet;
		}

		if (daoTimeShareChumonKanriReport == null) {
			daoTimeShareChumonKanriReport = new TimeShareChumonKanriReportDAO(appConfig);
		}
		DbInfo dbInfoReport = daoTimeShareChumonKanriReport.select(comH2, appConfig.getTenpoCd(), strChumonSite, strChumonNo, strSku);
		if (dbInfoReport.getMaxRowCount() != 0) {
			addErrorMessage(MessageFormat.format(CSV_ERR_MESSEGE_SHUKA
					, new Object[] { gyoNo, strChumonSite, strChumonNo, strSku }));
			return intRet;
		}

		if (daoTanpin == null) {
			daoTanpin = new TanpinMstDAO(appConfig);
		}
		String strTanpinCd = dbInfoShupin.getStringItem("TANPINCD");
		dbInfoTp = daoTanpin.selectTanpinInfo(comH2, appConfig.getTenpoCd(), dbInfoShupin.getStringItem("TANPINCD"));
		if (dbInfoTp.getMaxRowCount() == 0) {
			addErrorMessage(MessageFormat.format(CSV_ERR_MESSEGE_TANPIN
					+ " 単品コード：" + strTanpinCd, new Object[] { gyoNo }));
			return intRet;
		}

		for (int i = 0; i < frm.teMain.getRowCount(); i++) {
			if (strSku.equals(frm.teMain.getValueString(i, frm.colSku))
					&& strChumonNo.equals(frm.teMain.getValueString(i, frm.colChumonId))
					&& strChumonSite.equals(frm.teMain.getValueString(i, frm.colChumonSite))) {
				addErrorMessage(MessageFormat.format(CSV_ERR_MESSEGE_TANPINJYUFUKU
						, new Object[] { gyoNo, strChumonSite, strChumonNo, strSku }));
				return intRet;
			}
		}

		return 0;
	}

	// ｴﾗｰﾒｯｾｰｼﾞ
	private static String CSV_ERR_MESSEGE_COL = "{0}行目 項目数が指定のフォーマットと相違しています。CSVデータ項目数={1}";
	private static String CSV_ERR_MESSEGE_NUL = "{0}行目 必須項目に値がありません。項目名={1} 値={2} サイト名={3} 注文ＩＤ={4} SKU={5}";
	private static String CSV_ERR_MESSEGE_NUM = "{0}行目 数値が無効です。項目名={1} 値={2} サイト名={3} 注文ＩＤ={4} SKU={5}";
	private static String CSV_ERR_MESSEGE_STRING = "{0}行目 使用不可文字(カンマ,シングルクウォート,ダブルクウォート)が含まれています。項目名={1} 値={2} サイト名={3} 注文ＩＤ={4} SKU={5}";
	private static String CSV_ERR_MESSEGE_DATE_TIME = "{0}行目 日付が無効です。項目名={1} 値={2} サイト名={3} 注文ＩＤ={4} SKU={5}";
	private static String CSV_ERR_MESSEGE_KETA = "{0}行目 桁数がオーバーしています。項目名={1} 値={2} サイト名={3} 注文ＩＤ={4} SKU={5}";
	private static String CSV_ERR_MESSEGE_TANPIN = "{0}行目 在庫がありません。";
	private static String CSV_ERR_MESSEGE_SHUPIN = "{0}行目 出品データがありません。サイト名={1} 注文ＩＤ={2} SKU={3}";
	private static String CSV_ERR_MESSEGE_SHUKA = "{0}行目 注文サイト={1} 注文ID={2} SKU={3}のﾃﾞｰﾀはすでに取込しました。";
	private static String CSV_ERR_MESSEGE_TANPINJYUFUKU = "{0}行目 注文サイト{1} 注文ID={2} SKU={3}のﾃﾞｰﾀを2つ以上を設定できません。";

	// ﾃﾞｰﾀﾀｲﾌﾟ
	private final static int TYPE_INTEGER = 0;
	private final static int TYPE_DOUBLE = 1;
	private final static int TYPE_STRING = 2;
	private final static int TYPE_DATE_TIME = 3;

	// 列ｲﾝﾃﾞｯｸｽ
	private final static int CSS_COLUMN_SIZE = 32;

	private List<ColumnBean> columnList;

	/**
	 * 列ｵﾌﾞｼﾞｪｸﾄの作成
	 */
	private void initColumns() {
		int index = 0;
		columnList = new ArrayList<ColumnBean>(CSS_COLUMN_SIZE);
		// 出品リスト-列定義
		columnList.add(new ColumnBean(index++, "サイト名", 1, TYPE_STRING, 20,true));
		columnList.add(new ColumnBean(index++, "注文ＩＤ", 1, TYPE_STRING, 30, true));
		columnList.add(new ColumnBean(index++, "注文確定日", 0, TYPE_DATE_TIME, 19, true));
		columnList.add(new ColumnBean(index++, "購入者氏名", 1, TYPE_STRING, 50, true));
		columnList.add(new ColumnBean(index++, "購入者かな", 0, TYPE_STRING, 50, true));
		columnList.add(new ColumnBean(index++, "購入者郵便番号", 1, TYPE_STRING, 8, true));
		columnList.add(new ColumnBean(index++, "購入者都道府県", 1, TYPE_STRING, 8, true));
		columnList.add(new ColumnBean(index++, "購入者市区町村", 1, TYPE_STRING, 92, true));
		columnList.add(new ColumnBean(index++, "購入者番地", 1, TYPE_STRING, 100, true));
		columnList.add(new ColumnBean(index++, "購入者ビル・マンション名", 0, TYPE_STRING, 100, true));
		columnList.add(new ColumnBean(index++, "購入者Eメール", 0, TYPE_STRING, 100, true));
		columnList.add(new ColumnBean(index++, "購入者電話番号", 1, TYPE_STRING, 50, true));
		columnList.add(new ColumnBean(index++, "送付先氏名", 0, TYPE_STRING, 50, true));
		columnList.add(new ColumnBean(index++, "送付先かな", 0, TYPE_STRING, 50, true));
		columnList.add(new ColumnBean(index++, "送付先郵便番号", 0, TYPE_STRING, 8, true));
		columnList.add(new ColumnBean(index++, "送付先都道府県", 0, TYPE_STRING, 8, true));
		columnList.add(new ColumnBean(index++, "送付先市区町村", 0, TYPE_STRING, 92, true));
		columnList.add(new ColumnBean(index++, "送付先番地", 0, TYPE_STRING, 100, true));
		columnList.add(new ColumnBean(index++, "送付先ビル・マンション名", 0, TYPE_STRING, 100, true));
		columnList.add(new ColumnBean(index++, "送付先電話番号", 0, TYPE_STRING, 50, true));
		columnList.add(new ColumnBean(index++, "商品名", 0, TYPE_STRING, 256, true));
		columnList.add(new ColumnBean(index++, "SKU", 1, TYPE_STRING, 50, true));
		columnList.add(new ColumnBean(index++, "数量", 1, TYPE_INTEGER, 6, true));
		columnList.add(new ColumnBean(index++, "価格", 1, TYPE_INTEGER, 10, true));
		columnList.add(new ColumnBean(index++, "販売金額", 1, TYPE_INTEGER, 10, true));
		columnList.add(new ColumnBean(index++, "送料", 0, TYPE_INTEGER, 8, true));
		columnList.add(new ColumnBean(index++, "手数料", 0, TYPE_INTEGER, 8, true));
		columnList.add(new ColumnBean(index++, "値引き", 0, TYPE_INTEGER, 8, true));
		columnList.add(new ColumnBean(index++, "ポイント利用", 0, TYPE_INTEGER, 8, true));
		columnList.add(new ColumnBean(index++, "合計金額", 1, TYPE_INTEGER, 10, true));
		columnList.add(new ColumnBean(index++, "決済方法", 0, TYPE_STRING, 30, true));
		columnList.add(new ColumnBean(index++, "配送方法", 0, TYPE_STRING, 30, true));
	}

	static class ColumnBean {
		// ｲﾝﾃﾞｯｸｽ
		private int index;
		// 名称
		private String name;
		// 必須ﾌﾗｸﾞ
		private int isRequired;
		// 型
		private int type;
		// 最大長さ
		private int maxLength;
		// ﾁｪｯｸﾌﾗｸﾞ
		private boolean isChecked;
		// ﾏｽﾀﾁｪｯｸﾌﾗｸﾞ
		private boolean isMasterCheck;
		// ﾏｽﾀﾀｲﾌﾟ
		private int masterType;

		public ColumnBean(int index, String name, int isRequired, int type,
				int maxLength, boolean isChecked) {
			this.index = index;
			this.name = name;
			this.isRequired = isRequired;
			this.type = type;
			this.maxLength = maxLength;
			this.isChecked = isChecked;
			this.isMasterCheck = false;
			this.masterType = -1;
		}

		public ColumnBean(int index, String name, int isRequired, int type,
				int maxLength, boolean isChecked, boolean isMasterCheck,
				int masterType) {
			this(index, name, isRequired, type, maxLength, isChecked);
			this.isMasterCheck = isMasterCheck;
			this.masterType = masterType;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getRequired() {
			return isRequired;
		}

		public void setRequired(int isRequired) {
			this.isRequired = isRequired;
		}

		public int isType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public int getMaxLength() {
			return maxLength;
		}

		public void setMaxLength(int maxLength) {
			this.maxLength = maxLength;
		}

		public boolean isChecked() {
			return isChecked;
		}

		public void setChecked(boolean isChecked) {
			this.isChecked = isChecked;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public int getType() {
			return type;
		}

		public boolean isMasterCheck() {
			return isMasterCheck;
		}

		public void setMasterCheck(boolean isMasterCheck) {
			this.isMasterCheck = isMasterCheck;
		}

		public int getMasterType() {
			return masterType;
		}

		public void setMasterType(int masterType) {
			this.masterType = masterType;
		}
	}

	/**
	 * ｴﾗｰﾒｯｾｰｼﾞをﾘｽﾄに追加する
	 *
	 * @param message
	 */
	private void addErrorMessage(String message) {
		List<String> listErr = new ArrayList<String>();
		listErr.add(message);
		listErrCreate.add(listErr);
	}

	/**
	 * 文字のﾁｪｯｸ(「,」「'」「"」使用不可)
	 *
	 * @param value
	 * @return
	 */
	private boolean isValidString(String value) {
		if (!Util.isNullOrEmpty(value)) {
			if (value.indexOf(",") != -1 || value.indexOf("'") != -1
					|| value.indexOf("\"") != -1) {
				return false;
			}
		}
		return true;
	}

	static class ChumonId {

		// 注文サイト名
		String chumonSite;

		// 注文ID
		String chumonId;

		// 管理番号
		long kanriNo;

		public String getChumonSite() {
			return chumonSite;
		}

		public void setChumonSite(String chumonSite) {
			this.chumonSite = chumonSite;
		}

		public String getChumonId() {
			return chumonId;
		}

		public void setChumonId(String chumonId) {
			this.chumonId = chumonId;
		}

		public long getKanriNo() {
			return kanriNo;
		}

		public void setKanriNo(long kanriNo) {
			this.kanriNo = kanriNo;
		}

	}
}