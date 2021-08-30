package com.oilpalm3f.gradingapp.printer;

import android.content.Intent;
import android.hardware.usb.UsbDevice;

import com.android.print.sdk.PrinterInstance;

/**
 * Created by siva on 06/03/17.
 */

public interface IPrinterOpertion {
    public void open(Intent data);
    public void open(UsbDevice data);
    public void open(String data, boolean rePair);
    public void close();
    public void chooseDevice();
    public PrinterInstance getPrinter();
}
