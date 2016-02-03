package blove.kmm.fund.biz.bo;

import java.time.LocalDate;

import blove.kmm.fund.aview.util.Percentagized;
import blove.kmm.fund.aview.util.Precised;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FundInfo {
	private StringProperty fundId = new SimpleStringProperty();
	private StringProperty fundName = new SimpleStringProperty();
	public static final String PROPERTY_FUND_NAME = "fundName";
	private StringProperty categoryName = new SimpleStringProperty();

	// 持仓部分的购买金额加上购买手续费
	@ShowName("持仓成本")
	@Precised(2)
	private DoubleProperty myOriCost = new SimpleDoubleProperty();
	@ShowName("持仓份数")
	@Precised(2)
	private DoubleProperty myQuantity = new SimpleDoubleProperty();
	@ShowName("持仓均价")
	@Precised(4)
	private DoubleProperty myAvgPrice = new SimpleDoubleProperty();// auto
	@ShowName("持仓市值")
	@Precised(2)
	private DoubleProperty myAmount = new SimpleDoubleProperty();// auto
	public static final String PROPERTY_MY_AMOUNT = "myAmount";

	@ShowName("最新净值")
	@Precised(4)
	private DoubleProperty crtPrice = new SimpleDoubleProperty();
	@ShowName("最新净值日期")
	private ObjectProperty<LocalDate> crtPriceDate = new SimpleObjectProperty<>();
	@ShowName("浮动收益")
	@Precised(2)
	private DoubleProperty crtProfit = new SimpleDoubleProperty();// auto
	public static final String PROPERTY_CRT_PROFIT = "crtProfit";
	@ShowName("浮动收益率")
	@Percentagized(2)
	private DoubleProperty crtProfitRate = new SimpleDoubleProperty();// auto
	public static final String PROPERTY_CRT_PROFIT_RATE = "crtProfitRate";

	// 总购买金额加上购买手续费
	@ShowName("总购买成本")
	@Precised(2)
	private DoubleProperty totalCost = new SimpleDoubleProperty();
	// 总赎回金额除去赎回手续费
	@ShowName("总赎回入账")
	@Precised(2)
	private DoubleProperty totalSellGet = new SimpleDoubleProperty();
	@ShowName("历史分红")
	@Precised(2)
	private DoubleProperty totalDividen = new SimpleDoubleProperty();
	@ShowName("历史收益")
	@Precised(2)
	private DoubleProperty totalProfit = new SimpleDoubleProperty();
	@ShowName("历史收益率")
	@Percentagized(2)
	private DoubleProperty totalProfitRate = new SimpleDoubleProperty();// auto

	{
		myAvgPrice.bind(myOriCost.divide(myQuantity));
		myAmount.bind(crtPrice.multiply(myQuantity));

		crtProfit.bind(myAmount.subtract(myOriCost));
		crtProfitRate.bind(crtProfit.divide(myOriCost));

		totalProfitRate.bind(totalProfit.add(totalDividen).divide(totalCost.subtract(myOriCost)));
	}

	public final DoubleProperty myOriCostProperty() {
		return this.myOriCost;
	}

	public final double getMyOriCost() {
		return this.myOriCostProperty().get();
	}

	public final void setMyOriCost(final double myOriCost) {
		this.myOriCostProperty().set(myOriCost);
	}

	public final DoubleProperty myQuantityProperty() {
		return this.myQuantity;
	}

	public final double getMyQuantity() {
		return this.myQuantityProperty().get();
	}

	public final void setMyQuantity(final double myQuantity) {
		this.myQuantityProperty().set(myQuantity);
	}

	public final DoubleProperty myAvgPriceProperty() {
		return this.myAvgPrice;
	}

	public final double getMyAvgPrice() {
		return this.myAvgPriceProperty().get();
	}

	public final DoubleProperty myAmountProperty() {
		return this.myAmount;
	}

	public final double getMyAmount() {
		return this.myAmountProperty().get();
	}

	public final DoubleProperty crtPriceProperty() {
		return this.crtPrice;
	}

	public final double getCrtPrice() {
		return this.crtPriceProperty().get();
	}

	public final void setCrtPrice(final double crtPrice) {
		this.crtPriceProperty().set(crtPrice);
	}

	public final DoubleProperty crtProfitProperty() {
		return this.crtProfit;
	}

	public final double getCrtProfit() {
		return this.crtProfitProperty().get();
	}

	public final DoubleProperty totalCostProperty() {
		return this.totalCost;
	}

	public final double getTotalCost() {
		return this.totalCostProperty().get();
	}

	public final void setTotalCost(final double totalCost) {
		this.totalCostProperty().set(totalCost);
	}

	public final DoubleProperty totalSellGetProperty() {
		return this.totalSellGet;
	}

	public final double getTotalSellGet() {
		return this.totalSellGetProperty().get();
	}

	public final void setTotalSellGet(final double totalSellGet) {
		this.totalSellGetProperty().set(totalSellGet);
	}

	public final DoubleProperty totalDividenProperty() {
		return this.totalDividen;
	}

	public final double getTotalDividen() {
		return this.totalDividenProperty().get();
	}

	public final void setTotalDividen(final double totalDividen) {
		this.totalDividenProperty().set(totalDividen);
	}

	public final DoubleProperty totalProfitProperty() {
		return this.totalProfit;
	}

	public final double getTotalProfit() {
		return this.totalProfitProperty().get();
	}

	public final void setTotalProfit(final double totalProfit) {
		this.totalProfitProperty().set(totalProfit);
	}

	public final StringProperty fundIdProperty() {
		return this.fundId;
	}

	public final java.lang.String getFundId() {
		return this.fundIdProperty().get();
	}

	public final void setFundId(final java.lang.String fundId) {
		this.fundIdProperty().set(fundId);
	}

	public final StringProperty fundNameProperty() {
		return this.fundName;
	}

	public final java.lang.String getFundName() {
		return this.fundNameProperty().get();
	}

	public final void setFundName(final java.lang.String fundName) {
		this.fundNameProperty().set(fundName);
	}

	public final DoubleProperty crtProfitRateProperty() {
		return this.crtProfitRate;
	}

	public final double getCrtProfitRate() {
		return this.crtProfitRateProperty().get();
	}

	public final DoubleProperty totalProfitRateProperty() {
		return this.totalProfitRate;
	}

	public final double getTotalProfitRate() {
		return this.totalProfitRateProperty().get();
	}

	public final StringProperty categoryNameProperty() {
		return this.categoryName;
	}

	public final java.lang.String getCategoryName() {
		return this.categoryNameProperty().get();
	}

	public final void setCategoryName(final java.lang.String categoryName) {
		this.categoryNameProperty().set(categoryName);
	}

	public final ObjectProperty<LocalDate> crtPriceDateProperty() {
		return this.crtPriceDate;
	}

	public final java.time.LocalDate getCrtPriceDate() {
		return this.crtPriceDateProperty().get();
	}

	public final void setCrtPriceDate(final java.time.LocalDate crtPriceDate) {
		this.crtPriceDateProperty().set(crtPriceDate);
	}

	@Override
	public String toString() {
		return "FundInfo [\n\tfundId=" + fundId.get() + "\n\tfundName=" + fundName.get() + "\n\tmyOriAmount="
				+ myOriCost.get() + "\n\tmyQuantity=" + myQuantity.get() + "\n\tmyAvgPrice=" + myAvgPrice.get()
				+ "\n\tmyAmount=" + myAmount.get() + "\n\tcrtPrice=" + crtPrice.get() + "\n\tcrtProfit="
				+ crtProfit.get() + "\n\tcrtProfitRate=" + crtProfitRate.get() + "\n\ttotalBuy=" + totalCost.get()
				+ "\n\ttotalSell=" + totalSellGet.get() + "\n\ttotalDividen=" + totalDividen.get() + "\n\ttotalProfit="
				+ totalProfit.get() + "\n\ttotalProfitRate=" + totalProfitRate.get() + "\n]";
	}

}
