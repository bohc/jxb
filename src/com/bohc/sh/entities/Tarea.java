package com.bohc.sh.entities;

public class Tarea {
	private int aid;
	private String acode, ircode, area, areapy, pid, pname, remark;

	public int getAid() {
		return aid;
	}

	public void setAid(int aid) {
		this.aid = aid;
	}

	public String getAcode() {
		return acode;
	}

	public void setAcode(String acode) {
		this.acode = acode;
	}

	public String getIrcode() {
		return ircode;
	}

	public void setIrcode(String ircode) {
		this.ircode = ircode;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getAreapy() {
		return areapy;
	}

	public void setAreapy(String areapy) {
		this.areapy = areapy;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getPname() {
		return pname;
	}

	public void setPname(String pname) {
		this.pname = pname;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Override
	public String toString() {
		return "Tarea [aid=" + aid + ", " + (acode != null ? "acode=" + acode + ", " : "") + (ircode != null ? "ircode=" + ircode + ", " : "")
				+ (area != null ? "area=" + area + ", " : "") + (areapy != null ? "areapy=" + areapy + ", " : "") + (pid != null ? "pid=" + pid + ", " : "")
				+ (pname != null ? "pname=" + pname + ", " : "") + (remark != null ? "remark=" + remark : "") + "]";
	}

}
