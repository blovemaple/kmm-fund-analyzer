package blove.kmm.fund.support.db;

import java.time.LocalDate;

public class DBTransaction {
	private LocalDate date;
	private TransactionType type;
	private double amount;
	private double quantity;
	private double price;
	private double fee;

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public TransactionType getType() {
		return type;
	}

	public void setType(TransactionType type) {
		this.type = type;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getFee() {
		return fee;
	}

	public void setFee(double fee) {
		this.fee = fee;
	}

	@Override
	public String toString() {
		return "DBTransaction [\n\tdate=" + date + "\n\ttype=" + type + "\n\tamount=" + amount + "\n\tquantity="
				+ quantity + "\n\tprice=" + price + "\n\tfee=" + fee + "\n]";
	}

}
