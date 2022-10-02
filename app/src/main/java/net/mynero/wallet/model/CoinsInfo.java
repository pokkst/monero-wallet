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

package net.mynero.wallet.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class CoinsInfo implements Parcelable, Comparable<CoinsInfo> {
    public static final Creator<CoinsInfo> CREATOR = new Creator<CoinsInfo>() {
        @Override
        public CoinsInfo createFromParcel(Parcel in) {
            return new CoinsInfo(in);
        }

        @Override
        public CoinsInfo[] newArray(int size) {
            return new CoinsInfo[size];
        }
    };

    static {
        System.loadLibrary("monerujo");
    }

    long globalOutputIndex;
    boolean spent;
    String keyImage;
    long amount;
    String hash;
    String pubKey;
    boolean unlocked;
    long localOutputIndex;

    public CoinsInfo(long globalOutputIndex, boolean spent, String keyImage, long amount, String hash, String pubKey, boolean unlocked, long localOutputIndex) {
        this.globalOutputIndex = globalOutputIndex;
        this.spent = spent;
        this.keyImage = keyImage;
        this.amount = amount;
        this.hash = hash;
        this.pubKey = pubKey;
        this.unlocked = unlocked;
        this.localOutputIndex = localOutputIndex;
    }

    protected CoinsInfo(Parcel in) {
        globalOutputIndex = in.readLong();
    }

    public long getGlobalOutputIndex() {
        return globalOutputIndex;
    }

    public boolean isSpent() {
        return spent;
    }

    public String getKeyImage() {
        return keyImage;
    }

    public String getHash() {
        return hash;
    }

    public long getAmount() {
        return amount;
    }

    public String getPubKey() {
        return pubKey;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public long getLocalOutputIndex() {
        return localOutputIndex;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeLong(globalOutputIndex);
    }

    @Override
    public int compareTo(CoinsInfo another) {
        long b1 = this.amount;
        long b2 = another.amount;
        if (b1 > b2) {
            return -1;
        } else if (b1 < b2) {
            return 1;
        } else {
            return this.hash.compareTo(another.hash);
        }
    }
}
