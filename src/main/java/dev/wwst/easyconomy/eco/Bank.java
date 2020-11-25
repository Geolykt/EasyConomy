package dev.wwst.easyconomy.eco;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

public interface Bank {

    public default void addMoney(double amount) {
        // BigDecimal for less approximations when dealing with doubles (due to how floating point values are handled in
        // Java, there will always be approximations) ( https://docs.oracle.com/cd/E19957-01/806-3568/ncg_goldberg.html )
        this.setMoney(BigDecimal.valueOf(this.getMoney()).add(BigDecimal.valueOf(amount)).doubleValue());
    }

    public default void removeMoney(double amount) {
        // BigDecimal for less approximations when dealing with doubles (due to how floating point values are handled in
        // Java, there will always be approximations) ( https://docs.oracle.com/cd/E19957-01/806-3568/ncg_goldberg.html )
        this.setMoney(BigDecimal.valueOf(this.getMoney()).subtract(BigDecimal.valueOf(amount)).doubleValue());
    }

    public void setMoney(double amount);
    public double getMoney();

    public boolean isMember(@NotNull UUID player);
    public boolean isMember(@NotNull String player);

    public @NotNull String getName();

    public void serialize(@NotNull OutputStream outStream) throws IOException;
}
