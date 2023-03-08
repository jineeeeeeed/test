package jp.co.jp.css.functionDao;

import jp.co.css.base.AppConfig;
import jp.co.css.bean.DbInfo;
import jp.co.css.dao.BaseDAO;
import jp.co.css.dao.ChubunruiMstDAO;
import jp.co.css.dao.DaibunruiMstDAO;
import jp.co.css.dao.ShobunruiMstDAO;
import jp.co.css.webpos.common.except.TException;

/**
 * 商品マスタの取得、書き込みを行う。
 * 
 * @author 2008/07/29 S.Ohmori
 * 
 */
public class GetZeiKbDAO extends BaseDAO {

	public GetZeiKbDAO(AppConfig appConfig) {
		super(appConfig);
	}

	public int getZeiKb(int dai,int chu,int sho) throws TException{
 		ShobunruiMstDAO shodao=new ShobunruiMstDAO(appConfig);
		DbInfo db=shodao.select(sho);
		if( db != null ) return -1;
		if(db.getIntItem("KAZEIKB")==0){
			ChubunruiMstDAO chudao=new ChubunruiMstDAO(appConfig);
			db=chudao.select(chu);
			if(db.getIntItem("KAZEIKB")==0){
				DaibunruiMstDAO daidao=new DaibunruiMstDAO(appConfig);
				db=daidao.select(dai);
				if(db.getIntItem("KAZEIKB")==0){
					return 0;
				}
			}
		}
		return db.getIntItem("KAZEIKB");
	}
}
