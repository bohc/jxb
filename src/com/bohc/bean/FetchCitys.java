package com.bohc.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bohc.sh.entities.Tarea;

public class FetchCitys {
	private Date startdate;// ��ʼץȡ����
	private Date enddate;// ����ץȡ����
	private int overupnum = 0;// �س̶�ץȡ������
	private int rate = 0;// �����۸�
	private boolean fetchnext = false;// ץȡ�ڶ���
	private int intervaltime = 2;// ץ�����ʱ��
	private int intervaltimeend = 6;// ץ�����ʱ��
	private List<Tarea> fromcitys = new ArrayList<Tarea>();// ��������
	private List<Tarea> tocitys = new ArrayList<Tarea>();// �������
	private boolean ckgxhb;// ������
	private boolean cktdc;// ͷ�Ȳ�
	private boolean ckdhb;// ˫�����
	private boolean ckbcg;// ץȡ���ɹ�
	private boolean ck_zzhb;// ץȡ��ת����
	private boolean ckp0;// ��ץȡ�۸�Ϊ0�ĺ���
	private boolean ck_fetch_qunar;// ��ȥ�Ķ�ץȡ����
	private boolean ck_fetch_aliy;// �Ӱ���ץȡ����
	private int cururl = 1;// ��¼��ǰץȡ�����ĸ���ַ, 1 Ϊȥ�Ķ��� 2 Ϊ����
	private int flagfetchrandmax = 8;// ������ַ����ץȡ�������
	private int flagfetchrandmin = 3;// ������ַ����ץȡ����С��

	private boolean ckmorning, cknoon, ckanoon, ckevening;// ץȡ�����������ʶ

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
