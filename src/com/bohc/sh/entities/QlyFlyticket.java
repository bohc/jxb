/**
 *柏红春
 *Fri May 29 10:37:16 CST 2015
 */
package com.bohc.sh.entities;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class QlyFlyticket implements Serializable {
	/**
	 * 自增主键
	 */
	private Integer id;
	/**
	 * 航班号
	 */
	private String fltno;
	/**
	 * 航空公司
	 */
	private String aireline;
	/**
	 * 机型
	 */
	private String airtype;
	/**
	 * 出发城市
	 */
	private String startcity;
	/**
	 * 出发城市简码
	 */
	private String startcityjm;
	/**
	 * 到达城市
	 */
	private String arrivecity;
	/**
	 * 到达城市简码
	 */
	private String arrivecityjm;
	/**
	 * 出发机场
	 */
	private String startairport;
	/**
	 * 到达机场
	 */
	private String arriveairport;
	/**
	 * 出发时间
	 */
	private Date starttime;
	/**
	 * 到达时间
	 */
	private Date arrivetime;
	/**
	 * 偏差率
	 */
	private String offsetrate;
	/**
	 * 偏差时间
	 */
	private String offsettime;
	/**
	 * 机票价格
	 */
	private Integer ticketprice;
	/**
	 * 机票价格
	 */
	private Integer avgticketprice;
	/**
	 * 代理售票点
	 */
	private String agent;
	/**
	 * 机票日期
	 */
	private Date fetchdate;
	/**
	 * 操作时间
	 */
	private Date opttime;
	/**
	 * 航班时间分类:早上，中午，下午，晚上
	 * 
	 * @param id
	 */
	private String typetime;
	/**
	 * 中转航班号
	 */
	private String changeflyno;
	/**
	 * 中转城市
	 */
	private String changecity;
	/**
	 * 自定义的航班类型：直达，经停，转停，中转
	 */
	private String linetype;
	/**
	 * 是否共享航班
	 */
	private String shareline;
	/**
	 * 座位级别
	 */
	private String sitlevel;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFltno() {
		return fltno;
	}

	public void setFltno(String fltno) {
		this.fltno = fltno;
	}

	public String getAireline() {
		return aireline;
	}

	public void setAireline(String aireline) {
		this.aireline = aireline;
	}

	public String getAirtype() {
		return airtype;
	}

	public void setAirtype(String airtype) {
		this.airtype = airtype;
	}

	public String getStartcity() {
		return startcity;
	}

	public void setStartcity(String startcity) {
		this.startcity = startcity;
	}

	public String getStartcityjm() {
		return startcityjm;
	}

	public void setStartcityjm(String startcityjm) {
		this.startcityjm = startcityjm;
	}

	public String getArrivecity() {
		return arrivecity;
	}

	public void setArrivecity(String arrivecity) {
		this.arrivecity = arrivecity;
	}

	public String getArrivecityjm() {
		return arrivecityjm;
	}

	public void setArrivecityjm(String arrivecityjm) {
		this.arrivecityjm = arrivecityjm;
	}

	public String getStartairport() {
		return startairport;
	}

	public void setStartairport(String startairport) {
		this.startairport = startairport;
	}

	public String getArriveairport() {
		return arriveairport;
	}

	public void setArriveairport(String arriveairport) {
		this.arriveairport = arriveairport;
	}

	public Date getStarttime() {
		return starttime;
	}

	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}

	public Date getArrivetime() {
		return arrivetime;
	}

	public void setArrivetime(Date arrivetime) {
		this.arrivetime = arrivetime;
	}

	public String getOffsetrate() {
		return offsetrate;
	}

	public void setOffsetrate(String offsetrate) {
		this.offsetrate = offsetrate;
	}

	public String getOffsettime() {
		return offsettime;
	}

	public void setOffsettime(String offsettime) {
		this.offsettime = offsettime;
	}

	public Integer getTicketprice() {
		return ticketprice;
	}

	public void setTicketprice(Integer ticketprice) {
		this.ticketprice = ticketprice;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public Integer getAvgticketprice() {
		return avgticketprice;
	}

	public void setAvgticketprice(Integer avgticketprice) {
		this.avgticketprice = avgticketprice;
	}

	public Date getFetchdate() {
		return fetchdate;
	}

	public void setFetchdate(Date fetchdate) {
		this.fetchdate = fetchdate;
	}

	public Date getOpttime() {
		return opttime;
	}

	public void setOpttime(Date opttime) {
		this.opttime = opttime;
	}

	public String getTypetime() {
		return typetime;
	}

	public void setTypetime(String typetime) {
		this.typetime = typetime;
	}

	public String getChangeflyno() {
		return changeflyno;
	}

	public void setChangeflyno(String changeflyno) {
		this.changeflyno = changeflyno;
	}

	public String getChangecity() {
		return changecity;
	}

	public void setChangecity(String changecity) {
		this.changecity = changecity;
	}

	public String getLinetype() {
		return linetype;
	}

	public void setLinetype(String linetype) {
		this.linetype = linetype;
	}

	public String getShareline() {
		return shareline;
	}

	public void setShareline(String shareline) {
		this.shareline = shareline;
	}

	public String getSitlevel() {
		return sitlevel;
	}

	public void setSitlevel(String sitlevel) {
		this.sitlevel = sitlevel;
	}

	@Override
	public String toString() {
		return "QlyFlyticket [id=" + id + ", fltno=" + fltno + ", aireline=" + aireline + ", airtype=" + airtype + ", startcity=" + startcity + ", startcityjm=" + startcityjm
				+ ", arrivecity=" + arrivecity + ", arrivecityjm=" + arrivecityjm + ", startairport=" + startairport + ", arriveairport=" + arriveairport + ", starttime="
				+ starttime + ", arrivetime=" + arrivetime + ", offsetrate=" + offsetrate + ", offsettime=" + offsettime + ", ticketprice=" + ticketprice + ", avgticketprice="
				+ avgticketprice + ", agent=" + agent + ", fetchdate=" + fetchdate + ", opttime=" + opttime + ", typetime=" + typetime + ", changeflyno=" + changeflyno
				+ ", changecity=" + changecity + ", linetype=" + linetype + ", shareline=" + shareline + ", sitlevel=" + sitlevel + "]";
	}

}
