package blove.kmm.fund.support.price;

import java.util.List;

public class SinaFundInfo {
	private SinaFundResult result;

	public SinaFundResult getResult() {
		return result;
	}

	public void setResult(SinaFundResult result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "SinaFundInfo [\n\tresult=" + result + "\n]";
	}

	public static class SinaFundResult {
		private SinaFundResultData data;
		private SinaFundResultStatus status;

		public SinaFundResultData getData() {
			return data;
		}

		public void setData(SinaFundResultData data) {
			this.data = data;
		}

		public SinaFundResultStatus getStatus() {
			return status;
		}

		public void setStatus(SinaFundResultStatus status) {
			this.status = status;
		}

		@Override
		public String toString() {
			return "SinaFundResult [\n\tdata=" + data + "\n\tstatus=" + status + "\n]";
		}

	}

	public static class SinaFundResultStatus {
		private int code;

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		@Override
		public String toString() {
			return "SinaFundResultStatus [code=" + code + "]";
		}

	}

	public static class SinaFundResultData {
		private List<SinaFundDayPrice> data;
		private int total_num;

		public List<SinaFundDayPrice> getData() {
			return data;
		}

		public void setData(List<SinaFundDayPrice> data) {
			this.data = data;
		}

		public int getTotal_num() {
			return total_num;
		}

		public void setTotal_num(int total_num) {
			this.total_num = total_num;
		}

		@Override
		public String toString() {
			return "SinaFundResultData [\n\tdata=" + data + "\n\ttotal_num=" + total_num + "\n]";
		}

	}

	public static class SinaFundDayPrice {
		private String fbrq;
		private double jjjz;
		private double ljjz;

		public String getFbrq() {
			return fbrq;
		}

		public void setFbrq(String fbrq) {
			this.fbrq = fbrq;
		}

		public double getJjjz() {
			return jjjz;
		}

		public void setJjjz(double jjjz) {
			this.jjjz = jjjz;
		}

		public double getLjjz() {
			return ljjz;
		}

		public void setLjjz(double ljjz) {
			this.ljjz = ljjz;
		}

		@Override
		public String toString() {
			return "SinaFundDayPrice [fbrq=" + fbrq + ", jjjz=" + jjjz + ", ljjz=" + ljjz + "]";
		}

	}
}
