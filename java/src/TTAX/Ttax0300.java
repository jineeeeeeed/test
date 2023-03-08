package jp.co.css.TTAX;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import jp.co.css.base.AppConfig;
import jp.co.css.base.TFrame;
import jp.co.css.webpos.common.gui.TComboBox;
import jp.co.css.webpos.common.gui.TLabel;
import jp.co.css.webpos.common.gui.TTabbedPane;
import jp.co.css.webpos.common.gui.TTextArea;
/*******************************************************************************
 * 処理名称	：	免税記載事項設定	<br>
 * 作成日 	：	2019/01/22	<br>
 * 作成者	：	鄧　日佳	<br>
 ******************************************************************************/
public class Ttax0300 extends TFrame {

	private static final long serialVersionUID = 1L;

	public JPanel jPanelKey = null;

	public JPanel jPanel1 =null; //購入記録票

	public JPanel jPanel2 =null; //購入者誓約書

	public JPanel jPanelMsg1 = null;
	public JPanel jPanelMsg2 = null;
	public JPanel jPanelMsg3 = null;
	public JPanel jPanelMsg4 = null;

	public TTabbedPane tabbedPane = null;
	//言語区分
	public TComboBox cboLangKb =null;

	//購入記録表
	public TTextArea txtKirokuhyoTop;//印字位置　上
	public TTextArea txtKirokuhyoBut;//印字位置　下
	//購入者誓約書
	public TTextArea txtSeiyakushoTop;//印字位置　上
	public TTextArea txtSeiyakushoBut;//印字位置　下

	/**
	 * This is the default constructor
	 */
	public Ttax0300(AppConfig appConfig) {
		super(appConfig);
		this.setTitle("免税記載事項設定　TTAX0300");
		lblTitle.setText("　免税記載事項設定");
		jMainPanel.add(getjPanelKey(),null);

		tabbedPane = new TTabbedPane();
		tabbedPane.setBounds(new Rectangle(15, 140, 985, 526));
		tabbedPane.addTab("購入記録票", getjPanel1());
		tabbedPane.addTab("購入者誓約書", getjPanel2());
		jMainPanel.add(tabbedPane,null);
	}

	private JPanel getjPanelKey() {
		if (jPanelKey == null) {
			jPanelKey = new JPanel();
			jPanelKey.setLayout(null);
			jPanelKey.setBounds(new Rectangle(20, 60, 550, 70));
			jPanelKey.setBorder(BorderFactory.createTitledBorder(null, "KEY項目", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));
			jPanelKey.setVisible(true);

			TLabel tlblTitle = new TLabel();
			tlblTitle.setText("言語区分");
			tlblTitle.setBounds(new Rectangle(10, 25, 121, 31));
			JLabel lblWaku = new JLabel();
			lblWaku.setText("");
			lblWaku.setBounds(new Rectangle(130, 25, 161, 31));
			lblWaku.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
			cboLangKb = new TComboBox();
			cboLangKb.setBounds(new Rectangle(133, 28, 155, 25));
			cboLangKb.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

			jPanelKey.add(lblWaku, null);
			jPanelKey.add(tlblTitle, null);
			jPanelKey.add(cboLangKb, null);
		}
		return jPanelKey;
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getjPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(null);
			jPanel1.add(getjPanelMsg1(),null);
			jPanel1.add(getjPanelMsg2(),null);
		}
		return jPanel1;
	}
	private JPanel getjPanelMsg1() {
		if(jPanelMsg1 == null) {
			jPanelMsg1=new JPanel();
			jPanelMsg1.setLayout(null);
			jPanelMsg1.setBounds(new Rectangle(10,20,470,260));
			jPanelMsg1.setBorder(BorderFactory.createTitledBorder(null, "≪印字位置　上≫", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));

			txtKirokuhyoTop= new TTextArea();
			txtKirokuhyoTop.setBounds(new Rectangle(10, 30, 450, 210));
			txtKirokuhyoTop.setEnabled(true);
			txtKirokuhyoTop.setFont(new Font("Dialog", Font.PLAIN, 16));
			txtKirokuhyoTop.setLineWrap(true);
			txtKirokuhyoTop.setSaveStringValue1("≪印字位置　上≫");

			JScrollPane messageScroll1 = new JScrollPane(txtKirokuhyoTop);
			messageScroll1.setFont(new Font("Dialog", Font.PLAIN, 16));
			messageScroll1.setBounds(new Rectangle(10, 30, 450, 210));
			messageScroll1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			jPanelMsg1.add(messageScroll1,null);
		}
		return jPanelMsg1;
	}
	private JPanel getjPanelMsg2() {
		if(jPanelMsg2 == null) {
			jPanelMsg2=new JPanel();
			jPanelMsg2.setLayout(null);
			jPanelMsg2.setBounds(new Rectangle(495,20,470,260));
			jPanelMsg2.setBorder(BorderFactory.createTitledBorder(null, "≪印字位置　下≫", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));

			txtKirokuhyoBut= new TTextArea();
			txtKirokuhyoBut.setBounds(new Rectangle(10, 30, 450, 210));
			txtKirokuhyoBut.setEnabled(true);
			txtKirokuhyoBut.setFont(new Font("Dialog", Font.PLAIN, 16));
			txtKirokuhyoBut.setLineWrap(true);
			txtKirokuhyoBut.setSaveStringValue1("≪印字位置　下≫");

			JScrollPane messageScroll2 = new JScrollPane(txtKirokuhyoBut);
			messageScroll2.setFont(new Font("Dialog", Font.PLAIN, 16));
			messageScroll2.setBounds(new Rectangle(10, 30, 450, 210));
			messageScroll2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			jPanelMsg2.add(messageScroll2,null);
		}
		return jPanelMsg2;
	}

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getjPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.setLayout(null);
			jPanel2.add(getjPanelMsg3(),null);
			jPanel2.add(getjPanelMsg4(),null);
		}
		return jPanel2;
	}
	private JPanel getjPanelMsg3() {
		if(jPanelMsg3 == null) {
			jPanelMsg3=new JPanel();
			jPanelMsg3.setLayout(null);
			jPanelMsg3.setBounds(new Rectangle(10,20,470,260));
			jPanelMsg3.setBorder(BorderFactory.createTitledBorder(null, "≪印字位置　上≫", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));

			txtSeiyakushoTop= new TTextArea();
			txtSeiyakushoTop.setBounds(new Rectangle(10, 30, 450, 210));
			txtSeiyakushoTop.setEnabled(true);
			txtSeiyakushoTop.setFont(new Font("Dialog", Font.PLAIN, 16));
			txtSeiyakushoTop.setLineWrap(true);
			txtSeiyakushoTop.setSaveStringValue1("≪印字位置　上≫");

			JScrollPane messageScroll3 = new JScrollPane(txtSeiyakushoTop);
			messageScroll3.setFont(new Font("\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", Font.PLAIN, 16));
			messageScroll3.setBounds(new Rectangle(10, 30, 450, 210));
			messageScroll3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			jPanelMsg3.add(messageScroll3,null);
		}
		return jPanelMsg3;
	}
	private JPanel getjPanelMsg4() {
		if(jPanelMsg4 == null) {
			jPanelMsg4=new JPanel();
			jPanelMsg4.setLayout(null);
			jPanelMsg4.setBounds(new Rectangle(495,20,470,260));
			jPanelMsg4.setBorder(BorderFactory.createTitledBorder(null, "≪印字位置　下≫", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), Color.gray));

			txtSeiyakushoBut= new TTextArea();
			txtSeiyakushoBut.setBounds(new Rectangle(10, 30, 450, 210));
			txtSeiyakushoBut.setEnabled(true);
			txtSeiyakushoBut.setFont(new Font("Dialog", Font.PLAIN, 16));
			txtSeiyakushoBut.setLineWrap(true);
			txtSeiyakushoBut.setSaveStringValue1("≪印字位置　下≫");

			JScrollPane messageScroll4 = new JScrollPane(txtSeiyakushoBut);
			messageScroll4.setFont(new Font("Dialog", Font.PLAIN, 16));
			messageScroll4.setBounds(new Rectangle(10, 30, 450, 210));
			messageScroll4.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			jPanelMsg4.add(messageScroll4,null);
		}
		return jPanelMsg4;
	}
}
