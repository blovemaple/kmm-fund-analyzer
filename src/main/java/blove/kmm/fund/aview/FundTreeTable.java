package blove.kmm.fund.aview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import blove.kmm.fund.aview.FundTreeTable.NameValue;
import blove.kmm.fund.aview.util.PropertyLabelFactory;
import blove.kmm.fund.biz.FundBiz;
import blove.kmm.fund.biz.bo.FundInfo;
import blove.kmm.fund.biz.bo.ShowName;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

public class FundTreeTable extends TreeTableView<NameValue> {
	private static final String MAP_KEY_NAME = "name";
	private static final String MAP_KEY_VALUE = "value";

	private StringProperty selectedFundId = new SimpleStringProperty();

	private BooleanProperty waiting = new SimpleBooleanProperty(false);

	@SuppressWarnings("unchecked")
	public FundTreeTable(FundBiz biz) {
		// 添加列
		List<TreeTableColumn<NameValue, String>> columns = new ArrayList<>();
		columns.add(geneColumn(MAP_KEY_NAME, 240, false));
		columns.add(geneColumn(MAP_KEY_VALUE, 80, true));
		getColumns().addAll(columns.stream().toArray(TreeTableColumn[]::new));
		this.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

		// 添加内容
		TreeItem<NameValue> root = new TreeItemWithFundId<>();
		setRoot(root);
		setShowRoot(false);

		waiting.set(true);

		Task<List<FundInfo>> initQueryTask = new Task<List<FundInfo>>() {
			@Override
			protected List<FundInfo> call() throws Exception {
				return biz.getAllFundInfo();
			}

			@Override
			protected void succeeded() {
				try {
					get().stream().collect(Collectors.groupingBy(FundInfo::getCategoryName))
							.forEach((category, funds) -> {
						// 基金类别行
						NameValue fundCatRow = new NameValue(category, "");
						TreeItem<NameValue> fundCatItem = new TreeItem<>(fundCatRow);
						fundCatItem.setExpanded(true);
						root.getChildren().add(fundCatItem);

						funds.stream()
								.sorted(Comparator.comparing(fundInfo -> !Double.isFinite(fundInfo.getCrtProfitRate())))
								.forEach(fundInfo -> {
							// 添加基金名称行
							NameValue fundTitleRow = new NameValue(
									fundInfo.getFundName() + " ("
											+ biz.getFundCode(fundInfo.getFundId()).orElse("No Code") + ")",
									new PropertyLabelFactory<>(FundInfo.PROPERTY_CRT_PROFIT_RATE).call(fundInfo));
							TreeItem<NameValue> fundTitleItem = new TreeItemWithFundId<>(fundTitleRow,
									fundInfo.getFundId());
							fundCatItem.getChildren().add(fundTitleItem);

							// 添加各字段行
							List<TreeItem<NameValue>> fieldItems = toNameValueList(fundInfo).stream()
									.map(map -> new TreeItemWithFundId<>(map, fundInfo.getFundId()))
									.collect(Collectors.toList());
							fundTitleItem.getChildren().setAll(fieldItems);
						});
					});
					waiting.set(false);
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void failed() {
				getException().printStackTrace();
				waiting.set(false);
			}
		};
		new Thread(initQueryTask).start();

		// 业务逻辑：根据选择的行更新selectedFundId
		getSelectionModel().selectedItemProperty().addListener((property, oldValue, newValue) -> {
			if (newValue instanceof TreeItemWithFundId) {
				String fundId = ((TreeItemWithFundId<?>) newValue).fundId;
				if (fundId != null) {
					setSelectedFundId(fundId);
				}
			}
		});
	}

	private static class TreeItemWithFundId<T> extends TreeItem<T> {
		String fundId;

		public TreeItemWithFundId(T value, String fundId) {
			super(value);
			this.fundId = fundId;
		}

		public TreeItemWithFundId() {
		}
	}

	private TreeTableColumn<NameValue, String> geneColumn(String property, double minWidth, boolean rightAlignment) {
		TreeTableColumn<NameValue, String> column = new TreeTableColumn<>();
		column.setMinWidth(minWidth);
		column.setPrefWidth(minWidth + 20);// prefWidth比minWitch大一些，以便出现滚动条时可以缩小列宽
		column.setCellValueFactory(new TreeItemPropertyValueFactory<>(property));
		if (rightAlignment) {
			column.setStyle("-fx-alignment: CENTER-RIGHT;");
		}
		return column;
	}

	private List<NameValue> toNameValueList(FundInfo fundInfo) {
		return Arrays.stream(FundInfo.class.getDeclaredFields()).sequential()
				.filter(field -> field.getAnnotation(ShowName.class) != null).map(field -> {
					String name = field.getAnnotation(ShowName.class).value();
					ObservableValue<Label> value = new PropertyLabelFactory<>(field.getName()).call(fundInfo);
					return new NameValue(name, value);
				}).collect(Collectors.toList());
	}

	public final StringProperty selectedFundIdProperty() {
		return this.selectedFundId;
	}

	public final java.lang.String getSelectedFundId() {
		return this.selectedFundIdProperty().get();
	}

	public final void setSelectedFundId(final java.lang.String selectedFundId) {
		this.selectedFundIdProperty().set(selectedFundId);
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

	public static class NameValue {
		private StringProperty name = new SimpleStringProperty();
		private ObjectProperty<Object> value = new SimpleObjectProperty<Object>();

		public NameValue(String name, ObservableValue<?> value) {
			setName(name);
			valueProperty().bind(value);
		}

		public NameValue(String name, Object value) {
			setName(name);
			setValue(value);
		}

		public final StringProperty nameProperty() {
			return this.name;
		}

		public final java.lang.String getName() {
			return this.nameProperty().get();
		}

		public final void setName(final java.lang.String name) {
			this.nameProperty().set(name);
		}

		public final ObjectProperty<Object> valueProperty() {
			return this.value;
		}

		public final Object getValue() {
			return this.valueProperty().get();
		}

		public final void setValue(final Object value) {
			this.valueProperty().set(value);
		}

	}

}
