package dev.wwst.easyconomy.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Account {

    private final Set<UUID> memberUUIDs;
    private final String name;
    private double bal;

    public Account(String bankName, double balance, Set<UUID> members) {
        name = bankName;
        if (name.getBytes(StandardCharsets.UTF_8).length > 127) {
            throw new IllegalArgumentException("Bank names may not be longer than 127 bytes.");
        }
        bal = balance;
        memberUUIDs = members;
    }

    public void addMoney(double amount) {
        bal += amount;
    }

    public void removeMoney(double amount) {
        bal -= amount;
    }

    public void setMoney(double amount) {
        bal = amount;
    }

    public double getMoney() {
        return bal;
    }

    public boolean removeMember(UUID member) {
        return memberUUIDs.remove(member);
    }

    public boolean addMember(UUID member) {
        return memberUUIDs.add(member);
    }

    public boolean isMember(UUID user) {
        return memberUUIDs.contains(user);
    }

    public String getName() {
        return name;
    }

    // Serialisation protocol:
    //
    // ==============================================================================
    // NAMES : [HEADER]      [STRLEN]    [NAME]         [MONEY]   [MEMBERS]
    // LENGTH:    4 bytes      1 byte     STRLEN bytes    8 bytes    HEADER-STRLEN-9 bytes
    // LENGTH:             |<---------------------HEADER bytes--------------------->|
    // ==============================================================================
    //
    // Each member is encoded as 2 longs, so 16 bytes total. First long is the MostSignificantBits
    // the second the LeastSignificant
    // NAME is encoded in UTF-8 and STRLEN is the length of the byte array of NAME, as such
    // NAME may never be longer than 127 bytes.

    public void serialize(OutputStream out) throws IOException {
        byte[] nameCstr = name.getBytes(StandardCharsets.UTF_8); // The c-string representation of the name
        int headerval = memberUUIDs.size()*16 + 9 + nameCstr.length;
        ByteBuffer buff = ByteBuffer.allocate(headerval + 4)
                .putInt(headerval)
                .put((byte) nameCstr.length)
                .put(nameCstr)
                .putDouble(bal);
        for (UUID id : memberUUIDs) {
            buff.putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits());
        }
        out.write(buff.array());
    }

    public static Account deserialize(InputStream input) throws IOException {
        byte[] data = new byte[4];
        if (input.read(data) == -1) {
            return null;
        }
        data = new byte[ByteBuffer.wrap(data).getInt()];
        input.read(data);
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte[] cstrName = new byte[buffer.get()];
        buffer.get(cstrName);
        String name = new String(cstrName, StandardCharsets.UTF_8);

        double money = buffer.getDouble();

        HashSet<UUID> members = new HashSet<>();
        while (buffer.hasRemaining()) {
            members.add(new UUID(buffer.getLong(), buffer.getLong()));
        }
        return new Account(name, money, members);
    }
}
