package com.mykj.andr.model;

import java.io.Serializable;

import android.graphics.Bitmap;

/**
 * 抽奖机奖品实例
 * 
 * @author JiangYinZhi
 * 
 */
public class LotteryDrowPrize implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 图片的x坐标 */
	private float imgPosX;
	/** 图片的y坐标 */
	private float imgPosY;
	/** 文字的x坐标 */
	private float txtPosX;
	/** 文字的y坐标 */
	private float txtPosY;
	/** 数字的x坐标 */
	private float numPosX;
	/** 数字的y坐标 */
	private float numPosY;
	/** 一键抽奖结束获奖数量X坐标 */
	private float multiNumPosX;
	/** 一键抽奖结束获奖数量Y坐标 */
	private float multiNumPoxY;

	/** 索引 */
	private int index;
	/** 类型,2为乐豆 */
	private int type;
	/** id */
	private int id;
	/** 数量 */
	private int num;
	/** 一键抽奖获奖数量 */
	private int prizeNum;
	/** 名称 */
	private String name;
	/** 文件路径名称 */
	private String fileName;
	/** 图片实例 */
	private Bitmap prizeBitmap;
	/** 数量需要显示的图片 */
	private Bitmap numBitmap;
	/** 奖品数量动画的图片 */
	private Bitmap numAnimBitmap;
	/** 一键抽奖动画展示图片 */
	private Bitmap animShowBitmap;
	/** 奖品背景图片1、3、5、7、9、11、13、15为bg1图片2,4、8、10、12、16为bg2图片，6和14为bg3图片 */
	private Bitmap prizeBGBitmap;
	/** 是否被选中 */
	private boolean isSelected = false;

	/**
	 * 
	 * @param index
	 *            索引
	 */
	public LotteryDrowPrize(int index) {
		this.index = index;
	}

	public float getImgPosX() {
		return imgPosX;
	}

	public void setImgPosX(float imgPosX) {
		this.imgPosX = imgPosX;
	}

	public float getImgPosY() {
		return imgPosY;
	}

	public void setImgPosY(float imgPosY) {
		this.imgPosY = imgPosY;
	}

	public float getTxtPosX() {
		return txtPosX;
	}

	public void setTxtPosX(float txtPosX) {
		this.txtPosX = txtPosX;
	}

	public float getTxtPosY() {
		return txtPosY;
	}

	public void setTxtPosY(float txtPosY) {
		this.txtPosY = txtPosY;
	}

	public float getNumPosX() {
		return numPosX;
	}

	public void setNumPosX(float numPosX) {
		this.numPosX = numPosX;
	}

	public float getNumPosY() {
		return numPosY;
	}

	public void setNumPosY(float numPosY) {
		this.numPosY = numPosY;
	}

	public float getMultiNumPosX() {
		return multiNumPosX;
	}

	public void setMultiNumPosX(float multiNumPosX) {
		this.multiNumPosX = multiNumPosX;
	}

	public float getMultiNumPoxY() {
		return multiNumPoxY;
	}

	public void setMultiNumPoxY(float multiNumPoxY) {
		this.multiNumPoxY = multiNumPoxY;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Bitmap getPrizeBitmap() {
		return prizeBitmap;
	}

	public void setPrizeBitmap(Bitmap prizeBitmap) {
		this.prizeBitmap = prizeBitmap;
	}

	public Bitmap getNumBitmap() {
		return numBitmap;
	}

	public void setNumBitmap(Bitmap numBitmap) {
		this.numBitmap = numBitmap;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public Bitmap getNumAnimBitmap() {
		return numAnimBitmap;
	}

	public void setNumAnimBitmap(Bitmap numAnimBitmap) {
		this.numAnimBitmap = numAnimBitmap;
	}

	public int getPrizeNum() {
		return prizeNum;
	}

	public void setPrizeNum(int prizeNum) {
		this.prizeNum = prizeNum;
	}

	public Bitmap getAnimShowBitmap() {
		return animShowBitmap;
	}

	public void setAnimShowBitmap(Bitmap animShowBitmap) {
		this.animShowBitmap = animShowBitmap;
	}

	public Bitmap getPrizeBGBitmap() {
		return prizeBGBitmap;
	}

	public void setPrizeBGBitmap(Bitmap prizeBGBitmap) {
		this.prizeBGBitmap = prizeBGBitmap;
	}

}
