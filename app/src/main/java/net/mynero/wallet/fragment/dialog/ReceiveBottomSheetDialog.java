package net.mynero.wallet.fragment.dialog;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.mynero.wallet.R;
import net.mynero.wallet.data.Subaddress;
import net.mynero.wallet.service.AddressService;
import net.mynero.wallet.util.DayNightMode;
import net.mynero.wallet.util.Helper;
import net.mynero.wallet.util.NightmodeHelper;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class ReceiveBottomSheetDialog extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.receive_bottom_sheet_dialog, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView addressImageView = view.findViewById(R.id.monero_qr_imageview);
        TextView addressTextView = view.findViewById(R.id.address_textview);
        ImageButton copyAddressImageButton = view.findViewById(R.id.copy_address_imagebutton);

        Subaddress addr = AddressService.getInstance().currentSubaddress();
        addressTextView.setText(addr.getAddress());
        addressImageView.setImageBitmap(generate(addr.getAddress(), 256, 256));
        copyAddressImageButton.setOnClickListener(view1 -> Helper.clipBoardCopy(getContext(), "address", addr.getAddress()));
    }

    public Bitmap generate(String text, int width, int height) {
        if ((width <= 0) || (height <= 0)) return null;
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        try {
            BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    boolean night = NightmodeHelper.getPreferredNightmode() == DayNightMode.NIGHT;
                    if (bitMatrix.get(j, i)) {
                        pixels[i * width + j] = night ? 0xffffffff : 0x00000000;
                    } else {
                        pixels[i * height + j] = night ? getResources().getColor(R.color.oled_colorBackground) : 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException ex) {
            Timber.e(ex);
        }
        return null;
    }
}