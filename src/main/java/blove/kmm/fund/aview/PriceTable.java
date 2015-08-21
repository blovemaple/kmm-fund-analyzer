package blove.kmm.fund.aview;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import blove.kmm.fund.aview.util.PropertyLabelFactory;
import blove.kmm.fund.biz.FundBiz;
import blove.kmm.fund.biz.bo.DatePrice;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class PriceTable extends TableView<DatePrice> {
	private StringProperty fundId = new SimpleStringProperty();
	private ObjectProperty<LocalDate> fromDate = new SimpleObjectProperty<>();
	private ObjectProperty<LocalDate> toDate = new SimpleObjectProperty<>();

	private BooleanProperty waiting = new SimpleBooleanProperty(false);

	private final FundBiz biz;

	@SuppressWarnings("unchecked")
	public PriceTable(FundBiz biz) {
		this.biz = biz;

		// 添加列
		List<TableColumn<DatePrice, ?>> columns = new ArrayList<>();
		columns.add(geneColumn("日期", DatePrice.PROPERTY_DATE, false));
		columns.add(geneColumn("净值", DatePrice.PROPERTY_PRICE, true));
		columns.add(geneColumn("日增幅", DatePrice.PROPERTY_INCREASE_RATE, true));
		columns.add(geneColumn("持有均价", DatePrice.PROPERTY_AVG_PRICE, true));
		getColumns().addAll(columns.stream().toArray(TableColumn[]::new));

		// 业务逻辑：根据属性获取价格列表并填入内容列表
		fundId.addListener((p, oldValue, newValue) -> refresh());
		fromDate.addListener((p, oldValue, newValue) -> refresh());
		toDate.addListener((p, oldValue, newValue) -> refresh());
		refresh();
	}

	private TableColumn<DatePrice, Label> geneColumn(String showName, String property, boolean rightAlignment) {
		TableColumn<DatePrice, Label> column = new TableColumn<>(showName);
		column.setCellValueFactory(PropertyLabelFactory.forTable(property));
		if (rightAlignment) {
			column.setStyle("-fx-alignment: CENTER-RIGHT;");
		}
		return column;
	}

	private Task<ObservableList<DatePrice>> queryTask = null;

	private synchronized void refresh() {
		if (queryTask != null) {
			queryTask.cancel();
		}

		setItems(FXCollections.emptyObservableList());

		// 检查fromDate和toDate有没有，没有的话直接清空内容
		if (fundId.get() == null || fromDate.get() == null || toDate.get() == null) {
			return;
		}

		waiting.set(true);

		// 请求新的列表
		queryTask = new Task<ObservableList<DatePrice>>() {
			@Override
			protected ObservableList<DatePrice> call() throws Exception {
				return biz.getDatePrice(fundId.get(), fromDate.get(), toDate.get())
						.sorted(Comparator.comparing(DatePrice::getDate).reversed());
			}

			@Override
			protected void succeeded() {
				synchronized (PriceTable.this) {
					try {
						setItems(get());
						waiting.set(false);
						autosize();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			protected void failed() {
				getException().printStackTrace();
				waiting.set(false);
			}
		};
		new Thread(queryTask).start();
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

	public final ObjectProperty<LocalDate> fromDateProperty() {
		return this.fromDate;
	}

	public final java.time.LocalDate getFromDate() {
		return this.fromDateProperty().get();
	}

	public final void setFromDate(final java.time.LocalDate fromDate) {
		this.fromDateProperty().set(fromDate);
	}

	public final ObjectProperty<LocalDate> toDateProperty() {
		return this.toDate;
	}

	public final java.time.LocalDate getToDate() {
		return this.toDateProperty().get();
	}

	public final void setToDate(final java.time.LocalDate toDate) {
		this.toDateProperty().set(toDate);
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
