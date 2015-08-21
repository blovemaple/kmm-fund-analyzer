package blove.kmm.fund.aview;

import java.time.LocalDate;

import blove.kmm.fund.biz.FundBiz;
import blove.kmm.fund.biz.bo.DateRange;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

public class DateSelector extends FlowPane {
	private StringProperty fundId = new SimpleStringProperty();
	private ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
	private ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();

	private BooleanProperty waiting = new SimpleBooleanProperty(false);

	private FundBiz biz;

	private CheckBox autoCheckBox;
	private DatePicker startDatePicker, endDatePicker;

	public DateSelector(FundBiz biz) {
		super(10, 10);
		this.biz = biz;

		// 自动选择日期的复选框
		autoCheckBox = new CheckBox("自动选择日期");
		getChildren().add(autoCheckBox);

		// 起始日期
		getChildren().add(new Label("起始日期"));
		startDatePicker = new DatePicker();
		getChildren().add(startDatePicker);

		// 结束日期
		getChildren().add(new Label("结束日期"));
		endDatePicker = new DatePicker();
		getChildren().add(endDatePicker);

		// 显示基金ID
		Label waitingLabel = new Label();
		waitingLabel.setTextFill(Color.RED);
		getChildren().add(waitingLabel);

		// 绑定属性关系
		startDate.bindBidirectional(startDatePicker.valueProperty());
		endDate.bindBidirectional(endDatePicker.valueProperty());
		startDatePicker.disableProperty().bind(autoCheckBox.selectedProperty());
		endDatePicker.disableProperty().bind(autoCheckBox.selectedProperty());
		waitingLabel.textProperty().bind(Bindings.createStringBinding(() -> waiting.get() ? "请求中……" : "", waiting));

		// 选择自动日期或fundId改变后自动计算日期
		autoCheckBox.selectedProperty().addListener((property, oldValue, newValue) -> autoSelectDate());
		fundId.addListener((property, oldValue, newValue) -> autoSelectDate());

		autoCheckBox.setSelected(true);

	}

	private void autoSelectDate() {
		if (autoCheckBox.isSelected()) {
			DateRange dateRange = biz.getAutoDateRange(fundId.get());
			startDate.set(dateRange.getStartDate());
			endDate.set(dateRange.getEndDate());
		}
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

	public final BooleanProperty waitingProperty() {
		return this.waiting;
	}

	public final boolean isWaiting() {
		return this.waitingProperty().get();
	}

	public final void setWaiting(final boolean waiting) {
		this.waitingProperty().set(waiting);
	}

}
