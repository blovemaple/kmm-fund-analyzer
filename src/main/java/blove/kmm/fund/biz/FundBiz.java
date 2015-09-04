package blove.kmm.fund.biz;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import blove.kmm.fund.biz.bo.DatePrice;
import blove.kmm.fund.biz.bo.DateRange;
import blove.kmm.fund.biz.bo.FundInfo;
import blove.kmm.fund.biz.bo.Transaction;
import blove.kmm.fund.support.db.AccountDao;
import blove.kmm.fund.support.db.DBTransaction;
import blove.kmm.fund.support.db.TransactionType;
import blove.kmm.fund.support.price.PriceSupport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 业务逻辑。
 * 
 * @author blove
 */
public class FundBiz {
	private PriceSupport priceSupport;
	private AccountDao dao;

	public FundBiz(PriceSupport priceSupport, AccountDao dao) {
		this.priceSupport = priceSupport;
		this.dao = dao;
	}

	/**
	 * 获取所有基金信息。
	 * 
	 * @return 所有基金信息
	 */
	public List<FundInfo> getAllFundInfo() {
		Consumer<FundInfo> newPriceSetter = fundInfo -> {
			Map.Entry<LocalDate, Double> newPrice = priceSupport
					.getNewPrice(dao.getFundCode(fundInfo.getFundId()).orElse("")).entrySet().iterator().next();
			fundInfo.setCrtPrice(newPrice.getValue());
			fundInfo.setCrtPriceDate(newPrice.getKey());
		};
		return dao.getAllFundId().parallelStream().map(fundId -> analyze(fundId, null, LocalDate.now()).fundInfo)
				.peek(newPriceSetter).collect(Collectors.toList());
	}

	/**
	 * 获取自动设定的日期范围。如有交易，则返回第一次交易以前三天至今天。如无交易则返回过去三个月。
	 * 
	 * @param fundId
	 *            基金ID
	 * @return 日期范围
	 */
	public DateRange getAutoDateRange(String fundId) {
		LocalDate startDate, endDate;
		List<DBTransaction> dbTrans = dao.getTransactions(fundId, null, null);
		if (dbTrans.isEmpty()) {
			// 如无交易，返回过去三个月
			startDate = LocalDate.now().minusMonths(3);
			endDate = LocalDate.now();
		} else {
			dbTrans.sort(Comparator.comparing(DBTransaction::getDate));
			startDate = dbTrans.get(0).getDate().minusDays(5);
			endDate = LocalDate.now();
		}
		return new DateRange(startDate, endDate);
	}

	/**
	 * 获取交易记录。
	 * 
	 * @param fundId
	 *            基金ID
	 * @param fromDate
	 *            起始日期（包含）
	 * @param toDate
	 *            结束日期（包含）
	 * @return 交易记录
	 */
	public List<Transaction> getTransactions(String fundId, LocalDate fromDate, LocalDate toDate) {
		return analyze(fundId, fromDate, toDate).transactions;
	}

	/**
	 * 获取每日净值和持有均价。
	 * 
	 * @param fundId
	 *            基金ID
	 * @param fromDate
	 *            起始日期（包含）
	 * @param toDate
	 *            结束日期（包含）
	 * @return 每日净值和持有均价
	 */
	public ObservableList<DatePrice> getDatePrice(String fundId, LocalDate fromDate, LocalDate toDate) {
		// 生成新的列表
		ObservableList<DatePrice> newList = FXCollections.observableArrayList();

		// 获取每日净值及计算每日均价并填入
		List<DatePrice> newValues = new ArrayList<>();
		getFundCode(fundId).ifPresent(fundCode -> {
			NavigableMap<LocalDate, Double> prices = priceSupport.getPrice(fundCode, fromDate, toDate);
			NavigableMap<LocalDate, Double> avgPrices = analyze(fundId, fromDate, toDate).avgPrices;

			prices.forEach((date, price) -> {
				DatePrice datePrice = new DatePrice(date);
				datePrice.setPrice(price);
				Optional.ofNullable(prices.lowerEntry(date)).ifPresent(prePriceEntry -> {
					double prePrice = prePriceEntry.getValue();
					datePrice.setIncreaseRate((price - prePrice) / prePrice);
				});
				Entry<LocalDate, Double> avgPriceEntry = avgPrices.floorEntry(date);
				datePrice.setAvgPrice(avgPriceEntry == null ? 0 : avgPriceEntry.getValue());
				newValues.add(datePrice);
			});
		});
		newList.setAll(newValues);

		return newList;
	}

	/**
	 * 获取基金代码。
	 * 
	 * @param fundId
	 *            基金ID
	 * @return 基金代码
	 */
	public Optional<String> getFundCode(String fundId) {
		return dao.getFundCode(fundId);
	}

	private static class FundAnaInfo {
		FundInfo fundInfo = new FundInfo();
		List<Transaction> transactions = new ArrayList<>();
		NavigableMap<LocalDate, Double> avgPrices = new TreeMap<>();
	}

	private FundAnaInfo analyze(String fundId, LocalDate fromDate, LocalDate toDate) {
		FundAnaInfo res = new FundAnaInfo();
		res.fundInfo.setFundId(fundId);
		res.fundInfo.setFundName(dao.getFundName(fundId));
		res.fundInfo.setCategoryName(dao.getFundCategory(fundId));

		List<DBTransaction> dbTrans = dao.getTransactions(fundId, null, toDate);

		dbTrans.stream().sorted(Comparator.comparing(DBTransaction::getDate)).forEachOrdered(dbTran -> {
			// fundInfo
			double oriAvgPrice = res.fundInfo.getMyAvgPrice();
			double profit = 0;
			if (dbTran.getType() == null) {
				System.out.println(fundId + "\t" + dbTran);
			}
			switch (dbTran.getType()) {
			case BUY:
			case REINVEST:
				res.fundInfo.setMyOriCost(res.fundInfo.getMyOriCost() + dbTran.getAmount() + dbTran.getFee());
				res.fundInfo.setMyQuantity(res.fundInfo.getMyQuantity() + dbTran.getQuantity());
				res.fundInfo.setTotalCost(res.fundInfo.getTotalCost() + dbTran.getAmount() + dbTran.getFee());
				break;
			case SELL:
				res.fundInfo.setMyOriCost(
						res.fundInfo.getMyOriCost() * (1 - dbTran.getQuantity() / res.fundInfo.getMyQuantity()));
				res.fundInfo.setMyQuantity(res.fundInfo.getMyQuantity() - dbTran.getQuantity());
				res.fundInfo.setTotalSellGet(res.fundInfo.getTotalSellGet() + dbTran.getAmount() - dbTran.getFee());
				profit = dbTran.getAmount() - dbTran.getFee() - oriAvgPrice * dbTran.getQuantity();
				res.fundInfo.setTotalProfit(res.fundInfo.getTotalProfit() + profit);
				break;
			case DIVIDEN:
				res.fundInfo.setTotalDividen(res.fundInfo.getTotalDividen() + dbTran.getAmount());
				break;
			case ADD:
				res.fundInfo.setMyQuantity(res.fundInfo.getMyQuantity() + dbTran.getQuantity());
				break;
			default:
				System.err.println("Unrecognized trans type:" + dbTran.getType());
				break;
			}

			// transactions
			if ((fromDate == null || !dbTran.getDate().isBefore(fromDate))
					&& (toDate == null || !dbTran.getDate().isAfter(toDate))) {
				Transaction tran = new Transaction();
				switch (dbTran.getType()) {
				case SELL:
					tran.setProfit(profit);
					tran.setProfitRate(profit / (oriAvgPrice * dbTran.getQuantity()));
				case BUY:
				case REINVEST:
				case ADD:
					tran.setQuantity(dbTran.getQuantity());
					tran.setPrice(dbTran.getPrice());
				default:
					tran.setDate(dbTran.getDate());
					tran.setType(dbTran.getType());
					tran.setAmount(dbTran.getAmount());
					tran.setFee(dbTran.getFee());
				}
				if (dbTran.getType() == TransactionType.DIVIDEN) {
					tran.setProfit(dbTran.getAmount());
					tran.setProfitRate(dbTran.getAmount() / res.fundInfo.getMyOriCost());
				}
				res.transactions.add(tran);
			}

			// avgPrices
			double avgPrice = res.fundInfo.getMyQuantity() == 0 ? 0 : res.fundInfo.getMyAvgPrice();
			res.avgPrices.put((fromDate != null && dbTran.getDate().isBefore(fromDate)) ? fromDate : dbTran.getDate(),
					avgPrice);
		});

		return res;

	}
}
