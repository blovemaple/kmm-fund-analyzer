package blove.kmm.fund.support.db;

public enum TransactionType {
	BUY("购买"), SELL("赎回"), DIVIDEN("分红"), REINVEST("红利再投"), ADD("份额增加");

	private String showName;

	private TransactionType(String showName) {
		this.showName = showName;
	}

	@Override
	public String toString() {
		return showName;
	}
}