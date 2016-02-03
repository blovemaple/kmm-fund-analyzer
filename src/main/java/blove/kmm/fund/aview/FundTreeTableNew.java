package blove.kmm.fund.aview;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import blove.kmm.fund.aview.FundTreeTableNew.FundItem;
import blove.kmm.fund.aview.util.Percentagized;
import blove.kmm.fund.aview.util.Precised;
import blove.kmm.fund.aview.util.PropertyLabelFactory;
import blove.kmm.fund.biz.FundBiz;
import blove.kmm.fund.biz.bo.FundInfo;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

public class FundTreeTableNew extends TreeTableView<FundItem> {
	private StringProperty selectedFundId = new SimpleStringProperty();

	private BooleanProperty waiting = new SimpleBooleanProperty(false);

	@SuppressWarnings("unchecked")
	public FundTreeTableNew(FundBiz biz) {
		// 添加列
		List<TreeTableColumn<FundItem, Label>> columns = new ArrayList<>();
		columns.add(geneColumn("", FundItem.PROPERTY_FUND_NAME, false, 180));
		columns.add(geneColumn("持仓市值", FundItem.PROPERTY_MY_AMOUNT, true, 0));
		columns.add(geneColumn("浮动收益", FundItem.PROPERTY_CRT_PROFIT, true, 0));
		columns.add(geneColumn("浮动收益率", FundItem.PROPERTY_CRT_PROFIT_RATE, true, 0));
		getColumns().addAll(columns.stream().toArray(TreeTableColumn[]::new));
		this.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

		// 添加内容
		TreeItem<FundItem> root = new TreeItemWithFundId<>();
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
						TreeItem<FundItem> fundCatItem = new TreeItem<>();
						FundItem fundCatRow = FundItem.fromFundInfoList(category, fundCatItem.getChildren());
						fundCatItem.setValue(fundCatRow);
						fundCatItem.setExpanded(true);
						root.getChildren().add(fundCatItem);

						funds.stream()
								.sorted(Comparator.comparing(fundInfo -> !Double.isFinite(fundInfo.getCrtProfitRate())))
								.forEach(fundInfo -> {
							// 基金行
							FundItem fundRow = FundItem.fromFundInfo(fundInfo);
							TreeItem<FundItem> fundItem = new TreeItemWithFundId<>(fundRow, fundInfo.getFundId());
							fundCatItem.getChildren().add(fundItem);
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

	private TreeTableColumn<FundItem, Label> geneColumn(String showName, String property, boolean rightAlignment,
			double minWidth) {
		TreeTableColumn<FundItem, Label> column = new TreeTableColumn<>(showName);
		column.setCellValueFactory(PropertyLabelFactory.forTreeTable(property));
		if (rightAlignment) {
			column.setStyle("-fx-alignment: CENTER-RIGHT;");
		}
		if (minWidth > 0) {
			column.setMinWidth(minWidth);
			column.setPrefWidth(minWidth + 20);// prefWidth比minWitch大一些，以便出现滚动条时可以缩小列宽
		}
		return column;
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

	public static class FundItem {
		private static FundItem fromFundInfo(FundInfo info) {
			FundItem i = new FundItem();
			i.fundNameProperty().bind(info.fundNameProperty());
			i.myAmountProperty().bind(info.myAmountProperty());
			i.crtProfitProperty().bind(info.crtProfitProperty());
			i.crtProfitRateProperty().bind(info.crtProfitRateProperty());
			return i;
		}

		private static FundItem fromFundInfoList(String name, ObservableList<TreeItem<FundItem>> infos) {
			FundItem i = new FundItem();
			i.setFundName(name);
			infos.addListener((Observable list) -> {
				List<DoubleProperty> myAmounts = infos.stream().map(TreeItem::getValue).map(FundItem::myAmountProperty)
						.collect(Collectors.toList());
				i.myAmountProperty()
						.bind(Bindings.createDoubleBinding(
								() -> myAmounts.stream().collect(Collectors.summingDouble(DoubleProperty::get)),
								myAmounts.stream().toArray(Observable[]::new)));

				List<DoubleProperty> crtProfits = infos.stream().map(TreeItem::getValue)
						.map(FundItem::crtProfitProperty).collect(Collectors.toList());
				i.crtProfitProperty()
						.bind(Bindings.createDoubleBinding(
								() -> crtProfits.stream().collect(Collectors.summingDouble(DoubleProperty::get)),
								crtProfits.stream().toArray(Observable[]::new)));

				i.crtProfitRateProperty()
						.bind(i.crtProfitProperty().divide(i.myAmountProperty().subtract(i.crtProfitProperty())));
			});
			return i;
		}

		private StringProperty fundName = new SimpleStringProperty();
		public static final String PROPERTY_FUND_NAME = "fundName";
		@Precised(2)
		private DoubleProperty myAmount = new SimpleDoubleProperty();
		public static final String PROPERTY_MY_AMOUNT = "myAmount";
		@Precised(2)
		private DoubleProperty crtProfit = new SimpleDoubleProperty();
		public static final String PROPERTY_CRT_PROFIT = "crtProfit";
		@Percentagized(2)
		private DoubleProperty crtProfitRate = new SimpleDoubleProperty();
		public static final String PROPERTY_CRT_PROFIT_RATE = "crtProfitRate";

		public final StringProperty fundNameProperty() {
			return this.fundName;
		}

		public final java.lang.String getFundName() {
			return this.fundNameProperty().get();
		}

		public final void setFundName(final java.lang.String fundName) {
			this.fundNameProperty().set(fundName);
		}

		public final DoubleProperty myAmountProperty() {
			return this.myAmount;
		}

		public final double getMyAmount() {
			return this.myAmountProperty().get();
		}

		public final void setMyAmount(final double myAmount) {
			this.myAmountProperty().set(myAmount);
		}

		public final DoubleProperty crtProfitProperty() {
			return this.crtProfit;
		}

		public final double getCrtProfit() {
			return this.crtProfitProperty().get();
		}

		public final void setCrtProfit(final double crtProfit) {
			this.crtProfitProperty().set(crtProfit);
		}

		public final DoubleProperty crtProfitRateProperty() {
			return this.crtProfitRate;
		}

		public final double getCrtProfitRate() {
			return this.crtProfitRateProperty().get();
		}

		public final void setCrtProfitRate(final double crtProfitRate) {
			this.crtProfitRateProperty().set(crtProfitRate);
		}

	}

}
