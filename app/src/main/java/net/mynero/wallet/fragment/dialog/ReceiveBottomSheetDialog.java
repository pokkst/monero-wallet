package net.mynero.wallet.fragment.dialog;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import net.mynero.wallet.model.TransactionInfo;
import net.mynero.wallet.model.Wallet;
import net.mynero.wallet.model.WalletManager;
import net.mynero.wallet.service.AddressService;
import net.mynero.wallet.service.HistoryService;
import net.mynero.wallet.util.DayNightMode;
import net.mynero.wallet.util.Helper;
import net.mynero.wallet.util.NightmodeHelper;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class ReceiveBottomSheetDialog extends BottomSheetDialogFragment {
    private TextView addressTextView = null;
    private ImageView addressImageView = null;
    private ImageButton copyAddressImageButton = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.receive_bottom_sheet_dialog, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addressImageView = view.findViewById(R.id.monero_qr_imageview);
        addressTextView = view.findViewById(R.id.address_textview);
        copyAddressImageButton = view.findViewById(R.id.copy_address_imagebutton);
        ImageView freshAddressImageView = view.findViewById(R.id.fresh_address_imageview);
        Wallet wallet = WalletManager.getInstance().getWallet();
        AddressService addressService = AddressService.getInstance();

        Subaddress addr = addressService.currentSubaddress();
        setAddress(addr);
        freshAddressImageView.setOnClickListener(view1 -> {
            final int maxSubaddresses = addressService.getLastUsedSubaddress() + wallet.getDeviceType().getSubaddressLookahead();
            if(wallet.getNumSubaddresses() < maxSubaddresses) {
                setAddress(AddressService.getInstance().freshSubaddress());
            } else {
                Toast.makeText(getContext(), getResources().getString(R.string.max_subaddresses_warning), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAddress(Subaddress subaddress) {
        addressTextView.setText(subaddress.getAddress());
        addressImageView.setImageBitmap(generate(subaddress.getAddress(), 256, 256));
        copyAddressImageButton.setOnClickListener(view1 -> Helper.clipBoardCopy(getContext(), "address", subaddress.getAddress()));
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