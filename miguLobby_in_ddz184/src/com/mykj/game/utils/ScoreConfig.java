package com.mykj.game.utils;


public class ScoreConfig {

	 
	public static String getShowNamefromScore(int gameID,int score){
		
		if(gameID==AppConfig.GAMEID_DDZ){  //斗地主
			if(score>=0 && score<=50){
				return "平民百姓";
			}else if(score>50 && score<=100){
				return "少林弟子";
			}else if(score>100 && score<=200){
				return "武林新丁";
			}else if(score>200 && score<=400){
				return "江湖小虾	";
			}else if(score>400 && score<=700){
				return "后起之秀";
			}else if(score>700 && score<=1000){
				return "武林高手";
			}else if(score>1000 && score<=1500){
				return "江湖隐侠";
			}else if(score>1500 && score<=2200){
				return "风尘奇侠";
			}else if(score>2200 && score<=3000){
				return "无双隐士";
			}else if(score>3000 && score<=5000){
				return "世外高人";
			}else if(score>5000 && score<=10000){
				return "无敌圣者";
			}else if(score>10000){
				return "三界贤君";
			}else{
				return "";
			}
		}else if(gameID==AppConfig.GAMEID_WQ){  //围棋
			if(score>=0 && score<=50){
				return "围棋学徒";
			}else if(score>50 && score<=200){
				return "名师高徒";
			}else if(score>200 && score<=600){
				return "职业选手";
			}else if(score>600 && score<=1200){
				return " 围棋高手	";
			}else if(score>1200 && score<=2000){
				return "围棋大师 ";
			}else if(score>2000 && score<=3000){
				return " 国际名师";
			}else if(score>3000){
				return "一代宗师";
			}
		}else if(gameID==AppConfig.GAMEID_XQ){   //象棋
			if(score>=0 && score<10){
				return "预备棋士";
			}else if(score>=10 && score<30){
				return "九级棋士";
			}else if(score>=30 && score<80){
				return "八级棋士";
			}else if(score>=80 && score<200){
				return "七级棋士";
			}else if(score>=200 && score<400){
				return "六级棋士 ";
			}else if(score>=400 && score<700){
				return " 五级棋士";
			}else if(score>=700&& score<1100){
				return "四级棋士";
			}else if(score>=1100&& score<1600){
				return "三级棋士";
			}else if(score>=1600&& score<2200){
				return "二级棋士";
			}else if(score>=2200&& score<2900){
				return "一级棋士";
			}else if(score>=2900&& score<3700){
				return "象棋大师";
			}else if(score>=3700&& score<4600){
				return "特级大师";
			}else if(score>=4600&& score<5600){
				return "国际大师";
			}else if(score>=5600){
				return "国际大师";
			} 
		}else if(gameID==AppConfig.GAMEID_WZQ){  //五子棋
			if(score>=0 && score<=50){
				return "棋童";
			}else if(score>50 && score<=100){
				return "棋师";
			}else if(score>100 && score<=200){
				return "一段";
			}else if(score>200 && score<=300){
				return "二段";
			}else if(score>300 && score<=500){
				return "三段";
			}else if(score>500 && score<=700){
				return "四段";
			}else if(score>700&& score<=1000){
				return "五段";
			}else if(score>1000&& score<=1500){
				return "六段";
			}else if(score>1500&& score<=2000){
				return "七段";
			}else if(score>2000&& score<=3000){
				return "八段";
			}else if(score>3000){
				return "九段";
			} 
		}
		return "";
	}
	
	
}
