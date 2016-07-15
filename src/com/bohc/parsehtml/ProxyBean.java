package com.bohc.parsehtml;

public class ProxyBean {
	private String ip;
	private Integer port;
	private String proxytype;
	private String proxyadd;
	private String realip;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getProxytype() {
		return proxytype;
	}

	public void setProxytype(String proxytype) {
		this.proxytype = proxytype;
	}

	public String getProxyadd() {
		return proxyadd;
	}

	public void setProxyadd(String proxyadd) {
		this.proxyadd = proxyadd;
	}

	public String getRealip() {
		return realip;
	}

	public void setRealip(String realip) {
		this.realip = realip;
	}

	@Override
	public String toString() {
		return ip + "\t" + port + "\t" + proxytype + "\t\t" + proxyadd + "\t\t" + realip;
	}

}
