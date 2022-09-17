/*
 * Copyright (c) 2017 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.m2049r.xmrwallet.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.m2049r.xmrwallet.model.PendingTransaction;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.util.Helper;

// https://stackoverflow.com/questions/2139134/how-to-send-an-object-from-one-android-activity-to-another-using-intents
public class TxData implements Parcelable {

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<TxData> CREATOR = new Parcelable.Creator<TxData>() {
        public TxData createFromParcel(Parcel in) {
            return new TxData(in);
        }

        public TxData[] newArray(int size) {
            return new TxData[size];
        }
    };
    private String dstAddr;
    private long amount;
    private int mixin;
    private PendingTransaction.Priority priority;
    private UserNotes userNotes;

    public TxData() {
    }

    public TxData(TxData txData) {
        this.dstAddr = txData.dstAddr;
        this.amount = txData.amount;
        this.mixin = txData.mixin;
        this.priority = txData.priority;
    }

    public TxData(String dstAddr,
                  long amount,
                  int mixin,
                  PendingTransaction.Priority priority) {
        this.dstAddr = dstAddr;
        this.amount = amount;
        this.mixin = mixin;
        this.priority = priority;
    }

    protected TxData(Parcel in) {
        dstAddr = in.readString();
        amount = in.readLong();
        mixin = in.readInt();
        priority = PendingTransaction.Priority.fromInteger(in.readInt());

    }

    public String getDestinationAddress() {
        return dstAddr;
    }

    public void setDestinationAddress(String dstAddr) {
        this.dstAddr = dstAddr;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setAmount(double amount) {
        this.amount = Wallet.getAmountFromDouble(amount);
    }

    public double getAmountAsDouble() {
        return 1.0 * amount / Helper.ONE_XMR;
    }

    public int getMixin() {
        return mixin;
    }

    public void setMixin(int mixin) {
        this.mixin = mixin;
    }

    public PendingTransaction.Priority getPriority() {
        return priority;
    }

    public void setPriority(PendingTransaction.Priority priority) {
        this.priority = priority;
    }

    public UserNotes getUserNotes() {
        return userNotes;
    }

    public void setUserNotes(UserNotes userNotes) {
        this.userNotes = userNotes;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(dstAddr);
        out.writeLong(amount);
        out.writeInt(mixin);
        out.writeInt(priority.getValue());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("dstAddr:");
        sb.append(dstAddr);
        sb.append(",amount:");
        sb.append(amount);
        sb.append(",mixin:");
        sb.append(mixin);
        sb.append(",priority:");
        sb.append(priority);
        return sb.toString();
    }
}
