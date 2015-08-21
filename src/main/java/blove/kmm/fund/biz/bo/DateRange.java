package blove.kmm.fund.biz.bo;

import java.time.LocalDate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class DateRange {
	private ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
	private ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();

	public DateRange(LocalDate startDate, LocalDate endDate) {
		setStartDate(startDate);
		setEndDate(endDate);
	}

	public final ObjectProperty<LocalDate> startDateProperty() {
		return this.startDate;
	}

	public final java.time.LocalDate getStartDate() {
		return this.startDateProperty().get();
	}

	public final void setStartDate(final java.time.LocalDate startDate) {
		this.startDateProperty().set(startDate);
	}

	public final ObjectProperty<LocalDate> endDateProperty() {
		return this.endDate;
	}

	public final java.time.LocalDate getEndDate() {
		return this.endDateProperty().get();
	}

	public final void setEndDate(final java.time.LocalDate endDate) {
		this.endDateProperty().set(endDate);
	}

}
