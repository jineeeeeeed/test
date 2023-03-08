package jp.co.css.TTSA;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

import jp.co.css.base.AppConfig;
import jp.co.css.bean.DbInfo;
import jp.co.css.dao.ShohinMstDAO;
import jp.co.css.webpos.common.db.H2;
import jp.co.css.webpos.common.except.TException;
import jp.co.css.webpos.common.gui.FKey;
import jp.co.css.webpos.common.gui.FKeyAdapter;
import jp.co.css.webpos.common.gui.TComboBox;
import jp.co.css.webpos.common.gui.TGuiUtil;
import jp.co.css.webpos.common.gui.TLabel;
import jp.co.css.webpos.common.gui.TTextValue;
import jp.co.css.webpos.common.log.LogOut;
import jp.co.css.webpos.common.message.MessageBoxValue;
import jp.co.css.webpos.common.message.MessageDispNew;
import jp.co.css.webpos.common.util.Util;

/**
 * 　商品指定ﾎﾟｯﾌﾟｱｯﾌﾟ
 */
public class ShohinSitei implements MessageBoxValue
{
    private JDialog dlg = null;
	private JPanel jContentPane = null;

	private int ans = MB_CANCEL;			//どのﾎﾞﾀﾝが押されたかの戻り値
	private final String strMsgTitle = "商品指定"; //ﾒｯｾｰｼﾞ用ﾀｲﾄﾙ

	private JFrame frm = null;				//呼出元画面
	public MessageDispNew messageBox=new MessageDispNew();

	private JPanel jPanel = null;
    private TLabel tlblShohinKeyKb = null;
    private JLabel lblShohinKeyKb = null;
    public TComboBox cboShohinKeyKb = null;
    private JTextArea edit = null;
    private JScrollPane scroll = null;

	private FKey fKey = null;
	private JPanel jPanelFkey = null;
	private LogOut logOut;

	private List<String> list = null;
	private AppConfig appConfig;
	int currentShohinKeyKb = -1;//-1:未指定 0:商品コード 1:JANコード 2:規格番号
	//商品ﾏｽﾀDAO
	ShohinMstDAO shohinDao = null;
	DbInfo shohinInfo = null;

	//ｺﾝｽﾄﾗｸﾀ
	public ShohinSitei(JFrame frm, AppConfig appConfig, int shohinKeyKb, List<String> list){
		this.frm = frm;
		this.appConfig = appConfig;
		this.list = list;
		this.currentShohinKeyKb = shohinKeyKb;
		logOut = appConfig.getLogOut();
		getDlg();
	}

	/**
	 * ﾒｲﾝ画面表示
	 * @return
	 */
	public int disp() {
		if ( this.currentShohinKeyKb == -1 )
			this.currentShohinKeyKb = 2;
		cboShohinKeyKb.removeAllItems();
		cboShohinKeyKb.addTextValueItem("0", "商品コード");
		cboShohinKeyKb.addTextValueItem("1", "JANコード");
		cboShohinKeyKb.addTextValueItem("2", "規格番号");
		for(int i=0; i<cboShohinKeyKb.getItemCount(); i++){
			TTextValue textValue = (TTextValue)cboShohinKeyKb.getItemAt(i);
			if (Integer.parseInt(textValue.getValue()) == currentShohinKeyKb){
				cboShohinKeyKb.setSelectedIndex(i);
				break;
			}
		}

		if ( list != null ){
			for(int i=0; i<list.size(); i++){
				try{
					Document doc = edit.getDocument();
					doc.insertString(doc.getLength(), list.get(i)+"\n", null);
				}catch(BadLocationException e){}
			}
		}

		dlg.setModal(true);								//ﾓｰﾀﾞﾙに
		dlg.setResizable(false);						//ｻｲｽﾞ変更不可
		fKey.addFButtonListener(new ButtonListener());	//ﾘｽﾅ追加
		fKey.setLogOut(logOut);

		//ログ出力
		logOut.info("画面【" + dlg.getTitle() + "】を開きました。");
								//変更用ﾃｷｽﾄにﾌｫｰｶｽを当てる
//		画面サイズ調整
		TGuiUtil.resizeWindow(dlg);
		dlg.setVisible(true);							//画面表示

		return ans;
	}

	class ButtonListener extends FKeyAdapter
	{
		//ｷｬﾝｾﾙ
		public void f9Click(ActionEvent e) {
			ans = MB_CANCEL;
			//ログ出力
			logOut.info("画面【" + dlg.getTitle() + "】を閉じました。");
			dlg.dispose();
		}
		//OK
		public void f12Click(ActionEvent e) {
			ans = MB_OK;
			list.clear();
			List<String> allList	= new ArrayList<String>();
			List<String> errorList	= new ArrayList<String>();
			if ( !(edit.getText() == null) ){
				String[] split = edit.getText().split("\n");
				if ( split != null ){
					for(int i=0; i<split.length; i++){
						if ( !Util.isNullOrEmpty(split[i].trim()) ){
							allList.add(split[i].replaceAll("\r", "").replaceAll("\t", ""));
						}
					}
				}
			}
			currentShohinKeyKb = Integer.parseInt(cboShohinKeyKb.getSelectedItemValue().toString());

			if ( allList.size() > 0 ){
				H2 h2 = new H2();
				try{
					h2.h2ClientStart(appConfig.getDatabaseXmlBean());

					String error1 = "";
					String error2 = "";
					String error3 = "";
					String error4 = "";

					int maxLength = 13;
					if ( currentShohinKeyKb == 0 || currentShohinKeyKb ==1 ){
						maxLength = 13;
					}else{
						maxLength = 16;
					}
					//ﾏｽﾀにﾃﾞｰﾀの存在ﾁｪｯｸ
					shohinDao = new ShohinMstDAO(appConfig);
					for(int i=0; i<allList.size(); i++){
						//桁数ﾁｪｯｸ
						if ( allList.get(i).length() > maxLength){
							if ( !Util.isNullOrEmpty(error4) ) error4 += ",";
							error4 += allList.get(i);
							errorList.add(error4);
							continue;
						}
						//数字ﾁｪｯｸ
						if ( currentShohinKeyKb == 0 || currentShohinKeyKb ==1 ){
							if ( !Util.chkNumeric(allList.get(i)) ){
								if ( !Util.isNullOrEmpty(error3) ) error3 += ",";
								error3 += allList.get(i);
								errorList.add(error3);
								continue;
							}
						}
						shohinInfo = shohinDao.select(h2, appConfig.getOfflineMode(), currentShohinKeyKb, allList.get(i));

						//商品の存在ﾁｪｯｸ
						if (shohinInfo.getMaxRowCount() == 0){
							if ( !Util.isNullOrEmpty(error1) ) error1 += ",";
							error1 += allList.get(i);
							errorList.add(allList.get(i));
						}
						//商品が複数存在ﾁｪｯｸ
						else if( shohinInfo.getMaxRowCount() > 1 ){
							if ( !Util.isNullOrEmpty(error2) ) error2 += ",";
							error2 += allList.get(i);
//							errorList.add(allList.get(i));
							list.add(allList.get(i));
						}else{
							list.add(allList.get(i));
						}
					}
					//ｴﾗｰあり
					if ( errorList.size() > 0 ){
						String errorMsg = "";
						if ( !Util.isNullOrEmpty(error4) ){
							errorMsg += "「" + error4 + "」 の桁数がオーバーしています。" + "\n";
						}
						if ( !Util.isNullOrEmpty(error3) ){
							errorMsg += "「" + error3 + "」が数字ではありません。" + "\n";
						}
						if ( !Util.isNullOrEmpty(error1) ){
							errorMsg += "「" + error1 + "」がマスタに存在しません。" + "\n";
						}
						if ( !Util.isNullOrEmpty(error2) ){
							errorMsg += "「" + error2 + "」の商品が複数存在しています。" + "\n";
						}
						messageBox.disp(MB_CRITICAL, errorMsg, strMsgTitle);
					}

				}catch(TException te){
					messageBox.disp(te, MB_CRITICAL, te.toString() + "\n" + te.getMessage(), strMsgTitle);
					return ;
				}finally{
					try{
						h2.h2ClientStop();
					}catch(TException ex){}
				}
			}

			//ログ出力
			logOut.info("画面【" + dlg.getTitle() + "】を閉じました。");
			dlg.dispose();
		}
	}

	class WindowListener extends WindowAdapter{

		@Override
		public void windowActivated(WindowEvent arg0) {
			super.windowActivated(arg0);
			cboShohinKeyKb.requestFocus();
		}
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
			dlg.setSize(new Dimension(325, 507));//360, 257
			dlg.setTitle("商品指定");
			//画面の中央に表示
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (screen.width - dlg.getWidth()) / 2;
			int y = (screen.height - dlg.getHeight()) / 2;
			dlg.setLocation(x, y);
			dlg.addWindowListener(new WindowListener());
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
			jContentPane.add(getJPanel(), null);
			jContentPane.add(getJPanelFkey(), null);
	        jContentPane.add(getJScroolPane(), null);
			jContentPane.add(getFKey(), null);
		}
		return jContentPane;
	}

	private JScrollPane getJScroolPane(){
		if (scroll == null){
			edit = new JTextArea();
			edit.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.BOLD, 18));
			edit.enableInputMethods(false);
//			edit.getInputContext().setCharacterSubsets(null);
			scroll = new JScrollPane(edit);
			scroll.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 18));
			scroll.setBounds(new Rectangle(40, 65, 242, 330));//237
	        scroll.setRowHeaderView(new LineNumberView(edit));
		}
		return scroll;
	}
	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {

			tlblShohinKeyKb = new TLabel();
			tlblShohinKeyKb.setText("商品キー");
			tlblShohinKeyKb.setBounds(new Rectangle(15, 20, 121, 31));
			lblShohinKeyKb = new JLabel();
			lblShohinKeyKb.setText("");
			lblShohinKeyKb.setBounds(new Rectangle(135, 20, 121, 31));
			lblShohinKeyKb.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			cboShohinKeyKb = new TComboBox();
			cboShohinKeyKb.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			cboShohinKeyKb.setBounds(new Rectangle(140, 22, 111, 26));
			cboShohinKeyKb.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			cboShohinKeyKb.addKeyListener(new KeyAdapter(){
				public void keyPressed(KeyEvent e) {
					if ( e.getKeyCode() == KeyEvent.VK_ENTER ){
						edit.requestFocus();
					}
				}
			});

			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setBounds(new Rectangle(25, 5, 258, 60));
//			jPanel.setBorder(BorderFactory.createTitledBorder(null, "≪特別販売掛率設定≫", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));
			jPanel.add(lblShohinKeyKb, null);
			jPanel.add(cboShohinKeyKb, null);
			jPanel.add(tlblShohinKeyKb, null);

		}
		return jPanel;
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
			jPanelFkey.setBounds(new Rectangle(30, 420, 260, 46));
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
		if (fKey == null) {
			int[] intFormat = new int[] { 9, 12 };
			fKey = new FKey(intFormat);
			fKey.setLayout(null);
			fKey.setBounds(jPanelFkey.getBounds());
			fKey.setF9Text("F9　クリア");
			fKey.setF12Text("F12　確定");
		}
		return fKey;
	}

	public List<String> getShohinKeyList() {
		return list;
	}

	public int getShohinKeyKb(){
		return currentShohinKeyKb;
	}

	class LineNumberView extends JComponent {
		private static final long serialVersionUID = 1L;
		private static final int MARGIN = 5;
	    private final JTextArea text;
	    private final FontMetrics fontMetrics;
	    private final int topInset;
	    private final int fontAscent;
	    private final int fontHeight;

	    public LineNumberView(JTextArea textArea) {
	        text = textArea;
	        Font font   = text.getFont();
	        fontMetrics = getFontMetrics(font);
	        fontHeight  = fontMetrics.getHeight();
	        fontAscent  = fontMetrics.getAscent();
	        topInset    = text.getInsets().top;
	        text.getDocument().addDocumentListener(new DocumentListener() {
	            public void insertUpdate(DocumentEvent e) {
	                repaint();
	            }
	            public void removeUpdate(DocumentEvent e) {
	                repaint();
	            }
	            public void changedUpdate(DocumentEvent e) {}
	        });
	        text.addComponentListener(new ComponentAdapter() {
	            public void componentResized(ComponentEvent e) {
	                revalidate();
	                repaint();
	            }
	        });
	        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
	        setOpaque(true);
	        setBackground(Color.WHITE);
	    }
	    private int getComponentWidth() {
	        Document doc  = text.getDocument();
	        Element root  = doc.getDefaultRootElement();
	        int lineCount = root.getElementIndex(doc.getLength());
	        int maxDigits = Math.max(3, String.valueOf(lineCount).length());
	        return maxDigits*fontMetrics.stringWidth("0")+MARGIN*2;
	    }
	    public int getLineAtPoint(int y) {
	        Element root = text.getDocument().getDefaultRootElement();
	        int pos = text.viewToModel(new Point(0, y));
	        return root.getElementIndex(pos);
	    }
	    public Dimension getPreferredSize() {
	        return new Dimension(getComponentWidth(), text.getHeight());
	    }
	    public void paintComponent(Graphics g) {
	        Rectangle clip = g.getClipBounds();
	        g.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 18));
	        g.setColor(getBackground());
	        g.fillRect(clip.x, clip.y, clip.width, clip.height);
	        g.setColor(getForeground());
	        int base  = clip.y - topInset;
	        int start = getLineAtPoint(base);
	        int end   = getLineAtPoint(base+clip.height);
	        int y = topInset-fontHeight+fontAscent+start*fontHeight;
	        for(int i=start;i<=end;i++) {
	            String text = String.valueOf(i+1);
	            int x = getComponentWidth()-MARGIN-fontMetrics.stringWidth(text);
	            y = y + fontHeight;
	            g.drawString(text, x, y);
	        }
	    }
	}
}