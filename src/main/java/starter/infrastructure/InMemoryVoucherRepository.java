package starter.infrastructure;

import starter.dathang.usecase.VoucherRepository;
import starter.domain.Voucher;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter In-Memory cho VoucherRepository.
 */
public final class InMemoryVoucherRepository implements VoucherRepository {
    private final Map<String, Voucher> vouchers = new ConcurrentHashMap<>();

    @Override
    public Optional<Voucher> findByCode(String code) {
        if (code == null) return Optional.empty();
        return Optional.ofNullable(vouchers.get(code.toUpperCase()));
    }

    @Override
    public void save(Voucher voucher) {
        if (voucher != null) {
            vouchers.put(voucher.code().toUpperCase(), voucher);
        }
    }
}
