package blove.kmm.fund.biz.bo;

import java.time.LocalDate;

import blove.kmm.fund.aview.util.Percentagized;
import blove.kmm.fund.aview.util.Precised;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * 单日价格。
 * 
 * @author blove
 */
public class DatePrice {
	private ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
	public static final String PROPERTY_DATE = "date";

	@Precised(4)
	private ObjectProperty<Double> price = new SimpleObjectProperty<>();
	public static final String PROPERTY_PRICE = "price";

	@Percentagized(2)
	private ObjectProperty<Double> increaseRate = new SimpleObjectProperty<>();
	public static final String PROPERTY_INCREASE_RATE = "increaseRate";

	@Precised(4)
	private ObjectProperty<Double> avgPrice = new SimpleObjectProperty<>();
	public static final String PROPERTY_AVG_PRICE = "avgPrice";

	public DatePrice(LocalDate date) {
		this.date.set(date);
	}

	public DatePrice() {
	}

	public final ObjectProperty<LocalDate> dateProperty() {
		return this.date;
	}

	public final java.time.LocalDate getDate() {
		return this.dateProperty().get();
	}

	public final void setDate(final java.time.LocalDate date) {
		this.dateProperty().set(date);
	}

	public final ObjectProperty<Double> priceProperty() {
		return this.price;
	}

	public final java.lang.Double getPrice() {
		return this.priceProperty().get();
	}

	public final void setPrice(final java.lang.Double price) {
		this.priceProperty().set(price);
	}

	public final ObjectProperty<Double> increaseRateProperty() {
		return this.increaseRate;
	}

	public final java.lang.Double getIncreaseRate() {
		return this.increaseRateProperty().get();
	}

	public final void setIncreaseRate(final java.lang.Double increaseRate) {
		this.increaseRateProperty().set(increaseRate);
	}

	public final ObjectProperty<Double> avgPriceProperty() {
		return this.avgPrice;
	}

	public final java.lang.Double getAvgPrice() {
		return this.avgPriceProperty().get();
	}

	public final void setAvgPrice(final java.lang.Double avgPrice) {
		this.avgPriceProperty().set(avgPrice);
	}

	@Override
	public String toString() {
		return "DatePrice [date=" + date.get() + ", price=" + price.get() + ", avgPrice=" + avgPrice.get() + "]";
	}

}
