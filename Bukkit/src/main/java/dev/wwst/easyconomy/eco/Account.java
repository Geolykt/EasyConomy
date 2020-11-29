package dev.wwst.easyconomy.eco;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.geolykt.easyconomy.api.Bank;
import de.geolykt.easyconomy.api.PlaceholderBank;

public class Account implements Bank {

    private final Set<UUID> memberUUIDs;
    private final String name;
    private double bal;

    public Account(@NotNull String bankName, double balance, @Nullable Set<UUID> members) {
        name = bankName;
        if (name.getBytes(StandardCharsets.UTF_8).length > 127) {
            throw new IllegalArgumentException("Bank names may not be longer than 127 bytes.");
        }
        bal = balance;
        memberUUIDs = members == null ? new HashSet<>() : members;
    }

    @Override
    public void setMoney(double amount) {
        bal = amount;
    }

    @Override
    public double getMoney() {
        return bal;
    }

    /**
     * Warning: unofficial API.
     * Warning: removing all players may result in the Bank being converted to a {@link PlaceholderBank}!
     * Removes a given player from the memberlist
     * @param member The member to remove
     * @return True if the player was removed because it was on the list, false otherwise.
     */
    public boolean removeMember(@NotNull UUID member) {
        return memberUUIDs.remove(member);
    }

    /**
     * Warning: unofficial API.
     * Adds a given player to the memberlist
     * @param member The member to add
     * @return True if the player was added because it wasn't already on the list, false otherwise.
     */
    public boolean addMember(@NotNull UUID member) {
        return memberUUIDs.add(member);
    }

    @Override
    public boolean isMember(@NotNull UUID user) {
        return memberUUIDs.contains(user);
    }

    @Override
    public boolean isMember(@NotNull String playerName) {
        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null) {
            return false;
        }
        return memberUUIDs.contains(player.getUniqueId());
    }

    @NotNull
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

    @Override
    public void serialize(@NotNull OutputStream out) throws IOException {
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
}
