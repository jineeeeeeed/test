package jp.co.css.TTAX;

public class SendNoBean {

	private int tenpocd;//店舗コード
	private int denpyoNen;//伝票年
	private int denpyoKb;//伝票区分
	private int denpyoNo;//伝票番号
//	private int denpyoGyo;//伝票行位置
	private String sendNo;//送信番号


	public int getTenpocd() {
		return tenpocd;
	}
	public void setTenpocd(int tenpocd) {
		this.tenpocd = tenpocd;
	}
	public int getDenpyoNen() {
		return denpyoNen;
	}
	public void setDenpyoNen(int denpyoNen) {
		this.denpyoNen = denpyoNen;
	}
	public int getDenpyoKb() {
		return denpyoKb;
	}
	public void setDenpyoKb(int denpyoKb) {
		this.denpyoKb = denpyoKb;
	}
	public int getDenpyoNo() {
		return denpyoNo;
	}
	public void setDenpyoNo(int denpyoNo) {
		this.denpyoNo = denpyoNo;
	}
//	public int getDenpyoGyo() {
//		return denpyoGyo;
//	}
//	public void setDenpyoGyo(int denpyoGyo) {
//		this.denpyoGyo = denpyoGyo;
//	}
	public String getSendNo() {
		return sendNo;
	}
	public void setSendNo(String sendNo) {
		this.sendNo = sendNo;
	}

}
