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

package net.mynero.wallet.util;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import net.mynero.wallet.R;
import net.mynero.wallet.model.WalletManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import timber.log.Timber;

public class Helper {
    static public final String NOCRAZYPASS_FLAGFILE = ".nocrazypass";

    static public final int XMR_DECIMALS = 12;
    static public final long ONE_XMR = Math.round(Math.pow(10, Helper.XMR_DECIMALS));

    static public final boolean SHOW_EXCHANGERATES = true;
    static public final int PERMISSIONS_REQUEST_CAMERA = 7;
    static final int HTTP_TIMEOUT = 5000;
    static private final String WALLET_DIR = "wallets";
    static private final String MONERO_DIR = "monero";
    private final static char[] HexArray = "0123456789ABCDEF".toCharArray();
    static public boolean ALLOW_SHIFT = false;
    static public int DISPLAY_DIGITS_INFO = 5;
    static private Animation ShakeAnimation;

    static public File getWalletRoot(Context context) {
        return getStorage(context, WALLET_DIR);
    }

    static public File getStorage(Context context, String folderName) {
        File dir = new File(context.getFilesDir(), folderName);
        if (!dir.exists()) {
            Timber.i("Creating %s", dir.getAbsolutePath());
            dir.mkdirs(); // try to make it
        }
        if (!dir.isDirectory()) {
            String msg = "Directory " + dir.getAbsolutePath() + " does not exist.";
            Timber.e(msg);
            throw new IllegalStateException(msg);
        }
        return dir;
    }

    static public boolean getCameraPermission(Activity context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                Timber.w("Permission denied for CAMERA - requesting it");
                String[] permissions = {Manifest.permission.CAMERA};
                context.requestPermissions(permissions, PERMISSIONS_REQUEST_CAMERA);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    static public boolean getCameraPermission(Activity context, ActivityResultLauncher<String> launcher) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                Timber.w("Permission denied for CAMERA - requesting it");
                launcher.launch(Manifest.permission.CAMERA);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    static public File getWalletFile(Context context, String aWalletName) {
        File walletDir = getWalletRoot(context);
        File f = new File(walletDir, aWalletName);
        Timber.d("wallet=%s size= %d", f.getAbsolutePath(), f.length());
        return f;
    }

    static public void showKeyboard(Activity act) {
        InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
        final View focus = act.getCurrentFocus();
        if (focus != null)
            imm.showSoftInput(focus, InputMethodManager.SHOW_IMPLICIT);
    }

    static public void hideKeyboard(Activity act) {
        if (act == null) return;
        if (act.getCurrentFocus() == null) {
            act.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow((null == act.getCurrentFocus()) ? null : act.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    static public void showKeyboard(Dialog dialog) {
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    static public void hideKeyboardAlways(Activity act) {
        act.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    static public BigDecimal getDecimalAmount(long amount) {
        return new BigDecimal(amount).scaleByPowerOfTen(-XMR_DECIMALS);
    }

    static public String getDisplayAmount(long amount) {
        return getDisplayAmount(amount, XMR_DECIMALS);
    }

    static public String getDisplayAmount(long amount, int maxDecimals) {
        // a Java bug does not strip zeros properly if the value is 0
        if (amount == 0) return "0.00";
        BigDecimal d = getDecimalAmount(amount)
                .setScale(maxDecimals, BigDecimal.ROUND_HALF_UP)
                .stripTrailingZeros();
        if (d.scale() < 2)
            d = d.setScale(2, BigDecimal.ROUND_UNNECESSARY);
        return d.toPlainString();
    }

    static public String getFormattedAmount(double amount, boolean isCrypto) {
        // at this point selection is XMR in case of error
        String displayB;
        if (isCrypto) {
            if ((amount >= 0) || (amount == 0)) {
                displayB = String.format(Locale.US, "%,.5f", amount);
            } else {
                displayB = null;
            }
        } else { // not crypto
            displayB = String.format(Locale.US, "%,.2f", amount);
        }
        return displayB;
    }

    static public String getDisplayAmount(double amount) {
        // a Java bug does not strip zeros properly if the value is 0
        BigDecimal d = new BigDecimal(amount)
                .setScale(XMR_DECIMALS, BigDecimal.ROUND_HALF_UP)
                .stripTrailingZeros();
        if (d.scale() < 1)
            d = d.setScale(1, BigDecimal.ROUND_UNNECESSARY);
        return d.toPlainString();
    }

    static public Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return BitmapFactory.decodeResource(context.getResources(), drawableId);
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    static private Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    static public String getUrl(String httpsUrl) {
        HttpsURLConnection urlConnection = null;
        try {
            URL url = new URL(httpsUrl);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(HTTP_TIMEOUT);
            urlConnection.setReadTimeout(HTTP_TIMEOUT);
            InputStreamReader in = new InputStreamReader(urlConnection.getInputStream());
            StringBuffer sb = new StringBuffer();
            final int BUFFER_SIZE = 512;
            char[] buffer = new char[BUFFER_SIZE];
            int length = in.read(buffer, 0, BUFFER_SIZE);
            while (length >= 0) {
                sb.append(buffer, 0, length);
                length = in.read(buffer, 0, BUFFER_SIZE);
            }
            return sb.toString();
        } catch (SocketTimeoutException ex) {
            Timber.w("C %s", ex.getLocalizedMessage());
        } catch (MalformedURLException ex) {
            Timber.e("A %s", ex.getLocalizedMessage());
        } catch (IOException ex) {
            Timber.e("B %s", ex.getLocalizedMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    static public void clipBoardCopy(Context context, String label, String text) {
        if (context != null) {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(label, text);
            clipboardManager.setPrimaryClip(clip);
            Toast.makeText(context, context.getText(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
        }
    }

    static public String getClipBoardText(Context context) {
        final ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        try {
            if (clipboardManager.hasPrimaryClip()
                    && clipboardManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                final ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                return item.getText().toString();
            }
        } catch (NullPointerException ex) {
            // if we have don't find a text in the clipboard
            return null;
        }
        return null;
    }

    static public Animation getShakeAnimation(Context context) {
        if (ShakeAnimation == null) {
            synchronized (Helper.class) {
                if (ShakeAnimation == null) {
                    ShakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake);
                }
            }
        }
        return ShakeAnimation;
    }

    public static String bytesToHex(byte[] data) {
        if ((data != null) && (data.length > 0))
            return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
        else return "";
    }

    public static byte[] hexToBytes(String hex) {
        final int len = hex.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    // TODO make the log levels refer to the  WalletManagerFactory::LogLevel enum ?
    static public void initLogger(Context context, int level) {
        String home = getStorage(context, MONERO_DIR).getAbsolutePath();
        WalletManager.initLogger(home + "/monerujo", "monerujo.log");
        if (level >= WalletManager.LOGLEVEL_SILENT)
            WalletManager.setLogLevel(level);
    }
}
