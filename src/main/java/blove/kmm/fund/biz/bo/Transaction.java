package blove.kmm.fund.biz.bo;

import java.time.LocalDate;

import blove.kmm.fund.aview.util.Percentagized;
import blove.kmm.fund.aview.util.Precised;
import blove.kmm.fund.support.db.TransactionType;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * 交易。
 * 
 * @author blove
 */
public class Transaction {
	private ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
	public static final String PROPERTY_DATE = "date";
	private ObjectProperty<TransactionType> type = new SimpleObjectProperty<>();
	public static final String PROPERTY_TYPE = "type";
	@Precised(2)
	private DoubleProperty amount = new SimpleDoubleProperty();
	public static final String PROPERTY_AMOUNT = "amount";
	@Precised(2)
	private DoubleProperty fee = new SimpleDoubleProperty();
	public static final String PROPERTY_FEE = "fee";
	@Precised(2)
	private DoubleProperty quantity = new SimpleDoubleProperty();
	public static final String PROPERTY_QUANTITY = "quantity";
	@Precised(4)
	private DoubleProperty price = new SimpleDoubleProperty();
	public static final String PROPERTY_PRICE = "price";
	@Precised(2)
	private ObjectProperty<Double> profit = new SimpleObjectProperty<>();
	public static final String PROPERTY_PROFIT = "profit";
	@Percentagized(2)
	private DoubleProperty profitRate = new SimpleDoubleProperty(Double.NaN);
	public static final String PROPERTY_PROFIT_RATE = "profitRate";

	public final ObjectProperty<LocalDate> dateProperty() {
		return this.date;
	}

	public final LocalDate getDate() {
		return this.dateProperty().get();
	}

	public final void setDate(final LocalDate date) {
		this.dateProperty().set(date);
	}

	public final ObjectProperty<TransactionType> typeProperty() {
		return this.type;
	}

	public final TransactionType getType() {
		return this.typeProperty().get();
	}

	public final void setType(final TransactionType type) {
		this.typeProperty().set(type);
	}

	public final DoubleProperty amountProperty() {
		return this.amount;
	}

	public final double getAmount() {
		return this.amountProperty().get();
	}

	public final void setAmount(final double amount) {
		this.amountProperty().set(amount);
	}

	public final DoubleProperty quantityProperty() {
		return this.quantity;
	}

	public final double getQuantity() {
		return this.quantityProperty().get();
	}

	public final void setQuantity(final double quantity) {
		this.quantityProperty().set(quantity);
	}

	public final DoubleProperty priceProperty() {
		return this.price;
	}

	public final double getPrice() {
		return this.priceProperty().get();
	}

	public final void setPrice(final double price) {
		this.priceProperty().set(price);
	}

	public final ObjectProperty<Double> profitProperty() {
		return this.profit;
	}

	public final java.lang.Double getProfit() {
		return this.profitProperty().get();
	}

	public final void setProfit(final java.lang.Double profit) {
		this.profitProperty().set(profit);
	}

	public final DoubleProperty profitRateProperty() {
		return this.profitRate;
	}

	public final double getProfitRate() {
		return this.profitRateProperty().get();
	}

	public final void setProfitRate(final double profitRate) {
		this.profitRateProperty().set(profitRate);
	}

	public final DoubleProperty feeProperty() {
		return this.fee;
	}

	public final double getFee() {
		return this.feeProperty().get();
	}

	public final void setFee(final double fee) {
		this.feeProperty().set(fee);
	}

	@Override
	public String toString() {
		return "Transaction [\n\tdate=" + date.get() + "\n\ttype=" + type.get() + "\n\tamount=" + amount.get()
				+ "\n\tquantity=" + quantity.get() + "\n\tprice=" + price.get() + "\n\tprofit=" + profit.get()
				+ "\n\tprofitRate=" + profitRate.get() + "\n]";
	}

}
