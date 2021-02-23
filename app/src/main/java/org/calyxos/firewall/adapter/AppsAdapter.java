package org.calyxos.firewall.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.calyxos.firewall.R;
import org.calyxos.firewall.settings.SettingsManager;

import java.util.List;

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {

    private static final String TAG = AppsAdapter.class.getSimpleName();
    private Context mContext;
    private PackageManager mPackageManager;
    private SettingsManager mSettingsManager;
    private List<ApplicationInfo> mSystemApps;

    public AppsAdapter(Context context, PackageManager packageManager, List<ApplicationInfo> systemApps) {
        mSystemApps = systemApps;
        mContext = context;
        mPackageManager = packageManager;
        mSettingsManager = new SettingsManager(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_app_setting_accordion, parent, false);
        return new ViewHolder(view, mContext, mPackageManager, mSettingsManager);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplicationInfo apps = mSystemApps.get(position);
        holder.bind(apps);
    }

    @Override
    public int getItemCount() {
        if(mSystemApps != null)
            return mSystemApps.size();
        else return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        private Context mContext;
        private PackageManager mPackageManager;
        private SettingsManager mSettingsManager;
        private LinearLayout linearLayout;
        private SwitchCompat mMainToggle, mBackgroundToggle, mWifiToggle, mMobileToggle, mVpnToggle;
        private TextView appName, settingStatus;
        private ImageView appIcon, accordionIcon;

        private ApplicationInfo app;

        public ViewHolder(@NonNull View itemView, Context context, PackageManager packageManager, SettingsManager settingsManager) {
            super(itemView);

            mContext = context;
            mPackageManager = packageManager;
            mSettingsManager = settingsManager;

            mMainToggle = itemView.findViewById(R.id.main_toggle);
            mBackgroundToggle = itemView.findViewById(R.id.app_allow_background_toggle);
            mWifiToggle = itemView.findViewById(R.id.app_allow_wifi_toggle);
            mMobileToggle = itemView.findViewById(R.id.app_allow_mobile_toggle);
            mVpnToggle = itemView.findViewById(R.id.app_allow_vpn_toggle);

            appName = itemView.findViewById(R.id.app_name);
            settingStatus = itemView.findViewById(R.id.setting_status);
            appIcon = itemView.findViewById(R.id.app_icon);
            accordionIcon = itemView.findViewById(R.id.accordion_icon);

            linearLayout = itemView.findViewById(R.id.accordion_contents);

            itemView.setOnClickListener(this);
            accordionIcon.setOnClickListener(this);
            mMainToggle.setOnCheckedChangeListener(this);
            mBackgroundToggle.setOnCheckedChangeListener(this);
            mWifiToggle.setOnCheckedChangeListener(this);
            mMobileToggle.setOnCheckedChangeListener(this);
            mVpnToggle.setOnCheckedChangeListener(this);
        }

        public void bind(ApplicationInfo app) {
            this.app = app;
            try {
                //here just in case
                PackageInfo packageInfo = this.mPackageManager.getPackageInfo(app.packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            appIcon.setImageDrawable(app.loadIcon(mPackageManager));
            appName.setText(app.loadLabel(mPackageManager));

            //initialize toggle states
            //get background data status
            mBackgroundToggle.setChecked(mSettingsManager.isBlacklisted(app.uid));

            //get wifi status
            mWifiToggle.setChecked(mSettingsManager.getAppRestrictWifi(app.uid));

            //get mobile status
            mMobileToggle.setChecked(mSettingsManager.getAppRestrictCellular(app.uid));

            //get vpn status
            mVpnToggle.setChecked(mSettingsManager.getAppRestrictVpn(app.uid));

            //initialize main toggle
            if (mBackgroundToggle.isChecked() || mWifiToggle.isChecked() || mMobileToggle.isChecked() || mVpnToggle.isChecked())
                mMainToggle.setChecked(true);
            else mMainToggle.setChecked(false);

            //initialize settings status TODO: make a settings status text
            /*String statusText = "Allow ";
            if (!mBackgroundToggle.isChecked())
                statusText += "background data ";
            if (!mWifiToggle.isChecked())
                statusText += "wifi ";
            if (!mMobileToggle.isChecked())
                statusText += "mobile data ";
            if (!mVpnToggle.isChecked())
                statusText += "VPN";

            settingStatus.setText(statusText);*/
        }

        @Override
        public void onClick(View v) {
            if (linearLayout.getVisibility() == View.VISIBLE) {
                linearLayout.setVisibility(View.GONE);
                accordionIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_accordion_down));
            } else {
                linearLayout.setVisibility(View.VISIBLE);
                accordionIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_accordion_up));
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            switch (compoundButton.getId()) {
                case R.id.main_toggle:
                    if (compoundButton.isChecked()) {
                        //reset to default
                        mBackgroundToggle.setChecked(true);
                        mWifiToggle.setChecked(true);
                        mMobileToggle.setChecked(true);
                        mVpnToggle.setChecked(true);
                    } else {
                        //block everything
                        mBackgroundToggle.setChecked(false);
                        mWifiToggle.setChecked(false);
                        mMobileToggle.setChecked(false);
                        mVpnToggle.setChecked(false);
                    }
                    break;

                case R.id.app_allow_background_toggle:
                    if (compoundButton.isChecked())
                        mSettingsManager.setIsBlacklisted(app.uid, app.packageName, true);
                    else mSettingsManager.setIsBlacklisted(app.uid, app.packageName, false);
                    break;

                case R.id.app_allow_wifi_toggle:
                    if (compoundButton.isChecked())
                        mSettingsManager.setAppRestrictWifi(app.uid, true);
                    else mSettingsManager.setAppRestrictWifi(app.uid, false);
                    break;

                case R.id.app_allow_mobile_toggle:
                    if (compoundButton.isChecked())
                        mSettingsManager.setAppRestrictCellular(app.uid, true);
                    else mSettingsManager.setAppRestrictCellular(app.uid, false);
                    break;

                case R.id.app_allow_vpn_toggle:
                    if (compoundButton.isChecked())
                        mSettingsManager.setAppRestrictVpn(app.uid, true);
                    else mSettingsManager.setAppRestrictVpn(app.uid, false);
                    break;
            }
        }
    }
}