package jp.co.css.TTAX;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.FileInputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import jp.co.css.webpos.common.util.Util;

public class ReadKeyStore {

	public CertBean openPkcs12(String keyfile, String keypass) {
		if (Util.isNullOrEmpty(keyfile)) {
			return null;
		}
		CertBean certbean = new CertBean();
		try {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(keyfile);

			char[] nPassword = null;
			if ((keypass == null) || keypass.trim().equals("")) {
				nPassword = null;
			} else {
				nPassword = keypass.toCharArray();
			}
			ks.load(fis, nPassword);
			fis.close();

			KeyStore jksKeystore = KeyStore.getInstance("JKS");
			jksKeystore.load(null, nPassword);

			String keyAlias = "";
			Enumeration enums = ks.aliases();
			while (enums.hasMoreElements()) {
				keyAlias = (String) enums.nextElement();
				if (ks.isKeyEntry(keyAlias)) {
					Key key = ks.getKey(keyAlias, nPassword);
					Certificate[] certChain = ks.getCertificateChain(keyAlias);
					PrivateKey privateKey = (PrivateKey) ks.getKey(keyAlias,nPassword);
					certbean.setPKey(Base64.encode(privateKey.getEncoded()));
					jksKeystore.setKeyEntry(keyAlias, key, nPassword, certChain);
				} else if (ks.isCertificateEntry(keyAlias)) {
					Certificate cert = ks.getCertificate(keyAlias);
//					if (cert instanceof X509Certificate) {
//						X509Certificate[] certificates = new X509Certificate[] { (X509Certificate) cert };
//					}
					PublicKey publicKey = ((X509Certificate) cert).getPublicKey();
					certbean.setPKey(Base64.encode(publicKey.getEncoded()));
					jksKeystore.setCertificateEntry(keyAlias, cert);
				} else {
					throw new GeneralSecurityException(keyAlias + " is unknown to this keystore");
				}
			}
			Certificate cert = jksKeystore.getCertificate(keyAlias);
			certbean.setCert(Base64.encode(cert.getEncoded()));
			if ("X.509".equals(cert.getType())) {
				certbean.setExpiry(((X509Certificate) cert).getNotAfter());
//				((X509Certificate) cert).getPublicKey();
			}
//			PublicKey pubkey = cert.getPublicKey();
//			
//			certbean.setPKey(Base64.encode(pubkey.getEncoded()));
		} catch (Exception e) {
			return null;
		}
		return certbean;
	}

}
