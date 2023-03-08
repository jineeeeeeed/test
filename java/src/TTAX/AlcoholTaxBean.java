package jp.co.css.TTAX;

public class AlcoholTaxBean {
	//酒税適用有無（物品）
	private String strLqIndividual = "";
	//（酒税）品目分類
	private int intLqCode = 0;
	//（酒税）品目分類名称
	private String strLqCodeNm = "";
	//（酒税）税率
	private int intLqTaxRate = 0;
	//（酒税）容器容量
	private int intLqCapacity = 0;
	//（酒税）本数
	private int intLqNumber = 0;

	public String getStrLqIndividual() {
		return strLqIndividual;
	}
	public void setStrLqIndividual(String strLqIndividual) {
		this.strLqIndividual = strLqIndividual;
	}
	public int getIntLqCode() {
		return intLqCode;
	}
	public void setIntLqCode(int intLqCode) {
		this.intLqCode = intLqCode;
	}
	public String getStrLqCodeNm() {
		return strLqCodeNm;
	}
	public void setStrLqCodeNm(String strLqCodeNm) {
		this.strLqCodeNm = strLqCodeNm;
	}
	public int getIntLqTaxRate() {
		return intLqTaxRate;
	}
	public void setIntLqTaxRate(int intLqTaxRate) {
		this.intLqTaxRate = intLqTaxRate;
	}
	public int getIntLqCapacity() {
		return intLqCapacity;
	}
	public void setIntLqCapacity(int intLqCapacity) {
		this.intLqCapacity = intLqCapacity;
	}
	public int getIntLqNumber() {
		return intLqNumber;
	}
	public void setIntLqNumber(int intLqNumber) {
		this.intLqNumber = intLqNumber;
	}
}
