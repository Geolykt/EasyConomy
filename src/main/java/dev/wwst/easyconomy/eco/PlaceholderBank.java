package dev.wwst.easyconomy.eco;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import de.geolykt.easyconomy.api.Bank;

public class PlaceholderBank implements Bank {

    protected double money;
    protected final String name;

    public PlaceholderBank(@NotNull String bankName, double balance) {
        money = balance;
        name = bankName;
    }

    @Override
    public void setMoney(double amount) {
        money = amount;
    }

    @Override
    public double getMoney() {
        return money;
    }

    @Override
    public boolean isMember(@NotNull UUID player) {
        return false; // Unsupported.
    }

    @Override
    public boolean isMember(@NotNull String player) {
        return false; // Unsupported.
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public void serialize(@NotNull OutputStream out) throws IOException {
        byte[] nameCstr = name.getBytes(StandardCharsets.UTF_8); // The c-string representation of the name
        int headerval = 9 + nameCstr.length;
        ByteBuffer buff = ByteBuffer.allocate(headerval + 4)
                .putInt(headerval)
                .put((byte) nameCstr.length)
                .put(nameCstr)
                .putDouble(money);
        out.write(buff.array());
    }

}
