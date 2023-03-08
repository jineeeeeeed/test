package jp.co.css.TTSA;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import jp.co.css.COMMON.SearchBunruiCommon;
import jp.co.css.COMMON.SearchCode;
import jp.co.css.COMMON.SearchLabel;
import jp.co.css.COMMON.SearchMaker;
import jp.co.css.COMMON.SearchTorihikisaki;
import jp.co.css.COMMON.ShohinSiteiEx;
import jp.co.css.COMMON.SortSelect;
import jp.co.css.base.AppConfig;
import jp.co.css.bean.DbInfo;
import jp.co.css.dao.ChubunruiMstDAO;
import jp.co.css.dao.DaibunruiMstDAO;
import jp.co.css.dao.LabelMstDAO;
import jp.co.css.dao.MakerMstDAO;
import jp.co.css.dao.RankMstDAO;
import jp.co.css.dao.ShobunruiMstDAO;
import jp.co.css.dao.TenpoHanyoMstDAO;
import jp.co.css.dao.TenpoKakuchoMstDAO;
import jp.co.css.dao.TorihikisakiMstDAO;
import jp.co.css.talos_l.bean.TimeShareShuupinJokenBean;
import jp.co.css.talos_l.util.Constants;
import jp.co.css.webpos.common.db.H2;
import jp.co.css.webpos.common.except.TException;
import jp.co.css.webpos.common.gui.FKey;
import jp.co.css.webpos.common.gui.FKeyAdapter;
import jp.co.css.webpos.common.gui.TCheckBox;
import jp.co.css.webpos.common.gui.TComboBox;
import jp.co.css.webpos.common.gui.TDateField;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.gui.TLabel;
import jp.co.css.webpos.common.gui.TLabel2;
import jp.co.css.webpos.common.gui.TLabel3;
import jp.co.css.webpos.common.gui.TLabel6;
import jp.co.css.webpos.common.gui.TNumericField;
import jp.co.css.webpos.common.gui.TRadioButton;
import jp.co.css.webpos.common.gui.TTextField;
import jp.co.css.webpos.common.gui.TTextValue;
import jp.co.css.webpos.common.log.LogOut;
import jp.co.css.webpos.common.message.MessageBoxValue;
import jp.co.css.webpos.common.message.MessageDispNew;
import jp.co.css.webpos.common.util.SendTabKeys;
import jp.co.css.webpos.common.util.Util;

/**
 * 　検索条件設定ﾎﾟｯﾌﾟｱｯﾌﾟ
 */
public class SearchShuupinJoken implements MessageBoxValue
{
	//ｱﾏｿﾞﾝ出品登録画面
	public final static int FRAMEID_TAMZ0100 = 1;
	//アマゾン価格/数量登録画面
	public final static int FRAMEID_TAMZ0200 = 2;

	private String strMsgTitle = "条件指定";		//ﾒｯｾｰｼﾞ用ﾀｲﾄﾙ
	//private String strErrTitle = ""; 			//ｴﾗｰﾒｯｾｰｼﾞ用処理ﾀｲﾄﾙ
    private JDialog dlg = null;
	private JPanel jContentPane = null;

	private int ans	= MB_CANCEL;				//どのﾎﾞﾀﾝが押されたかの戻り値

	public MessageDispNew messageBox=new MessageDispNew();

    private FKey fKey = null;
	private JPanel jPanelFkey = null;

	private JFrame frm;
	private AppConfig appConfig;
	private LogOut logOut;

	//ﾎﾟｯﾌﾟｱｯﾌﾟ関係
	private ShohinSiteiEx shohinSitei = null;				//商品指定ﾎﾟｯﾌﾟｱｯﾌﾟ
	private SearchMaker searchMaker = null;				//ﾒｰｶｰ検索画面
	private SearchLabel searchLabel = null;				//ﾗｰﾍﾞﾙ検索画面
	private SearchTorihikisaki searchSiire = null;		//仕入検索画面

	//ﾌｫｰｶｽの移動
	List<Component> compList;							//ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを集めたﾃｰﾌﾞﾙ
	SendTabKeys sendTab = new SendTabKeys(); 	 		//ﾀﾌﾞが止まるｺﾝﾎﾟｰﾈﾝﾄを取得するｸﾗｽ

	private int intNone = -1;
	private int intMaker = 0;							//ﾒｰｶｰ検索
	private int intLabel = 1;							//ﾗｰﾍﾞﾙ検索
	private int intSiire = 2;							//仕入先検索
	private int intSearchFlg = intNone;					//ﾃﾞﾌｫﾙﾄは検索無し

	//検索条件
	public JPanel panelInput = null;
	//商品指定
	private TLabel2 tlblShohinSitei = null;
	public TLabel6 lblShohinSitei = null;
	public TLabel2 lblShohinKey = null;

	//表示順序
	private TLabel2 tlblHyojiSort = null;
	public TLabel6 lblHyojiSort = null;

	//商品カナ名
	private JLabel lblShohinKanaNmWaku = null;
	private TLabel tlblShohinKanaNm = null;
	public TTextField txtShohinKanaNm = null;
	public TComboBox cboShohinKanaNm = null;

	//規格番号
	public TLabel tlblKikakuNo = null;
	public JLabel lblKikakuNoWaku = null;
	public TTextField txtKikakuNo = null;
	public TComboBox cboKikakuNo = null;

	//JANｺｰﾄﾞ
	public TLabel tlblJanCd = null;
	public JLabel lblJanCdWaku = null;
	public TNumericField txtJanCd = null;
	public TComboBox cboJanCd = null;

	//発売日
	private TLabel tlblHatubaiDate = null;
	private JLabel lblHatubaiDateWaku = null;
	private JLabel lblHatubaiDateKara = null;
	public TDateField dateHatubaiDateFrom = null;
	public TDateField dateHatubaiDateTo = null;
	public TComboBox cboHatubaiDate = null;
	public TNumericField txtHatubaiDate = null;
	private JLabel lblHatubaiDateIjou = null;

	//大分類ｺｰﾄﾞ
	public TLabel tlblDaibunruiCd = null;
	public JLabel lblDaibunruiCd = null;
//	public TComboBox scodeDaibunrui = null;
	public SearchCode scodeDaibunrui = null;//2016/03/24 YANGCHAO 障害NO:10320 add
	//中分類ｺｰﾄﾞ
	public TLabel tlblChubunruiCd = null;
	public JLabel lblChubunruiCd = null;
//	public TComboBox scodeChubunrui = null;
	public SearchCode scodeChubunrui = null;//2016/03/24 YANGCHAO 障害NO:10320 add
	//小分類ｺｰﾄﾞ
	public TLabel tlblShobunruiCd = null;
	public JLabel lblShobunruiCd = null;
//	public TComboBox scodeShobunrui = null;
	public SearchCode scodeShobunrui = null;//2016/03/24 YANGCHAO 障害NO:10320 add

	//ﾒｰｶｰｺｰﾄﾞ
	private TLabel tlblMakerCd = null;
	private JLabel lblMakerCdWaku = null;
	public TNumericField txtMakerCd = null;
	public TLabel3 lblMakerNm = null;

	//ﾗｰﾍﾞﾙｺｰﾄﾞ
	private TLabel tlblLabelCd = null;
	private JLabel lblLabelCdWaku = null;
	public TNumericField txtLabelCd = null;
	public TLabel3 lblLabelNm = null;

	//ランク
	private TLabel tlblRank = null;
	private JLabel lblRank = null;
	public TComboBox cboRank = null;

	//在庫数
	private TLabel tlblZaikoSu = null;
	private JLabel lblZaikoSuWaku = null;
	private JLabel lblZaikoSuKara = null;
	public TNumericField txtZaikoSuFrom = null;
	public TNumericField txtZaikoSuTo = null;

	//出品日
	private TLabel tlblShuppinDate = null;
	private JLabel lblShuppinDateWaku = null;
	private JLabel lblShuppinDateKara = null;
	public TDateField dateShuppinDateFrom = null;
	public TDateField dateShuppinDateTo = null;
	public TComboBox cboShuppinDate = null;
	public TNumericField txtShuppinDate = null;
	private JLabel lblShuppinDateIjou = null;

	//出品数
	private TLabel tlblShuppinSu = null;
	private JLabel lblShuppinSuWaku = null;
	private JLabel lblShuppinSuKara = null;
	public TNumericField txtShuppinSuFrom = null;
	public TNumericField txtShuppinSuTo = null;

	//出品状態
	private TLabel tlblShuppinJotai = null;
	private JLabel lblShuppinJotai  = null;
	public TComboBox cboShuppinJotai = null;

	//出品可否
	private TLabel tlblShuppinKahi = null;
	private JLabel lblShuppinKahiWaku = null;
	public TRadioButton btnShuppinKa = null;
	public TRadioButton btnShuppinFuka = null;
	public TRadioButton btnSaiShuppin = null;
	public TRadioButton btnShuppinAll = null;

	//取引条件
	private TLabel tlblMiShuppinJoken = null;
	private JLabel lblMiShuppinJokenWaku = null;
	public TCheckBox cbKaitori = null;
	public TCheckBox cbSiire = null;
	public TCheckBox cbNyuko = null;
	public TCheckBox cbFurikae = null;
	private JLabel lblTaishoDateKara = null;
	public TDateField dateTaishoDateFrom = null;
	public TDateField dateTaishoDateTo = null;

	private TLabel tlblTorihikiDenpyoNo = null;
	private JLabel lblTorihikiDenpyoNoWaku = null;
	private JLabel lblTorihikiDenpyoNoKara = null;
	public TNumericField txtTorihikiDenpyoNoFrom = null;
	public TNumericField txtTorihikiDenpyoNoTo = null;


	//未出品のみ
	private TLabel tlblMiShuppinNomi = null;
	private JLabel lblMiShuppinNomi  = null;
	public TCheckBox chkMiShuppinNomi = null;

	//仕入先コード
	private TLabel tlblSiireSakiCd = null;
	private JLabel lblSiireSakiCdWaku = null;
	public TNumericField txtSiireSakiCd = null;
	public TLabel3 lblSiireSakiNm = null;

	//店舗汎用ﾏｽﾀ情報
	TenpoHanyoMstDAO tenpoHanyoDao = null;
	DbInfo dbHanyo = null;

	//店舗拡張マスタ
	TenpoKakuchoMstDAO tkcDao = null;
	DbInfo tkcinfo = null;

	//価格管理ﾗﾝｸﾏｽﾀ情報
	RankMstDAO rankDao = null;
	DbInfo rkAll = null;

	//小分類ﾏｽﾀDAO
	ShobunruiMstDAO shobunruiDao = null;
	DbInfo shobunruiInfo = null;
	//中分類ﾏｽﾀDAO
	ChubunruiMstDAO chubunruiDao = null;
	DbInfo chubunruiInfo = null;
	//大分類ﾏｽﾀDAO
	DaibunruiMstDAO daibunruiDao = null;
	DbInfo daibunruiInfo = null;

	//ﾒｰｶｰﾏｽﾀDAO
	MakerMstDAO makerDao = null;
	DbInfo makerInfo = null;
	//レーベルマスタDAO
	LabelMstDAO labelDao = null;
	DbInfo labelInfo = null;
	//仕入先マスタDAO
	TorihikisakiMstDAO ssdao = null;
	DbInfo ssInfo = null;

	int currentShohinKey = -1;//-1:未指定 0:商品コード 1:JANコード 2:規格番号
	List<String> shohinKeyList = new ArrayList<String>();

	private String[][] STR_SORT_LIST = new String[][]{{"SHUPPINJOTAI","出品状態"},{"SHOHINCD","商品コード"},
			{"KAKAKUKANRIRANK","ランク"},{"JANCD","JANコード"},{"KIKAKUNO","規格番号"},{"HATUBAIDATE","発売日"}
			,{"SHUPPINDATE","出品日"}
			,{"MAXDENPYONO","伝票番号"}
		};
	private List<TTextValue> listSort = null;
	private String strSort = "KIKAKUNO";
	private String strInitSort = "規格番号(昇順)";

	private TimeShareShuupinJokenBean jokenBean = null;
	private int frameId = 0;
	public H2 comH2=null;	//上位より
	//ｺﾝｽﾄﾗｸﾀ
	public SearchShuupinJoken(H2 h2,JFrame frm, AppConfig appConfig){
		this.appConfig = appConfig;
		this.frm = frm;
		comH2=h2;
		this.logOut = appConfig.getLogOut();
		getDlg();
		messageBox = new MessageDispNew(dlg,logOut);

		init();
	}

	/**
	 * ﾒｲﾝ画面表示
	 */
	public int disp(TimeShareShuupinJokenBean bean, int frameId){
		this.jokenBean = bean;
		this.frameId = frameId;

		//表示順序の初期化
		initHyojiJunjo();
		initTextByJokenBean();
		setVisible();

		//ｶｰｿﾙの制御　※Enableの制御が終わってから設定して!!
		compList = sendTab.setCompList(getJContentPane().getComponents());
		dlg.setFocusTraversalPolicy(sendTab.setCustomFocus());

		dlg.setModal(true);			//ﾓｰﾀﾞﾙに
		dlg.setResizable(false);	//ｻｲｽﾞ変更不可
//		画面サイズ調整
		TGuiUtil.resizeWindow(dlg);
		dlg.setVisible(true);		//画面表示

		return ans;
	}

	private void init(){

		ButtonGroup group = new ButtonGroup();
		group.add(btnShuppinKa);
		group.add(btnShuppinFuka);
		group.add(btnSaiShuppin);
		group.add(btnShuppinAll);

		//ﾌｧﾝｸｼｮﾝｷｰﾘｽﾅ
		fKey.addFButtonListener(new ButtonListener());

		txtShohinKanaNm.addKeyListener(new TKeyListener());
		txtShohinKanaNm.addFocusListener(new TFocusListener());
		cboShohinKanaNm.addKeyListener(new TKeyListener());
		cboShohinKanaNm.addFocusListener(new TFocusListener());
		txtKikakuNo.addKeyListener(new TKeyListener());
		txtKikakuNo.addFocusListener(new TFocusListener());
		cboKikakuNo.addKeyListener(new TKeyListener());
		cboKikakuNo.addFocusListener(new TFocusListener());
		txtJanCd.addKeyListener(new TKeyListener());
		txtJanCd.addFocusListener(new TFocusListener());
		cboJanCd.addKeyListener(new TKeyListener());
		cboJanCd.addFocusListener(new TFocusListener());
		cboHatubaiDate.addKeyListener(new TKeyListener());
		cboHatubaiDate.addItemListener(new ItemChangeListener());
		dateHatubaiDateFrom.addKeyListener(new TKeyListener());
		dateHatubaiDateFrom.addFocusListener(new TFocusListener());
		dateHatubaiDateTo.addKeyListener(new TKeyListener());
		dateHatubaiDateTo.addFocusListener(new TFocusListener());
		txtHatubaiDate.addKeyListener(new TKeyListener());
		txtHatubaiDate.addFocusListener(new TFocusListener());
		scodeDaibunrui.addKeyListener(new TKeyListener());
		scodeDaibunrui.addFocusListener(new TFocusListener());
		scodeChubunrui.addKeyListener(new TKeyListener());
		scodeChubunrui.addFocusListener(new TFocusListener());
		scodeShobunrui.addKeyListener(new TKeyListener());
		scodeShobunrui.addFocusListener(new TFocusListener());
		txtMakerCd.addKeyListener(new TKeyListener());
		txtMakerCd.addFocusListener(new TFocusListener());
		txtLabelCd.addKeyListener(new TKeyListener());
		txtLabelCd.addFocusListener(new TFocusListener());
		cboRank.addKeyListener(new TKeyListener());
		cboRank.addFocusListener(new TFocusListener());
		txtZaikoSuFrom.addKeyListener(new TKeyListener());
		txtZaikoSuFrom.addFocusListener(new TFocusListener());
		txtZaikoSuTo.addKeyListener(new TKeyListener());
		txtZaikoSuTo.addFocusListener(new TFocusListener());
		cboShuppinDate.addKeyListener(new TKeyListener());
		cboShuppinDate.addItemListener(new ItemChangeListener());
		dateShuppinDateFrom.addKeyListener(new TKeyListener());
		dateShuppinDateFrom.addFocusListener(new TFocusListener());
		dateShuppinDateTo.addKeyListener(new TKeyListener());
		dateShuppinDateTo.addFocusListener(new TFocusListener());
		txtShuppinDate.addKeyListener(new TKeyListener());
		txtShuppinDate.addFocusListener(new TFocusListener());
		txtShuppinSuFrom.addKeyListener(new TKeyListener());
		txtShuppinSuFrom.addFocusListener(new TFocusListener());
		txtShuppinSuTo.addKeyListener(new TKeyListener());
		txtShuppinSuTo.addFocusListener(new TFocusListener());
		chkMiShuppinNomi.addKeyListener(new TKeyListener());
		chkMiShuppinNomi.addFocusListener(new TFocusListener());
		cboShuppinJotai.addKeyListener(new TKeyListener());
		cboShuppinJotai.addFocusListener(new TFocusListener());
		btnShuppinKa.addKeyListener(new TKeyListener());
		btnShuppinKa.addFocusListener(new TFocusListener());
		btnShuppinFuka.addKeyListener(new TKeyListener());
		btnShuppinFuka.addFocusListener(new TFocusListener());
		btnSaiShuppin.addKeyListener(new TKeyListener());
		btnSaiShuppin.addFocusListener(new TFocusListener());
		btnShuppinAll.addKeyListener(new TKeyListener());
		btnShuppinAll.addFocusListener(new TFocusListener());
		cbKaitori.addKeyListener(new TKeyListener());
		cbKaitori.addFocusListener(new TFocusListener());
		cbSiire.addKeyListener(new TKeyListener());
		cbSiire.addFocusListener(new TFocusListener());
		cbNyuko.addKeyListener(new TKeyListener());
		cbNyuko.addFocusListener(new TFocusListener());
		cbFurikae.addKeyListener(new TKeyListener());
		cbFurikae.addFocusListener(new TFocusListener());
		dateTaishoDateFrom.addKeyListener(new TKeyListener());
		dateTaishoDateFrom.addFocusListener(new TFocusListener());
		dateTaishoDateTo.addKeyListener(new TKeyListener());
		dateTaishoDateTo.addFocusListener(new TFocusListener());
		txtTorihikiDenpyoNoFrom.addKeyListener(new TKeyListener());
		txtTorihikiDenpyoNoFrom.addFocusListener(new TFocusListener());
		txtTorihikiDenpyoNoTo.addKeyListener(new TKeyListener());
		txtTorihikiDenpyoNoTo.addFocusListener(new TFocusListener());
		txtSiireSakiCd.addKeyListener(new TKeyListener());
		txtSiireSakiCd.addFocusListener(new TFocusListener());

		tkcDao = new TenpoKakuchoMstDAO(appConfig);
		try {
			fKey.setF11Enabled(false);
			tkcinfo = tkcDao.select(comH2, appConfig.getTenpoCd());
			if(tkcinfo.getMaxRowCount()>0){
				if (tkcinfo.getIntItem("RAKUTENJOKENSHUPPINFLG")==1){
					fKey.setF11Enabled(true);
				}
			}
		} catch (TException e1) {
			e1.printStackTrace();
		}
		//ﾗﾝｸｺﾝﾎﾞﾎﾞｯｸｽの設定
		setCboRank();
		//2016/03/24 YANGCHAO 障害NO:10320 start
//		//大分類ﾃﾞｰﾀの設定
//		setComboDaibunnrui();
//		//中分類ﾃﾞｰﾀの設定
//		setComboCyubunnrui();
//		//小分類ﾃﾞｰﾀの設定
//		setComboSyobunnrui();
		new SearchBunruiCommon(appConfig, comH2, scodeDaibunrui, scodeChubunrui, scodeShobunrui);
		//2016/03/24 YANGCHAO 障害NO:10320 end
		//出品状態の設定
		setComboShuppinJotai();
		// 大分類の値を変わる処理
		//2016/03/24 YANGCHAO 障害NO:10320 start
//		scodeDaibunrui.addItemListener(new java.awt.event.ItemListener() {
//			public void itemStateChanged(java.awt.event.ItemEvent e) {
//
//				if(scodeDaibunrui.getSelectedIndex()>0){
//					scodeChubunrui.setEnabled(true);
//					scodeShobunrui.setEnabled(true);
//				} else {
//					scodeChubunrui.setEnabled(false);
//					scodeShobunrui.setEnabled(false);
//					scodeChubunrui.setSelectedIndex(0);
//					scodeShobunrui.setSelectedIndex(0);
//				}
//				// ｶｰｿﾙの制御　※Enableの制御が終わってから設定して!!
//				compList = sendTab.setCompList(panelInput.getComponents());
//				dlg.setFocusTraversalPolicy(sendTab.setCustomFocus());
//
//			}
//		});
		//2016/03/24 YANGCHAO 障害NO:10320 end
		//商品名カナ検索条件の設定
		setCboShohinKanaNm();
		//検索条件値の初期化
		initText();
	}

	class ItemChangeListener implements ItemListener {

		public void itemStateChanged(ItemEvent e) {

			if ( e.getSource().equals(cboHatubaiDate) ){
				if ("1".equals(cboHatubaiDate.getSelectedItemValue().toString()) ){
					//日以上
					lblHatubaiDateKara.setVisible(false);
					dateHatubaiDateFrom.setVisible(false);
					dateHatubaiDateTo.setVisible(false);
					txtHatubaiDate.setVisible(true);
					lblHatubaiDateIjou.setVisible(true);
				}else{
					//範囲指定
					lblHatubaiDateKara.setVisible(true);
					dateHatubaiDateFrom.setVisible(true);
					dateHatubaiDateTo.setVisible(true);
					txtHatubaiDate.setVisible(false);
					lblHatubaiDateIjou.setVisible(false);
				}
			}else if( e.getSource().equals(cboShuppinDate)){
				if ("1".equals(cboShuppinDate.getSelectedItemValue().toString()) ){
					//日以上
					lblShuppinDateKara.setVisible(false);
					dateShuppinDateFrom.setVisible(false);
					dateShuppinDateTo.setVisible(false);
					txtShuppinDate.setVisible(true);
					lblShuppinDateIjou.setVisible(true);
				}else{
					//範囲指定
					lblShuppinDateKara.setVisible(true);
					dateShuppinDateFrom.setVisible(true);
					dateShuppinDateTo.setVisible(true);
					txtShuppinDate.setVisible(false);
					lblShuppinDateIjou.setVisible(false);
				}
			}
			// ｶｰｿﾙの制御 ※Enableの制御が終わってから設定して!!
			compList = sendTab.setCompList(panelInput.getComponents());
			frm.setFocusTraversalPolicy(sendTab.setCustomFocus());
		}
    }

	private void setVisible(){
		if ( frameId == FRAMEID_TAMZ0200 ){
			cboShuppinJotai.setSelectedItemValue(String.valueOf(Constants.AMAZON_JOTAIKB_SHUPPINZUMI));
			cboShuppinJotai.setEnabled(false);
			btnShuppinKa.setSelected(true);
			btnShuppinKa.setEnabled(false);
			btnShuppinFuka.setEnabled(false);
			btnSaiShuppin.setEnabled(false);
			btnShuppinAll.setEnabled(false);
		}
	}
	/**
	 * 検索項目値のｸﾘｱ
	 */
	public void initText(){
		lblShohinSitei.setText("");
		txtShohinKanaNm.setText("");
		cboShohinKanaNm.setSelectedIndex(0);
		txtKikakuNo.setText("");
		cboKikakuNo.setSelectedIndex(0);
		txtJanCd.setText("");
		dateHatubaiDateFrom.setText("");
		dateHatubaiDateTo.setText("");
		cboHatubaiDate.setSelectedIndex(0);
		txtHatubaiDate.setText("");
		cboJanCd.setSelectedIndex(0);
		//2016/03/24 YANGCHAO 障害NO:10320 start
//		if (scodeDaibunrui.getItemCount() > 0){
//			scodeDaibunrui.setSelectedIndex(0);
//		}
//		if (scodeChubunrui.getItemCount() > 0){
//			scodeChubunrui.setSelectedIndex(0);
//		}
//		if (scodeShobunrui.getItemCount() > 0){
//			scodeShobunrui.setSelectedIndex(0);
//		}
		scodeDaibunrui.clearValues();				// 大分類サーチコード　
		scodeChubunrui.clearValues();				// 中分類サーチコード　
		scodeShobunrui.clearValues();				// 小分類サーチコード　
		//2016/03/24 YANGCHAO 障害NO:10320 end
		txtMakerCd.setText("");
		lblMakerNm.setText("");
		txtLabelCd.setText("");
		lblLabelNm.setText("");
		cboRank.setSelectedIndex(0);
		txtZaikoSuFrom.setText("");
		txtZaikoSuTo.setText("");
		dateShuppinDateFrom.setText("");
		dateShuppinDateTo.setText("");
		cboShuppinDate.setSelectedIndex(0);
		txtShuppinDate.setText("");
		txtShuppinSuFrom.setText("");
		txtShuppinSuTo.setText("");
		if ( cboShuppinJotai.getItemCount() > 0 ){
			cboShuppinJotai.setSelectedIndex(0);
		}
		btnShuppinKa.setSelected(true);
		cbKaitori.setSelected(false);
		cbSiire.setSelected(false);
		cbNyuko.setSelected(false);
		cbFurikae.setSelected(false);
		dateTaishoDateFrom.setText("");
		dateTaishoDateTo.setText("");
		txtTorihikiDenpyoNoFrom.setText("");
		txtTorihikiDenpyoNoTo.setText("");
		txtSiireSakiCd.setText("");
		lblSiireSakiNm.setText("");
	}

	/**
	 * 出荷状態ｺﾝﾎﾞﾎﾞｯｸｽを設定する
	 */
	private void setComboShuppinJotai(){
		cboShuppinJotai.removeAllItems();

		TenpoHanyoMstDAO tenpoHanyoDao = null;
		DbInfo dbHanyo = null;
		try{
			tenpoHanyoDao = new TenpoHanyoMstDAO(appConfig);
			// ｱﾏｿﾞﾝ商品状態の取得
			dbHanyo = tenpoHanyoDao.select("AMAZON出品状態");
			for(int i=0; i<dbHanyo.getMaxRowCount(); i++){
				dbHanyo.setCurRow(i);
				if ( dbHanyo.getIntItem("CD") >= 0 && dbHanyo.getIntItem("CD") != Constants.AMAZON_JOTAIKB_SAISHUPPIN){
					cboShuppinJotai.addTextValueItem(dbHanyo.getStringItem("CD"),dbHanyo.getStringItem("CD") + ":" + dbHanyo.getStringItem("NM1"));
				}
			}
		} catch (TException ex) {
			messageBox.disp(ex, MB_CRITICAL, "店舗汎用データの読み込みでエラーが発生しました。" + ex.toString() + "\n" +
					ex.getMessage(),strMsgTitle);
		}
	}

	/**
	 *
	 */
	private void initTextByJokenBean(){
		if ( jokenBean == null ){
			cbKaitori.setSelected(true);
			dateTaishoDateFrom.setText(Util.convertToYYYYMMDD(Util.getCurrentDate()));
			dateTaishoDateTo.setText(Util.convertToYYYYMMDD(Util.getCurrentDate()));
			return ;
		}
		shohinKeyList = jokenBean.getListShohinValue();
		currentShohinKey = jokenBean.getShohinKey();
		showShohinSiteiInfo();

		if ( !Util.isNullOrEmpty(jokenBean.getSortValue()) ){
			listSort = jokenBean.getListSort();
			strSort = jokenBean.getSortValue();
			strInitSort = jokenBean.getSortName();
			lblHyojiSort.setText(strInitSort);
		}
		if ( !Util.isNullOrEmpty(jokenBean.getShohinKanaNm()) ){
			txtShohinKanaNm.setText(jokenBean.getShohinKanaNm());
			cboShohinKanaNm.setSelectedItemValue(String.valueOf(jokenBean.getShohinKanaNmKb()));
		}
		if ( !Util.isNullOrEmpty(jokenBean.getKikakuNo()) ){
			txtKikakuNo.setText(jokenBean.getKikakuNo());
			cboKikakuNo.setSelectedItemValue(String.valueOf(jokenBean.getKikakuNoKb()));
		}
		if ( !Util.isNullOrEmpty(jokenBean.getJanCd()) ){
			txtJanCd.setText(jokenBean.getJanCd());
			cboJanCd.setSelectedItemValue(String.valueOf(jokenBean.getJanCdKb()));
		}
		cboHatubaiDate.setSelectedItemValue(String.valueOf(jokenBean.getHatubaiDateKb()));
		if ("0".equals( String.valueOf(jokenBean.getHatubaiDateKb()) )){
			dateHatubaiDateFrom.setText(Util.convertToYYYYMMDD(jokenBean.getHatubaiDateFrom()));
			dateHatubaiDateTo.setText(Util.convertToYYYYMMDD(jokenBean.getHatubaiDateTo()));
		}else{
			if(jokenBean.getHatubaiDateIjou() != null){
				txtHatubaiDate.setText(jokenBean.getHatubaiDateIjou());
			}
		}
		//2016/03/24 YANGCHAO 障害NO:10320 start
//		if ( jokenBean.getDaibunruiCd() != null ){
//			scodeDaibunrui.setSelectedItemValue(String.valueOf(jokenBean.getDaibunruiCd()));
//		}
//		if ( jokenBean.getChubunruiCd() != null ){
//			scodeChubunrui.setSelectedItemValue(String.valueOf(jokenBean.getChubunruiCd()));
//		}
//		if ( jokenBean.getShobunruiCd() != null ){
//			scodeShobunrui.setSelectedItemValue(String.valueOf(jokenBean.getShobunruiCd()));
//		}
		if ( !Util.isNullOrEmpty(jokenBean.getDaibunruiCds())){
			scodeDaibunrui.setSelectCodes(String.valueOf(jokenBean.getDaibunruiCds()));
		}
		if ( !Util.isNullOrEmpty(jokenBean.getChubunruiCds())){
			scodeChubunrui.setSelectCodes(String.valueOf(jokenBean.getChubunruiCds()));
		}
		if ( !Util.isNullOrEmpty(jokenBean.getShobunruiCds())){
			scodeShobunrui.setSelectCodes(String.valueOf(jokenBean.getShobunruiCds()));
		}
		//2016/03/24 YANGCHAO 障害NO:10320 end
		if ( jokenBean.getMakerCd() != null ){
			txtMakerCd.setText(jokenBean.getMakerCd());
			lblMakerNm.setText(jokenBean.getMakerNm());
		}
		if ( jokenBean.getLabelCd() != null ){
			txtLabelCd.setText(jokenBean.getLabelCd());
			lblLabelNm.setText(jokenBean.getLabelNm());
		}
		if ( jokenBean.getRankCd() != null ){
			cboRank.setSelectedItemValue(String.valueOf(jokenBean.getRankCd()));
		}
		if ( jokenBean.getZaikoSuFrom() != null ){
			txtZaikoSuFrom.setText(Util.getCurFormat(jokenBean.getZaikoSuFrom()));
		}
		if ( jokenBean.getZaikoSuTo() != null ){
			txtZaikoSuTo.setText(Util.getCurFormat(jokenBean.getZaikoSuTo()));
		}
		cboShuppinDate.setSelectedItemValue(String.valueOf(jokenBean.getShuppinDateKb()));
		if ("0".equals( String.valueOf(jokenBean.getShuppinDateKb()) )){
			dateShuppinDateFrom.setText(Util.convertToYYYYMMDD(jokenBean.getShuppinDateFrom()));
			dateShuppinDateTo.setText(Util.convertToYYYYMMDD(jokenBean.getShuppinDateTo()));
		}else{
			if(jokenBean.getShuppinDateIjou() != null){
				txtShuppinDate.setText(jokenBean.getShuppinDateIjou());
			}
		}
		if (jokenBean.getShuppinSuFrom() != null ){
			txtShuppinSuFrom.setText(Util.getCurFormat(jokenBean.getShuppinSuFrom()));
		}
		if (jokenBean.getShuppinSuTo() != null){
			txtShuppinSuTo.setText(Util.getCurFormat(jokenBean.getShuppinSuTo()));
		}
		//if ( jokenBean.getShuppinJotai() != null ){
		//	cboShuppinJotai.setSelectedItemValue(String.valueOf(jokenBean.getShuppinJotai()));
		//}
		chkMiShuppinNomi.setSelected(jokenBean.isMiShuppinNomiFlg());
		btnShuppinAll.setSelected(true);
		if ( jokenBean.getShuppinKahi() != null ){
			if (jokenBean.getShuppinKahi() == Constants.AMAZON_SHUPPINKAHI_KA ){
				btnShuppinKa.setSelected(true);
			}else if ( jokenBean.getShuppinKahi() == Constants.AMAZON_SHUPPINKAHI_FUKA){
				btnShuppinFuka.setSelected(true);
			}else if ( jokenBean.getShuppinKahi() == Constants.AMAZON_SHUPPINKAHI_SAISHUPPIN){
				btnSaiShuppin.setSelected(true);
			}
		}

		cbKaitori.setSelected(jokenBean.isKaitoriFlg());
		cbSiire.setSelected(jokenBean.isSiireFlg());
		cbNyuko.setSelected(jokenBean.isNyukoFlg());
		cbFurikae.setSelected(jokenBean.isFurikaeFlg());
		dateTaishoDateFrom.setText(Util.convertToYYYYMMDD(jokenBean.getTaishoDateFrom()));
		dateTaishoDateTo.setText(Util.convertToYYYYMMDD(jokenBean.getTaishoDateTo()));
		txtTorihikiDenpyoNoFrom.setText(jokenBean.getTorihikiDenpyoNoFrom());
		txtTorihikiDenpyoNoTo.setText(jokenBean.getTorihikiDenpyoNoTo());

		if ( jokenBean.getSiireSakiCd() != null ){
			txtSiireSakiCd.setText(jokenBean.getSiireSakiCd());
			lblSiireSakiNm.setText(jokenBean.getSiireSakiNm());
		}
	}
	/**
	 * 表示順序の初期化
	 */
	private void initHyojiJunjo(){
		TTextValue textValue = null;
		strSort = "SHUPPINJOTAI,SHOHINCD,KAKAKUKANRIRANK";

		listSort = new ArrayList<TTextValue>();
		textValue = new TTextValue();
		textValue.setText("出品状態(昇順)");
		textValue.setValue("SHUPPINJOTAI");
		textValue.setValue1(String.valueOf(SortSelect.INT_SORT_ASC));
		listSort.add(textValue);
		textValue = new TTextValue();
		textValue.setText("商品コード(昇順)");
		textValue.setValue("SHOHINCD");
		textValue.setValue1(String.valueOf(SortSelect.INT_SORT_ASC));
		listSort.add(textValue);
		textValue = new TTextValue();
		textValue.setText("ランク(昇順)");
		textValue.setValue("KAKAKUKANRIRANK");
		textValue.setValue1(String.valueOf(SortSelect.INT_SORT_ASC));
		listSort.add(textValue);

		lblHyojiSort.setText(strInitSort);
	}

	private void showShohinSiteiInfo(){
		if ( shohinKeyList != null && shohinKeyList.size() >0){
			String value = "";
			for(int i=0; i<shohinKeyList.size(); i++){
				if ( i!= 0 ){
					value += " ";
				}
				value += shohinKeyList.get(i);
			}
			lblShohinSitei.setText(value);
			String shohinKey = "未指定";
			switch( currentShohinKey ){
			case 0:
				shohinKey = "商品コード";
				break;
			case 1:
				shohinKey = "JANコード";
				break;
			case 2:
				shohinKey = "規格番号";
				break;
			}
			lblShohinKey.setText(shohinKey);
		}else{
			currentShohinKey = -1;
			lblShohinKey.setText("未指定");
			lblShohinSitei.setText("");
		}
	}
	/**
	 * 画面を閉じる前の最後の処理　と　画面を閉じる処理
	 */
	private void DispClose(){
		//ログ出力
		logOut.info("画面【" + dlg.getTitle() + "】を閉じました。");
		dlg.dispose();
	}

	//ﾍｯﾀﾞｰ ﾃｷｽﾄ・ｺﾝﾎﾞ/ｷｰﾘｽﾅ
	class TKeyListener extends KeyAdapter{
		public void keyPressed(java.awt.event.KeyEvent e) {
			int ans = 0;
			//ｴﾝﾀｰｷｰ
			if( e.getKeyCode() == KeyEvent.VK_ENTER ){

				try{
					//ﾒｰｶｰｺｰﾄﾞ列
					if ( e.getSource().equals(txtMakerCd) ){
						lblMakerNm.setText("");
						if ( !Util.isNullOrEmpty(txtMakerCd.getText()) ){
							ans = getMaker(txtMakerCd.getTextInt());
							//データ有
							if( ans == 0 ){
								//メーカー名称の表示
								lblMakerNm.setText(makerInfo.getStringItem("MAKERRYAKUNM"));
								//データ無
							}else{
								messageBox.disp(MB_INFORMATION, "該当するメーカーが存在しません。\nコード="+txtMakerCd.getText(), strMsgTitle);
								lblMakerNm.setText("");
								txtMakerCd.setText("");
								return;
							}
						}
					}
					//ﾚｰﾍﾞﾙｺｰﾄﾞ列
					else if ( e.getSource().equals(txtLabelCd) ){
						lblLabelNm.setText("");
						if ( !Util.isNullOrEmpty(txtLabelCd.getText()) ){
							ans = getLabel(txtLabelCd.getTextInt());
							//データ有
							if( ans == 0 ){
								//ﾚｰﾍﾞﾙ名称の表示
								lblLabelNm.setText(labelInfo.getStringItem("LABELRYAKUNM"));
								//データ無
							}else{
								messageBox.disp(MB_INFORMATION, "該当するレーベルが存在しません。\nコード="+txtLabelCd.getText(), strMsgTitle);
								lblLabelNm.setText("");
								txtLabelCd.setText("");
								return;
							}
						}
					}
					//仕入先コード列
					else if ( e.getSource().equals(txtSiireSakiCd) ){
						lblSiireSakiNm.setText("");
						if (!Util.isNullOrEmpty(txtSiireSakiCd.getText())){
							ans = getSiireSaki(txtSiireSakiCd.getTextLong());
							//データ有
							if(ans == 0){
								//仕入先名称の表示
								lblSiireSakiNm.setText(ssInfo.getStringItem("TORIHIKISAKIRYAKUNM"));
							}else{
								//データ無し
								messageBox.disp(MB_INFORMATION, "該当する仕入先が存在しません。\nコード="+txtSiireSakiCd.getText(), strMsgTitle);
								lblSiireSakiNm.setText("");
								txtSiireSakiCd.setText("");
								return;
							}
						}
					}

				}catch(TException te){
					messageBox.disp(te, MB_CRITICAL, te.toString() + "\n" + te.getMessage(), strMsgTitle);
				}

				sendTab.SendTabKeys(e);
			}
		}
	}

	//ﾃｷｽﾄ・ｺﾝﾎﾞ/ﾌｫｰｶｽﾘｽﾅｰ
	class TFocusListener extends FocusAdapter{
		public void focusGained(java.awt.event.FocusEvent e) {
			//ﾒｰｶｰｺｰﾄﾞ
			if( e.getSource().equals(txtMakerCd)){
				intSearchFlg = intMaker;
				fKey.setF5Enabled(true);
			}
			//ﾗｰﾍﾞﾙｺｰﾄﾞ
			else if ( e.getSource().equals(txtLabelCd)){
				intSearchFlg = intLabel;
				fKey.setF5Enabled(true);
			}
			//仕入先コード
			else if ( e.getSource().equals(txtSiireSakiCd)){
				intSearchFlg = intSiire;
				fKey.setF5Enabled(true);
			}else{
				fKey.setF5Enabled(false);
			}
		}
	}

	class ButtonListener extends FKeyAdapter
	{
		//F1 商品指定
		public void f1Click(ActionEvent e) {
			shohinSitei = new ShohinSiteiEx(frm, appConfig, currentShohinKey, shohinKeyList);
			int ans = shohinSitei.disp();
			if (ans == MB_OK ) {
				currentShohinKey = shohinSitei.getShohinKeyKb();
				shohinKeyList = shohinSitei.getShohinKeyList();
			}else{
				currentShohinKey = -1;
				shohinKeyList = new ArrayList<String>();
			}
			showShohinSiteiInfo();
			txtShohinKanaNm.requestFocus();

		}

		//F2 表示順序
		public void f2Click(ActionEvent e) {
			SortSelect sortSelect = new SortSelect(frm, appConfig);
//			if (sortSelect.disp(STR_SORT_LIST, listSort) == MB_YES) {
//				listSort = sortSelect.getSelectList();
//				strSort = sortSelect.getSelectSort();
//				strInitSort = sortSelect.getSelectName();
//				lblHyojiSort.setText(strInitSort);
//			}
			//取引有り　かつ 倉庫管理無し
			if (appConfig.getSoukoKanriFlg() != 1 && (cbKaitori.isSelected() || cbSiire.isSelected()
					|| cbNyuko.isSelected() || cbFurikae.isSelected())) {
				if (sortSelect.disp(STR_SORT_LIST, listSort) == MB_YES) {
					listSort = sortSelect.getSelectList();
					strSort = sortSelect.getSelectSort();
					strInitSort = sortSelect.getSelectName();
					lblHyojiSort.setText(strInitSort);
				}
			} else {
				if (sortSelect.disp(STR_SORT_LIST, listSort,"伝票番号" ) == MB_YES) {
					listSort = sortSelect.getSelectList();
					strSort = sortSelect.getSelectSort();
					strInitSort = sortSelect.getSelectName();
					lblHyojiSort.setText(strInitSort);
				}
			}
			txtShohinKanaNm.requestFocus();
		}

		//F5　検索
		public void f5Click(ActionEvent e) {
			int ans;

			//メーカー検索
			if ( intSearchFlg == intMaker ){
				searchMaker = new SearchMaker(comH2,appConfig);
				ans = searchMaker.disp();
				switch( ans ){
				case MB_YES:
					//ﾒｰｶｰｺｰﾄﾞの設定
					txtMakerCd.setText(searchMaker.GetCd());
					break;

				case MB_CANCEL:
					//何もしない
					break;
				}
				txtMakerCd.requestFocus();
			}
			//レーベル検索
			else if ( intSearchFlg == intLabel ){
				if ( Util.isNullOrEmpty(txtMakerCd.getText()) ){
					searchLabel = new SearchLabel(comH2,appConfig);
				}else{
					searchLabel = new SearchLabel(comH2,appConfig, txtMakerCd.getTextInt());
				}

				ans = searchLabel.disp();
				switch( ans ){
				case MB_YES:
					//レーベルｺｰﾄﾞの設定
					txtLabelCd.setText(searchLabel.GetCd());
					break;
				case MB_CANCEL:
					//何もしない
					break;
				}
				txtLabelCd.requestFocus();
			}
			//仕入先検索
			else if ( intSearchFlg == intSiire ){

				searchSiire = new SearchTorihikisaki(comH2, frm, appConfig, appConfig.getTenpoCd());
				ans = searchSiire.disp();
				switch( ans ){
				case MB_YES:
					//仕入先ｺｰﾄﾞの設定
					txtSiireSakiCd.setText(searchSiire.GetTorihikisakiCd());
					break;
				case MB_CANCEL:
					//何もしない
					break;
				}
				txtSiireSakiCd.requestFocus();
			}
		}

		//F6　条件ｸﾘｱ
		public void f6Click(ActionEvent e) {
			initText();
			currentShohinKey = -1;
			shohinKeyList.clear();
			initHyojiJunjo();
			txtShohinKanaNm.requestFocus();

		}

		//F9 戻る
		public void f9Click(ActionEvent e) {
			ans = MB_CANCEL;
			DispClose();
		}

		//F11 出品
		public void f11Click(ActionEvent e) {

			if ( chkInputData() ){
				return ;
			}

			// 確認メッセージを表示
			if (messageBox.disp(MB_QUESTION, MB_YESNO, "出品します。よろしいですか?", strMsgTitle) != MB_YES){
				return;
			}

			//検索条件BEANの作成
			jokenBean = createJokenBean();
			jokenBean.setShuppinMode(TimeShareShuupinJokenBean.SHUPPIN_MODE_JIDOU);

			ans = MB_OK;
			DispClose();
		}

		//F12 条件実行
		public void f12Click(ActionEvent e) {

			if ( chkInputData() ){
				return ;
			}
			//検索条件BEANの作成
			jokenBean = createJokenBean();
			jokenBean.setShuppinMode(TimeShareShuupinJokenBean.SHUPPIN_MODE_SHUDOU);

			ans = MB_OK;
			DispClose();
		}
	}

	// コンボボックスを設定する
	private void setCboRank() {
		cboRank.removeAllItems();
		cboRank.addTextValueItem("", "  :全て");
		try {
			rankDao = new RankMstDAO(appConfig);
			rkAll = rankDao.select(comH2,(Integer)null);

			for( int j=0; j<rkAll.getMaxRowCount(); j++ ){
				rkAll.setCurRow(j);
				cboRank.addTextValueItem(rkAll.getStringItem("KAKAKUKANRIRANK"),
						rkAll.getStringItem("KAKAKUKANRIRANK") + ":" + rkAll.getStringItem("KAKAKUKANRIRANKNM"),
							rkAll.getStringItem("KAKAKUKANRIRANK"));
			}

			cboRank.setSelectedIndex(0);
		} catch (TException ex) {
			messageBox.disp(ex, MB_CRITICAL, "価格管理ランクマスタの読み込みでエラーが発生しました。" + ex.toString() + "\n" + ex.getMessage(),strMsgTitle);
		}
	}

//	/*
//	 * 大分類コンボボックスを設定する。
//	 */
//	private void setComboDaibunnrui() {
//		daibunruiDao = new DaibunruiMstDAO(appConfig);
//		scodeDaibunrui.removeAllItems();
//		try {
//			DbInfo info =  daibunruiDao.select(comH2);
//			scodeDaibunrui.addTextValueItem("-1", "00:指定なし", "指定なし");
//			for( int j=0; j<info.getMaxRowCount(); j++ ){
//				info.setCurRow(j);
//				scodeDaibunrui.addTextValueItem(info.getStringItem("DAIBUNRUICD"),
//						String.format("%02d", info.getIntItem("DAIBUNRUICD")) + ":" + info.getStringItem("DAIBUNRUINM"),
//						info.getStringItem("DAIBUNRUINM"));
//			}
//		} catch (TException ex) {
//			messageBox.disp(ex, MB_CRITICAL, "大分類データの読み込みでエラーが発生しました。" + ex.toString() + "\n" +
//					ex.getMessage(),strMsgTitle);
//		}
//	}
//
//	/*
//	 * 中分類コンボボックスを設定する。
//	 */
//	private void setComboCyubunnrui() {
//		chubunruiDao = new ChubunruiMstDAO(appConfig);
//		DbInfo info = null;
//		scodeChubunrui.removeAllItems();
//		try {
//			info =  chubunruiDao.select(comH2);
//			scodeChubunrui.addTextValueItem("-1", "0000:指定なし", "指定なし");
//			for( int j=0; j<info.getMaxRowCount(); j++ ){
//				info.setCurRow(j);
//				scodeChubunrui.addTextValueItem(info.getStringItem("CHUBUNRUICD"),
//						String.format("%04d", info.getIntItem("CHUBUNRUICD")) + ":" + info.getStringItem("CHUBUNRUINM"),
//						info.getStringItem("CHUBUNRUINM"));
//			}
//		} catch (TException ex) {
//			messageBox.disp(ex, MB_CRITICAL, "中分類データの読み込みでエラーが発生しました。" + ex.toString() + "\n" +
//					ex.getMessage(),strMsgTitle);
//		}
//	}
//
//	/*
//	 * 小分類コンボボックスを設定する。
//	 */
//	private void setComboSyobunnrui() {
//		shobunruiDao = new ShobunruiMstDAO(appConfig);
//		DbInfo info;
//		scodeShobunrui.removeAllItems();
//		try {
//			info =  shobunruiDao.select(comH2);
//			scodeShobunrui.addTextValueItem("-1", "000000:指定なし", "指定なし");
//			for( int j=0; j<info.getMaxRowCount(); j++ ){
//				info.setCurRow(j);
//				scodeShobunrui.addTextValueItem(info.getStringItem("SHOBUNRUICD"),
//						String.format("%06d", info.getIntItem("SHOBUNRUICD")) + ":" + info.getStringItem("SHOBUNRUINM"),
//						info.getStringItem("SHOBUNRUINM"));
//			}
//		} catch (TException ex) {
//			messageBox.disp(ex, MB_CRITICAL, "小分類データの読み込みでエラーが発生しました。" + ex.toString() + "\n" +
//					ex.getMessage(),strMsgTitle);
//		}
//	}


	/**
	 * 商品名カナ検索条件の設定
	 */
	private void setCboShohinKanaNm(){
		cboShohinKanaNm.removeAllItems();
		cboShohinKanaNm.addTextValueItem("0", "から始まる");
		cboShohinKanaNm.addTextValueItem("1", "を含む");

		cboKikakuNo.removeAllItems();
		cboKikakuNo.addTextValueItem("0", "から始まる");
		cboKikakuNo.addTextValueItem("1", "を含む");

		cboJanCd.removeAllItems();
		cboJanCd.addTextValueItem("0", "から始まる");
		cboJanCd.addTextValueItem("1", "を含む");

		cboHatubaiDate.removeAllItems();
		cboHatubaiDate.addTextValueItem("0", "範囲指定");
		cboHatubaiDate.addTextValueItem("1", "経過日数");

		cboShuppinDate.removeAllItems();
		cboShuppinDate.addTextValueItem("0", "範囲指定");
		cboShuppinDate.addTextValueItem("1", "経過日数");
	}

	/**
	 * @param makerCd
	 * @return
	 */
	private int getMaker(Integer makercd){
		try{
			makerDao = new MakerMstDAO(appConfig);
			makerInfo = makerDao.select(comH2,makercd);

			if ( makerInfo == null || makerInfo.getMaxRowCount() == 0){
				return 1;
			}
			return 0;
		}catch(TException ex){
			messageBox.disp(ex, MB_CRITICAL, "メーカーマスタの取得でエラーが発生しました。[getMaker]"+"\n"+ex.getMessage(), strMsgTitle);
			return -1;
		}
	}

	/**
	 * @param labelCd
	 * @return
	 */
	private int getLabel(Integer labelcd){
		try{
			labelDao = new LabelMstDAO(appConfig);
			labelInfo = labelDao.select(comH2,labelcd);

			if ( labelInfo == null || labelInfo.getMaxRowCount() == 0){
				return 1;
			}
			return 0;
		}catch(TException ex){
			messageBox.disp(ex, MB_CRITICAL, "レーベルマスタの取得でエラーが発生しました。[getLabel]"+"\n"+ex.getMessage(), strMsgTitle);
			return -1;
		}
	}

	/**
	 * @param SiireSakiCd
	 * @return
	 */
	private int getSiireSaki(long siiresakicd){
		try{
			ssdao = new TorihikisakiMstDAO(appConfig);
			ssInfo = ssdao.select(comH2, appConfig.getTenpoCd() ,siiresakicd);

			if ( ssInfo == null || ssInfo.getMaxRowCount() == 0){
				return 1;
			}
			return 0;
		}catch(TException ex){
			messageBox.disp(ex, MB_CRITICAL, "仕入先マスタの取得でエラーが発生しました。[getSiireSaki]"+"\n"+ex.getMessage(), strMsgTitle);
			return -1;
		}
	}

	/**
	 * 入力ﾃﾞｰﾀ有効性のﾁｪｯｸ
	 * @return
	 */
	private boolean chkInputData(){

		//発売日
		if ("0".equals(cboHatubaiDate.getSelectedItemValue().toString()) ){

			if( !Util.isNullOrEmpty(dateHatubaiDateFrom.getText()) ){
				if( Util.isDate(dateHatubaiDateFrom.getText()) == false ){
					messageBox.disp(MB_EXCLAMATION, "発売日(から)の指定が正しくありません。", strMsgTitle);
					dateHatubaiDateFrom.requestFocus();
					return true;
				}
			}
			if( !Util.isNullOrEmpty(dateHatubaiDateTo.getText()) ){
				if( Util.isDate(dateHatubaiDateTo.getText()) == false ){
					messageBox.disp(MB_EXCLAMATION, "発売日(まで)の指定が正しくありません。", strMsgTitle);
					dateHatubaiDateTo.requestFocus();
					return true;
				}
			}
			if ( !Util.isNullOrEmpty(dateHatubaiDateFrom.getText()) &&
			    		!Util.isNullOrEmpty(dateHatubaiDateTo.getText())){

			    //大小ﾁｪｯｸ
			    if (dateHatubaiDateFrom.getTextInteger() > dateHatubaiDateTo.getTextInteger()){
		    		messageBox.disp(MB_EXCLAMATION, "発売日の設定が間違っています。", strMsgTitle);
		    		dateHatubaiDateFrom.requestFocus();
		    		return true;
			    }
			}
		}

	    //在庫数
	    if (txtZaikoSuFrom.getText().equals("") != true &&
	    		txtZaikoSuTo.getText().equals("") != true){
	    	//大小ﾁｪｯｸ
	    	if (txtZaikoSuFrom.getTextInt() > txtZaikoSuTo.getTextInt() ){
	    			messageBox.disp(MB_EXCLAMATION, "在庫数の設定が間違っています。", strMsgTitle);
	    			txtZaikoSuFrom.requestFocus();
	    			return true;
	    	}
	    }

	    //出品日
	    if ("0".equals(cboShuppinDate.getSelectedItemValue().toString()) ){
			if( !Util.isNullOrEmpty(dateShuppinDateFrom.getText()) ){
				if( Util.isDate(dateShuppinDateFrom.getText()) == false ){
					messageBox.disp(MB_EXCLAMATION, "出品日(から)の指定が正しくありません。", strMsgTitle);
					dateShuppinDateFrom.requestFocus();
					return true;
				}
			}
			if( !Util.isNullOrEmpty(dateShuppinDateTo.getText()) ){
				if( Util.isDate(dateShuppinDateTo.getText()) == false ){
					messageBox.disp(MB_EXCLAMATION, "出品日(まで)の指定が正しくありません。", strMsgTitle);
					dateShuppinDateTo.requestFocus();
					return true;
				}
			}
		    if (!Util.isNullOrEmpty(dateShuppinDateFrom.getText()) &&
		    		!Util.isNullOrEmpty(dateShuppinDateTo.getText())){

		    	//大小ﾁｪｯｸ
		    	if (dateShuppinDateFrom.getTextInteger() > dateShuppinDateTo.getTextInteger()){
	    			messageBox.disp(MB_EXCLAMATION, "出品日の設定が間違っています。", strMsgTitle);
	    			dateShuppinDateFrom.requestFocus();
	    			return true;
		    	}
		    }
	    }

	    //出品数
	    if (txtShuppinSuFrom.getText().equals("") != true &&
	    		txtShuppinSuTo.getText().equals("") != true){
	    	//大小ﾁｪｯｸ
	    	if (txtShuppinSuFrom.getTextInt() > txtShuppinSuTo.getTextInt() ){
	    			messageBox.disp(MB_EXCLAMATION, "出品数の設定が間違っています。", strMsgTitle);
	    			txtShuppinSuFrom.requestFocus();
	    			return true;
	    	}
	    }

//	    //出品状態
//	    if ( cboShuppinJotai.getItemCount()== 0 ){
//	    	messageBox.disp(MB_EXCLAMATION, "出品状態を選択してください。", strMsgTitle);
//	    	cboShuppinJotai.requestFocus();
//	    	return true;
//	    }

	    //取引日付
	    if ( cbKaitori.isSelected() || cbSiire.isSelected() || cbNyuko.isSelected() || cbFurikae.isSelected() ){
	    	if ( Util.isNullOrEmpty(dateTaishoDateFrom.getText()) &&
	    			Util.isNullOrEmpty(dateTaishoDateTo.getText()) ){
	    		messageBox.disp(MB_EXCLAMATION, "取引日付を入力してください。", strMsgTitle);
	    		dateTaishoDateFrom.requestFocus();
	    		return true;
	    	}
	    }

	    if( !Util.isNullOrEmpty(dateTaishoDateFrom.getText()) ){
			if( Util.isDate(dateTaishoDateFrom.getText()) == false ){
				messageBox.disp(MB_EXCLAMATION, "取引日付(から)の指定が正しくありません。", strMsgTitle);
				dateTaishoDateFrom.requestFocus();
				return true;
			}
		}
		if( !Util.isNullOrEmpty(dateTaishoDateTo.getText()) ){
			if( Util.isDate(dateTaishoDateTo.getText()) == false ){
				messageBox.disp(MB_EXCLAMATION, "取引日付(まで)の指定が正しくありません。", strMsgTitle);
				dateTaishoDateTo.requestFocus();
				return true;
			}
		}
	    if (!Util.isNullOrEmpty(dateTaishoDateFrom.getText()) &&
	    		!Util.isNullOrEmpty(dateTaishoDateTo.getText())){

	    	//大小ﾁｪｯｸ
	    	if (dateTaishoDateFrom.getTextInteger() > dateTaishoDateTo.getTextInteger()){
    			messageBox.disp(MB_EXCLAMATION, "取引日付の設定が間違っています。", strMsgTitle);
    			dateTaishoDateFrom.requestFocus();
    			return true;
	    	}
	    }

	    //取引伝票番号
	    if (txtTorihikiDenpyoNoFrom.getText().equals("") != true &&
	    		txtTorihikiDenpyoNoTo.getText().equals("") != true){
	    	//大小ﾁｪｯｸ
	    	if (txtTorihikiDenpyoNoFrom.getTextInt() > txtTorihikiDenpyoNoTo.getTextInt() ){
	    			messageBox.disp(MB_EXCLAMATION, "取引伝票番号範囲指定が不正です。", strMsgTitle);
	    			txtTorihikiDenpyoNoFrom.requestFocus();
	    			return true;
	    	}
	    }

		return false;
	}

	private TimeShareShuupinJokenBean createJokenBean(){
		TimeShareShuupinJokenBean result = new TimeShareShuupinJokenBean();

		//画面ID
		result.setFrameId(frameId);
		//店舗ｺｰﾄﾞ
		result.setTenpoCd(appConfig.getTenpoCd());
		//商品ｷｰ区分
		result.setShohinKey(currentShohinKey);
		//商品ｷｰ値
		List<String> keyList = new ArrayList<String>();
		keyList.addAll(shohinKeyList);
		if (currentShohinKey == 2 ){//規格番号の場合
			for(int i=0; i<shohinKeyList.size(); i++){
				String key = shohinKeyList.get(i);
				String kikakuNo = Util.chgKikakuNO(key);
				if ( !kikakuNo.equals(key) ){
					keyList.add(kikakuNo);
				}
			}
		}
		result.setListShohinValue(keyList);

		//取引無しが条件のときは伝票番号を除く
		if (appConfig.getSoukoKanriFlg() != 1 && cbKaitori.isSelected() == false && cbSiire.isSelected() == false
				&& cbNyuko.isSelected() == false && cbFurikae.isSelected() == false) {
			for (int i = 0; i <listSort.size(); i++) {
				if (listSort.get(i).getText().equals("伝票番号(昇順)") || listSort.get(i).getText().equals("伝票番号(降順)")) {
					listSort.remove(i);
					break;
				}
			}
			//取引無しのときはSQL条件から伝票番号を除く
			strSort = strSort.replace(",MAXDENPYONO ASC", "");
			strSort = strSort.replace(",MAXDENPYONO DESC", "");
			strSort = strSort.replace("MAXDENPYONO ASC,", "");
			strSort = strSort.replace("MAXDENPYONO DESC,", "");
			strSort = strSort.replace("MAXDENPYONO ASC", "");
			strSort = strSort.replace("MAXDENPYONO DESC", "");

			strInitSort = strInitSort.replace(",伝票番号(昇順)", "");
			strInitSort = strInitSort.replace(",伝票番号(降順)", "");
			strInitSort = strInitSort.replace("伝票番号(昇順),", "");
			strInitSort = strInitSort.replace("伝票番号(降順),", "");
			strInitSort = strInitSort.replace("伝票番号(昇順)", "");
			strInitSort = strInitSort.replace("伝票番号(降順)", "");
		}

		//表示順序ﾘｽﾄ
		result.setListSort(listSort);
		//表示順序
		result.setSortValue(strSort);
		//表示順序名称
		result.setSortName(strInitSort);
		//商品名称ｶﾅ
		result.setShohinKanaNm(txtShohinKanaNm.getText().trim());
		result.setShohinKanaNmKb(Integer.parseInt(cboShohinKanaNm.getSelectedItemValue().toString()));
		//規格番号
		result.setKikakuNo(txtKikakuNo.getText().trim());
		result.setKikakuNoKb(Integer.parseInt(cboKikakuNo.getSelectedItemValue().toString()));
		//JANｺｰﾄﾞ
		result.setJanCd(txtJanCd.getText().trim());
		result.setJanCdKb(Integer.parseInt(cboJanCd.getSelectedItemValue().toString()));

		//発売日
		result.setHatubaiDateKb(Integer.parseInt(cboHatubaiDate.getSelectedItemValue().toString()));
		if ("0".equals(cboHatubaiDate.getSelectedItemValue().toString()) ){
			//発売日FROM
			if ( !Util.isNullOrEmpty(dateHatubaiDateFrom.getText().trim()) ){
				result.setHatubaiDateFrom(dateHatubaiDateFrom.getTextDate());
			}else{
				result.setHatubaiDateFrom(null);
			}
			//発売日To
			if ( !Util.isNullOrEmpty(dateHatubaiDateTo.getText().trim()) ){
				result.setHatubaiDateTo(dateHatubaiDateTo.getTextDate());
			}else{
				result.setHatubaiDateTo(null);
			}
		}else{
			//発売日（日以上）
			if ( !Util.isNullOrEmpty(txtHatubaiDate.getText().trim()) ){
				result.setHatubaiDateIjou(txtHatubaiDate.getTextInt());
			}else{
				result.setHatubaiDateIjou(null);
			}
		}
		//2016/03/24 YANGCHAO 障害NO:10320 start
//		//大分類コード
//		if ( scodeDaibunrui.getSelectedIndex() > 0 ){
//			result.setDaibunruiCd(scodeDaibunrui.getSelectedItemNum());
//		}else{
//			result.setDaibunruiCd(null);
//		}
//		//中分類コード
//		if ( scodeChubunrui.getSelectedIndex() > 0 ){
//			result.setChubunruiCd(scodeChubunrui.getSelectedItemNum());
//		}else{
//			result.setChubunruiCd(null);
//		}
//		//小分類コード
//		if ( scodeShobunrui.getSelectedIndex() > 0 ){
//			result.setShobunruiCd(scodeShobunrui.getSelectedItemNum());
//		}else{
//			result.setShobunruiCd(null);
//		}
		//大分類コード
		result.setDaibunruiCds(scodeDaibunrui.getSelectCodes());
		//中分類コード
		result.setChubunruiCds(scodeChubunrui.getSelectCodes());
		//小分類コード
		result.setShobunruiCds(scodeShobunrui.getSelectCodes());
		//2016/03/24 YANGCHAO 障害NO:10320 end
		//ﾒｰｶｰｺｰﾄﾞ
		if ( !Util.isNullOrEmpty(txtMakerCd.getText().trim()) ){
			result.setMakerCd(txtMakerCd.getTextLong());
			result.setMakerNm(lblMakerNm.getText());
		}else{
			result.setMakerCd(null);
		}
		//ﾗｰﾍﾞﾙｺｰﾄﾞ
		if ( !Util.isNullOrEmpty(txtLabelCd.getText().trim()) ){
			result.setLabelCd(txtLabelCd.getTextLong());
			result.setLabelNm(lblLabelNm.getText());
		}else{
			result.setLabelCd(null);
		}
		//ﾗﾝｸ
		if ( cboRank.getSelectedIndex() > 0 ){
			result.setRankCd(cboRank.getSelectedItemNum());
		}else{
			result.setRankCd(null);
		}
		//在庫数FROM
		if ( !Util.isNullOrEmpty(txtZaikoSuFrom.getText().trim()) ){
			result.setZaikoSuFrom(txtZaikoSuFrom.getTextInt());
		}else{
			result.setZaikoSuFrom(null);
		}
		//在庫数To
		if ( !Util.isNullOrEmpty(txtZaikoSuTo.getText().trim()) ){
			result.setZaikoSuTo(txtZaikoSuTo.getTextInt());
		}else{
			result.setZaikoSuTo(null);
		}
		//出品日
		result.setShuppinDateKb(Integer.parseInt(cboShuppinDate.getSelectedItemValue().toString()));
		if ("0".equals(cboShuppinDate.getSelectedItemValue().toString()) ){
			//出品日FROM
			if ( !Util.isNullOrEmpty(dateShuppinDateFrom.getText().trim()) ){
				result.setShuppinDateFrom(dateShuppinDateFrom.getTextDate());
			}else{
				result.setShuppinDateFrom(null);
			}
			//出品日To
			if ( !Util.isNullOrEmpty(dateShuppinDateTo.getText().trim()) ){
				result.setShuppinDateTo(dateShuppinDateTo.getTextDate());
			}else{
				result.setShuppinDateTo(null);
			}
		}else{
			//出品日（日以上）
			if ( !Util.isNullOrEmpty(txtShuppinDate.getText().trim()) ){
				result.setShuppinDateIjou(txtShuppinDate.getTextInt());
			}else{
				result.setShuppinDateIjou(null);
				result.setShuppinDateKb(0);//No.11418 2016/12/02 CF add
			}
		}
		//出品数FROM
		if ( !Util.isNullOrEmpty(txtShuppinSuFrom.getText().trim()) ){
			result.setShuppinSuFrom(txtShuppinSuFrom.getTextInt());
		}else{
			result.setShuppinSuFrom(null);
		}
		//出品数To
		if ( !Util.isNullOrEmpty(txtShuppinSuTo.getText().trim()) ){
			result.setShuppinSuTo(txtShuppinSuTo.getTextInt());
		}else{
			result.setShuppinSuTo(null);
		}
		//出品状態(未使用)
		//if ( cboShuppinJotai.getSelectedIndex() >= 0 ){
		//	result.setShuppinJotai(cboShuppinJotai.getSelectedItemNum());
		//}else{
		//	result.setShuppinJotai(null);
		//}

		//未出品のみ表示
		result.setMiShuppinNomiFlg(chkMiShuppinNomi.isSelected());

		//出品可否
		if ( btnShuppinKa.isSelected() ){
			result.setShuppinKahi(Constants.AMAZON_SHUPPINKAHI_KA);
		}else if ( btnShuppinFuka.isSelected() ){
			result.setShuppinKahi(Constants.AMAZON_SHUPPINKAHI_FUKA);
		}else if ( btnSaiShuppin.isSelected() ){
			result.setShuppinKahi(Constants.AMAZON_SHUPPINKAHI_SAISHUPPIN);
		}else{
			result.setShuppinKahi(null);
		}
		//取引日付
		result.setKaitoriFlg(cbKaitori.isSelected());
		result.setSiireFlg(cbSiire.isSelected());
		result.setNyukoFlg(cbNyuko.isSelected());
		result.setFurikaeFlg(cbFurikae.isSelected());
		if ( !Util.isNullOrEmpty(dateTaishoDateFrom.getText().trim()) ){
			result.setTaishoDateFrom(dateTaishoDateFrom.getTextDate());
		}else{
			result.setTaishoDateFrom(null);
		}
		if ( !Util.isNullOrEmpty(dateTaishoDateTo.getText().trim()) ){
			result.setTaishoDateTo(dateTaishoDateTo.getTextDate());
		}else{
			result.setTaishoDateTo(null);
		}
		//取引伝票番号
		if ( !Util.isNullOrEmpty(txtTorihikiDenpyoNoFrom.getText().trim())){
			result.setTorihikiDenpyoNoFrom(txtTorihikiDenpyoNoFrom.getTextLong());
		}else{
			result.setTorihikiDenpyoNoFrom(null);
		}
		if ( !Util.isNullOrEmpty(txtTorihikiDenpyoNoTo.getText().trim())){
			result.setTorihikiDenpyoNoTo(txtTorihikiDenpyoNoTo.getTextLong());
		}else{
			result.setTorihikiDenpyoNoTo(null);
		}

		//仕入先ｺｰﾄﾞ
		if ( !Util.isNullOrEmpty(txtSiireSakiCd.getText().trim()) ){
			result.setSiireSakiCd(txtSiireSakiCd.getTextLong());
			result.setSiireSakiNm(lblSiireSakiNm.getText());
		}else{
			result.setSiireSakiCd(null);
		}

		return result;
	}

	public TimeShareShuupinJokenBean getJokenBean(){
		return jokenBean;
	}

	/**
	 * This method initializes dlg
	 *
	 * @return javax.swing.JDialog
	 */
	private JDialog getDlg() {
		if (dlg == null) {
			dlg = new JDialog(frm);
			dlg.setContentPane(getJContentPane());
			dlg.setSize(new Dimension(911, 621));//546
			dlg.setTitle(strMsgTitle);
			//画面の中央に表示
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
			jContentPane.add(getPanelInput(), null);
			jContentPane.add(getFKey(), null);

		}
		return jContentPane;
	}

	/**
	 * This method initializes panelInput
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelInput() {

		if ( panelInput == null ){
			int intX = 15, intY = 10;
			int intX1 = 430, intY1 = 50;
			tlblShohinSitei = new TLabel2();
			tlblShohinSitei.setText("商品指定");
			tlblShohinSitei.setBounds(new Rectangle(intX+15, intY+25, 111, 31));
			lblShohinSitei = new TLabel6();
			lblShohinSitei.setText("T-AAA T-BBB T-CCC T-DDD T-EEE T-FFF T-GGG T-HHH");
			lblShohinSitei.setBounds(new Rectangle(intX+125, intY+25, 371, 31));

			lblShohinKey = new TLabel2();
			lblShohinKey.setText("未指定");
			lblShohinKey.setBounds(new Rectangle(intX+501, intY+25, 94, 31));
			lblShohinKey.setBackground(new Color(255, 200, 0));

			tlblHyojiSort = new TLabel2();
			tlblHyojiSort.setText("表示順序");
			tlblHyojiSort.setBounds(new Rectangle(intX+600,intY+25, 96, 31));
			lblHyojiSort = new TLabel6();
			lblHyojiSort.setText("品番昇順");
			lblHyojiSort.setBounds(new Rectangle(intX+695, intY+25, 320, 31));

			tlblShohinKanaNm = new TLabel();
			tlblShohinKanaNm.setText("商品名称カナ");
			tlblShohinKanaNm.setBounds(new Rectangle(intX+15, intY+65, 111, 31));
			lblShohinKanaNmWaku = new JLabel();
			lblShohinKanaNmWaku.setText("");
			lblShohinKanaNmWaku.setBounds(new Rectangle(intX+125, intY+65, 240, 31));
			lblShohinKanaNmWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtShohinKanaNm = new TTextField();
			txtShohinKanaNm.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtShohinKanaNm.setText("ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀ");
			txtShohinKanaNm.setMaxLength(30);
			txtShohinKanaNm.setIMType(TTextField.IM_HALFKANA);
			txtShohinKanaNm.setBounds(new Rectangle(intX+127, intY+67, 121, 26));
			txtShohinKanaNm.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			cboShohinKanaNm = new TComboBox();
			cboShohinKanaNm.setBounds(new Rectangle(intX+250, intY+67, 111, 28));
			cboShohinKanaNm.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			cboShohinKanaNm.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));

			tlblKikakuNo = new TLabel();
			tlblKikakuNo.setText("規格番号");
			tlblKikakuNo.setBounds(new Rectangle(intX+15, intY+105, 111, 31));
			lblKikakuNoWaku = new JLabel();
			lblKikakuNoWaku.setText("");
			lblKikakuNoWaku.setBounds(new Rectangle(intX+125, intY+105, 240, 31));
			lblKikakuNoWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtKikakuNo = new TTextField();
			txtKikakuNo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtKikakuNo.setText("ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀ");
			txtKikakuNo.setMaxLength(16);
			txtKikakuNo.setIMType(TTextField.IM_OFF);
			txtKikakuNo.setBounds(new Rectangle(intX+127, intY+107, 121, 26));
			txtKikakuNo.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			cboKikakuNo = new TComboBox();
			cboKikakuNo.setBounds(new Rectangle(intX+250, intY+107, 111, 28));
			cboKikakuNo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			cboKikakuNo.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));

			tlblJanCd = new TLabel();
			tlblJanCd.setText("JANコード");
			tlblJanCd.setBounds(new Rectangle(intX+15, intY+145, 111, 31));
			lblJanCdWaku = new JLabel();
			lblJanCdWaku.setText("");
			lblJanCdWaku.setBounds(new Rectangle(intX+125, intY+145, 240, 31));
			lblJanCdWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtJanCd = new TNumericField();
			txtJanCd.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtJanCd.setText("1234567890123");
			txtJanCd.setMaxLength(13);
			txtJanCd.setIMType(TTextField.IM_OFF);
			txtJanCd.setBounds(new Rectangle(intX+127, intY+147, 121, 26));
			txtJanCd.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			cboJanCd = new TComboBox();
			cboJanCd.setBounds(new Rectangle(intX+250, intY+147, 111, 28));
			cboJanCd.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			cboJanCd.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));

			tlblHatubaiDate = new TLabel();
			tlblHatubaiDate.setBounds(new Rectangle(intX+15, intY+185, 111, 31));
			tlblHatubaiDate.setText("発売日");
			lblHatubaiDateWaku = new JLabel();
			lblHatubaiDateWaku.setBounds(new Rectangle(intX+125, intY+185, 240, 31));
			lblHatubaiDateWaku.setText("");
			lblHatubaiDateWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			cboHatubaiDate = new TComboBox();
			cboHatubaiDate.setBounds(new Rectangle(intX+127, intY+187, 81, 28));
			cboHatubaiDate.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			cboHatubaiDate.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 12));
			//範囲指定
			dateHatubaiDateFrom = new TDateField();
			dateHatubaiDateFrom.setBounds(new Rectangle(intX+207, intY+187, 66, 26));
			dateHatubaiDateFrom.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			dateHatubaiDateFrom.setText("19900101");
			dateHatubaiDateFrom.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 12));
			lblHatubaiDateKara = new JLabel();
			lblHatubaiDateKara.setBounds(new Rectangle(intX+272, intY+187, 21, 26));
			lblHatubaiDateKara.setHorizontalTextPosition(SwingConstants.CENTER);
			lblHatubaiDateKara.setText("\uff5e");
			lblHatubaiDateKara.setHorizontalAlignment(SwingConstants.CENTER);
			dateHatubaiDateTo = new TDateField();
			dateHatubaiDateTo.setBounds(new Rectangle(intX+294, intY+187, 66, 26));
			dateHatubaiDateTo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			dateHatubaiDateTo.setText("19900101");
			dateHatubaiDateTo.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 12));
			//経過日数
			txtHatubaiDate = new  TNumericField();
			txtHatubaiDate.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtHatubaiDate.setText("12345678");
			txtHatubaiDate.setMaxLength(8);
			txtHatubaiDate.setIMType(TTextField.IM_OFF);
			txtHatubaiDate.setBounds(new Rectangle(intX+207, intY+187, 101, 26));
			txtHatubaiDate.setHorizontalAlignment(SwingConstants.RIGHT);
			txtHatubaiDate.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			lblHatubaiDateIjou = new JLabel();
			lblHatubaiDateIjou.setBounds(new Rectangle(intX+307, intY+227, 51, 26));
			lblHatubaiDateIjou.setHorizontalTextPosition(SwingConstants.CENTER);
			lblHatubaiDateIjou.setText("日以上");
			lblHatubaiDateIjou.setHorizontalAlignment(SwingConstants.CENTER);

			tlblDaibunruiCd = new TLabel();
			tlblDaibunruiCd.setBounds(new Rectangle(intX+15, intY+225, 111, 31));
			tlblDaibunruiCd.setText("大分類");
			lblDaibunruiCd = new JLabel();
			lblDaibunruiCd.setBounds(new Rectangle(intX+125, intY+225, 240, 31));
			lblDaibunruiCd.setText("");
			lblDaibunruiCd.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			scodeDaibunrui = new SearchCode();
			scodeDaibunrui.setBounds(new Rectangle(intX+127, intY+227, 235, 28));
			scodeDaibunrui.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			scodeDaibunrui.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));

			tlblChubunruiCd = new TLabel();
			tlblChubunruiCd.setBounds(new Rectangle(intX+15, intY+265, 111, 31));
			tlblChubunruiCd.setText("中分類");
			lblChubunruiCd = new JLabel();
			lblChubunruiCd.setBounds(new Rectangle(intX+125, intY+265, 240, 31));
			lblChubunruiCd.setText("");
			lblChubunruiCd.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			scodeChubunrui = new SearchCode();
			scodeChubunrui.setBounds(new Rectangle(intX+127, intY+267, 235, 28));
			scodeChubunrui.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
//			scodeChubunrui.setEnabled(false);
			scodeChubunrui.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));

			tlblShobunruiCd = new TLabel();
			tlblShobunruiCd.setBounds(new Rectangle(intX+15, intY+305, 111, 31));
			tlblShobunruiCd.setText("小分類");
			lblShobunruiCd = new JLabel();
			lblShobunruiCd.setBounds(new Rectangle(intX+125, intY+305, 240, 31));
			lblShobunruiCd.setText("");
			lblShobunruiCd.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			scodeShobunrui = new SearchCode();
			scodeShobunrui.setBounds(new Rectangle(intX+127, intY+307, 235, 28));
			scodeShobunrui.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
//			scodeShobunrui.setEnabled(false);
			scodeShobunrui.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));

			tlblMakerCd = new TLabel();
			tlblMakerCd.setBounds(new Rectangle(intX+15, intY+345, 111, 31));
			tlblMakerCd.setText("メーカーコード");
			lblMakerCdWaku = new JLabel();
			lblMakerCdWaku.setBounds(new Rectangle(intX+125, intY+345, 240, 31));
			lblMakerCdWaku.setText("");
			lblMakerCdWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtMakerCd = new TNumericField();
			txtMakerCd.setBounds(new Rectangle(intX+127, intY+347, 81, 26));
			txtMakerCd.setText("123456789");
			txtMakerCd.setMaxLength(9);
			txtMakerCd.setNumericFormat("#");
			txtMakerCd.setHorizontalAlignment(SwingConstants.RIGHT);
			txtMakerCd.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			txtMakerCd.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			lblMakerNm = new TLabel3();
			lblMakerNm.setBounds(new Rectangle(intX+210, intY+347, 151, 27));
			lblMakerNm.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			lblMakerNm.setText("あいうえおかきくけこさしすせそ");

			tlblLabelCd = new TLabel();
			tlblLabelCd.setBounds(new Rectangle(intX+15, intY+385, 111, 31));
			tlblLabelCd.setText("レーベルコード");
			lblLabelCdWaku = new JLabel();
			lblLabelCdWaku.setBounds(new Rectangle(intX+125, intY+385, 240, 31));
			lblLabelCdWaku.setText("");
			lblLabelCdWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtLabelCd = new TNumericField();
			txtLabelCd.setBounds(new Rectangle(intX+127, intY+387, 81, 26));
			txtLabelCd.setText("123456789");
			txtLabelCd.setMaxLength(9);
			txtLabelCd.setNumericFormat("#");
			txtLabelCd.setHorizontalAlignment(SwingConstants.RIGHT);
			txtLabelCd.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			txtLabelCd.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			lblLabelNm = new TLabel3();
			lblLabelNm.setBounds(new Rectangle(intX+210, intY+387, 151, 27));
			lblLabelNm.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			lblLabelNm.setText("あいうえおかきくけこさしすせそ");

			tlblRank = new TLabel();
			tlblRank.setBounds(new Rectangle(intX+15, intY+425, 111, 31));
			tlblRank.setText("ランク");
			lblRank = new JLabel();
			lblRank.setBounds(new Rectangle(intX+125, intY+425, 140, 31));
			lblRank.setText("");
			lblRank.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			cboRank = new TComboBox();
			cboRank.setBounds(new Rectangle(intX+127, intY+427, 135, 28));
			cboRank.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			cboRank.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));

			tlblZaikoSu = new TLabel();
			tlblZaikoSu.setBounds(new Rectangle(intX1+15, intY1+25, 111, 31));
			tlblZaikoSu.setText("在庫数");
			lblZaikoSuWaku = new JLabel();
			lblZaikoSuWaku.setBounds(new Rectangle(intX1+125, intY1+25, 210, 31));
			lblZaikoSuWaku.setText("");
			lblZaikoSuWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtZaikoSuFrom = new TNumericField();
			txtZaikoSuFrom.setBounds(new Rectangle(intX1+127, intY1+27, 91, 26));
			txtZaikoSuFrom.setText("12345678");
			txtZaikoSuFrom.setMaxLength(8);
			txtZaikoSuFrom.setNumericFormat("#");
			txtZaikoSuFrom.setHorizontalAlignment(SwingConstants.RIGHT);
			txtZaikoSuFrom.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			txtZaikoSuFrom.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			lblZaikoSuKara = new JLabel();
			lblZaikoSuKara.setBounds(new Rectangle(intX1+218, intY1+27, 21, 26));
			lblZaikoSuKara.setHorizontalTextPosition(SwingConstants.CENTER);
			lblZaikoSuKara.setText("\uff5e");
			lblZaikoSuKara.setHorizontalAlignment(SwingConstants.CENTER);
			txtZaikoSuTo = new TNumericField();
			txtZaikoSuTo.setBounds(new Rectangle(intX1+240, intY1+27, 91, 26));
			txtZaikoSuTo.setText("12345678");
			txtZaikoSuTo.setMaxLength(8);
			txtZaikoSuTo.setNumericFormat("#");
			txtZaikoSuTo.setHorizontalAlignment(SwingConstants.RIGHT);
			txtZaikoSuTo.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			txtZaikoSuTo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

			//出品日
			tlblShuppinDate = new TLabel();
			tlblShuppinDate.setBounds(new Rectangle(intX1+15, intY1+65, 111, 31));
			tlblShuppinDate.setText("出品日");
			lblShuppinDateWaku = new JLabel();
			lblShuppinDateWaku.setBounds(new Rectangle(intX1+125, intY1+65, 240, 31));
			lblShuppinDateWaku.setText("");
			lblShuppinDateWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			cboShuppinDate = new TComboBox();
			cboShuppinDate.setBounds(new Rectangle(intX1+127, intY1+67, 81, 28));
			cboShuppinDate.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			cboShuppinDate.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 12));
			//範囲指定
			dateShuppinDateFrom = new TDateField();
			dateShuppinDateFrom.setBounds(new Rectangle(intX1+207, intY1+67, 66, 26));
			dateShuppinDateFrom.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			dateShuppinDateFrom.setText("19900101");
			dateShuppinDateFrom.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 12));
			lblShuppinDateKara = new JLabel();
			lblShuppinDateKara.setBounds(new Rectangle(intX1+272, intY1+67, 21, 26));
			lblShuppinDateKara.setHorizontalTextPosition(SwingConstants.CENTER);
			lblShuppinDateKara.setText("\uff5e");
			lblShuppinDateKara.setHorizontalAlignment(SwingConstants.CENTER);
			dateShuppinDateTo = new TDateField();
			dateShuppinDateTo.setBounds(new Rectangle(intX1+294, intY1+67, 66, 26));
			dateShuppinDateTo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			dateShuppinDateTo.setText("19900101");
			dateShuppinDateTo.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 12));
			//経過日数
			txtShuppinDate = new  TNumericField();
			txtShuppinDate.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			txtShuppinDate.setText("12345678");
			txtShuppinDate.setMaxLength(8);
			txtShuppinDate.setIMType(TTextField.IM_OFF);
			txtShuppinDate.setBounds(new Rectangle(intX1+207, intY1+67, 101, 26));
			txtShuppinDate.setHorizontalAlignment(SwingConstants.RIGHT);
			txtShuppinDate.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			lblShuppinDateIjou = new JLabel();
			lblShuppinDateIjou.setBounds(new Rectangle(intX1+307, intY1+67, 51, 26));
			lblShuppinDateIjou.setHorizontalTextPosition(SwingConstants.CENTER);
			lblShuppinDateIjou.setText("日以上");
			lblShuppinDateIjou.setHorizontalAlignment(SwingConstants.CENTER);

			tlblShuppinSu = new TLabel();
			tlblShuppinSu.setBounds(new Rectangle(intX1+15, intY1+105, 111, 31));
			tlblShuppinSu.setText("出品済数");
			lblShuppinSuWaku = new JLabel();
			lblShuppinSuWaku.setBounds(new Rectangle(intX1+125, intY1+105, 210, 31));
			lblShuppinSuWaku.setText("");
			lblShuppinSuWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtShuppinSuFrom = new TNumericField();
			txtShuppinSuFrom.setBounds(new Rectangle(intX1+127, intY1+107, 91, 26));
			txtShuppinSuFrom.setText("12345678");
			txtShuppinSuFrom.setMaxLength(8);
			txtShuppinSuFrom.setNumericFormat("#");
			txtShuppinSuFrom.setHorizontalAlignment(SwingConstants.RIGHT);
			txtShuppinSuFrom.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			txtShuppinSuFrom.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			lblShuppinSuKara = new JLabel();
			lblShuppinSuKara.setBounds(new Rectangle(intX1+218, intY1+107, 21, 26));
			lblShuppinSuKara.setHorizontalTextPosition(SwingConstants.CENTER);
			lblShuppinSuKara.setText("\uff5e");
			lblShuppinSuKara.setHorizontalAlignment(SwingConstants.CENTER);
			txtShuppinSuTo = new TNumericField();
			txtShuppinSuTo.setBounds(new Rectangle(intX1+240, intY1+107, 91, 26));
			txtShuppinSuTo.setText("12345678");
			txtShuppinSuTo.setMaxLength(8);
			txtShuppinSuTo.setNumericFormat("#");
			txtShuppinSuTo.setHorizontalAlignment(SwingConstants.RIGHT);
			txtShuppinSuTo.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			txtShuppinSuTo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

			//未出品のみ表示
			tlblMiShuppinNomi = new TLabel();
			tlblMiShuppinNomi.setBounds(new Rectangle(intX1+15, intY1+145, 111, 31));
			tlblMiShuppinNomi.setText("出品状態");
			lblMiShuppinNomi = new JLabel();
			lblMiShuppinNomi.setBounds(new Rectangle(intX1+125, intY1+145, 140, 31));
			lblMiShuppinNomi.setText("");
			lblMiShuppinNomi.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			chkMiShuppinNomi = new TCheckBox();
			chkMiShuppinNomi.setBounds(new Rectangle(intX1+127, intY1+147, 135, 28));
			chkMiShuppinNomi.setText("未出品のみ表示");
			chkMiShuppinNomi.setSelected(true);

			//出品状態
			tlblShuppinJotai = new TLabel();
			tlblShuppinJotai.setBounds(new Rectangle(intX1+15, intY1+145, 111, 31));
			tlblShuppinJotai.setText("出品状態");
			lblShuppinJotai = new JLabel();
			lblShuppinJotai.setBounds(new Rectangle(intX1+125, intY1+145, 140, 31));
			lblShuppinJotai.setText("");
			lblShuppinJotai.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			cboShuppinJotai = new TComboBox();
			cboShuppinJotai.setBounds(new Rectangle(intX1+127, intY1+147, 135, 28));
			cboShuppinJotai.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			cboShuppinJotai.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));

			//出品可否
			tlblShuppinKahi = new TLabel();
			tlblShuppinKahi.setBounds(new Rectangle(intX1+15, intY1+185, 111, 31));
			tlblShuppinKahi.setText("出品可否");
			lblShuppinKahiWaku = new JLabel();
			lblShuppinKahiWaku.setBounds(new Rectangle(intX1+125, intY1+185, 240, 31));
			lblShuppinKahiWaku.setText("");
			lblShuppinKahiWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			btnShuppinKa = new TRadioButton();
			btnShuppinKa.setBounds(new Rectangle(intX1+127, intY1+187, 41, 28));
			btnShuppinKa.setSelected(true);
			btnShuppinKa.setText("可");
			btnShuppinFuka = new TRadioButton();
			btnShuppinFuka.setBounds(new Rectangle(intX1+170, intY1+187, 51, 28));
			btnShuppinFuka.setSelected(false);
			btnShuppinFuka.setText("不可");
			btnSaiShuppin = new TRadioButton();
			btnSaiShuppin.setBounds(new Rectangle(intX1+225, intY1+187, 65, 28));
			btnSaiShuppin.setSelected(false);
			btnSaiShuppin.setText("再出品");
			btnShuppinAll = new TRadioButton();
			btnShuppinAll.setBounds(new Rectangle(intX1+295, intY1+187, 65, 28));
			btnShuppinAll.setSelected(false);
			btnShuppinAll.setText("すべて");

			tlblMiShuppinJoken = new TLabel();
			tlblMiShuppinJoken.setBounds(new Rectangle(intX1+15, intY1+225, 111, 72));
			tlblMiShuppinJoken.setText("取引日付");
			lblMiShuppinJokenWaku = new JLabel();
			lblMiShuppinJokenWaku.setBounds(new Rectangle(intX1+125, intY1+225, 210, 72));
			lblMiShuppinJokenWaku.setText("");
			lblMiShuppinJokenWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

			cbKaitori = new TCheckBox();
			cbKaitori.setBounds(new Rectangle(intX1+127,intY1+227,53,28));
			cbKaitori.setText("買取");
			cbSiire = new TCheckBox();
			cbSiire.setBounds(new Rectangle(intX1+178,intY1+227,53,28));
			cbSiire.setText("仕入");
			cbNyuko = new TCheckBox();
			cbNyuko.setBounds(new Rectangle(intX1+231,intY1+227,53,28));
			cbNyuko.setText("入庫");
			cbFurikae = new TCheckBox();
			cbFurikae.setBounds(new Rectangle(intX1+282,intY1+227,51,28));
			cbFurikae.setText("振替");

			dateTaishoDateFrom = new TDateField();
			dateTaishoDateFrom.setBounds(new Rectangle(intX1+127, intY1+267, 91, 26));
			dateTaishoDateFrom.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			dateTaishoDateFrom.setText("19900101");
			dateTaishoDateFrom.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			lblTaishoDateKara = new JLabel();
			lblTaishoDateKara.setBounds(new Rectangle(intX1+218, intY1+267, 21, 26));
			lblTaishoDateKara.setHorizontalTextPosition(SwingConstants.CENTER);
			lblTaishoDateKara.setText("\uff5e");
			lblTaishoDateKara.setHorizontalAlignment(SwingConstants.CENTER);
			dateTaishoDateTo = new TDateField();
			dateTaishoDateTo.setBounds(new Rectangle(intX1+240, intY1+267, 91, 26));
			dateTaishoDateTo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			dateTaishoDateTo.setText("19900101");
			dateTaishoDateTo.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));

			tlblTorihikiDenpyoNo = new TLabel();
			tlblTorihikiDenpyoNo.setBounds(new Rectangle(intX1+15, intY1+296, 111, 39));
			tlblTorihikiDenpyoNo.setText("取引伝票番号");
			lblTorihikiDenpyoNoWaku = new JLabel();
			lblTorihikiDenpyoNoWaku.setBounds(new Rectangle(intX1+125, intY1+296, 210, 39));
			lblTorihikiDenpyoNoWaku.setText("");
			lblTorihikiDenpyoNoWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtTorihikiDenpyoNoFrom = new TNumericField();
			txtTorihikiDenpyoNoFrom.setBounds(new Rectangle(intX1+127, intY1+307, 91, 26));
			txtTorihikiDenpyoNoFrom.setText("12345678");
			txtTorihikiDenpyoNoFrom.setMaxLength(8);
			txtTorihikiDenpyoNoFrom.setNumericFormat("#");
			txtTorihikiDenpyoNoFrom.setHorizontalAlignment(SwingConstants.RIGHT);
			txtTorihikiDenpyoNoFrom.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			txtTorihikiDenpyoNoFrom.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			lblTorihikiDenpyoNoKara = new JLabel();
			lblTorihikiDenpyoNoKara.setBounds(new Rectangle(intX1+218, intY1+307, 21, 26));
			lblTorihikiDenpyoNoKara.setHorizontalTextPosition(SwingConstants.CENTER);
			lblTorihikiDenpyoNoKara.setText("\uff5e");
			lblTorihikiDenpyoNoKara.setHorizontalAlignment(SwingConstants.CENTER);
			txtTorihikiDenpyoNoTo = new TNumericField();
			txtTorihikiDenpyoNoTo.setBounds(new Rectangle(intX1+240, intY1+307, 91, 26));
			txtTorihikiDenpyoNoTo.setText("12345678");
			txtTorihikiDenpyoNoTo.setMaxLength(8);
			txtTorihikiDenpyoNoTo.setNumericFormat("#");
			txtTorihikiDenpyoNoTo.setHorizontalAlignment(SwingConstants.RIGHT);
			txtTorihikiDenpyoNoTo.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			txtTorihikiDenpyoNoTo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

			tlblSiireSakiCd = new TLabel();
			tlblSiireSakiCd.setBounds(new Rectangle(intX1+15, intY1+345, 111, 31));
			tlblSiireSakiCd.setText("仕入先コード");
			lblSiireSakiCdWaku = new JLabel();
			lblSiireSakiCdWaku.setBounds(new Rectangle(intX1+125, intY1+345, 240, 31));
			lblSiireSakiCdWaku.setText("");
			lblSiireSakiCdWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			txtSiireSakiCd = new TNumericField();
			txtSiireSakiCd.setBounds(new Rectangle(intX1+127, intY1+347, 81, 26));
			txtSiireSakiCd.setText("123456789");
			txtSiireSakiCd.setMaxLength(9);
			txtSiireSakiCd.setNumericFormat("#");
			txtSiireSakiCd.setHorizontalAlignment(SwingConstants.RIGHT);
			txtSiireSakiCd.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			txtSiireSakiCd.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			lblSiireSakiNm = new TLabel3();
			lblSiireSakiNm.setBounds(new Rectangle(intX1+210, intY1+347, 151, 27));
			lblSiireSakiNm.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			lblSiireSakiNm.setText("あいうえおかきくけこさしすせそ");

			panelInput = new JPanel();
			panelInput.setLayout(null);
			panelInput.setBounds(new Rectangle(40, 25, 820, 486));
			panelInput.setBorder(BorderFactory.createTitledBorder(null, "\u226a\u691c\u7d22\u6761\u4ef6\u226b", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));

			panelInput.add(tlblShohinSitei, null);
			panelInput.add(lblShohinSitei, null);
			panelInput.add(lblShohinKey, null);
			panelInput.add(tlblShohinKanaNm, null);
			panelInput.add(lblShohinKanaNmWaku, null);
			panelInput.add(txtShohinKanaNm, null);
			panelInput.add(cboShohinKanaNm, null);
			panelInput.add(tlblKikakuNo,null);
			panelInput.add(lblKikakuNoWaku,null);
			panelInput.add(txtKikakuNo,null);
			panelInput.add(cboKikakuNo,null);
			panelInput.add(tlblJanCd, null);
			panelInput.add(lblJanCdWaku, null);
			panelInput.add(txtJanCd, null);
			panelInput.add(cboJanCd, null);
			panelInput.add(cboHatubaiDate, null);
			panelInput.add(tlblHatubaiDate, null);
			panelInput.add(lblHatubaiDateWaku, null);
			panelInput.add(lblHatubaiDateKara, null);
			panelInput.add(dateHatubaiDateFrom, null);
			panelInput.add(dateHatubaiDateTo, null);
			panelInput.add(txtHatubaiDate, null);
			panelInput.add(lblHatubaiDateIjou, null);
			panelInput.add(tlblDaibunruiCd, null);
			panelInput.add(lblDaibunruiCd, null);
			panelInput.add(scodeDaibunrui, null);
			panelInput.add(tlblChubunruiCd, null);
			panelInput.add(lblChubunruiCd, null);
			panelInput.add(scodeChubunrui, null);
			panelInput.add(tlblShobunruiCd, null);
			panelInput.add(lblShobunruiCd, null);
			panelInput.add(scodeShobunrui, null);
			panelInput.add(tlblMakerCd, null);
			panelInput.add(lblMakerCdWaku, null);
			panelInput.add(txtMakerCd, null);
			panelInput.add(lblMakerNm, null);
			panelInput.add(tlblLabelCd, null);
			panelInput.add(lblLabelCdWaku, null);
			panelInput.add(txtLabelCd, null);
			panelInput.add(lblLabelNm, null);
			panelInput.add(tlblRank, null);
			panelInput.add(lblRank, null);
			panelInput.add(cboRank, null);
			panelInput.add(tlblZaikoSu, null);
			panelInput.add(lblZaikoSuWaku, null);
			panelInput.add(lblZaikoSuKara, null);
			panelInput.add(txtZaikoSuFrom, null);
			panelInput.add(txtZaikoSuTo, null);
			panelInput.add(tlblShuppinDate, null);
			panelInput.add(lblShuppinDateWaku, null);
			panelInput.add(lblShuppinDateKara, null);
			panelInput.add(cboShuppinDate, null);
			panelInput.add(dateShuppinDateFrom, null);
			panelInput.add(dateShuppinDateTo, null);
			panelInput.add(txtShuppinDate, null);
			panelInput.add(lblShuppinDateIjou ,null);
			panelInput.add(tlblShuppinSu, null);
			panelInput.add(lblShuppinSuWaku, null);
			panelInput.add(lblShuppinSuKara, null);
			panelInput.add(txtShuppinSuFrom, null);
			panelInput.add(txtShuppinSuTo, null);
			panelInput.add(tlblMiShuppinNomi, null);
			panelInput.add(lblMiShuppinNomi, null);
			panelInput.add(chkMiShuppinNomi, null);
			panelInput.add(tlblShuppinKahi, null);
			panelInput.add(lblShuppinKahiWaku, null);
			panelInput.add(btnShuppinKa, null);
			panelInput.add(btnShuppinFuka, null);
			panelInput.add(btnSaiShuppin, null);
			panelInput.add(btnShuppinAll, null);
			panelInput.add(tlblMiShuppinJoken, null);
			panelInput.add(lblMiShuppinJokenWaku, null);
			panelInput.add(cbKaitori, null);
			panelInput.add(cbSiire, null);
			panelInput.add(cbNyuko, null);
			panelInput.add(cbFurikae, null);
			panelInput.add(lblTaishoDateKara, null);
			panelInput.add(dateTaishoDateFrom, null);
			panelInput.add(dateTaishoDateTo, null);
			panelInput.add(tlblTorihikiDenpyoNo, null);
			panelInput.add(lblTorihikiDenpyoNoWaku, null);
			panelInput.add(txtTorihikiDenpyoNoFrom, null);
			panelInput.add(lblTorihikiDenpyoNoKara, null);
			panelInput.add(txtTorihikiDenpyoNoTo, null);
			panelInput.add(tlblSiireSakiCd, null);
			panelInput.add(lblSiireSakiCdWaku, null);
			panelInput.add(txtSiireSakiCd, null);
			panelInput.add(lblSiireSakiNm, null);
		}
		return panelInput;
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
			jPanelFkey.setBounds(new Rectangle(40, 527, 820, 46));
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
			intFormat= new int[] { 1, 2, 5, 6, 9, 11, 12 };
			fKey = new FKey(intFormat);
			fKey.setLayout(null);
			fKey.setBounds(jPanelFkey.getBounds());
			fKey.setF1Text("F1 商品指定");
			fKey.setF2Text("F2 表示順序");
			fKey.setF5Text("F5 検索");
			fKey.setF6Text("F6 条件ｸﾘｱ");
			fKey.setF9Text("F9 ｷｬﾝｾﾙ");
			fKey.setF11Text("F11 出品");
			fKey.setF12Text("F12 確認出品");
		}
		return fKey;
	}
}