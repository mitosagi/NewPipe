package org.schabi.newpipe.about;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.schabi.newpipe.R;
import org.schabi.newpipe.util.ThemeHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.schabi.newpipe.util.Localization.assureCorrectAppLanguage;

public class LicenseDialog extends DialogFragment {
    private static final String LICENSE = "license";

    public static LicenseDialog newInstance(final License license) {
        if (license == null) {
            throw new NullPointerException("license is null");
        }
        final LicenseDialog fragment = new LicenseDialog();
        final Bundle bundle = new Bundle();
        bundle.putParcelable(LICENSE, license);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * @param context the context to use
     * @param license the license
     * @return String which contains a HTML formatted license page
     * styled according to the context's theme
     */
    private static String getFormattedLicense(@NonNull final Context context,
                                              @NonNull final License license) {
        final StringBuilder licenseContent = new StringBuilder();
        final String webViewData;
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(
                    context.getAssets().open(license.getFilename()), StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {
                licenseContent.append(str);
            }
            in.close();

            // split the HTML file and insert the stylesheet into the HEAD of the file
            webViewData = licenseContent.toString().replace("</head>",
                    "<style>" + getLicenseStylesheet(context) + "</style></head>");
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Could not get license file: " + license.getFilename(), e);
        }
        return webViewData;
    }

    /**
     * @param context the context to use
     * @return String which is a CSS stylesheet according to the context's theme
     */
    private static String getLicenseStylesheet(final Context context) {
        final boolean isLightTheme = ThemeHelper.isLightThemeSelected(context);
        return "body{padding:12px 15px;margin:0;"
                + "background:#" + getHexRGBColor(context, isLightTheme
                ? R.color.light_license_background_color
                : R.color.dark_license_background_color) + ";"
                + "color:#" + getHexRGBColor(context, isLightTheme
                ? R.color.light_license_text_color
                : R.color.dark_license_text_color) + "}"
                + "a[href]{color:#" + getHexRGBColor(context, isLightTheme
                ? R.color.light_youtube_primary_color
                : R.color.dark_youtube_primary_color) + "}"
                + "pre{white-space:pre-wrap}";
    }

    /**
     * Cast R.color to a hexadecimal color value.
     *
     * @param context the context to use
     * @param color   the color number from R.color
     * @return a six characters long String with hexadecimal RGB values
     */
    private static String getHexRGBColor(final Context context, final int color) {
        return context.getResources().getString(color).substring(3);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        assureCorrectAppLanguage(requireContext());
        final License license = getArguments().getParcelable(LICENSE);

        final WebView webView = new WebView(requireActivity());
        final boolean isLightTheme = ThemeHelper.isLightThemeSelected(requireActivity());
        // to prevent flickering
        webView.setBackgroundColor(getResources().getColor(isLightTheme
                ? R.color.light_license_background_color
                : R.color.dark_license_background_color));
        final String webViewData = Base64.encodeToString(
                getFormattedLicense(requireActivity(), license)
                        .getBytes(StandardCharsets.UTF_8), Base64.NO_PADDING);
        webView.loadData(webViewData, "text/html; charset=UTF-8", "base64");

        final AlertDialog.Builder alert = new AlertDialog.Builder(requireActivity());
        alert.setTitle(license.getName());
        alert.setView(webView);
        alert.setNegativeButton(requireActivity().getString(R.string.finish),
                (dialog, which) -> dialog.dismiss());
        return alert.create();
    }
}
