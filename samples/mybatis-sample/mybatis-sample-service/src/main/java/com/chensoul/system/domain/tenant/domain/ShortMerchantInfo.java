package com.chensoul.system.domain.tenant.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by igor on 2/27/18.
 */

@AllArgsConstructor
public class ShortMerchantInfo {

    @Getter
    @Setter
    private Long merchantId;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private boolean isPublic;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShortMerchantInfo that = (ShortMerchantInfo) o;

        return merchantId.equals(that.merchantId);

    }

    @Override
    public int hashCode() {
        return merchantId.hashCode();
    }
}
