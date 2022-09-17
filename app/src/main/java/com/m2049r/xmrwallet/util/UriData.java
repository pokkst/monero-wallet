package com.m2049r.xmrwallet.util;

import com.m2049r.xmrwallet.model.Wallet;

import java.util.HashMap;

public class UriData {
    private final String address;
    private final HashMap<String, String> params;

    public UriData(String address, HashMap<String, String> params) {
        this.address = address;
        this.params = params;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public String getAddress() {
        return address;
    }

    public String getAmount() {
        return params.get(Constants.URI_ARG_AMOUNT);
    }

    public boolean hasAmount() {
        return params.containsKey(Constants.URI_ARG_AMOUNT);
    }

    public static UriData parse(String uri) {
        HashMap<String, String> params = new HashMap<>();
        String[] uriParts = uri.replace(Constants.URI_PREFIX, "").split("\\?");
        String finalAddress = uriParts[0];
        String queryParams = "";
        if(uriParts.length > 1) {
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
        if(valid) {
            return new UriData(finalAddress, params);
        } else {
            return null;
        }
    }
}
