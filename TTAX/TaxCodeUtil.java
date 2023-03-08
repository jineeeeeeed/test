package jp.co.css.TTAX;

import java.util.LinkedHashMap;
import java.util.Map;

public class TaxCodeUtil {
	
	
	public Map<String, String> getStatMap(){
		Map<String, String> hashMap = new LinkedHashMap<String, String>();
		hashMap.put("11","短期滞在");
		hashMap.put("14","外交");
		hashMap.put("17","公用");
		hashMap.put("21","芸術");
		hashMap.put("24","宗教");
		hashMap.put("27","報道");
		hashMap.put("31","法律・会計業務");
		hashMap.put("34","医療");
		hashMap.put("37","興行");
		hashMap.put("41","文化活動");
		hashMap.put("44","留学");
		hashMap.put("47","研修");
		hashMap.put("51","家族滞在");
		hashMap.put("54","特定活動");
		hashMap.put("81","日本人の配偶者等");
		hashMap.put("85","永住者の配偶者等");
		hashMap.put("91","上陸許可書による入国");
		hashMap.put("95","非居住者に該当する日本国籍の者");
		hashMap.put("99","その他");
		
		return hashMap;
	}
	
	public Map<String, String> getLiquorMap(){
		Map<String, String> hashMap = new LinkedHashMap<String, String>();
		hashMap.put("110","清酒");
		hashMap.put("115","清酒（発泡）");
		hashMap.put("117","清酒（発泡（本則））");
		hashMap.put("150","合成清酒");
		hashMap.put("152","合成清酒（措置法）");
		hashMap.put("155","合成清酒（発泡）");
		hashMap.put("157","合成清酒（発泡（本則））");
		hashMap.put("210","連続式蒸留焼酎");
		hashMap.put("215","連続式蒸留焼酎（発泡）");
		hashMap.put("217","連続式蒸留焼酎（発泡（本則））");
		hashMap.put("250","単式蒸留焼酎");
		hashMap.put("255","単式蒸留焼酎（発泡）");
		hashMap.put("257","単式蒸留焼酎（発泡（本則））");
		hashMap.put("310","みりん");
		hashMap.put("311","みりん（措置法１）");
		hashMap.put("312","みりん（措置法２）");
		hashMap.put("313","みりん（措置法３）");
		hashMap.put("315","みりん（発泡）");
		hashMap.put("317","みりん（発泡（本則））");
		hashMap.put("350","ビール");
		hashMap.put("410","果実酒");
		hashMap.put("415","果実酒（発泡）");
		hashMap.put("417","果実酒（発泡（本則））");
		hashMap.put("450","甘味果実酒");
		hashMap.put("455","甘味果実酒（発泡）");
		hashMap.put("457","甘味果実酒（発泡（本則））");
		hashMap.put("510","ウイスキー");
		hashMap.put("515","ウイスキー（発泡）");
		hashMap.put("517","ウイスキー（発泡（本則））");
		hashMap.put("550","ブランデー");
		hashMap.put("555","ブランデー（発泡）");
		hashMap.put("557","ブランデー（発泡（本則））");
		hashMap.put("570","原料用アルコール");
		hashMap.put("581","発泡酒(１)（麦芽比率50％以上又はアルコール分10度以上）");
		hashMap.put("582","発泡酒(２)（麦芽比率25％以上50％未満）");
		hashMap.put("583","発泡酒(３)（その他）");
		hashMap.put("591","その他の醸造酒");
		hashMap.put("595","その他の醸造酒（発泡）");
		hashMap.put("597","その他の醸造酒（発泡（本則））");
		hashMap.put("610","スピリッツ");
		hashMap.put("615","スピリッツ（発泡）");
		hashMap.put("617","スピリッツ（発泡（本則））");
		hashMap.put("710","リキュール");
		hashMap.put("715","リキュール（発泡）");
		hashMap.put("717","リキュール（発泡（本則））");
		hashMap.put("820","粉末酒");
		hashMap.put("830","その他の雑酒");
		hashMap.put("831","その他の雑酒（みりん類似）");
		hashMap.put("833","その他の雑酒（みりん類似・措置法適用分１）");
		hashMap.put("834","その他の雑酒（みりん類似・措置法適用分２）");
		hashMap.put("838","その他の雑酒（みりん類似・措置法適用分３）");
		hashMap.put("832","その他の雑酒（その他のもの）");
		hashMap.put("837","その他の雑酒（その他のもの・発泡）");
		hashMap.put("850","雑酒");
		hashMap.put("852","雑酒（みりん類似）");
		hashMap.put("855","雑酒（発泡）");
		hashMap.put("857","雑酒（発泡（本則））");
		hashMap.put("000","全酒類");
		
		return hashMap;
	}
}
