ｑpackage jp.co.css.TTAX;

import java.util.Date;

public class CertBean {
	private String cert = "";
	private String certClass = "";
	private String pKey = "";
	private Date expiry = null;
	public String getCert() {
		return cert;
	}
	public void setCert(String cert) {
		this.cert = cert;
	}
	public String getCertClass() {
		return certClass;
	}
	public void setCertClass(String certClass) {
		this.certClass = certClass;
	}
	public String getPKey() {
		return pKey;
	}
	public void setPKey(String pKey) {
		this.pKey = pKey;
	}
	public Date getExpiry() {
		return expiry;
	}
	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}
	

	
}
