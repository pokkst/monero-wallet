package net.mynero.wallet.util;

import net.mynero.wallet.model.Wallet;

import java.util.HashMap;

public class UriData {
    private final String address;
    private final HashMap<String, String> params;

    public UriData(String address, HashMap<String, String> params) {
        this.address = address;
        this.params = params;
    }

    public static UriData parse(String uri) {
        HashMap<String, String> params = new HashMap<>();
        String[] uriParts = uri.replace(Constants.URI_PREFIX, "").split("\\?");
        String finalAddress = uriParts[0];
        String queryParams = "";
        if (uriParts.length > 1) {
            queryParams = uriParts[1];
            String[] queryParts = queryParams.split("&");
            for (String param : queryParts) {
                String[] paramParts = param.split("=");
                String variable = paramParts[0];
                String value = paramParts[1];
                params.put(variable, value);
            }
        }
        boolean valid = Wallet.isAddressValid(finalAddress);
        if (valid) {
            return new UriData(finalAddress, params);
        } else {
            return null;
        }
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public String getAddress() {
        return address;
    }

    public String getAmount() {
        String txAmount = params.get(Constants.URI_ARG_AMOUNT);
        if (txAmount == null) {
            return params.get(Constants.URI_ARG_AMOUNT2);
        }
        return txAmount;
    }

    public boolean hasAmount() {
        return params.containsKey(Constants.URI_ARG_AMOUNT) || params.containsKey(Constants.URI_ARG_AMOUNT2);
    }
}
