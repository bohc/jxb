package com.bohc.bean;

import java.util.Date;

import com.bohc.sh.entities.QlyFlyticket;

public class FetchAirLine {
	private String webflag;// 到哪个网站抓取
	private String url;// 去哪儿网址
	private String aliyurl;// 阿里网址
	private String fromcity;
	private String fromcityjm;
	private String tocity;
	private String tocityjm;
	private Date flydate;
	private boolean fetching = false;// 是否继续抓取 false ,默认为抓取, true 为不抓取
	private int level = 0;// 0 表示第一层，1 表示直达第二层， 2 表示中转第二层；
	private QlyFlyticket qft;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFromcity() {
		return fromcity;
	}

	public void setFromcity(String fromcity) {
		this.fromcity = fromcity;
	}

	public String getFromcityjm() {
		return fromcityjm;
	}

	public void setFromcityjm(String fromcityjm) {
		this.fromcityjm = fromcityjm;
	}

	public String getTocity() {
		return tocity;
	}

	public void setTocity(String tocity) {
		this.tocity = tocity;
	}

	public String getTocityjm() {
		return tocityjm;
	}

	public void setTocityjm(String tocityjm) {
		this.tocityjm = tocityjm;
	}

	public Date getFlydate() {
		return flydate;
	}

	public void setFlydate(Date flydate) {
		this.flydate = flydate;
	}

	public boolean isFetching() {
		return fetching;
	}

	public void setFetching(boolean fetching) {
		this.fetching = fetching;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public QlyFlyticket getQft() {
		return qft;
	}

	public void setQft(QlyFlyticket qft) {
		this.qft = qft;
	}

	public String getWebflag() {
		return webflag;
	}

	public void setWebflag(String webflag) {
		this.webflag = webflag;
	}

	public String getAliyurl() {
		return aliyurl;
	}

	public void setAliyurl(String aliyurl) {
		this.aliyurl = aliyurl;
	}

	@Override
	public String toString() {
		return "FetchAirLine [webflag=" + webflag + ", url=" + url + ", aliyurl=" + aliyurl + ", fromcity=" + fromcity + ", fromcityjm=" + fromcityjm + ", tocity=" + tocity + ", tocityjm=" + tocityjm + ", flydate="
				+ flydate + ", fetching=" + fetching + ", level=" + level + ", qft=" + qft + "]";
	}

}
