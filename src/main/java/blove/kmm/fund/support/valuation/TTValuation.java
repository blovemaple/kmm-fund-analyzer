package blove.kmm.fund.support.valuation;

import java.util.List;

public class TTValuation {
	/**
	 * 估值时间，YYYY-MM-DD
	 */
	private String gztime;
	/**
	 * 昨日单位净值
	 */
	private Double zrdwjz;
	/**
	 * 估值数据，["id,MM:SS,估值与昨日净值相比的涨幅，无百分号"]
	 */
	private List<String> gzdata;

	public String getGztime() {
		return gztime;
	}

	public void setGztime(String gztime) {
		this.gztime = gztime;
	}

	public Double getZrdwjz() {
		return zrdwjz;
	}

	public void setZrdwjz(Double zrdwjz) {
		this.zrdwjz = zrdwjz;
	}

	public List<String> getGzData() {
		return gzdata;
	}

	public void setGzdata(List<String> gzdata) {
		this.gzdata = gzdata;
	}

	@Override
	public String toString() {
		return "TTValuation [\n\tgztime=" + gztime + "\n\tzrdwjz=" + zrdwjz + "\n\tgzdata=" + gzdata + "\n]";
	}

}
