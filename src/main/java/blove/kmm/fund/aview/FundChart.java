package blove.kmm.fund.aview;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import blove.kmm.fund.biz.FundBiz;
import blove.kmm.fund.biz.bo.DatePrice;
import blove.kmm.fund.biz.bo.Transaction;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class FundChart extends LineChart<String, Number> {
	private ObjectProperty<ObservableList<DatePrice>> prices = new SimpleObjectProperty<>();
	private ObjectProperty<ObservableList<Transaction>> transactions = new SimpleObjectProperty<>();

	private BooleanProperty yAxisFrom0 = new SimpleBooleanProperty();

	@SuppressWarnings("unchecked")
	public FundChart(FundBiz biz) {
		super(new CategoryAxis(), new NumberAxis());
		setLegendVisible(false);
		setAnimated(false);// animated貌似有bug

		// 每日净值线
		XYChart.Series<String, Number> priceSeries = new XYChart.Series<>("每日净值", FXCollections.observableArrayList());
		setData(FXCollections.observableArrayList(priceSeries));
		// 设置线条颜色（必须在添加进图表之后）
		setLineColor(priceSeries, Color.RED, 1.0);

		// 持有均价线列表
		ObservableList<XYChart.Series<String, Number>> avgPriceSeriesList = FXCollections.observableArrayList();

		// 交易点（“线”）列表
		ObservableList<XYChart.Series<String, Number>> transDotList = FXCollections.observableArrayList();

		// 业务逻辑：根据参数获取数据并替换图表内容
		ListChangeListener<DatePrice> priceListListener = change -> {
			changePriceLines(change.getList(), priceSeries, avgPriceSeriesList);
		};
		prices.addListener((property, oldValue, newValue) -> {
			if (oldValue != null) {
				oldValue.removeListener(priceListListener);
			}
			if (newValue != null) {
				newValue.addListener(priceListListener);
				changePriceLines(newValue, priceSeries, avgPriceSeriesList);
			}
		});

		ListChangeListener<Transaction> transListListener = change -> {
			changeTransDotList(change.getList(), transDotList);
		};
		transactions.addListener((property, oldValue, newValue) -> {
			if (oldValue != null) {
				oldValue.removeListener(transListListener);
			}
			if (newValue != null) {
				newValue.addListener(transListListener);
				changeTransDotList(newValue, transDotList);
			}
		});
		changeTransDotList(transactions.get(), transDotList);

		// 业务逻辑：数据变化或设置改变时应用设置
		ListChangeListener<XYChart.Data<String, Number>> settingApplyListener = change -> autoYAxisBound();
		getData().forEach(series -> series.getData().addListener(settingApplyListener));
		getData().addListener((ListChangeListener<Series<String, Number>>) change -> {
			while (change.next()) {
				change.getRemoved().forEach(series -> series.getData().removeListener(settingApplyListener));
				change.getAddedSubList().forEach(series -> series.getData().addListener(settingApplyListener));
			}
		});

		yAxisFrom0.addListener((p, oldValue, newValue) -> autoYAxisBound(newValue));
		autoYAxisBound();
	}

	private void changePriceLines(List<? extends DatePrice> newList, XYChart.Series<String, Number> priceSeries,
			ObservableList<XYChart.Series<String, Number>> avgPriceSeriesList) {
		if (newList == null) {
			return;
		}

		List<XYChart.Data<String, Number>> priceList = newList.stream().sorted(Comparator.comparing(DatePrice::getDate))
				.map(dataPrice -> {
					XYChart.Data<String, Number> dot = new XYChart.Data<>(String.valueOf(dataPrice.getDate()),
							dataPrice.getPrice());
					dot.setNode(new Circle());
					return dot;
				}).collect(Collectors.toList());
		priceSeries.getData().setAll(priceList);

		getData().removeAll(avgPriceSeriesList);

		List<XYChart.Series<String, Number>> newAvgPriceSeriesList = new ArrayList<>();
		LinkedList<XYChart.Data<String, Number>> crtAvgPriceSeries = new LinkedList<>();
		newList.stream().sorted(Comparator.comparing(DatePrice::getDate)).forEachOrdered(datePrice -> {
			if (datePrice.getAvgPrice() == 0) {
				if (!crtAvgPriceSeries.isEmpty()) {
					XYChart.Data<String, Number> dot = new XYChart.Data<>(String.valueOf(datePrice.getDate()),
							crtAvgPriceSeries.getLast().getYValue());
					dot.setNode(new Circle());
					crtAvgPriceSeries.add(dot);

					XYChart.Series<String, Number> newSeries = new XYChart.Series<>("",
							FXCollections.observableArrayList(crtAvgPriceSeries));
					newAvgPriceSeriesList.add(newSeries);
					crtAvgPriceSeries.clear();
				}
			} else {
				XYChart.Data<String, Number> dot = new XYChart.Data<>(String.valueOf(datePrice.getDate()),
						datePrice.getAvgPrice());
				dot.setNode(new Circle());
				crtAvgPriceSeries.add(dot);
			}
		});
		if (!crtAvgPriceSeries.isEmpty()) {
			XYChart.Series<String, Number> newSeries = new XYChart.Series<>("",
					FXCollections.observableArrayList(crtAvgPriceSeries));
			newAvgPriceSeriesList.add(newSeries);
			crtAvgPriceSeries.clear();
		}
		avgPriceSeriesList.setAll(newAvgPriceSeriesList);
		getData().addAll(avgPriceSeriesList);

		// 设置线条颜色（必须在添加进图表之后）
		avgPriceSeriesList.forEach(series -> setLineColor(series, Color.GREEN, 0.5));
	}

	private void changeTransDotList(List<? extends Transaction> newList,
			ObservableList<XYChart.Series<String, Number>> transDotList) {
		if (newList == null) {
			return;
		}

		getData().removeAll(transDotList);

		List<XYChart.Series<String, Number>> newDots = newList.stream()
				.sorted(Comparator.comparing(Transaction::getDate)).filter(transaction -> transaction.getPrice() > 0)
				.map(transaction -> {
					XYChart.Data<String, Number> dot = new XYChart.Data<>();
					dot.XValueProperty().bind(transaction.dateProperty().asString());
					dot.YValueProperty().bind(transaction.priceProperty());

					Color dotColor;
					switch (transaction.getType()) {
					case BUY:
						dotColor = Color.DEEPSKYBLUE;
						break;
					case SELL:
						dotColor = Color.DARKORANGE;
						break;
					default:
						dotColor = Color.BLACK;
					}
					dot.setNode(new Circle(10, dotColor.deriveColor(0, 1.0, 1.0, 0.5)));

					return new XYChart.Series<>("", FXCollections.singletonObservableList(dot));
				}).collect(Collectors.toList());
		transDotList.setAll(newDots);
		getData().addAll(transDotList);
	}

	private void setLineColor(XYChart.Series<?, ?> series, Color color, double alpha) {
		Node line = series.getNode().lookup(".chart-series-line");
		String rgb = String.format("%d, %d, %d", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
				(int) (color.getBlue() * 255));
		line.setStyle("-fx-stroke: rgba(" + rgb + ", " + alpha + ");");
	}

	private void autoYAxisBound() {
		autoYAxisBound(yAxisFrom0.get());
	}

	private void autoYAxisBound(boolean yAxisFrom0) {
		if (yAxisFrom0) {
			getYAxis().setAutoRanging(true);
		} else {
			getYAxis().setAutoRanging(false);

			DoubleSummaryStatistics yValueStats = getData().stream().flatMap(series -> series.getData().stream())
					.collect(Collectors.summarizingDouble(data -> data.getYValue().doubleValue()));

			double lowerBound, upperBound;
			if (yValueStats.getCount() > 0) {
				double rangeSize = yValueStats.getMax() - yValueStats.getMin();
				if (rangeSize == 0) {
					lowerBound = yValueStats.getMin() - 1;
					upperBound = yValueStats.getMax() + 1;
				} else {
					lowerBound = yValueStats.getMin() - rangeSize / 20;
					lowerBound = Math.floor(lowerBound * 100) / 100;
					upperBound = yValueStats.getMax() + rangeSize / 20;
					upperBound = Math.ceil(upperBound * 100) / 100;
				}
			} else {
				lowerBound = upperBound = 0;
			}

			NumberAxis yAxis = ((NumberAxis) getYAxis());
			yAxis.setLowerBound(lowerBound);
			yAxis.setUpperBound(upperBound);
		}
	}

	public final ObjectProperty<ObservableList<DatePrice>> pricesProperty() {
		return this.prices;
	}

	public final javafx.collections.ObservableList<blove.kmm.fund.biz.bo.DatePrice> getPrices() {
		return this.pricesProperty().get();
	}

	public final void setPrices(final javafx.collections.ObservableList<blove.kmm.fund.biz.bo.DatePrice> prices) {
		this.pricesProperty().set(prices);
	}

	public final ObjectProperty<ObservableList<Transaction>> transactionsProperty() {
		return this.transactions;
	}

	public final javafx.collections.ObservableList<blove.kmm.fund.biz.bo.Transaction> getTransactions() {
		return this.transactionsProperty().get();
	}

	public final void setTransactions(
			final javafx.collections.ObservableList<blove.kmm.fund.biz.bo.Transaction> transactions) {
		this.transactionsProperty().set(transactions);
	}

	public final BooleanProperty yAxisFrom0Property() {
		return this.yAxisFrom0;
	}

	public final boolean isYAxisFrom0() {
		return this.yAxisFrom0Property().get();
	}

	public final void setYAxisFrom0(final boolean yAxisFrom0) {
		this.yAxisFrom0Property().set(yAxisFrom0);
	}

}
