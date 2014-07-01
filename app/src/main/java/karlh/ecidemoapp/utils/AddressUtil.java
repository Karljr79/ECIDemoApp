/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package karlh.ecidemoapp.utils;

import com.paypal.merchant.sdk.domain.Address;
import com.paypal.merchant.sdk.domain.DomainFactory;

public class AddressUtil {

    public static Address getDefaultUSMerchantAddress() {
        Address.Builder builder = DomainFactory.newAddressBuilder();
        builder.
                setLine1("1569 Oxford St").
                setLine2("").
                setCity("Redwood City").
                setState("CA").
                setCountryCode("US").
                setPostalCode("94061").
                setPhoneNumber("8082038363");
        return builder.build();
    }

    public static Address getDefaultUKMerchantAddress() {
        Address.Builder builder = DomainFactory.newAddressBuilder();
        builder.
                setLine1("34726 South Broadway").
                setCity("Wolverhampton").
                setState("West Midlands").
                setCountryCode("GB").
                setPostalCode("W12 4LQ").
                setPhoneNumber("05725854438");
        return builder.build();
    }
}
