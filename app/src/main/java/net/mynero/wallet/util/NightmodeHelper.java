/*
 * Copyright (c) 2020 m2049r
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

package net.mynero.wallet.util;

import android.annotation.SuppressLint;

import androidx.appcompat.app.AppCompatDelegate;

import net.mynero.wallet.service.PrefService;

public class NightmodeHelper {
    public static DayNightMode getPreferredNightmode() {
        return DayNightMode.valueOf(PrefService.getInstance().getString(Constants.PREF_NIGHT_MODE, DayNightMode.NIGHT.name()));
    }

    public static void getAndSetPreferredNightmode() {
        setNightMode(getPreferredNightmode());
    }

    public static void setAndSavePreferredNightmode(DayNightMode mode) {
        PrefService.getInstance().edit().putString(Constants.PREF_NIGHT_MODE, mode.name()).apply();
        setNightMode(mode);
    }

    @SuppressLint("WrongConstant")
    public static void setNightMode(DayNightMode mode) {
        AppCompatDelegate.setDefaultNightMode(mode.getNightMode());
    }
}
