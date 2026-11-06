package starter.infrastructure;

import starter.entity.Voucher;
import starter.usecase.VoucherRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryVoucherRepository implements VoucherRepository {
    private final Map<String, Voucher> vouchers = new HashMap<>();

    @Override
    public Optional<Voucher> findByCode(String code) {
        if (code == null) return Optional.empty();
        return Optional.ofNullable(vouchers.get(code.toUpperCase()));
    }

    @Override
    public void save(Voucher voucher) {
        vouchers.put(voucher.code().toUpperCase(), voucher);
    }
}
