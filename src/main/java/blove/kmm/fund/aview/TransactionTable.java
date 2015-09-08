package blove.kmm.fund.aview;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import blove.kmm.fund.aview.util.PropertyLabelFactory;
import blove.kmm.fund.biz.FundBiz;
import blove.kmm.fund.biz.bo.Transaction;
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

public class TransactionTable extends TableView<Transaction> {
	private StringProperty fundId = new SimpleStringProperty();
	private ObjectProperty<LocalDate> fromDate = new SimpleObjectProperty<>();
	private ObjectProperty<LocalDate> toDate = new SimpleObjectProperty<>();

	private BooleanProperty waiting = new SimpleBooleanProperty(false);

	private final FundBiz biz;

	@SuppressWarnings("unchecked")
	public TransactionTable(FundBiz biz) {
		this.biz = biz;

		// 添加列
		List<TableColumn<Transaction, ?>> columns = new ArrayList<>();
		columns.add(geneColumn("交易日期", Transaction.PROPERTY_DATE, false));
		columns.add(geneColumn("交易类型", Transaction.PROPERTY_TYPE, false));
		columns.add(geneColumn("交易金额", Transaction.PROPERTY_AMOUNT, true));
		columns.add(geneColumn("手续费", Transaction.PROPERTY_FEE, true));
		columns.add(geneColumn("交易份数", Transaction.PROPERTY_QUANTITY, true));
		columns.add(geneColumn("交易净值", Transaction.PROPERTY_PRICE, true));
		columns.add(geneColumn("净收益", Transaction.PROPERTY_PROFIT, true));
		columns.add(geneColumn("收益率", Transaction.PROPERTY_PROFIT_RATE, true));
		getColumns().addAll(columns.stream().toArray(TableColumn[]::new));
		this.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

		// 添加内容列表
		ObservableList<Transaction> itemList = FXCollections.observableArrayList();
		setItems(itemList);

		// 业务逻辑：根据属性获取交易记录并填入内容列表
		fundId.addListener((p, oldValue, newValue) -> refreshData());
		fromDate.addListener((p, oldValue, newValue) -> refreshData());
		toDate.addListener((p, oldValue, newValue) -> refreshData());
		refreshData();
	}

	private TableColumn<Transaction, Label> geneColumn(String showName, String property, boolean rightAlignment) {
		TableColumn<Transaction, Label> column = new TableColumn<>(showName);
		column.setCellValueFactory(PropertyLabelFactory.forTable(property));
		if (rightAlignment) {
			column.setStyle("-fx-alignment: CENTER-RIGHT;");
		}
		return column;
	}

	private Task<List<Transaction>> queryTask = null;

	private synchronized void refreshData() {
		if (queryTask != null) {
			queryTask.cancel();
		}

		getItems().clear();

		LocalDate fromDateValue = fromDate.get();
		LocalDate toDateValue = toDate.get();
		if (fundId.get() == null || fromDateValue == null || toDateValue == null) {
			return;
		}

		waiting.set(true);

		queryTask = new Task<List<Transaction>>() {
			@Override
			protected List<Transaction> call() throws Exception {
				return biz.getTransactions(fundId.get(), fromDateValue, toDateValue).stream()
						.sorted(Comparator.comparing(Transaction::getDate).reversed()).collect(Collectors.toList());
			}

			@Override
			protected void succeeded() {
				synchronized (TransactionTable.this) {
					try {
						getItems().setAll(get());
						waiting.set(false);
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
