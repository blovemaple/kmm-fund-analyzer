package blove.kmm.fund.support.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AccountDao {
	private String dbFilePath;
	private Connection conn;

	public AccountDao(String dbFilePath) {
		this.dbFilePath = dbFilePath;
	}

	private void query(String sql, Consumer<ResultSet> resultHandler) throws SQLException {
		System.out.println("SQL Query: " + sql);
		if (conn == null || !conn.isValid(0)) {
			if (conn != null)
				conn.close();
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
		}
		Statement stat = conn.createStatement();
		ResultSet resultSet = stat.executeQuery(sql);
		while (resultSet.next()) {
			resultHandler.accept(resultSet);
		}
		resultSet.close();
		stat.close();
	}

	public List<String> getAllFundId() {
		try {
			List<String> fundIds = new ArrayList<>();
			query("select id from kmmAccounts where accountType=15", resultSet -> {
				try {
					fundIds.add(resultSet.getString("id"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			return fundIds;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getFundName(String fundId) {
		try {
			List<String> accountNames = new ArrayList<>();
			query("select accountName from kmmAccounts where id='" + fundId + "'", resultSet -> {
				try {
					accountNames.add(resultSet.getString("accountName"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			if (accountNames.isEmpty()) {
				return null;
			}
			return accountNames.get(0);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getFundCategory(String fundId) {
		try {
			List<String> parentAccountNames = new ArrayList<>();
			query("select accountName from kmmAccounts where id=(select parentId from kmmAccounts where id='" + fundId
					+ "')", resultSet -> {
						try {
							parentAccountNames.add(resultSet.getString("accountName"));
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
			if (parentAccountNames.isEmpty()) {
				return null;
			}
			String parentAccountName = parentAccountNames.get(0);

			String[] split = parentAccountName.split("：", 2);
			if (split.length >= 2) {
				return split[1];
			} else {
				return parentAccountName;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Optional<String> getFundCode(String fundId) {
		try {
			List<String> currencyIds = new ArrayList<>();
			query("select currencyId from kmmAccounts where id='" + fundId + "'", resultSet -> {
				try {
					currencyIds.add(resultSet.getString("currencyId"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			if (currencyIds.isEmpty()) {
				return Optional.empty();
			}
			List<String> fundCodes = new ArrayList<>();
			query("select symbol from kmmSecurities where id='" + currencyIds.get(0) + "'", resultSet -> {
				try {
					fundCodes.add(resultSet.getString("symbol"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			if (fundCodes.isEmpty()) {
				return Optional.empty();
			}
			return Optional.of(fundCodes.get(0));
		} catch (SQLException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	public List<DBTransaction> getTransactions(String fundId, LocalDate fromDate, LocalDate toDate) {
		try {
			List<DBTransaction> res = new ArrayList<>();
			StringBuilder sql = new StringBuilder(
					"select transactionId,postDate,action,valueFormatted,sharesFormatted,priceFormatted from kmmSplits where accountId='"
							+ fundId + "'");
			if (fromDate != null) {
				sql.append(" and postDate>='").append(fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("'");
			}
			if (toDate != null) {
				sql.append(" and postDate<='").append(toDate.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("'");
			}
			Map<String, DBTransaction> dividens = new HashMap<>();
			Map<String, DBTransaction> buySells = new HashMap<>();
			query(sql.toString(), resultSet -> {
				try {
					DBTransaction transaction = new DBTransaction();
					transaction.setDate(LocalDate.parse(resultSet.getString("postDate")));
					transaction.setType(parseTransType(resultSet.getString("action"),
							Double.parseDouble(resultSet.getString("valueFormatted"))));
					transaction.setAmount(Math.abs(Double.parseDouble(resultSet.getString("valueFormatted"))));
					transaction.setQuantity(Math.abs(Double.parseDouble(resultSet.getString("sharesFormatted"))));
					String priceFormatted = resultSet.getString("priceFormatted");
					transaction.setPrice(priceFormatted == null ? 0 : Double.parseDouble(priceFormatted));
					transaction.setFee(0);// 默认手续费为0，后面再查
					res.add(transaction);
					switch (transaction.getType()) {
					case BUY:
					case SELL:
					case REINVEST:
						// 买卖、红利再投需要查询手续费
						buySells.put(resultSet.getString("transactionId"), transaction);
						break;
					case DIVIDEN:
						// 分红需要查询分红金额
						dividens.put(resultSet.getString("transactionId"), transaction);
						break;
					default:
						// 其余类型什么都不用查
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			// 查询手续费
			if (!buySells.isEmpty()) {
				StringBuilder feeSql = new StringBuilder(
						"select s.transactionId,s.valueFormatted from kmmSplits s left join kmmAccounts a on s.accountId=a.id"
								+ " where a.accountType=13 and s.transactionId in (");
				feeSql.append(String.join(",",
						buySells.keySet().stream().map(tranId -> "'" + tranId + "'").collect(Collectors.toList())));
				feeSql.append(")");
				query(feeSql.toString(), resultSet -> {
					try {
						DBTransaction tran = buySells.get(resultSet.getString("transactionId"));
						if (tran == null) {
							System.err.println("Unexpected: no transaction for fee, tranId="
									+ resultSet.getString("transactionId"));
							return;
						}
						tran.setFee(Double.parseDouble(resultSet.getString("valueFormatted")));
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}

			// 查询分红金额
			if (!dividens.isEmpty()) {
				StringBuilder dividenSql = new StringBuilder(
						"select transactionId,valueFormatted from kmmSplits where valueFormatted>0 and transactionId in (");
				dividenSql.append(String.join(",",
						dividens.keySet().stream().map(tranId -> "'" + tranId + "'").collect(Collectors.toList())));
				dividenSql.append(")");
				query(dividenSql.toString(), resultSet -> {
					try {
						DBTransaction tran = dividens.get(resultSet.getString("transactionId"));
						if (tran == null) {
							System.err.println("Unexpected: no transaction for dividen, tranId="
									+ resultSet.getString("transactionId"));
							return;
						}
						tran.setAmount(Double.parseDouble(resultSet.getString("valueFormatted")));
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}

			return res;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private TransactionType parseTransType(String dbType, double value) {
		switch (dbType) {
		case "Buy":
			if (value >= 0) {
				return TransactionType.BUY;
			} else {
				return TransactionType.SELL;
			}
		case "Dividend":
			return TransactionType.DIVIDEN;
		case "Reinvest":
			return TransactionType.REINVEST;
		case "Add":
			return TransactionType.ADD;
		default:
			return null;
		}
	}
}
