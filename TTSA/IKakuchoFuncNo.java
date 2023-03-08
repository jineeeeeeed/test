package jp.co.css.TTSA;

public interface IKakuchoFuncNo {
	static final int RETURN_SELECT_KAKAKUKOTEi_SELECT_ALL = 1;	//価格固定(全行)
	static final int RETURN_SELECT_KAKAKUKOTEi_KAIJO_ALL = 2;	//価格固定解除(全行)
	static final int RETURN_SELECT_KOUKAI_SELECT_ALL = 3;		//公開(全行)
	static final int RETURN_SELECT_KOUKAI_KAIJO_ALL = 4;		//公開解除(全行)
	static final int RETURN_SELECT_SHUPPINHUKA = 5;				//出品不可一括
	static final int RETURN_SELECT_SHUPPINKA = 6;				//出品可一括
	static final int RETURN_SELECT_POSBAIKAHENKOU = 7;			//POS売価一括変更
}