package com.oilpalm3f.gradingapp.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.oilpalm3f.gradingapp.R;
import com.oilpalm3f.gradingapp.cloudhelper.ApplicationThread;
import com.oilpalm3f.gradingapp.common.CommonUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;

public class UiUtils {

    public static final String LOG_TAG = UiUtils.class.getName();


    /*
     * If backgrount type 0(Zero) = Green
     *                    1        = Red*/
    public static void showCustomToastMessage(final String message, final Context context, final int backgroundColorType) {
        showCustomToastMessageLong(message,context, backgroundColorType, Toast.LENGTH_SHORT);
    }

    public static void showCustomToastMessageLong(final String message, final Context context, final int backgroundColorType, final int length) {
        ApplicationThread.uiPost(LOG_TAG, "show custom toast", new Runnable() {
            @Override
            public void run() {

                if (null == context)
                    return;

                LayoutInflater inflater = LayoutInflater.from(context);
                View toastRoot = inflater.inflate(R.layout.custom_toast, null);
                TextView messageToDisplay = (TextView) toastRoot.findViewById(R.id.toast_message);
                messageToDisplay.setBackground(context.getDrawable(backgroundColorType == 0 ? R.drawable.toast_msg_green : R.drawable.toast_bg));
                messageToDisplay.setText(message);
                Toast toast = new Toast(context);
                // Set layout to toast
                toast.setView(toastRoot);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_VERTICAL, 0, 0);
                toast.setDuration(length);
                toast.show();
            }
        });
    }

    public static void saveReceiptAsPdf(final String filePath, final View contentToRender, final ApplicationThread.OnComplete onComplete) {
        ApplicationThread.bgndPost(LOG_TAG, "", new Runnable() {
            @Override
            public void run() {
                String folderToSave = Environment.getExternalStorageDirectory().getAbsolutePath() + "/3F_OilPalm_Files/Receipts";

                File filesDirectory = new File(folderToSave);
                if (!filesDirectory.exists()) {
                    filesDirectory.mkdirs();
                }

                PdfDocument document = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    document = new PdfDocument();
                }

                int pageNumber = 1;
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(contentToRender.getWidth(),
                        contentToRender.getHeight(), pageNumber).create();

                PdfDocument.Page page = document.startPage(pageInfo);
                contentToRender.draw(page.getCanvas());

                document.finishPage(page);
                File outputFile = new File(folderToSave + "/" + filePath);
                try {
                    OutputStream out = new FileOutputStream(outputFile);
                    document.writeTo(out);
                    document.close();
                    out.close();
                    onComplete.execute(true, outputFile.getName(), "file saved success fully");
                    Log.v(LOG_TAG, "file saved success fully at " + outputFile.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                    onComplete.execute(true, "failed to save file", "failed to save file due to " + e.getMessage());
                }
            }
        });
    }

    private void revealShow(View rootView, final View view, boolean reveal, final AlertDialog dialog) {
        int w = view.getWidth();
        int h = view.getHeight();
        float maxRadius = (float) Math.sqrt(w * w / 4 + h * h / 4);

        if (reveal) {
            Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view,
                    w / 2, h / 2, 0, maxRadius);
            view.setVisibility(View.VISIBLE);
            revealAnimator.start();
        } else {
            Animator anim = ViewAnimationUtils.createCircularReveal(view, w / 2, h / 2, maxRadius, 0);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dialog.dismiss();
                    view.setVisibility(View.INVISIBLE);

                }
            });

            anim.start();
        }
    }

    public static ArrayAdapter createAdapter(Context mContext, LinkedHashMap<String, String> dataMap, String spinnerType){
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item,
                CommonUtils.fromMap(dataMap, spinnerType));
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return spinnerArrayAdapter;
    }
}


