package com.bohc.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bohc.sh.entities.Tarea;

public class FetchCitys {
	private Date startdate;// 开始抓取日期
	private Date enddate;// 结束抓取日期
	private int overupnum = 0;// 回程多抓取的天数
	private int rate = 0;// 浮动价格
	private boolean fetchnext = false;// 抓取第二层
	private int intervaltime = 2;// 抓到间隔时间
	private int intervaltimeend = 6;// 抓到间隔时间
	private List<Tarea> fromcitys = new ArrayList<Tarea>();// 出发城市
	private List<Tarea> tocitys = new ArrayList<Tarea>();// 到达城市
	private boolean ckgxhb;// 共享航班
	private boolean cktdc;// 头等舱
	private boolean ckdhb;// 双航班号
	private boolean ckbcg;// 抓取不成功
	private boolean ck_zzhb;// 抓取中转航班
	private boolean ckp0;// 不抓取价格为0的航班
	private boolean ck_fetch_qunar;// 从去哪儿抓取航班
	private boolean ck_fetch_aliy;// 从阿里抓取航班
	private int cururl = 1;// 记录当前抓取的是哪个地址, 1 为去哪儿， 2 为阿里
	private int flagfetchrandmax = 8;// 两个地址连续抓取的最大数
	private int flagfetchrandmin = 3;// 两个地址连续抓取的最小数

	private boolean ckmorning, cknoon, ckanoon, ckevening;// 抓取的早中晚航班标识

	public int getOverupnum() {
		return overupnum;
	}

	public void setOverupnum(int overupnum) {
		this.overupnum = overupnum;
	}

	public List<Tarea> getFromcitys() {
		return fromcitys;
	}

	public void setFromcitys(List<Tarea> fromcitys) {
		this.fromcitys = fromcitys;
	}

	public List<Tarea> getTocitys() {
		return tocitys;
	}

	public void setTocitys(List<Tarea> tocitys) {
		this.tocitys = tocitys;
	}

	public boolean isFetchnext() {
		return fetchnext;
	}

	public void setFetchnext(boolean fetchnext) {
		this.fetchnext = fetchnext;
	}

	public int getIntervaltime() {
		return intervaltime;
	}

	public void setIntervaltime(int intervaltime) {
		this.intervaltime = intervaltime;
	}

	public int getIntervaltimeend() {
		return intervaltimeend;
	}

	public void setIntervaltimeend(int intervaltimeend) {
		this.intervaltimeend = intervaltimeend;
	}

	public Date getStartdate() {
		return startdate;
	}

	public void setStartdate(Date startdate) {
		this.startdate = startdate;
	}

	public Date getEnddate() {
		return enddate;
	}

	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public boolean isCkgxhb() {
		return ckgxhb;
	}

	public void setCkgxhb(boolean ckgxhb) {
		this.ckgxhb = ckgxhb;
	}

	public boolean isCktdc() {
		return cktdc;
	}

	public void setCktdc(boolean cktdc) {
		this.cktdc = cktdc;
	}

	public boolean isCkdhb() {
		return ckdhb;
	}

	public void setCkdhb(boolean ckdhb) {
		this.ckdhb = ckdhb;
	}

	public boolean isCkbcg() {
		return ckbcg;
	}

	public void setCkbcg(boolean ckbcg) {
		this.ckbcg = ckbcg;
	}

	public boolean isCk_zzhb() {
		return ck_zzhb;
	}

	public void setCk_zzhb(boolean ck_zzhb) {
		this.ck_zzhb = ck_zzhb;
	}

	public boolean isCkmorning() {
		return ckmorning;
	}

	public void setCkmorning(boolean ckmorning) {
		this.ckmorning = ckmorning;
	}

	public boolean isCknoon() {
		return cknoon;
	}

	public void setCknoon(boolean cknoon) {
		this.cknoon = cknoon;
	}

	public boolean isCkanoon() {
		return ckanoon;
	}

	public void setCkanoon(boolean ckanoon) {
		this.ckanoon = ckanoon;
	}

	public boolean isCkevening() {
		return ckevening;
	}

	public void setCkevening(boolean ckevening) {
		this.ckevening = ckevening;
	}

	public boolean isCkp0() {
		return ckp0;
	}

	public void setCkp0(boolean ckp0) {
		this.ckp0 = ckp0;
	}

	public boolean isCk_fetch_qunar() {
		return ck_fetch_qunar;
	}

	public void setCk_fetch_qunar(boolean ck_fetch_qunar) {
		this.ck_fetch_qunar = ck_fetch_qunar;
	}

	public boolean isCk_fetch_aliy() {
		return ck_fetch_aliy;
	}

	public void setCk_fetch_aliy(boolean ck_fetch_aliy) {
		this.ck_fetch_aliy = ck_fetch_aliy;
	}

	public int getCururl() {
		return cururl;
	}

	public void setCururl(int cururl) {
		this.cururl = cururl;
	}

	public int getFlagfetchrandmax() {
		return flagfetchrandmax;
	}

	public void setFlagfetchrandmax(int flagfetchrandmax) {
		this.flagfetchrandmax = flagfetchrandmax;
	}

	public int getFlagfetchrandmin() {
		return flagfetchrandmin;
	}

	public void setFlagfetchrandmin(int flagfetchrandmin) {
		this.flagfetchrandmin = flagfetchrandmin;
	}

}
