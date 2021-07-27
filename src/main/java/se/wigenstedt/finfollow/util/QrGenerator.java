package se.wigenstedt.finfollow.util;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import se.wigenstedt.finfollow.services.WebClientService;

import java.awt.image.BufferedImage;

/**
 * Created by Julia Wigenstedt
 * Date: 2021-07-26
 * Time: 13:49
 * Project: FinFollow
 * Copyright: MIT
 */
public class QrGenerator {

    public static BufferedImage generateQR(String string) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(string, BarcodeFormat.QR_CODE, 200, 200);


        return MatrixToImageWriter.toBufferedImage(bitMatrix);

    }

}
